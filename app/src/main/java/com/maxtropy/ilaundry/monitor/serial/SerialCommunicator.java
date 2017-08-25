package com.maxtropy.ilaundry.monitor.serial;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.TooManyListenersException;
import java.util.concurrent.locks.ReentrantLock;

import purejavacomm.SerialPort;
import purejavacomm.SerialPortEvent;
import purejavacomm.SerialPortEventListener;

/**
 * 对于一个设备，一次只能发送一个请求，只有在请求返回或者超时之后才能继续对该设备发送请求。
 * 注意：调用返回的结果同发送的请求不处于同一线程。
 * 由于对于请求的发送一般都是在主线程，而串口信息的到来都是在串口各自的线程，因此需要先将处理相应逻辑的线程切换到
 * HandlerThread的worker线程，避免相应的多线程同步问题。
 * <p>
 * Created by wusp on 2017/3/31.
 */

public class SerialCommunicator {
    private static final String COMMAND_PORT = "ttyS5";
    private static final long REQUEST_TIME_OUT = 1000;

    private static final int ACTION_CLEAR_PORT = 0x00;
    private static final int ACTION_OPEN_PORT = 0x01;
    private static final int ACTION_RESTART_PORT = 0x02;
    private static final int ACTION_SEND_REQUEST = 0x03;
    private static final int ACTION_RECEIVE_RESPONSE = 0x04;

    private static final byte ACK = 0x06;
    private static final byte NAK = 0x15;
    private static final byte INV = 0x09;

    private static SerialCommunicator mInstance = new SerialCommunicator();

    private SerialPort mPort;
    private Handler mHandler;
    private HandlerThread worker;
    private volatile boolean isPortOpened = false;
    private volatile boolean canWrite = false;

    // 握有lock的线程才可以对消息队列进行添加消息操作
    // 保证消息发送能够有自己的消息处理逻辑（不会被别人干扰）
    private ReentrantLock msgLock = new ReentrantLock();
    // 当前正在服务的线程。当对方发回ACK时会被唤醒
    private Thread currentServingThread;
    // 0x00 表示没有在等待的数据包
    private byte waitResponseCode = (byte)0xff;
    private SerialPacket lastReq;
    private int retryTimes = 0;

    public void lock() {
        msgLock.lock();
    }

    public void unlock() {
        msgLock.unlock();
    }

    // This listener need to be assigned after created
    private volatile SerialResponseListener responseListener = new SerialResponseListener() {
        @Override
        public void onResponse(SerialPacket msg) {
            Log.e(Const.TAG, "SerialResponseListener not assigned error.");
        }
        @Override
        public void onError(String reason) {
            Log.e(Const.TAG, "SerialResponseListener not assigned error.");
        }
    };
    private boolean responseListenerAssignedFlag = false;

    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            onTimeOut();
        }
    };

    public void setResponseListener(SerialResponseListener responseListener) {
        if(responseListenerAssignedFlag)
            Log.w(Const.TAG, "ResponseListener has been assigned.");
        responseListenerAssignedFlag = true;
        this.responseListener = responseListener;
    }

    private SerialCommunicator() {
        worker = new HandlerThread("SerialCommunicator");
        worker.start();
        mHandler = new Handler(worker.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case ACTION_OPEN_PORT:
                        onOpenPort();
                        break;
                    case ACTION_CLEAR_PORT:
                        onClearPort();
                        break;
                    case ACTION_RESTART_PORT:
                        onClearPort();
                        onOpenPort();
                        break;
                    case ACTION_SEND_REQUEST:
                        if (msg.obj != null) {
                            writeDataToBuffer((byte[]) msg.obj);
                        }
                        break;
                    case ACTION_RECEIVE_RESPONSE:
                        if (msg.obj != null) {
                            onReceiveResponse((SerialPacket) msg.obj);
                        }
                        break;
                }
                return true;
            }
        });
    }

    public static SerialCommunicator getInstance() {
        return mInstance;
    }

    public void openPort() {
        mHandler.obtainMessage(ACTION_OPEN_PORT).sendToTarget();
        Log.d(Const.TAG, "open port and start to run.");
    }

    public void restart() {
        mHandler.obtainMessage(ACTION_RESTART_PORT).sendToTarget();
    }

    private void onClearPort() {
        Log.i(Const.TAG, "Clearing port: " + COMMAND_PORT);
        if (mPort != null) {
            mPort.removeEventListener();
            mPort.close();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        isPortOpened = false;
        canWrite = false;
        responseListener = null;
    }

    private void onOpenPort() {
        if (isPortOpened) {
            return;
        }
        Log.i(Const.TAG, "Opening port: " + COMMAND_PORT);
        mPort = RS485Tool.openSerial(COMMAND_PORT);
        if (mPort != null) {
            try {
                isPortOpened = true;
                mPort.addEventListener(mListener);
                // sendTestPacket();
                Log.i(Const.TAG, "port " + COMMAND_PORT + " opened");
            } catch (TooManyListenersException e) {
                e.printStackTrace();
                onError("Too many listeners Exception on add listener to port.");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isReady() {
        return !(mPort == null || !canWrite || !isPortOpened);
    }

    void sendTestPacket() throws IOException {
        byte[] data = new byte[]{0x02, 0x02, 0x10, 0x00, 0x10};
        mPort.getOutputStream().write(data);
        mPort.getOutputStream().flush();
    }

    void resetTimeout() {
        mHandler.removeCallbacks(timeoutRunnable);
        mHandler.postDelayed(timeoutRunnable, REQUEST_TIME_OUT);
    }

    private void sendControl(byte ctrl) {
        switch(ctrl) {
            case ACK:
                Log.d(Const.TAG, ">> ACK");
                break;
            case NAK:
                Log.d(Const.TAG, ">> NAK");
                break;
            case INV:
                Log.d(Const.TAG, ">> INV");
                break;
        }
        byte[] buffer = new byte[]{ctrl};
        resetTimeout();
        writeDataToBuffer(buffer);
    }

    /**
     * 预设发送数据包函数：会发送最近发送的一个数据包。用于初次发送或者失败重新发送
     */
    private void sendPacket() {
        if(retryTimes == 7) {
            onError("Retried 7 times.");
            return;
        }
        retryTimes ++;
        Log.d(Const.TAG, ">> " + lastReq.getClass().getSimpleName() + " : " + bufferToStr(lastReq.getData()));
        writeDataToBuffer(lastReq.toBytes());
        resetTimeout();
        /*
        try {
            Thread.sleep(5);
        } catch(Exception e) {
            e.printStackTrace();
        }*/
    }

    public boolean sendPacket(SerialPacket req, Thread callingThread) {
        return sendPacket(req, callingThread, (byte)0x00);
    }

    public boolean sendPacket(SerialPacket req, Thread callingThread, byte waitResponseCode) {
        if (req == null || req.getData() == null || mPort == null || !canWrite || !isPortOpened) {
            Log.e(Const.TAG, "ERROR WHEN SENDING A PACKET: PORT NOT READY OR EMPTY PACKET");
            return false;
        }
        if(currentServingThread != null) {
            // 某个线程没有拿到锁就发了消息，导致其他线程介入并且在之前逻辑没走完就发了新的消息
            Log.e(Const.TAG, "ERROR! Not synchronized packet sending. Get a lock before send the packet and put the thread to sleep!");
            // return false;
        }
        this.waitResponseCode = waitResponseCode;
        currentServingThread = callingThread;
        lastReq = req;
        retryTimes = 0;
        nakCount = 0;
        sendPacket();
        synchronized (callingThread) {
            try {
                Log.v(Const.TAG, "wait");
                callingThread.wait();
            } catch(InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * 将已经转换成比特数组的Mod-Bus request写入到串口的输出缓冲区。
     *
     * @param data 要被传输的数据
     */
    private void writeDataToBuffer(byte[] data) {
        if (data == null)
            return;
        try {
            if(mPort.getOutputStream() == null) {
                Log.e(Const.TAG, "no output stream");
                return;
            }
            mPort.getOutputStream().write(data);
            mPort.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            onError("Exception occurs on sending Modbus Request.");
        }
    }

    private void onReceiveResponse(SerialPacket res) {
        responseListener.onResponse(res);
    }

    /**
     * 运行过程中出现重要错误.
     */
    private void onError(String reason) {
        Log.e(Const.TAG, "Error: " + reason);
        responseListener.onError(reason);
        // restart();
    }

    void wakeThread() {
        synchronized (currentServingThread) {
            waitResponseCode = (byte)0xff;
            Log.v(Const.TAG, "notify");
            currentServingThread.notify();
            currentServingThread = null;
        }
    }

    ArrayDeque<Byte> buff = new ArrayDeque<>();

    void onMessageArrives(byte[] rawMsg) {
        mHandler.removeCallbacks(timeoutRunnable);
        retryTimes = 0;
        if(rawMsg.length == 1) {
            switch(rawMsg[0]) {
                case ACK:
                    // ACK: 唤醒business logic线程，令其可以继续逻辑或者释放锁
                    Log.d(Const.TAG, "<< ACK");
                    retryTimes = 0;
                    if(waitResponseCode == 0x00) {
                        wakeThread();
                    }
                    break;
                case NAK:
                    // NAK
                    Log.w(Const.TAG, "<< NAK : " + lastReq.getTag());
                    sendPacket();
                    break;
                case INV:
                    // INV
                    Log.w(Const.TAG, "<< INV : " + lastReq.getTag());
                    sendPacket();
                    break;
            }
            return;
        }
        // TODO 没有判断包与包之间超时，即waitResponse有可能导致死锁
        // 收到完整包
        final SerialPacket res = new SerialPacket(rawMsg);
        if(!res.isValid()) {
            sendControl(INV);
            return;
        }
        sendControl(ACK);
        onReceiveResponse(res);
        if(waitResponseCode == res.getData()[2]) {
            wakeThread();
        } else if(waitResponseCode != (byte)0xff)
            Log.w(Const.TAG, "Wait response code mismatch: " + waitResponseCode + " : " + res.getData()[2]);
    }


    private SerialPortEventListener mListener = new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            if (serialPortEvent.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY) {
                canWrite = true;
            }
            if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                try {
                    byte[] buffer;
                    buffer = new byte[mPort.getInputStream().available()];
                    int n = mPort.getInputStream().read(buffer);
                    if (n != buffer.length) {
                        onError("Read buffer size: " + n + " is not equal to available length: " + buffer.length);
                    }
                    String tmps = "<< data:";
                    for(int i =0; i < n; ++i)
                        tmps += " " + buffer[i];
                    Log.d(Const.TAG, tmps);
                    for(int i = 0; i < n; ++i)
                        buff.addLast(buffer[i]);
                    while(!buff.isEmpty()) {
                        byte head = buff.getFirst();
                        if(head == ACK || head == NAK || head == INV) {
                            buffer = new byte[1];
                            buffer[0] = buff.pollFirst();
                            onMessageArrives(buffer);
                        } else if(head == 0x02) {
                            if (buff.size() > 1) {
                                Byte tmp = buff.pollFirst();
                                byte len = buff.peekFirst();
                                buff.addFirst(tmp);
                                if (buff.size() >= len + 3) {
                                    buffer = new byte[len + 3];
                                    for (int i = 0; i < len + 3; ++i)
                                        buffer[i] = buff.pollFirst();
                                    onMessageArrives(buffer);
                                } else
                                    break;
                            } else
                                break;
                        } else {
                            Log.e(Const.TAG, "INVALID CONTROL: " + buff.pollFirst());
                            sendControl(INV);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    int nakCount = 0;

    void onTimeOut() {
        // TODO no resend mechanism enforced
        // restart();
        if(++nakCount == 7){
            onError("Timeout 7 times.");
            nakCount = 0;
            wakeThread();
        }
        else{
        // Try use resend packet instead of sending NAK when packet timeout occurs
            sendPacket();
//            sendControl(NAK);
        }
    }

    String bufferToStr(byte[] buf) {
        String tmp = "";
        for(int i = 0; i < buf.length; ++i) {
            int anUnsignedByte = (int) buf[i] & 0xff;
            tmp += anUnsignedByte + " ";
        }
        return tmp;
    }

}

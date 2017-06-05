package com.maxtropy.ilaundry.monitor.serial;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.model.SerialPacket;

import java.io.IOException;
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
    private byte waitResponseCode = 0x00;
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

    void setResponseListener(SerialResponseListener responseListener) {
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
            } catch (TooManyListenersException e) {
                e.printStackTrace();
                onError("Too many listeners Exception on add listener to port.");
            }
        }
    }

    /**
     * 预设发送数据包函数：会发送最近发送的一个数据包。用于初次发送或者失败重新发送
     */
    private void sendPacket() {
        if(retryTimes == 3) {
            onError("Retried 3 times.");
            return;
        }
        retryTimes ++;
        writeDataToBuffer(lastReq.getData());
        mHandler.postDelayed(timeoutRunnable, REQUEST_TIME_OUT);
    }

    public boolean sendPacket(SerialPacket req, Thread callingThread) {
        return sendPacket(req, callingThread, (byte)0x00);
    }

    public boolean sendPacket(SerialPacket req, Thread callingThread, byte waitResponseCode) {
        if (req == null || req.getData() != null || mPort == null || !canWrite || !isPortOpened) {
            Log.e(Const.TAG, "ERROR WHEN SENDING A PACKET: PORT NOT READY OR EMPTY PACKET");
            return false;
        }
        if(currentServingThread != null) {
            // 某个线程没有拿到锁就发了消息，导致其他线程介入并且在之前逻辑没走完就发了新的消息
            Log.e(Const.TAG, "ERROR! Not synchronized packet sending. Get a lock before send the packet and put the thread to sleep!");
            return false;
        }
        try {
            this.waitResponseCode = waitResponseCode;
            currentServingThread = callingThread;
            lastReq = req;
            sendPacket();
            callingThread.wait();
            return true;
        } catch(InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将已经转换成比特数组的Mod-Bus request写入到串口的输出缓冲区。
     *
     * @param data 要被传输的数据
     */
    private void writeDataToBuffer(byte[] data) {
        if (data == null) {
            return;
        }
        for (byte b : data) {
            Log.d(Const.TAG, "data: " + b);
        }
        try {
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
        restart();
    }

    private SerialPortEventListener mListener = new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            if (serialPortEvent.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY) {
                canWrite = true;
            }
            if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                try {
                    Log.d(Const.TAG, "Data_Available. " + mPort.getInputStream().available());
                    byte[] buffer;
                    buffer = new byte[mPort.getInputStream().available()];
                    int n = mPort.getInputStream().read(buffer);
                    if (n != buffer.length) {
                        onError("Read buffer size: " + n + " is not equal to available length: " + buffer.length);
                    }
                    // TODO 包也许没有完整传送
                    mHandler.removeCallbacks(timeoutRunnable);
                    retryTimes = 0;
                    if(buffer.length == 1) {
                        switch(buffer[0]) {
                            case 0x06:
                                // ACK: 唤醒business logic线程，令其可以继续逻辑或者释放锁
                                if(waitResponseCode == 0x00) {
                                    currentServingThread.notify();
                                    currentServingThread = null;
                                }
                                break;
                            case 0x15:
                                // NAK
                                Log.w(Const.TAG, "NAK : " + lastReq.getTag());
                                sendPacket();
                                break;
                            case 0x09:
                                // INV
                                Log.w(Const.TAG, "INV : " + lastReq.getTag());
                                sendPacket();
                                break;
                        }
                        return;
                    }
                    // TODO 没有判断包与包之间超时，即waitResponse有可能导致死锁
                    // 收到完整包
                    final SerialPacket res = new SerialPacket(buffer);
                    if(!res.isValid()) {
                        return;
                    }
                    mHandler.obtainMessage(ACTION_RECEIVE_RESPONSE, res).sendToTarget();
                    if(waitResponseCode == res.getData()[2]) {
                        waitResponseCode = 0x00;
                        currentServingThread.notify();
                        currentServingThread = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    void onTimeOut() {
        // TODO no resend mechanism enforced
        restart();
    }

}

package com.maxtropy.ilaundry.monitor.serial;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.model.SerialPacket;

import java.io.IOException;
import java.util.TooManyListenersException;

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
    private volatile SerialResponseListener responseListener;  //已发送的请求同收到的请求进行配对
    private SerialOTRunnable mOTRunnable = new SerialOTRunnable();

    private SerialCommunicator() {
        worker = new HandlerThread("SerialCommunicator");
        worker.start();
        mHandler = new Handler(worker.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case ACTION_OPEN_PORT:
                        openPort();
                        break;
                    case ACTION_CLEAR_PORT:
                        clearPort();
                        break;
                    case ACTION_RESTART_PORT:
                        clearPort();
                        openPort();
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

    public void run() {
        mHandler.obtainMessage(ACTION_OPEN_PORT).sendToTarget();
        Log.d(Const.TAG, "open port and start to run.");
    }

    public void restart() {
        mHandler.obtainMessage(ACTION_RESTART_PORT).sendToTarget();
    }

    private void clearPort() {
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

    private void openPort() {
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

    public boolean sendModbusRequest(SerialPacket req) {
        if (req == null || req.getData() != null || mPort == null || !canWrite || !isPortOpened) {
            return false;
        }
        byte[] data = null;
        writeDataToBuffer(data);
        mOTRunnable.setRequest(req);
        mHandler.postDelayed(mOTRunnable, REQUEST_TIME_OUT);
        return true;
    }

    /**
     * 将已经转换成比特数组的Mod-Bus request写入到串口的输出缓冲区。
     *
     * @param data
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

    /**
     * 接收到已经编码的Mod-Bus response, 将其提交给该返回消息的监听者，并移除超时触发器和监听者。
     *
     * @param res
     */
    private void onReceiveResponse(SerialPacket res) {
        if (responseListener != null) {
            mHandler.removeCallbacks(mOTRunnable);
            responseListener.onResponse(res);
        } else {
            Log.e(Const.TAG, "responseListener is null.");
        }
    }

    /**
     * 运行过程中出现重要错误.
     */
    private void onError(String reason) {
        Log.e(Const.TAG, "Error: " + reason);
        mOTRunnable.run();
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
                    // TODO maybe the packet is not sent in a whole
                    if(buffer.length == 1) {
                        return;
                    }
                    final SerialPacket res = new SerialPacket(buffer);

                    mHandler.obtainMessage(ACTION_RECEIVE_RESPONSE, res).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 用来激发检查已经超时的请求，由于在结果成功返回时本超时检查任务会从Handler中移除，因此被激发则说明请求已经超时。
     */
    private class SerialOTRunnable implements Runnable {
        private SerialPacket req;

        public void setRequest(SerialPacket req) {
            this.req = req;
        }

        @Override
        public void run() {
            if (responseListener != null) {
                responseListener.onOverTime(req);
                restart();
            }
        }
    }
}

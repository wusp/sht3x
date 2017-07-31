package com.maxtropy.ilaundry.monitor.gpio;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.softwinner.Gpio;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 通过工作线程对GPIO进行采样并将结果上报至GPIOCenter处理, 数据上传部分直接拿处理后的结果进行上报以尽量避免多线程问题
 * <p>
 * Created by wutian on 2017/6/07.
 */

public class GPIOCenter {

    private static GPIOCenter mInstance = new GPIOCenter();

    private Handler mHandler;
    private HandlerThread worker;
    private GPIOSampleRunnable mGpioSampleRunnable;

    // 其实用set也没关系。万一以后要用到key呢
    private Map<String, GPIOHandler> handlerMap;

    private boolean startSample = false;

    private GPIOCenter() {
        worker = new HandlerThread("GPIOCenter");
        worker.start();
        mHandler = new Handler(worker.getLooper());
        mGpioSampleRunnable = new GPIOSampleRunnable();
        handlerMap = new HashMap<>();
    }

    public static GPIOCenter getInstance() {
        return mInstance;
    }

    public void run() {
        Log.d(Const.TAG, "start GPIO sampling.");
        startSample = true;
        mHandler.postDelayed(mGpioSampleRunnable, Const.GPIO_START_SAMPLE_TIME_OUT);
    }

    public void registerPath(String path, Runnable runnable) {
        handlerMap.put(path, new GPIOHandler(path, runnable));
    }

    public void setValue(String path, int value) {
        Gpio.setRaw(path, String.valueOf(value));
    }

    public void disableCardReader() {
        setValue(Const.GPIO_CARD_READER_ENABLE, 0);
        Log.d("Card reader", "Card reader disabled.");
    }

    public void enableCardReader() {
        setValue(Const.GPIO_CARD_READER_ENABLE, 1);
        Log.d("Card reader", "Card reader enabled.");
    }

    /**
     * 用来激发每秒一次的GPIO采样
     */
    private class GPIOSampleRunnable implements Runnable {
        @Override
        public void run() {
            if(startSample) {
                for(GPIOHandler handler: handlerMap.values()) {
                    handler.doSampling();
                }
            }
            mHandler.postDelayed(mGpioSampleRunnable, Const.GPIO_SAMPLE_TIME_OUT);
        }
    }


}

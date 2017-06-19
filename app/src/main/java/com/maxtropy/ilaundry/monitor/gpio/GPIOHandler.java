package com.maxtropy.ilaundry.monitor.gpio;

import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.softwinner.Gpio;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Gerald on 6/15/2017.
 */

public class GPIOHandler {

    String path;
    Runnable callback;
    private ReentrantLock lock;

    private volatile int[] sampleWindow= new int[Const.windowsSize];
    private int windowCount = 0;
    private int sampleSum = 0;

    public GPIOHandler(String path, Runnable callback) {
        this.path = path;
        this.callback = callback;
        lock = new ReentrantLock();
    }

    public void doSampling() {
        try {
            int sampleValue = Gpio.readRaw(path);
            Log.v(Const.TAG, "GPIO sample result: " + sampleValue);
            lock.lock();

            // Use array to simulate a FIFO window.
            sampleSum -= sampleWindow[windowCount];
            sampleSum += sampleValue;
            sampleWindow[windowCount] = sampleValue;
            windowCount = (windowCount +1) % Const.windowsSize;
            if(sampleSum >= Const.threshold && sampleSum < Const.limit){
                callback.run();
            }
        } finally {
            lock.unlock();
        }
    }

}
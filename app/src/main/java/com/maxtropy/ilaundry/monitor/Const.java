package com.maxtropy.ilaundry.monitor;

/**
 * Created by wusp on 2017/4/27.
 */

public class Const {
    public static final String TAG = "iLaundryMonitor";

    public static final String GPIO_COIN = "/sys/class/gpio_sw/PH11/data"; //1:close 0:open
    public static final String GPIO_CARD_READER = "/sys/class/gpio_sw/PH11/data"; //1:close 0:open

    public static final long GPIO_SAMPLE_TIME_OUT = 4;
    public static final long GPIO_START_SAMPLE_TIME_OUT = 10000;
    public static final int windowsSize = 500;
    public static final int threshold = 190;
    public static final int limit = 300;
}

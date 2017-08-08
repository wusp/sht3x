package com.maxtropy.ilaundry.monitor;

/**
 * Created by wusp on 2017/4/27.
 */

public class Const {
    public static final String TAG = "iLaundryMonitor";

    public static final String GPIO_CARD_READER_ENABLE = "/sys/class/gpio_sw/PH16/data";    //1:enable card reader 0:disable card reader
    public static final String GPIO_CARD_READER = "/sys/class/gpio_sw/PH11/data";           //0_1_0_1 signal input of card payment event

    public static final String GPIO_MDC_POWER_ENABLE = "/sys/class/gpio_sw/PH18/data";      //Power mangement of MDC board, 1:Power on MDC 0: Power off MDC . Need to power on MDC when App start running.
    public static final String GPIO_MDC_AVAILABLE = "/sys/class/gpio_sw/PH15/data";         //1:MDC not available(machine is in busy status) 0:MDC is available(not in busy status), check manual for details.

    public static final long GPIO_SAMPLE_TIME_OUT = 4;
    public static final long GPIO_START_SAMPLE_TIME_OUT = 10000;
    public static final int windowsSize = 500;
    public static final int threshold = 190;
    public static final int limit = 300;

    public static final int[] mdcCycleMapping = {-1, 2, 3, 1, 4};

    public static final String emptyOrderId = "0";
    public static final String cardWashOrderId = "-1";

    public static final int reserverTime = 5 * 60 * 1000;
}

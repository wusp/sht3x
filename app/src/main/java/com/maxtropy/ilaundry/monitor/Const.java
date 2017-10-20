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

    public static final int[] mdcFuncToCycleMapping = {             // 洗衣/烘干程序(1-8) -> MDC cycle
            -1,              // 异常cycle
            2, 3, 1, 4,      // 服务器下发内容 1 - normal / 2 - perm press / 3 - heavy / 4 - delicate
            10, 30, 40, 50   // 烘干机时长 5 - 10 / 6 - 30 / 7 - 40 / 8 - 50 min
    };

    public static final int[] centurionDefaultCycleToServerCycleMapping = {
            -1,             // 异常cycle
            3, 3, 6, 9,    // 洗衣机模式 3 - normal & cold & enable heavy (according to heavy cycle)/ 3 - normal & cold / 6 - normal & cold / 9 - delicate & cold
            10, 30, 40, 50  //烘干机时长，通过编程调整烘干机时间
    };


    public static final int[] mdcCycleToConfigurationMapping = {   // MDC cycle(2314) -> Frontload Cycle configuration
            -1,              // 异常cycle
            5,               // 滚筒洗衣机洗衣程序 2洗3清
            4,               // 滚筒洗衣机洗衣程序 1洗3清
            3,               // 滚筒洗衣机洗衣程序 1洗2清
            4                // 滚筒洗衣机洗衣程序 1洗3清
    };

    public static final String emptyOrderId = "0";
    public static final String cardWashOrderId = "-1";

    public static final int reserverTime = 3 * 60 * 1000;
}

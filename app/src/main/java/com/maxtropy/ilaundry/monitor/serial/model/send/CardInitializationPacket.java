package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

/**
 *
 * Used on Power-Up mode to initialize FEC and CR
 *
 * Created by Gerald on 6/3/2017.
 * 3.6.7
 */
public class CardInitializationPacket extends SerialPacket {
    public final static byte code = 0x13;
    public static final int CR_DRIVEN_VEND_OPTION = 0x10;
    public static final int FEC_DRIVEN_VEND_OPTION = 0x20;

    public CardInitializationPacket(int manufacturerId, int firmwareVersion) {
        tag = getClass().getSimpleName();
        data = new byte[7];
        data[0] = code;
        putThree(1, manufacturerId);
        putShort(4, firmwareVersion);
        data[6] = CR_DRIVEN_VEND_OPTION;
    }

}

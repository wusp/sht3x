package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.Global;
import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

/**
 * Created by Gerald on 6/3/2017.
 * 3.6.7
 */
public class CardInsertedTopOffPacket extends SerialPacket {
    public final static byte code = 0x42;

    public CardInsertedTopOffPacket() {
        initalization(Global.vendPrice * 2, Global.vendPrice);
    }

    public CardInsertedTopOffPacket(int cardBalance, int vendPrice) {
        initalization(cardBalance, vendPrice);
    }

    void initalization(int cardBalance, int vendPrice) {
        tag = this.getClass().getName();
        switch (Global.systemType) {
            case MDC:
                data = new byte[6];
                data[0] = code;
                data[1] = (byte)(cardBalance / 0x100);
                data[2] = (byte)(cardBalance % 0x100);
                data[3] = (byte)(vendPrice / 0x100);
                data[4] = (byte)(vendPrice % 0x100);
                data[5] = Global.TOP_OFF_TIME;
                break;
            default:
                data = new byte[6];
                data[0] = code;
                data[1] = (byte)(cardBalance / 0x100);
                data[2] = (byte)(cardBalance % 0x100);
        }
    }
}

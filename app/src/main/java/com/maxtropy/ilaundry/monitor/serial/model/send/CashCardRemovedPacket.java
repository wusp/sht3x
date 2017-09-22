package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.Global;
import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

/**
 * Cash Card Removed Packet would tell the FEC the remain balance.
 * Created by Gerald on 6/3/2017.
 * 3.6.7
 */
public class CashCardRemovedPacket extends SerialPacket {
    public final static byte code = 0x41;

    void init(int cardBalance, int vendPrice) {
        tag = "cash valued card inserted packet";
        switch (Global.systemType) {
            case MDC:
                data = new byte[5];
                data[0] = code;
                data[1] = (byte)(cardBalance / 0x100);
                data[2] = (byte)(cardBalance % 0x100);
                data[3] = (byte)(vendPrice / 0x100);
                data[4] = (byte)(vendPrice % 0x100);
                break;
            default:
                data = new byte[3];
                data[0] = code;
                data[1] = (byte)(cardBalance / 0x100);
                data[2] = (byte)(cardBalance % 0x100);
        }
    }

    public CashCardRemovedPacket() {
        init(Global.vendPrice, Global.vendPrice);
    }

}

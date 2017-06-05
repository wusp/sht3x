package com.maxtropy.ilaundry.monitor.model.send;

import com.maxtropy.ilaundry.monitor.model.SerialPacket;

/**
 * Created by Gerald on 6/3/2017.
 * 3.6.7
 */
public class CashCardRemovedPacket extends SerialPacket {
    public final static byte code = 0x41;

    public CashCardRemovedPacket(int cardBalance, int vendPrice) {
        tag = "cash valued card inserted packet";
        data = new byte[5];
        data[0] = code;
        data[1] = (byte)(cardBalance / 0x100);
        data[2] = (byte)(cardBalance % 0x100);
        data[3] = (byte)(vendPrice / 0x100);
        data[4] = (byte)(vendPrice % 0x100);
    }
}

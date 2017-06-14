package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

/**
 * Created by Gerald on 6/3/2017.
 * 3.6.7
 */
public class CardInsertedPacket extends SerialPacket {
    public final static byte code = 0x40;

    public CardInsertedPacket() {
        init(5000, 100);
    }

    public CardInsertedPacket(int cardBalance, int vendPrice) {
        init(cardBalance, vendPrice);
    }

    void init(int cardBalance, int vendPrice) {
        tag = "cash valued card inserted packet";
        data = new byte[5];
        data[0] = code;
        data[1] = (byte)(cardBalance / 0x100);
        data[2] = (byte)(cardBalance % 0x100);
        data[3] = (byte)(vendPrice / 0x100);
        data[4] = (byte)(vendPrice % 0x100);
    }
}

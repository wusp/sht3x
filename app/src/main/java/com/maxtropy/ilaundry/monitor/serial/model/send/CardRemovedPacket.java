package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

/**
 * General Card Removed Packet, not report remain balance
 * Created by Gerald on 6/3/2017.
 * 3.6.9
 */
public class CardRemovedPacket extends SerialPacket {
    public final static byte code = 0x41;

    public CardRemovedPacket() {
        tag = "card removed";
        data = new byte[1];
        data[0] = code;
    }
}

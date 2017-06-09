package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

/**
 * Created by Gerald on 6/3/2017.
 */
public class StatusRequestPacket extends SerialPacket {

    public StatusRequestPacket(boolean cardInReader) {
        tag = "Status Request";
        data = new byte[2];
        data[0] = 0x10;
        data[1] = (byte)(cardInReader ? 0x80 : 0x00);
    }
}

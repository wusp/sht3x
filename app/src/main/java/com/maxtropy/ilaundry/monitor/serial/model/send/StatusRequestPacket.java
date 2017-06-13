package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.Global;
import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

import static com.maxtropy.ilaundry.monitor.Global.SystemType.MDC;

/**
 * Created by Gerald on 6/3/2017.
 */
public class StatusRequestPacket extends SerialPacket {

    public StatusRequestPacket(boolean cardInReader) {
        tag = "Status Request";
        data = new byte[2];
        data[0] = (byte)(Global.systemType == MDC ? 0x10 : 0x16);
        data[1] = (byte)(cardInReader ? 0x80 : 0x00);
    }
}

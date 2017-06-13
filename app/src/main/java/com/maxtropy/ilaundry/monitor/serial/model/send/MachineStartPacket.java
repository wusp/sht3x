package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.Global;
import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

/**
 * Created by Gerald on 6/3/2017.
 * 3.6.9
 */
public class MachineStartPacket extends SerialPacket {
    public final static byte code = 0x48;

    public MachineStartPacket() {
        tag = "machine start";
        data = new byte[1];
        data[0] = code;
    }

}

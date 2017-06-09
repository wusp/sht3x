package com.maxtropy.ilaundry.monitor.serial.model.receive;

import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Gerald on 6/3/2017.
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class MachineStatusPacket extends SerialPacket {

    public final static byte code = 0x11;

    int machineType, keypadData, cycleType, commandToReader, machineStatus;
    int remainMinute, remainSecond;

    public boolean isMode(int bit) {
        return (machineStatus & (1 << bit-1) ) != 0;
    }

}

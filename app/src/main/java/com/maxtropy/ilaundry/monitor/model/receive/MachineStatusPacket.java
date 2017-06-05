package com.maxtropy.ilaundry.monitor.model.receive;

import com.maxtropy.ilaundry.monitor.model.SerialPacket;

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

    public MachineStatusPacket(SerialPacket packet) {
        data = packet.getData();
        machineType = data[1];
        keypadData = data[2];
        cycleType = data[3];
        commandToReader = data[4];
        machineStatus = data[5] * 0x100 + data[6];
        remainMinute = data[9];
        remainSecond = data[10];
    }
}

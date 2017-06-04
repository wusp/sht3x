package com.maxtropy.ilaundry.monitor.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Gerald on 6/3/2017.
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class MachineStatusPacket extends SerialPacket {

    int machineType, keypadData, cycleType, commandToReader, machineStatus;
    int remainMinute, remainSecond;

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

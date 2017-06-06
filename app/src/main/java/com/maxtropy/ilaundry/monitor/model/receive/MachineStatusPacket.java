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

    public final static byte code = 11;

    int machineType, keypadData, cycleType, commandToReader, machineStatus;
    int remainMinute, remainSecond;

    public boolean isMode(int bit) {
        return (machineStatus & (1 << bit-1) ) != 0;
    }

    public MachineStatusPacket(SerialPacket packet) {
        data = packet.getData();
        machineType = data[2];
        keypadData = data[3];
        cycleType = data[4];
        commandToReader = data[5];
        machineStatus = data[6] * 0x100 + data[7];
        remainMinute = data[10];
        remainSecond = data[11];
    }
}

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
        machineType = data[3];
        keypadData = data[4];
        cycleType = data[5];
        commandToReader = data[6];
        machineStatus = data[7] * 0x100 + data[8];
        remainMinute = data[11];
        remainSecond = data[12];
    }
}

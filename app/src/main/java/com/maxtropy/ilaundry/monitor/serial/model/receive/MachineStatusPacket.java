package com.maxtropy.ilaundry.monitor.serial.model.receive;

import com.maxtropy.ilaundry.monitor.Global;
import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.maxtropy.ilaundry.monitor.Global.SystemType.MDC;

/**
 * Created by Gerald on 6/3/2017.
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class MachineStatusPacket extends SerialPacket {

    public static final byte code = getCode();

    public static byte getCode() {
        return (byte)(Global.systemType == MDC ? 0x11 : 0x17);
    }

    int machineType, keypadData, cycleType, commandToReader, machineStatus;
    int remainMinute, remainSecond;

    public boolean isMode(int bit) {
        return (machineStatus & (1 << bit) ) != 0;
    }

}

package com.maxtropy.ilaundry.monitor.serial.builder.machine_status;

import com.maxtropy.ilaundry.monitor.Global;
import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;
import com.maxtropy.ilaundry.monitor.serial.model.receive.MachineStatusPacket;

/**
 * Created by Gerald on 6/8/2017.
 */

public abstract class MachineStatusBuilder {

    public static MachineStatusPacket build(SerialPacket packet) {
        switch(Global.systemType) {
            case MDC:
                return new MDCMachineStatusBuilder().doBuild(packet);
            case Centurion:
                return new CenturionMachineStatusBuilder().doBuild(packet);
        }
        return null;
    }

    public abstract MachineStatusPacket doBuild(SerialPacket packet);
}

package com.maxtropy.ilaundry.monitor.serial.builder;

import com.maxtropy.ilaundry.monitor.Global;
import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;
import com.maxtropy.ilaundry.monitor.serial.model.receive.MachineStatusPacket;

/**
 * Created by Gerald on 6/8/2017.
 */

public abstract class MachineStatusBuilder {

    public static MachineStatusBuilder getBuilder() {
        switch(Global.systemType) {
            case MDC:
                return new MDCMachineStatusBuilder();
            case Centurion:
                return new CenturionMachineStatusBuilder();
        }
        return null;
    }

    public abstract MachineStatusPacket build(SerialPacket packet);
}

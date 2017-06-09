package com.maxtropy.ilaundry.monitor;

import com.maxtropy.ilaundry.monitor.roc.message.receive.MachineTypeResponse;
import com.maxtropy.ilaundry.monitor.serial.SerialCommunicator;

/**
 * Created by Gerald on 6/8/2017.
 */

public class Global {

    public enum MachineType {
        Undefined(0),
        TopLoadWasher(1),
        FrontLoadWasher(2),
        WasherExtractor(3),
        Tumbler(4),
        Dryer(5);
        private int type;
        MachineType(int type) {
            this.type = type;
        }
    }

    public enum SystemType {
        Undefined(0),
        MDC(1),
        Centurion(2);
        private int type;
        SystemType(int type) {
            this.type = type;
        }
    }

    /*
    public static MachineType machineType = MachineType.Undefined;
    public static SystemType systemType = SystemType.Undefined;
    */

    public static MachineType machineType = MachineType.TopLoadWasher;
    public static SystemType systemType = SystemType.MDC;

    public static boolean initialized() {
        return machineType != MachineType.Undefined;
    }

    public static void initialMachineType(MachineTypeResponse response) {
        machineType = MachineType.values()[response.getMachineType()];
        systemType = SystemType.values()[response.getSystemType()];
    }

}

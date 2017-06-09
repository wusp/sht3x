package com.maxtropy.ilaundry.monitor.serial.builder.machine_status;

import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;
import com.maxtropy.ilaundry.monitor.serial.model.receive.MachineStatusPacket;

/**
 * Created by Gerald on 6/8/2017.
 */

public class MDCMachineStatusBuilder extends MachineStatusBuilder {

    @Override
    public MachineStatusPacket doBuild(SerialPacket packet) {
        MachineStatusPacket res = new MachineStatusPacket();
        byte[] data = packet.getData();
        res.setMachineType(data[3]);
        res.setKeypadData(data[4]);
        res.setCycleType(data[5]);
        res.setCommandToReader(data[6]);
        res.setMachineStatus(data[7]);
        res.setRemainMinute(data[11]);
        res.setRemainSecond(data[12]);
        return res;
    }
}

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
public class MachineControlInitializationPacket extends SerialPacket {

    public static final byte code = 0x14;

}

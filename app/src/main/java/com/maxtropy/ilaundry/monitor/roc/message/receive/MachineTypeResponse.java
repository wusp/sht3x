package com.maxtropy.ilaundry.monitor.roc.message.receive;

import com.maxtropy.mockingbirds.annotation.MessageKey;
import com.maxtropy.mockingbirds.annotation.MessageType;
import com.maxtropy.mockingbirds.protocol.AbstractMessageV2;
import com.maxtropy.mockingbirds.protocol.MessageConstV2;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 带洗衣模式的洗衣请求
 * Created by Yankai.
 */
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@ToString
@MessageType(subType = MachineTypeResponse.SUB_TYPE, type = MessageConstV2.TYPE_REQUEST)
public class MachineTypeResponse extends AbstractMessageV2 {

    public static final int SUB_TYPE = 0x499107;

    @MessageKey(1)
    private int machineType;

    @MessageKey(2)
    private int systemType;

}

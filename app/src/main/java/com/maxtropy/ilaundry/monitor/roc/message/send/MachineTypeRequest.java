package com.maxtropy.ilaundry.monitor.roc.message.send;

import com.maxtropy.mockingbirds.annotation.MessageType;
import com.maxtropy.mockingbirds.protocol.AbstractMessageV2;
import com.maxtropy.mockingbirds.protocol.MessageConstV2;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 剩余时间报文
 * Created by Yankai.
 */
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@ToString
@MessageType(subType = MachineTypeRequest.SUB_TYPE, type = MessageConstV2.TYPE_REQUEST)
public class MachineTypeRequest extends AbstractMessageV2 {

    static final int SUB_TYPE = 0x499106;
    static final String topic = "ilaundry_machine_request";

    public MachineTypeRequest() {
        setTopic(topic);
    }

}

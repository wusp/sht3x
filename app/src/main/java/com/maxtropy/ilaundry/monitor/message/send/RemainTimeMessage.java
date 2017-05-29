package com.maxtropy.ilaundry.monitor.message.send;

import com.maxtropy.mockingbirds.annotation.MessageKey;
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
@MessageType(subType = RemainTimeMessage.SUB_TYPE, type = MessageConstV2.TYPE_REPORT)
public class RemainTimeMessage extends AbstractMessageV2 {

    static final int SUB_TYPE = 0x499103;
    static final String topic = "ilaundry_wash_remain";

    @MessageKey(1)
    private int remainingTime;

    public RemainTimeMessage() {
        setTopic(topic);
    }

}

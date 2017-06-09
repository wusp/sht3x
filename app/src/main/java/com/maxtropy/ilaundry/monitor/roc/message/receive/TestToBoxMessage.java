package com.maxtropy.ilaundry.monitor.roc.message.receive;

import com.maxtropy.mockingbirds.annotation.MessageKey;
import com.maxtropy.mockingbirds.annotation.MessageType;
import com.maxtropy.mockingbirds.protocol.AbstractMessageV2;
import com.maxtropy.mockingbirds.protocol.MessageConstV2;

import lombok.EqualsAndHashCode;
import lombok.Data;
import lombok.ToString;

/**
 * 测试用报文
 * Created by Yankai.
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString
@MessageType(subType = TestToBoxMessage.SUB_TYPE, type = MessageConstV2.TYPE_REPORT)
public class TestToBoxMessage extends AbstractMessageV2 {
    public static final int SUB_TYPE = 0x499102;
    @MessageKey(1)
    private String timestamp;
}

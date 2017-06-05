package com.maxtropy.ilaundry.monitor.message.send;

import com.maxtropy.mockingbirds.annotation.MessageKey;
import com.maxtropy.mockingbirds.annotation.MessageType;
import com.maxtropy.mockingbirds.protocol.AbstractMessageV2;
import com.maxtropy.mockingbirds.protocol.MessageConstV2;

/**
 * 可预约状态修改报文
 * Created by Yankai.
 */
@MessageType(subType = WasherErrorMessage.SUB_TYPE, type = MessageConstV2.TYPE_REPORT)
public class WasherErrorMessage extends AbstractMessageV2 {
    static final int SUB_TYPE = 0x499105;
    static final String topic = "ilaundry_device_error";
    @MessageKey(1)
    private String reason;
    public WasherErrorMessage(String reason) {
        this.reason = reason;
        setTopic(topic);
    }
}

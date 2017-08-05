package com.maxtropy.ilaundry.monitor.roc.message.send;

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
    @MessageKey(2)
    private String orderId;
    public WasherErrorMessage(String reason, String orderId) {
        this.reason = reason;
        this.orderId = orderId;
        setTopic(topic);
    }
}

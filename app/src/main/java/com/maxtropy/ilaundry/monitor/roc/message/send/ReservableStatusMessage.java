package com.maxtropy.ilaundry.monitor.roc.message.send;

import com.maxtropy.mockingbirds.annotation.MessageKey;
import com.maxtropy.mockingbirds.annotation.MessageType;
import com.maxtropy.mockingbirds.protocol.AbstractMessageV2;
import com.maxtropy.mockingbirds.protocol.MessageConstV2;

/**
 * 可预约状态修改报文
 * Created by Yankai.
 */
@MessageType(subType = ReservableStatusMessage.SUB_TYPE, type = MessageConstV2.TYPE_REPORT)
public class ReservableStatusMessage extends AbstractMessageV2 {
    static final int SUB_TYPE = 0x499104;
    static final String topic = "ilaundry_device_resevable";
    @MessageKey(1)
    private int reservable;
    public ReservableStatusMessage(int reservable) {
        this.reservable = reservable;
        setTopic(topic);
    }
}

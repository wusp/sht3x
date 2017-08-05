package com.maxtropy.ilaundry.monitor.roc.message.send;

import com.maxtropy.ilaundry.monitor.service.ConfigService;
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
    public enum Status{
        available(0),
        in_use(1),
        offline(2),
        error(3),
        reserved_deprecated(4),
        card_reader_reserved(5),
        machine_running(6);
        int value;
        Status(int value) {
            this.value = value;
        }
    }
    @MessageKey(1)
    private int reservable;
    @MessageKey(2)
    private String orderId;
    public ReservableStatusMessage(Status status) {
        this.reservable = status.value;
        this.orderId = ConfigService.getInstance().getOrderId();
        setTopic(topic);
    }
    public ReservableStatusMessage(int reservable) {
        this.reservable = reservable;
        this.orderId = ConfigService.getInstance().getOrderId();
        setTopic(topic);
    }
}

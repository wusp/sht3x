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
@MessageType(subType = ReserveRequest.SUB_TYPE, type = MessageConstV2.TYPE_REQUEST)
public class ReserveRequest extends AbstractMessageV2 {

    public static final int SUB_TYPE = 0x499108;

    @MessageKey(1)
    private int reserveState;

    @MessageKey(2)
    private String orderId;
}

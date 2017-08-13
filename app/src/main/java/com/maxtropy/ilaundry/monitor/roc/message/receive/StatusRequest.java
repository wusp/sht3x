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
 * 请求Box上报洗衣机状态
 * Created by Yankai.
 */
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@ToString
@MessageType(subType = StatusRequest.SUB_TYPE, type = MessageConstV2.TYPE_REQUEST)
public class StatusRequest extends AbstractMessageV2 {
    public static final int SUB_TYPE = 0x499109;
}

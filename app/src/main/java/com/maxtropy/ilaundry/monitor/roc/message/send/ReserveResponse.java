package com.maxtropy.ilaundry.monitor.roc.message.send;

import com.maxtropy.mockingbirds.annotation.MessageKey;
import com.maxtropy.mockingbirds.annotation.MessageType;
import com.maxtropy.mockingbirds.protocol.AbstractMessageV2;
import com.maxtropy.mockingbirds.protocol.MessageConstV2;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by wusp on 2017/10/11.
 */
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@ToString
@NoArgsConstructor
@MessageType(subType = ReserveResponse.SUB_TYPE, type = MessageConstV2.TYPE_RESPONSE)
public class ReserveResponse extends AbstractMessageV2 {

    public static final int SUB_TYPE = 0x49910a;

    @MessageKey(1)
    private Integer status;

    @MessageKey(2)
    private Boolean reserved;
}

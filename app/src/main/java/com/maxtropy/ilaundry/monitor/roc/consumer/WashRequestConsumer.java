package com.maxtropy.ilaundry.monitor.roc.consumer;

import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.roc.message.receive.WashRequest;
import com.maxtropy.ilaundry.monitor.roc.Roc;
import com.maxtropy.ilaundry.monitor.service.SerialService;
import com.maxtropy.mockingbirds.protocol.MessageAnnotationRegistry;
import com.maxtropy.roc.IMessageConsumer;
import com.maxtropy.roc.RocChannel;
import com.maxtropy.roc.model.MessageData;

/**
 * Created by Gerald on 5/28/2017.
 */

public class WashRequestConsumer implements IMessageConsumer {

    Roc roc;

    public WashRequestConsumer(Roc roc) {
        this.roc = roc;
    }

    @Override
    public void consume(MessageData data, RocChannel rocChannel) {
        try {
            final WashRequest response = MessageAnnotationRegistry.getInstance().fromData(data.getMessageV2(), WashRequest.class);
            if(response == null) {
                Log.d(Const.TAG, "[washRequest] null");
                return;
            }
            Log.d(Const.TAG, "[washRequest] " + response.getMode());
            SerialService.getInstance().initiateWechatWash(response.getMode(), response.getPrice());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer consumeType() {
        return WashRequest.SUB_TYPE;
    }
}

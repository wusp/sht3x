package com.maxtropy.ilaundry.monitor.roc.consumer;

import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.roc.Roc;
import com.maxtropy.ilaundry.monitor.roc.message.receive.StatusRequest;
import com.maxtropy.ilaundry.monitor.service.SerialService;
import com.maxtropy.mockingbirds.protocol.MessageAnnotationRegistry;
import com.maxtropy.roc.IMessageConsumer;
import com.maxtropy.roc.RocChannel;
import com.maxtropy.roc.model.MessageData;

/**
 * Created by Gerald on 5/28/2017.
 */

public class StatusRequestConsumer implements IMessageConsumer {

    public StatusRequestConsumer() {
    }

    @Override
    public void consume(MessageData data, RocChannel rocChannel) {
        try {
            final StatusRequest response = MessageAnnotationRegistry.getInstance().fromData(data.getMessageV2(), StatusRequest.class);
            if(response == null) {
                Log.d(Const.TAG, "[statusRequest] null");
                return;
            }
            Log.d(Const.TAG, "[statusRequest]");
            SerialService.getInstance().reportReserveStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer consumeType() {
        return StatusRequest.SUB_TYPE;
    }
}

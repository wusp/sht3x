package com.maxtropy.ilaundry.monitor.roc.consumer;

import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.roc.Roc;
import com.maxtropy.ilaundry.monitor.roc.message.receive.ReserveRequest;
import com.maxtropy.ilaundry.monitor.roc.message.receive.WashRequest;
import com.maxtropy.ilaundry.monitor.service.SerialService;
import com.maxtropy.mockingbirds.protocol.MessageAnnotationRegistry;
import com.maxtropy.roc.IMessageConsumer;
import com.maxtropy.roc.RocChannel;
import com.maxtropy.roc.model.MessageData;

/**
 * Created by Gerald on 5/28/2017.
 */

public class ReserveRequestConsumer implements IMessageConsumer {

    Roc roc;

    public ReserveRequestConsumer(Roc roc) {
        this.roc = roc;
    }

    @Override
    public void consume(MessageData data, RocChannel rocChannel) {
        try {
            final ReserveRequest request = MessageAnnotationRegistry.getInstance().fromData(data.getMessageV2(), ReserveRequest.class);
            if(request == null) {
                Log.d(Const.TAG, "[reserveRequest] null");
                return;
            }
            Log.d(Const.TAG, "[reserveRequest] " + request.getReserveState());
            SerialService.getInstance().changeReserveState(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer consumeType() {
        return ReserveRequest.SUB_TYPE;
    }
}

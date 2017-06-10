package com.maxtropy.ilaundry.monitor.roc.consumer;

import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.Global;
import com.maxtropy.ilaundry.monitor.roc.Roc;
import com.maxtropy.ilaundry.monitor.roc.message.receive.MachineTypeResponse;
import com.maxtropy.ilaundry.monitor.roc.message.receive.WashRequest;
import com.maxtropy.ilaundry.monitor.service.SerialService;
import com.maxtropy.mockingbirds.protocol.MessageAnnotationRegistry;
import com.maxtropy.roc.IMessageConsumer;
import com.maxtropy.roc.RocChannel;
import com.maxtropy.roc.model.MessageData;

/**
 * Created by Gerald on 5/28/2017.
 */

public class MachineTypeConsumer implements IMessageConsumer {

    public MachineTypeConsumer() {
    }

    @Override
    public void consume(MessageData data, RocChannel rocChannel) {
        try {
            Log.d(Const.TAG, "[ROC] << machine type");
            final MachineTypeResponse response = MessageAnnotationRegistry.getInstance().fromData(data.getMessageV2(), MachineTypeResponse.class);
            if(response == null) {
                Log.d(Const.TAG, "[washRequest] null");
                return;
            }
            Global.initialMachineType(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer consumeType() {
        return MachineTypeResponse.SUB_TYPE;
    }
}

package com.maxtropy.ilaundry.monitor.roc.consumer;

import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.roc.message.receive.TestToBoxMessage;
import com.maxtropy.mockingbirds.protocol.MessageAnnotationRegistry;
import com.maxtropy.roc.IMessageConsumer;
import com.maxtropy.roc.RocChannel;
import com.maxtropy.roc.model.MessageData;

/**
 * Created by Gerald on 5/28/2017.
 */

public class TestToBoxConsumer implements IMessageConsumer {
    @Override
    public void consume(MessageData data, RocChannel rocChannel) {
        try {
            final TestToBoxMessage response = MessageAnnotationRegistry.getInstance().fromData(data.getMessageV2(), TestToBoxMessage.class);
            if(response != null) {
                Log.d(Const.TAG, "[test to box message] " + response.getTimestamp());
            } else {
                Log.d(Const.TAG, "[test to box message] null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer consumeType() {
        return TestToBoxMessage.SUB_TYPE;
    }
}

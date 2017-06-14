package com.maxtropy.ilaundry.monitor.roc.consumer;

import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.roc.message.receive.TestToBoxMessage;
import com.maxtropy.ilaundry.monitor.serial.model.send.CardInsertedPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.CardRemovedPacket;
import com.maxtropy.ilaundry.monitor.service.SerialService;
import com.maxtropy.mockingbirds.protocol.MessageAnnotationRegistry;
import com.maxtropy.roc.IMessageConsumer;
import com.maxtropy.roc.RocChannel;
import com.maxtropy.roc.model.MessageData;

import org.json.JSONObject;

/**
 * Created by Gerald on 5/28/2017.
 */

public class TestToBoxConsumer implements IMessageConsumer {

    SerialService serial;

    public TestToBoxConsumer() {
        serial = SerialService.getInstance();
    }

    @Override
    public void consume(MessageData data, RocChannel rocChannel) {
        try {
            final TestToBoxMessage response = MessageAnnotationRegistry.getInstance().fromData(data.getMessageV2(), TestToBoxMessage.class);
            if(response != null) {
                Log.d(Const.TAG, "[test to box message] " + response.getTimestamp());
            } else {
                Log.d(Const.TAG, "[test to box message] null");
            }
            JSONObject json = new JSONObject(response.getTimestamp());
            byte code = (byte)(json.getInt("code"));
            if(code == CardInsertedPacket.code)
                serial.sendSingleRequest(new CardInsertedPacket());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer consumeType() {
        return TestToBoxMessage.SUB_TYPE;
    }
}

package com.maxtropy.ilaundry.monitor.roc.consumer;

import android.filterfw.core.Program;
import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.gpio.GPIOCenter;
import com.maxtropy.ilaundry.monitor.roc.Roc;
import com.maxtropy.ilaundry.monitor.roc.message.receive.TestToBoxMessage;
import com.maxtropy.ilaundry.monitor.roc.message.send.WasherErrorMessage;
import com.maxtropy.ilaundry.monitor.serial.model.send.CardInsertedPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.CardRemovedPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.CashCardRemovedPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.ProgrammingDataPacket;
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
            String packet = json.getString("packet");
            if(packet.equals(CardInsertedPacket.class.getSimpleName()))
                serial.sendSingleRequest(new CardInsertedPacket());
            if(packet.equals(CashCardRemovedPacket.class.getSimpleName())) {
                serial.removeCard();
                serial.sendSingleRequest(new CashCardRemovedPacket());
            }
            if(packet.equals(CardRemovedPacket.class.getSimpleName())) {
                serial.removeCard();
                serial.sendSingleRequest(new CardRemovedPacket());
            }
            if(packet.equals(ProgrammingDataPacket.class.getSimpleName())) {
                serial.program(json.getInt("arg1"));
            }
            if(packet.equals("Error")) {
                Roc.getInstance().sendMessage(new WasherErrorMessage("Test Error Message", Const.emptyOrderId));
            }
            if(packet.equals("GPIO")) {
                GPIOCenter.getInstance().setValue(Const.GPIO_CARD_READER_ENABLE, json.getInt("arg1"));
            }
            if(packet.equals("ec")) {
                serial.showErrorCode(json.getInt("arg1"));
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

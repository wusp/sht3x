
package com.maxtropy.ilaundry.monitor.service;

import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.Global;
import com.maxtropy.ilaundry.monitor.roc.message.send.RemainTimeMessage;
import com.maxtropy.ilaundry.monitor.roc.message.send.WasherErrorMessage;
import com.maxtropy.ilaundry.monitor.serial.builder.machine_status.MachineStatusBuilder;
import com.maxtropy.ilaundry.monitor.serial.model.receive.MachineStatusPacket;
import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.AddTimePacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.AudioBeepRequest;
import com.maxtropy.ilaundry.monitor.serial.model.send.CardInsertedPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.CardInsertedTopOffPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.CardRemovedPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.CashCardRemovedPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.MachineStartPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.StatusRequestPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.ProgrammingDataPacket;
import com.maxtropy.ilaundry.monitor.roc.Roc;
import com.maxtropy.ilaundry.monitor.serial.SerialCommunicator;
import com.maxtropy.ilaundry.monitor.serial.SerialResponseListener;
import com.maxtropy.ilaundry.monitor.serial.model.send.VendPricePacket;

/**
 * Created by Gerald on 6/3/2017.
 */

public class SerialService implements SerialResponseListener {

    SerialCommunicator serial;

    static SerialService instance = new SerialService();

    public static SerialService getInstance() {
        return instance;
    }

    Roc roc;

    private SerialService() {
        serial = SerialCommunicator.getInstance();
        serial.openPort();
        serial.setResponseListener(this);
        roc = Roc.getInstance();
    }

    boolean programming = false;

    MachineStatusPacket machineStatus = null;

    boolean almostDoneNotified = false;
    boolean doneNotified = true;

    boolean cardInReader = false;

    public void powerupMode() {
        serial.lock();
        try {
            // 发送完需要等待MachineStatusPacket返回
            serial.sendPacket(new StatusRequestPacket(cardInReader), Thread.currentThread(), MachineStatusPacket.code);
            // 只需要等待ACK即可
            serial.sendPacket(new ProgrammingDataPacket(2), Thread.currentThread());
        } finally {
            serial.unlock();
        }
    }

    public void insertCard() {
        cardInReader = true;
    }

    public void removeCard() {
        cardInReader = false;
    }

    public MachineStatusPacket getMachineStatus() {
        try {
            serial.lock();
            serial.sendPacket(new StatusRequestPacket(cardInReader), Thread.currentThread(), MachineStatusPacket.code);
            serial.sendPacket(new VendPricePacket(), Thread.currentThread());
            return machineStatus;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            serial.unlock();
        }
    }

    public void initiateWechatWash(int cycle, int price) {
        try {
            serial.lock();
            insertCard();
            serial.sendPacket(new ProgrammingDataPacket(cycle), Thread.currentThread());
            Thread.sleep(2000);
            removeCard();
            serial.sendPacket(new CardRemovedPacket(), Thread.currentThread());
            Thread.sleep(200);

            serial.sendPacket(new StatusRequestPacket(cardInReader), Thread.currentThread(), MachineStatusPacket.code);
            serial.sendPacket(new VendPricePacket(), Thread.currentThread());

            insertCard();
            serial.sendPacket(new CardInsertedPacket(Global.vendPrice * 2, Global.vendPrice), Thread.currentThread());
            serial.sendPacket(new AudioBeepRequest(4), Thread.currentThread());
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            serial.unlock();
        }
    }

    public void additionalTime() {
        try {
            insertCard();
            getMachineStatus();
            Thread.sleep(500);
            sendSingleRequest(new CardInsertedTopOffPacket());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendSingleRequest(SerialPacket msg) {
        serial.lock();
        try {
            if(msg.getTag() == ProgrammingDataPacket.class.getName())
                programming = true;
            if(msg.getTag() == CardRemovedPacket.class.getName())
                programming = false;
            serial.sendPacket(msg, Thread.currentThread());
        } finally {
            serial.unlock();
        }
    }

    @Override
    public void onResponse(SerialPacket msg) {
        // On serial message received
        byte[] data = msg.getData();
        if(data[2] == MachineStatusPacket.code) {
            Log.d(Const.TAG, "<< Machine status received");
            machineStatus = MachineStatusBuilder.build(msg);
            onStatusUpdate(machineStatus);
        }
    }

    @Override
    public void onError(String reason) {
        Roc.getInstance().sendMessage(new WasherErrorMessage(reason));
    }

    public boolean isReady() {
        return serial.isReady();
    }

    synchronized void onStatusUpdate(MachineStatusPacket status) {
        if(status.isMode(6)) {
            onError("Error mode reported by washer");
            return;
        }
        switch(status.getCommandToReader()) {
            case 0x46:
                // TODO start washing
                sendSingleRequest(new MachineStartPacket());
                removeCard();
                sendSingleRequest(new CashCardRemovedPacket());
                break;
            case 0x47:
                // deduct topoff vend
                sendSingleRequest(new AddTimePacket());
                removeCard();
                sendSingleRequest(new CashCardRemovedPacket());
                break;
        }
        if(status.isMode(5) && !doneNotified) {
            doneNotified = true;
            roc.sendMessage(new RemainTimeMessage(0));
        }
        int remainSec = status.getRemainMinute() * 60 + status.getRemainSecond();
        if(status.isMode(4) && !almostDoneNotified && remainSec <= 300) {
            almostDoneNotified = true;
            roc.sendMessage(new RemainTimeMessage(remainSec));
        }
    }
}

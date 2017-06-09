
package com.maxtropy.ilaundry.monitor.service;

import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.roc.message.send.RemainTimeMessage;
import com.maxtropy.ilaundry.monitor.roc.message.send.WasherErrorMessage;
import com.maxtropy.ilaundry.monitor.serial.model.receive.MachineStatusPacket;
import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.CardInsertedPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.CardRemovedPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.CashCardRemovedPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.MachineStartPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.StatusRequestPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.TumblerProgrammingDataPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.VendPricePacket;
import com.maxtropy.ilaundry.monitor.roc.Roc;
import com.maxtropy.ilaundry.monitor.serial.SerialCommunicator;
import com.maxtropy.ilaundry.monitor.serial.SerialResponseListener;

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

    public void powerupMode() {
        serial.lock();
        try {
            // 发送完需要等待MachineStatusPacket返回
            serial.sendPacket(new StatusRequestPacket(), Thread.currentThread(), MachineStartPacket.code);
            // 只需要等待ACK即可
            serial.sendPacket(new TumblerProgrammingDataPacket(2), Thread.currentThread());
        } finally {
            serial.unlock();
        }
    }

    public synchronized MachineStatusPacket getMachineStatus() {
        if(programming)
            return null;
        serial.lock();
        try {
            // 发送完需要等待MachineStatusPacket返回
            serial.sendPacket(new StatusRequestPacket(), Thread.currentThread(), MachineStatusPacket.code);
            // 只需要等待ACK即可
            serial.sendPacket(new VendPricePacket(), Thread.currentThread());
            return machineStatus;
        } finally {
            serial.unlock();
        }
    }

    public void initiateWechatWash(int cycle) {
        sendSingleRequest(new TumblerProgrammingDataPacket(cycle));
        sendSingleRequest(new CardRemovedPacket());
        sendSingleRequest(new CardInsertedPacket(0, 0));
    }

    public void sendSingleRequest(SerialPacket msg) {
        serial.lock();
        try {
            if(msg.getTag() == TumblerProgrammingDataPacket.class.getName())
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
        switch(data[2]) {
            case MachineStatusPacket.code:
                Log.d(Const.TAG, "<< Machine status received");
                machineStatus = new MachineStatusPacket(msg);
                onStatusUpdate(machineStatus);
                break;
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
        if(status.getCommandToReader() == 0x46) {
            // TODO start washing
            sendSingleRequest(new MachineStartPacket());
            sendSingleRequest(new CashCardRemovedPacket(0, 0));
        }
        if(status.isMode(5) && !doneNotified) {
            doneNotified = true;
            roc.sendMessage(new RemainTimeMessage(0));
        }
        if(status.isMode(4) && !almostDoneNotified) {
            almostDoneNotified = true;
            roc.sendMessage(new RemainTimeMessage(status.getRemainMinute() * 60 + status.getRemainSecond()));
        }
    }
}

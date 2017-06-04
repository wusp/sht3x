package com.maxtropy.ilaundry.monitor.service;

import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.model.MachineStatusPacket;
import com.maxtropy.ilaundry.monitor.model.SerialPacket;
import com.maxtropy.ilaundry.monitor.model.StatusRequestPacket;
import com.maxtropy.ilaundry.monitor.model.VendPricePacket;
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

    private SerialService() {
        serial = SerialCommunicator.getInstance();
        serial.openPort();
    }

    MachineStatusPacket machineStatus = null;

    public MachineStatusPacket getMachineStatus() {
        serial.lock();
        try {
            // 发送完需要等待MachineStatusPacket返回
            serial.sendPacket(new StatusRequestPacket(), Thread.currentThread(), true);
            // 只需要等待ACK即可
            serial.sendPacket(new VendPricePacket(), Thread.currentThread(), false);
            return machineStatus;
        } finally {
            serial.unlock();
        }
    }

    @Override
    public void onResponse(SerialPacket msg) {
        // On serial message received
        byte[] data = msg.getData();
        switch(data[0]) {
            case MachineStatusPacket.code:
                Log.d(Const.TAG, "Machine status received");
                machineStatus = new MachineStatusPacket(msg);
                break;
        }
    }
}


package com.maxtropy.ilaundry.monitor.service;

import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.Global;
import com.maxtropy.ilaundry.monitor.gpio.GPIOCenter;
import com.maxtropy.ilaundry.monitor.roc.message.receive.ReserveRequest;
import com.maxtropy.ilaundry.monitor.roc.message.send.RemainTimeMessage;
import com.maxtropy.ilaundry.monitor.roc.message.send.ReservableStatusMessage;
import com.maxtropy.ilaundry.monitor.roc.message.send.WasherErrorMessage;
import com.maxtropy.ilaundry.monitor.serial.builder.machine_status.MachineStatusBuilder;
import com.maxtropy.ilaundry.monitor.serial.model.receive.MachineControlInitializationPacket;
import com.maxtropy.ilaundry.monitor.serial.model.receive.MachineStatusPacket;
import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.AddTimePacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.AudioBeepRequest;
import com.maxtropy.ilaundry.monitor.serial.model.send.CardInitializationPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.CardInsertedPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.CardInsertedTopOffPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.CardRemovedPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.CashCardRemovedPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.DisplayRequest;
import com.maxtropy.ilaundry.monitor.serial.model.send.MachineStartPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.StatusRequestPacket;
import com.maxtropy.ilaundry.monitor.serial.model.send.ProgrammingDataPacket;
import com.maxtropy.ilaundry.monitor.roc.Roc;
import com.maxtropy.ilaundry.monitor.serial.SerialCommunicator;
import com.maxtropy.ilaundry.monitor.serial.SerialResponseListener;
import com.maxtropy.ilaundry.monitor.serial.model.send.VendPricePacket;

import java.util.HashMap;

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

    enum Status {
        initialization(0),
        idle(1),
        reserved(2),
        paid(3),
        started(4),
        done(5),
        error(6);    // before notification
        int value;
        private Status(int value) {
            this.value = value;
        }
    }

    public Status status = Status.initialization;

    MachineStatusPacket machineStatus = null;
    public static MachineControlInitializationPacket machineInfo = null;

    boolean cardInReader = false;

    public static boolean initialized = false;

    private GPIOCenter gpio;

    public void initialize() {
        try {
            serial.lock();
            switch(Global.systemType) {
                case MDC:
                    serial.sendPacket(new StatusRequestPacket(cardInReader), Thread.currentThread(), MachineStatusPacket.code);
                    serial.sendPacket(new VendPricePacket(), Thread.currentThread());
                    break;
                case Centurion:
                    serial.sendPacket(
                            new CardInitializationPacket(Global.ManufacturerId, Global.FirmwareVersion),
                            Thread.currentThread(),
                            MachineControlInitializationPacket.code);
                    serial.sendPacket(new StatusRequestPacket(cardInReader), Thread.currentThread(), MachineStatusPacket.code);
                    serial.sendPacket(new VendPricePacket(), Thread.currentThread());
                    break;
            }
            gpio = GPIOCenter.getInstance();
            gpio.enableCardReader();
            initialized = true;
        } catch(Exception e) {
            e.printStackTrace();
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

    int[] ledDigits = {126, 12, 182, 158, 204, 218, 250, 14, 254, 222};

    public void showErrorCode(int code) {
        sendSingleRequest(new DisplayRequest(242, 114, ledDigits[code / 10 % 10], ledDigits[code % 10]));
    }

    public MachineStatusPacket getMachineStatus() {
        try {
            serial.lock();
            serial.sendPacket(new StatusRequestPacket(cardInReader), Thread.currentThread(), MachineStatusPacket.code);
            serial.sendPacket(new VendPricePacket(), Thread.currentThread());
            serial.unlock();
            onStatusUpdate(machineStatus);
            return machineStatus;
        } catch(Exception e) {
            e.printStackTrace();
            serial.unlock();
            return null;
        }
    }

    public void program(int cycle) {
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
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            serial.unlock();
        }
    }

    public void initiateWechatWash(int cycle, int price) {
        try {
            status = Status.paid;
            lastNotification = 0;
            Global.vendPrice = price;
            roc.sendMessage(new ReservableStatusMessage(ReservableStatusMessage.Status.reserved_deprecated));
            gpio.disableCardReader();
            program(cycle);
            serial.lock();
            insertCard();
            serial.sendPacket(new CardInsertedPacket(Global.vendPrice * 2, Global.vendPrice), Thread.currentThread());
            serial.sendPacket(new AudioBeepRequest(4), Thread.currentThread());
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            serial.unlock();
        }
    }

    public void initiateCoinWash() {
        initiateWechatWash(2, Global.vendPrice);
    }

    public void initiateCardWash() {
        initiateWechatWash(2, Global.vendPrice);
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
        byte code = data[2];
        if(code == MachineStatusPacket.code) {
            Log.d(Const.TAG, "<< Machine status received");
            machineStatus = MachineStatusBuilder.build(msg);
        }
        if(code == MachineControlInitializationPacket.code) {
            Log.d(Const.TAG, "<< Centurion Machine initialization packet received");
            machineInfo = new MachineControlInitializationPacket(msg);
        }
    }

    boolean error = false;
    int lastNotification = 0;

    @Override
    public void onError(String reason) {
        if(status == Status.error)
            return;
        Roc.getInstance().sendMessage(new WasherErrorMessage(reason));
        gpio.disableCardReader();
        status = Status.error;
    }

    public boolean isReady() {
        return serial.isReady();
    }

    void toIdleState() {
        status = Status.idle;
        gpio.enableCardReader();
    }

    void toReserveState() {
        status = Status.reserved;
        gpio.disableCardReader();
    }

    synchronized void onStatusUpdate(MachineStatusPacket status) {
        if(status.isMode(6)) {
            onError("Error mode reported by washer");
            return;
        }
        error = false;
        switch(status.getCommandToReader()) {
            // See Centurion Manule 3-16
            case 0x13:
                // Centurion initialize comm seq
                initialize();
                break;
            case 0x46:
                roc.sendMessage(new ReservableStatusMessage(ReservableStatusMessage.Status.in_use));
                this.status = Status.started;
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
        if(status.isMode(1) && this.status == Status.initialization) {
            toIdleState();
        }
        if(status.isMode(5) && this.status == Status.started) {
            // Job finished. Report availability.
            toIdleState();
            roc.sendMessage(new RemainTimeMessage(0));
        }
        int minute = status.getRemainMinute();
        if(status.isMode(4) && Math.abs(lastNotification - minute) >= 2) {
            lastNotification = minute;
            roc.sendMessage(new RemainTimeMessage(minute));
        }
    }

    public void changeReserveState(ReserveRequest request) {
        if(request.getReserveState() == 1) {
            toReserveState();
        } else {
            if(status == Status.idle) {
                gpio.enableCardReader();
            }
        }
    }
}

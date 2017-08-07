
package com.maxtropy.ilaundry.monitor.service;

import android.text.TextUtils;
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

import org.w3c.dom.Text;

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
    public boolean heartbeatDisabled = false;

    enum Status {
        initialization(0),
        idle(1),
        reserved(2),
        paid(3),
        started(4),
        done(5),  // deprecated
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
    ConfigService config;

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
            initialized = true;
            status = Status.initialization;
            config = ConfigService.getInstance();
            doRecover();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            serial.unlock();
        }
    }

    /**
     * 从断电状态中恢复。
     */
    public void doRecover() {
        String orderId = config.getOrderId();
        Status prevStatus = config.getSerialStatus();
        if(!orderId.equals(Const.emptyOrderId) && prevStatus == Status.paid) {
            // 只需保持状态。微信和洗衣机都会在等待用户按start的状态
            cardInReader = true;
            this.status = prevStatus;
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
            if(heartbeatDisabled) {
                serial.unlock();
                return null;
            }
            serial.sendPacket(new StatusRequestPacket(cardInReader), Thread.currentThread(), MachineStatusPacket.code);
            serial.sendPacket(new VendPricePacket(), Thread.currentThread());
            serial.unlock();
            onStatusUpdate(machineStatus);
            return machineStatus;
        } catch (Exception e) {
            e.printStackTrace();
            serial.unlock();
            return null;
        }
    }

    public void program(int cycle) {
        try {
            serial.lock();
            insertCard();
            serial.sendPacket(new ProgrammingDataPacket(cycle, cycle==3?0:1), Thread.currentThread());
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

    public void initiateWash(int cycle, int price, String orderId) {
        try {
            if(!TextUtils.equals(orderId, config.getOrderId())) {
                toIdleState();
                return;
            }
            heartbeatDisabled = true;
            changeStatus(Status.paid);
            config.saveCycle(cycle);
            config.savePrice(price);
            lastNotification = 0;
            Global.vendPrice = price;
            gpio.disableCardReader();
            program(cycle);
            serial.lock();
            insertCard();
            serial.sendPacket(new CardInsertedPacket(Global.vendPrice * 2, Global.vendPrice), Thread.currentThread());
            serial.sendPacket(new AudioBeepRequest(4), Thread.currentThread());
            heartbeatDisabled = false;
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            serial.unlock();
        }
    }

    public void initiateCoinWash() {
        initiateWash(2, Global.vendPrice, Const.cardWashOrderId);
    }

    public void initiateCardWash() {
        config.saveOrderId(Const.cardWashOrderId);
        roc.sendMessage(new ReservableStatusMessage(ReservableStatusMessage.Status.card_reader_reserved));
        initiateWash(2, Global.vendPrice, Const.cardWashOrderId);
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
        Roc.getInstance().sendMessage(new WasherErrorMessage(reason, config.getOrderId()));
        gpio.disableCardReader();
        status = Status.error;
    }

    public boolean isReady() {
        return serial.isReady();
    }

    void changeStatus(Status status) {
        this.status = status;
        config.setSerialStatus(status);
    }

    void toIdleState() {
        changeStatus(Status.idle);
        gpio.enableCardReader();
        roc.sendMessage(new ReservableStatusMessage(ReservableStatusMessage.Status.available));
        // 在发出消息后才清除本地缓存的OrderId. 用于防止盒子断电前洗衣机在工作, 上电后洗衣机已完成时无法正确上报带OrderId的空闲状态.
        config.clearOrderId();
        Log.d(Const.TAG, "[Status] Idle");
    }

    void toReserveState(String orderId) {
        changeStatus(Status.reserved);
        config.saveOrderId(orderId);
        gpio.disableCardReader();
        Log.d(Const.TAG, "[Status] Reserved");
    }

    void toStartedState() {
        roc.sendMessage(new ReservableStatusMessage(ReservableStatusMessage.Status.machine_running));
        gpio.disableCardReader();
        changeStatus(Status.started);
        Log.d(Const.TAG, "[Status] Started");
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
                // toStartedState();
                heartbeatDisabled = true;
                sendSingleRequest(new MachineStartPacket());
                removeCard();
                sendSingleRequest(new CashCardRemovedPacket());
                heartbeatDisabled = false;
                break;
            case 0x47:
                // deduct topoff vend
                sendSingleRequest(new AddTimePacket());
                removeCard();
                sendSingleRequest(new CashCardRemovedPacket());
                break;
        }


        String tmp = "Status: ";
        for(int i = 0; i < 8; ++i)
            if(status.isMode(i))
                tmp += i + " ";
        Log.d(Const.TAG, tmp);

        if((status.isMode(1) || status.isMode(5)) && (this.status == Status.initialization || this.status == Status.started || this.status == Status.error)) {
            // Job finished. Report availability.
            if(this.status == Status.started) {
                roc.sendMessage(new RemainTimeMessage(0, config.getOrderId()));
                program(2);
            }
            toIdleState();
        }
        int minute = status.getRemainMinute();
        if(status.isMode(4)) {
            if(this.status != Status.started) {
                toStartedState();
            }
            if(Math.abs(lastNotification - minute) >= 2) {
                lastNotification = minute;
                roc.sendMessage(new RemainTimeMessage(minute, config.getOrderId()));
            }
        }
    }

    public void changeReserveState(ReserveRequest request) {
        if(request.getReserveState() == 1) {
            // 如果状态非idle, 记录的orderId等于-1时返回刷卡预约
            if(status != Status.idle && TextUtils.equals(config.getOrderId(),Const.cardWashOrderId)) {
                roc.sendMessage(new ReservableStatusMessage(ReservableStatusMessage.Status.card_reader_reserved));
                return;
            }
            // TODO: 对于状态非idle, 且记录的orderid不等于-1时, 是否会有记录的orderid不等于request.orderId的情况? 此时应该如何处理?
            toReserveState(request.getOrderId());
        } else {
            if(status == Status.reserved) {
                toIdleState();
            }
        }
    }
}

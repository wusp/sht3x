package com.maxtropy.ilaundry.monitor.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.Global;
import com.maxtropy.ilaundry.monitor.serial.model.receive.MachineStatusPacket;

import java.util.Random;

/**
 * 当超时直接上传前面一条，停止后续的采集同时重新开串口。
 * 所有回调都工作在工作线程
 * Created by wusp on 2017/5/3.
 */

public class MachineStatusCronService extends BroadcastReceiver {

    SerialService serial = SerialService.getInstance();

    private Context app;
    private Random ran = new Random(System.currentTimeMillis());

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!Global.initialized() || !serial.isReady())
            return;
        app = context.getApplicationContext();
        MachineStatusPacket machineStatus = serial.getMachineStatus();
        if(machineStatus == null) {
            Log.w(Const.TAG, "Not able to get machine status");
            return;
        }
        /*
        SerialCommunicator.getInstance();
        preparedToSend();
        */
        /*
        report = new MonitorReportMessage();   //the new data report.
        Roc.getInstance(app).sendMessage(report);

        ReservableStatusMessage rsvMsg = new ReservableStatusMessage(1);
        Roc.getInstance(app).sendMessage(rsvMsg);
        */
    }

    /*
    private void preparedToSend() {
        int opened = Gpio.readRaw(Const.PATH_DOOR_SWITCHER);
        if (opened > 1 || opened < 0) {
            //unusual
            report.setDoorOpened(2);
        } else {
            report.setDoorOpened(opened);
        }
        Log.e(Const.TAG, "Sending report data: " + report.toString());
        Roc.getInstance(app).sendMessage(report);
        prev = report;
    }
    */

}

package com.maxtropy.ilaundry.monitor.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.message.send.MonitorReportMessage;
import com.maxtropy.ilaundry.monitor.message.send.ReservableStatusMessage;
import com.maxtropy.ilaundry.monitor.model.SerialPacket;
import com.maxtropy.ilaundry.monitor.serial.SerialResponseListener;
import com.maxtropy.ilaundry.monitor.roc.Roc;
import com.maxtropy.ilaundry.monitor.serial.SerialCommunicator;
import com.maxtropy.ilaundry.monitor.serial.Utils;
import com.softwinner.Gpio;

import java.util.Random;

/**
 * 当超时直接上传前面一条，停止后续的采集同时重新开串口。
 * 所有回调都工作在工作线程
 * Created by wusp on 2017/5/3.
 */

public class MachineStatusCronService extends BroadcastReceiver {
    private MonitorReportMessage prev = new MonitorReportMessage("Maxtropy", "", "", "", "", "", 2, 2);
    private MonitorReportMessage report;
    private Context app;
    private Random ran = new Random(System.currentTimeMillis());

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Const.TAG, "Receive intent action on MachineStatusCronService: " + intent.getAction());
        app = context.getApplicationContext();
        report = new MonitorReportMessage();   //the new data report.
        /*
        SerialCommunicator.getInstance();
        preparedToSend();
        */
        Roc.getInstance(app).sendMessage(report);

        ReservableStatusMessage rsvMsg = new ReservableStatusMessage(1);
        Roc.getInstance(app).sendMessage(rsvMsg);
    }

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

    private void onError(SerialPacket errorReq) {
        if (errorReq == null) {
            return;
        }
        SerialCommunicator.getInstance().restart();
    }

    private SerialResponseListener htListener = new SerialResponseListener() {
        @Override
        public void onResponse(SerialPacket msg) {
            byte[] datas = msg.getData();
            if (datas.length != 4) {
                report.setHumi(prev.getHumi());
                report.setTemp(prev.getTemp());
            } else {
                Log.d(Const.TAG, "receive temp: " + Utils.byte2Int(new byte[]{0x00, 0x00, datas[0], datas[1]}) / 10.0f);
                Log.d(Const.TAG, "receive temp: " + Utils.byte2Int(new byte[]{0x00, 0x00, datas[2], datas[3]}) / 10.0f);
                report.setTemp("" + Utils.byte2Int(new byte[]{0x00, 0x00, datas[0], datas[1]}) / 10.0f);
                report.setHumi("" + Utils.byte2Int(new byte[]{0x00, 0x00, datas[2], datas[3]}) / 10.0f);
            }
        }

        @Override
        public void onOverTime(SerialPacket msg) {
            onError(msg);
        }
    };

}

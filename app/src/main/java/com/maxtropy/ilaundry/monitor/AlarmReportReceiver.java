package com.maxtropy.ilaundry.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.maxtropy.ilaundry.monitor.message.send.MonitorReportMessage;
import com.maxtropy.ilaundry.monitor.message.send.ReservableStatusMessage;
import com.maxtropy.ilaundry.monitor.model.ElectricityRequest;
import com.maxtropy.ilaundry.monitor.model.HumiTempRequest;
import com.maxtropy.ilaundry.monitor.model.ModbusRequest;
import com.maxtropy.ilaundry.monitor.model.ModbusResponse;
import com.maxtropy.ilaundry.monitor.model.ModbusResponseListener;
import com.maxtropy.ilaundry.monitor.roc.Roc;
import com.maxtropy.ilaundry.monitor.serial.Utils;
import com.softwinner.Gpio;

import java.util.Random;

/**
 * 当超时直接上传前面一条，停止后续的采集同时重新开串口。
 * 所有回调都工作在工作线程
 * Created by wusp on 2017/5/3.
 */

public class AlarmReportReceiver extends BroadcastReceiver {
    private MonitorReportMessage prev = new MonitorReportMessage("Maxtropy", "", "", "", "", "", 2, 2);
    private MonitorReportMessage report;
    private Context app;
    private Random ran = new Random(System.currentTimeMillis());

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Const.TAG, "Receive intent action on AlarmReportReceiver: " + intent.getAction());
        app = context.getApplicationContext();
        report = new MonitorReportMessage();   //the new data report.
        /*
        ModbusCenter.getInstance().sendModbusRequest(new HumiTempRequest(), htListener);
        try {
            Thread.sleep(1200 + ran.nextInt(500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ModbusCenter.getInstance().sendModbusRequest(new ElectricityRequest(), electricityListener);
        try {
            Thread.sleep(1200 + ran.nextInt(500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    private void onError(ModbusRequest errorReq) {
        if (errorReq == null) {
            return;
        }
        if (errorReq.getSlaveAddress() == HumiTempRequest.ADDRESS) {
            report.setHumi(prev.getHumi());
            report.setTemp(prev.getTemp());
        } else if (errorReq.getSlaveAddress() == ElectricityRequest.ADDRESS) {
            //Inform error anyway.
            report.setCompressorWorking(2);
        }
        // ModbusCenter.getInstance().restart();
    }

    private ModbusResponseListener htListener = new ModbusResponseListener() {
        @Override
        public void onResponse(ModbusResponse res) {
            byte[] datas = res.getData();
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
        public void onOverTime(ModbusRequest req) {
            onError(req);
        }
    };

    private ModbusResponseListener electricityListener = new ModbusResponseListener() {
        @Override
        public void onResponse(ModbusResponse res) {
            byte[] datas = res.getData();
            if (datas.length != 2) {
                Log.e(Const.TAG, "Electricity error, current data's length: " + datas.length);
                report.setCompressorWorking(2);
            } else {
                //文档有误，按照0.01来换算
                float ele = Utils.byte2Int(new byte[]{0x00, 0x00, datas[0], datas[1]}) * 0.01f;
                Log.d(Const.TAG, "receive concurrency: " + ele);
                if (ele >= 1.0f) {
                    report.setCompressorWorking(1);
                } else {
                    report.setCompressorWorking(0);
                }
            }
        }

        @Override
        public void onOverTime(ModbusRequest req) {
            onError(req);
        }
    };
}

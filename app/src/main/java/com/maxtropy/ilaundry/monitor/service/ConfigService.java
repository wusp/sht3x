package com.maxtropy.ilaundry.monitor.service;

import android.content.SharedPreferences;
import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;

/**
 * Created by Gerald on 7/31/2017.
 */

public class ConfigService {

    private static ConfigService service;

    public static void init(SharedPreferences settings) {
        service = new ConfigService(settings);
    }

    public static ConfigService getInstance() {
        return service;
    }

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    public ConfigService(SharedPreferences settings) {
        this.settings = settings;
        editor = settings.edit();
    }

    public void saveMachineType(String machineType) {
        editor.putString("machineType", machineType);
        Log.d(Const.TAG, "[Save MachineType from Cache]: " + machineType);
        editor.commit();
    }

    public String getMachineType() {
        String tmps = settings.getString("machineType", null);
        Log.d(Const.TAG, "[Get MachineType from Cache]: " + tmps);
        return tmps;
    }

    public void saveOrderId(String orderId) {
        editor.putString("orderId", orderId);
        Log.d(Const.TAG, "[Save Order Id to storage]: " + orderId);
        editor.commit();
    }

    public String getOrderId() {
        String tmps = settings.getString("orderId", Const.emptyOrderId);
        Log.d(Const.TAG, "[Get OrderId from Storage]: " + tmps);
        return tmps;
    }

    public void clearOrderId(){
        saveOrderId(Const.emptyOrderId);
    }

    public SerialService.Status getSerialStatus() {
        int tmp = settings.getInt("serialStatus", SerialService.Status.initialization.value);
        Log.v(Const.TAG, "[Get status from Storage]: " + tmp);
        return SerialService.Status.values()[tmp];
    }

    public void setSerialStatus(SerialService.Status status) {
        editor.putInt("serialStatus", status.value);
        Log.v(Const.TAG, "[Save status from Storage]: " + status.value);
    }
    public void saveCycle(int cycle) {
        editor.putInt("cycle", cycle);
    }

    public void savePrice(int price) {
        editor.putInt("price", price);
    }

    public int getCycle() {
        return settings.getInt("cycle", 2);
    }

    public int getPrice() {
        return settings.getInt("price", 1);
    }
}

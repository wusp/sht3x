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
        String tmps = settings.getString("orderId", null);
        Log.d(Const.TAG, "[Get OrderId from Storage]: " + tmps);
        return tmps;
    }

}

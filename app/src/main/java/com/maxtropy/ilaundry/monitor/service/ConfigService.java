package com.maxtropy.ilaundry.monitor.service;

import android.content.SharedPreferences;

/**
 * Created by Gerald on 7/31/2017.
 */

public class ConfigService {

    static ConfigService service;

    public static ConfigService getInstance() {
        if(service == null)
            service = new ConfigService();
        return service;
    }

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    private ConfigService() {
        editor = settings.edit();
    }

    public void saveMachineType(String machineType) {
        editor.putString("machineType", machineType);
        editor.commit();
    }

    public String getMachineType() {
        return settings.getString("machineType", null);
    }

}

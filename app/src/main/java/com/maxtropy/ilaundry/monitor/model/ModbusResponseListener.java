package com.maxtropy.ilaundry.monitor.model;

/**
 * Created by wusp on 2017/4/28.
 */

public interface ModbusResponseListener {
    void onResponse(ModbusResponse res);
    void onOverTime(ModbusRequest req);
}

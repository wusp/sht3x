package com.maxtropy.ilaundry.monitor.serial;

import com.maxtropy.ilaundry.monitor.model.SerialPacket;

/**
 * Created by wusp on 2017/4/28.
 */

public interface SerialResponseListener {
    void onResponse(SerialPacket msg);
    void onOverTime(SerialPacket msg);
}

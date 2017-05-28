package com.maxtropy.ilaundry.monitor;

import android.support.multidex.MultiDexApplication;

import com.maxtropy.ilaundry.monitor.roc.Roc;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by wusp on 2017/5/7.
 */

public class MyApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "998fb72648", true);
        Roc.getInstance(this);
    }
}
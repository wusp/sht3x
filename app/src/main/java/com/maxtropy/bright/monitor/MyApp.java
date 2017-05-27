package com.maxtropy.bright.monitor;

import android.support.multidex.MultiDexApplication;

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

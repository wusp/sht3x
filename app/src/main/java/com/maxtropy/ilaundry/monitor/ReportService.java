package com.maxtropy.ilaundry.monitor;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.maxtropy.ilaundry.monitor.serial.ModbusCenter;
import com.maxtropy.ilaundry.monitor.serial.Utils;
import com.maxtropy.roc.util.IntentReceiver;


/**
 * Created by wusp on 2017/4/13.
 */

public class ReportService extends Service {
    private AlarmManager am;
    private PendingIntent alarmIntent;
    private IntentReceiver coreIntentReceiver = null;

    @Override
    public void onCreate() {
        super.onCreate();
        android.os.Debug.waitForDebugger();
        Log.d(Const.TAG, "Service created.");
        coreIntentReceiver = new IntentReceiver(getClass().getName());
        registerReceiver(coreIntentReceiver, new IntentFilter(com.maxtropy.roc.Const.HEALTH_CHECK_ACTION));
        startForeground(3333, new Notification.Builder(this)
                .setContentTitle("iLaundry")
                .setContentText("iLaundry Service")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Const.TAG, "Service started.");
        // ModbusCenter.getInstance().run();
        Roc.getInstance(this);
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, AlarmReportReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Utils.nextUploadTimeGap(), 300 * 1000, alarmIntent);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (am!= null) {
            am.cancel(alarmIntent);
        }
        if (coreIntentReceiver != null) {
            unregisterReceiver(coreIntentReceiver);
        }
        Roc.getInstance(this).stop();
    }
}

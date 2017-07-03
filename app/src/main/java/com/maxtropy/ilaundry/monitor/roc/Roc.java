package com.maxtropy.ilaundry.monitor.roc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.R;
import com.maxtropy.ilaundry.monitor.roc.consumer.MachineTypeConsumer;
import com.maxtropy.ilaundry.monitor.roc.consumer.TestToBoxConsumer;
import com.maxtropy.ilaundry.monitor.roc.consumer.WashRequestConsumer;
import com.maxtropy.ilaundry.monitor.roc.message.receive.MachineTypeResponse;
import com.maxtropy.ilaundry.monitor.roc.message.send.MachineTypeRequest;
import com.maxtropy.ilaundry.monitor.service.SerialService;
import com.maxtropy.mockingbirds.protocol.IMessageV2;
import com.maxtropy.roc.RocChannel;
import com.maxtropy.roc.RocMessageReceiver;
import com.maxtropy.roc.ServerId;
import com.maxtropy.roc.ServiceBinder;
import com.maxtropy.roc.model.MessageData;

/**
 * 1. 对于需要监听的消息，需要在建立同RocService的连接Binder之前添加进消息监听器RocMessageReceiver.
 * 2. 消息监听器不能动态修改，监听的消息在连接建立之后也不能取消监听。
 * 3. 每个业务单元，对于需要监听的消息，需要添加对应消息的回调接口，在不需要继续监听消息时需要移除相应的回调接口。
 * 4. 对于能否正常发送消息到服务器，需要判断以下条件是否成立：
 *     a. RocChannel能否正常将消息发送到RocService.
 *     b. 当前网络可用.
 *     c. RocService正常工作.
 * Created by wusp on 2016/12/26.
 */

public class Roc {
    private static Roc singleton = null;
    private ServiceBinder rocBinder;
    private RocMessageReceiver rocMessageReceiver;
    private RocChannel rocChannel;
    private String appDescriptor;

    private ConnectivityManager cm;

    public static Roc getInstance(@NonNull Context context) {
        if (singleton == null) {
            synchronized (Roc.class) {
                if (singleton == null) {
                    singleton = new Roc(context);
                }
            }
        }
        return singleton;
    }

    // TODO This is a hack..
    public static Roc getInstance() {
        return singleton;
    }

    private Roc(Context context) {
        init(context);
    }

    private void init(Context context) {
        singleton = this;
        rocMessageReceiver = new RocMessageReceiver();
        rocMessageReceiver.addConsumer(new TestToBoxConsumer());
        rocMessageReceiver.addConsumer(new WashRequestConsumer(this));
        rocMessageReceiver.addConsumer(new MachineTypeConsumer());
        rocBinder = new ServiceBinder(context, rocMessageReceiver);
        appDescriptor = context.getResources().getString(R.string.app_name);
        rocChannel = bindRocService(appDescriptor, ServerId.OTHER, rocBinder);
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        sendMessage(new MachineTypeRequest());
    }

    /**
     * 通过ServiceBinder绑定App及RocService, 返回的RocChannel用于后续发送Roc消息.
     * @param name app's descriptor
     * @param id Roc Server ID
     * @param binder help to build the connection between RocChannel and RocService
     * @return null if the arguments is invalid.
     */
    private RocChannel bindRocService(String name, ServerId id, ServiceBinder binder) {
        if (TextUtils.isEmpty(name) || (id != ServerId.MAXTROPY && id != ServerId.OTHER)
                || binder == null) {
            return null;
        }
        return binder.bindRocService(name, id);
    }

    /**
     * 发送之前先进行RocChannel的相关检查
     * @return
     */
    private boolean checkChannel() {
        if (rocChannel == null || !rocChannel.isOpen()) {
            return false;
        }
        //Connection check and recovery the channel if connection fails.
        if (!rocChannel.isConnecting()) {
            Log.e(Const.TAG, "Cannot send a Roc message. Roc channel connecting status: " + rocChannel.isConnecting());
            Log.i(Const.TAG, "Try to recovery RocChannel.");
            rocChannel = bindRocService(appDescriptor, ServerId.OTHER, rocBinder);
            return false;
        }
        return true;
    }

    /**
     * Detect whether the network is available.
     * @return
     */
    private boolean isNetworkAvailable() {
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
        return false;
    }

    public void sendMessage(IMessageV2 msg) {
        if (checkChannel()) {
            Log.d(Const.TAG, "Send an Roc message, message's topic: " + msg.getTopic());
            //mockingbirds might throw some exceptions to this, use try-catch to prevent uncaught exception.
            try {
                rocChannel.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(MessageData msg) {
        if (checkChannel()) {
            Log.d(Const.TAG, "Send an Roc message, message's topic: " + msg.getTopic());
            try {
                rocChannel.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Should be called when the application do not need ROC any more.
     */
    public void stop() {
        if (rocBinder != null) {
            try {
                rocBinder.unbindRocService();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

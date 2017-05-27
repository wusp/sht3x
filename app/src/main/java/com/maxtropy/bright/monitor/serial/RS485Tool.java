package com.maxtropy.bright.monitor.serial;


import android.text.TextUtils;
import android.util.Log;

import com.maxtropy.bright.monitor.Const;

import java.util.ArrayList;
import java.util.List;

import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * 负责打开/关闭串口
 * Created by wusp on 16/8/17.
 */
class RS485Tool {

    static List<SerialPort> prepareSerials(String... portIdentifiers) {
        if (portIdentifiers == null || portIdentifiers.length < 1) {
            return null;
        }
        List<SerialPort> serialPorts = new ArrayList<>();
        for (String port1 : portIdentifiers) {
            Log.d(Const.TAG, "port1: " + port1);
            SerialPort port = openSerial(port1);
            if (port != null) {
                serialPorts.add(port);
            }
        }
        return serialPorts;
    }

    static SerialPort openSerial(String portIdentifier) {
        if (TextUtils.isEmpty(portIdentifier)) {
            Log.d(Const.TAG, "portI cannot be null.");
            return null;
        }
        SerialPort port;
        try {
            port = (SerialPort) CommPortIdentifier.getPortIdentifier(portIdentifier).open("interface", 1000);
            port.notifyOnDataAvailable(true);
            port.notifyOnOutputEmpty(true);
            port.setInputBufferSize(100);
            port.setOutputBufferSize(100);
            port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            return port;
        } catch (PortInUseException | NoSuchPortException | UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void closeSerial(List<SerialPort> ports) {
        for (SerialPort serialPort : ports) {
            serialPort.close();
        }
        ports.clear();
    }
}

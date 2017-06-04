package com.maxtropy.ilaundry.monitor.model;

/**
 * Created by Gerald on 6/2/2017.
 */

public class SerialPacket {

    byte[] data;

    public SerialPacket(byte[] data) {
        this.data = data;
    }

    byte calcBCC(byte[] ret) {
        byte res = 0;
        for(int i = 0; i < ret.length - 1; ++i)
            res ^= ret[i];
        return res;
    }

    public byte[] toBytes() {
        byte[] ret = new byte[data.length + 3];
        int n = data.length + 3;
        ret[0] = 0x02;
        ret[1] = (byte)data.length;
        for(int i = 0; i < data.length; ++i)
            ret[i + 2] = data[i];
        ret[n - 1] = calcBCC(ret);
        return ret;
    }

    public boolean isValid() {
        if(data == null || data.length <= 3)
            return false;
        if(data[0] != 0x02 || data.length != data[1] + 3)
            return false;
        byte bcc = calcBCC(data);
        if(data[data.length - 1] != bcc)
            return false;
        return true;
    }

    public byte[] getData() {
        return data;
    }

}

package com.maxtropy.ilaundry.monitor.serial.model;

/**
 * Created by Gerald on 6/2/2017.
 */

public class SerialPacket {

    protected byte[] data;

    public SerialPacket() {
        data = null;
        tag = "";
    }

    public SerialPacket(byte[] data) {
        this.data = data;
        tag = "";
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

    protected String tag;

    public String getTag() {
        return tag;
    }

    public void putThree(int offset, int value) {
        data[offset] = (byte)(value / 0x10000);
        data[offset + 1] = (byte)(value % 0x10000 / 0x100);
        data[offset + 2] = (byte)(value % 0x100);
    }

    public void putShort(int offset, int value) {
        data[offset] = (byte)(value / 0x100);
        data[offset + 1] = (byte)(value % 0x100);
    }

    public void putByte(int offset, int value) {
        data[offset] =  (byte)value;
    }

}

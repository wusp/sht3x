package com.maxtropy.ilaundry.monitor.serial;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by wusp on 2017/4/6.
 */

public class Utils {
    private static final int CRC16_SEED = 0xFFFF;

    /**
     * 普通的crc16值，如果是Modbus，只需要提取低位的16位
     * @param buffer
     * @return
     */
    public static int crc16(final byte[] buffer) {
        int crc = 0xFFFF;

        for (int j = 0; j < buffer.length ; j++) {
            crc = ((crc  >>> 8) | (crc  << 8) )& 0xffff;
            crc ^= (buffer[j] & 0xff);//byte to int, trunc sign
            crc ^= ((crc & 0xff) >> 4);
            crc ^= (crc << 12) & 0xffff;
            crc ^= ((crc & 0xFF) << 5) & 0xffff;
        }
        crc &= 0xffff;
        return crc;
    }

    public static int byte2Int(byte[] rno) {
        return (rno[0]<<24)&0xff000000|
                (rno[1]<<16)&0x00ff0000|
                (rno[2]<< 8)&0x0000ff00|
                (rno[3])&0x000000ff;
    }

    public static byte[] short2Bytes(short x) {
        byte[] ret = new byte[2];
        ret[0] = (byte)(x & 0xff);
        ret[1] = (byte)((x >> 8) & 0xff);
        return ret;
    }

    public static short byte2Short(byte[] b) {
        return (short) (((b[1] << 8) | b[0] & 0xff));
    }

    public static byte[] int2Bytes(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }


    public static String parseTimeStamp2FormatDate(long timestamp) {
        Date date = new Date(timestamp);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return df.format(date);
    }

    public static long nextUploadTimeGap() {
        Calendar c = Calendar.getInstance();
        int minR = c.get(Calendar.MINUTE) % 5;
        int secondR = c.get(Calendar.SECOND) % 60;
        long milliSecondR = c.get(Calendar.MILLISECOND);
        return 5 * 60 * 1000 - minR * 60 * 1000 - secondR * 1000 - milliSecondR;
    }

    public static float bytes2Float(byte[] datas) {
        ByteBuffer buffer = ByteBuffer.wrap(datas);
        return buffer.getFloat();
    }
}

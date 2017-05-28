package com.maxtropy.ilaundry.monitor.model;

import com.maxtropy.ilaundry.monitor.serial.Utils;
import com.maxtropy.mockingbirds.annotation.MessageKey;
import com.maxtropy.mockingbirds.annotation.MessageType;
import com.maxtropy.mockingbirds.protocol.AbstractMessageV2;
import com.maxtropy.mockingbirds.protocol.MessageConstV2;

/**
 * Created by wusp on 2017/4/12.
 */
@MessageType(subType = DataReport.SUB_TYPE, type = MessageConstV2.TYPE_REPORT)
public class DataReport extends AbstractMessageV2 {
    static final int SUB_TYPE = 0x499001;
    static final String topic = "ilaundry_data";
    @MessageKey(1)
    String source;
    @MessageKey(2)
    String temp;
    @MessageKey(3)
    String humi;
    @MessageKey(4)
    String latitude;
    @MessageKey(5)
    String longitude;
    @MessageKey(6)
    String speed;
    @MessageKey(7)
    String timestamp;
    @MessageKey(8)
    int isDoorOpened; //1:open 0:close
    @MessageKey(9)
    int isCompressorWorking; //1:working 0:shutdown

    public DataReport(String source, String temp, String humi, String latitude,
                      String longitude, String speed, int isDoorOpened, int isCompressorWorking) {
        this.source = source;
        this.temp = temp;
        this.humi = humi;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.timestamp = Utils.parseTimeStamp2FormatDate(System.currentTimeMillis());
        setTopic(topic);
        this.isDoorOpened = isDoorOpened;
        this.isCompressorWorking = isCompressorWorking;
    }

    public DataReport() {
        this.source = "Maxtropy";
        this.timestamp = Utils.parseTimeStamp2FormatDate(System.currentTimeMillis());
        setTopic(topic);
        this.latitude = "";
        this.longitude = "";
        this.speed = "";
        //Remain four fields to be set.
    }

    @Override
    public String toString() {
        return "DataReport{" +
                "humi='" + humi + '\'' +
                ", source='" + source + '\'' +
                ", temp='" + temp + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", speed='" + speed + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", isDoorOpened=" + isDoorOpened +
                ", isCompressorWorking=" + isCompressorWorking +
                '}';
    }

    public String getHumi() {
        return humi;
    }

    public void setHumi(String humi) {
        this.humi = humi;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public static int getSubType() {
        return SUB_TYPE;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int isCompressorWorking() {
        return isCompressorWorking;
    }

    public void setCompressorWorking(int compressorWorking) {
        isCompressorWorking = compressorWorking;
    }

    public int isDoorOpened() {
        return isDoorOpened;
    }

    public void setDoorOpened(int doorOpened) {
        isDoorOpened = doorOpened;
    }
}

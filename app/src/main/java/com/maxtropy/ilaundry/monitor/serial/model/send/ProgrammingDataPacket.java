package com.maxtropy.ilaundry.monitor.serial.model.send;

import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.Global;
import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

/**
 * Created by Gerald on 6/4/2017.
 */

public class ProgrammingDataPacket extends SerialPacket {

    public byte code = getCode();

    public byte getCode() {
        switch(Global.machineType) {
            case TopLoadWasher:
                return 0x21;
            break;
            case FrontLoadWasher:
                return 0x22;
                break;
            case WasherExtractor:
                return 0x24;
                break;
            case Tumbler:
                return 0x28;
                break;
            case Dryer:
                return 0x29;
                break;
        }
    }

    public ProgrammingDataPacket(int cycle) {
        tag = this.getClass().getName();
        // 不想创很多类，这样是最简单的方法了
        // 虽然可维护性会不是很好
        switch(Global.systemType) {
            case MDC:
                switch(Global.machineType) {
                    case TopLoadWasher:
                        data = new byte[43];
                        data[0] = code;
                        putShort(1, 100);    // Vend price
                        putShort(3, 25);    // Coin 1
                        putShort(5, 100);   // Coin 2
                        putShort(7, 100);    // Start pulse
                        putByte(9, 1);     // cycle length
                        putByte(10, 8);     // control configuration
                        putByte(11, cycle);    // default cycle
                        break;
                    case Tumbler:
                        data = new byte[43];
                        data[0] = code;
                        putShort(1, 25);    // Vend price
                        putShort(3, 25);    // Coin 1
                        putShort(5, 100);   // Coin 2
                        putShort(7, 25);    // Start pulse
                        putByte(9, 10);     // cycle time
                        putByte(10, 1);     // cool down time
                        putByte(11, 10);    // coin 1 topoff
                        putByte(12, 40);    // coin 2 topoff
                        putByte(13, 0);     // h temp
                        putByte(14, 2);     // m temp
                        putByte(15, 6);     // l temp
                        putByte(16, 12);    // delicate temp
                        putByte(17, 12);    // control conf
                        putByte(18, cycle); // default cycle: 1 Heavy, 2: Normal, 3: Perm Press, 4: Delicate
                        break;
                    default:
                        Log.e(Const.TAG, "Programming packet for this type of machine not implemented");
                }
                break;
            case Centurion:
            default:
                Log.e(Const.TAG, "Programming packet for this type of machine not implemented");
        }
    }

}

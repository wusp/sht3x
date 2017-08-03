package com.maxtropy.ilaundry.monitor.serial.model.send;

import android.util.Log;

import com.maxtropy.ilaundry.monitor.Const;
import com.maxtropy.ilaundry.monitor.Global;
import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;
import com.maxtropy.ilaundry.monitor.service.SerialService;

/**
 * Created by Gerald on 6/4/2017.
 */

public class ProgrammingDataPacket extends SerialPacket {

    public byte code = getCode();

    public byte getCode() {
        switch(Global.machineType) {
            case TopLoadWasher:
                return 0x21;
            case FrontLoadWasher:
                return 0x22;
            case WasherExtractor:
                return 0x24;
            case Tumbler:
                return 0x28;
            case Dryer:
                return 0x29;
            default:
                Log.e(Const.TAG, "Program before initialization.");
                return 0x00;
        }
    }

    public ProgrammingDataPacket(int cycle, int length) {
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
                        putByte(9, length);     // cycle length
                        putByte(10, 8);     // control configuration
                        putByte(11, cycle);    // default cycle
                        break;
                    case FrontLoadWasher:
                        data = new byte[43];
                        data[0] = code;
                        putShort(1, 100);    // Vend price
                        putShort(3, 25);    // Coin 1
                        putShort(5, 100);   // Coin 2
                        putShort(7, 100);    // Start pulse
                        putByte(9, 3);     // cycle configuration
                        putByte(10, 4);     // control configuration
                        putByte(11, cycle);    // default cycle
                        break;
                    case Dryer:
                        data = new byte[43];
                        data[0] = code;
                        putShort(1, 100);    // Vend price
                        putShort(3, 25);    // Coin 1
                        putShort(5, 100);   // Coin 2
                        putShort(7, 25);    // Start pulse
                        putByte(9, 45);     // cycle time
                        putByte(10, 3);     // cool down time
                        putByte(11, 15);    // coin 1 topoff
                        putByte(12, 60);    // coin 2 topoff
                        putByte(13, 0);     // h temp
                        putByte(14, 1);     // m temp
                        putByte(15, 3);     // l temp
                        putByte(16, 9);    // delicate temp
                        putByte(17, 12);    // control conf
                        putByte(18, cycle); // default cycle: 1 Heavy, 2: Normal, 3: Perm Press, 4: Delicate
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
                switch(Global.machineType) {
                    case TopLoadWasher:
                        data = new byte[45];
                        data[0] = code;
                        data[1] = SerialService.machineInfo.getProductByte1();
                        data[2] = SerialService.machineInfo.getProductByte2();
                        data[3] = SerialService.machineInfo.getProductByte3();
                        for(int i = 6; i <= 22; i += 2)
                            putShort(i, 100);

                        putShort(25);
                        putByte(1);
                        putByte(2);
                        putByte(0);
                        putByte(0);

                        putByte(10);
                        putByte(0);
                        putByte(4);
                        putByte(7);

                        putByte(10);
                        putByte(0);
                        putByte(4);
                        putByte(6);

                        putByte(10);
                        putByte(0);
                        putByte(4);
                        putByte(5);

                        putByte(cycle);
                        putByte(0x1D);
                        putByte(0);
                        assert nowOffset == data.length;
                        break;
                    case FrontLoadWasher:
                        data = new byte[34];
                        data[0] = code;
                        data[1] = SerialService.machineInfo.getProductByte1();
                        data[2] = SerialService.machineInfo.getProductByte2();
                        data[3] = SerialService.machineInfo.getProductByte3();
                        data[5] = SerialService.machineInfo.getDomesticInternational();
                        for(int i = 6; i <= 22; i += 2)
                            putShort(i, 100);

                        putShort(25);
                        putShort(50);
                        putByte(0x02);
                        putByte(0x03);
                        putByte(2);

                        putByte(cycle);
                        putByte(0x1D);
                        putByte(0);
                        assert nowOffset == data.length;
                        break;
                    case WasherExtractor:
                        data = new byte[38];
                        data[0] = code;
                        data[1] = SerialService.machineInfo.getProductByte1();
                        data[2] = SerialService.machineInfo.getProductByte2();
                        data[3] = SerialService.machineInfo.getProductByte3();
                        data[5] = SerialService.machineInfo.getDomesticInternational();
                        for(int i = 6; i <= 22; i += 2)
                            putShort(i, 100);

                        putShort(25);
                        putShort(25);
                        putShort(25);
                        putShort(50);
                        putByte(0x01);
                        putByte(0x03);
                        putByte(2);

                        putByte(cycle);
                        putByte(0x1D);
                        putByte(0);
                        assert nowOffset == data.length;
                        break;
                    case Dryer:
                        data = new byte[33];
                        data[0] = code;
                        data[1] = SerialService.machineInfo.getProductByte1();
                        data[2] = SerialService.machineInfo.getProductByte2();
                        data[3] = SerialService.machineInfo.getProductByte3();
                        data[4] = SerialService.machineInfo.getMachineConfig();

                        putShort(6, 100);
                        putShort(100);
                        putShort(25);
                        putByte(45);
                        putByte(0);
                        putByte(45);
                        putByte(0);
                        putByte(3);
                        putByte(3);
                        putByte(3);
                        putByte(3);
                        putByte(1);
                        putByte(15);
                        putByte(0);
                        putByte(25);
                        putByte(24);
                        putByte(22);
                        putByte(16);
                        putByte(cycle);
                        putByte(0x1D);
                        putByte(0);
                        putByte(1);
                        putByte(0);

                        assert nowOffset == data.length;
                        break;
                    case Tumbler:
                        data = new byte[33];
                        data[0] = code;
                        data[1] = SerialService.machineInfo.getProductByte1();
                        data[2] = SerialService.machineInfo.getProductByte2();
                        data[3] = SerialService.machineInfo.getProductByte3();
                        data[4] = SerialService.machineInfo.getMachineConfig();

                        putShort(6, 25);
                        putShort(25);
                        putShort(25);
                        putByte(8);
                        putByte(0);
                        putByte(8);
                        putByte(0);
                        putByte(1);
                        putByte(1);
                        putByte(1);
                        putByte(1);
                        putByte(1);
                        putByte(8);
                        putByte(0);
                        putByte(31);
                        putByte(29);
                        putByte(25);
                        putByte(19);
                        putByte(cycle);
                        putByte(0x1D);
                        putByte(0);
                        putByte(1);
                        putByte(0);

                        assert nowOffset == data.length;
                        break;
                    default:
                        Log.e(Const.TAG, "Programming packet for this type of machine not implemented");
                }
            default:
                Log.e(Const.TAG, "Programming packet for this type of machine not implemented");
        }
    }

}

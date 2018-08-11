package ua.mezon.xtrainervr;

import android.bluetooth.BluetoothDevice;

import com.don11995.log.SimpleLog;

import java.util.ArrayList;

public class BLeDeviceList {
    private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<>();
    private ArrayList<byte[]> mRecords = new ArrayList<byte[]>();
    private ArrayList<Integer> mRSSIs = new ArrayList<Integer>();

    public BLeDeviceList() {
        super();
    }

    public boolean addDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (!mLeDevices.contains(device)) {
            SimpleLog.v("TODEL NOT DUPLICATED mLeDevices.add((device) " + device.toString());
            mLeDevices.add(device);
            mRSSIs.add(rssi);
            mRecords.add(scanRecord);
            return true;
        }
        return false;
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }


    public int getRssi(int index) {
        return mRSSIs.get(index);
    }

    public void clear() {
        mLeDevices.clear();
        mRSSIs.clear();
        mRecords.clear();
    }


}

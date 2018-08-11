package ua.mezon.xtrainervr.model;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.don11995.log.SimpleLog;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import ua.mezon.xtrainervr.BLeDeviceList;
import ua.mezon.xtrainervr.FullscreenActivity;
import ua.mezon.xtrainervr.R;
import ua.mezon.xtrainervr.services.GattAttributes;

import static ua.mezon.xtrainervr.FullscreenActivity.REQUEST_ENABLE_BT;
import static ua.mezon.xtrainervr.services.GattAttributes.ACTION_DATA_AVAILABLE;
import static ua.mezon.xtrainervr.services.GattAttributes.ACTION_GATT_CONNECTED;
import static ua.mezon.xtrainervr.services.GattAttributes.ACTION_GATT_DISCONNECTED;
import static ua.mezon.xtrainervr.services.GattAttributes.ACTION_GATT_SERVICES_DISCOVERED;


/**
 * Created by MezM on 05.06.2017.
 */

public class BLEProc {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String MAIN_SERVICE = "63a21f41-5c56-4677-b276-6d84e42b8fd7";
    public static final String Accel_X_CHARC = "6ecc9e4c-9848-4e30-a1e1-c5cf99fba9a9";
    public static final String Accel_Y_CHARC = "e379c733-1a49-48c7-ac61-cb27976e4c09";
    public static final String Accel_Z_CHARC = "e379c733-1a49-48c7-ac62-cb27976e4c09";
    public static final String ADDR_VTRAINER = "e805c592-b0a7-405e-ac83-e2bf56efac07";
    //attributes.put("1a8f6007-a27e-4355-8557-db16a7c92fe0", "VRTren CONTROL");

    //        attributes.put("63a21f41-5c56-4677-b276-6d84e42b8fd7", "VR Trainer Sensors Service");
//
//        attributes.put("6ecc9e4c-9848-4e30-a1e1-c5cf99fba9a9", "VR Trainer Accel X");
//        attributes.put("e379c733-1a49-48c7-ac61-cb27976e4c09", "VR Trainer Accel Y");
//        attributes.put("e379c733-1a49-48c7-ac62-cb27976e4c09", "VR Trainer Accel Z");
//        attributes.put("e805c592-b0a7-405e-ac83-e2bf56efac07", "VR Trainer Param");
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private final static String TAG = BLEProc.class.getSimpleName();
    private static final BLEProc ourInstance = new BLEProc();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private static final long SCAN_PERIOD = 30000;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE + characteristic.getStringValue(0));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int
                status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE + characteristic.getStringValue(0));
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE + characteristic.getStringValue(0));
        }
    };
    private FullscreenActivity mActivity;
    //
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private BLeDeviceList mBLeDeviceList = new BLeDeviceList();
    private Handler mHandler = new Handler();

    private BLEProc() {
    }


    public PublishSubject<String> messsubject = PublishSubject.create();
    public ReplaySubject<String> pingsubject = ReplaySubject.create();
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

            if (null != device.getUuids()) {
                SimpleLog.v("TODEL LeScanCallback called " + device.getAddress() + "<>" + device.getUuids()
                        .length + "<>" + device.getUuids().toString());
                for (int i = 0; i < device.getUuids().length; i++) {
                    SimpleLog.v("TODEL UIIDS %d List " + device.getUuids()[i].getUuid().toString(), i);

                }
            } else {
                SimpleLog.v("TODEL UIIDS IS NULL");
            }

            if (ADDR_VTRAINER.equalsIgnoreCase(device.getAddress())) {
                sendName(device, rssi, scanRecord);
                messsubject.onComplete();
                scanLeDevice(false);
                connect(device.getAddress());
            } else {
                sendName(device, rssi, scanRecord);
            }


        }
    };

    private void broadcastUpdate(String actionGattConnected) {
        pingsubject.onNext(actionGattConnected);
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            SimpleLog.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mActivity, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    private void sendName(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (mBLeDeviceList.addDevice(device, rssi, scanRecord)) {
            messsubject.onNext(GattAttributes.lookup(device.getAddress(), "NoName"));
        }

    }

    public static BLEProc getInstance() {
        return ourInstance;
    }

    public void initBLE(final FullscreenActivity activity) {
        mActivity = activity;
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(activity, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            activity.finish();
            return;
        }
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                SimpleLog.d("TODEL !mBluetoothAdapter.isEnabled() ");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }


        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        // Initializes list view adapter.
        SimpleLog.d("TODEL scanLeDevice runned");
        scanLeDevice(true);

    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            stopScanLEtimer();


            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            SimpleLog.v("TODEL startLeScan(mLeScanCallback) called ");
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            SimpleLog.v("TODEL stopLeScan(mLeScanCallback) called ");
        }
    }

    private void stopScanLEtimer() {
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                SimpleLog.v("TODEL stopLeScan(mLeScanCallback) called TIMER END ");
            }
        }, SCAN_PERIOD);
    }


    public void stopBLEScan() {
        if (mBluetoothAdapter != null) {
            scanLeDevice(false);
            if (mBLeDeviceList != null) {
                mBLeDeviceList.clear();
            }
        }
    }


}


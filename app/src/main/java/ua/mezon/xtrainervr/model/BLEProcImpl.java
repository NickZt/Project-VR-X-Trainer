package ua.mezon.xtrainervr.model;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.don11995.log.SimpleLog;

import java.util.UUID;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import ua.mezon.xtrainervr.BLEConnector;
import ua.mezon.xtrainervr.BLeDeviceList;
import ua.mezon.xtrainervr.R;
import ua.mezon.xtrainervr.services.GattAttributes;

import static ua.mezon.xtrainervr.FullscreenActivity.REQUEST_ENABLE_BT;
import static ua.mezon.xtrainervr.services.GattAttributes.ACTION_DATA_AVAILABLE;
import static ua.mezon.xtrainervr.services.GattAttributes.ACTION_GATT_CONNECTED;
import static ua.mezon.xtrainervr.services.GattAttributes.ACTION_GATT_DISCONNECTED;
import static ua.mezon.xtrainervr.services.GattAttributes.ACTION_GATT_SERVICES_DISCOVERED;
import static ua.mezon.xtrainervr.services.GattAttributes.DEVICE_NAME_STRING;
import static ua.mezon.xtrainervr.services.GattAttributes.resolveValueTypeDescription;


/**
 * Created by MezM on 05.06.2017.
 */

public class BLEProcImpl implements BLEConnector {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String MAIN_SERVICE = "1a8f6007-a27e-4355-8557-db16a7c92fe0";
    public static final String Accel_X_CHARC = "6ecc9e4c-9848-4e30-a1e1-c5cf99fba9a9";
    public static final String Accel_Y_CHARC = "e379c733-1a49-48c7-ac61-cb27976e4c09";
    public static final String Accel_Z_CHARC = "e379c733-1a49-48c7-ac62-cb27976e4c09";
    public static final String BLE_ADDR_VTRAINER = "e805c592-b0a7-405e-ac83-e2bf56efac07";
    public static final String BT_ADDR_VTRAINER = "30:AE:A4:8B:2A:5A";
    //attributes.put("1a8f6007-a27e-4355-8557-db16a7c92fe0", "VRTren CONTROL");
    public static final String MAIN_VR_TRAINER_SENSORS_SERVICE = "63a21f41-5c56-4677-b276-6d84e42b8fd7";
    //        attributes.put("63a21f41-5c56-4677-b276-6d84e42b8fd7", "VR Trainer Sensors Service");
//
//        attributes.put("6ecc9e4c-9848-4e30-a1e1-c5cf99fba9a9", "VR Trainer Accel X");
//        attributes.put("e379c733-1a49-48c7-ac61-cb27976e4c09", "VR Trainer Accel Y");
//        attributes.put("e379c733-1a49-48c7-ac62-cb27976e4c09", "VR Trainer Accel Z");
//        attributes.put("e805c592-b0a7-405e-ac83-e2bf56efac07", "VR Trainer Param");
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private final static String TAG = BLEProcImpl.class.getSimpleName();
    private static final BLEProcImpl ourInstance = new BLEProcImpl();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private static final long SCAN_PERIOD = 30000;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private AppCompatActivity mActivity;
    private PublishSubject<String> mMesssubject = PublishSubject.create();
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                SimpleLog.v("TODEL Connected to GATT server.");
                // Attempts to discover services after successful connection.
                SimpleLog.v("TODEL Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                SimpleLog.v("TODEL Disconnected from GATT server.");
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED + gatt.getServices().toString());

                //   findAndOnNotify(gatt, MAIN_VR_TRAINER_SENSORS_SERVICE, Accel_X_CHARC);
                //   findAndOnNotify(gatt, MAIN_VR_TRAINER_SENSORS_SERVICE, Accel_Z_CHARC);

                for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
                    SimpleLog.v("TODEL L1 onServicesDiscovered received Service.getUuid(): " +
                            bluetoothGattService.getUuid().toString() + "{}" + GattAttributes
                            .lookup(bluetoothGattService.getUuid().toString(), "NoName"));


                    if (MAIN_SERVICE.equalsIgnoreCase(bluetoothGattService.getUuid().toString())) {
                        SimpleLog.v("TODEL MAIN_SERVICE..equals  bluetoothGattService.getUuid()");
                        deviceNameFinded(gatt.getDevice().getName());
                        deviceMainVRFinded(bluetoothGattService);
                    }

                    for (BluetoothGattCharacteristic characteristic : bluetoothGattService
                            .getCharacteristics()) {

                        SimpleLog.v("TODEL L2 received  characteristic.:.: " +
                                characteristic.getUuid().toString() + "{lookup}" + (GattAttributes
                                .lookup(characteristic.getUuid().toString(), "NoName")) +
                                "{describeContents}" +
                                resolveValueTypeDescription(characteristic.getProperties()));

                        if (DEVICE_NAME_STRING.equals(characteristic.getUuid())) {
                            SimpleLog.v("TODEL DEVICE_NAME_STRING.equals  bluetoothGattDescriptor.: " +
                                    "getProperties>" +
                                    resolveValueTypeDescription(characteristic.getProperties
                                            ()) +
                                    "{}");
                            if (characteristic.getStringValue(0) != null) {
                                deviceNameFinded(gatt.getDevice().getName());
                            }
                        }
                        if (Accel_Y_CHARC.equalsIgnoreCase(characteristic.getUuid().toString())) {
                            SimpleLog.v("TODEL L2 Accel_Y_CHARC.equals  " +
                                    resolveValueTypeDescription(characteristic
                                            .getProperties()) +
                                    "{}" + characteristic.getInstanceId());
                            boolean enabled = true;

                            setORcleanChangeNotificationAboutCharacteristic(gatt,
                                    characteristic, enabled);

                        }

                        for (BluetoothGattDescriptor bluetoothGattDescriptor : characteristic
                                .getDescriptors()) {

                            SimpleLog.v("TODEL L3 bluetoothGattDescriptor.:.getUuid(): " +
                                    bluetoothGattDescriptor.getUuid().toString() + "{lookup}" + (GattAttributes
                                    .lookup(bluetoothGattDescriptor.getUuid().toString(), "NoName")) +
                                    "{describeContents}" +
                                    resolveValueTypeDescription(bluetoothGattDescriptor.describeContents()));


                        }

                    }


                }
            } else {
                SimpleLog.v("TODEL  onServicesDiscovered NO SUCCES received: " + status);
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
            broadcastUpdate(ACTION_DATA_AVAILABLE + characteristic.getStringValue(0)); //characteristic
            // .getFloatValue(FORMAT_FLOAT,0)
            SimpleLog.d("TODEL onCharacteristicChanged() called with: gatt = [" + gatt.getDevice().getName()
                    + "], " +
                    "characteristic = [" +
                    GattAttributes.lookup(characteristic.getUuid().toString(), "NoName"));


        }
    };
    private ReplaySubject<String> mPingsubject = ReplaySubject.create();
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
                //   SimpleLog.v("TODEL UIIDS IS NULL");
            }

            if (BT_ADDR_VTRAINER.equalsIgnoreCase(device.getAddress())) {
                //set this if wanna to connect specific device only
                scanLeDevice(false);
                addNUnicCheckDeviceToDeepScan(device, rssi, scanRecord);


            } else {
                //   addNUnicCheckDeviceToDeepScan(device, rssi, scanRecord);
            }


        }
    };

    private BLEProcImpl() {
    }

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private BLeDeviceList mBLeDeviceList = new BLeDeviceList();
    private Handler mHandler = new Handler();

    public static BLEProcImpl getInstance() {
        return ourInstance;
    }

    private void findAndOnNotify(BluetoothGatt gatt, String mainService, String accel_Y_CHARC) {
        BluetoothGattCharacteristic charact = gatt.getService(UUID.fromString(mainService))
                .getCharacteristic(UUID.fromString(accel_Y_CHARC));

        SimpleLog.e("TODEL charact == null");
        if (charact == null) {
            SimpleLog.e("TODEL charact == null");
        } else {
            setORcleanChangeNotificationAboutCharacteristic(gatt,
                    charact, true);
        }
    }

    private void setORcleanChangeNotificationAboutCharacteristic(BluetoothGatt gatt,
                                                                 BluetoothGattCharacteristic
                                                                         bluetoothGattCharacteristic, boolean
                                                                         enabled) {
        gatt.setCharacteristicNotification(bluetoothGattCharacteristic, enabled);

        BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(
                GattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
        SimpleLog.v("TODEL setORcleanChangeNotificationAboutCharacteristic: " +
                bluetoothGattCharacteristic.getUuid().toString() + "{lookup}" + (GattAttributes
                .lookup(bluetoothGattCharacteristic.getUuid().toString(), "NoName")));


    }

    private void deviceMainVRFinded(BluetoothGattService bluetoothGattService) {
        SimpleLog.v("TODEL deviceMainVRFinded() called with: bluetoothGattService = [" + bluetoothGattService
                .getUuid() + "]");
        //   mMesssubject.onComplete();
        //  scanLeDevice(false);
    }

    private void broadcastUpdate(String actionGattConnected) {
        // mPingsubject.onNext(actionGattConnected);
        mMesssubject.onNext(actionGattConnected);
    }

    private void addNUnicCheckDeviceToDeepScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (mBLeDeviceList.addDevice(device, rssi, scanRecord)) {
            SimpleLog.v("TODEL addNUnicCheckDeviceToDeepScan() called with: device = [" + device + "], rssi =" +
                    " [" + rssi + "], " +
                    "scanRecord " +
                    "= [" + scanRecord.toString() + "]");
            deviceNameFinded(device.getAddress());
            connect(device.getAddress());
        }

    }

    private void deviceNameFinded(String stringValue) {
        mMesssubject.onNext(stringValue);
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
            SimpleLog.w("TODEL BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            SimpleLog.w("TODEL Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            SimpleLog.w("TODEL Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mActivity, false, mGattCallback);
        SimpleLog.w("TODEL Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void initBLE(final AppCompatActivity activity) {
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

    public PublishSubject<String> getMesssubject() {
        return mMesssubject;
    }

    public void setMesssubject(PublishSubject<String> messsubject) {
        mMesssubject = messsubject;
    }

    public ReplaySubject<String> getPingsubject() {
        return mPingsubject;
    }

    public void setPingsubject(ReplaySubject<String> pingsubject) {
        mPingsubject = pingsubject;
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            stopScanLEtimer();
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            SimpleLog.v("TODEL startLeScan(mLeScanCallback) called ");
        } else {
            stopScan();
            SimpleLog.v("TODEL stopLeScan(mLeScanCallback) called ");
        }
    }

    private void stopScanLEtimer() {
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
                SimpleLog.v("TODEL stopLeScan(mLeScanCallback) called TIMER END ");
            }
        }, SCAN_PERIOD);
    }

    private void stopScan() {
        mScanning = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
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


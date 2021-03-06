package ua.mezon.xtrainervr.services;

import android.util.SparseArray;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class includes a  subset of standard GATT attributes plus wrk names.
 */
public class GattAttributes {
    public final static String ACTION_GATT_CONNECTED =
            "ua.mezon.xtrainervr.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "ua.mezon.xtrainervr.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "ua.mezon.xtrainervr.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "ua.mezon.xtrainervr.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "ua.mezon.xtrainervr.EXTRA_DATA";

    final static public UUID DEVICE_NAME_STRING = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");
    final static public UUID MANUFACTURER_STRING = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    final static public UUID MODEL_NUMBER_STRING = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
    final static public UUID FIRMWARE_REVISION_STRING = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    final static public UUID APPEARANCE = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");
    final static public UUID BODY_SENSOR_LOCATION = UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb");
    final static public UUID BATTERY_LEVEL = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static HashMap<String, String> attributes = new HashMap();
    private static SparseArray<String> mValueFormats = new SparseArray<String>();

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");

        attributes.put("00001811-0000-1000-8000-00805f9b34fb", "Alert Notification Service");
        attributes.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");
        attributes.put("00001810-0000-1000-8000-00805f9b34fb", "Blood Pressure");
        attributes.put("00001805-0000-1000-8000-00805f9b34fb", "Current Time Service");
        attributes.put("00001818-0000-1000-8000-00805f9b34fb", "Cycling Power");
        attributes.put("00001816-0000-1000-8000-00805f9b34fb", "Cycling Speed and Cadence");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute");
        attributes.put("00001808-0000-1000-8000-00805f9b34fb", "Glucose");
        attributes.put("00001809-0000-1000-8000-00805f9b34fb", "Health Thermometer");
        attributes.put("00001812-0000-1000-8000-00805f9b34fb", "Human Interface Device");
        attributes.put("00001802-0000-1000-8000-00805f9b34fb", "Immediate Alert");
        attributes.put("00001803-0000-1000-8000-00805f9b34fb", "Link Loss");
        attributes.put("00001819-0000-1000-8000-00805f9b34fb", "Location and Navigation");
        attributes.put("00001807-0000-1000-8000-00805f9b34fb", "Next DST Change Service");
        attributes.put("0000180e-0000-1000-8000-00805f9b34fb", "Phone Alert Status Service");
        attributes.put("00001806-0000-1000-8000-00805f9b34fb", "Reference Time Update Service");
        attributes.put("00001814-0000-1000-8000-00805f9b34fb", "Running Speed and Cadence");
        attributes.put("00001813-0000-1000-8000-00805f9b34fb", "Scan Parameters");
        attributes.put("00001804-0000-1000-8000-00805f9b34fb", "Tx Power");

        attributes.put("4fafc201-1fb5-459e-8fcc-c5c9c331914b", "LED CONTROL");
        attributes.put("4a78b8dd-a43d-46cf-9270-f6b750a717c8", "Ambient Light");

        attributes.put("45ecddcf-c316-488d-8558-3222e5cb9b3c", "Environmental Sensors Service");
        attributes.put("beb5483e-36e1-4688-b7f5-ea07361b26a8", "LED SWITCH");
        attributes.put("d240dbed-7d22-45bb-b810-add58a6c856b", "Temperature");
        attributes.put("3abcedb6-e652-4415-9741-ebecc11c4580", "Humidity");
        attributes.put("00002a43-0000-1000-8000-00805f9b34fb", "Alert Category ID");
        attributes.put("00002a42-0000-1000-8000-00805f9b34fb", "Alert Category ID Bit Mask");
        attributes.put("00002a06-0000-1000-8000-00805f9b34fb", "Alert Level");
        attributes.put("00002a44-0000-1000-8000-00805f9b34fb", "Alert Notification Control Point");
        attributes.put("00002a3f-0000-1000-8000-00805f9b34fb", "Alert Status");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        attributes.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level");
        attributes.put("00002a49-0000-1000-8000-00805f9b34fb", "Blood Pressure Feature");
        attributes.put("00002a35-0000-1000-8000-00805f9b34fb", "Blood Pressure Measurement");
        attributes.put("00002a38-0000-1000-8000-00805f9b34fb", "Body Sensor Location");
        attributes.put("00002a22-0000-1000-8000-00805f9b34fb", "Boot Keyboard Input Report");
        attributes.put("00002a32-0000-1000-8000-00805f9b34fb", "Boot Keyboard Output Report");
        attributes.put("00002a33-0000-1000-8000-00805f9b34fb", "Boot Mouse Input Report");
        attributes.put("00002a5c-0000-1000-8000-00805f9b34fb", "CSC Feature");
        attributes.put("00002a5b-0000-1000-8000-00805f9b34fb", "CSC Measurement");
        attributes.put("00002a2b-0000-1000-8000-00805f9b34fb", "Current Time");
        attributes.put("00002a66-0000-1000-8000-00805f9b34fb", "Cycling Power Control Point");
        attributes.put("00002a65-0000-1000-8000-00805f9b34fb", "Cycling Power Feature");
        attributes.put("00002a63-0000-1000-8000-00805f9b34fb", "Cycling Power Measurement");
        attributes.put("00002a64-0000-1000-8000-00805f9b34fb", "Cycling Power Vector");
        attributes.put("00002a08-0000-1000-8000-00805f9b34fb", "Date Time");
        attributes.put("00002a0a-0000-1000-8000-00805f9b34fb", "Day Date Time");
        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        attributes.put("00002a09-0000-1000-8000-00805f9b34fb", "Day of Week");
        attributes.put("00002a0d-0000-1000-8000-00805f9b34fb", "DST Offset");
        attributes.put("00002a0c-0000-1000-8000-00805f9b34fb", "Exact Time 256");
        attributes.put("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision String");
        attributes.put("00002a51-0000-1000-8000-00805f9b34fb", "Glucose Feature");
        attributes.put("00002a18-0000-1000-8000-00805f9b34fb", "Glucose Measurement");
        attributes.put("00002a34-0000-1000-8000-00805f9b34fb", "Glucose Measurement Context");
        attributes.put("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision String");
        attributes.put("00002a39-0000-1000-8000-00805f9b34fb", "Heart Rate Control Point");
        attributes.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        attributes.put("00002a4c-0000-1000-8000-00805f9b34fb", "HID Control Point");
        attributes.put("00002a4a-0000-1000-8000-00805f9b34fb", "HID Information");
        attributes.put("00002a2a-0000-1000-8000-00805f9b34fb", "IEEE 11073-20601 Regulatory Certification " +
                "Data List");
        attributes.put("00002a36-0000-1000-8000-00805f9b34fb", "Intermediate Cuff Pressure");
        attributes.put("00002a1e-0000-1000-8000-00805f9b34fb", "Intermediate Temperature");
        attributes.put("00002a6b-0000-1000-8000-00805f9b34fb", "LN Control Point");
        attributes.put("00002a6a-0000-1000-8000-00805f9b34fb", "LN Feature");
        attributes.put("00002a0f-0000-1000-8000-00805f9b34fb", "Local Time Information");
        attributes.put("00002a67-0000-1000-8000-00805f9b34fb", "Location and Speed");
        attributes.put("00002a21-0000-1000-8000-00805f9b34fb", "Measurement Interval");
        attributes.put("00002a24-0000-1000-8000-00805f9b34fb", "Model Number String");
        attributes.put("00002a68-0000-1000-8000-00805f9b34fb", "Navigation");
        attributes.put("00002a46-0000-1000-8000-00805f9b34fb", "New Alert");
        attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "Peripheral Preferred Connection Parameters");
        attributes.put("00002a02-0000-1000-8000-00805f9b34fb", "Peripheral Privacy Flag");
        attributes.put("00002a50-0000-1000-8000-00805f9b34fb", "PnP ID");
        attributes.put("00002a69-0000-1000-8000-00805f9b34fb", "Position Quality");
        attributes.put("00002a4e-0000-1000-8000-00805f9b34fb", "Protocol Mode");
        attributes.put("00002a03-0000-1000-8000-00805f9b34fb", "Reconnection Address");
        attributes.put("00002a52-0000-1000-8000-00805f9b34fb", "Record Access Control Point");
        attributes.put("00002a14-0000-1000-8000-00805f9b34fb", "Reference Time Information");
        attributes.put("00002a4d-0000-1000-8000-00805f9b34fb", "Report");
        attributes.put("00002a4b-0000-1000-8000-00805f9b34fb", "Report Map");
        attributes.put("00002a40-0000-1000-8000-00805f9b34fb", "Ringer Control Point");
        attributes.put("00002a41-0000-1000-8000-00805f9b34fb", "Ringer Setting");
        attributes.put("00002a54-0000-1000-8000-00805f9b34fb", "RSC Feature");
        attributes.put("00002a53-0000-1000-8000-00805f9b34fb", "RSC Measurement");
        attributes.put("00002a55-0000-1000-8000-00805f9b34fb", "SC Control Point");
        attributes.put("00002a4f-0000-1000-8000-00805f9b34fb", "Scan Interval Window");
        attributes.put("00002a31-0000-1000-8000-00805f9b34fb", "Scan Refresh");
        attributes.put("00002a5d-0000-1000-8000-00805f9b34fb", "Sensor Location");
        attributes.put("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");
        attributes.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");
        attributes.put("00002a28-0000-1000-8000-00805f9b34fb", "Software Revision String");
        attributes.put("00002a47-0000-1000-8000-00805f9b34fb", "Supported New Alert Category");
        attributes.put("00002a48-0000-1000-8000-00805f9b34fb", "Supported Unread Alert Category");
        attributes.put("00002a23-0000-1000-8000-00805f9b34fb", "System ID");
        attributes.put("00002a1c-0000-1000-8000-00805f9b34fb", "Temperature Measurement");
        attributes.put("00002a1d-0000-1000-8000-00805f9b34fb", "Temperature Type");
        attributes.put("00002a12-0000-1000-8000-00805f9b34fb", "Time Accuracy");
        attributes.put("00002a13-0000-1000-8000-00805f9b34fb", "Time Source");
        attributes.put("00002a16-0000-1000-8000-00805f9b34fb", "Time Update Control Point");
        attributes.put("00002a17-0000-1000-8000-00805f9b34fb", "Time Update State");
        attributes.put("00002a11-0000-1000-8000-00805f9b34fb", "Time with DST");
        attributes.put("00002a0e-0000-1000-8000-00805f9b34fb", "Time Zone");
        attributes.put("00002a07-0000-1000-8000-00805f9b34fb", "Tx Power Level");
        attributes.put("00002a45-0000-1000-8000-00805f9b34fb", "Unread Alert Status");
        attributes.put("00002AA6-0000-1000-8000-00805f9b34fb", "Central Address Resolution");


        mValueFormats.put(Integer.valueOf(52), "32bit float");
        mValueFormats.put(Integer.valueOf(50), "16bit float");
        mValueFormats.put(Integer.valueOf(34), "16bit signed int");
        mValueFormats.put(Integer.valueOf(36), "32bit signed int");
        mValueFormats.put(Integer.valueOf(33), "8bit signed int");
        mValueFormats.put(Integer.valueOf(18), "16bit unsigned int");
        mValueFormats.put(Integer.valueOf(20), "32bit unsigned int");
        mValueFormats.put(Integer.valueOf(17), "8bit unsigned int");
        mValueFormats.put(0, "Z Unknown");
        mValueFormats.put(64, "Generic Phone");
        mValueFormats.put(128, "Generic Computer");
        mValueFormats.put(192, "Generic Watch");
        mValueFormats.put(193, "Watch: Sports Watch");
        mValueFormats.put(256, "Generic Clock");
        mValueFormats.put(320, "Generic Display");
        mValueFormats.put(384, "Generic Remote Control");
        mValueFormats.put(448, "Generic Eye-glasses");
        mValueFormats.put(512, "Generic Tag");
        mValueFormats.put(576, "Generic Keyring");
        mValueFormats.put(640, "Generic Media Player");
        mValueFormats.put(704, "Generic Barcode Scanner");
        mValueFormats.put(768, "Generic Thermometer");
        mValueFormats.put(769, "Thermometer: Ear");
        mValueFormats.put(832, "Generic Heart rate Sensor");
        mValueFormats.put(833, "Heart Rate Sensor: Heart Rate Belt");
        mValueFormats.put(896, "Generic Blood Pressure");
        mValueFormats.put(897, "Blood Pressure: Arm");
        mValueFormats.put(898, "Blood Pressure: Wrist");
        mValueFormats.put(960, "Human Interface Device (HID)");
        mValueFormats.put(961, "Keyboard");
        mValueFormats.put(962, "Mouse");
        mValueFormats.put(963, "Joystick");
        mValueFormats.put(964, "Gamepad");
        mValueFormats.put(965, "Digitizer Tablet");
        mValueFormats.put(966, "Card Reader");
        mValueFormats.put(967, "Digital Pen");
        mValueFormats.put(968, "Barcode Scanner");
        mValueFormats.put(1024, "Generic Glucose Meter");
        mValueFormats.put(1088, "Generic: Running Walking Sensor");
        mValueFormats.put(1089, "Running Walking Sensor: In-Shoe");
        mValueFormats.put(1090, "Running Walking Sensor: On-Shoe");
        mValueFormats.put(1091, "Running Walking Sensor: On-Hip");
        mValueFormats.put(1152, "Generic: Cycling");
        mValueFormats.put(1153, "Cycling: Cycling Computer");
        mValueFormats.put(1154, "Cycling: Speed Sensor");
        mValueFormats.put(1155, "Cycling: Cadence Sensor");
        mValueFormats.put(1156, "Cycling: Power Sensor");
        mValueFormats.put(1157, "Cycling: Speed and Cadence Sensor");
        mValueFormats.put(3136, "Generic: Pulse Oximeter");
        mValueFormats.put(3137, "Fingertip");
        mValueFormats.put(3138, "Wrist Worn");
        mValueFormats.put(3200, "Generic: Weight Scale");
        mValueFormats.put(5184, "Generic: Outdoor Sports Activity");
        mValueFormats.put(5185, "Location Display Device");
        mValueFormats.put(5186, "Location and Navigation Display Device");
        mValueFormats.put(5187, "Location Pod");
        mValueFormats.put(5188, "Location and Navigation Pod");

        attributes.put("1a8f6007-a27e-4355-8557-db16a7c92fe0", "VRTren CONTROL");

        attributes.put("63a21f41-5c56-4677-b276-6d84e42b8fd7", "VR Trainer Sensors Service");

        attributes.put("6ecc9e4c-9848-4e30-a1e1-c5cf99fba9a9", "VR Trainer Accel X");
        attributes.put("e379c733-1a49-48c7-ac61-cb27976e4c09", "VR Trainer Accel Y");
        attributes.put("e379c733-1a49-48c7-ac62-cb27976e4c09", "VR Trainer Accel Z");
        attributes.put("e805c592-b0a7-405e-ac83-e2bf56efac07", "VR Trainer Param");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

    static public String resolveValueTypeDescription(final int format) {
        Integer tmp = Integer.valueOf(format);
        return mValueFormats.get(tmp, "Unknown Format " + tmp);
    }

}
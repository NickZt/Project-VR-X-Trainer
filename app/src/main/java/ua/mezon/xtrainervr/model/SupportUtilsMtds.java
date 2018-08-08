package ua.mezon.xtrainervr.model;

import android.os.Environment;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * Created by MezM on 05.06.2017.
 */

public class SupportUtilsMtds {

    // Shared preferences file name
    public static final String PREF_NAME = "vrxtrainer";

    public static final String IS_sVolume = "IssVolume";
    public static final int PRIVATE_MODE = 0;


    public static final String FILE_EXT_PLAY = ".mp4";
    public static final String ROOT_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/VRTren/";
    public static final String SD_DIR = Environment.getExternalStorageDirectory().toString() + "/Downloads"+ "/VRTren/";
    public static final int READ_EXTERNAL_STORAGE_PERMISSION_CODE = 1;
    public static final int READ_BLUETOOTH_PERMISSION_CODE = 2;
    private static final String TAG = "TODEL";


    /* Проверяет, доступно ли external storage как минимум для чтения */
    public boolean isExternalStorageReadable()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
    public static String getLocalIpAddress() { //getIpV4Address
        //   TODO: Main detect method
        //паттерн для проверки является ли это адресом в формате IPv4
        Pattern pattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
        String result = "address-unknown";  //"http://"+"address-unknown" + ":"
        try {
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
                Enumeration inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) inetAddresses.nextElement();
                    String ip = inetAddress.getHostAddress();
                    if (!pattern.matcher(ip).matches()) {
                        continue;
                    }
                    if (!ip.startsWith("127")) {//пропускаем неуникальные IP
                        result = "http://" + ip + ":"; //"http://" + formatedIpAddress + ":"
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        Log.i(TAG, result);
        return result;
    }
}

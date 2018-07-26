package ua.mezon.xtrainervr.model;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import ua.mezon.xtrainervr.services.BluetoothLeService;


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
    public static final String Param_CHARC = "e805c592-b0a7-405e-ac83-e2bf56efac07";
    //attributes.put("1a8f6007-a27e-4355-8557-db16a7c92fe0", "VRTren CONTROL");

//        attributes.put("63a21f41-5c56-4677-b276-6d84e42b8fd7", "VR Trainer Sensors Service");
//
//        attributes.put("6ecc9e4c-9848-4e30-a1e1-c5cf99fba9a9", "VR Trainer Accel X");
//        attributes.put("e379c733-1a49-48c7-ac61-cb27976e4c09", "VR Trainer Accel Y");
//        attributes.put("e379c733-1a49-48c7-ac62-cb27976e4c09", "VR Trainer Accel Z");
//        attributes.put("e805c592-b0a7-405e-ac83-e2bf56efac07", "VR Trainer Param");

    private final static String TAG = BLEProc.class.getSimpleName();
    private static final BLEProc ourInstance = new BLEProc();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private BluetoothLeService mBluetoothLeService;


    private BLEProc() {
    }




    public PublishSubject<String> messsubject = PublishSubject.create();
    public ReplaySubject<String> pingsubject = ReplaySubject.create();

//


    public static BLEProc getInstance() {
        return ourInstance;
    }


}


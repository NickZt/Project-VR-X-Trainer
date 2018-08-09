package ua.mezon.xtrainervr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDPinchConfig;
import com.don11995.log.SimpleLog;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import ua.mezon.xtrainervr.model.BLEProc;
import ua.mezon.xtrainervr.model.SupportUtilsMtds;

import static ua.mezon.xtrainervr.model.SupportUtilsMtds.IS_sVolume;
import static ua.mezon.xtrainervr.model.SupportUtilsMtds.PREF_NAME;
import static ua.mezon.xtrainervr.model.SupportUtilsMtds.PRIVATE_MODE;
import static ua.mezon.xtrainervr.model.SupportUtilsMtds.READ_BLUETOOTH_PERMISSION_CODE;
import static ua.mezon.xtrainervr.model.SupportUtilsMtds.READ_EXTERNAL_STORAGE_PERMISSION_CODE;


public class FullscreenActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 12;
    private static final long SCAN_PERIOD = 10000;
    private static final String URLEXT = "file:///storage/external_SD/Download/VRTren/VikingCoaster(3D).mp4";
    //4.avi
    private static final String TAG = "VRTrainer";
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final String URL0 = "file://";
    private static final boolean AUTO_HIDE = true;
    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            dialogSetupPrefs();
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }


            return false;
        }
    };
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private BLeDeviceList mBLeDeviceList = new BLeDeviceList();
    private Handler mHandler = new Handler();
    private TextView mConnectionState;
    private ListView mDataField;
    private String mDeviceName;
    private MediaPlayerWrapper mMediaPlayerWrapper = new MediaPlayerWrapper();
    private boolean bMediaPlayerWrapperisWorked = false;
    private MDVRLibrary mVRLibrary;
    private BLEProc mBLEProc;
    private View mContentView_L, mContentView_R;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView_L.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private TextView mTextContentView_L, mTextContentView_R;
    private String mDeviceAddress;
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SimpleLog.v("TODEL LeScanCallback called " + device.toString());
                            mBLeDeviceList.addDevice(device);
                        }
                    });
                }
            };
    private int sVolume = 20;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private GLSurfaceView tmpGLSurfaceView;
    private String mDeviceNAME = "b";
    private boolean mbMediaPlayerWrapperisPaused = false;


    /* check if user agreed to enable BT */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // user didn't want to turn on BT
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private BleWrapper mBleWrapper = null;

    @Override
    protected void onResume() {
        super.onResume();

        // onresumePermissionCheck();
        if (mVRLibrary != null) {
            mVRLibrary.onResume(this);
        }
        if (mMediaPlayerWrapper != null) {
            mMediaPlayerWrapper.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVRLibrary != null) {
            mVRLibrary.onPause(this);
        }
        if (mMediaPlayerWrapper != null) {
            mMediaPlayerWrapper.pause();
        }
        if (mBluetoothAdapter != null) {
            scanLeDevice(false);
            if (mBLeDeviceList != null) {
                mBLeDeviceList.clear();
            }
        }

    }

    private void loadValprefs() {
        // sVolume

        pref = getSharedPreferences(PREF_NAME, PRIVATE_MODE);

//        mBLEProc.sCliName = pref.getString(IS_CliName, mBLEProc.sCliName);
        sVolume = pref.getInt(IS_sVolume, sVolume);


    }

    public void cancelBusy() {
        showFullMess("");
    }


    public MDVRLibrary getVRLibrary() {
        return mVRLibrary;
    }

    /*************
     * Events
     *************/


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == SupportUtilsMtds.READ_EXTERNAL_STORAGE_PERMISSION_CODE ||
                requestCode == SupportUtilsMtds.READ_BLUETOOTH_PERMISSION_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do it
                // filelist();
                setuptheSurfacevideo();
                initBLE();

            } else {

                // permission denied, boo!
                finish();
            }
            return;
        }
    }

    private void initBLE() {
        // initialize BleWrapper object
        mBleWrapper.initialize();


        // Automatically start scanning for devices
        mScanning = true;
        // remember to add timeout for scanning to not run it forever and drain the battery
        addScanningTimeout();
        SimpleLog.d("TODEL scanLeDevice runned");
        mBleWrapper.startScanning();

        invalidateOptionsMenu();


//
//        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
//        // BluetoothAdapter through BluetoothManager.
//        final BluetoothManager bluetoothManager =
//                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        mBluetoothAdapter = bluetoothManager.getAdapter();
//
//        // Checks if Bluetooth is supported on the device.
//        if (mBluetoothAdapter == null) {
//            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
//            SimpleLog.d("TODEL Ble not supported ");
//            finish();
//            return;
//        }
//        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
//        // fire an intent to display a dialog asking the user to grant permission to enable it.
//        if (!mBluetoothAdapter.isEnabled()) {
//            if (!mBluetoothAdapter.isEnabled()) {
//                SimpleLog.d("TODEL !mBluetoothAdapter.isEnabled() ");
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//            }
//        }
//        mBluetoothAdapter.stopLeScan(mLeScanCallback);
//        SimpleLog.d("TODEL scanLeDevice runned");
//        scanLeDevice(true);

    }

    private void addScanningTimeout() {
        Runnable timeout = new Runnable() {
            @Override
            public void run() {
                if (mBleWrapper == null) {
                    return;
                }
                mScanning = false;
                mBleWrapper.stopScanning();
                invalidateOptionsMenu();
            }
        };
        mHandler.postDelayed(timeout, SCAN_PERIOD);


//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mScanning = false;
//                mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                SimpleLog.v("TODEL stopLeScan(mLeScanCallback) called  time is end");
//                invalidateOptionsMenu();
//            }
//        }, SCAN_PERIOD);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {


            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            SimpleLog.v("TODEL startLeScan(mLeScanCallback) called ");
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            SimpleLog.v("TODEL stopLeScan(mLeScanCallback) called ");
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bleMissing();
        }


        mBLEProc = BLEProc.getInstance();


        loadValprefs();
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView_L = findViewById(R.id.fullscreen_content_L);
        mContentView_R = findViewById(R.id.fullscreen_content_R);
        mTextContentView_L = (TextView) mContentView_L;
        mTextContentView_R = (TextView) mContentView_R;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView_L.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        // create BleWrapper with empty callback object except uiDeficeFound function (we need only that here)
        mBleWrapper = new BleWrapper(this, new BleWrapperUiCallbacks.Null() {
            @Override
            public void uiDeviceFound(final BluetoothDevice device, final int rssi, final byte[] record) {
                handleFoundDevice(device, rssi, record);
            }
        });

        // check if we have BT and BLE on board
        if (mBleWrapper.checkBleHardwareAvailable() == false) {
            bleMissing();
        }

        checkPermission();


        //  receiveMessagesFromBLE();
    }

    private void bleMissing() {
        Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void handleFoundDevice(final BluetoothDevice device, int rssi, byte[] record) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SimpleLog.v("TODEL handleFoundDevice called " + device.toString());
                mBLeDeviceList.addDevice(device);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVRLibrary != null) {
            mVRLibrary.onDestroy();
        }
        if (mMediaPlayerWrapper != null) {
            mMediaPlayerWrapper.destroy();
        }
    }

    private void checkPermission() {
        //check for permission
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            // finish();
        }
        boolean tmpba = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED);
        Log.d(TAG, "TODEL checkPermission(LUETOOTH_ADMIN) called PackageManager.PERMISSION_DENIED" + tmpba);
        boolean tmpb = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED);
        Log.d(TAG, "TODEL checkPermission() called PERMISSION_DENIED " + tmpb);
        if (tmpb) {
            //ask for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH, Manifest.permission
                                .BLUETOOTH_ADMIN},
                        READ_BLUETOOTH_PERMISSION_CODE);
            }
        }
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager
                .PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) !=
                PackageManager.PERMISSION_GRANTED)) {
            //ask for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest
                                .permission.BLUETOOTH, Manifest.permission
                                .BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION},
                        READ_EXTERNAL_STORAGE_PERMISSION_CODE);
            }
        } else {
            SimpleLog.v("TODEL OK STARTED");
            setuptheSurfacevideo();
            initBLE();

        }
    }

    private void setuptheSurfacevideo() {
        // set up the Surface video sink
        init();
        mMediaPlayerWrapper.init();
        mMediaPlayerWrapper.setPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                cancelBusy();
                if (getVRLibrary() != null) {
                    getVRLibrary().notifyPlayerChanged();
                }
            }
        });
        mMediaPlayerWrapper.setmCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                Toast.makeText(FullscreenActivity.this, "The end", Toast.LENGTH_SHORT).show();

                resetMediaPlay();
            }
        });


        mMediaPlayerWrapper.getPlayer().setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                String error = String.format("Play Error what=%d extra=%d", what, extra);
                Toast.makeText(FullscreenActivity.this, error, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mMediaPlayerWrapper.getPlayer().setOnVideoSizeChangedListener(new IMediaPlayer
                .OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                getVRLibrary().onTextureResize(width, height);
            }
        });
    }

    private void resetMediaPlay() {
        if (bMediaPlayerWrapperisWorked) {//mMediaPlayerWrapper.getPlayer().isPlaying(
            mMediaPlayerWrapper.pause();
            mMediaPlayerWrapper.stop();
            mMediaPlayerWrapper.destroy();
            mMediaPlayerWrapper.init();
            bMediaPlayerWrapperisWorked = false;


            if (mDeviceNAME != "") {
                showFullMess("Connected to device NAME>" + mDeviceNAME);
            } else {
                showFullMess("VR-X-Trainer");
            }
        }
    }

    protected void init() {
        tmpGLSurfaceView = findViewById(R.id.surfaceview);
        mVRLibrary = MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        mMediaPlayerWrapper.setSurface(surface);
                    }
                })
                .ifNotSupport(new MDVRLibrary.INotSupportCallback() {
                    @Override
                    public void onNotSupport(int mode) {
                        String tip = mode == MDVRLibrary.INTERACTIVE_MODE_MOTION
                                ? "onNotSupport:MOTION" : "onNotSupport:" + String.valueOf(mode);
                        Toast.makeText(FullscreenActivity.this, tip, Toast.LENGTH_SHORT).show();
                    }
                })
                .pinchConfig(new MDPinchConfig().setMin(1.0f).setMax(8.0f).setDefaultValue(0.1f))
                .pinchEnabled(true)
                .directorFactory(new MD360DirectorFactory() {
                    @Override
                    public MD360Director createDirector(int index) {
                        return MD360Director.builder().setPitch(90).build();
                    }
                })
                .projectionFactory(new CustomProjectionFactory())
                .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(false).setScale(0.95f))
                .build(findViewById(R.id.surfaceview));


    }


    public void receiveMessagesFromBLE() {

        mBLEProc.messsubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
//                .subscribeOn(Schedulers.computation())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String v) {
//                        Log.v(TAG, "accept() mBLEProc.messsubject called with: v = [" + v + "]");
                        //  mServerItemsAdapter .reset();
//                        if (v.regionMatches(true, 0, CommandSymbol, 0, 2)) {
//                            doMessSelector(v.substring(2));
//                        } else {
//
//                            showFullMess("mess>" + v);
//                        }

                    }
                });
        mBLEProc.pingsubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())

                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String v) {
                        Log.v(TAG, "accept() mBLEProc.pingsubject called with: v = [" + v + "]");


                    }
                });

    }

    private void startFile(String substring) {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!mbMediaPlayerWrapperisPaused) {
            resetMediaPlay();
            setPlayer(substring);

        } else {
            Log.d(TAG, "startFile:  mMediaPlayerWrapper.resume();");
            mMediaPlayerWrapper.resume();
            mbMediaPlayerWrapperisPaused = false;
        }


    }

    private void setPlayer(String strmedia) {
        if (strmedia == null) {
            return;
        }
        Uri media = Uri.parse(URL0 + SupportUtilsMtds.ROOT_DIR + strmedia);

        if (!TextUtils.isEmpty(media.toString())) {
//                Toast toast = Toast.makeText(this, media.toString(), Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
//                toast.show();
        }
        tuneMPlay(strmedia);
        startPlay(media);


    }

    private void pauseFile() {
        if (!mbMediaPlayerWrapperisPaused) {
            mMediaPlayerWrapper.pause();
            mbMediaPlayerWrapperisPaused = true;
        }
    }

    public void showFullMess(String tmp) {

        mTextContentView_L.setText(tmp);
        mTextContentView_R.setText(tmp);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);


    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView_L.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void tuneMPlay(String strmedia) {
        mVRLibrary.switchDisplayMode(this, MDVRLibrary.DISPLAY_MODE_GLASS);
        mVRLibrary.switchInteractiveMode(this, MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION);
//        mVRLibrary.switchDisplayMode(this, MDVRLibrary.DISPLAY_MODE_NORMAL);

        mVRLibrary.switchProjectionMode(this, MDVRLibrary.PROJECTION_MODE_PLANE_FULL);
        mVRLibrary.switchProjectionMode(this, MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_HORIZONTAL);

        mVRLibrary.setAntiDistortionEnabled(true);
//VMMD
    }

    private void startPlay(Uri media) {
        resetMediaPlay();
        if (media != null) {
            mMediaPlayerWrapper.openRemoteFile(media.toString());
            mMediaPlayerWrapper.prepare();
            bMediaPlayerWrapperisWorked = true;
        }
    }

    private void resetFile() {
        resetMediaPlay();
        mbMediaPlayerWrapperisPaused = false;
    }

    private void dialogSetupPrefs() {


        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.setup_cli_dialog); //layout for dialog
        dialog.setTitle("sETUP device connection");
        dialog.setCancelable(false); //none-dismiss when touching outside Dialog
//sound vol and addr

        ImageView simageView = dialog.findViewById(R.id.imageView);


        final EditText tDeviceCode = dialog.findViewById(R.id.device_code);
        tDeviceCode.setText("1a8f6007-a27e-4355-8557-db16a7c92fe0");
        final EditText tsDevName = dialog.findViewById(R.id.devName);
        tsDevName.setText("VRTren CONTROL");
        final SeekBar soundvolseekBar = dialog.findViewById(R.id.soundvolseekBar);
        soundvolseekBar.setProgress(sVolume);
//


        //Buttons
        View btnAdd = dialog.findViewById(R.id.btn_ok);
        View btnCancel = dialog.findViewById(R.id.btn_cancel);


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {      //onConfirmListener


                saveValuesToPrefs(String.valueOf(tDeviceCode.getText()), String.valueOf(tsDevName.getText()),
                        soundvolseekBar.getProgress());

                dialog.dismiss();

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //onCancelListener
                // do nothing
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void saveValuesToPrefs(String s, String s1, int progress) {
        // sVolume
        sVolume = progress;


        pref = getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();

        editor.putInt(IS_sVolume, sVolume);

        editor.commit();


    }

    private class BLeDeviceList {
        private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<BluetoothDevice>();


        public BLeDeviceList() {
            super();
        }

        public void addDevice(BluetoothDevice device) {
            SimpleLog.v("addDevice " + device.toString());
            if (!mLeDevices.contains(device)) {
                SimpleLog.v("!mLeDevices.contains(device) " + device.toString());
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }


    }
}

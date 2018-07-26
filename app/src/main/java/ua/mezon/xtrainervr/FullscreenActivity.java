package ua.mezon.xtrainervr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
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


public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final String URL0 = "file://";
    private static final String URLEXT = "file:///storage/external_SD/Download/VRTren/";//4.avi
    private static final String TAG = "VRTrainer";
    private TextView mConnectionState;
    private ListView mDataField;
    private String mDeviceName;
    private static final boolean AUTO_HIDE = true;
    private String mDeviceAddress;
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
    private String mServerNAME = "";

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private int sVolume = 20;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;


    private GLSurfaceView tmpGLSurfaceView;

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            d_setupPrefs_dialog();
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }


            return false;
        }
    };
    private boolean mbMediaPlayerWrapperisPaused = false;


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

            } else {

                // permission denied, boo!
                finish();
            }
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBLEProc = BLEProc.getInstance();
        setContentView(R.layout.activity_fullscreen);

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
        //  mServProc.mMainWebSocket.getServer().


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

                reset_MediaPlay();
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


        checkfile();
        receiveFullMess();
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


    @Override
    protected void onResume() {
        super.onResume();
        mVRLibrary.onResume(this);
        mMediaPlayerWrapper.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVRLibrary.onPause(this);
        mMediaPlayerWrapper.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVRLibrary.onDestroy();
        mMediaPlayerWrapper.destroy();
    }

    private void setPlayer(String strmedia) {
        if (strmedia == null) {
            return;
        }
        //init();


        Uri media = Uri.parse(URL0 + SupportUtilsMtds.ROOT_DIR + strmedia);


        if (!TextUtils.isEmpty(media.toString())) {
//                Toast toast = Toast.makeText(this, media.toString(), Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
//                toast.show();
        }


        tuneMPlay(strmedia);

        fire_Play(media);


    }

    private void fire_Play(Uri media) {
        reset_MediaPlay();
        if (media != null) {
            mMediaPlayerWrapper.openRemoteFile(media.toString());
            mMediaPlayerWrapper.prepare();
            bMediaPlayerWrapperisWorked = true;
        }
    }

    private void reset_MediaPlay() {
        if (bMediaPlayerWrapperisWorked) {//mMediaPlayerWrapper.getPlayer().isPlaying(
            mMediaPlayerWrapper.pause();
            mMediaPlayerWrapper.stop();
            mMediaPlayerWrapper.destroy();
            mMediaPlayerWrapper.init();
            bMediaPlayerWrapperisWorked = false;


            if (mServerNAME != "") {
                showFullMess("Connected to server NAME>" + mServerNAME);
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

    private void checkfile() {
        //check for permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            //ask for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SupportUtilsMtds
                        .READ_EXTERNAL_STORAGE_PERMISSION_CODE);
            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED) {
            //ask for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH, Manifest.permission
                        .BLUETOOTH_ADMIN}, SupportUtilsMtds
                        .READ_BLUETOOTH_PERMISSION_CODE);
            }
        } else {
            //filelist();
        }
    }

    public void receiveFullMess() {

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
            reset_MediaPlay();
            setPlayer(substring);

        } else {
            Log.d(TAG, "startFile:  mMediaPlayerWrapper.resume();");
            mMediaPlayerWrapper.resume();
            mbMediaPlayerWrapperisPaused = false;
        }


    }


    private void resetFile() {
        reset_MediaPlay();
        mbMediaPlayerWrapperisPaused = false;
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
        mVRLibrary.switchProjectionMode(this, MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_VERTICAL);

        mVRLibrary.setAntiDistortionEnabled(true);

    }

    private void saveValprefs(String s, String s1, int progress) {
        // sVolume
        sVolume = progress;


        pref = getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();

        editor.putInt(IS_sVolume, sVolume);

        editor.commit();


    }

    private void d_setupPrefs_dialog() {


        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.setup_cli_dialog); //layout for dialog
        dialog.setTitle("Add a new Timer");
        dialog.setCancelable(false); //none-dismiss when touching outside Dialog
//sound vol and addr

        ImageView simageView = dialog.findViewById(R.id.imageView);


        final EditText tServer_addr = dialog.findViewById(R.id.server_addr);
        tServer_addr.setText("222");
        final EditText tsCliName = dialog.findViewById(R.id.cliName);
        tsCliName.setText("222");
        final SeekBar soundvolseekBar = dialog.findViewById(R.id.soundvolseekBar);
        soundvolseekBar.setProgress(sVolume);
//


        //Buttons
        View btnAdd = dialog.findViewById(R.id.btn_ok);
        View btnCancel = dialog.findViewById(R.id.btn_cancel);


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {      //onConfirmListener


                saveValprefs(String.valueOf(tServer_addr.getText()), String.valueOf(tsCliName.getText()),
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


}

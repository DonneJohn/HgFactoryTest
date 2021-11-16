package com.henggu.factorytest;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.henggu.factorytest.common.CommProp;
import com.henggu.factorytest.common.Config;
import com.henggu.factorytest.utils.Common;
import com.henggu.factorytest.utils.LogUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class FunctiontestActivity extends Activity {

    private Context mContext;

    private RelativeLayout mTestLayout = null;
    private LinearLayout mCheckLayout = null;

    private TextView mTipStbid = null;
    private EditText mInputWifiId = null;

    private TextView tvPlayTime;
    private TextView tvVersionPoint;
    private TextView tvVersionNum;
    private TextView tvEthMac;
    private TextView tvEthIp;
    private TextView tvWifiIp;

    private Button btn_change_output;

    private RelativeLayout rlBluetooth;
    private TextView tvTestVersion;
    private TextView tvTestUsb1;
    private TextView tvTestUsb2;
    private TextView tvTestSdcard;
    private TextView tvTestEth;
    private TextView tvTestBlue;
    private TextView tvTestHdmi;
    private TextView tvTestCvbs;
    private TextView tvTestWifi;
    private TextView tvTestCA;


    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer player;
    private String mPlayUrl = null;
    private static final String MEDIA_SOURCE_NAME = "media.avi";

    private boolean isHdmiMode = true;// 默认HDMI输出

    private TestThread mTestThread = null;
    private boolean mTestSysVerState = false;
    private boolean mTestUsb1State = false;
    private boolean mTestUsb2State = false;
    private boolean mTestSDState = false;
    private boolean mTestEthState = false;
    private boolean mTestBlueState = false;
    private boolean mTestHdmiState = false;
    private boolean mTestAVState = false;
    private boolean mTestWifiState = false;
    private boolean mTestCAState = false;

    private boolean mTestAllPass = false;

    private boolean mTestUsb1Item, mTestUsb2Item, mTestSdcardItem, mTestEthItem, mTestBlueItem, mTestHdmiItem, mTestAvItem, mTestWifiItem, mTestCAItem = false;

    private CommProp mCommProp = null;

    private Timer timeTimer;
    private int playTime = 0;
    private final int WHAT_UPGRADE_TIME = 0xf1;

    private static final long MSG_SEND_DELAY = 500;//500ms


    private static final int MSG_TEST_VER_PASS = 0xb0;
    private static final int MSG_TEST_USB1_PASS = 0xb1;
    private static final int MSG_TEST_USB2_PASS = 0xb2;
    private static final int MSG_TEST_SD_PASS = 0xb3;
    private static final int MSG_TEST_ETH_PASS = 0xb4;
    private static final int MSG_TEST_BLUE_PASS = 0xb5;
    private static final int MSG_TEST_HDMI_PASS = 0xb6;
    private static final int MSG_TEST_AV_PASS = 0xb7;
    private static final int MSG_TEST_FAIL = 0xb8;
    private static final int MSG_TEST_END = 0xb9;
    private static final int MSG_TEST_WIFI_PASS = 0xc0;
    private static final int MSG_SET_WIFI_IP = 0xc1;

    private String sdcardFlag = "sdcard";

    private boolean testBlueFlag = false;
    private BluetoothAdapter ba;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_UPGRADE_TIME:
//                    if (player.isPlaying())
                    playTime++;
                    tvPlayTime.setText(MessageFormat.format(
                            "{0,number,00}:{1,number,00}:{2,number,00}",
                            playTime / 60 / 60, playTime / 60 % 60, playTime % 60));
                    break;
                case MSG_TEST_VER_PASS:
                case MSG_TEST_USB1_PASS:
                case MSG_TEST_USB2_PASS:
                case MSG_TEST_SD_PASS:
                case MSG_TEST_ETH_PASS:
                case MSG_TEST_BLUE_PASS:
                case MSG_TEST_HDMI_PASS:
                case MSG_TEST_AV_PASS:
                case MSG_TEST_WIFI_PASS:

                    if (msg.what == MSG_TEST_VER_PASS && tvTestVersion != null) {
                        dealTextResult(tvTestVersion, true);
                    }
                    if (msg.what == MSG_TEST_USB1_PASS && tvTestUsb1 != null) {
                        dealTextResult(tvTestUsb1, true);
                    }
                    if (msg.what == MSG_TEST_USB2_PASS && tvTestUsb2 != null) {
                        dealTextResult(tvTestUsb2, true);
                    }
                    if (msg.what == MSG_TEST_SD_PASS && tvTestSdcard != null) {
                        dealTextResult(tvTestSdcard, true);
                    }
                    if (msg.what == MSG_TEST_ETH_PASS && tvTestEth != null) {
                        dealTextResult(tvTestEth, true);
                    }
                    if (msg.what == MSG_TEST_BLUE_PASS && tvTestBlue != null) {
                        dealTextResult(tvTestBlue, true);
                    }
                    if (msg.what == MSG_TEST_HDMI_PASS && tvTestHdmi != null) {
                        dealTextResult(tvTestHdmi, true);
                    }
                    if (msg.what == MSG_TEST_WIFI_PASS && tvTestWifi != null) {
                        dealTextResult(tvTestWifi, true);
                    }
                    if (msg.what == MSG_TEST_AV_PASS) {
                        mTestAVState = true;
                    }
                    if (mTestSysVerState && mTestUsb1State == mTestUsb1Item && mTestUsb2State == mTestUsb2Item
                            && mTestSDState == mTestSdcardItem && mTestEthState == mTestEthItem
                            && mTestHdmiState == mTestHdmiItem && mTestBlueState == mTestBlueItem
                            && mTestWifiState == mTestWifiItem && mTestCAItem == mTestCAState) {
                        btn_change_output.setFocusable(true);
                        btn_change_output.requestFocus();
                        if(mTestAVState == mTestAvItem && !mTestAllPass){
                            mTestAllPass = true;
                            Common.getSysUtilHandler().setFunctiontestResult(true);
                            int testResult = Common.getSysUtilHandler().getFunctiontestResult();
                            if (testResult == Config.TAG_TEST_SUCC) {
                                if (!mCommProp.mMergeStation) {
                                    if (mCommProp.mUseLocalPrompt) {
                                        AlertDialog lastStationDialog = new AlertDialog.Builder(mContext)
                                                .setTitle(getResources().getString(R.string.app_name)).setMessage("测试项通过，请断电!")
                                                .setIcon(android.R.drawable.ic_dialog_alert).create();
                                        lastStationDialog.show();
                                        lastStationDialog.setCancelable(false);
                                    } else {
                                        Common.sendDataReportBroadcast("", Config.FUNCTION_STATION, true, mContext);
                                    }
                                } else {
                                    Intent intent = new Intent(FunctiontestActivity.this, BurnIdActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                    }
                    break;
                case MSG_TEST_FAIL:
                    if (!mTestSysVerState && tvTestVersion != null) {
                        dealTextResult(tvTestVersion, false);
                    }
                    if (!mTestUsb1State && tvTestUsb1 != null) {
                        dealTextResult(tvTestUsb1, false);
                    }
                    if (!mTestUsb2State && tvTestUsb2 != null) {
                        dealTextResult(tvTestUsb2, false);
                    }
                    if (!mTestSDState && tvTestSdcard != null) {
                        dealTextResult(tvTestSdcard, false);
                    }
                    if (!mTestEthState && tvTestEth != null) {
                        dealTextResult(tvTestEth, false);
                    }
                    if (!mTestBlueState && tvTestBlue != null) {
                        dealTextResult(tvTestBlue, false);
                    }
                    if (!mTestHdmiState && tvTestHdmi != null) {
                        dealTextResult(tvTestHdmi, false);
                    }
                    if (!mTestWifiItem && tvTestWifi != null) {
                        dealTextResult(tvTestWifi, false);
                    }
                    if (!mTestAllPass) {
                        Common.getSysUtilHandler().setFunctiontestResult(false);
                    }
                    break;
                case MSG_SET_WIFI_IP:
                    String wifiAddr = (String) msg.obj;
                    tvWifiIp.setText(wifiAddr);
                    break;
                default:
                    break;
            }
        }
    };

    private void dealTextResult(TextView tv, boolean ifPass) {
        tv.setVisibility(View.VISIBLE);
        if (ifPass) {
            tv.setBackgroundColor(getResources().getColor(R.color.green));
            tv.setTextColor(getResources().getColor(R.color.yellow));
            tv.setText(getResources().getString(R.string.str_pass));
        } else {
            tv.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            tv.setTextColor(getResources().getColor(android.R.color.white));
            tv.setText(getResources().getString(R.string.str_dispass));
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = FunctiontestActivity.this;
        setContentView(R.layout.activity_function);
        mCommProp = new CommProp();
        initView();
        if (mCommProp.mUseLocalPrompt) {
            testIperf();
        }else {
            Common.sendCheckStationBroadcast("", Config.WIFI_THRO_STATION, Config.FUNCTION_STATION, false, mContext);
        }

        if (mCommProp.mSkipCheckLastStation) {
            // begin test
            if (mCheckLayout != null) {
                mCheckLayout.setVisibility(View.GONE);
            }
            if (mTestLayout != null) {
                mTestLayout.setVisibility(View.VISIBLE);
            }
            init();
            mTestThread = new TestThread();
            mTestThread.mRunning = true;
            mTestThread.mHandler = mHandler;
            mTestThread.start();
        }
    }

    private void initView() {
        mCheckLayout = (LinearLayout) findViewById(R.id.checkLayout);
        mTestLayout = (RelativeLayout) findViewById(R.id.testLayout);
        mTestLayout.setVisibility(View.GONE);
        mTipStbid = (TextView) findViewById(R.id.tip_check_stbid);
        mInputWifiId = (EditText) findViewById(R.id.input_wifiid);
        mInputWifiId.setFocusable(true);
        mInputWifiId.setInputType(InputType.TYPE_NULL);
        mInputWifiId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                LogUtils.debug(mContext, "keyCode is " + keyCode);
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    return checkInputWifiId();
                }
                return false;
            }
        });

        tvPlayTime = (TextView) findViewById(R.id.tv_play_time);
        tvVersionPoint = (TextView) findViewById(R.id.tv_version_point);
        tvVersionNum = (TextView) findViewById(R.id.tv_version_num);
        tvEthMac = (TextView) findViewById(R.id.eth_mac);
        tvEthIp = (TextView) findViewById(R.id.eth_ip);
        tvWifiIp = (TextView) findViewById(R.id.wifi_ip);

        btn_change_output = (Button) findViewById(R.id.change_output);


        rlBluetooth = (RelativeLayout) findViewById(R.id.rl_bluetooth);

        tvTestVersion = (TextView) findViewById(R.id.tv_test_version);
        tvTestUsb1 = (TextView) findViewById(R.id.tv_test_usb1);
        tvTestUsb2 = (TextView) findViewById(R.id.tv_test_usb2);
        tvTestSdcard = (TextView) findViewById(R.id.tv_text_sdcard);
        tvTestEth = (TextView) findViewById(R.id.tv_test_eth);
        tvTestBlue = (TextView) findViewById(R.id.tv_test_bluetooth);
        tvTestHdmi = (TextView) findViewById(R.id.tv_test_hdmi);
        tvTestCvbs = (TextView) findViewById(R.id.tv_test_cvbs);
        tvTestWifi = (TextView) findViewById(R.id.tv_test_wifi);

        if (mCommProp.mFunctionTestItems != null) {
            LogUtils.debug(mContext, "function test items: " + mCommProp.mFunctionTestItems);
            if (mCommProp.mFunctionTestItems.contains("usb1")) {
                LogUtils.debug(mContext, "function test usb1");
                mTestUsb1Item = true;
                findViewById(R.id.rl_test_usb1).setVisibility(View.VISIBLE);
            }
            if (mCommProp.mFunctionTestItems.contains("usb2")) {
                LogUtils.debug(mContext, "function test usb2");
                mTestUsb2Item = true;
                findViewById(R.id.rl_test_usb2).setVisibility(View.VISIBLE);
            }
            if (mCommProp.mFunctionTestItems.contains("sdcard")) {
                LogUtils.debug(mContext, "function test sdcard");
                mTestSdcardItem = true;
                findViewById(R.id.rl_test_sdcard).setVisibility(View.VISIBLE);
            }
            if (mCommProp.mFunctionTestItems.contains("eth")) {
                LogUtils.debug(mContext, "function test eth");
                mTestEthItem = true;
                findViewById(R.id.rl_test_eth).setVisibility(View.VISIBLE);
            }
            if (mCommProp.mFunctionTestItems.contains("bluetooth")) {
                LogUtils.debug(mContext, "function test bluetooth");
                mTestBlueItem = true;
                rlBluetooth.setVisibility(View.VISIBLE);
                ba = BluetoothAdapter.getDefaultAdapter();
                if(ba != null) {
                    if (ba.isEnabled()) {
                        ba.disable();
                    }
                    boolean blueEnable = ba.enable();
                    LogUtils.debug(mContext, "bluetooth open again: " + blueEnable);
                    LogUtils.debug(mContext, "bluetooth start scan");
                    ba.startDiscovery();
                } else {
                    mTestBlueItem = false;
                }

            }
            if (mCommProp.mFunctionTestItems.contains("hdmi")) {
                LogUtils.debug(mContext, "function test hdmi");
                mTestHdmiItem = true;
                findViewById(R.id.rl_test_hdmi).setVisibility(View.VISIBLE);
            }
            if (mCommProp.mFunctionTestItems.contains("cvbs")) {
                LogUtils.debug(mContext, "function test cvbs");
                mTestAvItem = true;
                findViewById(R.id.rl_test_cvbs).setVisibility(View.VISIBLE);
            }
            if (mCommProp.mFunctionTestItems.contains("wifi")) {
                LogUtils.debug(mContext, "function test wifi");
                mTestWifiItem = true;
                findViewById(R.id.rl_test_wifi).setVisibility(View.VISIBLE);
            }
        }


        surfaceView = (SurfaceView) findViewById(R.id.VideoView01);
    }

    private void checkCA() {
        String hsca = SystemProperties.get("ro.hs.security.l1.enable", "false");
        if (!TextUtils.isEmpty(hsca) && "true".equals(hsca)) {
            mTestCAItem = true;
            findViewById(R.id.rl_test_ca).setVisibility(View.VISIBLE);
            tvTestCA = (TextView) findViewById(R.id.tv_test_ca);

            String caResult = Common.getSysUtilHandler().readCAState();
            LogUtils.debug(mContext, "get caResult: " + caResult);
            if (!TextUtils.isEmpty(caResult)) {
                caResult = caResult.trim();
               if ("false".equals(caResult)) {
                    mTestCAState = false;
                    dealTextResult(tvTestCA, false);
                } else if ("true".equals(caResult)) {
                    mTestCAState = true;
                    dealTextResult(tvTestCA, true);
                }
            }
        }
    }

    private boolean checkInputWifiId() {
        String wifiIdStr = mInputWifiId.getText().toString();
        String rWifiIdStr = Common.getSysUtilHandler().readWifiId();
        LogUtils.debug(mContext, " input wifiId: " + wifiIdStr + " read wifiId: " + rWifiIdStr);
        if ((wifiIdStr != null) && wifiIdStr.equals(rWifiIdStr)) {
            if (mCheckLayout != null) {
                mCheckLayout.setVisibility(View.GONE);
            }
            if (mTestLayout != null) {
                mTestLayout.setVisibility(View.VISIBLE);
            }
            init();
            if (surfaceView != null) {
                surfaceView.getHolder().addCallback(mSHCallback);
                surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }

            mTestThread = new TestThread();
            mTestThread.mRunning = true;
            mTestThread.mHandler = mHandler;
            mTestThread.start();
        } else {
            mInputWifiId.selectAll();
            if (mTipStbid != null) {
                mTipStbid.setBackgroundColor(Color.parseColor("#cc0000"));
                if ((rWifiIdStr == null) || ((rWifiIdStr != null) && (rWifiIdStr.length() == 0)))
                    mTipStbid.setText(mContext.getResources().getString(R.string.check_stbid) + "Unkown.");
                else
                    mTipStbid.setText(mContext.getResources().getString(R.string.check_stbid) + rWifiIdStr);
            }
        }
        return true;
    }

    private void init() {
        String chiptype = SystemProperties.get("ro.product.chiptype", "unknown");
        if (chiptype.startsWith("Hi")) {
            sdcardFlag = "mmcblk1";
        } else if (chiptype.startsWith("Aml")) {
            sdcardFlag = "sdcard";
        }

        mTestSysVerState = false;
        testSysVer();
        checkCA();

        btn_change_output.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mTestAvItem) changeOutputDisplay();
            }
        });

        if (surfaceView != null) {
            surfaceView.getHolder().addCallback(mSHCallback);
            surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        tvVersionPoint.setText(Common.getSysUtilHandler().getProjectTarget() + "("
                + Common.getSysUtilHandler().getProjectProvince() + ")");

        tvEthMac.setText(Common.getSysUtilHandler().getEthMacAddr());
        tvEthIp.setText(Common.getLocalIpAddress());
    }

    private void testSysVer() {
        String strVer = null;
        try {
            strVer = SystemProperties.get(mCommProp.mSysVerProp, null);
            LogUtils.debug(mContext, mCommProp.mSysVerProp + ": " + strVer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (strVer != null) {
            if (tvVersionNum != null)
                tvVersionNum.setText(strVer);
            if (strVer.equals(mCommProp.mSysVer)) {
                mTestSysVerState = true;
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(MSG_TEST_VER_PASS, MSG_SEND_DELAY);
                }
                return;
            }
        }
        if (mHandler != null) {
            mTestSysVerState = false;
            mHandler.sendEmptyMessageDelayed(MSG_TEST_FAIL, MSG_SEND_DELAY);
        }

        LogUtils.debug(mContext, "test sys version fail, get version is " + strVer + " but get prop version is " + mCommProp.mSysVer);
    }

    private void changeOutputDisplay() {
//        isHdmiMode = Common.getSysUtilHandler().isHdmiOutputMode();

        if (isHdmiMode) {
            Common.getSysUtilHandler().setAvOutputDispMode();
            boolean avPass = Common.getSysUtilHandler().isAvOutputMode();
            dealTextResult(tvTestCvbs, avPass);
        } else {
            Common.getSysUtilHandler().setHdmiOutputDispMode();
            dealTextResult(tvTestHdmi, Common.getSysUtilHandler().isHDMISuspendEnable());
            if (tvTestCvbs.getText().toString().equals(getString(R.string.str_pass))
                    && tvTestHdmi.getText().toString().equals(getString(R.string.str_pass))) {
                sendResult(MSG_TEST_AV_PASS);
            }
        }
        isHdmiMode = !isHdmiMode;
    }


    private SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        }

        public void surfaceCreated(SurfaceHolder holder) {
            LogUtils.debug(mContext, "[surfaceCreated]");
            surfaceHolder = holder;
            initPlayer();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            LogUtils.debug(mContext, "[surfaceDestroyed]");
            surfaceHolder = null;
            surfaceView = null;
        }
    };

    private void initPlayer() {
        LogUtils.debug(mContext, "[initPlayer]mSurfaceHolder:" + surfaceHolder);
        if (surfaceHolder == null) {
            return;
        }

        player = new MediaPlayer();
        player.setOnPreparedListener(mPreparedListener);
        player.setOnCompletionListener(mCompletionListener);
        player.setOnErrorListener(mErrorListener);
        player.setDisplay(surfaceHolder);

        try {
            LogUtils.debug(mContext, "initPlayer playurl: " + mPlayUrl);
            if (mPlayUrl == null) {
                AssetManager assetManager = this.getAssets();
                AssetFileDescriptor afd = assetManager.openFd(MEDIA_SOURCE_NAME);
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            } else {
                player.setDataSource(mPlayUrl);
            }
            player.setLooping(true);
            player.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            LogUtils.debug(mContext, "[mPreparedListener]");
//            start();
            player.start();
            playtimer();
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            LogUtils.debug(mContext, "[onCompletion]");
//            mCurTime = 0; // reset current time
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener =
            new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    LogUtils.debug(mContext, "Error: " + what + "," + extra);
                    return true;
                }
            };

    private void playtimer() {
        if (timeTimer == null) {
            timeTimer = new Timer(true);
            timeTimer.schedule(new TimerTask() {
                public void run() {
                    mHandler.sendEmptyMessage(WHAT_UPGRADE_TIME);
                }
            }, 1000, 1000);
        }
    }


    private class TestThread extends Thread {
        public boolean mRunning = false;
        public Handler mHandler = null;

        @Override
        public void run() {
            int testNum = 0;
            while (mRunning) {
                try {
                    if (!mTestUsb1State || !mTestUsb2State || !mTestSDState) {
                        List<String> paths = mCommProp.getStorageDir();

                        if (mTestUsb1Item && !mTestUsb1State) {
                            mTestUsb1State = testUsb(paths, "usb1.mpg");
                            if (mTestUsb1State) sendResult(MSG_TEST_USB1_PASS);
                        }
                        if (mTestUsb2Item && !mTestUsb2State) {
                            mTestUsb2State = testUsb(paths, "usb2.mpg");
                            if (mTestUsb2State) sendResult(MSG_TEST_USB2_PASS);
                        }

                        if (mTestSdcardItem && !mTestSDState) {
                            mTestSDState = testSd(paths);
                            if (mTestSDState) sendResult(MSG_TEST_SD_PASS);
                        }
                    }

                    if (mTestEthItem && !mTestEthState) {
                        mTestEthState = testEth();
                        if (mTestEthState)
                            sendResult(MSG_TEST_ETH_PASS);
                    }

                    if (mTestBlueItem && !mTestBlueState) {
                        testBluetooth();
                    }
                    if (mTestHdmiItem && !mTestHdmiState) {
                        mTestHdmiState = testHdmi();
                        if (mTestHdmiState)
                            sendResult(MSG_TEST_HDMI_PASS);
                    }
                    if (mTestWifiItem && !mTestWifiState) {
                        mTestWifiState = testWifi();
                        if (mTestWifiState)
                            sendResult(MSG_TEST_WIFI_PASS);
                    }
                    testNum++;
                    if (mTestAllPass) {
                        mRunning = false;
                        LogUtils.debug(mContext, "test thread end!");
                        return;
                    }
                    if (testNum >= mCommProp.mFactoryTestNum) {
                        mRunning = false;
                        sendResult(MSG_TEST_FAIL);
                        LogUtils.debug(mContext, "test thread fail, test num over " + mCommProp.mFactoryTestNum);
                        return;
                    }
                    Thread.sleep(2 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                    mRunning = false;
                    LogUtils.debug(mContext, "test thread failed!");
                }
            }
        }
    }

    private void sendResult(int what) {
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(what, MSG_SEND_DELAY);
        }
    }

    private boolean testSd(List<String> paths) {
        LogUtils.debug(mContext, "testSd");
        if (paths == null) {
            LogUtils.debug(mContext, "testSd dir paths is null!");
            return false;
        }
        int count = paths.size();
        LogUtils.debug(mContext, "dir path count is " + count);
        for (int i = 0; i < count; i++) {
            String path = paths.get(i);
            if (path.contains(sdcardFlag)) {
                File file = mCommProp.getFile(path + "/" + "sdcard.mpg");
                if (file != null) {
                    paths.remove(i);
                    return true;
                }
            }
        }
        LogUtils.debug(mContext, "Not found sdcard.mpg");
        return false;
    }

    private boolean testUsb(List<String> paths, String filename) {
        LogUtils.debug(mContext, "testUsb");
        if (paths == null) {
            LogUtils.debug(mContext, "dir paths is null!");
            return false;
        }
        int count = paths.size();
        LogUtils.debug(mContext, "dir path count is " + count);
        for (int i = 0; i < count; i++) {
            String path = paths.get(i);
            if (!path.contains("sdcard")) {
                File file = mCommProp.getFile(path + "/" + filename);
                if (file != null) {
                    paths.remove(i);
                    return true;
                }
            }
        }
        LogUtils.debug(mContext, "Not found " + filename);
        return false;
    }

    private boolean testEth() {
        File ethRecodeFile = new File(Config.ETH_RECODE_FILE);
        if (ethRecodeFile != null && ethRecodeFile.exists()) {
            String ethRlt = Common.getFileValue(Config.ETH_RECODE_FILE);
            LogUtils.debug(mContext, "last station test eth" + ethRlt);
            if (!TextUtils.isEmpty(ethRlt) && ethRlt.equals("1")) {
                return true;
            }
        }
        String ethIp = Common.getLocalIpAddress();
        if (TextUtils.isEmpty(ethIp)) return false;

        if (mCommProp.mEthPingIp != null)
            return callPing(mCommProp.mEthPingIp);

        String ethPingIp = ethIp == null ? null : ethIp.substring(0, ethIp.lastIndexOf(".") + 1)
                + String.valueOf(Integer.parseInt(ethIp.substring(ethIp.lastIndexOf(".") + 1, ethIp.length())) + 1);
        if (ethPingIp != null)
            return callPing(ethPingIp);

        return false;

    }

    public boolean callPing(String mIpAddr) {
        try {
            LogUtils.debug(mContext, "Ping ip: " + mIpAddr);
            Process p = Runtime.getRuntime().exec("ping -c 2 -w 5 " + mIpAddr);
            int status = p.waitFor();
            if (status == 0) {
                LogUtils.debug(mContext, "Ping ip");
                return true;
            } else {
                LogUtils.debug(mContext, "Ping fail, status: " + status);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean testHdmi() {
        return Common.getSysUtilHandler().isHdmiOutputMode();
    }

    private boolean testWifi() {
        String wifiAddr = Common.getSysUtilHandler().getWifiAddr(mContext);
        LogUtils.debug(mContext, "wifi addr is: " + wifiAddr);
        if (TextUtils.isEmpty(wifiAddr)) return false;
        Message msg =Message.obtain();  
        msg.obj = wifiAddr;
        msg.what = MSG_SET_WIFI_IP;
        mHandler.sendMessage(msg);
        if(wifiAddr.equals("0.0.0.0")) return false;
        return true;
    }

    boolean btcallback = false;
    private void testBluetooth() {
        if (btcallback) return;
        boolean btEnable = ba.isEnabled();
        LogUtils.debug(mContext, "bluetooth open when test: " + btEnable);
        if (!btEnable) {
            ba.enable();
            ba.startDiscovery();
        }
        if (ba.isEnabled() && !btcallback) {
            btcallback = ba.startLeScan(leScanCallback);
            LogUtils.debug(mContext, "bluetooth callback result: " + btcallback);
        }
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//            LogUtils.debug(mContext, "bluetooth name ：" + device.getName());
            if (testBlueFlag) return;
            LogUtils.debug(mContext, "bluetooth address ：" + device.getAddress());
            if (device != null) {
                testBlueFlag = true;
                mTestBlueState = true;
//                ba.disable();
//                ba.stopLeScan(this);
                mHandler.sendEmptyMessage(MSG_TEST_BLUE_PASS);
            } else {
                mTestBlueState = false;
                mHandler.sendEmptyMessage(MSG_TEST_FAIL);
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.removeMessages(MSG_TEST_VER_PASS);
            mHandler.removeMessages(MSG_TEST_USB1_PASS);
            mHandler.removeMessages(MSG_TEST_USB2_PASS);
            mHandler.removeMessages(MSG_TEST_SD_PASS);
            mHandler.removeMessages(MSG_TEST_ETH_PASS);
            mHandler.removeMessages(MSG_TEST_BLUE_PASS);
            mHandler.removeMessages(MSG_TEST_HDMI_PASS);
            mHandler.removeMessages(MSG_TEST_AV_PASS);
            mHandler.removeMessages(MSG_TEST_WIFI_PASS);
            mHandler.removeMessages(MSG_TEST_FAIL);
        }

        stop();
        release();
        mTestThread.mHandler = null;
        mTestThread.mRunning = false;
    }


    private void stop() {
        LogUtils.debug(mContext, "[stop]");
        player.stop();
        player.reset();
    }

    private void release() {
        LogUtils.debug(mContext, "[release]");
        player.release();
        player = null;
    }

    private void testIperf() {
        if (mCommProp.mIperfCheckDigit > 0) {
            String iperfRlt = Common.getFileValue(Config.IPERF_RECODE_FILE);
            LogUtils.debug(Config.TAG, "testiperfRlt: " + iperfRlt + " checkIperfRlt: " + mCommProp.mIperfCheckDigit);
            if (iperfRlt == null) {
                AlertDialog lastStationDialog = new AlertDialog.Builder(mContext)
                        .setTitle(getResources().getString(R.string.app_name)).setMessage("吞吐量未测试，请检查")
                        .setIcon(android.R.drawable.ic_dialog_info).create();
                lastStationDialog.show();
                lastStationDialog.setCancelable(false);
            } else {
                try {
                    iperfRlt = iperfRlt.trim();
                    if (Integer.parseInt(iperfRlt) == 0) {
                        AlertDialog lastStationDialog = new AlertDialog.Builder(mContext)
                                .setTitle(getResources().getString(R.string.app_name))
                                .setMessage("吞吐量未通过，请检查！")
                                .setIcon(android.R.drawable.ic_dialog_info).create();
                        lastStationDialog.show();
                        lastStationDialog.setCancelable(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (mCommProp.mAgingCheckDigit > 0) {
                String agingRlt = Common.getFileValue(Config.AGING_RECODE_FILE);
                LogUtils.debug(Config.TAG, "testAgingRlt: " + agingRlt + " checkAgingRlt: " + mCommProp.mAgingCheckDigit);
                if (TextUtils.isEmpty(agingRlt)) {
                    AlertDialog lastStationDialog = new AlertDialog.Builder(mContext)
                            .setTitle(getResources().getString(R.string.app_name)).setMessage("老化未测试，请检查")
                            .setIcon(android.R.drawable.ic_dialog_info).create();
                    lastStationDialog.show();
                    lastStationDialog.setCancelable(false);
                } else {
                    try {
                        agingRlt = agingRlt.trim();
                        if (Integer.parseInt(agingRlt) < mCommProp.mAgingCheckDigit) {
                            AlertDialog lastStationDialog = new AlertDialog.Builder(mContext)
                                    .setTitle(getResources().getString(R.string.app_name))
                                    .setMessage("老化时间不足，请检查！")
                                    .setIcon(android.R.drawable.ic_dialog_info).create();
                            lastStationDialog.show();
                            lastStationDialog.setCancelable(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            
        }
    }

}

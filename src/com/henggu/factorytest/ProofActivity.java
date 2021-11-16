package com.henggu.factorytest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.os.SystemProperties;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.henggu.factorytest.common.CommProp;
import com.henggu.factorytest.common.Config;
import com.henggu.factorytest.utils.Common;
import com.henggu.factorytest.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;


public class ProofActivity extends Activity {

    private static final int MSG_FACREST = 0xa0;
    private Context mContext;
    private EditText EditSn = null;
    private EditText EditStbid = null;
    private TextView GetSn = null;
    private TextView GetStbid = null;
    private TextView GetVer = null;

    private TextView TextCurrentStatus = null;
    private CommProp mCommProp = null;
    private String mGetSnStr = null;
    private String mGetStbidStr = null;

    private String mSysBtMac = null;

    private int checksn = 0;
    private int checkstbid = 0;
    private int checkver = 0;
    private boolean bCheckWifiMac = false;
    private boolean bCheckBtMac = false;
    private int checkWifiMac = 0;
    private int checkBtMac = 0;
    private int checkCA = 0;
    private boolean bCheckCA = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_proof);
        mContext = ProofActivity.this;
        mCommProp = new CommProp();
        init();
        initView();
    }

    private void init() {
        mGetSnStr = null;
        mGetStbidStr = null;

        mSysBtMac = null;

        checksn = 0;
        checkstbid = 0;
    }

    private void initView() {
        if (!mCommProp.mUseLocalPrompt) {
            Common.sendCheckStationBroadcast("", Config.UPGRADE_STATION, Config.PROOF_STATION, false, mContext);
        }
        TextCurrentStatus = (TextView) findViewById(R.id.current_status);
        EditSn = (EditText) findViewById(R.id.sn_edit);
        EditSn.setFocusable(true);
        EditSn.setInputType(InputType.TYPE_NULL);
        EditSn.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    return checkEditSn();
                }
                return false;
            }
        });
        EditSn.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return checkEditSn();
            }
        });

        GetSn = (TextView) findViewById(R.id.get_sn);
        mGetSnStr = Common.getSysUtilHandler().readSN();
        LogUtils.debug(mContext, " get sn: " + mGetSnStr);
        if (mGetSnStr != null)
            GetSn.setText(mGetSnStr);

        EditStbid = (EditText) findViewById(R.id.stbid_edit);
        EditStbid.setFocusable(false);
        EditStbid.setInputType(InputType.TYPE_NULL);
        EditStbid.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    return checkEditStbid();
                }
                return false;
            }
        });
        EditStbid.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return checkEditStbid();
            }
        });

        GetStbid = (TextView) findViewById(R.id.get_stbid);
        mGetStbidStr = Common.getSysUtilHandler().getStbid();
        if (mCommProp.mWifiMacBurnRules != null) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        }
        if (mCommProp.mBTMacBurnRules != null) {
            BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
            if (ba != null) {
                if (!ba.isEnabled()){
                    boolean blueEnable = ba.enable();
                    LogUtils.debug(mContext, "bluetooth open : " + blueEnable);
                }
                mSysBtMac = ba.getAddress();
                LogUtils.debug(mContext, "blue mac is:" + mSysBtMac);
            }
        }
        LogUtils.debug(mContext, " get stbid: " + mGetStbidStr);
        if (mGetStbidStr != null)
            GetStbid.setText(mGetStbidStr);

        GetVer = (TextView) findViewById(R.id.get_ver);
        try {
            String strVer = SystemProperties.get(mCommProp.mSysVerProp, null);
            LogUtils.debug(mContext, mCommProp.mSysVerProp + ": " + strVer + ", equals " + mCommProp.mSysVer);
            if (strVer != null) {
                GetVer.setText(strVer);
                if (strVer.equals(mCommProp.mSysVer)) {
                    checkver = 1;
                } else {
                    checkver = -1;
                }
            }
            setResult();
        } catch (Exception e) {
            e.printStackTrace();
        }

        checkCA();
    }
    
    private void checkCA() {
        String hsca = SystemProperties.get("ro.hs.security.l1.enable", "false");
        if (!TextUtils.isEmpty(hsca) && "true".equals(hsca)) {
            bCheckCA = true;
            findViewById(R.id.tr_getca).setVisibility(View.VISIBLE);
            String caResult = Common.getSysUtilHandler().readCAState();
            LogUtils.debug(mContext, "get caResult: " + caResult);
            if (!TextUtils.isEmpty(caResult)) {
                caResult = caResult.trim();
                TextView tvGetCa = (TextView) findViewById(R.id.tv_get_ca);
                if ("true".equals(caResult)) {
                    checkCA = 1;
                    tvGetCa.setText("通过");
                } else if ("false".equals(caResult)) {
                    checkCA = -1;
                    tvGetCa.setTextColor(Color.RED);
                    tvGetCa.setText("不通过");
                }
            }
        }
    }

    private boolean checkEditSn() {
        String SnString = EditSn.getText().toString();
        int len = SnString.length();
        LogUtils.debug(mContext, " input sn: " + SnString + " length: " + len);
        if (len <= 0) {
            LogUtils.debug(mContext, "input string len <= 0!!");
            return true;
        }
        if ((SnString != null) && SnString.equals(mGetSnStr)) {
            checksn = 1;
            if (EditStbid != null) {
                EditStbid.setFocusable(true);
                EditStbid.requestFocus();
            }
        } else {
            checksn = -1;
            EditSn.selectAll();
        }
        setResult();
        return true;
    }

    private boolean checkEditStbid() {
        String StbidString = EditStbid.getText().toString();
        int len = StbidString.length();
        LogUtils.debug(mContext, " input stdid: " + StbidString + " length: " + len);
        if (len <= 0) {
            LogUtils.debug(mContext, "input string len <= 0!!");
            return true;
        }
        String mStbid = null;
        if (len > 20) {
//            mMac = StbidString.substring(0, 2) + ":" + StbidString.substring(2, 4)
//                    + ":" + StbidString.substring(4, 6) + ":" + StbidString.substring(6, 8)
//                    + ":" + StbidString.substring(8, 10) + ":" + StbidString.substring(10);
//            LogUtils.debug(mContext, "get mac: " + mMac);
            mStbid = StbidString;
            LogUtils.debug(mContext, "get stbid: " + mStbid);
        }

        String lanMac = Common.getSysUtilHandler().getEthMacAddr().replace(":", "");
        LogUtils.debug(mContext, "get lan mac: " + lanMac);
        BigInteger bLanMac = new BigInteger(lanMac, 16);
        if (mCommProp.mWifiMacBurnRules != null) {
            bCheckWifiMac = true;
            String wifiMac = Common.getSysUtilHandler().getWifiMacAddr().replace(":", "");
            LogUtils.debug(mContext, "get wifi mac: " + wifiMac);
            BigInteger bWifiMacRules = new BigInteger(mCommProp.mWifiMacBurnRules, 16);
            String sWifiMac = bLanMac.add(bWifiMacRules).toString(16);
            LogUtils.debug(mContext, "check wifi mac: " + sWifiMac);
            if (!TextUtils.isEmpty(wifiMac) && !TextUtils.isEmpty(lanMac)
                    && wifiMac.equalsIgnoreCase(sWifiMac) && wifiMac.substring(0, 6).equalsIgnoreCase(lanMac.substring(0, 6))) {
                checkWifiMac = 1;
            } else {
                checkWifiMac = -1;
            }
        }

        if (mCommProp.mBTMacBurnRules != null) {
            bCheckBtMac = true;
            if (!TextUtils.isEmpty(mSysBtMac)) {
                String btmac = mSysBtMac.replace(":", "");
                BigInteger bBtMacRules = new BigInteger(mCommProp.mBTMacBurnRules, 16);
                String sBtMac = bLanMac.add(bBtMacRules).toString(16);
                if (!TextUtils.isEmpty(btmac) && !TextUtils.isEmpty(lanMac)
                        && sBtMac.equalsIgnoreCase(btmac) && btmac.substring(0, 6).equalsIgnoreCase(lanMac.substring(0, 6))) {
                    checkBtMac = 1;
                } else {
                    checkBtMac = -1;
                }
            }
        }

        if ((mStbid != null) && mStbid.equalsIgnoreCase(mGetStbidStr)) {
            checkstbid = 1;
            EditStbid.selectAll();
        } else {
            checkstbid = -1;
            EditStbid.selectAll();
                    /*InputMethodManager imm = (InputMethodManager)
                            mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(v, 0);*/
        }
        setResult();
        return true;
    }

    private void setResult() {
        checkCA();
        StringBuffer strBuf = new StringBuffer();
        if (checksn > 0)
            strBuf.append("SN" + getString(R.string.check_success) + "\n");
        else if (checksn < 0)
            strBuf.append("SN" + getString(R.string.check_error) + "\n");

        if (checkstbid > 0)
            strBuf.append("STBID" + getString(R.string.check_success) + "\n");
        else if (checkstbid < 0)
            strBuf.append("STBID" + getString(R.string.check_error) + "\n");

        if (checkWifiMac > 0)
            strBuf.append("wifiMac" + getString(R.string.check_success) + "\n");
        else if (checkWifiMac < 0)
            strBuf.append("wifiMac" + getString(R.string.check_error) + "\n");

        if (checkBtMac > 0)
            strBuf.append("蓝牙Mac" + getString(R.string.check_success) + "\n");
        else if (checkBtMac < 0)
            strBuf.append("蓝牙Mac" + getString(R.string.check_error) + "\n");

        if (checkver > 0)
            strBuf.append(getString(R.string.tip_version_check) + getString(R.string.check_success) + "\n");
        else if (checkver < 0)
            strBuf.append(getString(R.string.tip_version_check) + getString(R.string.check_error) + "\n");

        if (checkCA > 0)
            strBuf.append(getString(R.string.get_ca) + getString(R.string.check_success) + "\n");
        else if (checkCA < 0)
            strBuf.append(getString(R.string.get_ca) + getString(R.string.check_error) + "\n");

        if ((checksn > 0) && (checkstbid > 0) && (checkver > 0)
            && ((bCheckWifiMac && checkWifiMac > 0) || !bCheckWifiMac)
            && ((bCheckBtMac && checkBtMac > 0) || !bCheckBtMac)
            && ((bCheckCA && checkCA > 0) || !bCheckCA)) {
            strBuf.append(getString(R.string.tip_facreset));
            TextCurrentStatus.setTextColor(Color.BLUE);
            TextCurrentStatus.setText(new String(strBuf));
            /*if (mfactReset != null)
                mfactReset.setVisibility(View.VISIBLE);*/

            if (mHandler != null) {
                if (mCommProp.mUseLocalPrompt) {
                    mHandler.sendEmptyMessageDelayed(MSG_FACREST, 2 * 1000);
                } else {
                    Common.sendDataReportBroadcast("", Config.PROOF_STATION, true, mContext);
                }
            }
        } else {
            TextCurrentStatus.setTextColor(Color.RED);
            TextCurrentStatus.setText(new String(strBuf));
        }
    }

    private final String TMP_DATA_FILE = "/data/local/todo_factory_default.txt";
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //LOGI(TAG,"[handleMessage]msg:"+msg);
            switch (msg.what) {
                case MSG_FACREST:
                    boolean resetFac = Common.getSysUtilHandler().setResetFacFlag(true);
                    LogUtils.debug(mContext, "resetFac flag : " + resetFac);
                    if (!resetFac) return;
                    File mResetFile = new File(TMP_DATA_FILE);
                    try {
                        mResetFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!mResetFile.exists()) return;
                    sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
                    try {
                        RecoverySystem.rebootWipeUserData(mContext);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };


}

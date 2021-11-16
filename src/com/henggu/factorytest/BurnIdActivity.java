package com.henggu.factorytest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.henggu.factorytest.common.CommProp;
import com.henggu.factorytest.common.Config;
import com.henggu.factorytest.utils.Common;
import com.henggu.factorytest.utils.LogUtils;

import java.math.BigInteger;

public class BurnIdActivity extends Activity {

    private Context mContext;
    private TextView mTipCheck = null;
    private LinearLayout mBurnLayout = null;
    private EditText EditSnAndMac = null;
    private EditText EditSn = null;
    private EditText EditMac = null;
    private EditText EditBurnSn = null;
    private EditText EditBurnMac = null;
    private TextView TextCurrentStatus = null;

    private String mSn = null;
    private String mMac = null;
    private String mMacStr = null;
    private CommProp mCommProp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burn);
        mContext = BurnIdActivity.this;
        mCommProp = new CommProp();
        initView();
    }

    private void initView() {
        mTipCheck = (TextView) findViewById(R.id.tip_check_test);

        mBurnLayout = (LinearLayout) findViewById(R.id.burnLayout);
        if (!checkTest())
            mBurnLayout.setVisibility(View.GONE);
        else
            mTipCheck.setVisibility(View.GONE);
        EditSnAndMac = (EditText) findViewById(R.id.input_code);
        EditSnAndMac.setInputType(InputType.TYPE_NULL);
        EditSnAndMac.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                LogUtils.debug(mContext, "keyCode is " + keyCode);
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    return onEditTextComplete();
                }
                return false;
            }
        });
        /*EditSnAndMac.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                return true;
            }
        });*/

        EditSn = (EditText) findViewById(R.id.get_sn);
        EditSn.setInputType(InputType.TYPE_NULL);
        EditBurnSn = (EditText) findViewById(R.id.sn_code);
        EditBurnSn.setInputType(InputType.TYPE_NULL);
        String sn = Common.getSysUtilHandler().readSN();
        if (sn != null)
            EditBurnSn.setText(sn);

        EditMac = (EditText) findViewById(R.id.get_mac);
        EditMac.setInputType(InputType.TYPE_NULL);
        EditBurnMac = (EditText) findViewById(R.id.mac_code);
        EditBurnMac.setInputType(InputType.TYPE_NULL);
        String mac = Common.getSysUtilHandler().getEthMacAddr();
        if (mac != null)
            EditBurnMac.setText(mac);

        TextCurrentStatus = (TextView) findViewById(R.id.current_status);
    }

    private boolean onEditTextComplete() {
        String inputStr = EditSnAndMac.getText().toString();
        int len = inputStr.length();
        String str = null;
        LogUtils.debug(mContext, "input is " + inputStr + ", length is " + len);
        if (len <= 0) {
            LogUtils.debug(mContext, "input string len <= 0!!");
            return true;
        }
        TextCurrentStatus.setText("");
        if (len == 12 || inputStr.contains(":")) {
            mMacStr = inputStr;
            if (!inputStr.contains(":")) {
                mMac = inputStr.substring(0, 2) + ":" + inputStr.substring(2, 4)
                        + ":" + inputStr.substring(4, 6) + ":" + inputStr.substring(6, 8)
                        + ":" + inputStr.substring(8, 10) + ":" + inputStr.substring(10);
            } else {
                mMacStr = inputStr.replaceAll(":", "");
                mMac = inputStr;
            }
            LogUtils.debug(mContext, "get mac: " + mMac);
            EditMac.setText(mMac);
        } else {
            EditSn.setText("");
            if (mCommProp.mSnCheckInitial == null) {
                if (mCommProp.mSnCheckDigit > 0) {
                    if (len != mCommProp.mSnCheckDigit) {
                        EditSnAndMac.setText("");
                        mSn = null;
                        str = getString(R.string.input_str) + "(" + inputStr + ")" + getString(R.string.input_fail);
                        TextCurrentStatus.setTextColor(Color.parseColor("#cc0000"));
                        TextCurrentStatus.setText(str);
                        LogUtils.debug(mContext, "check sn fail, sn digit is not right!");
                        return true;
                    }
                }
                mSn = inputStr;
            } else {
                if (!inputStr.startsWith(mCommProp.mSnCheckInitial)) {
                    EditSnAndMac.setText("");
                    mSn = null;
                    str = getString(R.string.input_str) + "(" + inputStr + ")" + getString(R.string.input_fail);
                    TextCurrentStatus.setTextColor(Color.parseColor("#cc0000"));
                    TextCurrentStatus.setText(str);
                    LogUtils.debug(mContext, "check sn fail, sn initial is not right!");
                    return true;
                }
                if (mCommProp.mSnCheckDigit > 0) {
                    if (len != mCommProp.mSnCheckDigit) {
                        EditSnAndMac.setText("");
                        mSn = null;
                        str = getString(R.string.input_str) + "(" + inputStr + ")" + getString(R.string.input_fail);
                        TextCurrentStatus.setTextColor(Color.parseColor("#cc0000"));
                        TextCurrentStatus.setText(str);
                        LogUtils.debug(mContext, "check sn fail, sn digit is not right!");
                        return true;
                    }
                }
                mSn = inputStr;
            }
            EditSn.setText(mSn);
        }

        EditSnAndMac.setText("");
        if ((mSn != null) && (mMac != null)) {
            LogUtils.debug(mContext, "=====start burn=====");
            EditSnAndMac.setFocusable(false);
            EditSnAndMac.setEnabled(false);
            startBurn();
            return true;
        } else {
            LogUtils.debug(mContext, "start burn fail, beacase input sn(" + mSn + ") or mac(" + mMac + ") is null!");
        }
        EditSnAndMac.requestFocus();
        return true;
    }

    public void startBurn() {
        boolean result = false;

        if (mMac != null) {
            if (mMac.length() != 17) {
                LogUtils.debug(mContext, "start_burn, input mac len error, mac: " + mMac);
                return;
            }
            if (mCommProp.mWifiMacBurnRules != null) {
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }
                BigInteger bLanMac = new BigInteger(mMacStr, 16);
                BigInteger bWifiMacRules = new BigInteger(mCommProp.mWifiMacBurnRules, 16);

                String sWifiMac = bLanMac.add(bWifiMacRules).toString(16);
                String sWifiMacPrefix = sWifiMac.substring(0, 6);
                String sLanMacPrefix = mMacStr.substring(0, 6);
                LogUtils.debug(mContext, "start_burn, WifiMac Pre: " + sWifiMacPrefix + " macpre: " + sLanMacPrefix + " wifiMac: " + sWifiMac);
                if (sWifiMacPrefix.equalsIgnoreCase(sLanMacPrefix)) {
                    LogUtils.debug(mContext, "start_burn, WifiMac Pre: " + sWifiMacPrefix + " lanmacpre: " + sLanMacPrefix + " wifiMac: " + sWifiMac);
                    String wifiMac = sWifiMac.substring(0, 2) + ":" + sWifiMac.substring(2, 4)
                            + ":" + sWifiMac.substring(4, 6) + ":" + sWifiMac.substring(6, 8)
                            + ":" + sWifiMac.substring(8, 10) + ":" + sWifiMac.substring(10,12);
                    result = Common.getSysUtilHandler().burnMac("mac_wifi", wifiMac);
                    if (!result) {
                        TextCurrentStatus.setTextColor(Color.parseColor("#cc0000"));
                        TextCurrentStatus.setText("wifi mac" + getString(R.string.burn_fail));
                        return;
                    }
                } else {
                    TextCurrentStatus.setTextColor(Color.parseColor("#cc0000"));
                    TextCurrentStatus.setText("wifi mac越界，请检查！");
                    return;
                }
            }

            if (mCommProp.mBTMacBurnRules != null) {
                BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
                if (ba != null) {
                    if (!ba.isEnabled()){
                        boolean blueEnable = ba.enable();
                        LogUtils.debug(mContext, "bluetooth open : " + blueEnable);
                    }
                    String mSysBtMac = ba.getAddress();
                    LogUtils.debug(mContext, "blue mac is:" + mSysBtMac);
                }
                BigInteger bLanMac = new BigInteger(mMacStr, 16);
                BigInteger bBTMacRules = new BigInteger(mCommProp.mBTMacBurnRules, 16);

                String sBtMac = bLanMac.add(bBTMacRules).toString(16);
                String sLanMacPrefix = mMacStr.substring(0, 6);
                String sBtMacPrefix = sBtMac.substring(0, 6);

                LogUtils.debug(mContext, "start_burn, BT Mac Pre: " + sBtMacPrefix + " macpre: " + sLanMacPrefix + " bTMac: " + sBtMac);
                if (sLanMacPrefix.equalsIgnoreCase(sBtMacPrefix)) {
                    String bTMac = sBtMac.substring(0, 2) + ":" + sBtMac.substring(2, 4)
                            + ":" + sBtMac.substring(4, 6) + ":" + sBtMac.substring(6, 8)
                            + ":" + sBtMac.substring(8, 10) + ":" + sBtMac.substring(10, 12);
                    result = Common.getSysUtilHandler().burnMac("mac_bt", bTMac);
                    if (!result) {
                        TextCurrentStatus.setTextColor(Color.parseColor("#cc0000"));
                        TextCurrentStatus.setText("BT mac" + getString(R.string.burn_fail));
                        return;
                    }
                } else {
                    TextCurrentStatus.setTextColor(Color.parseColor("#cc0000"));
                    TextCurrentStatus.setText("BT mac越界，请检查！");
                    return;
                }
            }


            result = Common.getSysUtilHandler().burnMac("mac", mMac);
            if (!result) {
                TextCurrentStatus.setTextColor(Color.parseColor("#cc0000"));
                TextCurrentStatus.setText(getString(R.string.burn_fail));
                return;
            }
        }
        String mac = Common.getSysUtilHandler().getEthMacAddr();
        if (mac != null)
            EditBurnMac.setText(mac);

        if (mSn != null) {
            boolean burnResult = Common.getSysUtilHandler().burnSN(mSn);
            if (!burnResult) {
                TextCurrentStatus.setTextColor(Color.parseColor("#cc0000"));
                TextCurrentStatus.setText(getString(R.string.burn_fail));
            }
        }
        String sn = Common.getSysUtilHandler().readSN();
        if (sn != null)
            EditBurnSn.setText(sn);

        if ((mac != null) && (sn != null)) {
            TextCurrentStatus.setTextColor(Color.parseColor("#00CD00"));
            TextCurrentStatus.setText(getString(R.string.burn_success));
        }
    }

    private boolean checkTest() {
        if (mCommProp.mSkipCheckLastStation) return true;
        int testResult = Common.getSysUtilHandler().getFunctiontestResult();
        if (testResult == Config.TAG_TEST_SUCC) {
            return true;
        }

        StringBuffer strBuf = new StringBuffer();
        if (testResult == Config.TAG_NO_TEST) {
            strBuf.append(getString(R.string.tip_test_result2));
        } else if (testResult == Config.TAG_TEST_FAIL) {
            strBuf.append(getString(R.string.tip_test_result3));
        }

        strBuf.append(getString(R.string.tip_test_result4));
        mTipCheck.setBackgroundColor(Color.parseColor("#cc0000"));
        mTipCheck.setText(new String(strBuf));
        return false;

    }
}

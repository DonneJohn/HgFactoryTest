package com.henggu.factorytest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.henggu.factorytest.common.CommProp;
import com.henggu.factorytest.common.WifiSimpleInfo;
import com.henggu.factorytest.utils.Common;
import com.henggu.factorytest.utils.LogUtils;
import com.henggu.factorytest.utils.WifiUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class IperfActivity extends Activity {
    private TextView tvLfResult, tvHfResult, tvWifiAddr;
    private EditText etCmdlineargs;
    private CommProp commProp;
    private String iperfCmd;
    private WifiManager wifiManager;
    private final  int MSG_RUN_SCTY_24G = 1;
    private final  int MSG_RUN_SCTY_5G = 2;
    private final  int MSG_GET_WIFI_ADDR = 3;
    private  WifiUtils mWifiUtils;
    private boolean run24g= false;
    private boolean run5g = false;
    private WifiSimpleInfo iperfConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iperf);
        registWifiBroadcast();
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        try {
            Common.unzip(getAssets().open("iperf.zip"), "/data/data/com.henggu.factorytest/bin");
        } catch (IOException e) {
            e.printStackTrace();
        }
        commProp = new CommProp();
        mWifiUtils = new WifiUtils(wifiManager);
        mWifiUtils.mConnectListener  = new WifiManager.ActionListener() {
            @Override
            public void onSuccess() {
                LogUtils.debug(IperfActivity.this, "Wifi AP connect successed!");
                runIperf();
            }

            @Override
            public void onFailure(int reason) {
                LogUtils.debug(IperfActivity.this, "Wifi AP connect failed, reason: " + reason);
            }
        };
        connectLfConfig();

        TextView tvEthAddr = (TextView) findViewById(R.id.eth_ipaddr);
        tvWifiAddr = (TextView) findViewById(R.id.wifi_ipaddr);
        tvLfResult = (TextView) findViewById(R.id.tv_lf_result);
        tvHfResult = (TextView) findViewById(R.id.tv_hf_result);
        etCmdlineargs = (EditText) findViewById(R.id.et_cmdlineargs);

        if (commProp.mIperfCmd != null) {
            iperfCmd = commProp.mIperfCmd;
        } else {
            iperfCmd = "iperf -s -p8000 -m -u -M";
        }
        etCmdlineargs.setText(iperfCmd);

        Button restartButton = (Button) findViewById(R.id.startstopButton);
//        restartButton.setFocusable(false);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btnStr = ((Button) v).getText().toString();
                if (getString(R.string.begin).equals(btnStr)) {
                    ((Button) v).setText(getString(R.string.stop));
                    connectLfConfig();
                } else {
                    ((Button) v).setText(getString(R.string.begin));
                }
            }
        });


        tvEthAddr.setText(getString(R.string.str_netip) + Common.getLocalIpAddress());

    }

    private void registWifiBroadcast(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction("com.henggu.factorytest.wificonnected");
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(new WifiChangeReceiver(), filter);
    }

    public class WifiChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){//wifi打开与否
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);

                if(wifistate == WifiManager.WIFI_STATE_DISABLED){
                    LogUtils.debug(IperfActivity.this,"wifi close");
                }
                else if(wifistate == WifiManager.WIFI_STATE_ENABLED){
                    LogUtils.debug(IperfActivity.this,"wifi open");
                }
            }else if (intent.getAction().equals("com.henggu.factorytest.wificonnected")){
                runIperf();
            }else if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                LogUtils.debug(IperfActivity.this,"receive wifi scan broadcast");
                mWifiUtils.configWifi(IperfActivity.this, iperfConfig);
            }

        }
    }


    private boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            NetworkInfo.State wifi = activeNetInfo.getState();
            if (wifi == NetworkInfo.State.CONNECTED) {
                //WIFI已连接
                return true;
            }
            return false;
        }
        return false;
    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
//            LogUtils.debug(IperfActivity.this, "msg is:" + msg.what + " :" + msg.obj );
            if (msg.what == MSG_RUN_SCTY_24G) {
                run24g = true;
                String iperfRlt = (String) msg.obj ;
                LogUtils.debug(IperfActivity.this, "iperfRlt is:" + iperfRlt );

                if(iperfRlt.contains("Sent") && iperfRlt.contains("datagrams")){
                    run24g = false;
                    killIperf();
                    tvLfResult.setText(iperfRlt);
                    LogUtils.debug(IperfActivity.this, "test iperf server" );
                    connectHfConfig();
                }


            }else if(msg.what == MSG_RUN_SCTY_5G){
                run5g = true;
                String iperfRlt = (String) msg.obj ;
                LogUtils.debug(IperfActivity.this, "5G iperfRlt is:" + iperfRlt );

                if(iperfRlt.contains("Sent") && iperfRlt.contains("datagrams")){
                    run5g = false;
                    killIperf();
                    tvHfResult.setText(iperfRlt);
                    LogUtils.debug(IperfActivity.this, "5G test iperf server" );
                    connectLfConfig();
                }

            }else if (msg.what == MSG_GET_WIFI_ADDR){

                tvWifiAddr.setText(getString(R.string.wifi_ipaddr) + msg.obj );
            }
            return false;
        }
    });

    private String intToIp(int i) {
        LogUtils.debug(IperfActivity.this, "add Network ip: " + i);
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    private void connectLfConfig() {
        iperfConfig = commProp.parseConfigWifi(commProp.TAG_IPERF_LF_CONFIG);
        LogUtils.debug(IperfActivity.this, "iperfLfConfig get from config : " + iperfConfig);
        if(iperfConfig == null){
            iperfConfig = new WifiSimpleInfo();
            iperfConfig.mSSID = "scty";
            iperfConfig.mPWD = "";
            iperfConfig.mMode = "static";
            iperfConfig.mIP = "192.168.1.3";
            iperfConfig.mMask = "255.255.255.0";
            iperfConfig.mGW = "192.168.1.1";
            iperfConfig.mDNS = "192.168.1.1";
        }
        if (mWifiUtils == null){
            mWifiUtils = new WifiUtils(wifiManager);
        }
        LogUtils.debug(IperfActivity.this, "iperfLfConfig");
        mWifiUtils.disconnectWifi();
        mWifiUtils.configWifi(this, iperfConfig);


    }

    private void connectHfConfig() {
        iperfConfig = commProp.parseConfigWifi(commProp.TAG_IPERF_HF_CONFIG);

        if(iperfConfig == null){
            iperfConfig = new WifiSimpleInfo();
            iperfConfig.mSSID = "SCTY-5G";
            iperfConfig.mPWD = "";
            iperfConfig.mMode = "static";
            iperfConfig.mIP = "192.168.1.4";
            iperfConfig.mMask = "255.255.255.0";
            iperfConfig.mGW = "192.168.1.1";
            iperfConfig.mDNS = "192.168.1.1";
        }

        if (iperfConfig != null) {
            if (mWifiUtils == null){
                mWifiUtils = new WifiUtils(wifiManager);
            }
            mWifiUtils.disconnectWifi();
            mWifiUtils.configWifi(this, iperfConfig);

        }
    }

    private void runIperf(){
        WifiInfo mWifiInfo;
        while(true){
            mWifiInfo = wifiManager.getConnectionInfo();
            final String ssid = mWifiInfo.getSSID();
            if (!TextUtils.isEmpty(ssid) && !ssid.equals("<unknown ssid>")){
                String wifiAddr = (mWifiInfo == null) ? "0" : intToIp(mWifiInfo.getIpAddress());
                Message msg = Message.obtain();
                msg.what = MSG_GET_WIFI_ADDR;
                msg.obj = wifiAddr;
                mHandler.sendMessage(msg);

                LogUtils.debug(IperfActivity.this, "before test iperf wifiAddr: " + wifiAddr
                        +" ssid:" + ssid);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LogUtils.debug(IperfActivity.this, "before test iperf: " + iperfCmd + " ssid:" + ssid);
                        String result = callCmd(iperfCmd, "", ssid);
                    }
                }).start();
                break;
            }
        }


    }

    private WifiConfigInfo parseWifiMsg(String configMsg) {
        WifiConfigInfo mWifiConfigInfo = new WifiConfigInfo();
        if (configMsg != null) {
            String[] configstrs = configMsg.split("\\|");
            if (configstrs.length == 2) {
                mWifiConfigInfo = new WifiConfigInfo();
                mWifiConfigInfo.mSsid = configstrs[0];
                mWifiConfigInfo.mpwd = configstrs[1];
            } else if (configstrs.length == 1) {
                mWifiConfigInfo = new WifiConfigInfo();
                mWifiConfigInfo.mSsid = configstrs[0];
                mWifiConfigInfo.mpwd = "";
            }
        } else {
            mWifiConfigInfo = null;
            LogUtils.debug(IperfActivity.this, "Config Wifi: " + configMsg + " is error!");
        }
        return mWifiConfigInfo;
    }

    public class WifiConfigInfo {
        public String mSsid = null;
        public String mpwd = null;
    }


    public String callCmd(String cmd, String filter, String ssid) {
        LogUtils.debug(IperfActivity.this, "ssid is : " + ssid);
        String result = "";
        String line = "";
        int mgsWhat = 0;
        if (ssid.equals("\"scty\"")){
            if (run24g) return null;
            mgsWhat = MSG_RUN_SCTY_24G;
        }else if(ssid.equals("\"SCTY-5G\"")){
            if (run5g) return null;
            mgsWhat = MSG_RUN_SCTY_5G;
        }
        LogUtils.debug(IperfActivity.this, "msg.what is : " + mgsWhat);
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStreamReader is = new InputStreamReader(proc.getInputStream());
            BufferedReader br = new BufferedReader(is);
            // Exec the command, retrieve the line with "filter"
            while ((line = br.readLine()) != null) {
                if (filter != null) {
                    if (line.contains(filter) == true) {
                        result += line;
                    }
                } else {
                    result += line;
                }
                Message msg = Message.obtain();
                msg.what = mgsWhat;
                LogUtils.debug(IperfActivity.this, "msg.what is : " + mgsWhat);
                msg.obj = line;
                mHandler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void killIperf(){
        LogUtils.debug(IperfActivity.this, "killall iperf");
        try {
            Process proc = Runtime.getRuntime().exec("killall iperf");
            InputStreamReader is = new InputStreamReader(proc.getInputStream());
            BufferedReader br = new BufferedReader(is);
            // Exec the command, retrieve the line with "filter"
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

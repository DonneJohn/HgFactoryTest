package com.henggu.factorytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.henggu.factorytest.common.WifiSimpleInfo;
import com.henggu.factorytest.utils.LogUtils;
import com.henggu.factorytest.utils.WifiUtils;

public class WifiToolsReceiver extends BroadcastReceiver {

    private  WifiUtils mWifiUtils;
    private WifiSimpleInfo iperfConfig;
    private WifiManager wifiManager;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("com.henggu.factorytest.WIFI_TOOLS")){

            String ssid = intent.getExtras().getString("SSID");
            String pwd = intent.getExtras().getString("PWD");
            String mode = intent.getExtras().getString("MODE");
            String ip = intent.getExtras().getString("IP");
            String mask = intent.getExtras().getString("MASK");
            String gw = intent.getExtras().getString("GATEWAY");
            String dns = intent.getExtras().getString("DNS");

            LogUtils.debug(context, "iperfLfConfig msg : " + ssid + " pwd:" + pwd
                    + " mode:" + mode + " ip:" + ip + " mask:" + mask + " gw:" + gw + " dns:" + dns);

            if(iperfConfig == null){
                iperfConfig = new WifiSimpleInfo();
            }

            iperfConfig.mSSID = ssid;
            iperfConfig.mPWD = pwd;
            iperfConfig.mMode = mode;
            iperfConfig.mIP = ip;
            iperfConfig.mMask = mask;
            iperfConfig.mGW = gw;
            iperfConfig.mDNS = dns;
            if (wifiManager == null){
                wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            }
            if (mWifiUtils == null){
                mWifiUtils = new WifiUtils(wifiManager);
            }
            //mWifiUtils.disconnectWifi();
            //mWifiUtils.configWifi(context, iperfConfig);
            mWifiUtils.wifiConfigImpl(iperfConfig);
        }


    }
}

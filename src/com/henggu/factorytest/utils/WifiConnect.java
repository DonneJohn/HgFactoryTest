package com.henggu.factorytest.utils;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

public class WifiConnect {

    private static final String TAG = "SocketTest";
    private WifiManager wifiManager;

    // 定义WifiInfo对象
    private WifiInfo mWifiInfo;

    //构造函数
    public WifiConnect(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
        // 取得WifiInfo对象
        mWifiInfo = wifiManager.getConnectionInfo();
    }

    //打开wifi功能
    public boolean OpenWifi() {
        boolean bRet = true;
        if (!wifiManager.isWifiEnabled()) {
            bRet = wifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    //提供一个外部接口，传入要连接的无线网
    public boolean connect(String SSID, String Password) {
        if (!this.OpenWifi()) {
            return false;
        }
        //开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
        //状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
        while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            try {
                //为了避免程序一直while循环，让它睡个100毫秒在检测……
                Thread.currentThread();
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        }

        WifiConfiguration wifiConfig = this.CreateWifiInfo(SSID, Password);
        //
        if (wifiConfig == null) {
            return false;
        }

        WifiConfiguration tempConfig = this.IsExsits(SSID);

        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }

        int netID = wifiManager.addNetwork(wifiConfig);
        boolean bRet = wifiManager.enableNetwork(netID, true);
        Log.i(TAG, "net work enable: " + bRet + " netId: " + netID);
        boolean connected = wifiManager.reconnect();
        Log.i(TAG, "net work connected: " + connected);
        return connected;
    }

    //查看以前是否也配置过这个网络
    private WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    private WifiConfiguration CreateWifiInfo(String ssid,
                                             String pwd) {
        WifiConfiguration localWifiConfiguration = new WifiConfiguration();

        localWifiConfiguration.SSID = ("\"" + ssid + "\"");
        if (pwd != null) {
            localWifiConfiguration.preSharedKey = '"' + pwd + '"';
        }
        localWifiConfiguration.hiddenSSID = true;
        localWifiConfiguration.allowedAuthAlgorithms.clear();
        localWifiConfiguration.allowedGroupCiphers.clear();
        localWifiConfiguration.allowedKeyManagement.clear();
        localWifiConfiguration.allowedPairwiseCiphers.clear();
        localWifiConfiguration.allowedProtocols.clear();

//        localWifiConfiguration.wepKeys[0] = "";
        localWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//        localWifiConfiguration.wepTxKeyIndex = 0;

        int i = wifiManager.addNetwork(localWifiConfiguration);
        Log.i(TAG, "add Network returned " + i);
        boolean bool = wifiManager.enableNetwork(i, true);
        Log.i(TAG, "enableNetwork returned " + bool);
        return localWifiConfiguration;
    }

    // 得到IP地址
    public String getIPAddress() {
        return (mWifiInfo == null) ? "0" : intToIp(mWifiInfo.getIpAddress());
    }

    private String intToIp(int i) {
        Log.i(TAG, "add Network ip: " + i);
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

}
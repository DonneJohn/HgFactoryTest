package com.henggu.factorytest.utils;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration.IpAssignment;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.henggu.factorytest.common.WifiSimpleInfo;

import java.net.InetAddress;
import java.util.List;

public class WifiUtils {
    public static final String TAG = "WifiUtils";

    private static final String MODE_DHCP = "dhcp";
    private static final String MODE_STATIC = "static";

    /*
     * Wifi configuration
     */
    private static final String SECURITY_NONE = "NONE";
    private static final String SECURITY_WEP = "WEP";
    private static final String SECURITY_PSK = "PSK";
    private static final String SECURITY_EAP = "EAP";

    protected WifiManager mWM = null;
    public WifiManager.ActionListener mConnectListener;

    public WifiUtils(WifiManager wifiManager){
        mWM = wifiManager;
    }

    public void configWifi(Context context, WifiSimpleInfo wifiInfo) {
        Log.i(TAG, "hgSysUtils_amlogic configWifi");

        if (mWM == null) {
            mWM = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }

        if (wifiInfo != null) {

            if (!mWM.isWifiEnabled()) {
                Log.i(TAG, "Wifi not Enabled");
                mWM.setWifiEnabled(true);
            }else {
                WifiInfo mConWifiInfo = mWM.getConnectionInfo();
                if (mConWifiInfo != null) {
                    Log.d(TAG, "getConnectionInfo ssid: " + mConWifiInfo.getSSID());
                }
                if ((wifiInfo.mSSID != null) && (mConWifiInfo != null) && (mConWifiInfo.getSSID() != null)
                        && mConWifiInfo.getSSID().equals(convertToQuotedString(wifiInfo.mSSID))) {
                    Log.w(TAG, wifiInfo.mSSID + " is connected!");
                    context.sendBroadcast(new Intent("com.henggu.factorytest.wificonnected"));
                } else {
                    scanAP(wifiInfo);
                }
            }
        }

    }

    public void wifiConfigImpl(WifiSimpleInfo mWifiInfo) {
        if ((mWifiInfo == null) || (mWifiInfo.mSSID == null) || (mWifiInfo.mPWD == null)) {
            Log.w(TAG, "wifiConfigImpl mWifiInfo, mWifiInfo.mSSID or mWifiInfo.mPWD is null!");
            return;
        }
        WifiConfiguration config = new WifiConfiguration();
        LinkProperties mLinkProperties = new LinkProperties();

        config.SSID = convertToQuotedString(mWifiInfo.mSSID);
        if (SECURITY_NONE.equals(mWifiInfo.mSecurity)) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (SECURITY_WEP.equals(mWifiInfo.mSecurity)) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            int length = mWifiInfo.mPWD.length();
            if ((length == 10 || length == 26 || length == 58) &&
                    mWifiInfo.mPWD.matches("[0-9A-Fa-f]*")) {
                config.wepKeys[0] = mWifiInfo.mPWD;
            } else {
                config.wepKeys[0] = '"' + mWifiInfo.mPWD + '"';
            }
            Log.d(TAG, "WEP password: " + config.wepKeys[0]);
        } else if (SECURITY_PSK.equals(mWifiInfo.mSecurity)) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            if (mWifiInfo.mPWD.matches("[0-9A-Fa-f]{64}")) {
                config.preSharedKey = mWifiInfo.mPWD;
            } else {
                config.preSharedKey = '"' + mWifiInfo.mPWD + '"';
            }
            Log.d(TAG, "PSK password: " + config.preSharedKey);
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            Log.w(TAG, "Security mode: " + mWifiInfo.mSecurity + " is not support!");
        }

        if (MODE_DHCP.equals(mWifiInfo.mMode)) {
            config.ipAssignment = IpAssignment.DHCP;
        } else if (MODE_STATIC.equals(mWifiInfo.mMode)) {
            config.ipAssignment = IpAssignment.STATIC;
            validateIpConfigFields(mWifiInfo, mLinkProperties);
        } else {
            config.ipAssignment = IpAssignment.DHCP;
            Log.w(TAG, "wifiConfigImpl mode: " + mWifiInfo.mMode + " is not support!");
        }
        config.linkProperties = new LinkProperties(mLinkProperties);

        mWM.connect(config, mConnectListener);
    }

    protected String getSecurity(ScanResult result) {
        if ((result == null) || (result.capabilities == null)) {
            Log.w(TAG, "getSecurity result or result.capabilities is null!");
            return SECURITY_NONE;
        }
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }


    /*
     * WiFi configuration related
     */
    protected String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    private void validateIpConfigFields(WifiSimpleInfo mWifiInfo, LinkProperties linkProperties) {
        if (mWifiInfo == null) {
            Log.w(TAG, "validateIpConfigFields mWifiInfo is null!");
            return;
        }
        if (TextUtils.isEmpty(mWifiInfo.mIP)) {
            Log.w(TAG, "IP: " + mWifiInfo.mIP + " is invalid!");
            return;
        }

        InetAddress inetAddr = null;
        try {
            inetAddr = NetworkUtils.numericToInetAddress(mWifiInfo.mIP);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

        int networkPrefixLength = -1;
        try {
            networkPrefixLength = getNetMask(mWifiInfo.mMask);
            if (networkPrefixLength < 0 || networkPrefixLength > 32) {
                Log.w(TAG, "Mask: " + mWifiInfo.mMask + " is invalid!");
                return;
            }

            linkProperties.addLinkAddress(new LinkAddress(inetAddr, networkPrefixLength));
            linkProperties.addRoute(new RouteInfo(new LinkAddress(inetAddr, networkPrefixLength)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }

        if (TextUtils.isEmpty(mWifiInfo.mGW)) {
            Log.w(TAG, "GW: " + mWifiInfo.mGW + " is invalid!");
            return;
        }

        InetAddress gatewayAddr = null;
        try {
            gatewayAddr = NetworkUtils.numericToInetAddress(mWifiInfo.mGW);
            linkProperties.addRoute(new RouteInfo(gatewayAddr));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

        InetAddress dnsAddr = null;
        if (TextUtils.isEmpty(mWifiInfo.mDNS)) {
            Log.w(TAG, "Dns: " + mWifiInfo.mDNS + " is invalid!");
            return;
        }
        try {
            dnsAddr = NetworkUtils.numericToInetAddress(mWifiInfo.mDNS);
            linkProperties.addDns(dnsAddr);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }
    }

    public void scanAP(WifiSimpleInfo mWifiInfo) {
        List<WifiConfiguration> configs = (mWM != null) ? mWM.getConfiguredNetworks() : null;
        if ((mWifiInfo != null) && (configs != null)) {
            for (WifiConfiguration config : configs) {
                Log.d(TAG, config.SSID + "/" + mWifiInfo.mSSID);
                if ((config.SSID != null) && (mWifiInfo.mSSID != null)
                        && config.SSID.equals(convertToQuotedString(mWifiInfo.mSSID))) {
                    Log.w(TAG, config.SSID + " is configured!");
                    wifiConfigImpl(mWifiInfo);
                    return;
                }
            }
        }

        List<ScanResult> results = (mWM != null) ? mWM.getScanResults() : null;
        if ((mWifiInfo != null) && (results != null)) {
            for (ScanResult result : results) {
                if (result.SSID == null || result.SSID.length() == 0
                        || result.capabilities.contains("[IBSS]")) {
                    continue;
                }
                Log.d(TAG, "scanAP ssid: " + result.SSID + ", bssid: " + result.BSSID);
                if (result.SSID.equals(mWifiInfo.mSSID)) {
                    mWifiInfo.mSecurity = getSecurity(result);
                    Log.d(TAG, "wifiConfigImpl ssid: " + result.SSID + ", bssid: " + result.BSSID);
                    wifiConfigImpl(mWifiInfo);
                    return;
                }
            }
            Log.w(TAG, "scanAP not find ssid: " + mWifiInfo.mSSID);
        }
    }

    private int getNetMask(String netmarks) {
        StringBuffer sbf;
        String str;
        int inetmask = 0, len1 = 0, len2 = 0;
        String[] ipList = netmarks.split("\\.");
        len1 = ipList.length;
        for (int n = 0; n < len1; n++) {
            sbf = toBin(Integer.parseInt(ipList[n]));
            str = sbf.reverse().toString();
            len2 = str.length();
            for (int i = 0; i < len2; i++) {
                char ch = str.charAt(i);
                if (ch != '1') return inetmask;
                inetmask++;
            }
        }
        return inetmask;
    }

    private StringBuffer toBin(int x) {
        StringBuffer result = new StringBuffer();
        result.append(x % 2);
        x /= 2;
        while (x > 0) {
            result.append(x % 2);
            x /= 2;
        }
        return result;
    }

    public void disconnectWifi() {
        WifiInfo wifiInfo = mWM.getConnectionInfo();
        int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
        if (mWM.isWifiEnabled() && ipAddress != 0) {
            int netid = mWM.getConnectionInfo().getNetworkId();
            mWM.disableNetwork(netid);
            mWM.disconnect();
        }

    }


}

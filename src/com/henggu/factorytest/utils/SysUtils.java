package com.henggu.factorytest.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.os.SystemProperties;
import android.util.Log;

import com.henggu.factorytest.common.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SysUtils {

    public boolean setLEDStatus(String ledName, boolean isOn) {
        LogUtils.debug(Config.TAG, getClass().getSimpleName() + " [ledname]: " + ledName);
        return false;
    }


    public boolean isHdmiOutputMode() {
        return true;
    }

    public boolean isAvOutputMode() {
        return true;
    }

    public void setAvOutputDispMode() {

    }

    public void setHdmiOutputDispMode() {

    }

    public boolean isHDMISuspendEnable() {
        return true;
    }

    public String readWifiId() {
        return Common.getFileValue(Config.WIFI_TEST_SCANID);
    }

    public String getProjectTarget() {
        return "ott";
    }

    public String getProjectProvince() {
        return "all";
    }

    public String getEthMacAddr() {
        return Common.getFileValue(Config.ETH_MAC_ADDR);
    }

    public String getWifiMacAddr() {
        return Common.getFileValue(Config.WIFI_MAC_ADDR);
    }

    public String getWifiAddr(Context context){
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo mConWifiInfo = wm.getConnectionInfo();
        if(mConWifiInfo == null) return "";
        long ipAddressInt = mConWifiInfo.getIpAddress();
        return longToIp2(ipAddressInt);
    }

    public String longToIp2(long ip) {
    return (ip & 0xFF) + "."
        + ((ip >> 8) & 0xFF) + "."
        + ((ip >> 16) & 0xFF) + "."
        + ((ip >> 24) & 0xFF);
    }

    public String getStbid(){
        return SystemProperties.get("ro.serialno");
    }

    public String readSN() {
        return null;
    }

    public boolean burnSN(String sn) {
        return false;
    }

    public boolean burnMac(String macType, String mac) {
        return false;
    }

    public boolean supportBluetooth() {
        return false;
    }

    public void setFunctiontestResult(boolean pass) {
    }

    public int getFunctiontestResult() {
        return 0;
    }

    public boolean setResetFacFlag(boolean flag){
        return false;
    }

    public String readBobSN() {
        return null;
    }

    public String readCAState(){
        return null;
    }

}

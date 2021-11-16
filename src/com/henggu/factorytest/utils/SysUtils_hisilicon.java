package com.henggu.factorytest.utils;

import android.os.SystemProperties;
import android.text.TextUtils;

import com.henggu.factorytest.common.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class SysUtils_hisilicon extends SysUtils {

    public static final int HDMI = 0;
    public static final int AV = 1;

    public static final int DISABLE = 0;
    public static final int ENABLE = 1;

    private static final String FUNCTIONTEST_RESULT_FILE = "/data/local/functiontest_result";

    @Override
    public boolean setLEDStatus(String ledName, boolean isOn) {
        if (isOn) {
            Common.callCmd("gpio-led " + ledName + " on", null);
        } else {
            Common.callCmd("gpio-led " + ledName + " off", null);
        }
        return true;
    }

    @Override
    public boolean isHdmiOutputMode() {
        String HiDisplayManagerClassName;
        if (Common.isHisiV300()) {
            HiDisplayManagerClassName = "com.hisilicon.android.hidisplaymanager.HiDisplayManager";
        } else {
            HiDisplayManagerClassName = "com.hisilicon.android.HiDisplayManager";
        }
        int result = (Integer) ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName(HiDisplayManagerClassName),
                "getOutputEnable", HDMI);
        LogUtils.debug(Config.TAG, "isHdmiOutputMode: " + result);
        return result == ENABLE;
    }

    @Override
    public boolean isAvOutputMode() {
        String HiDisplayManagerClassName;
        if (Common.isHisiV300()) {
            HiDisplayManagerClassName = "com.hisilicon.android.hidisplaymanager.HiDisplayManager";
        } else {
            HiDisplayManagerClassName = "com.hisilicon.android.HiDisplayManager";
        }
        int result = (Integer) ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName(HiDisplayManagerClassName),
                "getOutputEnable", AV);

        LogUtils.debug(Config.TAG, "isAvOutputMode: " + result);
        return result == ENABLE;
    }

    @Override
    public void setAvOutputDispMode() {
        String HiDisplayManagerClassName;
        if (Common.isHisiV300()) {
            HiDisplayManagerClassName = "com.hisilicon.android.hidisplaymanager.HiDisplayManager";
            ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName("com.hisilicon.android.hisysmanager.HiSysManager"),
                "adjustDevState", "proc/msp/disp1", " fmt pal ");
        } else {
            HiDisplayManagerClassName = "com.hisilicon.android.HiDisplayManager";
        }
        int hdmistate = (Integer) ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName(HiDisplayManagerClassName),
                "setOutputEnable", HDMI, DISABLE);
        LogUtils.debug(Config.TAG, "setAv hdmi state: " + hdmistate);
        int result = (Integer) ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName(HiDisplayManagerClassName),
                "setOutputEnable", AV, ENABLE);
        LogUtils.debug(Config.TAG, "setAvOutputDispMode: " + result);
    }

    @Override
    public void setHdmiOutputDispMode() {
        String HiDisplayManagerClassName;
        if (Common.isHisiV300()) {
            HiDisplayManagerClassName = "com.hisilicon.android.hidisplaymanager.HiDisplayManager";
        } else {
            HiDisplayManagerClassName = "com.hisilicon.android.HiDisplayManager";
        }
        ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName(HiDisplayManagerClassName),
                "setOutputEnable", AV, DISABLE);
        int result = (Integer) ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName(HiDisplayManagerClassName),
                "setOutputEnable", HDMI, ENABLE);
        LogUtils.debug(Config.TAG, "setHdmiOutputDispMode: " + result);
    }

    @Override
    public boolean isHDMISuspendEnable() {
        String HiDisplayManagerClassName;
        if (Common.isHisiV300()) {
            HiDisplayManagerClassName = "com.hisilicon.android.hidisplaymanager.HiDisplayManager";
        } else {
            HiDisplayManagerClassName = "com.hisilicon.android.HiDisplayManager";
        }
        int manufResult = (Integer) ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName(HiDisplayManagerClassName),
                "getDisplayDeviceType");
        LogUtils.debug(Config.TAG, "isHDMISuspendEnable: " + manufResult);
        return manufResult == 1 ? true : false;
    }

    @Override
    public String getProjectTarget() {
        return SystemProperties.get("ro.hgi.proj_target", "unknown");
    }

    @Override
    public String getProjectProvince() {
        return SystemProperties.get("ro.hgi.proj_province", "unknown");
    }

    public String getSysParam(String tag, String default_value) {
        String val = default_value;
        int len = tag.length();
        CommandPipe cs = new CommandPipe();

        String cmd = "rootcmd mtdinfo get " + tag;
        String result = cs.execute(cmd);

        LogUtils.debug(Config.TAG, "getSysParam: " + result);
        if (result.length() > len) {
            val = result.substring(len + 2, result.length() - 1);
        }

        return val;
    }

    public boolean setSysParam(String tag, String val) {
        boolean ret = false;
        CommandPipe cs = new CommandPipe();

        String cmd = "rootcmd mtdinfo set " + tag + " " + val;
        String result = cs.execute(cmd);
        if (result.equals("success")) {
            ret = true;
        }

        return ret;
    }

    @Override
    public String readSN() {
        String sn = getSysParam("sn", "");
        LogUtils.debug(Config.TAG, "get sn result: " + sn);
        return sn;
        /*String sn;
        if (Common.isHisiV300()) {
            sn = (String) ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName("com.hisilicon.android.hisysmanager.HiSysManager"),
                    "getDeviceinfo", 18, 24);
        } else {
            ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName("com.hisilicon.android.hisysmanager.HiSysManager"),
                    "getFlashInfo", "deviceinfo", 18, 24);
            sn = Common.getFileValue("/mnt/mtdinfo");
        }
        return sn;*/
    }

    @Override
    public boolean burnSN(String sn) {
        return setSysParam("sn", sn);
        /*int result;
        if (Common.isHisiV300()) {
            result = (Integer) ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName("com.hisilicon.android.hisysmanager.HiSysManager"),
                    "setDeviceinfo", 18, sn.length(), sn);
        } else {
            result = (Integer) ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName("com.hisilicon.android.hisysmanager.HiSysManager"),
                    "setFlashInfo", "deviceinfo", 18, sn.length(), sn);
        }
        return result == 0 ? true : false;*/
    }

    @Override
    public String readBobSN() {
        String bobsn = getSysParam("tyid", "");
        LogUtils.debug(Config.TAG, "get bobsn result: " + bobsn);
        return bobsn;
    }

    @Override
    public boolean burnMac(String macType, String mac) {
        return setSysParam(macType, mac);
        /*int result;
        if (Common.isHisiV300()) {
            result = (Integer) ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName("com.hisilicon.android.hisysmanager.HiSysManager"),
                    "setDeviceinfo", 0, 17, mac);
        } else {
            result = (Integer) ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName("com.hisilicon.android.hisysmanager.HiSysManager"),
                    "setFlashInfo", "deviceinfo", 0, 17, mac);
        }
        return result == 0 ? true : false;*/
    }

    @Override
    public boolean supportBluetooth() {
        String chiptype = SystemProperties.get("ro.product.chiptype", "unknown");
        return "Hi3798MV300".equals(chiptype) ? true : false;
    }

    @Override
    public void setFunctiontestResult(boolean pass) {
        if (pass)
            Common.writeFileValue(FUNCTIONTEST_RESULT_FILE, "pass");
        else
            Common.writeFileValue(FUNCTIONTEST_RESULT_FILE, "fail");
    }

    @Override
    public int getFunctiontestResult() {
        String result = Common.getFileValue(FUNCTIONTEST_RESULT_FILE);
        if ("pass".equals(result)) {
            return Config.TAG_TEST_SUCC;
        } else if ("fail".equals(result)) {
            return Config.TAG_TEST_FAIL;
        } else {
            return Config.TAG_NO_TEST;
        }
    }

    @Override
    public boolean setResetFacFlag(boolean flag) {
                int result;
        String flagStr = flag ? "true" : "false";
        if (Common.isHisiV300()) {
            result = (Integer) ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName("com.hisilicon.android.hisysmanager.HiSysManager"),
                    "setDeviceinfo", 122, flagStr.length(), flagStr);
        } else {
            result = (Integer) ReflectUtils.invokeMethod(ReflectUtils.getClassObjectFromName("com.hisilicon.android.hisysmanager.HiSysManager"),
                    "setFlashInfo", "deviceinfo", 122, flagStr.length(), flagStr);
        }
        return result == 0 ? true : false;
    }

    @Override
    public String readCAState(){
        CommandPipe cs = new CommandPipe();
        String cmd = "rootcmd lockchip getlockstate";
        String result = cs.execute(cmd);
        return result;
    }

}

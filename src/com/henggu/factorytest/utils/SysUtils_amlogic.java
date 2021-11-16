package com.henggu.factorytest.utils;

import android.os.SystemProperties;

import com.henggu.factorytest.HgApplication;
import com.henggu.factorytest.common.Config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SysUtils_amlogic extends SysUtils {
    private static final String HDMI_HPD_STATE = "/sys/class/amhdmitx/amhdmitx0/hpd_state";
    private static final String HDMI_DISP_LIST_SYSFS = "/sys/class/amhdmitx/amhdmitx0/disp_cap";

    private static final String NET_LED_FILE = "/proc/ledlight/netled/state";
    private static final String POWER_LED_FILE = "/proc/ledlight/powerled/state";

    private static final String HDMI_DISP_MODE = "/sys/class/display/mode";
    private static final String MOD_OFF = "/sys/class/aml_mod/mod_off";
    private static final String MOD_ON = "/sys/class/aml_mod/mod_on";

    private static final String FB0_FREESCALE_MODE = "/sys/class/graphics/fb0/freescale_mode";
    private static final String FB0_FREE_SCALE_AXIS = "/sys/class/graphics/fb0/free_scale_axis";
    private static final String FB0_WINDOW_AXIS = "/sys/class/graphics/fb0/window_axis";
    private static final String FB0_FREE_SCALE = "/sys/class/graphics/fb0/free_scale";

    private static final String ATTACH_FILE = "/sys/class/unifykeys/attach";
    private static final String NAME_FILE = "/sys/class/unifykeys/name";
    private static final String WRITE_FILE = "/sys/class/unifykeys/write";
    private static final String READ_FILE = "/sys/class/unifykeys/read";



    /*
    @Override
    public boolean setLEDStatus(String ledName, boolean isOn) {
        LogUtils.debug(Config.TAG, getClass().getSimpleName() + " [ledname]: " + ledName);

        String cmd = null;
        String led = null;

        if (ledName.equals("net")) {
            led = "netled";
        } else if (ledName.equals("pwr")) {
            led = "powerled";
        } else {
            return false;
        }

        if (isOn) {
            cmd = "echocmd.sh led on " + led;
        } else {
            cmd = "echocmd.sh led off " + led;
        }

        // Log.i(TAG, "hgSysUtils_amlogic setLEDStatus command: " + cmd);
        Common.callCmd(cmd, null);

        return false;
    } */

    private static Object mSyWriter;

    private static Object getSysWrite() {
        if (mSyWriter == null) {
            mSyWriter = HgApplication.mContext.getSystemService("system_write");
        }
        return mSyWriter;
    }

    @Override
    public boolean setLEDStatus(String ledName, boolean isOn) {
        String led;
        String state;
        if (ledName.equals("net")) {
            led = NET_LED_FILE;
        } else if (ledName.equals("pwr")) {
            led = POWER_LED_FILE;
        } else {
            return false;
        }

        if (isOn) {
            state = "on";
        } else {
            state = "off";
        }

        ReflectUtils.invokeMethod(getSysWrite(), "writeSysfs", led, state);
        return true;
    }

    @Override
    public boolean isHdmiOutputMode() {
        if (isHDMIPlugged(getSysWrite())) {
            String dispCap = null;
            dispCap = (String) ReflectUtils.invokeMethod(getSysWrite(), "readSysfs", HDMI_DISP_LIST_SYSFS);
            LogUtils.debug(Config.TAG, "disp_cap: " + dispCap);
            if ((dispCap != null) && !dispCap.contains("null edid")) {
                return true;
            }
            LogUtils.debug(Config.TAG, "hdmi disp_cap is error, disp_cap: " + dispCap);
        }
        return false;
    }

    private boolean isHDMIPlugged(Object mSysWriter) {
        String status = null;
        if (mSysWriter != null)
            status = (String) ReflectUtils.invokeMethod(mSysWriter, "readSysfs", HDMI_HPD_STATE);
        LogUtils.debug(Config.TAG, "hpd_state: " + status);
        if ("1".equals(status))
            return true;
        else
            return false;
    }

    @Override
    public boolean isAvOutputMode() {
//        return !isHdmiOutputMode();
        return true;
    }

    @Override
    public String getProjectTarget() {
        return SystemProperties.get("sys.proj.type", "unknown");
    }

    @Override
    public String getProjectProvince() {
        return SystemProperties.get("sys.proj.tender.type", "unknown");
    }

    @Override
    public void setAvOutputDispMode() {
        openVdac();
        setCvbs();
    }

    private void openVdac() {
        if (SystemProperties.getBoolean("ro.platform.hdmionly", false)) {
            ReflectUtils.invokeMethod(getSysWrite(), "writeSysfs", MOD_ON, "vdac");
        }
    }

    private void setCvbs() {
        Object mSysWriter = getSysWrite();
        if (mSysWriter != null) {
            ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", HDMI_DISP_MODE, "576cvbs");
            ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", FB0_FREESCALE_MODE, "1");
            if (SystemProperties.get("ubootenv.var.uimode", "720p").equals("1080p")) {
                ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", FB0_FREE_SCALE_AXIS, "0 0 1919 1079");
            } else {
                ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", FB0_FREE_SCALE_AXIS, "0 0 1279 719");
            }
            ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", FB0_WINDOW_AXIS, "0 0 719 575");
            ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", FB0_FREE_SCALE, "0x10001");
        }
    }

    @Override
    public void setHdmiOutputDispMode() {
        closeVdac();
        setHdmi();
    }

    private void closeVdac() {
        if (SystemProperties.getBoolean("ro.platform.hdmionly", false)) {
            ReflectUtils.invokeMethod(getSysWrite(), "writeSysfs", MOD_OFF, "vdac");
        }
    }

    private void setHdmi() {
        Object mSysWriter = getSysWrite();
        if (mSysWriter != null) {
            String outmode = getHdmiMode("720p");
            if (outmode == null) {
                LogUtils.debug(Config.TAG, "set hdmi outmode is null");
                return;
            }
            ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", HDMI_DISP_MODE, outmode);
            ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", FB0_FREESCALE_MODE, "1");
            if (SystemProperties.get("ubootenv.var.uimode", "720p").equals("1080p")) {
                ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", FB0_FREE_SCALE_AXIS, "0 0 1919 1079");
            } else {
                ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", FB0_FREE_SCALE_AXIS, "0 0 1279 719");
            }
            ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", FB0_WINDOW_AXIS, "0 0 1279 719");
            ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", FB0_FREE_SCALE, "0x10001");
        }
    }

    private String getHdmiMode(String mode) {
        String str = null;
        try {
            FileReader fr = new FileReader(HDMI_DISP_LIST_SYSFS);
            BufferedReader br = new BufferedReader(fr);
            try {
                while ((str = br.readLine()) != null) {
                    if (str != null) {
                        if (str.contains("null edid")) {
                            LogUtils.debug(Config.TAG, "getHdmiMode error, disp_cap: " + str);
                            return null;
                        }
                        String outMode = null;
                        if (str.contains("*")) {
                            outMode = new String(str.substring(0, str.length() - 1));
                        } else {
                            outMode = new String(str);
                        }
                        LogUtils.debug(Config.TAG, "getHdmiMode is: " + outMode);
                        if ((outMode != null) && (outMode.contains(mode))) {
                            fr.close();
                            br.close();
                            return outMode;
                        }
                    }
                }
                fr.close();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setFunctiontestResult(boolean pass) {
        if (pass) {
            burnFactory("pass");
        } else {
            burnFactory("fail");
        }
    }

    private boolean burnFactory(String value) {
        return writeAmlSystemValue("tyfactory", value);
    }

    @Override
    public int getFunctiontestResult() {
        String result = readFactory();
        if ("pass".equals(result)) {
            return Config.TAG_TEST_SUCC;
        } else if ("fail".equals(result)) {
            return Config.TAG_TEST_FAIL;
        } else {
            return Config.TAG_NO_TEST;
        }
    }

    private String readFactory() {
        return readAmlSystemValue("tyfactory");
    }

    @Override
    public boolean supportBluetooth() {
        String chiptype = SystemProperties.get("ro.product.chiptype2", "unknown");
        return "S905L3".equals(chiptype) ? true : false;
    }

    @Override
    public boolean burnMac(String macType, String mac) {
        return writeAmlSystemValue(macType, mac);
    }

    @Override
    public boolean burnSN(String sn) {
        return writeAmlSystemValue("usid", sn);
    }

    @Override
    public String readSN() {
        return readAmlSystemValue("usid");
    }

    @Override
    public String readBobSN() {
        return readAmlSystemValue("tystbid");
    }

    @Override
    public boolean setResetFacFlag(boolean flag) {
        return writeAmlSystemValue("tyresetfac",String.valueOf(flag));
    }

    private boolean writeAmlSystemValue(String key, String value) {
        boolean ret = false;
        Object mSysWriter = getSysWrite();
        if (mSysWriter == null) {
            return ret;
        }
        ret = (Boolean) ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", ATTACH_FILE, "1");
        if (!ret) {
            return ret;
        }
        ret = (Boolean) ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", NAME_FILE, key);
        if (!ret) {
            return ret;
        }
        ret = (Boolean) ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", WRITE_FILE, value);
        if (!ret) {
            return ret;
        }
        return ret;
    }

    private String readAmlSystemValue(String key) {
        boolean ret = false;
        Object mSysWriter = getSysWrite();
        if (mSysWriter == null) {
            return null;
        }
        ret = (Boolean) ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", ATTACH_FILE, "1");
        if (!ret) {
            return null;
        }
        ret = (Boolean) ReflectUtils.invokeMethod(mSysWriter, "writeSysfs", NAME_FILE, key);
        if (!ret) {
            return null;
        }
        return (String) ReflectUtils.invokeMethod(mSysWriter, "readSysfs", READ_FILE);
    }

}

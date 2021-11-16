package com.henggu.factorytest.common;

import android.util.Log;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CommProp {
    private static final String TAG = "CommProp";

    public static final String PROPDIR = ".hgfactory";
    private static final String PROPNAME = "com.henggu.factorytest.properties";

    private static final String TAG_SYSVER_PROP = "SystemVersionProp";
    private static final String TAG_SYSVERSION = "SystemVersion";
    private static final String TAG_FACTEST_NUM = "FactoryTestNum";
    //private static final String TAG_FACTORY_RESET = "FactoryReset";
    private static final String TAG_INITIAL_CHECK_SN = "InitialCheckSn";
    private static final String TAG_DIGIT_CHECK_SN = "DigitCheckSn";
    private static final String TAG_DIGIT_AGING_TIME = "DigitAgingTime";
    private static final String TAG_DIGIT_Iperf_RLT = "DigitIperfResult";

    private static final String TAG_SKIP_CHECK_LASTSTATION = "SkipCheckLastStation";
    private static final String TAG_USE_LOCAL_PROMPT = "UseLocalPrompt";

    private static final String TAG_ETH_PING_IP = "EthernetPingIp";
    private static final String TAG_IPERF_CMD = "IperfCmd";

    private static final String TAG_WIFIMAC_BURN_RULES = "WifiMacBurnRules";
    private static final String TAG_BTMAC_BURN_RULES = "BTMacBurnRules";

    private static final String TAG_FUNCION_TEST_ITEMS = "FunctionTestItems";
    private static final String TAG_MERGE_STATION = "MergeStation";

    public static final String TAG_IPERF_HF_CONFIG = "IperfHfConfig";
    public static final String TAG_IPERF_LF_CONFIG = "IperfLfConfig";

    public String mEthPingIp = null;

    public String mSysVerProp = "ro.build.version.incremental";
    public String mSysVer = null;
    //public boolean mFactoryReset = false;
    public int mFactoryTestNum = 5;
    public int mSnCheckDigit = 0;
    public String mSnCheckInitial = null;

    public String mWifiMacBurnRules = null;
    public String mBTMacBurnRules = null;
    public String mFunctionTestItems = null;
    public boolean mMergeStation = false;

    public int mAgingCheckDigit = 0;
    public int mIperfCheckDigit = 0;
    public boolean mSkipCheckLastStation = false;

    public boolean mUseLocalPrompt = true;


    public String mIperfCmd = null;

    private Properties prop = null;

    public CommProp() {
        initProp();
    }

    public List<String> getStorageDir() {
        File mounts = getFile("/proc/mounts");
        List<String> paths = new ArrayList<String>();

        if (mounts != null) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(mounts));
                try {
                    String text = null;
                    while ((text = reader.readLine()) != null) {
                        //Log.d(TAG, text);
                        if (text.startsWith("/dev/block/vold/")) {
                            String[] splits = text.split(" ");
                            //Log.d(TAG, "len= " + splits.length);
                            if (splits.length > 2) {
                                Log.d(TAG, splits[1]);
                                paths.add(splits[1]);
                            }
                        }
                    }
                } finally {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.w(TAG, "File</proc/mounts> is not exists");
        }
        return paths;
    }

    public File getFile(String path) {
        Log.d(TAG, "getFile: " + path);
        try {
            File file = new File(path);
            if (file.exists()) {
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Properties loadProperties(File file) {
        Properties properties = new Properties();

        try {
            FileInputStream input = new FileInputStream(file);
            properties.load(input);
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

    private void initProp() {
        List<String> paths = getStorageDir();
        int count = paths.size();
        for (int i = 0; i < count; i++) {
            String path = paths.get(i);
            File propFile = getFile(new String(path + "/" + PROPDIR + "/" + PROPNAME));
            if (propFile != null) {
                prop = loadProperties(propFile);
                if (prop != null) {
                    String sysVerProp = prop.getProperty(TAG_SYSVER_PROP, null);
                    Log.d(TAG, TAG_SYSVER_PROP + ": " + sysVerProp);
                    if (sysVerProp != null) {
                        mSysVerProp = sysVerProp.trim();
                    }

                    mSysVer = prop.getProperty(TAG_SYSVERSION, null);
                    mSysVer = mSysVer == null ? null : mSysVer.trim();
                    Log.d(TAG, TAG_SYSVERSION + ": " + mSysVer);

                    String numStr = prop.getProperty(TAG_FACTEST_NUM, null);
                    numStr = numStr == null ? null : numStr.trim();
                    Log.d(TAG, TAG_FACTEST_NUM + ": " + numStr);
                    if (numStr != null) {
                        try {
                            mFactoryTestNum = Integer.parseInt(numStr);
                            if (mFactoryTestNum < 5) {
                                mFactoryTestNum = 5;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    /*String facReset = prop.getProperty(TAG_FACTORY_RESET, null);
                    Log.d(TAG, TAG_FACTORY_RESET + ": " + facReset);
                    if ("true".equals(facReset)) {
                        mFactoryReset = true;
                    }*/

                    mSnCheckInitial = prop.getProperty(TAG_INITIAL_CHECK_SN, null);
                    mSnCheckInitial = mSnCheckInitial == null ? null : mSnCheckInitial.trim();
                    Log.d(TAG, TAG_INITIAL_CHECK_SN + ": " + mSnCheckInitial);

                    String checksnStr = prop.getProperty(TAG_DIGIT_CHECK_SN, null);
                    checksnStr = checksnStr == null ? null : checksnStr.trim();
                    Log.d(TAG, TAG_DIGIT_CHECK_SN + ": " + checksnStr);
                    if (checksnStr != null) {
                        try {
                            mSnCheckDigit = Integer.parseInt(checksnStr);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    String ageingtimeStr = prop.getProperty(TAG_DIGIT_AGING_TIME, null);
                    ageingtimeStr = ageingtimeStr == null ? null : ageingtimeStr.trim();
                    Log.d(TAG, TAG_DIGIT_AGING_TIME + ": " + ageingtimeStr);
                    if (ageingtimeStr != null) {
                        try {
                            mAgingCheckDigit = Integer.parseInt(ageingtimeStr);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    String iperfResultStr = prop.getProperty(TAG_DIGIT_Iperf_RLT, null);
                    iperfResultStr = iperfResultStr == null ? null : iperfResultStr.trim();
                    Log.d(TAG, TAG_DIGIT_Iperf_RLT + ": " + iperfResultStr);
                    if (iperfResultStr != null) {
                        try {
                            mIperfCheckDigit = Integer.parseInt(iperfResultStr);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    String checkLaststationStr = prop.getProperty(TAG_SKIP_CHECK_LASTSTATION, null);
                    checkLaststationStr = checkLaststationStr == null ? null : checkLaststationStr.trim();
                    Log.d(TAG, TAG_SKIP_CHECK_LASTSTATION + ": " + checkLaststationStr);
                    if ("true".equals(checkLaststationStr)) {
                        mSkipCheckLastStation = true;
                    } else if ("false".equals(checkLaststationStr)) {
                        mSkipCheckLastStation = false;
                    }

                    String useLocalPromptStr = prop.getProperty(TAG_USE_LOCAL_PROMPT, null);
                    useLocalPromptStr = useLocalPromptStr == null ? null : useLocalPromptStr.trim();
                    Log.d(TAG, TAG_USE_LOCAL_PROMPT + ": " + useLocalPromptStr);
                    if ("true".equals(useLocalPromptStr)) {
                        mUseLocalPrompt = true;
                    } else if ("false".equals(useLocalPromptStr)) {
                        mUseLocalPrompt = false;
                    }

                    String ethTestIp = prop.getProperty(TAG_ETH_PING_IP, numStr);
                    ethTestIp = ethTestIp == null ? null : ethTestIp.trim();
                    Log.i(TAG, TAG_ETH_PING_IP + ": " + ethTestIp);
                    if (ethTestIp != null) {
                        mEthPingIp = ethTestIp;
                    }

                    String iperfCmd = prop.getProperty(TAG_IPERF_CMD, null);
                    iperfCmd = iperfCmd == null ? null : iperfCmd.trim();
                    Log.i(TAG, TAG_IPERF_CMD + ": " + iperfCmd);
                    if (iperfCmd != null) {
                        mIperfCmd = iperfCmd;
                    }

                    String wifiMacBurnRules = prop.getProperty(TAG_WIFIMAC_BURN_RULES, null);
                    wifiMacBurnRules = wifiMacBurnRules == null ? null : wifiMacBurnRules.trim();
                    Log.i(TAG, TAG_WIFIMAC_BURN_RULES + ": " + wifiMacBurnRules);
                    if (wifiMacBurnRules != null) {
                        mWifiMacBurnRules = wifiMacBurnRules;
                    }

                    String bTMacBurnRules = prop.getProperty(TAG_BTMAC_BURN_RULES, null);
                    bTMacBurnRules = bTMacBurnRules == null ? null : bTMacBurnRules.trim();
                    Log.i(TAG, TAG_BTMAC_BURN_RULES + ": " + bTMacBurnRules);
                    if (bTMacBurnRules != null) {
                        mBTMacBurnRules = bTMacBurnRules;
                    }

                    String functionTestItems = prop.getProperty(TAG_FUNCION_TEST_ITEMS, null);
                    functionTestItems = functionTestItems == null ? null : functionTestItems.trim();
                    Log.i(TAG, TAG_FUNCION_TEST_ITEMS + ": " + functionTestItems);
                    if (functionTestItems != null) {
                        mFunctionTestItems = functionTestItems;
                    }

                    String mergerStationStr = prop.getProperty(TAG_MERGE_STATION, null);
                    mergerStationStr = mergerStationStr == null ? null : mergerStationStr.trim();
                    Log.d(TAG, TAG_MERGE_STATION + ": " + mergerStationStr);
                    if ("true".equals(mergerStationStr)) {
                        mMergeStation = true;
                    } else if ("false".equals(mergerStationStr)) {
                        mMergeStation = false;
                    }

                }
            }
        }
    }

    public WifiSimpleInfo parseConfigWifi(String tag) {
        Log.i(TAG, "Parsing wifi configuration ...");
        WifiSimpleInfo mWifiInfo = null;
        if (prop != null) {
            mWifiInfo = new WifiSimpleInfo();
            String apinfo = prop.getProperty(tag, null);
            if (apinfo != null) {
                String[] apstrs = apinfo.split("\\|");
                if (apstrs.length == 2) {
                    mWifiInfo.mSSID = apstrs[0];
                    mWifiInfo.mPWD = apstrs[1];
                    mWifiInfo.mMode = "dhcp";
                } else if (apstrs.length == 6) {
                    mWifiInfo = new WifiSimpleInfo();
                    mWifiInfo.mSSID = apstrs[0];
                    mWifiInfo.mPWD = apstrs[1];
                    mWifiInfo.mMode = "static";
                    mWifiInfo.mIP = apstrs[2];
                    mWifiInfo.mMask = apstrs[3];
                    mWifiInfo.mGW = apstrs[4];
                    mWifiInfo.mDNS = apstrs[5];
                } else {
                    Log.w(TAG, "Config wifi: " + apinfo + " is error!");
                    mWifiInfo = null;
                }
            }
        }

        return mWifiInfo;
    }
}

package com.henggu.factorytest.common;

public class Config {
    public static final String TAG = "HgFactorytest";
    public static final Boolean Debug = Boolean.valueOf(true);


    public static final String AGING_RECODE_FILE = "/data/local/aging-test";
    public static final String ETH_RECODE_FILE = "/data/local/eth-test";
    public static final String ETH_MAC_ADDR = "/sys/class/net/eth0/address";
    public static final String WIFI_MAC_ADDR = "/sys/class/net/wlan0/address";

    public static final String IPERF_RECODE_FILE = "/data/local/iperf-test";
    public static final String WIFI_TEST_SCANID = "/data/local/wifi_scanid";

    public static final int TAG_NO_TEST = 0;
    public static final int TAG_TEST_FAIL = 1;
    public static final int TAG_TEST_SUCC = 2;

    // data report station
    public static final String WIFI_THRO_STATION = "WIFI_THRO";
    public static final String FUNCTION_STATION = "Features_Test";
    public static final String UPGRADE_STATION = "Expand_1";
    public static final String PROOF_STATION = "Expand_2";
}

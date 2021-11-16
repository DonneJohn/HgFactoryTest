package com.henggu.factorytest;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import java.io.File;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.provider.Settings;

import static android.net.wifi.SupplicantState.DISCONNECTED;
import static org.apache.http.protocol.HTTP.UTF_8;

import org.json.JSONException;
import org.json.JSONObject;

import com.henggu.factorytest.utils.Common;
import com.henggu.factorytest.common.Config;

// added by zhangcc

public class FactoryReportReceiver extends BroadcastReceiver {
    private static final String TAG = "FactoryDataReport";

    private static final String SUBMIT_URL = "http://192.168.2.101:8888";
    private static final String CHECK_STATION_URL = "/DBATE/CheckStation";
    private static final String UPDATE_STATUS_URL = "/DBATE/UpdateStatus";

    private static final int MSG_REPORT = 0x01;
    private static final int MSG_FACRSET = 0xa0;
    private static final int MSG_NETERROR = 0xa1;

    private final String TMP_DATA_FILE = "/data/local/todo_factory_default.txt";

    private Context mContext;
    private String mReportSn = "";
    private static final String TYPE_CHECK_CURRENT_STATION = "CHECK_CURRENT_STATION";
    private static final String TYPE_CHECK_LAST_STATION = "CHECK_LAST_STATION";
    private static final String TYPE_UPDATE_STATUS = "UPDATE_STATUS";

    Handler myHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_REPORT) {
                ReportRequestData requeseData = (ReportRequestData) msg.obj;
                Log.i(TAG, "Resend DataReport for " + requeseData.url);

                postHttpReport(requeseData);
            } else if (msg.what == MSG_FACRSET) {
                boolean resetFac = Common.getSysUtilHandler().setResetFacFlag(true);
                Log.i(TAG, "resetFac flag : " + resetFac);
                if (!resetFac) return;
                File mResetFile = new File(TMP_DATA_FILE);
                try {
                    mResetFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!mResetFile.exists()) return;
                mContext.sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
                try {
                    RecoverySystem.rebootWipeUserData(mContext);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            } else if (msg.what == MSG_NETERROR) {
                Common.showCommonDialog(mContext, "提示", "网络不通请检查网线连接", R.drawable.ic_shutdown);
            }
        }
    };


    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;

        String action = intent.getAction();

        Log.i(TAG, "FactoryDataReport received broadcast.");
        if (action.equals("com.henggu.factorydatareport.CHECK_STATION")) {
            // for APK start-up DataReport.
            String bobsn = intent.getExtras().getString("bobsn");
            String preStation = intent.getExtras().getString("preStation");
            String currentStation = intent.getExtras().getString("currentStation");
            boolean isCurrentStation = intent.getExtras().getBoolean("currentStation");
            Log.i(TAG, "checkstation bobsn: " + bobsn + " preStation: " + preStation + " currentStation: " + currentStation);
            if (TextUtils.isEmpty(bobsn)) {
                bobsn = Common.getSysUtilHandler().readBobSN();
            }
            checkStationState(bobsn, preStation, currentStation, isCurrentStation);
        } else if (action.equals("com.henggu.factorydatareport.UPDATE_STATUS")) {
            String bobsn = intent.getExtras().getString("bobsn");
            String currentStation = intent.getExtras().getString("currentStation");
            String status = intent.getExtras().getString("status");
            Log.i(TAG, "update report station status bobsn: " + bobsn + " currentStation: " + currentStation + " status: " + status);
            if (TextUtils.isEmpty(bobsn)) {
                bobsn = Common.getSysUtilHandler().readBobSN();
            }
            reportFactoryData(bobsn, currentStation, status);
        }

        return;
    }


    private class MyTask extends AsyncTask<ReportRequestData, Integer, ReportResponseData> {

        protected ReportResponseData doInBackground(ReportRequestData... reportRequestData) {
            // Async post
            ReportRequestData requestData = reportRequestData[0];
            ReportResponseData responseData = null;
            String url_tmp = requestData.url;
            String content = requestData.requestContent;

            String result = "";
            BufferedReader reader = null;
            try {
                URL realurl = new URL(url_tmp);
                Log.i(TAG, "Ready to send DataReport to " + url_tmp + "?" + content);

                HttpURLConnection conn = null;

                conn = (HttpURLConnection) realurl.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);

                conn.setRequestMethod("POST");
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setUseCaches(false);
                conn.setRequestProperty("Connection", "Keep-Alive");
                //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Type", "application/json");

                conn.connect();
                //DataOutputStream流
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());

                //将要上传的内容写入流中
                out.writeBytes(content);
                //刷新、关闭
                out.flush();
                out.close();

                Log.i(TAG, "Complete sending DataReport, ret: " + conn.getResponseCode());
                if (conn.getResponseCode() == 200) {
                    reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    result = reader.readLine();
                }

                conn.disconnect();
            } catch (ConnectException e) {
                System.out.println("%%%% ConnectException when sending DataReport");
                // e.printStackTrace();
                // Try to send async DataReport
                //resendDataReport(requestData);
                myHandler.sendEmptyMessage(MSG_NETERROR);
            } catch (MalformedURLException e) {
                System.out.println("%%%% MalformedURLException when sending DataReport");
                // e.printStackTrace();
                // Try to send async DataReport
                resendDataReport(requestData);
            } catch (UnknownHostException e) {
                System.out.println("%%%% UnknownHostException when sending DataReport");
                // e.printStackTrace();
                // Try to send async DataReport
                resendDataReport(requestData);
            } catch (Exception e) {
                System.out.println("%%%% Unknown Exception when sending DataReport");
                // e.printStackTrace();
                resendDataReport(requestData);
            }
            responseData = new ReportResponseData(url_tmp, requestData.bobsn, requestData.preStation, requestData.currentStation, requestData.status, requestData.reportType, result);
            return responseData;
        }

        protected void onPostExecute(ReportResponseData responseData) {
            Log.i(TAG, "onPostExecute result is: " + responseData.responseContent);
            if (!TextUtils.isEmpty(responseData.responseContent)) {
                parseReturnJson(responseData);
            }
        }
    }

    private void parseReturnJson(ReportResponseData responseData) {
        try {
            String json = responseData.responseContent;
            JSONObject jsonObj = new JSONObject(json);
            boolean isSuccess = jsonObj.getBoolean("IsSuccess");
            String message = jsonObj.getString("Message");
            Log.i(TAG, "receive service isSuccess: " + isSuccess + " message: " + message);
            if (isSuccess) {
                if (responseData.reportType.equals(TYPE_UPDATE_STATUS)) {
                    checkStationState(responseData.bobsn, responseData.preStation, responseData.currentStation, true);
                } else if (responseData.reportType.equals(TYPE_CHECK_CURRENT_STATION)) {
                    String stationStr = "";
                    if (Config.FUNCTION_STATION.equals(responseData.currentStation)) {
                        stationStr = "功能站";
                    } else if (Config.UPGRADE_STATION.equals(responseData.currentStation)) {
                        stationStr = "升级站";
                    } else if (Config.PROOF_STATION.equals(responseData.currentStation)) {
                        stationStr = "校验站";
                    }
                    Common.showCommonDialog(mContext, "通过", stationStr + "测试通过并上报成功！", R.drawable.ic_launcher);
                    if (responseData.currentStation.equals(Config.PROOF_STATION)) {
                         Message msg = Message.obtain();
                         msg.what = MSG_FACRSET;
                         myHandler.sendMessageDelayed(msg, 1000);
                    }
                }
            } else {
                if (responseData.reportType.equals(TYPE_CHECK_LAST_STATION)) {
                    Common.showCommonDialog(mContext, "提示", message, R.drawable.ic_shutdown);
                } else if (responseData.reportType.equals(TYPE_UPDATE_STATUS)) {
                    Common.showCommonDialog(mContext, "本站测试失败", message, R.drawable.ic_shutdown);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void resendDataReport(ReportRequestData requestData) {
        Message msg = Message.obtain();
        msg.what = MSG_REPORT;
        msg.obj = requestData;
        myHandler.sendMessageDelayed(msg, 10000);
    }


    private void postHttpReport(ReportRequestData requestData) {
        MyTask mTask = new MyTask();

        mTask.execute(requestData);
    }


    private String parseReportData(LinkedHashMap<String, String> hm) {
        String content = "";

        //要上传的参数
        Iterator it = hm.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            content = content + key + "=" + hm.get(key) + "&";
        }

        Log.i(TAG, "DataReport content: " + content);
        return content;
    }


    //检查站点情况
    private void checkStationState(String bobsn, String preStation, String currentStation, boolean isCurrentStation) {
        Log.i(TAG, "checkStationState: bobsn" + bobsn + " preStation: " + preStation + " currentStation : " + currentStation);
        if (TextUtils.isEmpty(bobsn)) {
            bobsn = Common.getSysUtilHandler().readBobSN();
        }

        /*LinkedHashMap<String, String> hm = new LinkedHashMap();
        // factory sn means bobsn
        hm.put("Dut_Sn", bobsn);
        hm.put("Pre_Station", preStation);
        hm.put("Cur_Station", currentStation);
        String content = parseReportData(hm);*/
		JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.accumulate("Dut_Sn", bobsn);
            jsonObj.accumulate("Pre_Station", preStation);
            jsonObj.accumulate("Cur_Station", currentStation);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = SUBMIT_URL + CHECK_STATION_URL;
        String checkType = isCurrentStation? TYPE_CHECK_CURRENT_STATION: TYPE_CHECK_LAST_STATION;
        ReportRequestData requestData = new ReportRequestData(url, bobsn, preStation, currentStation, "", checkType, jsonObj.toString());
        postHttpReport(requestData);
    }

    // 上报当前站点信息
    private void reportFactoryData(String bobsn, String currentStation, String status) {
        Log.i(TAG, "reportFactoryData: bobsn" + bobsn + " currentStation: " + currentStation + " status : " + status);
        /*LinkedHashMap<String, String> hm = new LinkedHashMap();
        // factory sn means bobsn
        hm.put("Dut_Sn", bobsn);
        hm.put("Cur_Station", currentStation);
        hm.put("Status", status);
        String content = parseReportData(hm);*/
		JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.accumulate("Dut_Sn", bobsn);
            jsonObj.accumulate("Cur_Station", currentStation);
            jsonObj.accumulate("Status", status);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = SUBMIT_URL + UPDATE_STATUS_URL;
        String reportType = TYPE_UPDATE_STATUS;
        ReportRequestData requestData = new ReportRequestData(url, bobsn, "", currentStation, status, reportType, jsonObj.toString());
        postHttpReport(requestData);
    }

    private class ReportRequestData {
        public String url;
        public String bobsn;
        public String preStation;
        public String currentStation;
        public String status;
        public String reportType;
        public String requestContent;

        public ReportRequestData(String mUrl, String mBobsn, String mPreStation, String mCurrentStation, String mStatus,
                String mReportType, String mRequestContent) {
            url = mUrl;
            bobsn = mBobsn;
            preStation = mPreStation;
            currentStation = mCurrentStation;
            status = mStatus;
            reportType = mReportType;
            requestContent = mRequestContent;
        }
    }

    private class ReportResponseData {
        public String url;
        public String bobsn;
        public String preStation;
        public String currentStation;
        public String status;
        public String reportType;
        public String responseContent;

        public ReportResponseData(String mUrl, String mBobsn, String mPreStation, String mCurrentStation, String mStatus,
                String mReportType, String mResponseContent) {
            url = mUrl;
            bobsn = mBobsn;
            preStation = mPreStation;
            currentStation = mCurrentStation;
            status = mStatus;
            reportType = mReportType;
            responseContent = mResponseContent;
        }
    }
}

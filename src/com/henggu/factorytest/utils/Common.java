package com.henggu.factorytest.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.view.Window;
import android.view.WindowManager;

import com.henggu.factorytest.common.Config;
import com.henggu.factorytest.R;

public class Common {

    public static SysUtils getSysUtilHandler() {
        SysUtils sysUtil = null;
        String chiptype = SystemProperties.get("ro.product.chiptype", "unknown");
        LogUtils.debug(Config.TAG, "chiptype: " + chiptype);
        if (chiptype.startsWith("Hi")) {
            sysUtil = new SysUtils_hisilicon();
        } else if (chiptype.startsWith("Aml")) {
            sysUtil = new SysUtils_amlogic();
        } else {
            sysUtil = new SysUtils();
        }
        return sysUtil;
    }

    public static String callCmd(String cmd, String filter) {
        String result = "";
        String line = "";
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getFileValue(String filePath) {
        String value = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        BufferedReader bufferedReader = null;
        bufferedReader = new BufferedReader(fileReader);
        try {
            value = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return value;
    }

    public static boolean writeFileValue(String filepath, String value) {
        Boolean bool = false;

        FileOutputStream fos = null;
        PrintWriter pw = null;
        try {
            File file = new File(filepath);
            StringBuffer buffer = new StringBuffer();

            buffer.append(value);

            fos = new FileOutputStream(file);
            pw = new PrintWriter(fos);
            pw.write(buffer.toString().toCharArray());
            pw.flush();
            bool = true;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            try {
                if (pw != null) {
                    pw.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bool;
    }

    public static boolean isHisiV300() {
        String chiptype = SystemProperties.get("ro.product.chiptype", "unknown");
        if (chiptype.equals("Hi3798MV100")) return false;

        return true;
    }

    public static void unzip(InputStream paramInputStream, String destDir) {
//        File dir = new File(destDir);
//        // create output directory if it doesn't exist
//        if(!dir.exists()) dir.mkdirs();
//        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
//            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(paramInputStream);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to " + newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
//            fis.close();
            paramInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                if (intf.getName().toLowerCase().equals("eth0")) {
                    Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses();
                    while (enumIpAddr.hasMoreElements()) {
                        InetAddress inetAddress = (InetAddress) enumIpAddr
                                .nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && !inetAddress.isLinkLocalAddress()) {
                            return inetAddress.getHostAddress().toString();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            LogUtils.debug(Config.TAG, "IpAddress ex:" + ex.toString());
        }
        return null;
    }

    public static void sendDataReportBroadcast(String bobsn, String currentStation, boolean status, Context mContext) {
        Intent mIntent = new Intent("com.henggu.factorydatareport.UPDATE_STATUS");
        mIntent.putExtra("bobsn", bobsn);
        mIntent.putExtra("currentStation", currentStation);
        String statusStr = status ? "PASS" : "FAIL";
        mIntent.putExtra("status", statusStr);
        mContext.sendBroadcast(mIntent);
    }

    public static void sendCheckStationBroadcast(String bobsn, String preStation, String currentStation, boolean isCurrentStation, Context mContext) {
        Intent mIntent = new Intent("com.henggu.factorydatareport.CHECK_STATION");
        mIntent.putExtra("bobsn", bobsn);
        mIntent.putExtra("preStation", preStation);
        mIntent.putExtra("currentStation", currentStation);
        mIntent.putExtra("isCurrentStation", isCurrentStation);
        mContext.sendBroadcast(mIntent);
    }

    public static void showCommonDialog (Context context, String tipTitle, String tipMsg, int icon) {
        AlertDialog aDialog = new AlertDialog.Builder(context)
                .setTitle(tipTitle)
                .setMessage(tipMsg)
                .setIcon(icon)
                .create();

        Window window = aDialog.getWindow();
        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        aDialog.setCancelable(false);
        aDialog.show();
        return;
    }
}

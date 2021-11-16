package com.henggu.factorytest;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.henggu.factorytest.utils.SocketThread;
import com.henggu.factorytest.utils.WifiConnect;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketActivity extends Activity {
    private static final String TAG = "SocketTest";
    private ThreadHandler mThreadHandler = null;
    private TextView tvIp;
    private WifiConnect wifiConnect;

    public class ThreadHandler extends Thread {
        private ServerSocket mServerSocket = null;

        public ThreadHandler() {
            setName("ThreadHandler");
        }

        public void run() {
            ScoketServer();
        }

        public void stopThread() {
            Log.i(SocketActivity.TAG, "--------------->ServerSocket stop");
            try {
                if (this.mServerSocket != null) {
                    this.mServerSocket.close();
                }
                this.mServerSocket = null;
            } catch (IOException e) {
                Log.i(SocketActivity.TAG, "--------------->ServerSocket close IOException");
                e.printStackTrace();
            }
        }

        public void ScoketServer() {
            try {
                this.mServerSocket = new ServerSocket(8000);
            } catch (IOException e) {
                Log.i(SocketActivity.TAG, "--------------->ServerSocket IOException");
                this.mServerSocket = null;
                e.printStackTrace();
            }
            Log.i(SocketActivity.TAG, "--------------->ServerSocket");
            while (this.mServerSocket != null) {
                Log.i(SocketActivity.TAG, "--------------->connected 1");
                Socket socket = null;
                try {
                    socket = this.mServerSocket.accept();
                } catch (IOException e2) {
                    Log.i(SocketActivity.TAG, "--------------->Socket IOException");
                    e2.printStackTrace();
                }
                Log.i(SocketActivity.TAG, "--------------->connected 2");
                System.out.println(socket.getInetAddress().getHostAddress() + "连接进入");
                Log.i(SocketActivity.TAG, "--------------->connected 3");
                try {
                    new SocketThread(socket).start();
                } catch (IOException e22) {
                    Log.i(SocketActivity.TAG, "--------------->SocketThread IOException");
                    e22.printStackTrace();
                }
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        this.tvIp = (TextView) findViewById(R.id.textView2);
        this.wifiConnect = new WifiConnect((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE));
        if (this.wifiConnect.OpenWifi()) {
            this.tvIp.setText("Ip 地址：" + this.wifiConnect.getIPAddress());
        }
        if (this.mThreadHandler == null) {
            this.mThreadHandler = new ThreadHandler();
            this.mThreadHandler.start();
        }
        Button button = (Button) findViewById(R.id.button);
        button.requestFocus();
        button.requestFocusFromTouch();
//        getWifiInfo();
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SocketActivity.this.getWifiInfo();
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onPause() {
        super.onPause();
        if (this.mThreadHandler != null) {
            this.mThreadHandler.stopThread();
            this.mThreadHandler = null;
        }
    }

    protected void onStop() {
        super.onStop();
    }

    private void getWifiInfo() {
        this.wifiConnect = new WifiConnect((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE));
        if (this.wifiConnect.OpenWifi()) {
//            boolean ifWifi = this.wifiConnect.connect("CMCC_henggu", "henggu123456");
            boolean ifWifi = this.wifiConnect.connect("scty", "");
            Log.i(TAG, "wifiConnect scty: " + ifWifi);
            if (ifWifi) {
                if (this.wifiConnect.getIPAddress().equals("0.0.0.0")) {
//                    SystemClock.sleep(3000);
//                    getWifiInfo();
                    this.tvIp.setText("Ip 地址：" + this.wifiConnect.getIPAddress());
                } else {
                    this.tvIp.setText("Ip 地址：" + this.wifiConnect.getIPAddress());
                    if (this.mThreadHandler == null) {
                        this.mThreadHandler = new ThreadHandler();
                        this.mThreadHandler.start();
                    }
                }
                Log.i(TAG, "wifiConnect ifWifi" + this.wifiConnect.getIPAddress());
                return;
            }
            this.tvIp.setText("scty WIFI未连接，请检查");
            return;
        }
        this.tvIp.setText("WIFI连接未打开");
    }
}
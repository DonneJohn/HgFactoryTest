package com.henggu.factorytest.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dell on 2016/9/13.
 */
public class SocketThread extends Thread {

    private static final String TAG = "SocketReceiver";
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private OutputStream output;
    private InputStream input;
    private Socket curSocket;

    public SocketThread(Socket socket) throws IOException {
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket
                .getInputStream()));
        this.printWriter = new PrintWriter(socket.getOutputStream());
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        this.curSocket = socket;
    }

    @Override
    public void run() {
        String line = null;
        String[] sections;
        String method = null;
        String str = null;
        byte[] buf = new byte[100 * 1024];


        while (true) {
            try {
                line = bufferedReader.readLine();
                System.out.println(line);

                sections = line.split(" ");
                method = sections[0];
                String prefix = sections[1].substring(0, 9);  // /anysize/ is 9 bytes


                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E a");
                long times = System.currentTimeMillis();

                Date date = new Date(times);
                String timeAll = sdf.format(date);
                System.out.println(timeAll);

                Log.d(TAG, "method: " + method);
                if ("GET".equalsIgnoreCase(method)) {
                    Log.d(TAG, "prefix:4rrrrrrrrrrrrrrrrrrrrrrrrrrr " + prefix);
                    if ("/anysize/".equalsIgnoreCase(prefix)) {
                        printWriter.write("HTTP/1.1 200 OK\r\n");
                        printWriter.write("Connection: Close\r\n");
                        printWriter.write("Content-Type: text/plain\r\n");
                        printWriter.write("Content-Length: ");
                        printWriter.write(sections[1].substring(9));
                        printWriter.write("\r\n\r\n");

                        for (int i =0 ; i< Integer.MAX_VALUE; i ++){
                            output.write(buf);
                        }
                        printWriter.flush();
                        printWriter.close();
                        output.close();
                        this.curSocket.close();
                        Log.d(TAG, "------over1-------");
                    } else {
                        printWriter.write("HTTP/1.1 200 OK\r\n");
                        printWriter.write("Connection: Close\r\n");
                        printWriter.write("Content-Type: text/plain\r\n");
                        printWriter.write("\r\n");
                        printWriter.write("This is a java process for wifi throutput test!\r\n");
                        printWriter.write("Author: Chen Xi\r\n");
                        printWriter.write(timeAll);


                        printWriter.flush();
                        printWriter.close();
                        output.close();
                        this.curSocket.close();
                        Log.d(TAG, "------over2-------");
                    }
                } else if ("POST".equalsIgnoreCase(method)) {
                    while (input.read(buf) != -1) {
                    }
                }
                break;
            } catch (IOException e) {

            }
        }
    }

}


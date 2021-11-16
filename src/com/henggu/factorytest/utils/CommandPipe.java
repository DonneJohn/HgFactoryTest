package com.henggu.factorytest.utils;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by steven.gao on 2018/4/15.
 */

public final class CommandPipe {
    private static final String TAG = "commandpipe";
    private static final boolean DEBUG = false;

    int buflen = 0;
    InputStream mIn;
    OutputStream mOut;
    LocalSocket mSocket;
    byte buf[] = new byte[1024];

    private boolean connect() {
        if (mSocket != null) {
            return true;
        }

        if (DEBUG) Log.i(TAG, "connecting...");
        try {
            mSocket = new LocalSocket();

            LocalSocketAddress address = new LocalSocketAddress("commandpipe",
                    LocalSocketAddress.Namespace.RESERVED);

            mSocket.connect(address);

            mIn = mSocket.getInputStream();
            mOut = mSocket.getOutputStream();
        } catch (IOException ex) {
            disconnect();
            return false;
        }
        return true;
    }

    private void disconnect() {
        if (DEBUG) Log.i(TAG, "disconnecting...");
        try {
            if (mSocket != null)
                mSocket.close();
        } catch (IOException ex) {
        }
        try {
            if (mIn != null)
                mIn.close();
        } catch (IOException ex) {
        }
        try {
            if (mOut != null)
                mOut.close();
        } catch (IOException ex) {
        }
        mSocket = null;
        mIn = null;
        mOut = null;
    }

    private boolean readBytes(byte buffer[], int len) {
        int off = 0, count;
        if (len < 0)
            return false;
        while (off != len) {
            try {
                count = mIn.read(buffer, off, len - off);
                if (count <= 0) {
                    Log.e(TAG, "read error " + count);
                    break;
                }
                off += count;
            } catch (IOException ex) {
                Log.e(TAG, "read exception");
                break;
            }
        }
        if (DEBUG) Log.i(TAG, "read " + len + " bytes");
        if (off == len)
            return true;
        disconnect();
        return false;
    }

    private boolean readReply() {
        int len;
        buflen = 0;
        if (!readBytes(buf, 2))
            return false;
        len = (((int) buf[0]) & 0xff) | ((((int) buf[1]) & 0xff) << 8);
        if (len > 1024) {
            Log.e(TAG, "invalid reply length (" + len + ")");
            disconnect();
            return false;
        }
        if (len < 1 || !readBytes(buf, len))
            return false;
        buflen = len;
        return true;
    }

    private boolean writeCommand(String _cmd) {
        byte[] cmd = _cmd.getBytes();
        int len = cmd.length;

        if ((len < 1) || (len > 1024))
            return false;

        buf[0] = (byte) (len & 0xff);
        buf[1] = (byte) ((len >> 8) & 0xff);
        try {
            mOut.write(buf, 0, 2);
            mOut.write(cmd, 0, len);
        } catch (IOException ex) {
            Log.e(TAG, "write error");
            disconnect();
            return false;
        }
        return true;
    }

    private synchronized String transaction(String cmd) {
        if (!connect()) {
            Log.e(TAG, "connection failed");
            return "-1";
        }

        if (!writeCommand(cmd)) {
            Log.e(TAG, "write command failed? reconnect!");
            if (!connect() || !writeCommand(cmd)) {
                return "-1";
            }
        }
        if (DEBUG) Log.i(TAG, "sent: '" + cmd + "'");

        if (readReply()) {
            String s = new String(buf, 0, buflen);
            if (DEBUG) Log.i(TAG, "recv: '" + s + "'");

            disconnect();
            return s;
        }

        if (DEBUG) Log.i(TAG, "fail");
        return "-1";
    }

    // command: rootcmd <shell-command>
    public String execute(String cmd) {
        String ret = transaction(cmd);

        return ret;
    }
}

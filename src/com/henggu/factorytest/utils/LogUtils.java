package com.henggu.factorytest.utils;

import android.content.Context;
import android.util.Log;

import com.henggu.factorytest.common.Config;


public class LogUtils {
    public static void debug(String msg) {
        if (Config.Debug.booleanValue()) {
            StackTraceElement[] elements = new Throwable().getStackTrace();
            String[] calssStrings = elements[1].getClassName().split("\\.");
            String callerMethodName = elements[1].getMethodName();
            Log.d("HgFactorytest->" + calssStrings[calssStrings.length - 1], "[" + callerMethodName + ":" + elements[1].getLineNumber() + "]->" + "xxxxxxxx" + msg);
        }
    }

    public static void debug(Context context, String msg) {
        if (Config.Debug.booleanValue()) {
            Log.d(Config.TAG, context.getClass().getSimpleName() + " [msg]" + msg);
        }
    }

    public static void debug(String tag, String msg) {
        if (Config.Debug.booleanValue()) {
            Log.d(tag, msg);
        }
    }

    public static void debug(Class clazz, String msg) {
        if (Config.Debug.booleanValue()) {
            Log.d(clazz.getName().toString(), msg);
        }
    }

    public static void debug() {
        if (Config.Debug.booleanValue()) {
            StackTraceElement[] elements = new Throwable().getStackTrace();
            String[] calssStrings = elements[1].getClassName().split("\\.");
            String callerMethodName = elements[1].getMethodName();
            Log.d("HgFactorytest->" + calssStrings[calssStrings.length - 1], "[" + callerMethodName + ":" + elements[1].getLineNumber() + "]->");
        }
    }

    public static void debug(int i) {
        if (Config.Debug.booleanValue()) {
            StackTraceElement[] elements = new Throwable().getStackTrace();
            String[] calssStrings = elements[1].getClassName().split("\\.");
            String callerMethodName = elements[1].getMethodName();
            Log.d("HgFactorytest->" + calssStrings[calssStrings.length - 1], "[" + callerMethodName + ":" + elements[1].getLineNumber() + "]->" + i);
        }
    }

    public static void debug(Boolean b) {
        if (Config.Debug.booleanValue()) {
            StackTraceElement[] elements = new Throwable().getStackTrace();
            String[] calssStrings = elements[1].getClassName().split("\\.");
            String callerMethodName = elements[1].getMethodName();
            Log.d("HgFactorytest->" + calssStrings[calssStrings.length - 1], "[" + callerMethodName + ":" + elements[1].getLineNumber() + "]->" + "xxxxxxxx" + b);
        }
    }
}

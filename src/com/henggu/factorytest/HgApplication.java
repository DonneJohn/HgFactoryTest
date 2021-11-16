package com.henggu.factorytest;

import android.app.Application;
import android.content.Context;

public class HgApplication extends Application {

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }
}

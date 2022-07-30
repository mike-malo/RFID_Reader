package com.hcuhf;

import android.content.Context;

import com.kongzue.baseframework.BaseApp;


public class MyApplication extends BaseApp<MyApplication> {
    private static Context mContext;
    @Override
    public void init() {
        mContext = this;
    }
    public static Context getContext() {
        return mContext;
    }
}

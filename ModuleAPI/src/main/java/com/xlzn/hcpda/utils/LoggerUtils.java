package com.xlzn.hcpda.utils;

import android.util.Log;

public class LoggerUtils {

    private static String _TAG="CHLOG";

    private static boolean debugFlag=true;
    public static void d(String TAG,String data){
        if(debugFlag)Log.d(_TAG,TAG+"==>data="+data);
    }
    public static void d(String TAG,byte[] data){
        if(debugFlag)Log.d(_TAG,TAG+"==>data="+DataConverter.bytesToHex(data));
    }
    public static boolean isDebug(){
        return debugFlag;
    }
}

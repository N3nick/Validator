package com.google.mlkit.codelab.translate.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private static PrefManager mInstance;
    private static Context mCtx;
    private static final String SHARED_PREF_NAME = "mypref";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String URL_KEY = "Url";
    private static final String DEVICE_ID_KEY = "DeviceId";

    private PrefManager(Context context) {
        mCtx = context;
        sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized PrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PrefManager(context);
        }
        return mInstance;
    }

    public boolean isFirstTimeLaunch() {
        return sharedPreferences.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }
    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
        setSharedPreferences();
    }

    public void setSharedPreferences(){
        editor.putString(URL_KEY, "https://demo.ticketano.com/ticket-boarding");
        editor.putString(DEVICE_ID_KEY, "1aac75011bf30e06fa9e06c973a28234");
        editor.commit();
    }

    public void setDeviceId(String deviceId){
        editor.putString(DEVICE_ID_KEY, deviceId);
        editor.commit();
    }

    public String getDeviceId(){
        return  sharedPreferences.getString(DEVICE_ID_KEY, "1aac75011bf30e06fa9e06c973a28234");
    }

    public String getUrlKey(){
        return sharedPreferences.getString(URL_KEY, "https://demo.ticketano.com/ticket-boarding");
    }

    public void setUrlKey(String url){
        editor.putString(URL_KEY, "https://demo.ticketano.com/ticket-boarding");
        editor.commit();
    }

  /*  private fun saveSharedPrefs() {
        val sharedPreferences : SharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply{
            putString("URL_KEY", "https://demo.ticketano.com/ticket-boarding")
            putString("DEVICE_KEY", "1aac75011bf30e06fa9e06c973a28234")
        }.apply()
    }*/


}

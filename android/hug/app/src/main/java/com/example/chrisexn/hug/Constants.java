package com.example.chrisexn.hug;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by chrisg on 11/12/16.
 */

public class Constants {
    public static final String WEB_URL = "http://qhackserver-dev.us-west-2.elasticbeanstalk.com";


    public static void updateAccount(Context activity, String token){
        //Write to shared memory
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(activity.getString(R.string.session_id), token);
        editor.apply();
    }

    public static String getToken(Context activity){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        return sharedPref.getString(activity.getString(R.string.session_id),"no");
    }
}

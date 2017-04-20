package com.aim.graderecorder2.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.aim.graderecorder2.Constants;

/**
 * Created by Matt Boutell on 10/2/2015.
 */
public class SharedPreferencesUtils {
    public static String getCurrentUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Activity.MODE_PRIVATE);
        return prefs.getString(Constants.UID_KEY, "");
    }

    public static void setCurrentUser(Context context, String uid) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.UID_KEY, uid);
        editor.commit();
    }

    public static void removeCurrentUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Constants.UID_KEY);
        editor.apply();
    }

    public static String getCurrentCourseKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Activity.MODE_PRIVATE);
        return prefs.getString(Constants.COURSE_KEY, "");
    }

    public static void setCurrentCourseKey(Context context, String courseKey) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.COURSE_KEY, courseKey);
        editor.commit();
    }

    public static void removeCurrentCourseKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Constants.COURSE_KEY);
        editor.apply();
    }
}

package com.example.apptracker;

import android.graphics.drawable.Drawable;

import java.util.concurrent.TimeUnit;

public class AppInfo {

    private String appName, time;
    private Drawable icon;
    private long millis;

    public long getMillis() {
        return millis;
    }

    public AppInfo(String appName, long millis, Drawable icon) {
        this.appName=appName;
        this.millis=millis;
        this.icon=icon;
        this.time = String.format("%02d h %02d m", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}

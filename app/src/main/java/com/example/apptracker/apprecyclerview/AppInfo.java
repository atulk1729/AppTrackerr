package com.example.apptracker.apprecyclerview;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class AppInfo implements Parcelable {

    private String appName, time, packageName;
    private Drawable icon;
    public long millis;
    public int launchCount;

    protected AppInfo(Parcel in) {
        appName = in.readString();
        time = in.readString();
        millis = in.readLong();
        packageName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeString(time);
        dest.writeLong(millis);
        dest.writeString(packageName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

    public long getMillis() {
        return millis;
    }

    public AppInfo(String packageName, String appName, long millis, Drawable icon) {
        this.packageName = packageName;
        this.appName=appName;
        this.millis=millis;
        this.icon=icon;
        long hour = millis/3600000;
        long min = (millis/60000)%60;
        if(hour==0L) this.time = min + " m";
        else this.time = hour + " h " + min + " m";
    }

    public AppInfo(String packageName, String appName, Drawable icon) {
        this.packageName = packageName;
        this.appName=appName;
        this.icon=icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTime() {
        long hour = millis/3600000;
        long min = (millis/60000)%60;
        if(hour==0L) time = min + " m";
        else time = hour + " h " + min + " m";
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

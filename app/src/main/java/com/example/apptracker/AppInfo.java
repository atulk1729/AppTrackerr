package com.example.apptracker;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

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
        this.time = String.format("%02d h %02d m", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
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
        time = String.format("%02d h %02d m", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
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

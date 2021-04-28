package com.example.apptracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(checkForPermission(this)) checkForPermission(this);
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        long start = calendar.getTimeInMillis();
        long end = System.currentTimeMillis();
        Map<String, UsageStats> stats = usageStatsManager.queryAndAggregateUsageStats(start, end);


        ArrayList<AppInfo> appInfos = new ArrayList<>();
        for(Map.Entry<String, UsageStats> entry: stats.entrySet()) {
            UsageStats us = entry.getValue();
            AppInfo aI = new AppInfo(getAppLable(this,us.getPackageName()),us.getTotalTimeInForeground(),getIcon(this,us.getPackageName()));
            appInfos.add(aI);
        }

        Collections.sort(appInfos, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                if(o1.getMillis()<o2.getMillis())
                    return 1;
                else if(o1.getMillis()==o2.getMillis())
                    return 0;
                else return -1;
            }
        });

        while(appInfos.size()>20) {
            appInfos.remove(appInfos.size()-1);
        }

        RecyclerView recyclerView = findViewById(R.id.appListRecView);
        AppListAdapter adapter = new AppListAdapter(appInfos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private boolean checkForPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        if(!granted) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
        return granted;
    }

    public String getAppLable(Context context, String packageName) {
        PackageManager mPm = context.getPackageManager();
        String label = packageName;
        try {
            ApplicationInfo appInfo = mPm.getApplicationInfo(packageName, 0);
            label = mPm.getApplicationLabel(appInfo).toString();
        } catch (Exception e) {
            // This package may be gone.
        }
        return label;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Drawable getIcon(Context context, String packageName) {
        PackageManager mPm = context.getPackageManager();
        Drawable icon = null;
        try {
            ApplicationInfo appInfo = mPm.getApplicationInfo(packageName, 0);
            icon = mPm.getApplicationIcon(packageName);
        } catch (Exception e) {
            // This package may be gone.
        }
        return icon;
    }
}
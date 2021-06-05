package com.example.apptracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469;

    private MaterialButton limitButton;
    protected ArrayList<AppInfo> appInfos = new ArrayList<AppInfo>();
    private PieChart pieChart;
    private BackgroundService backgroundService;
    private Intent backgroundServiceIntent;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SYSTEM_EXIT_WINDOW Permission to get back to home screen from background service (For android 10 and higher)
        checkPermission();


        //Permission for UsageStats Manager
        if(checkForPermission(this)) checkForPermission(this);
        getUsageStatistics();

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

        pieChart = findViewById(R.id.main_pieChart);
        RecyclerView recyclerView = findViewById(R.id.appListRecView);
        AppListAdapter adapter = new AppListAdapter(appInfos, 5);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        //Background service to detect apps from background(When AppTracker is closed)
        backgroundService = new BackgroundService();
        backgroundServiceIntent = new Intent(getApplicationContext(),backgroundService.getClass());
        if (!isMyServiceRunning(backgroundService.getClass())) {
            startService(backgroundServiceIntent);
        }

        setupPieChart();
        loadPieChartData();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(0);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("App Usage Stats");
        pieChart.setCenterTextSize(24);
        pieChart.getDescription().setEnabled(false);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(true);
    }

    private void loadPieChartData() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        long others = 0;
        int count = 0;
        for(AppInfo x : appInfos) {
            if( count < 5 ) {
                entries.add(new PieEntry(x.getMillis(), x.getAppName()));
                count++;
            }
            else others += x.getMillis();
        }
        entries.add(new PieEntry(others, "others"));

        ArrayList<Integer> colors = new ArrayList<>();
        for( int color: ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }
        for( int color: ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "Apps");
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // You don't have permission
                checkPermission();
            } else {

            }

        }


        //Background service to detect apps from background(When AppTracker is closed)
        Intent backgroundService = new Intent(getApplicationContext(),BackgroundService.class);
        startService(backgroundService);
    }


    public void setLimitButton( View v ) {
        Intent intent = new Intent(this, AppSelectorActivity.class);
        intent.putExtra("AppInfoList", appInfos);
        startActivity(intent);
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

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void getUsageStatistics() {

        UsageEvents.Event currentEvent;
        List<UsageEvents.Event> allEvents = new ArrayList<>();
        HashMap<String, AppInfo> map = new HashMap <String, AppInfo> ();
        HashMap<String, Long> flag = new HashMap<String, Long>();

        long currTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();

        UsageStatsManager mUsageStatsManager =  (UsageStatsManager)
                this.getSystemService(Context.USAGE_STATS_SERVICE);

        assert mUsageStatsManager != null;
        UsageEvents usageEvents = mUsageStatsManager.queryEvents(startTime, currTime);

        //capturing all events in a array to compare with next element
        while (usageEvents.hasNextEvent()) {
            currentEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(currentEvent);
            if (currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                    currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                allEvents.add(currentEvent);
                String key = currentEvent.getPackageName();
                // taking it into a collection to access by package name
                if (map.get(key)==null) {
                    map.put(key, new AppInfo(key, getAppLable(this, key), getIcon(this, key)));
                    flag.put(key,-1L);
                }
            }
        }

        //iterating through the arraylist
        for (int i=0;i<allEvents.size();i++){
            UsageEvents.Event E = allEvents.get(i);

            if(E.getEventType()==1) {
                flag.put(E.getPackageName(),E.getTimeStamp());
            }

            if(E.getEventType()==2) {
                long lasttime = flag.get(E.getPackageName());
                long diff = E.getTimeStamp() - lasttime;
                if(lasttime==-1L) diff = E.getTimeStamp() - startTime;
                map.get(E.getPackageName()).millis+=diff;
                flag.put(E.getPackageName(),E.getTimeStamp());
            }
        }
        //transferred final data into modal class object
        appInfos = new ArrayList<>(map.values());

    }

    protected void onDestroy() {
        Toast.makeText(this, "service stopped by main", Toast.LENGTH_LONG).show();
        stopService(backgroundServiceIntent);
        super.onDestroy();
    }
}
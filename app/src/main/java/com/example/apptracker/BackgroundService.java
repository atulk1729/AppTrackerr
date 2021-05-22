package com.example.apptracker;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.*;
import android.os.*;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


// This class checks for time limit of app on foreground and blocks them
public class BackgroundService extends Service {

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    public SharedPreferences sharedpreferences = null;
    public String MyPREFERENCES = "AppInfos";
    public ArrayList<AppInfo> appInfos = new ArrayList<>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        long start = calendar.getTimeInMillis();
        long end = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        Map<String, UsageStats> stats = usageStatsManager.queryAndAggregateUsageStats(start, end);

        handler = new Handler();


        runnable = new Runnable() {
            public void run() {
                ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
                String runningApp = retriveNewApp();
                if(sharedpreferences.contains(runningApp) && stats.get(runningApp).getTotalTimeInForeground()>sharedpreferences.getLong(runningApp,0)){

                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);

                    am.killBackgroundProcesses(runningApp );
                }
                handler.postDelayed(runnable, 1000);


            }
        };

        handler.postDelayed(runnable, 1500);
    }

    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
    }

    // Returns the package name of app currently on screen
    private String retriveNewApp() {
        if (Build.VERSION.SDK_INT >= 21) {
            String currentApp = null;
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (applist != null && applist.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : applist) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }

            return currentApp;

        }
        else {

            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            String mm=(manager.getRunningTasks(1).get(0)).topActivity.getPackageName();
            return mm;
        }
    }
}
package com.example.apptracker;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, ServiceRestarterReceiver.class);
        this.sendBroadcast(broadcastIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        super.onStartCommand(intent, flags, startid);
        startServiceWork();
        return START_STICKY;
    }

    private void startServiceWork() {
        handler = new Handler();

        runnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void run() {
                ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
                String runningApp = retriveNewApp();
                if(sharedpreferences.contains(runningApp) ) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DATE, 0);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    long runningTime = getRunningTime(runningApp);
                    long timeLimit = sharedpreferences.getLong(runningApp,0);

                    if(runningTime > timeLimit) {
                        ArrayList<String> details = new ArrayList<>();
                        String time = "";
                        long hrs = timeLimit/3600000, min = (timeLimit/60000)%60;
                        if(hrs>0 && min>0) time = hrs + " h " + min + " m";
                        else if(min==0) time = hrs + " h";
                        else if(hrs==0) time = min + " m";
                        details.add(time);
                        details.add("You used all your time for " + getAppLable(context,runningApp) + ". You can use it again Tomorrow.");
                        Intent dialogIntent = new Intent(context, TimeOverPromptActivity.class);
                        dialogIntent.putExtra("AppName",details);
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(dialogIntent);

                        am.killBackgroundProcesses(runningApp);
                    }
                    else {
                        long hrs = timeLimit/3600000;
                        for(long i = hrs; i>0; i--) {
                            if(timeLimit-runningTime >= i*3600000 && timeLimit-runningTime <= i*3600000+3000) {
                                createNotificationChannel();
                                addNotification(timeLimit-runningTime, runningApp);
                            }
                        }
                        if((timeLimit-runningTime >= 30*60*1000 && timeLimit-runningTime <= 30*60*1000+3000) || (timeLimit-runningTime >= 5*60*1000 && timeLimit-runningTime <= 5*60*1000+3000)) {
                            createNotificationChannel();
                            addNotification(timeLimit-runningTime, runningApp);
                        }
                    }
                }
                handler.postDelayed(runnable, 1000);
            }
        };

        handler.postDelayed(runnable, 1500);
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    long getRunningTime( String runningApp ) {

        long runningTime = 0;
        UsageEvents.Event currentEvent;
        List<UsageEvents.Event> allEvents = new ArrayList<>();

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
            if ((currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                    currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) && currentEvent.getPackageName().equals(runningApp)) {
                allEvents.add(currentEvent);
            }
        }

        if(allEvents.get(0).getEventType()==2) runningTime+=allEvents.get(0).getTimeStamp()-startTime;
        //iterating through the arraylist
        for (int i=0;i<allEvents.size()-1;i++){
            UsageEvents.Event E0=allEvents.get(i);
            UsageEvents.Event E1=allEvents.get(i+1);


            //for UsageTime of apps in time range
            if (E0.getEventType()==1 && E1.getEventType()==2
                    && E0.getClassName().equals(E1.getClassName())){
                long diff = E1.getTimeStamp()-E0.getTimeStamp();
                runningTime += diff;
            }
        }
        // this will add the present running time of the app
        runningTime += ( currTime - allEvents.get( allEvents.size()-1 ).getTimeStamp());
        return runningTime;
    }

    public void addNotification(long timeLeft, String packageName) {
            long hrs = timeLeft/3600000, min = (timeLeft/60000)%60;
            String time = "";
            if(hrs>0 && min>0) time = hrs + " hours and " + min + "minutes";
            else if(min==0) time = hrs + " hours";
            else if(hrs==0) time = min + " minutes";
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this, "id")
                            .setSmallIcon(R.mipmap.ic_launcher_round) //set icon for notification
                            .setContentTitle(getAppLable(this,packageName)+ " : " + time + " left") //set title of notification
                            .setContentText("Tap to go to Apptracker")//this is notification message
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT); //set priority of notification

            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            // Add as notification
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "AppTracker";
            String description = "Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel("id", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
}
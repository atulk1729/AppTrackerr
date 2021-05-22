package com.example.apptracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LimitSetterActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limit_setter);

        ArrayList<Float> pastSevenDaysUse = this.getWeekData(getIntent().getStringExtra("AppName"));
        setBarGraph(pastSevenDaysUse);


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ArrayList<Float> getWeekData(String packageName) {
        ArrayList<Float> timeOfSevenDays = new ArrayList<>();
        float totalTime=0;
        for(int i=6; i>0; i--) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -i);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long start = calendar.getTimeInMillis();
            long end = start+24*3600000;
            HashMap<String,AppUsageInfo> stats = getUsageStatistics(start, end);
            float time = 0;
            if(stats.get(packageName)!=null) time = (float) stats.get(packageName).timeInForeground/3600000;
            timeOfSevenDays.add(time);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long start = calendar.getTimeInMillis();
        long end = System.currentTimeMillis();
        HashMap<String,AppUsageInfo> stats = getUsageStatistics(start, end);
        float time = (float) stats.get(packageName).timeInForeground/3600000;
        totalTime+=time;
        timeOfSevenDays.add(time);
        timeOfSevenDays.add(totalTime);
        return timeOfSevenDays;
    }

    private class AppUsageInfo {
        String appName, packageName;
        long timeInForeground;

        AppUsageInfo(String pName) {
            this.packageName=pName;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    HashMap<String,AppUsageInfo> getUsageStatistics(long startTime, long endTime) {

        UsageEvents.Event currentEvent;
        List<UsageEvents.Event> allEvents = new ArrayList<>();
        HashMap<String, AppUsageInfo> map = new HashMap <String, AppUsageInfo> ();

        UsageStatsManager mUsageStatsManager =  (UsageStatsManager)
                this.getSystemService(Context.USAGE_STATS_SERVICE);

        assert mUsageStatsManager != null;
        UsageEvents usageEvents = mUsageStatsManager.queryEvents(startTime, endTime);

        //capturing all events in a array to compare with next element
        while (usageEvents.hasNextEvent()) {
            currentEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(currentEvent);
            if (currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                    currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                allEvents.add(currentEvent);
                String key = currentEvent.getPackageName();
                // taking it into a collection to access by package name
                if (map.get(key)==null)
                    map.put(key,new AppUsageInfo(key));
            }
        }

        //iterating through the arraylist
        for (int i=0;i<allEvents.size()-1;i++){
            UsageEvents.Event E0=allEvents.get(i);
            UsageEvents.Event E1=allEvents.get(i+1);

            //for UsageTime of apps in time range
            if (E0.getEventType()==1 && E1.getEventType()==2
                    && E0.getClassName().equals(E1.getClassName())){
                long diff = E1.getTimeStamp()-E0.getTimeStamp();
                map.get(E0.getPackageName()).timeInForeground+= diff;
            }
        }
        return map;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setBarGraph(ArrayList<Float> pastSevenDaysUse) {
        BarChart barChart = findViewById(R.id.fragment_verticalbarchart_chart);
        ArrayList<BarEntry> entries = new ArrayList<>();
        for(int i=0; i<7; i++) {
            entries.add(new BarEntry(pastSevenDaysUse.get(i),i));
        }

        BarDataSet bardataset = new BarDataSet(entries, "Hours");

        String day = LocalDate.now().getDayOfWeek().name();
        String days[] = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int curr_day_index=0;
        for(int i=0; i<7; i++) {
            if(days[i].toUpperCase().equals(day)) curr_day_index=i;
        }
        Toast.makeText(this, ""+day+" "+curr_day_index, Toast.LENGTH_SHORT).show();
        ArrayList<String> labels = new ArrayList<String>();
        for(int i=curr_day_index+1; i<7; i++) {
            labels.add(days[i].substring(0,1));
        }
        for(int i=0; i<=curr_day_index; i++) {
            labels.add(days[i].substring(0,1));
        }

        BarData data = new BarData(labels, bardataset);
        barChart.setData(data); // set the data and list of labels into chart
        barChart.setDescription("App usage over past 7 days");
        bardataset.setColors(Collections.singletonList(ColorTemplate.getHoloBlue()));
        barChart.animateY(2000);
    }
}
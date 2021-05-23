package com.example.apptracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LimitSetterActivity extends AppCompatActivity {

    private BarChart barChart;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limit_setter);

        ArrayList<Float> pastSevenDaysUse = this.getWeekData(getIntent().getStringExtra("AppName"));
        barChart = findViewById(R.id.verticalbarchart_chart);
        loadBarGraph(pastSevenDaysUse);

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
    public void loadBarGraph(ArrayList<Float> pastSevenDaysUse) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for( int i = 0; i < pastSevenDaysUse.size(); i++ ) {
            entries.add(new BarEntry(i+1,pastSevenDaysUse.get(i)));
        }
        BarDataSet barDataSet = new BarDataSet(entries, "bar graph");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(12f);

        BarData barData = new BarData(barDataSet);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateY(2000);
    }
}

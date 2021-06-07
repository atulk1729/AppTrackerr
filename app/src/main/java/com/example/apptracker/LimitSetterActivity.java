package com.example.apptracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LimitSetterActivity extends AppCompatActivity {

    private String appPackageName;
    private BarChart barChart;
    private TimePicker timePicker;
    private SwitchMaterial switchMaterial;
    private SharedPreferences sharedPreferences = null;
    private String MyPREFERENCES = "AppInfos";
    private RelativeLayout relativeLayout;
    private TextView description;
    private TextView timeused;
    private long LongTime=0;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limit_setter);

        appPackageName = getIntent().getStringExtra("AppName");
        ArrayList<Float> pastSevenDaysUse = this.getWeekData(appPackageName);
        barChart = findViewById(R.id.verticalbarchart_chart);
        loadBarGraph(pastSevenDaysUse);

        relativeLayout = findViewById(R.id.time_picker_relayout);
        description = findViewById(R.id.limit_description);

        timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        switchMaterial = findViewById(R.id.switchMaterial);

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        setTimeText(appPackageName);
    }

    protected void onResume() {
        super.onResume();
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if( sharedPreferences.contains(appPackageName) ) {
            long milli = sharedPreferences.getLong(appPackageName, 0);
            int hours = (int)(milli/3600000);
            int minutes = (int)((milli/60000) % 60);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.setHour(hours);
                timePicker.setMinute(minutes);
            }
            else{
                timePicker.setCurrentHour(hours);
                timePicker.setCurrentMinute(minutes);
            }
            timePicker.setEnabled(false);
            switchMaterial.setChecked(true);
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.setHour(0);
                timePicker.setMinute(0);
            }
            else{
                timePicker.setCurrentHour(0);
                timePicker.setCurrentMinute(0);
            }
        }
    }

    // For expanding and collapsing set limit card
    public void onCardClicked(View v) {
        if (relativeLayout.getVisibility() == View.GONE) {
            // it's collapsed - expand it
            relativeLayout.setVisibility(View.VISIBLE);
            description.setVisibility(View.GONE);
        } else {
            // it's expanded - collapse it
            relativeLayout.setVisibility(View.GONE);
            description.setVisibility(View.VISIBLE);
        }
    }

    public void onSwitchClick(View v ) {

        if( switchMaterial.isChecked() ) {
            int hour;
            int minute;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                hour = timePicker.getHour();
                minute = timePicker.getMinute();
            }
            else {
                hour = timePicker.getCurrentHour();
                minute = timePicker.getCurrentMinute();
            }
            long milli = (hour * 60 + minute) * 60000;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(appPackageName, milli);
            editor.apply();
            timePicker.setEnabled(false);
            Toast.makeText(this, "Limit set successfully", Toast.LENGTH_LONG).show();
            setTimeText(appPackageName);
        }
        else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(appPackageName);
            editor.apply();
            timePicker.setEnabled(true);
            setTimeText(appPackageName);
        }

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
            float time = (float) getRunningTime(packageName,start,end)/3600000;
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
        long longTime = getRunningTime(packageName,start,end);
        float time = (float) longTime/3600000;

        LongTime = longTime;

        timeOfSevenDays.add(time);
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
    long getRunningTime( String runningApp, long startTime, long currTime ) {

        long runningTime = 0;
        UsageEvents.Event currentEvent;
        List<UsageEvents.Event> allEvents = new ArrayList<>();

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

        //if app was being used during the transition from one day to another
        if(!allEvents.isEmpty() && allEvents.get(0).getEventType()==2) runningTime+=allEvents.get(0).getTimeStamp()-startTime;

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
        if( allEvents.size() > 0 && allEvents.get(allEvents.size()-1).getEventType() == 1 ) {
            runningTime += (currTime - allEvents.get(allEvents.size() - 1).getTimeStamp());
        }
        return runningTime;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void loadBarGraph(ArrayList<Float> pastSevenDaysUse) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        String[] days = new String[] { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };

        ArrayList<BarEntry> entries = new ArrayList<>();
        for( int i = 0; i < 7; i++ ) {
            entries.add(new BarEntry(i+1,pastSevenDaysUse.get(i)));
        }
        BarDataSet barDataSet = new BarDataSet(entries, "bar graph");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(12f);

        BarData barData = new BarData(barDataSet);

        ArrayList<String> xAxisLabel = new ArrayList<>();
        for( int i = 0; i < 7; i++ ) {
            day = day % 7;
            xAxisLabel.add(days[day]);
            day++;
        }
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter(){
            public String getFormattedValue(float value) {
                if( value > 0 && value <= 7 ) return xAxisLabel.get((int)value-1);
                else return "";
            }
        });
        barChart.setData(barData);
        barChart.setDrawValueAboveBar(false);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateY(2000);
        barChart.invalidate();
    }

    public void setTimeText(String appPackageName) {
        timeused = findViewById(R.id.timeusedBytimelimit);
        String message = "Used ";
        long Hrs = LongTime/3600000, Min = (LongTime/60000)%60;
        if(Hrs!=0L && Min!=0L) {
            message += Hrs + " h " + Min + " m ";
        }
        else if(Hrs==0L) {
            message += Min + " m ";
        }
        else if(Min==0L) {
            message += Hrs + " h ";
        }
        if(sharedPreferences.contains(appPackageName)) {
            message += "out of ";
            long milli = sharedPreferences.getLong(appPackageName, 0);
            int hours = (int)(milli/3600000);
            int minutes = (int)((milli/60000) % 60);
            if(hours!=0L && minutes!=0L) {
                message += hours + " h " + minutes + " m ";
            }
            else if(hours==0) {
                message += minutes + " m ";
            }
            else if(minutes==0) {
                message += hours + " h ";
            }
            if(milli < LongTime) timeused.setTextColor(Color.RED);
        }
        else timeused.setTextColor(Color.rgb(152,151,151));
        message += "today";
        timeused.setText(message);
    }
}

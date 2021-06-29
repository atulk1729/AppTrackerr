package com.example.apptracker.appselector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.example.apptracker.apprecyclerview.AppInfo;
import com.example.apptracker.apprecyclerview.AppSelectorAdapter;
import com.example.apptracker.R;

import java.util.ArrayList;

public class AppSelectorActivity extends AppCompatActivity {

    private ArrayList<AppInfo> appInfos = new ArrayList<AppInfo>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selector);

        appInfos = getIntent().getParcelableArrayListExtra("AppInfoList");
        for( AppInfo a : appInfos ) {
            a.setIcon(getIcon(this, a.getPackageName()));
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        AppSelectorAdapter adapter = new AppSelectorAdapter(appInfos, appInfos.size(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

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
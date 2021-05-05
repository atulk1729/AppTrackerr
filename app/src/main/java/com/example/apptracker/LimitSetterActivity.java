package com.example.apptracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class LimitSetterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limit_setter);

        String s = getIntent().getStringExtra("AppName");
        TextView appName = findViewById(R.id.appName);
        appName.setText(s);
    }
}
package com.example.apptracker.background;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.apptracker.MainActivity;
import com.example.apptracker.R;

import java.util.ArrayList;

public class TimeOverPromptActivity extends AppCompatActivity {

    private ArrayList<String> details = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_over_prompt);

        TextView time = findViewById(R.id.time_over_time);
        TextView message = findViewById(R.id.time_over_message);

        details = getIntent().getStringArrayListExtra("AppName");
        time.setText(details.get(0));
        message.setText(details.get(1));

    }

    public void gotoAppTracker(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    public void onCancel(View v) {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        this.finish();
    }
}
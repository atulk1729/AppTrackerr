package com.example.apptracker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppSelectorAdapter extends RecyclerView.Adapter<AppSelectorAdapter.ViewHolder> {

    private ArrayList<AppInfo> appInfos = new ArrayList<>();
    private int size;
    private static Context context;

    public AppSelectorAdapter(ArrayList<AppInfo> appInfos, int size, Context context) {
        this.appInfos = appInfos;
        this.size = size;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.app_list_card,parent,false);
        AppSelectorAdapter.ViewHolder viewHolder = new AppSelectorAdapter.ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo appInfo = appInfos.get(position);
        holder.appName.setText(appInfo.getAppName());
        holder.appImage.setImageDrawable(appInfo.getIcon());
        holder.appTime.setText(appInfo.getTime());
    }

    @Override
    public int getItemCount() {
        return size;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView appImage;
        public TextView appName, appTime;
        public CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.appImage = itemView.findViewById(R.id.app_logo);
            this.appName = itemView.findViewById(R.id.app_label);
            this.appTime = itemView.findViewById(R.id.app_time);
            cardView = itemView.findViewById(R.id.appList_card);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, LimitSetterActivity.class);
            intent.putExtra("AppName", appName.getText());
            context.startActivity(intent);
        }
    }
}

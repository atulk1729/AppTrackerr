package com.example.apptracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    private ArrayList<AppInfo> appInfos = new ArrayList<>();
    private int size;

    public AppListAdapter(ArrayList<AppInfo> appInfos, int size) {
        this.appInfos=appInfos;
        this.size = size;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.app_list_card,parent,false);
        ViewHolder viewHolder = new ViewHolder(listItem);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView appImage;
        public TextView appName, appTime;
        public CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.appImage = itemView.findViewById(R.id.app_logo);
            this.appName = itemView.findViewById(R.id.app_label);
            this.appTime = itemView.findViewById(R.id.app_time);
            cardView = itemView.findViewById(R.id.appList_card);
        }
    }
}

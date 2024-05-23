package com.example.mos.ui.dailyLogs.dailyLogRV;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mos.R;
import com.example.mos.ui.dailyLogs.AddEditDailyLogActivity;

import java.util.ArrayList;

public class DailyLogsRVadapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<DailyLogModel> dailyLogs;

    public DailyLogsRVadapter(Context context, ArrayList<DailyLogModel> dailyLogs){
        this.context = context;
        this.dailyLogs = dailyLogs;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rv_item_daily_log, parent, false);
        DailyLogItemViewHolder dailyLogItemViewHolder = new DailyLogItemViewHolder(view);
        return dailyLogItemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DailyLogItemViewHolder dailyLogItemViewHolder = (DailyLogItemViewHolder) holder;
        DailyLogModel dailyLog = dailyLogs.get(position);
        dailyLogItemViewHolder.dayTV.setText(dailyLog.getDay());
        dailyLogItemViewHolder.DMYdateTV.setText(dailyLog.getDate()+"-"+dailyLog.getMonth()+"-"+dailyLog.getYear());

        dailyLogItemViewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddEditDailyLogActivity.class);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return dailyLogs.size();
    }

    public static class DailyLogItemViewHolder extends RecyclerView.ViewHolder {
        TextView dayTV, DMYdateTV;
        ConstraintLayout layout;
        public DailyLogItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.dayTV=itemView.findViewById(R.id.dayTV);
            this.DMYdateTV=itemView.findViewById(R.id.DMYdateTV);
            this.layout=itemView.findViewById(R.id.dailyLogItemLayout);
        }
    }
}

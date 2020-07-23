package com.sysu.sdcs.weatherstation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class Hour_Adapter extends RecyclerView.Adapter<Hour_Adapter.Hour_Holder> {

    private static final String TAG = "Hour_Adapter";
    private Context mContext;
    private List<Integer> maxData;
    private List<Integer> minData;
    private List<String> days;
    private int minValue;
    private int maxValue;

    public Hour_Adapter(Context context, List<Integer> maxData, List<Integer> minData, List<String> days) {
        this.maxData = maxData;
        this.minData = minData;
        this.days = days;
        minValue = Collections.min(minData);
        maxValue = Collections.max(maxData);
        mContext = context;
    }

    @NonNull
    @Override
    public Hour_Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.hourlyitem, viewGroup, false);
        return new Hour_Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Hour_Holder hour_holder, int i) {

        //如果是第一个
        if (i == 0) {
            hour_holder.mTemperatureViewMax.setDrawLeftLine(false);
            hour_holder.mTemperatureViewMin.setDrawLeftLine(false);
        }
        //除第一个以外
        else {
            hour_holder.mTemperatureViewMax.setDrawLeftLine(true);
            hour_holder.mTemperatureViewMax.setLastValue(maxData.get(i - 1));

            hour_holder.mTemperatureViewMin.setDrawLeftLine(true);
            hour_holder.mTemperatureViewMin.setLastValue(minData.get(i - 1));
        }

        //如果是最后一个
        if (i == maxData.size() - 1) {
            hour_holder.mTemperatureViewMax.setDrawRightLine(false);
            hour_holder.mTemperatureViewMin.setDrawRightLine(false);
        }
        //除最后一个以外
        else {
            hour_holder.mTemperatureViewMax.setDrawRightLine(true);
            hour_holder.mTemperatureViewMax.setNextValue(maxData.get(i + 1));

            hour_holder.mTemperatureViewMin.setDrawRightLine(true);
            hour_holder.mTemperatureViewMin.setNextValue(minData.get(i + 1));
        }

        hour_holder.mTemperatureViewMax.setCurrentValue(maxData.get(i));
        hour_holder.mTemperatureViewMax.setCurrentDay(days.get(i));

        hour_holder.mTemperatureViewMin.setCurrentValue(minData.get(i));
        hour_holder.mTemperatureViewMin.setCurrentDay("");

    }

    @Override
    public int getItemCount() {
        return maxData.size();
    }

    class Hour_Holder extends RecyclerView.ViewHolder {

        private TemperatureView mTemperatureViewMax;
        private TemperatureView mTemperatureViewMin;

        public Hour_Holder(@NonNull View itemView) {
            super(itemView);
            mTemperatureViewMax = itemView.findViewById(R.id.temp_view_max);
            mTemperatureViewMax.setMinValue(minValue);
            mTemperatureViewMax.setMaxValue(maxValue);
            mTemperatureViewMax.setColor("#FF2014");
            mTemperatureViewMax.setyBias(-20);
            mTemperatureViewMin = itemView.findViewById(R.id.temp_view_min);
            mTemperatureViewMin.setMinValue(minValue);
            mTemperatureViewMin.setMaxValue(maxValue);
            mTemperatureViewMin.setColor("#1480ff");
            mTemperatureViewMin.setyBias(50);
        }
    }

}
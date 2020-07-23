package com.sysu.sdcs.weatherstation;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedList;

public class CityAdapter extends BaseAdapter {

    private Context mContext;
    private LinkedList<SimWea> mData;

    public CityAdapter() {
    }

    public CityAdapter(LinkedList<SimWea> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.city = (TextView) convertView.findViewById(R.id.lv_city);
            holder.temp = (TextView) convertView.findViewById(R.id.lv_temp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.city.setText(mData.get(position).getCity());
        holder.temp.setText(mData.get(position).getTemp());
        return convertView;
    }

    static private class ViewHolder {
        TextView city;
        TextView temp;
    }

    public void add(SimWea weather) {
        if (mData == null) {
            mData = new LinkedList<>();
        }
        mData.add(weather);
        notifyDataSetChanged();
    }
}
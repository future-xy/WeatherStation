package com.sysu.sdcs.weatherstation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CityList extends AppCompatActivity {
    private WeatherDB weatherDb;
    private ListView lv;
    private ArrayList<SimWea> weas;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x123) {
                Cursor cursor = weatherDb.query("WeatherNow", null, null, null, null);
                listCites(cursor);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        lv = findViewById(R.id.listView);
        weatherDb = new WeatherDB(this);

        // 将增加城市按钮放到listview的最后一栏
        View view = View.inflate(CityList.this, R.layout.add_button, null);
        view.findViewById(R.id.addcity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CityList.this, AddCityActivity.class);
                //startActivity(intent);
                startActivityForResult(intent, 1);
            }
        });
        lv.addFooterView(view);

        listCity();
    }

    //列出所有城市及其温度
    private void listCity() {
        Cursor cursor = weatherDb.query("WeatherNow", null, null, null, null);
        listCites(cursor);
    }

    private void listCites(Cursor cursor) {
        if (cursor != null) {
            weas = new ArrayList<SimWea>();
            while (cursor.moveToNext()) {
                SimWea tmp = new SimWea();
                tmp.setID(cursor.getString(cursor.getColumnIndex("LocationID")));
                tmp.setCity(cursor.getString(cursor.getColumnIndex("City")));
                tmp.setTemp(cursor.getString(cursor.getColumnIndex("Temperature")));
                //tmp.setText(cursor.getString(cursor.getColumnIndex("Text")));
                weas.add(tmp);
            }
        }

        ArrayList<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < weas.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("City", weas.get(i).getCity());
            map.put("Temperature", weas.get(i).getTemp());
            data.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.list_item, new String[]{"City", "Temperature"},
                new int[]{R.id.lv_city, R.id.lv_temp});
        lv.setAdapter(adapter);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        lv.setLayoutParams(lp);

        // 短按跳转到详细天气页面
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        // 长按删除该城市
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                PopupMenu popup = new PopupMenu(CityList.this, view);
                popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());
                final SimWea tmp = weas.get(position);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.del:
                                delCity(tmp);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
                return true;
            }
        });
    }

    // 删除城市
    private void delCity(SimWea city) {
        String id = city.getID();
        int ret = weatherDb.delete("WeatherNow", "LocationID=?", new String[]{String.valueOf(id)});
        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
        Cursor newCursor = weatherDb.query("WeatherNow", null, null, null, null);
        listCites(newCursor);

    }

    @Override  // 用于响应AddCityActivity的跳转回复
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1) {//当请求码是1&&返回码是1进行下面操作
            new Thread() {
                @Override
                public void run() {
                    try {
                        // 刷新城市列表，等待API调用完毕
                        for (int i = 0; i < 20; i++) {
                            handler.sendEmptyMessage(0x123);
                            Thread.sleep(100);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.start();
        }
    }

}
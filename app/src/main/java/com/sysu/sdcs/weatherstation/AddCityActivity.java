package com.sysu.sdcs.weatherstation;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.base.Code;
import interfaces.heweather.com.interfacesmodule.bean.base.Lang;
import interfaces.heweather.com.interfacesmodule.bean.base.Mode;
import interfaces.heweather.com.interfacesmodule.bean.base.Range;
import interfaces.heweather.com.interfacesmodule.bean.base.Unit;
import interfaces.heweather.com.interfacesmodule.bean.geo.GeoBean;
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherNowBean;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;


public class AddCityActivity extends AppCompatActivity {
    private WeatherDB weatherDb;
    private ArrayList<String> mStrs;
    private SearchView mSearchView;
    private ListView mListView;
    private ArrayAdapter adapter;
    private Context context;
    private ArrayList<GeoBean.LocationBean> lis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcity);


        weatherDb = new WeatherDB(this);
        context = this;
        lis = new ArrayList<>();
        mSearchView = findViewById(R.id.searchView);
        mSearchView.setIconifiedByDefault(false);
        mListView = findViewById(R.id.searchlistView);
        mStrs = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mStrs);
        mListView.setAdapter(adapter);
        mListView.setTextFilterEnabled(true);

        // 设置搜索结果listview监听
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GeoBean.LocationBean tmp = lis.get(position);
                // 先直接插入城市
                ContentValues cv = new ContentValues();
                cv.put("LocationID", tmp.getId());
                cv.put("City", tmp.getName());
                cv.put("Temperature", "--");
                weatherDb.insert("WeatherNow", cv);
                // 更新城市温度
                getTempandInsert(tmp.getId(), tmp.getName());
                Intent data = new Intent();
                //resultCode是返回码,用来确定是哪个页面传来的数据，这里设置返回码是2
                setResult(1, data);
                finish();
            }
        });

        // 设置搜索文本监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                // 调用API模糊搜索城市信息
                HeWeather.getGeoCityLookup(context, newText, Mode.FUZZY, Range.WORLD, 10, Lang.ZH_HANS, new HeWeather.OnResultGeoListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        Log.i("err", "getGeoCityLookup onError: " + throwable);
                    }

                    @Override
                    public void onSuccess(GeoBean geoBean) {
                        //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                        if (Code.OK.getCode().equalsIgnoreCase(geoBean.getStatus())) {
                            lis.clear();
                            List<GeoBean.LocationBean> tmpLis = geoBean.getLocationBean();
                            lis.addAll(tmpLis);
                            mStrs.clear();
                            for (int i = 0; i < lis.size(); i++) {
                                mStrs.add(lis.get(i).getName());
                            }
                            mListView.setAdapter(adapter);

                        } else {
                            //在此查看返回数据失败的原因
                            String status = geoBean.getStatus();
                            Code code = Code.toEnum(status);
                            Log.i("res", "failed code: " + code);
                        }
                    }
                });

                //adapter.getFilter().filter(newText);//通过适配器过滤
                return false;
            }
        });

    }

    // 获取所选城市温度，插入数据库
    private void getTempandInsert(final String lid, final String name) {
        final String[] temperature = new String[1];
        // 调用查询实况天气API
        HeWeather.getWeatherNow(this, lid, Lang.ZH_HANS, Unit.METRIC, new HeWeather.OnResultWeatherNowListener() {
            @Override
            public void onError(Throwable e) {
                Log.i("err", "getWeather onError: " + e);
            }

            @Override
            public void onSuccess(WeatherNowBean weatherBean) {
                Log.i("res", "getWeather onSuccess: " + new Gson().toJson(weatherBean));
                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                if (Code.OK.getCode().equalsIgnoreCase(weatherBean.getCode())) {
                    WeatherNowBean.NowBaseBean now = weatherBean.getNow();
                    temperature[0] = now.getTemp();

                    ContentValues cv = new ContentValues();
                    cv.put("LocationID", lid);
                    cv.put("City", name);
                    cv.put("Temperature", temperature[0]);
                    weatherDb.update("WeatherNow", cv, "LocationID=?", new String[]{lid});
                    if (!MainActivity.city_names.contains(name)) {
                        MainActivity.city_names.add(name);
                    }
                } else {
                    //在此查看返回数据失败的原因
                    String status = weatherBean.getCode();
                    Code code = Code.toEnum(status);
                    Log.i("err", "failed code: " + code);
                }
            }
        });

    }


}

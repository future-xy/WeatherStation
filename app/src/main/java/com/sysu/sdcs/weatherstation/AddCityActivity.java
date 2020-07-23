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

        HeConfig.init("HE2007222112481749", "a737d0e84c084b84ba41b547cf25b98a");
        HeConfig.switchToDevService();
        //HeConfig.init(""); //("@string/PublicId", "@string/AppKey");

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
//                mStrs.clear();
//                mStrs.add("广州");
//                mStrs.add("白云");
//                mStrs.add("番禺");
//                mStrs.add("越秀");
//                mStrs.add("增城");
//                mStrs.add("海珠");
//                mStrs.add("天河");
//                mStrs.add("花都");
//                mStrs.add("从化");
//                mStrs.add("黄埔");
                mStrs.clear();
                mStrs.add("广州");

                HeWeather.getGeoCityLookup(context, newText, Mode.FUZZY, Range.WORLD, 1, Lang.ZH_HANS, new HeWeather.OnResultGeoListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        Log.i("err", "getGeoCityLookup onError: " + throwable);
                    }

                    @Override
                    public void onSuccess(GeoBean geoBean) {
                        Log.i("res", "getGeoCityLookup onSuccess: " + new Gson().toJson(geoBean));
                        //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                        if (Code.OK.getCode().equalsIgnoreCase(geoBean.getStatus())) {
                            System.out.println("****************enter success case");

                            lis.clear();
                            List<GeoBean.LocationBean> tmpLis = geoBean.getLocationBean();
                            //mStrs.add(tmpLis.get(i).getName());
                            lis.addAll(tmpLis);

                        } else {
                            //在此查看返回数据失败的原因
                            String status = geoBean.getStatus();
                            Code code = Code.toEnum(status);
                            Log.i("res", "failed code: " + code);
                        }
                        System.out.println("***************before finish onsuccess");
                    }
                });
                System.out.println("***************get out of heweather");
                adapter.getFilter().filter(newText);//通过适配器过滤
                System.out.println("***************before return");
                return false;
            }
        });
    }

    private void getTempandInsert(final String lid, final String name) {
        final String[] temperature = new String[1];
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
                    System.out.println("******************the temp now:");
                    System.out.println(now.getTemp());
                    System.out.println(temperature[0]);

                    ContentValues cv = new ContentValues();
                    cv.put("LocationID", lid);
                    cv.put("City", name);
                    cv.put("Temperature", temperature[0]);
                    weatherDb.insert("WeatherNow", cv);
                    System.out.println("insert successfully!!!");
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

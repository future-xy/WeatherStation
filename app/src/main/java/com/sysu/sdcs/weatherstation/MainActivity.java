package com.sysu.sdcs.weatherstation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.gson.Gson;
import com.heweather.plugin.view.HeWeatherConfig;
import com.heweather.plugin.view.RightLargeView;
import com.heweather.plugin.view.VerticalView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.air.AirNowBean;
import interfaces.heweather.com.interfacesmodule.bean.base.Code;
import interfaces.heweather.com.interfacesmodule.bean.base.Lang;
import interfaces.heweather.com.interfacesmodule.bean.base.Unit;
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherDailyBean;
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherDailyBean.DailyBean;
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherNowBean;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class MainActivity extends AppCompatActivity implements Handler.Callback {
    final String TAG = "MWW";
    private String HWID = "HE2007141352421044";
    private String HWKEY = "a6e621bd44ed41559b84f5450b42896c";
    private String APIKEY = "ff91402a13b144cf8ec6829df147c84f";
    private String curCity = "广州";//默认城市
    private VerticalView verticalView;
    private RightLargeView rightLargeView;
    private WeatherNowBean.NowBaseBean nowBaseBean;
    private List<DailyBean> _15DBean;
    private AirNowBean.NowBean nowAirBean;

    final private Handler handler = new Handler(this);
    private RecyclerView recyclerView;
    private TextView temperature;
    private TextView weather1;
    private TextView windDirection;
    private TextView relativeHumility;
    private TextView pm25;
    private TextView pm10;
    private TextView so2;
    private TextView no2;
    private ArcProgress arcProgress;
    private SunriseView sunriseView;
    private int sunriseW;
    private int sunriseH;
    private String sunriseToday;
    private String sunsetToday;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //edited by hyj

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        太丑了，感觉没必要
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle("天气屋");
        */
        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("天气屋");
        mToolbar.setSubtitle("测试版");
        mToolbar.inflateMenu(R.menu.menu);
        setSupportActionBar(mToolbar);

        recyclerView = findViewById(R.id.daily);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(manager);
        pm25 = findViewById(R.id.PM2_5);
        pm10 = findViewById(R.id.PM10);
        so2 = findViewById(R.id.SO2);
        no2 = findViewById(R.id.NO2);

        final Cities cities = new Cities();
        HeWeatherConfig.init(HWKEY, curCity);//UI SDK
        HeConfig.init(HWID, HWKEY);//DATA SDK
        HeConfig.switchToDevService();//没有专业版,切换到开发者模式

        //切换城市
        final String[] city_names = cities.getCitynames();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, city_names);
        Spinner spinner = findViewById(R.id.city_name_spinner);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, city_names[position]);
                getWeatherInfo(cities.getCode(city_names[position]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "NOTHING!");
            }
        });
        arcProgress = findViewById(R.id.arc_progress);

        getWeatherInfo("CN" + cities.getCode(curCity));//好像失灵了...
        sunriseView = findViewById(R.id.sun);

   /*   这部分代码移植到getWeatherInfo函数中了
        HeWeather.getWeatherNow(MainActivity.this, "CN101010100", Lang.ZH_HANS, Unit.METRIC, new HeWeather.OnResultWeatherNowListener() {
            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "getWeather onError: " + e);
            }

            @Override
            public void onSuccess(WeatherNowBean weatherBean) {
                Log.i(TAG, "getWeather onSuccess: " + new Gson().toJson(weatherBean));
                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                if (Code.OK.getCode().equalsIgnoreCase(weatherBean.getCode())) {
                    Log.d(TAG, "msg");
                    nowBaseBean = weatherBean.getNow();
                    Log.d(TAG, new Gson().toJson(nowBaseBean));
                    handler.sendEmptyMessage(0);
                } else {
                    //在此查看返回数据失败的原因
                    String status = weatherBean.getCode();
                    Code code = Code.toEnum(status);
                    Log.i(TAG, "failed code: " + code);
                }
            }
        });

        HeWeather.getWeather15D(MainActivity.this, "CN101010100", Lang.ZH_HANS, Unit.METRIC, new HeWeather.OnResultWeatherDailyListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.i(TAG, "getWeather onError: " + throwable);
            }

            @Override
            public void onSuccess(WeatherDailyBean weatherDailyBean) {
                Log.i(TAG, "getWeather onSuccess: " + new Gson().toJson(weatherDailyBean));
                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                if (Code.OK.getCode().equalsIgnoreCase(weatherDailyBean.getCode())) {
                    Log.d(TAG, "msg");
                    _15DBean = weatherDailyBean.getDaily();
                    Log.d(TAG, new Gson().toJson(nowBaseBean));
                    handler.sendEmptyMessage(15);
                } else {
                    //在此查看返回数据失败的原因
                    String status = weatherDailyBean.getCode();
                    Code code = Code.toEnum(status);
                    Log.i(TAG, "failed code: " + code);
                }
            }
        });*/
//        unitTest();
//        BottomBar bottomBar = findViewById(R.id.bottomBar);//底部导航栏的使用方法见 https://github.com/roughike/BottomBar
//        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
//            @Override
//            public void onTabSelected(int tabId) {
//                switch (tabId) {
//                    case R.id.tab_home:
//                        Log.d("nav", "to page home");
//                        break;
//                    case R.id.tab_map:
//                        Log.d("nav", "to page map");
//                        Intent intent = new Intent(MainActivity.this, CloudGraphActivity.class);
//                        startActivity(intent);
//                        break;
//                    case R.id.tab_setting:
//                        Log.d("nav", "to page setting");
//                        break;
//                }
//            }
//        });
    }
    public void  startSunAnim(int sunrise,int sunset){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if(hour < sunrise){
            sunriseView.sunAnim(0,sunriseW,sunriseH);
        }else if(hour > sunset){
            sunriseView.sunAnim(1,sunriseW,sunriseH);
        }else {
            sunriseView.sunAnim(((float) hour - (float) sunrise) / ((float) sunset - (float) sunrise),sunriseW,sunriseH);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            sunriseH = sunriseView.getLayoutParams().height;
            sunriseW =  sunriseView.getLayoutParams().width;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cloud:
                Intent intent = new Intent(MainActivity.this, CloudGraphActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                break;
            case R.id.action_quit:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getWeatherInfo(String cityID) {
        /*
            cityID: e.g. CN1010100
         */
        HeWeather.getWeatherNow(MainActivity.this, cityID, Lang.ZH_HANS, Unit.METRIC, new HeWeather.OnResultWeatherNowListener() {
            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "getWeather onError: " + e);
            }

            @Override
            public void onSuccess(WeatherNowBean weatherBean) {
                Log.i(TAG, "getWeather onSuccess: " + new Gson().toJson(weatherBean));
                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                if (Code.OK.getCode().equalsIgnoreCase(weatherBean.getCode())) {
                    nowBaseBean = weatherBean.getNow();
                    handler.sendEmptyMessage(0);
                } else {
                    //在此查看返回数据失败的原因
                    String status = weatherBean.getCode();
                    Code code = Code.toEnum(status);
                    Log.i(TAG, "failed code: " + code);
                }
            }
        });

        HeWeather.getWeather15D(MainActivity.this, cityID, Lang.ZH_HANS, Unit.METRIC, new HeWeather.OnResultWeatherDailyListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.i(TAG, "getWeather onError: " + throwable);
            }

            @Override
            public void onSuccess(WeatherDailyBean weatherDailyBean) {
                Log.i(TAG, "getWeather onSuccess: " + new Gson().toJson(weatherDailyBean));
                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                if (Code.OK.getCode().equalsIgnoreCase(weatherDailyBean.getCode())) {
                    _15DBean = weatherDailyBean.getDaily();
                    sunriseToday = _15DBean.get(0).getSunrise();
                    sunsetToday = _15DBean.get(0).getSunset();

                    handler.sendEmptyMessage(15);
                } else {
                    //在此查看返回数据失败的原因
                    String status = weatherDailyBean.getCode();
                    Code code = Code.toEnum(status);
                    Log.i(TAG, "failed code: " + code);
                }
            }
        });

        HeWeather.getAirNow(MainActivity.this, cityID, Lang.ZH_HANS,new HeWeather.OnResultAirNowListener(){


            @Override
            public void onError(Throwable throwable) {
                Log.i(TAG, "getAir onError: " + throwable);

            }

            @Override
            public void onSuccess(AirNowBean airNowBean) {
                Log.i(TAG, "getAir onSuccess: " + new Gson().toJson(airNowBean));
                if (Code.OK.getCode().equalsIgnoreCase(airNowBean.getCode())) {
                    nowAirBean = airNowBean.getNow();
                    handler.sendEmptyMessage(25);
                } else {
                    //在此查看返回数据失败的原因
                    String status = airNowBean.getCode();
                    Code code = Code.toEnum(status);
                    Log.i(TAG, "failed code: " + code);
                }
            }
        });

        arcProgress.setVisibility(View.VISIBLE);
    }


//    void unitTest() {
//        System.out.println("code of gz: " + cities.getCode("广州"));//TEST1
//        //NetUtils.sendInfo("https://devapi.heweather.net/v7/weather/now?location=101010100&key=ff91402a13b144cf8ec6829df147c84f");
//        //getWeatherInfo("CN"+cities.getCode(curCity));//TEST2
//    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        if (msg.what == 15) {
            //15天天气在这里更新
            List<Integer> data = new ArrayList<>();
            List<String> days = new ArrayList<>();
            for (DailyBean dailyBean : _15DBean) {
                data.add(Integer.valueOf(dailyBean.getTempMax()));
                String date = dailyBean.getFxDate();
                days.add(date.split("-", 2)[1]);
            }
            Hour_Adapter adapter = new Hour_Adapter(this, data, days);
            recyclerView.setAdapter(adapter);

            TextView sunriseTime = findViewById(R.id.sunrise_time);
            TextView sunsetTime = findViewById(R.id.sunset_time);
            sunriseTime.setText(sunriseToday);
            sunsetTime.setText(sunsetToday);
            startSunAnim(getHourFromTime(sunriseToday),getHourFromTime(sunsetToday));
            return true;
        }
        else if (msg.what == 0) {
            //首页天气在这里更新
            //即时温度
            temperature = findViewById(R.id.temperature);
            temperature.setText(String.format("%s℃", nowBaseBean.getTemp()));
            //温度范围
            windDirection = findViewById(R.id.wind_direction);
            windDirection.setText(String.format("%s级%s", nowBaseBean.getWindScale(), nowBaseBean.getWindDir()));
            //天气(晴雨)
            weather1 = findViewById(R.id.weather1);
            int cloud = Integer.parseInt(nowBaseBean.getCloud());
            double rain = Double.parseDouble(nowBaseBean.getPrecip());
            String cloudL = cloudLevel(cloud);
            String rainL = rainLevel(rain);
            String weatherL;
            if (rainL.equals("晴")) {
                weatherL = cloudL;
            } else {
                weatherL = rainL;
            }
            weather1.setText(weatherL);
            ImageView weatherPic = findViewById(R.id.weatherpic);
            switch (weatherL) {
                case "小雨":
                    weatherPic.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.w_smallrain));
                case "中雨":
                    weatherPic.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.w_middlerain));
                case "大雨":
                    weatherPic.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.w_heavyrain));
                    break;
                case "暴雨":
                    weatherPic.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.w_rainstorm));
                    break;
                case "阴":
                    weatherPic.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.w_cloudy));
                    break;
                case "多云":
                    weatherPic.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.w_cloud));
                    break;
                case "晴":
                default:
                    weatherPic.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.w_sunny));
                    break;
            }
            //weatherPic.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.w_sunny));
            // 相对湿度

            relativeHumility = findViewById(R.id.relative_humility);
            relativeHumility.setText(String.format("湿度%s%%", nowBaseBean.getHumidity()));

            return true;
        }
        else if(msg.what==25){
            String unit = "μg/m³";
            int quality = Integer.parseInt(nowAirBean.getAqi());
            String qualityDescription = "";
            if (quality<=50){
                qualityDescription = "优";
            }else if(quality<=100){
                qualityDescription = "良";
            }else if(quality<=150){
                qualityDescription = "轻度污染";
            }else if(quality<=200){
                qualityDescription = "重度污染";
            }else{
                qualityDescription = "严重污染";
            }
            arcProgress.setProgress(quality);
            arcProgress.setBottomText(qualityDescription);
            pm25.setText(nowAirBean.getPm2p5()+unit);
            pm10.setText(nowAirBean.getPm10()+unit);
            so2.setText(nowAirBean.getSo2()+unit);
            no2.setText(nowAirBean.getNo2()+unit);
            nowAirBean.getNo2();

            return true;
        }
        else if(msg.what==13){
            return true;
        }
        else
            return false;
    }
    private int getHourFromTime(String time){
        String hour = time.substring(0,2);
        return Integer.parseInt(hour);
    }
    private String cloudLevel(Integer cloud) {
        if (cloud <= 30)
            return "晴";
        else if (cloud <= 60)
            return "多云";
        else if (cloud <= 100)
            return "阴";
        return "阴转多云";
    }

    private String rainLevel(Double rain) {
        if (rain <= 5)
            return "晴";
        else if (rain <= 30)
            return "小雨";
        else if (rain <= 60)
            return "中雨";
        else if (rain <= 90)
            return "暴雨";
        return "特大暴雨";
    }
}
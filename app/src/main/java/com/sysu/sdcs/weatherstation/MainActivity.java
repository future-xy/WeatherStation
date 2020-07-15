package com.sysu.sdcs.weatherstation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.heweather.plugin.view.HeContent;
import com.heweather.plugin.view.HeWeatherConfig;
import com.heweather.plugin.view.HorizonView;
import com.heweather.plugin.view.LeftLargeView;
import com.heweather.plugin.view.RightLargeView;
import com.heweather.plugin.view.VerticalView;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import interfaces.heweather.com.interfacesmodule.bean.base.Code;
import interfaces.heweather.com.interfacesmodule.bean.base.Lang;
import interfaces.heweather.com.interfacesmodule.bean.base.Unit;
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
    private Cities cities;
    final private Handler handler = new Handler(this);

    private TextView cityname;

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
        cities = new Cities();
        HeWeatherConfig.init(HWKEY, curCity);//UI SDK
        HeConfig.init(HWID, HWKEY);//DATA SDK
        HeConfig.switchToDevService();//没有专业版,切换到开发者模式
        nowBaseBean = new WeatherNowBean.NowBaseBean();
        getWeatherInfo("CN" + cities.getCode(curCity));//好像失灵了...
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
//        HeWeather.getWeatherNow(MainActivity.this
//                , "CN1010100", Lang.ZH_HANS, Unit.METRIC, new HeWeather.OnResultWeatherNowListener() {
//                    @Override
//                    public void onError(Throwable throwable) {
//                        Log.i(TAG, "onError:", throwable);
//                        System.out.println("Weather Now Error:" + new Gson());
//                    }
//
//                    @Override
//                    public void onSuccess(WeatherNowBean weatherNowBean) {
//                        Log.i(TAG, "getWeather onSuccess: " + new Gson().toJson(weatherNowBean));
//                        if (Code.OK.getCode().equalsIgnoreCase(weatherNowBean.getCode())) {
//                            nowBaseBean = weatherNowBean.getNow();
//                            Log.d(TAG, "msg");
//                            handler.sendEmptyMessage(0);
//                            /* now 的使用方法
//                            属性	说明	示例值
//                            getObsTime	实况观测时间	2013-12-30 13:14
//                            getFeelsLike	体感温度，默认单位：摄氏度	23
//                            getTemp	温度，默认单位：摄氏度	21
//                            getIcon	实况天气状况代码	100
//                            getText	实况天气状况代码	晴
//                            getWind360	风向360角度	305
//                            getWindDir	风向	西北
//                            getWindScale	风力	3-4
//                            getWindSpeed	风速，公里/小时	15
//                            getHumidity	相对湿度	40
//                            getPrecip	降水量	0
//                            getPressure	大气压强	1020
//                            getVis	能见度，默认单位：公里	10
//                            getCloud	云量	23
//                            getDew	实况云量	23
//                            */
//                        } else {
//                            String status = weatherNowBean.getCode();
//                            Code code = Code.toEnum(status);
//                            Log.i("error: ", "failed code " + code);
//                        }
//                    }
//                });


        unitTest();
        BottomBar bottomBar = findViewById(R.id.bottomBar);//底部导航栏的使用方法见 https://github.com/roughike/BottomBar
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(int tabId) {
                switch (tabId) {
                    case R.id.tab_home:
                        Log.d("nav", "to page home");
                        break;
                    case R.id.tab_map:
                        Log.d("nav", "to page map");
                        break;
                    case R.id.tab_setting:
                        Log.d("nav", "to page setting");
                        break;
                }
            }
        });

        //从上到下更新控件
        //更新控件必须在成功get之后
    }

    public void getWeatherInfo(String cityID) {
        /*
            cityID: e.g. CN1010100
         */
        HeWeather.getWeatherNow(MainActivity.this
                , "CN1010100", Lang.ZH_HANS, Unit.METRIC, new HeWeather.OnResultWeatherNowListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        Log.i("error", "onError:", throwable);
                        System.out.println("Weather Now Error:" + new Gson());
                    }

                    @Override
                    public void onSuccess(WeatherNowBean weatherNowBean) {
                        if (Code.OK.getCode().equalsIgnoreCase(weatherNowBean.getCode())) {
                            nowBaseBean = weatherNowBean.getNow();
                            /* now 的使用方法
                            属性	说明	示例值
                            getObsTime	实况观测时间	2013-12-30 13:14
                            getFeelsLike	体感温度，默认单位：摄氏度	23
                            getTemp	温度，默认单位：摄氏度	21
                            getIcon	实况天气状况代码	100
                            getText	实况天气状况代码	晴
                            getWind360	风向360角度	305
                            getWindDir	风向	西北
                            getWindScale	风力	3-4
                            getWindSpeed	风速，公里/小时	15
                            getHumidity	相对湿度	40
                            getPrecip	降水量	0
                            getPressure	大气压强	1020
                            getVis	能见度，默认单位：公里	10
                            getCloud	云量	23
                            getDew	实况云量	23
                            */
                        } else {
                            String status = weatherNowBean.getCode();
                            Code code = Code.toEnum(status);
                            Log.i("error: ", "failed code " + code);
                        }
                    }
                });

    }


    void unitTest() {
        System.out.println("code of gz: " + cities.getCode("广州"));//TEST1
        NetUtils.sendInfo("https://devapi.heweather.net/v7/weather/now?location=101010100&key=ff91402a13b144cf8ec6829df147c84f");
        //getWeatherInfo("CN"+cities.getCode(curCity));//TEST2
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        Log.d(TAG, nowBaseBean.getTemp()+"C");
        cityname = findViewById(R.id.location);
        cityname.setText(nowBaseBean.getTemp() + "℃");
        return true;
    }
}
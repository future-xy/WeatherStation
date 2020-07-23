package com.sysu.sdcs.weatherstation;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.heweather.plugin.view.HeWeatherConfig;
import com.heweather.plugin.view.RightLargeView;
import com.heweather.plugin.view.VerticalView;

import java.lang.reflect.GenericSignatureFormatError;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import interfaces.heweather.com.interfacesmodule.bean.air.AirNowBean;
import interfaces.heweather.com.interfacesmodule.bean.base.Code;
import interfaces.heweather.com.interfacesmodule.bean.base.Lang;
import interfaces.heweather.com.interfacesmodule.bean.base.Unit;
import interfaces.heweather.com.interfacesmodule.bean.geo.GeoBean;
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
    private String defaultCity = "广州";//默认城市
    private VerticalView verticalView;
    private RightLargeView rightLargeView;
    //临时存储天气
    private WeatherNowBean.NowBaseBean nowBaseBean;
    private List<DailyBean> _15DBean;
    private AirNowBean.NowBean nowAirBean;
    ArrayAdapter<String> arrayAdapter;
    //DB
    SQLiteDatabase db;
    final static ArrayList<String> city_names = new ArrayList<>();
    static boolean setDefault = false;
    final Cities cities = new Cities();
    LocationManager locationManager;

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

        arcProgress = findViewById(R.id.arc_progress);

        HeWeatherConfig.init(HWKEY, defaultCity);//UI SDK
        HeConfig.init(HWID, HWKEY);//DATA SDK
        HeConfig.switchToDevService();//没有专业版,切换到开发者模式

        if (!setDefault) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                String location = getLocation();
                if (location.length() > 0) {
                    HeWeather.getGeoCityLookup(MainActivity.this, location, new HeWeather.OnResultGeoListener() {
                        @Override
                        public void onError(Throwable throwable) {
                            Log.i(TAG, "onError: " + throwable);
                        }

                        @Override
                        public void onSuccess(GeoBean geoBean) {
                            //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                            if (Code.OK.getCode().equalsIgnoreCase(geoBean.getStatus())) {
                                List<GeoBean.LocationBean> locationBeans = geoBean.getLocationBean();
                                defaultCity = locationBeans.get(0).getAdm2();
                                Log.d(TAG, "onSuccess: " + defaultCity);
                                handler.sendEmptyMessage(1);
                                setDefault = true;
                            } else {
                                //在此查看返回数据失败的原因
                                String status = geoBean.getStatus();
                                Code code = Code.toEnum(status);
                                Log.i("res", "failed code: " + code);
                            }
                        }
                    });
                }
            }
        }

        DBOpenHandler dbOpenHandler = new DBOpenHandler(this, "dbWeather.db3", null, 1);
        db = dbOpenHandler.getWritableDatabase();
        Cursor cursor = db.query("WeatherNow", new String[]{"City"}, null, null, null, null, null);
        city_names.clear();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("City"));
                if (name != null)
                    city_names.add(name);
            }
            cursor.close();
        }
//        if (city_names.size() == 0)
//            city_names.add(defaultCity);
        Log.d(TAG, "onCreate: " + city_names.size());

        //通过Spinner切换城市，测试版
        arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, city_names);
        Spinner spinner = findViewById(R.id.city_name_spinner);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, city_names.get(position));
                getWeatherInfo(cities.getCode(city_names.get(position)), city_names.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "NOTHING!");
            }
        });

        sunriseView = findViewById(R.id.sun);

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

    //判断是否有网络连接
    boolean networkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network network = cm.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities nc = cm.getNetworkCapabilities(network);
            if (nc != null) {
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return true;
                else return nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            }
        }

        return false;
    }

    String getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: permission");
            Toast.makeText(MainActivity.this, "请开启位置(GPS)权限", Toast.LENGTH_LONG).show();
            return "";
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            return longitude + "," + latitude;
        }
        Log.d(TAG, "getLocation: null");
        return "";
    }


    public void startSunAnim(int sunrise, int sunset) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < sunrise) {
            sunriseView.sunAnim(0, sunriseW, sunriseH);
        } else if (hour > sunset) {
            sunriseView.sunAnim(1, sunriseW, sunriseH);
        } else {
            sunriseView.sunAnim(((float) hour - (float) sunrise) / ((float) sunset - (float) sunrise), sunriseW, sunriseH);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            sunriseH = sunriseView.getLayoutParams().height;
            sunriseW = sunriseView.getLayoutParams().width;
//            getCityNames();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Action Bar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cloud:
                Intent intent = new Intent(MainActivity.this, CloudGraphActivity.class);
                startActivity(intent);
                break;
            case R.id.action_city_list:
                Intent intent1 = new Intent(MainActivity.this, CityList.class);
                startActivity(intent1);
                break;
            case R.id.action_quit:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //以后在这个函数添加功能时，如果顺利获取天气就存在db中
    public void getWeatherInfo(final String cityID, final String cityName) {
        /*
            cityID: e.g. CN1010100
         */
        //如果有网则更新天气
        if (networkConnected()) {
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

                        //插入数据到数据库
                        ContentValues cv = new ContentValues();
                        cv.put("LocationID", cityID);
                        cv.put("DayBean", new Gson().toJson(nowBaseBean));
                        cv.put("City", cityName);
                        cv.put("Temperature", nowBaseBean.getTemp());
                        db.replace("WeatherNow", String.format("LocationID=%s", cityID), cv);

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
                        Log.d(TAG, new Gson().toJson(_15DBean));
                        sunriseToday = _15DBean.get(0).getSunrise();
                        sunsetToday = _15DBean.get(0).getSunset();
                        String _15DStr = new Gson().toJson(_15DBean);

                        ContentValues cv = new ContentValues();
                        cv.put("Status", _15DStr);
                        cv.put("LocationID", cityID);
                        long ret = db.replace("WeatherDaily", String.format("LocationID=%s", cityID), cv);

                        Log.d(TAG, String.format("LocationID=%s", cityID) + " " + ret);
                        handler.sendEmptyMessage(15);
                    } else {
                        //在此查看返回数据失败的原因
                        String status = weatherDailyBean.getCode();
                        Code code = Code.toEnum(status);
                        Log.i(TAG, "failed code: " + code);
                    }
                }
            });

            HeWeather.getAirNow(MainActivity.this, cityID, Lang.ZH_HANS, new HeWeather.OnResultAirNowListener() {
                @Override
                public void onError(Throwable throwable) {
                    Log.i(TAG, "getAir onError: " + throwable);
                }

                @Override
                public void onSuccess(AirNowBean airNowBean) {
                    Log.i(TAG, "getAir onSuccess: " + new Gson().toJson(airNowBean));
                    if (Code.OK.getCode().equalsIgnoreCase(airNowBean.getCode())) {
                        nowAirBean = airNowBean.getNow();
                        ContentValues cv = new ContentValues();
                        cv.put("AirBean", new Gson().toJson(nowAirBean));
                        Log.d(TAG, new Gson().toJson(nowAirBean));
                        long ret = db.update("WeatherNow", cv, String.format("LocationID=%s", cityID), null);
                        Log.d(TAG, "onSuccess: " + ret);
                        handler.sendEmptyMessage(25);
                    } else {
                        //在此查看返回数据失败的原因
                        String status = airNowBean.getCode();
                        Code code = Code.toEnum(status);
                        Log.i(TAG, "failed code: " + code);
                    }
                }
            });
        } else {
            Log.d(TAG, "No Net!!!");
            //从数据库中读取缓存数据
            //15天数据
            Cursor cursor = db.query("WeatherDaily", new String[]{"Status"}, "LocationID=?", new String[]{cityID}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Type collectionType = new TypeToken<List<DailyBean>>() {
                    }.getType();
                    String jsonString = cursor.getString(cursor.getColumnIndex("Status"));
                    _15DBean = new Gson().fromJson(jsonString, collectionType);
                }
                if (_15DBean != null) {
                    sunriseToday = _15DBean.get(0).getSunrise();
                    sunsetToday = _15DBean.get(0).getSunset();
                    update15Days();
                }
                cursor.close();
            } else {
                Log.d(TAG, "NO 15 data");
            }
            cursor = db.query("WeatherNow", new String[]{"AirBean"}, "LocationID=?", new String[]{cityID}, null, null, null);
            if (cursor != null)
                while (cursor.moveToNext()) {
                    String s = cursor.getString(cursor.getColumnIndex("AirBean"));
                    if (s != null)
                        Log.d(TAG, s);
                    else
                        Log.d(TAG, "SSS");
                }

            cursor = db.query("WeatherNow", new String[]{"AirBean", "DayBean"}, "LocationID=?", new String[]{cityID}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Type airBean = new TypeToken<AirNowBean.NowBean>() {
                    }.getType();
                    Type nowBean = new TypeToken<WeatherNowBean.NowBaseBean>() {
                    }.getType();
                    String airString = cursor.getString(cursor.getColumnIndex("AirBean"));
                    Log.d(TAG, "AIR");
                    if (airString == null)
                        Log.d(TAG, "NULLAIR");
                    String nowString = cursor.getString(cursor.getColumnIndex("DayBean"));
                    nowBaseBean = new Gson().fromJson(nowString, nowBean);
                    nowAirBean = new Gson().fromJson(airString, airBean);
                }
                if (nowAirBean != null)
                    updateAir();
                if (nowBaseBean != null) {
                    updateMain();
                }
                cursor.close();
            } else {
                Log.d(TAG, "NO now data");
            }
        }

        arcProgress.setVisibility(View.VISIBLE);
    }


//    void unitTest() {
//        System.out.println("code of gz: " + cities.getCode("广州"));//TEST1
//        //NetUtils.sendInfo("https://devapi.heweather.net/v7/weather/now?location=101010100&key=ff91402a13b144cf8ec6829df147c84f");
//        //getWeatherInfo("CN"+cities.getCode(curCity));//TEST2
//    }

    //更新15天天气
    boolean update15Days() {
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
        startSunAnim(getHourFromTime(sunriseToday), getHourFromTime(sunsetToday));
        return true;
    }

    //首页天气在这里更新
    boolean updateMain() {
        Log.d(TAG, new Gson().toJson(nowBaseBean));
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
//        /*
//        JUST FOR TEST!!!
//        DELETE IT!!!
//         */
//        String[] testWweather = new String[]{"小雨", "中雨", "大雨", "暴雨", "阴", "多云", "晴"};
//        Random random = new Random();
//        weatherL = testWweather[(random.nextInt() % testWweather.length + testWweather.length) % testWweather.length];
//        /*
//        TEST END
//         */
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

    //更新空气信息
    boolean updateAir() {
        String unit = "μg/m³";
        int quality = Integer.parseInt(nowAirBean.getAqi());
        String qualityDescription = "";
        if (quality <= 50) {
            qualityDescription = "优";
        } else if (quality <= 100) {
            qualityDescription = "良";
        } else if (quality <= 150) {
            qualityDescription = "轻度污染";
        } else if (quality <= 200) {
            qualityDescription = "重度污染";
        } else {
            qualityDescription = "严重污染";
        }
        arcProgress.setProgress(quality);
        arcProgress.setBottomText(qualityDescription);
        pm25.setText(nowAirBean.getPm2p5() + unit);
        pm10.setText(nowAirBean.getPm10() + unit);
        so2.setText(nowAirBean.getSo2() + unit);
        no2.setText(nowAirBean.getNo2() + unit);
        nowAirBean.getNo2();

        return true;
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        //如果获取到15天天气
        Log.d(TAG, String.valueOf(msg.what));
        if (msg.what == 15) {
            return update15Days();
        } else if (msg.what == 0) {
            //如果获取到现在天气
            return updateMain();
        } else if (msg.what == 25) {
            //如果获取到空气信息
            return updateAir();
        } else if (msg.what == 1) {
            //如果获取到用户位置
            return updateDefault();
        } else return msg.what == 13;
    }

    boolean updateDefault() {
        //更新default之后更新天气
        city_names.add(defaultCity);
        getWeatherInfo(cities.getCode(defaultCity), defaultCity);
        arrayAdapter.notifyDataSetChanged();
        return true;
    }


    private int getHourFromTime(String time) {
        String hour = time.substring(0, 2);
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
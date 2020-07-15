package com.sysu.sdcs.weatherstation;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetUtils {
    public interface HttpCallbackListener {

        JSONObject onFinish(String response) throws JSONException;

        void onError(Exception e);
    }

    public static void sendHttpRequest(final String address,
                                       final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                System.out.println("Hello World!");
                try {
                    URL url = new URL(address);
                    System.out.println("尝试进入");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);//超时链接时间设置为8秒
                    connection.setReadTimeout(8000);
                    System.out.println("成功了吗");
                    InputStream in = connection.getInputStream();
                    System.out.println("成功了");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    if (listener != null) {
                        // 回调onFinish()方法
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        // 回调onError()方法
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        System.out.println("失败");
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public static JSONObject sendInfo(String addr) {
        sendHttpRequest(addr, new HttpCallbackListener() {
            @Override
            public JSONObject onFinish(String response) throws JSONException {
                Log.d("net", "receive msg" + response);
                return new JSONObject(response);
            }

            @Override
            public void onError(Exception e) {
                Log.d("net", "network error ", e);
            }
        });
        return new JSONObject();
    }
}

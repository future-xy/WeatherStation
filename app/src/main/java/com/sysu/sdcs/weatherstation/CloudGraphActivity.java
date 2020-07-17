package com.sysu.sdcs.weatherstation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class CloudGraphActivity extends AppCompatActivity {

    ActionBar actionBar;
    SeekBar seekBar;
    ImageView imageView;
    RadioGroup radioGroup;
    ConcurrentHashMap<Integer, Bitmap> cg_buffer;
    ArrayList<String> urls;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 0) {
                Bitmap bitmap = (Bitmap) msg.obj;
                imageView.setImageBitmap(bitmap);
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_graph);
        Calendar calendar = Calendar.getInstance();
        actionBar = getSupportActionBar();
        if (actionBar == null)
            Log.d("isNULL", "m");
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(calendar.getTime().toLocaleString() + " " + getResources().getString(R.string.cloud_graph));

        cg_buffer = new ConcurrentHashMap<>();
        imageView = findViewById(R.id.cg_image);
        seekBar = findViewById(R.id.cg_seeker);
        radioGroup = findViewById(R.id.cg_selection);
        radioGroup.check(R.id.true_color);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                renew_urls();
            }
        });

        renew_urls();

        seekBar.setMin(1);
        seekBar.setMax(urls.size());
        seekBar.setProgress(urls.size());
        seekBar.setProgress(urls.size());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                download_disp(urls, seekBar.getMax() - progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (cg_buffer.isEmpty()) return false;
                seekBar.setProgress(seekBar.getMin());
                Timer timer = new Timer();
                for (int i = seekBar.getMin(); i <= seekBar.getMax(); i++) {
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            int pro = seekBar.getProgress();
                            if (pro + 1 <= seekBar.getMax()) {
                                seekBar.setProgress(pro + 1);
                            }
                        }
                    };
                    timer.schedule(task, 500 * (i - seekBar.getMin() + 1));
                }
                return true;
            }
        });
    }

    public void renew_urls() {
        if (!cg_buffer.isEmpty()) {
            cg_buffer.clear();
        }
        Calendar calendar = Calendar.getInstance(TimeZone.GMT_ZONE);
        CG_URL_Handler handler = new CG_URL_Handler(calendar);
        int id;
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.ultra_red:
                id = 1;
                break;
            case R.id.vis_color:
                id = 2;
                break;
            case R.id.steam:
                id = 3;
                break;
            default:
                id = 0;
                break;
        }
        urls = handler.generate(10, id);
        for (int i = urls.size() - 1; i > 0; i--) {
            download_disp(urls, i);
        }
        download_disp(urls, seekBar.getMax() - seekBar.getProgress());
    }

    public void download_disp(ArrayList<String> urls, int progress) {
        final int c_pro = progress;
        if (cg_buffer.containsKey(progress)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = cg_buffer.get(c_pro);
                    assert bitmap != null;
                    Message message = new Message();
                    message.what = 0;
                    message.obj = bitmap;
                    handler.sendMessage(message);
                }
            }).start();
        } else {
            final String target = urls.get(c_pro);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap r_bitmap = getBitmapFromURL(target);
                    if (r_bitmap != null) {
                        Matrix matrix = new Matrix();
                        matrix.setRotate(90, (float) r_bitmap.getWidth() / 2, (float) r_bitmap.getHeight() / 2);
                        Bitmap bitmap = Bitmap.createBitmap(r_bitmap, 0, 0, r_bitmap.getWidth(), r_bitmap.getHeight(), matrix, true);
                        Message message = new Message();
                        message.what = 0;
                        message.obj = bitmap;
                        handler.sendMessage(message);
                        cg_buffer.put(c_pro, bitmap);
                    }
                }
            }).start();
        }
    }

    public Bitmap getBitmapFromURL(final String url_str) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(url_str);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(6000);
            connection.setDoInput(true);
            connection.setUseCaches(true);
            connection.connect();
            InputStream stream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(stream);
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    static class CG_URL_Handler {
        private String[] url_pattern = {"http://image.nmc.cn/product/", "/WXBL/SEVP_NSMC_WXBL_FY4A_ETCC_ACHN_LNO_PY_", "00000.JPG?v="};
        private String[] minutes = {"00", "15", "30", "45"};
        public int year, month, day, c_hour, c_minute;

        CG_URL_Handler(Calendar calendar) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1;
            day = calendar.get(Calendar.DATE);
            c_hour = calendar.get(Calendar.HOUR_OF_DAY);
            c_minute = calendar.get(Calendar.MINUTE) / 15;
        }

        public ArrayList<String> generate(int count, int type) {
            switch (type) {
                case 1:
                    url_pattern[1] = "/WXBL/SEVP_NSMC_WXBL_FY4A_EC012_ACHN_LNO_PY_";
                    break;
                case 2:
                    url_pattern[1] = "/WXBL/medium/SEVP_NSMC_WXBL_FY4A_EC001_ACHN_LNO_PY_";
                    break;
                case 3:
                    url_pattern[1] = "/WXBL/SEVP_NSMC_WXBL_FY4A_EC009_ACHN_LNO_PY_";
                    break;
                default:
                    url_pattern[1] = "/WXBL/SEVP_NSMC_WXBL_FY4A_ETCC_ACHN_LNO_PY_";
                    break;
            }
            return generate(count);
        }

        public ArrayList<String> generate(int count) {
            int i = 0;
            ArrayList<String> res = new ArrayList<>();
            int hour = c_hour, minute = c_minute;
            minute = minute == 0 ? 3 : minute - 1;
            hour = minute == 3 ? hour - 1 : hour;
            while (i < count) {
                if (c_hour < 0) break;
                minute = minute == 0 ? 3 : minute - 1;
                hour = minute == 3 ? hour - 1 : hour;
                Calendar t_calendar = Calendar.getInstance(TimeZone.GMT_ZONE);
                t_calendar.set(year, month, day, hour, minute * 15, 0);
                String builder = url_pattern[0] +
                        String.format("%4d/%02d/%02d", year, month, day) +
                        url_pattern[1] +
                        String.format("%4d%02d%02d%02d%s", year, month, day, hour, minutes[minute]) +
                        url_pattern[2] +
                        t_calendar.getTimeInMillis();
                res.add(builder);
                i++;
            }
            return res;
        }
    }
}
package com.sysu.sdcs.weatherstation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class SettingActivity extends AppCompatActivity {
    private UserInfoDB db; // 数据库
    Button change_password, change_picture, btn_quit;
    ImageView user_picture;
    TextView user_name;
    String current_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar mToolbar = findViewById(R.id.setting_toolbar);
        mToolbar.setTitle("用户中心");
        mToolbar.setBackgroundColor(Color.BLACK);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        db = new UserInfoDB(SettingActivity.this);

        // 获取控件
        change_password = (Button) findViewById(R.id.change_password);
        change_picture = (Button) findViewById(R.id.change_picture);
        btn_quit = (Button) findViewById(R.id.btn_quit);
        user_picture = (ImageView) findViewById(R.id.user_picture);
        user_name = (TextView) findViewById(R.id.user_name);

        init();
    }

    private void init() {
        // 获取当前用户名
        current_name = getSharedPreferences("loginInfo", MODE_PRIVATE).getString("loginUserName", null);

        // 显示用户名
        user_name.setText(current_name);

        // 显示头像
        Cursor cursor = db.query("UserInfo", null, "Name=?", new String[]{current_name}, null);
        cursor.moveToNext();
        byte[] bytes = cursor.getBlob(cursor.getColumnIndex("Picture"));
        if(bytes!=null) {
            // 用户设置了头像
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
            BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
            Drawable drawable = bitmapDrawable;
            user_picture.setBackground(drawable);
        }

        // 修改密码
        change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示输入对话框
                final EditText edit = new EditText(SettingActivity.this);
                edit.setHint("请输入新密码");
                AlertDialog.Builder editDialog = new AlertDialog.Builder(SettingActivity.this);
                editDialog.setTitle("设置密码");
                editDialog.setIcon(R.mipmap.ic_launcher_round);

                //设置dialog布局
                editDialog.setView(edit);

                //设置按钮
                editDialog.setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                editDialog.setPositiveButton("确定"
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String pwd = edit.getText().toString();
                                if(pwd!=null) {
                                    // 更新密码
                                    ContentValues cv = new ContentValues();
                                    cv.put("Password", MD5Utils.md5(pwd));
                                    db.update("UserInfo", cv, "Name=?", new String[]{current_name});
                                    Toast.makeText(SettingActivity.this,
                                            "修改成功！",Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                                else {
                                    // 密码为空
                                    Toast.makeText(SettingActivity.this,
                                            "密码不能为空！",Toast.LENGTH_SHORT).show();
                                }
                            }
                });

                editDialog.create().show();
            }
        });

        // 修改头像
        change_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 激活系统图库，选择一张图片
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                // 开启一个带有返回值的Activity，请求码为1
                startActivityForResult(intent, 1);
            }
        });

        // 退出登录
        btn_quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //loginInfo表示文件名  SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
                SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
                //获取编辑器
                SharedPreferences.Editor editor=sp.edit();
                //退出登录状态
                editor.putBoolean("isLogin", false);
                //提交修改
                editor.commit();

                //销毁当前界面
                SettingActivity.this.finish();
                //跳转到主界面，登录成功的状态传递到 MainActivity 中
                startActivity(new Intent(SettingActivity.this, MainActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1) {
            if(data!=null) {
                // 图片路径
                Uri uri = data.getData();
                Bitmap bitmap = null;
                if (uri != null) {
                    try {
                        bitmap = getBitmapFromUri(this, uri);
                        // 将图片转换为字节流
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                        byte[] bytes =  os.toByteArray();
                        // 将字节流存入数据库
                        ContentValues cv = new ContentValues();
                        cv.put("Picture", bytes);
                        db.update("UserInfo", cv, "Name=?", new String[]{current_name});
                        Toast.makeText(SettingActivity.this,
                                "修改成功！",Toast.LENGTH_SHORT).show();
                    }
                    catch (FileNotFoundException e) {
                        Toast.makeText(SettingActivity.this,
                                "无法找到图片！",Toast.LENGTH_SHORT).show();
                    }
                    catch (IOException e) {
                        Toast.makeText(SettingActivity.this,
                                "输入输出流出现异常！",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static Bitmap getBitmapFromUri(SettingActivity ac, Uri uri) throws FileNotFoundException, IOException {
        InputStream input = ac.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        //图片分辨率以480x800为标准
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        //比例压缩
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//设置缩放比例
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = ac.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return bitmap;//再进行质量压缩
    }
}
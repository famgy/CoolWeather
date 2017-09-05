package com.suninfo.emm.coolweather.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.Manifest.permission.INTERNET;

/**
 * Created by famgy on 9/1/17.
 */

public class HttpUtil extends Activity {
    public static void ask_permission(Activity activity, Context context, String permission) {
        // 要申请的权限
//        private String[] permissions = {Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        String[] permissions = {permission};

        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= 23) {

            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(context, permission);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(activity, permissions, 1);
            }

        }
    }

    // Manifest.permission.READ_CALL_LOG
    public static void get_permission(Activity activity, String permission)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                ask_permission(activity, activity, permission);
            }
        }
    }

    public static void sendHttpRequest(final Activity activity, final String address, final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (null != activity)
                {
                    get_permission(activity, Manifest.permission.INTERNET);
                    get_permission(activity, Manifest.permission.ACCESS_NETWORK_STATE);
                }

                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    if (listener != null) {
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}

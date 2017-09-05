package com.suninfo.emm.coolweather.util;

/**
 * Created by famgy on 9/1/17.
 */

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}

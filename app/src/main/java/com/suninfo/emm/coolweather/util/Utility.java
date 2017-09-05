package com.suninfo.emm.coolweather.util;

/**
 * Created by famgy on 9/1/17.
 */

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.suninfo.emm.coolweather.db.CoolWeatherDB;
import com.suninfo.emm.coolweather.model.City;
import com.suninfo.emm.coolweather.model.County;
import com.suninfo.emm.coolweather.model.Province;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class Utility {

    /*
    http://flash.weather.com.cn/wmaps/xml/china.xml

    <china dn="day">
        <city quName="黑龙江" pyName="heilongjiang" cityname="哈尔滨" state1="4" state2="4" stateDetailed="雷阵雨" tem1="25" tem2="18" windState="南风4-5级转3-4级"/>
        <city quName="吉林" pyName="jilin" cityname="长春" state1="4" state2="1" stateDetailed="雷阵雨转多云" tem1="25" tem2="18" windState="西南风4-5级转3-4级"/>
    </china>
    */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB, String response) {
        boolean bHasValue = false;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(response));
            int eventType = xmlPullParser.getEventType();
            String quName = "";
            String pyName = "";
            String cityName = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = xmlPullParser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG: {
                        if ("city".equals(nodeName)) {
                            quName = xmlPullParser.getAttributeValue(0);
                            pyName = xmlPullParser.getAttributeValue(1);
                            cityName = xmlPullParser.getAttributeValue(2);
                        }

                        break;
                    }
                    case XmlPullParser.END_TAG: {
                        if ("city".equals(nodeName)) {
                            Province province = new Province();
                            province.setProvinceName(quName);
                            province.setProvinceCode(pyName);

                            // 将解析出来的数据存储到Province表
                            coolWeatherDB.saveProvince(province);
                            bHasValue = true;
                        }

                        break;
                    }
                    default: {
                        break;
                    }
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bHasValue;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId) {
        boolean bHasValue = false;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(response));
            int eventType = xmlPullParser.getEventType();
            String cityname = "";
            String pyName = "";
            String cityName = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = xmlPullParser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG: {
                        if ("city".equals(nodeName)) {
                            cityname = xmlPullParser.getAttributeValue(2);
                            pyName = xmlPullParser.getAttributeValue(5);
                        }

                        break;
                    }
                    case XmlPullParser.END_TAG: {
                        if ("city".equals(nodeName)) {
                            City city = new City();
                            city.setCityName(cityname);
                            city.setCityCode(pyName);
                            city.setProvinceId(provinceId);

                            // 将解析出来的数据存储到Province表
                            coolWeatherDB.saveCity(city);
                            bHasValue = true;
                        }

                        break;
                    }
                    default: {
                        break;
                    }
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bHasValue;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response, int cityId, String cityCode) {
        boolean bHasValue = false;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(response));
            int eventType = xmlPullParser.getEventType();
            String countyName = "";
            String url = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = xmlPullParser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG: {
                        if ("city".equals(nodeName)) {
                            countyName = xmlPullParser.getAttributeValue(2);
                            url = xmlPullParser.getAttributeValue(17);
                        }

                        break;
                    }
                    case XmlPullParser.END_TAG: {
                        if ("city".equals(nodeName)) {
                            County county = new County();
                            county.setCountyName(countyName);
                            county.setCountyCode(url);
                            county.setCityId(cityId);
                            county.setCityCode(cityCode);

                            // 将解析出来的数据存储到Province表
                            coolWeatherDB.saveCounty(county);
                            bHasValue = true;
                        }

                        break;
                    }
                    default: {
                        break;
                    }
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bHasValue;
    }

    /**
     * 解析服务器返回的JSON数据，并将解析出的数据存储到本地。
     */
    public static void handleWeatherResponse(Context context, String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
            String cityName = weatherInfo.getString("city");
            String weatherCode = weatherInfo.getString("cityid");
            String temp1 = weatherInfo.getString("temp1");
            String temp2 = weatherInfo.getString("temp2");
            String weatherDesp = weatherInfo.getString("weather");
            String publishTime = weatherInfo.getString("ptime");
            saveWeatherInfo(context, cityName, weatherCode, temp1, temp2,
                    weatherDesp, publishTime);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析服务器返回的JSON数据，并将解析出的数据存储到本地。
     */
    public static void handleWeatherResponseNew(Context context, String url, String countyName, String stateDetailed, String tem1, String tem2, String temNow) {
        String cityName = countyName;
        String weatherCode = url;
        String temp1 = tem1;
        String temp2 = tem2;
        String weatherDesp = stateDetailed;
        String publishTime = temNow;
        saveWeatherInfo(context, cityName, weatherCode, temp1, temp2,
                weatherDesp, publishTime);
    }

    /**
     * 将服务器返回的所有天气信息存储到SharedPreferences文件中。
     */
    public static void saveWeatherInfo(Context context, String cityName,
                                       String weatherCode, String temp1, String temp2, String weatherDesp,
                                       String publishTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();
    }

}
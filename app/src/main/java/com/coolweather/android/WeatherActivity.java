package com.coolweather.android;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Aqi;
import com.coolweather.android.gson.AqiWeather;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Lifestyle;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.Util;

public class WeatherActivity extends AppCompatActivity {
    private static final String TAG = "WeatherActivity";

    private ScrollView weatherLayout;
    private LinearLayout forecastLayout;

    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private ImageView bingPicImg;
    private ImageView infoImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        //初始化各控件
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);

        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        infoImg = (ImageView) findViewById(R.id.pic_info);
        //先从本地缓存中读取数据
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        String aqiWeatherString = prefs.getString("aqiWeather",null);
        String bingPic = prefs.getString("bing_pic", null);


        if(bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

        if(weatherString != null){
            //有缓存直接解析
            AqiWeather aqiWeather = Utility.handleAqiWeatherResponse(aqiWeatherString);
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
            showAqiWeatherInfo(aqiWeather);
        }else{
            //无缓存时去服务器查询天气
            //从选择城市活动通过Intent传值到显示天气活动 在此处从intent从取出天气id
            String weatherId = getIntent().getStringExtra("weather_id");
            //隐藏ScrollView 因为此时界面没有数据，看着很奇怪。
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
            requestAqiWeather(weatherId);
        }
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                Log.d(TAG, "picurl="+bingPic);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                        (WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 处理并展示aqi数据
     * @param aqiWeather
     */
    private void showAqiWeatherInfo(AqiWeather aqiWeather) {
        String aqi = aqiWeather.aqicla.aqi;
        aqiText.setText(aqi);
        String pm25 = aqiWeather.aqicla.pm25;
        pm25Text.setText(pm25);
    }

    /**
     * 处理并展示Weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
//        Drawable drawable = getResources().getDrawable(R.mipmap.cloud);
//        drawable.setBounds(-8,0,58,53);
//        weatherInfoText.setCompoundDrawables(drawable,null,null,null);
        String codetxt = weather.now.info_code;

        switch (codetxt)
        {
            case "100n":
                infoImg.setColorFilter(Color.WHITE);
                Glide.with(WeatherActivity.this).load(R.mipmap.icon_100n).into(infoImg);
                break;
            case "100":
                infoImg.setColorFilter(Color.WHITE);
                Glide.with(WeatherActivity.this).load(R.mipmap.icon_100).into(infoImg);
                break;
            case "101":
                infoImg.setColorFilter(Color.WHITE);
                Glide.with(WeatherActivity.this).load(R.mipmap.icon_101).into(infoImg);
                break;
            case "102":
                infoImg.setColorFilter(Color.WHITE);
                Glide.with(WeatherActivity.this).load(R.mipmap.icon_102).into(infoImg);
                break;
            case "103":
                infoImg.setColorFilter(Color.WHITE);
                Glide.with(WeatherActivity.this).load(R.mipmap.icon_103).into(infoImg);
                break;
            case "103n":
                infoImg.setColorFilter(Color.WHITE);
                Glide.with(WeatherActivity.this).load(R.mipmap.icon_103n).into(infoImg);
                break;
            case "104":
                infoImg.setColorFilter(Color.WHITE);
                Glide.with(WeatherActivity.this).load(R.mipmap.icon_104).into(infoImg);
                break;
            case "104n":
                infoImg.setColorFilter(Color.WHITE);
                Glide.with(WeatherActivity.this).load(R.mipmap.icon_104n).into(infoImg);
                break;
            default:
                infoImg.setColorFilter(Color.WHITE);
                Glide.with(WeatherActivity.this).load(R.mipmap.icon_999).into(infoImg);
                break;
        }


        forecastLayout.removeAllViews();
        for(int i = 0; i < 3; i++){
            Forecast forecast = weather.forecastList.get(i);
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    forecastLayout, false);
            TextView dateText =(TextView) view.findViewById(R.id.date_text);
            TextView infoText =(TextView) view.findViewById(R.id.info_text);
            TextView  maxText =(TextView) view.findViewById(R.id.max_text);
            TextView  minText =(TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.info);
            maxText.setText(forecast.max);
            minText.setText(forecast.min);
            forecastLayout.addView(view);

        }
        Lifestyle lifestyle = weather.lifestyleList.get(0);
        String comfort = "舒适度：" + lifestyle.suggestionBrief +"\n"+ lifestyle.info;
        Lifestyle lifestyle2 = weather.lifestyleList.get(6);
        String washCar = "洗车指数：" + lifestyle2.suggestionBrief + "\n"+lifestyle2.info;
        Lifestyle lifestyle3 = weather.lifestyleList.get(3);
        String sport = "运动建议：" + lifestyle3.suggestionBrief + "\n"+lifestyle3.info;
        comfortText.setText(comfort);
        carWashText.setText(washCar);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 根据天气id请求aqi信息
     * @param weatherId
     */
    private void requestAqiWeather(String weatherId) {
        String weatherUrl = "https://free-api.heweather.com/s6/air/now?location=" +
                weatherId + "&key=cf85da83456047b2a28ce9b359129635";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取AQI信息失败",Toast
                                .LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final AqiWeather aqiWeather = Utility.handleAqiWeatherResponse(responseText);
                //现在得到了解析后的aqiweather 就回到主线程 并判断
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(aqiWeather != null && "ok".equals(aqiWeather.status)){
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("aqiWeather",responseText);
                            editor.apply();
                            showAqiWeatherInfo(aqiWeather);
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取AQI信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 根据天气id请求城市天气信息
     * @param weatherId
     */
    private void requestWeather(String weatherId) {
        String weatherUrl = "https://free-api.heweather.com/s6/weather?location=" +
                weatherId + "&key=cf85da83456047b2a28ce9b359129635";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast
                        .LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                Log.d(TAG, "cityname="+weather.basic.cityName);
                //现在得到了解析后的weather 就回到主线程 并判断
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        loadBingPic();
    }
}

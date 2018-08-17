package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class AqiWeather {
    @SerializedName("air_now_city")
    public Aqi aqicla;

    public String status;
}

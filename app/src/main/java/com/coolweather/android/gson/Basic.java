package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    //将JSON字段与JAVA字段建立映射关系
    @SerializedName("location")
    public String cityName;

    @SerializedName("cid")
    public String weatherId;

}

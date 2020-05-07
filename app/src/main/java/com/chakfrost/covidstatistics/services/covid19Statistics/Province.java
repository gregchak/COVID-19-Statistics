package com.chakfrost.covidstatistics.services.covid19Statistics;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Province
{
    @SerializedName("iso")
    public String ISO;

    @SerializedName("name")
    public String Name;

    @SerializedName("province")
    public String Province;

    @SerializedName("lat")
    public String Lattitude;

    @SerializedName("long")
    public String Longitude;

    @SerializedName("cities")
    public List<City> Cities;
}

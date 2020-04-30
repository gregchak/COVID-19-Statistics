package com.chakfrost.covidstatistics.services.covidApi;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class CountryStatistics
{
    @SerializedName("Country")
    public String Name;

    @SerializedName("CountryCode")
    public String Code;
    public String Slug;

    public int NewConfirmed;
    public int TotalConfirmed;
    public int NewDeaths;
    public int TotalDeaths;
    public int NewRecovered;
    public int TotalRecovered;

    public Date StatusDate;
}

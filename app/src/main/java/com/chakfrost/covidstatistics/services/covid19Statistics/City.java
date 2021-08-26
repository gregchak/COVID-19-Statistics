package com.chakfrost.covidstatistics.services.covid19Statistics;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class City
{
    @SerializedName("name")
    public String Name;

    @SerializedName("date")
    public Date StatusDate;

    @SerializedName("fips")
    public String Fips;

    @SerializedName("confirmed")
    public int Confirmed;

    @SerializedName("deaths")
    public int Deaths;

    @SerializedName("confirmed_diff")
    public int ConfirmedDiff;

    @SerializedName("deaths_diff")
    public int DeathsDiff;

    @SerializedName("last_update")
    public Date LastUpdate;

}

package com.chakfrost.covidstatistics.services.covid19Statistics;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ReportStatistics
{
    @SerializedName("date")
    public Date date;

    @SerializedName("confirmed")
    public int confirmed;

    @SerializedName("deaths")
    public int deaths;

    @SerializedName("recovered")
    public int recovered;

    @SerializedName("confirmed_diff")
    public int confirmedDiff;

    @SerializedName("deaths_diff")
    public int deathsDiff;

    @SerializedName("recovered_diff")
    public int recoveredDiff;

    @SerializedName("last_update")
    public Date lastUpdate;

    @SerializedName("active")
    public int active;

    @SerializedName("active_diff")
    public int activeDiff;

    @SerializedName("fatality_rate")
    public double fatalityRate;

    @SerializedName("region")
    public Province region;
}

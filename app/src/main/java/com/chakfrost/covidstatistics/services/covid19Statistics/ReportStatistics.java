package com.chakfrost.covidstatistics.services.covid19Statistics;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ReportStatistics
{
    @SerializedName("date")
    public Date StatusDate;

    @SerializedName("confirmed")
    public int Confirmed;

    @SerializedName("deaths")
    public int Deaths;

    @SerializedName("recovered")
    public int Recovered;

    @SerializedName("confirmed_diff")
    public int ConfirmedDiff;

    @SerializedName("deaths_diff")
    public int DeathsDiff;

    @SerializedName("recovered_diff")
    public int RecoveredDiff;

    @SerializedName("last_update")
    public Date LastUpdate;

    @SerializedName("active")
    public int Active;

    @SerializedName("active_diff")
    public int ActiveDiff;

    @SerializedName("fatality_rate")
    public double FatalityRate;

    @SerializedName("region")
    public Province Region;
}

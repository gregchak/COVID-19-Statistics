package com.chakfrost.covidstatistics.services.covidActNow;

import com.chakfrost.covidstatistics.services.covid19Statistics.ReportStatistics;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class State
{
    public String fips;
    public String country;
    public String state;
    public String county;
    public String level;

    @SerializedName("lat")
    public String latitude;
    public String locationId;

    @SerializedName("long")
    public String longitude;
    public int population;
    public int cdcTransmissionLevel;
    public Date lastUpdatedDate;
    public String url;


    public Metric metrics;
    public RiskLevel riskLevels;
    public Actual actuals;

    public DataAnnotation annotations;


}

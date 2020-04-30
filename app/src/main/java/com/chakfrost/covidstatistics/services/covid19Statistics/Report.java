package com.chakfrost.covidstatistics.services.covid19Statistics;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Report
{
    @SerializedName("data")
    public List<ReportStatistics> Data;
}

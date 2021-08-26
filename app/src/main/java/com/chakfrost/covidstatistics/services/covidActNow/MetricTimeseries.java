package com.chakfrost.covidstatistics.services.covidActNow;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class MetricTimeseries extends Metric
{
    @SerializedName("date")
    public Date metricDate;
}

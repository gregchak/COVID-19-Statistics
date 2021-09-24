package com.chakfrost.covidstatistics.services.covidActNow;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class CdcTransmissionLevelTimeseries
{
    @SerializedName("cdcTransmissionLevel")
    public Integer cdcTransmissionLevel;
    @SerializedName("date")
    public Date transmissionLevelDate;
}

package com.chakfrost.covidstatistics.services.covidActNow;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class AnnotationAnomaly
{
    @SerializedName("date")
    public Date snomalityDate;
    public String type;
    @SerializedName("original_observation")
    public double originalObservation;
}

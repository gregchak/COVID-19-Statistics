package com.chakfrost.covidstatistics.services.covidActNow;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class ActualTimeseries extends Actual
{
    @SerializedName("date")
    public Date actualDate;
}

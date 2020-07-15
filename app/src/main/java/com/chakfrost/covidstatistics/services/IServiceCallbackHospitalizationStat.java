package com.chakfrost.covidstatistics.services;

import com.android.volley.VolleyError;
import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.HospitalizationStat;

public interface IServiceCallbackHospitalizationStat
{
    void onSuccess(HospitalizationStat statistic);

    void onError(VolleyError err);
}

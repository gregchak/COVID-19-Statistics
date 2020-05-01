package com.chakfrost.covidstatistics.services;

import com.android.volley.VolleyError;
import com.chakfrost.covidstatistics.models.CovidStats;

public interface IServiceCallbackCovidStats
{
    void onSuccess(CovidStats statistics);

    void onError(VolleyError err);
}

package com.chakfrost.covidstatistics.services;

import com.android.volley.VolleyError;
import com.chakfrost.covidstatistics.models.GlobalStats;

public interface IServiceCallbackGlobalStats
{
    void onSuccess(GlobalStats stats);

    void onError(VolleyError err);
}

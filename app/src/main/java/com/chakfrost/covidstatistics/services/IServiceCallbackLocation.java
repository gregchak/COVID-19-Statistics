package com.chakfrost.covidstatistics.services;

import com.android.volley.VolleyError;
import com.chakfrost.covidstatistics.models.Location;

interface IServiceCallbackLocation
{
    void onSuccess(Location statistics);

    void onError(VolleyError err);
}

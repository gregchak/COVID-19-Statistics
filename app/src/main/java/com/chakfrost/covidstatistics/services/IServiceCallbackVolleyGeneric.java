package com.chakfrost.covidstatistics.services;

import com.android.volley.VolleyError;

public interface IServiceCallbackVolleyGeneric
{
    <T> void onSuccess(T result);

    void onError(VolleyError err);
}

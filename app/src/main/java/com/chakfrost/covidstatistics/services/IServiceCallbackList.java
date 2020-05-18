package com.chakfrost.covidstatistics.services;

        import com.android.volley.VolleyError;

        import java.util.List;

public interface IServiceCallbackList
{
    <T> void onSuccess(List<T> list);

    void onError(VolleyError err);
}

package com.chakfrost.covidstatistics.services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class CovidRequestQueue
{
    private static CovidRequestQueue instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private CovidRequestQueue(Context context)
    {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized CovidRequestQueue getInstance(Context context) {
        if (instance == null) {
            instance = new CovidRequestQueue(context);
        }
        return instance;
    }

    /**
     * Gets a Singleton RequestQueue
     *
     * @return RequestQueue
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    /**
     * Adds a request to the RequestQueue
     *
     * @param req Request to be added to RequestQueue
     * @param <T> Request Type
     */
    public <T> void addToRequestQueue(Request<T> req)
    {
        getRequestQueue().add(req);
    }
}

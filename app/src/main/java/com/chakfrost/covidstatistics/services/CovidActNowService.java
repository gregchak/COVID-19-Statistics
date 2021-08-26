package com.chakfrost.covidstatistics.services;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.services.covidActNow.ActualTimeseries;
import com.chakfrost.covidstatistics.services.covidActNow.MetricTimeseries;
import com.chakfrost.covidstatistics.services.covidActNow.State;
import com.chakfrost.covidstatistics.services.covidActNow.StateHistory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class CovidActNowService
{
    private static List<StateHistory> cachedData = new ArrayList<>();
    private static final String baseApi = "https://api.covidactnow.org/v2";
    private static final String apiKey = "4c5f2261093742f5b90b3e1ffb2cf621";

    public static void getStateStat(final String state, IServiceCallbackCovidStats callback)
    {
        String url;
        url = MessageFormat.format("{0}/state/{1}.json?apiKey={2}",baseApi, state, apiKey);

        Log.d("CovidActNowService.getStateStat()", url);

        GetCurrent(url, callback);
    }

    public static void getStateHistoryStat(final String state, Calendar dateToCheck, IServiceCallbackCovidStats callback)
    {
        // Find cached data
        StateHistory cache = cachedData.stream()
                .filter(c -> c.state != null && c.state.equals(state) && c.level.equals("state"))
                .findFirst()
                .orElse(null);

        if (cache != null)
        {
            // Process request from cache
            CovidStats result = ProcessServiceResult(cache, dateToCheck);
            callback.onSuccess(result);
            return;
        }
        else
        {
            // Process request by calling service
            String url;
            url = MessageFormat.format("{0}/state/{1}.timeseries.json?apiKey={2}",baseApi, state, apiKey);

            Log.d("CovidActNowService.getStateHistoryStat()", url);

            GetTimeseries(url, dateToCheck, callback);
        }
    }

    public static void getCountyStat(final String countyFips, IServiceCallbackCovidStats callback)
    {
        String url;
        url = MessageFormat.format("{0}/county/{1}.json?apiKey={2}",baseApi, countyFips, apiKey);

        Log.d("CovidActNowService.getCountyStat()", url);

        GetCurrent(url, callback);
    }

    public static void getCountyHistoricalStat(final String countyFips, Calendar dateToCheck, IServiceCallbackCovidStats callback)
    {
        // Find cached data
        StateHistory cache = cachedData.stream()
                .filter(c -> c.fips.equals(countyFips) && c.level.equals("county"))
                .findFirst()
                .orElse(null);

        if (cache != null)
        {
            // Process request from cache
            CovidStats result = ProcessServiceResult(cache, dateToCheck);
            callback.onSuccess(result);
            cache = null;
            return;
        }
        else
        {
            String url;
            url = MessageFormat.format("{0}/county/{1}.timeseries.json?apiKey={2}",baseApi, countyFips, apiKey);

            Log.d("CovidActNowService.getCountyHistoricalStat()", url);

            GetTimeseries(url, dateToCheck, callback);
        }
    }

    public static void getUSStats(IServiceCallbackCovidStats callback)
    {
        String url;
        url = MessageFormat.format("{0}/country/US.json?apiKey={1}",baseApi, apiKey);

        Log.d("CovidActNowService.getUSStats()", url);

        GetCurrent(url, callback);
    }

    public static void getUSHistoricalStats(Calendar dateToCheck, IServiceCallbackCovidStats callback)
    {
        // Find cached data
        StateHistory cache = cachedData.stream()
                .filter(c -> c.fips.equals("0") && c.country.equals("US") && c.level.equals("country"))
                .findFirst()
                .orElse(null);

        if (cache != null)
        {
            // Process request from cache
            CovidStats result = ProcessServiceResult(cache, dateToCheck);
            callback.onSuccess(result);
            return;
        }
        else
        {
            String url;
            url = MessageFormat.format("{0}/country/US.timeseries.json?apiKey={1}",baseApi, apiKey);

            Log.d("CovidActNowService.getUSHistoricalStats()", url);

            GetTimeseries(url, dateToCheck, callback);
        }
    }

    public static void getCountyFips(final String state, final String county, IServiceCallbackVolleyGeneric callback)
    {
        String url;
        url = MessageFormat.format("{0}/county/{1}.json?apiKey={3}", baseApi, state, apiKey);

        Log.d("CovidActNowService.getCountyFips()", url);

        JsonObjectRequest jor = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response ->
                {
                    // Transform response to service object
                    Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    State[] r = g.fromJson(response.toString(), State[].class);
                    
                    // Find county
                    Optional<State> countyFips = Arrays.stream(r)
                            .filter(c -> c.county == county)
                            .findFirst();

                    if (countyFips.isPresent())
                    {
                        callback.onSuccess(countyFips.get().fips);
                    }
                    else
                    {
                        // Add "county" to name and re-search
                        String countyToFind = county.toLowerCase().contains("county") ? county : county + " County";

                        // Find county
                        countyFips = Arrays.stream(r)
                                .filter(c -> c.county == countyToFind)
                                .findFirst();

                        if (countyFips.isPresent())
                        {
                            callback.onSuccess(countyFips.get().fips);
                        }
                        else
                        {
                            callback.onError(new VolleyError("County not found"));    
                        }
                    }
                    return;
                },
                error ->
                {
                    if (!TextUtils.isEmpty(error.getMessage()))
                        VolleyLog.e("CovidActNowService.getStateStat()", error.toString());
                    else
                        VolleyLog.e("CovidActNowService.getStateStat()", "Error occurred while executing CovidService.getStateStat()");

                    Log.e("CovidActNowService.getStateStat()", CovidUtils.formatError(error.getMessage(), error.getStackTrace().toString()));
                    callback.onError(error);
                }
        );

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
    }

    private static void GetCurrent(String url, IServiceCallbackCovidStats callback)
    {
        JsonObjectRequest jor = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response ->
                {
                    // Transform response to service object
                    Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    State r = g.fromJson(response.toString(), State.class);

                    // Process response
                    CovidStats result = ProcessServiceResult(r);
                    callback.onSuccess(result);
                    return;
                },
                error ->
                {
                    if (!TextUtils.isEmpty(error.getMessage()))
                        VolleyLog.e("CovidActNowService.getStateStat()", error.toString());
                    else
                        VolleyLog.e("CovidActNowService.getStateStat()", "Error occurred while executing CovidService.getStateStat()");

                    Log.e("CovidActNowService.getStateStat()", CovidUtils.formatError(error.getMessage(), error.getStackTrace().toString()));
                    callback.onError(error);
                }
        );

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
    }

    private static void GetTimeseries(String url, Calendar dateToCheck, IServiceCallbackCovidStats callback)
    {
        JsonObjectRequest jor = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response ->
                {
                    // Transform response to service object
                    Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    StateHistory r = g.fromJson(response.toString(), StateHistory.class);

                    // Add to cache
                    cachedData.add(r);

                    // Process response
                    CovidStats result = ProcessServiceResult(r, dateToCheck);

                    callback.onSuccess(result);
                    return;
                },
                error ->
                {
                    if (!TextUtils.isEmpty(error.getMessage()))
                        VolleyLog.e("CovidActNowService.getStateHistoryStat()", error.toString());
                    else
                        VolleyLog.e("CovidActNowService.getStateHistoryStat()", "Error occurred while executing CovidActNowService.getStateHistoryStat()");

                    Log.e("CovidActNowService.getStateHistoryStat()", CovidUtils.formatError(error.getMessage(), error.getStackTrace().toString()));
                    callback.onError(error);
                }
        );

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
    }

    private static CovidStats ProcessServiceResult(State response)
    {
        // Set local list
        CovidStats result = new CovidStats();

        result.setStatusDate(response.lastUpdatedDate);
        result.setLastUpdate(response.lastUpdatedDate);

        result.setTotalConfirmed(response.actuals.cases);
        result.settNewConfirmed(response.actuals.newCases);
        result.setTotalDeaths(response.actuals.deaths);
        result.setNewDeaths(response.actuals.newDeaths);

        result.setStatusDate(response.lastUpdatedDate);

        // TODO: Hospitalizations = hospitalBed.currentUsageCovid + icuBeds.currentUsageCovid
        result.setHospitalizationsCurrent(null == response.actuals.hospitalBeds ? 0 : response.actuals.hospitalBeds.currentUsageCovid);
        result.setICUCurrent(null == response.actuals.icuBeds ? 0 : response.actuals.icuBeds.currentUsageCovid);
        result.setPositivityRate(response.metrics.testPositivityRatio * 100);

        result.setCaseDensity(response.metrics.caseDensity);
        result.setCdcTransimssionLevel(response.cdcTransmissionLevel);

        return result;
    }

    private static CovidStats ProcessServiceResult(StateHistory response, Calendar dateToCheck)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        // Get Metric Timeseries for provided date
        MetricTimeseries metric = response.metricsTimeseries.stream()
                .filter(m -> dateFormat.format(m.metricDate.getTime()).equals(dateFormat.format(dateToCheck.getTimeInMillis())))
                .findFirst()
                .orElse(null);

        // Get Actual Timeseries for provided date
        ActualTimeseries actual = response.actualsTimeseries.stream()
                .filter(m -> dateFormat.format(m.actualDate.getTime()).equals(dateFormat.format(dateToCheck.getTimeInMillis())))
                .findFirst()
                .orElse(null);

        // Confirm a Metric and Actual were found
        if (null != metric && null != actual)
        {
            Log.d("CovidActionNowService.ProcessServiceResults()", MessageFormat.format("Processing {0} on {1}", response.state, dateFormat.format(dateToCheck.getTimeInMillis())));
            // Set local list
            CovidStats result = new CovidStats();

            result.setStatusDate(actual.actualDate);
            result.setLastUpdate(actual.actualDate);

            result.setTotalConfirmed(actual.cases);
            result.settNewConfirmed(actual.newCases);
            result.setNewDeaths(actual.newDeaths);
            result.setTotalDeaths(actual.deaths);

            // TODO: Hospitalizations = hospitalBed.currentUsageCovid + icuBeds.currentUsageCovid
            result.setHospitalizationsCurrent(null == actual.hospitalBeds ? 0 : actual.hospitalBeds.currentUsageCovid);
            result.setICUCurrent(null == actual.icuBeds ? 0 : actual.icuBeds.currentUsageCovid);
            result.setPositivityRate(metric.testPositivityRatio * 100);

            result.setCaseDensity(metric.caseDensity);
            result.setCdcTransimssionLevel(-1);

            metric = null;
            actual = null;
            return result;
        }
        else
        {
            Log.e("CovidActNowService.ProcessServiceResult()", "metric and/or actual not found");
            return null;
        }
    }
}

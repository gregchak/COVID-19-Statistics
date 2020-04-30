package com.chakfrost.covidstatistics.services;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.models.Country;
import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.GlobalStats;
import com.chakfrost.covidstatistics.models.Province;
import com.chakfrost.covidstatistics.services.covid19Statistics.Municipality;
import com.chakfrost.covidstatistics.services.covid19Statistics.Provinces;
import com.chakfrost.covidstatistics.services.covid19Statistics.Region;
import com.chakfrost.covidstatistics.services.covid19Statistics.Regions;
import com.chakfrost.covidstatistics.services.covid19Statistics.Report;
import com.chakfrost.covidstatistics.services.covid19Statistics.ReportStatistics;
import com.chakfrost.covidstatistics.services.covidApi.Summary;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;


@Singleton
public class CovidService
{
    private static final String COVID_19_API_URL = "https://api.covid19api.com";
    private static final String COVID_19_STATISTICS_URL = "https://covid-19-statistics.p.rapidapi.com";
    private static final String RAPID_KEY_COVID_19_STATISTICS = "f1c4b33103mshe843fad99e58b9ep1ae933jsnd1143d9bcb8f";

    /**
     * Gets countries from service
     *
     * @param callback  The callback method(s) called after data is received from the service
     */
    public static void countries(final IServiceCallbackList callback)
    {
       // RequestQueue queue = CovidRequestQueue.getInstance(CovidApplication.getContext()).getRequestQueue();
        String url = MessageFormat.format("{0}/regions", COVID_19_STATISTICS_URL);

        JsonObjectRequest jor = new JsonObjectRequest(
                Request.Method.GET, url, null,

                response ->
                {
                    // Transform response to service object
                    Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    Regions regions = g.fromJson(response.toString(), Regions.class);

                    // Set local list
                    List<Country> countries = new ArrayList<>();

                    // COVID-19 Statistics
                    try
                    {
                        // Loop through data array
                        for (Region r : regions.Data)
                            countries.add(new Country(r.Name, r.ISO));
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }

                    callback.onSuccess(countries);
                },

                error ->
                {
                    VolleyLog.e("CovidService.Countries()", error.toString());
                    //Log.e("onErrorResponse", error.toString());
                    callback.onError(error);
                }
        )
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> params = new HashMap<>();
                params.put("x-rapidapi-key", RAPID_KEY_COVID_19_STATISTICS);
                return params;
            }
        };

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
        //queue.add(jor);
    }

    /**
     * Gets provinces/states for a given country
     *
     * @param country   Country object
     * @param callback  The callback method(s) called after data is received from the service
     */
    public static void provinces(final Country country, final IServiceCallbackList callback)
    {
        if (null == country)
            return;

        //RequestQueue queue = CovidRequestQueue.getInstance(CovidApplication.getContext()).getRequestQueue();
        String url = MessageFormat.format("{0}/provinces?iso={1}", COVID_19_STATISTICS_URL, country.getISO2());

        JsonObjectRequest jor = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response ->
                {
                    // Transform response to service object
                    Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    Provinces provinces = g.fromJson(response.toString(), Provinces.class);

                    // Set local list
                    List<Province> result = new ArrayList<>();
                    try
                    {
                        // Loop through data array
                        for (com.chakfrost.covidstatistics.services.covid19Statistics.Province p : provinces.Data)
                        {
                            if (!p.Province.equals(""))
                                result.add(new Province(p.Province.trim(), country));
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    callback.onSuccess(result);
                },
                error ->
                {
                    VolleyLog.e("CovidService.Provinces()", error.toString());
                    //Log.e("onErrorResponse", error.getMessage());
                    callback.onError(error);
                }
        )
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> params = new HashMap<>();
                params.put("x-rapidapi-key", RAPID_KEY_COVID_19_STATISTICS);
                return params;
            }
        };

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
        //queue.add(jor);
    }

    /**
     * Gets cities/municipalities for a given province and/or country
     *
     * @param iso       Country ISO code
     * @param province  Name of the province
     * @param region    Name of the region/country
     * @param callback  The callback method(s) called after data is received from the service
     */
    public static void municipalities(final String iso, final String province, final String region, final IServiceCallbackList callback)
    {
        //RequestQueue queue = CovidRequestQueue.getInstance(CovidApplication.getContext()).getRequestQueue();

        String url;
        if (null == province || province.equals(""))
            url = MessageFormat.format("{0}/reports?iso={1}&region_name={2}", COVID_19_STATISTICS_URL, iso, region);
        else
            url = MessageFormat.format("{0}/reports?iso={1}&region_province={2}&region_name={3}", COVID_19_STATISTICS_URL, iso, province, region);


        JsonObjectRequest jor = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response ->
                {
                    // Set local list
                    List<String> result = new ArrayList<>();
                    try
                    {
                        Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                        Report report = g.fromJson(response.toString(), Report.class);

                        if (report.Data.size() == 0)
                        {
                            callback.onSuccess(null);
                            return;
                        }

                        for(Municipality m: report.Data.get(0).Region.Municipalities)
                            result.add(m.Name);

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.e("CovidSvc.Municipalities", e.toString());
                    }

                    callback.onSuccess(result);
                },
                error ->
                {
                    VolleyLog.e("CovidService.Municipalities()", error.toString());
                    //Log.e("onErrorResponse", error.getMessage());
                    callback.onError(error);
                }
        )
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> params = new HashMap<>();
                params.put("x-rapidapi-key", RAPID_KEY_COVID_19_STATISTICS);
                return params;
            }
        };

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
        //queue.add(jor);
    }

    /**
     * Gets statistics for the given region, province or municipality.  If the return
     * from the service is null, then there are no more stats to be retrieved and subsequent calls
     * for daily report data should be stopped.
     *
     * @param iso           Country ISO code
     * @param province      Name of the province
     * @param region        Name of the region/country
     * @param municipality  Name of teh municipality
     * @param callback      The callback method(s) called after data is received from the service
     */
    public static void report(final String iso, final String province, final String region, final String municipality,
                              final Calendar dateToCheck, final IServiceCallbackCovidStats callback)
    {
        //RequestQueue queue = CovidRequestQueue.getInstance(CovidApplication.getContext()).getRequestQueue();
        String url;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (TextUtils.isEmpty(province) && TextUtils.isEmpty(municipality))
            url = MessageFormat.format("{0}/reports?iso={1}&date={2}",
                    COVID_19_STATISTICS_URL, iso, dateFormat.format(dateToCheck.getTime()));
        else if (!TextUtils.isEmpty(province) && TextUtils.isEmpty(municipality))
            url = MessageFormat.format("{0}/reports?iso={1}&region_name={2}&date={3}&region_province={4}",
                    COVID_19_STATISTICS_URL, iso, region, dateFormat.format(dateToCheck.getTime()), province);
        else
            url = MessageFormat.format("{0}/reports?iso={1}&region_name={2}&date={3}&region_province={4}&city_name={5}",
                    COVID_19_STATISTICS_URL, iso, region, dateFormat.format(dateToCheck.getTime()), province, municipality);

//        if (null == province || province.equals(""))
//            url = MessageFormat.format("{0}/reports?iso={1}&region_name={2}&date={3}",
//                    COVID_19_STATISTICS_URL, iso, region, dateFormat.format(dateToCheck.getTime()));
//        else
//            url = MessageFormat.format("{0}/reports?iso={1}&region_name={2}&date={3}&region_province={4}",
//                    COVID_19_STATISTICS_URL, iso, region, dateFormat.format(dateToCheck.getTime()), province);

        Log.d("CovidService.Report()", url);

        JsonObjectRequest jor = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response ->
                {
                    // Transform response to service object
                    Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                    Report report = g.fromJson(response.toString(), Report.class);

                    // If report has no data, return null
                    // Returning null is an indicator to the caller
                    // that there are no more stats to retrieve
                    if (report.Data.size() == 0)
                    {
                        callback.onSuccess(null);
                        return;
                    }

                    // Set local list
                    CovidStats result = new CovidStats();

                    try
                    {
                        // Loop through data array
                        for(ReportStatistics r: report.Data)
                        {
                            result.setStatusDate(r.StatusDate);

                            if (TextUtils.isEmpty(municipality))
                            {
                                result.setTotalConfirmed(result.getTotalConfirmed() + r.Confirmed);
                                result.setTotalDeaths(result.getTotalDeaths() + r.Deaths);
                                result.setDiffConfirmed(result.getDiffConfirmed() + r.ConfirmedDiff);
                                result.setDiffDeaths(result.getDiffDeaths() + r.DeathsDiff);

                                result.setTotalRecovered(result.getTotalRecovered() + r.Recovered);
                                result.setDiffRecovered(result.getDiffRecovered() + r.RecoveredDiff);

//                                result.TotalConfirmed += r.Confirmed;
//                                result.TotalDeaths += r.Deaths;
//                                result.DiffConfirmed += r.ConfirmedDiff;
//                                result.DiffDeaths += r.DeathsDiff;
//
//                                result.TotalRecovered += r.Recovered;
//                                result.DiffRecovered += r.RecoveredDiff;


                            }
                            else // province and municipality are both not null
                            {
                                Municipality m = r.Region.Municipalities.get(0);
//                                Municipality filtered = r.Region.Municipalities.stream()
//                                        .filter(m -> m.Name.equals(municipality))
//                                        .findFirst()
//                                        .orElse(null);

                                if (null == m)
                                    throw new RuntimeException("Municipality not found");

                                result.setTotalConfirmed(m.Confirmed);
                                result.setTotalDeaths(m.Deaths);
                                result.setDiffConfirmed(m.ConfirmedDiff);
                                result.setDiffDeaths(m.DeathsDiff);

//                                result.TotalConfirmed = r.Confirmed;
//                                result.TotalDeaths = r.Deaths;
//                                result.DiffConfirmed = r.ConfirmedDiff;
//                                result.DiffDeaths = r.DeathsDiff;
                            }
                        }

                    }
                    catch (Exception e)
                    {
                        Log.e("onErrorResponse", e.getMessage());
                        result = null;
                    }

                    callback.onSuccess(result);
                },
                error ->
                {
                    if (!TextUtils.isEmpty(error.getMessage()))
                        VolleyLog.e("CovidService.Report()", error.toString());
                    else
                        VolleyLog.e("CovidService.Report()", "Error occurred while executing CovidService.Report()");
                    //Log.e("onErrorResponse", "Error occurred while executing ConvidService.Report()");
                    callback.onError(error);
                }
        )
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> params = new HashMap<>();
                params.put("x-rapidapi-key", RAPID_KEY_COVID_19_STATISTICS);
                return params;
            }
        };

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
        //queue.add(jor);
    }

    /**
     * Gets summary statistics for global and individual countries
     *
     * @param callback  The callback method(s) called after data is received from the service
     */
    public static void summary(IserviceCallbackGlobalStats callback)
    {
        //RequestQueue queue = CovidRequestQueue.getInstance(CovidApplication.getContext()).getRequestQueue();
        String url = MessageFormat.format("{0}/summary", COVID_19_API_URL);

        JsonObjectRequest jor = new JsonObjectRequest(
                Request.Method.GET, url, null,

                response ->
                {
                    // Transform response to service object
                    Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
                    Summary summary = g.fromJson(response.toString(), Summary.class);

                    GlobalStats result = new GlobalStats();

                    result.setStatusDate(new Date());
                    result.setNewConfirmed(summary.Global.NewConfirmed);
                    result.setTotalConfirmed(summary.Global.TotalConfirmed);
                    result.setNewDeaths(summary.Global.NewDeaths);
                    result.setTotalDeaths(summary.Global.TotalDeaths);
                    result.setNewRecovered(summary.Global.NewRecovered);
                    result.setTotalRecovered(summary.Global.TotalRecovered);

//                    result.StatusDate = new Date();
//                    result.NewConfirmed = summary.Global.NewConfirmed;
//                    result.TotalConfirmed = summary.Global.TotalConfirmed;
//                    result.NewDeaths = summary.Global.NewDeaths;
//                    result.TotalDeaths = summary.Global.TotalDeaths;
//                    result.NewRecovered = summary.Global.NewRecovered;
//                    result.TotalRecovered = summary.Global.TotalRecovered;

                    callback.onSuccess(result);
                },

                error ->
                {
                    VolleyLog.e("CovidService.Summary()", error.toString());
                    //Log.e("onErrorResponse", error.toString());
                    callback.onError(error);
                }
        );

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
        //queue.add(jor);
    }
}



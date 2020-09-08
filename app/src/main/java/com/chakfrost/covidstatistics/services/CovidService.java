package com.chakfrost.covidstatistics.services;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.models.Country;
import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.GlobalStats;
import com.chakfrost.covidstatistics.models.HospitalizationStat;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.models.Province;
import com.chakfrost.covidstatistics.services.covid19Statistics.*;
import com.chakfrost.covidstatistics.services.covid19Statistics.Provinces;
import com.chakfrost.covidstatistics.services.covid19Statistics.Region;
import com.chakfrost.covidstatistics.services.covid19Statistics.Regions;
import com.chakfrost.covidstatistics.services.covid19Statistics.Report;
import com.chakfrost.covidstatistics.services.covid19Statistics.ReportStatistics;
import com.chakfrost.covidstatistics.services.covid19Statistics.ReportsTotal;
import com.chakfrost.covidstatistics.services.covidApi.Summary;
import com.chakfrost.covidstatistics.services.covidTracking.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class CovidService
{
    private static int errorCounter;
    private static int callCounter;
    private static final String COVID_19_API_URL = "https://api.covid19api.com";
    private static final String COVID_API_URL = "https://covid-api.com/api";
    private static final String COVID_TRACKING_API_URL = "https://api.covidtracking.com/api/v1";
    private static final String COVID_SUMMARY_TO_USE = "COVID_API_URL";
    private static final String THE_VIRUS_TRACKER = "https://api.thevirustracker.com";
    //private static final String COVID_19_STATISTICS_URL = "https://covid-19-statistics.p.rapidapi.com";
    //private static final String RAPID_KEY_COVID_19_STATISTICS = "2b0656f909mshf12452ea67727c5p1cdac2jsn392ca7d9ed82";

    /**
     * Gets countries from service
     *
     * @param callback  The callback method(s) called after data is received from the service
     */
    public static void countries(final IServiceCallbackList callback)
    {
        String url = MessageFormat.format("{0}/regions", COVID_API_URL);

        JsonObjectRequest jor = new JsonObjectRequest(
                Request.Method.GET, url, null,

                response ->
                {
                    // Transform response to service object
                    Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    Regions regions = g.fromJson(response.toString(), Regions.class);

                    // Set local list
                    List<Country> countries = new ArrayList<>();

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
                    VolleyLog.e("CovidService.countries()", error.toString());
                    Log.e("CovidService.countries()", CovidUtils.formatError(error.getMessage(), error.getStackTrace().toString()));
                    callback.onError(error);
                }
        )
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> params = new HashMap<>();
                //params.put("x-rapidapi-key", RAPID_KEY_COVID_19_STATISTICS);
                return params;
            }
        };

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
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

        String url = MessageFormat.format("{0}/provinces/{1}", COVID_API_URL, country.getISO2());

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
                    VolleyLog.e("CovidService.provinces()", error.toString());
                    Log.e("CovidService.provinces()", CovidUtils.formatError(error.getMessage(), error.getStackTrace().toString()));
                    callback.onError(error);
                }
        )
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> params = new HashMap<>();
                //params.put("x-rapidapi-key", RAPID_KEY_COVID_19_STATISTICS);
                return params;
            }
        };

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
    }

    /**
     * Gets cities/municipalities for a given province and/or country
     *
     * @param iso       Country ISO code
     * @param province  Name of the province
     * @param region    Name of the region/country
     * @param callback  The callback method(s) called after data is received from the service
     */
    public static void municipalities(final String iso, final String province,
                                      final String region, final IServiceCallbackList callback) {
        String url;
        if (null == province || province.equals(""))
            url = MessageFormat.format("{0}/reports?iso={1}&region_name={2}", COVID_API_URL, iso, region);
        else
            url = MessageFormat.format("{0}/reports?iso={1}&region_province={2}&region_name={3}", COVID_API_URL, iso, province, region);


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

                        for(City m: report.Data.get(0).region.Cities)
                            result.add(m.Name);

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.e("CovidSvc.Municipalities", CovidUtils.formatError(e.getMessage(), e.getStackTrace().toString()));
                    }

                    callback.onSuccess(result);
                },
                error ->
                {
                    VolleyLog.e("CovidService.municipalities()", error.toString());
                    Log.e("CovidService.municipalities()", CovidUtils.formatError(error.getMessage(), error.getStackTrace().toString()));
                    callback.onError(error);
                }
        )
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> params = new HashMap<>();
                //params.put("x-rapidapi-key", RAPID_KEY_COVID_19_STATISTICS);
                return params;
            }
        };

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
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
        String url;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        url = MessageFormat.format("{0}/reports?iso={1}",COVID_API_URL, iso);
        if (!TextUtils.isEmpty(region))
            url = MessageFormat.format("{0}&region_name={1}", url, region);
        if (!TextUtils.isEmpty(province))
            url = MessageFormat.format("{0}&region_province={1}", url, province);
        if (!TextUtils.isEmpty(municipality))
            url = MessageFormat.format("{0}&city_name={1}", url, municipality);
        if (null != dateToCheck)
            url = MessageFormat.format("{0}&date={1}", url, dateFormat.format(dateToCheck.getTime()));


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
                            result.setStatusDate(r.date);
                            result.setLastUpdate(r.lastUpdate);

                            if (TextUtils.isEmpty(municipality))
                            {
                                result.setTotalConfirmed(result.getTotalConfirmed() + r.confirmed);
                                result.setTotalDeaths(result.getTotalDeaths() + r.deaths);
                                result.setDiffConfirmed(result.getDiffConfirmed() + r.confirmedDiff);
                                result.setDiffDeaths(result.getDiffDeaths() + r.deathsDiff);

                                result.setTotalRecovered(result.getTotalRecovered() + r.recovered);
                                result.setDiffRecovered(result.getDiffRecovered() + r.recoveredDiff);

                                result.setTotalActive(result.getTotalActive() + r.active);
                                result.setDiffActive(result.getDiffActive() + r.activeDiff);

                                result.setFatalityRate(r.fatalityRate);
                            }
                            else // province and municipality are both not null
                            {
                                City c = r.region.Cities.get(0);
    //                                Municipality filtered = r.Region.Municipalities.stream()
    //                                        .filter(m -> m.Name.equals(municipality))
    //                                        .findFirst()
    //                                        .orElse(null);

                                if (null == c)
                                    throw new RuntimeException("Municipality not found");

                                result.setTotalConfirmed(c.Confirmed);
                                result.setTotalDeaths(c.Deaths);
                                result.setDiffConfirmed(c.ConfirmedDiff);
                                result.setDiffDeaths(c.DeathsDiff);
                            }
                        }

                    }
                    catch (Exception e)
                    {
                        Log.e("CovidService.report()", CovidUtils.formatError(e.getMessage(), e.getStackTrace().toString()));
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
                    Log.e("CovidService.report()", CovidUtils.formatError(error.getMessage(), error.getStackTrace().toString()));
                    callback.onError(error);
                }
        )
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> params = new HashMap<>();
                //params.put("x-rapidapi-key", RAPID_KEY_COVID_19_STATISTICS);
                return params;
            }
        };

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
    }

    /**
     * Gets statistics for the given region, province or municipality.  If the return
     * from the service is null, then there are no more stats to be retrieved and subsequent calls
     * for daily report data should be stopped.
     *
     * @param location      Location for which to get statistics
     * @param dateToCheck   Date for which to get statistics
     * @param callback      The callback method(s) called after data is received from the service
     */
    public static void report(@NotNull final Location location, @NotNull final Calendar dateToCheck,
                              @NotNull final IServiceCallbackCovidStats callback)
    {
        String url;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        url = MessageFormat.format("{0}/reports?iso={1}",COVID_API_URL, location.getIso());

        if (location.isCountry())
        {
            url = MessageFormat.format("{0}&region_name={1}", url, location.getRegion());
        }
        else if (location.isProvince())
        {
            url = MessageFormat.format("{0}&region_province={1}", url, location.getProvince());
        }
        else if (location.isMunicipality())
        {
            url = MessageFormat.format("{0}&region_province={1}&city_name={2}", url,
                    location.getProvince(), location.getMunicipality());
        }


        if (null != dateToCheck)
            url = MessageFormat.format("{0}&date={1}", url, dateFormat.format(dateToCheck.getTime()));


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
                            result.setStatusDate(r.date);
                            result.setLastUpdate(r.lastUpdate);

                            //if (TextUtils.isEmpty(municipality))
                            if (!location.isMunicipality())
                            {
                                result.setTotalConfirmed(result.getTotalConfirmed() + r.confirmed);
                                result.setTotalDeaths(result.getTotalDeaths() + r.deaths);
                                result.setDiffConfirmed(result.getDiffConfirmed() + r.confirmedDiff);
                                result.setDiffDeaths(result.getDiffDeaths() + r.deathsDiff);

                                result.setTotalRecovered(result.getTotalRecovered() + r.recovered);
                                result.setDiffRecovered(result.getDiffRecovered() + r.recoveredDiff);

                                result.setTotalActive(result.getTotalActive() + r.active);
                                result.setDiffActive(result.getDiffActive() + r.activeDiff);

                                result.setFatalityRate(r.fatalityRate);
                            }
                            else // province and municipality are both not null
                            {
                                City c = r.region.Cities.get(0);
                                //                                Municipality filtered = r.Region.Municipalities.stream()
                                //                                        .filter(m -> m.Name.equals(municipality))
                                //                                        .findFirst()
                                //                                        .orElse(null);

                                if (null == c)
                                    throw new RuntimeException("Municipality not found");

                                result.setTotalConfirmed(c.Confirmed);
                                result.setTotalDeaths(c.Deaths);
                                result.setDiffConfirmed(c.ConfirmedDiff);
                                result.setDiffDeaths(c.DeathsDiff);
                            }
                        }

                    }
                    catch (Exception e)
                    {
                        Log.e("onErrorResponse", CovidUtils.formatError(e.getMessage(), e.getStackTrace().toString()));
                        result = null;
                    }

                    callback.onSuccess(result);
                },
                error ->
                {
                    if (!TextUtils.isEmpty(error.getMessage()))
                        VolleyLog.e("CovidService.report()", error.toString());
                    else
                        VolleyLog.e("CovidService.report()", "Error occurred while executing CovidService.Report()");

                    Log.e("CovidService.report()", CovidUtils.formatError(error.getMessage(), error.getStackTrace().toString()));
                    callback.onError(error);
                }
        )
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> params = new HashMap<>();
                //params.put("x-rapidapi-key", RAPID_KEY_COVID_19_STATISTICS);
                return params;
            }
        };

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
    }

    /**
     * Gets a CovidStat via callback method based on Location and Calendar date
     *
     * @param location              Location for which to get statistics
     * @param dateToUse             Calendar date for which to get statistics for given Location
     * @param hospitalizationStats  List of HospitalizationStat for a given US State
     * @param callback              Callback method that will return a CovidStat
     */
    public static void getCovidStat(Location location, Calendar dateToUse,
                                    List<HospitalizationStat> hospitalizationStats, IServiceCallbackCovidStats callback)
    {
        // Get report statistics
        //report(location.getIso(), location.getProvince(), location.getRegion(), location.getMunicipality(), dateToUse, new IServiceCallbackCovidStats()
        report(location, dateToUse, new IServiceCallbackCovidStats()
        {
            @Override
            public void onSuccess(CovidStats stat)
            {
                if (null != stat)
                {
                    // Check for hospitalisation statistics
                    if (null != hospitalizationStats)
                    {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);

                        HospitalizationStat hStat = hospitalizationStats.stream()
                                .filter(s -> s.getDate() == Integer.parseInt(dateFormat.format(dateToUse.getTime())))
                                .findFirst()
                                .orElse(null);

                        if (null != hStat)
                        {
                            // Set Hospitalization stats
                            stat.setHospitalizationsTotal( hStat.getHospitalizedTotal());
                            stat.setHospitalizationsDiff(hStat.getHospitalizedChange());
                            stat.setHospitalizationsCurrent(hStat.getHospitalizedCurrent());
                            stat.setICUCurrent(hStat.getIcuCurrent());
                            stat.setICUTotal(hStat.getIcuTotal());
                        }

                        callback.onSuccess(stat);
                    }
                    else if (!TextUtils.isEmpty(location.getUsStateAbbreviation()))
                    {
                        // This is a US State and HospitalizationStats were not passed
                        getUSStateHospitalizations(location.getUsStateAbbreviation(), dateToUse, new IServiceCallbackHospitalizationStat()
                        {
                            @Override
                            public void onSuccess(HospitalizationStat hStat)
                            {
                                if (null != hStat)
                                {
                                    // Set Hospitalization stats
                                    stat.setHospitalizationsTotal( hStat.getHospitalizedTotal());
                                    stat.setHospitalizationsDiff(hStat.getHospitalizedChange());
                                    stat.setHospitalizationsCurrent(hStat.getHospitalizedCurrent());
                                    stat.setICUCurrent(hStat.getIcuCurrent());
                                    stat.setICUTotal(hStat.getIcuTotal());
                                }

                                callback.onSuccess(stat);
                            }

                            @Override
                            public void onError(VolleyError err)
                            {
                                Log.e("CovidService.getCovidStat().getUSStateHospitalization()", err.toString());
                                Log.e("CovidService.getCovidStat()", CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));
                                callback.onSuccess(stat);
                            }
                        });
                    }
                    else if (CovidUtils.isUS(location))
                    {
                        // This is US and hospitalization Stats were not passed
                        getUSHospitalizations(dateToUse, new IServiceCallbackHospitalizationStat()
                        {
                            @Override
                            public void onSuccess(HospitalizationStat hStat)
                            {
                                if (null != hStat)
                                {
                                    // Set Hospitalization stats
                                    stat.setHospitalizationsTotal( hStat.getHospitalizedTotal());
                                    stat.setHospitalizationsDiff(hStat.getHospitalizedChange());
                                    stat.setHospitalizationsCurrent(hStat.getHospitalizedCurrent());
                                    stat.setICUCurrent(hStat.getIcuCurrent());
                                    stat.setICUTotal(hStat.getIcuTotal());
                                }

                                callback.onSuccess(stat);
                            }

                            @Override
                            public void onError(VolleyError err)
                            {
                                Log.e("CovidService.getCovidStat().getUSHospitalization()", err.toString());
                                Log.e("CovidService.getCovidStat()", CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));
                                callback.onSuccess(stat);
                            }
                        });
                    }
                    else
                    {
                        callback.onSuccess(stat);
                    }
                }
                else
                {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onError(VolleyError err)
            {
                Log.d("CovidService.getCovidStat().report()", err.toString());
                Log.e("CovidService.getCovidStat()", CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));
                callback.onError(err);
            }
        });
    }

    /**
     * Gets summary statistics for global and individual countries
     *
     * @param callback  The callback method(s) called after data is received from the service
     */
    public static void summary(IServiceCallbackGlobalStats callback)
    {
        if (COVID_SUMMARY_TO_USE == "COVID_19_API_URL")
        {
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

                        callback.onSuccess(result);
                    },
                    error ->
                    {
                        VolleyLog.e("CovidService.summary()" + COVID_SUMMARY_TO_USE, error.toString());
                        Log.e("CovidService.summary()", CovidUtils.formatError(error.getMessage(), error.getStackTrace().toString()));
                        callback.onError(error);
                    }
            );

            // Add request to queue
            CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
        }
        else if (COVID_SUMMARY_TO_USE == "COVID_API_URL")
        {

            String url = MessageFormat.format("{0}/reports/total", COVID_API_URL);
            JsonObjectRequest jor = new JsonObjectRequest(
                    Request.Method.GET, url, null,

                    response ->
                    {
                        // Transform response to service object
                        Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                        ReportsTotal totals = g.fromJson(response.toString(), ReportsTotal.class);

                        GlobalStats result = new GlobalStats();

                        result.setStatusDate(totals.data.date);
                        result.setLastUpdate(totals.data.lastUpdate);
                        result.setNewConfirmed(totals.data.confirmedDiff);
                        result.setTotalConfirmed(totals.data.confirmed);
                        result.setNewDeaths(totals.data.deathsDiff);
                        result.setTotalDeaths(totals.data.deaths);
                        result.setNewRecovered(totals.data.recoveredDiff);
                        result.setTotalRecovered(totals.data.recovered);
                        result.setNewActive(totals.data.activeDiff);
                        result.setTotalActive(totals.data.active);
                        result.setFatalityRate(totals.data.fatalityRate);

                        callback.onSuccess(result);
                    },
                    error ->
                    {
                        VolleyLog.e("CovidService.summary()" + COVID_SUMMARY_TO_USE, error.getStackTrace().toString());
                        Log.e("CovidService.summary()", CovidUtils.formatError(error.getMessage(), error.getStackTrace().toString()));
                        callback.onError(error);
                    }
            );

            // Add request to queue
            CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
        }
    }

    /**
     * Gets hospitalization stats for a US for a given date
     *
     * @param dateToCheck   Date for which to get stats
     * @param callback      The callback method(s) called after data is received from the service
     */
    public static void getUSHospitalizations(@NotNull final Calendar dateToCheck,
                                             @NotNull final IServiceCallbackHospitalizationStat callback)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);

        // Create the URI to call
        String url = MessageFormat.format("{0}/us/{1}.json",
                COVID_TRACKING_API_URL,
                dateFormat.format(dateToCheck.getTime()));

        Log.d("COVID Tracking URI", url);

        JsonObjectRequest jor = new JsonObjectRequest(
                Request.Method.GET, url, null,

                response ->
                {
                    // Transform response to service object
                    Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    UnitedStatesStat stat = g.fromJson(response.toString(), UnitedStatesStat.class);

                    HospitalizationStat result = new HospitalizationStat();
                    result.setDate(stat.date);
                    result.setHospitalizedTotal(null == stat.hospitalizedCumulative ? 0 : stat.hospitalizedCumulative);
                    result.setHospitalizedCurrent(null == stat.hospitalizedCurrently ? 0 : stat.hospitalizedCurrently);
                    result.setHospitalizedChange(null == stat.hospitalizedIncrease ? 0 : stat.hospitalizedIncrease);
                    result.setIcuTotal(null == stat.inIcuCumulative ? 0 : stat.inIcuCumulative);
                    result.setIcuCurrent(null == stat.inIcuCurrently ? 0 : stat.inIcuCurrently);

                    callback.onSuccess(result);
                },

                error ->
                {
                    VolleyLog.e("CovidService.getUSHospitalization()", error.toString());
                    Log.e("CovidService.getUSHospitalizations()", CovidUtils.formatError(error.getMessage(), error.getStackTrace().toString()));
                    callback.onError(error);
                }
        );

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
    }

    /**
     * Gets all hospitalization stats for a US.  This would typically be called
     * when adding a new location.  This SHOULD NOT be called when looking for new or
     * updated hospitalization stats.
     *
     * @param callback  The callback method(s) called after data is received from the service
     */
    public static void getUSHospitalizations(final IServiceCallbackList callback)
    {
        // Create the URI to call
        String url = MessageFormat.format("{0}/us/daily.json",
                COVID_TRACKING_API_URL);

        Log.d("COVID Tracking URI", url);

        JsonArrayRequest jor = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response ->
                {
                    List<HospitalizationStat> result = new ArrayList<>();
                    try
                    {
                        // Transform response to service object
                        Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                        UnitedStatesStat[] usStats = g.fromJson(response.toString(), UnitedStatesStat[].class);

                        // Create HospitalizationStat for looping
                        HospitalizationStat tempStat;
                        UnitedStatesStat stat;
                        for (int i = 0; i < usStats.length; i++)
                        {
                            // Get current stat
                            stat = usStats[i];

                            // Initialize new HospitalizationStat
                            tempStat = new HospitalizationStat();
                            tempStat.setDate(stat.date);

                            tempStat.setHospitalizedTotal(null == stat.hospitalizedCumulative ? 0 : stat.hospitalizedCumulative);
                            tempStat.setHospitalizedCurrent(null == stat.hospitalizedCurrently ? 0 : stat.hospitalizedCurrently);
                            tempStat.setHospitalizedChange(null == stat.hospitalizedIncrease ? 0 : stat.hospitalizedIncrease);
                            tempStat.setIcuTotal(null == stat.inIcuCumulative ? 0 : stat.inIcuCumulative);
                            tempStat.setIcuCurrent(null == stat.inIcuCurrently ? 0 : stat.inIcuCurrently);

                            // Add stat to result List
                            result.add(tempStat);
                        }
                    }
                    catch (Exception ex)
                    {
                        Log.e("CovidService.getUSHospitalizations()", CovidUtils.formatError(ex.getMessage(), ex.getStackTrace().toString()));
                        callback.onError(new VolleyError(ex));
                    }

                    callback.onSuccess(result);
                },
                error ->
                {
                    VolleyLog.e("CovidService.getUSHospitalization()", error.toString());

                    Log.e("CovidService.getUSHospitalizations()",
                            CovidUtils.formatError(error.getMessage(), error.getStackTrace().toString()));
                    callback.onError(error);
                }
        );

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
    }

    /**
     * Gets hospitalization stats for a US state for a given date
     *
     * @param abbreviation      Two character US state abbreviation
     * @param dateToCheck   Date for which to get stats
     * @param callback      The callback method(s) called after data is received from the service
     */
    public static void getUSStateHospitalizations(@NotNull final String abbreviation,
                                                  @NotNull final Calendar dateToCheck,
                                                  @NotNull final IServiceCallbackHospitalizationStat callback)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);

        // Create the URI to call
        String url = MessageFormat.format("{0}/states/{1}/{2}.json",
                COVID_TRACKING_API_URL,
                abbreviation.toLowerCase(),
                dateFormat.format(dateToCheck.getTime()));

        Log.d("COVID Tracking URI", url);

        JsonObjectRequest jor = new JsonObjectRequest(
                Request.Method.GET, url, null,

                response ->
                {
                    // Transform response to service object
                    Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    StateStat stat = g.fromJson(response.toString(), StateStat.class);

                    HospitalizationStat result = new HospitalizationStat();
                    result.setDate(stat.date);

                    try
                    {
                        result.setHospitalizedTotal(null == stat.hospitalizedCumulative ? 0 : stat.hospitalizedCumulative);
                        result.setHospitalizedCurrent(null == stat.hospitalizedCurrently ? 0 : stat.hospitalizedCurrently);
                        result.setHospitalizedChange(null == stat.hospitalizedIncrease ? 0 : stat.hospitalizedIncrease);
                        result.setIcuTotal(null == stat.inIcuCumulative ? 0 : stat.inIcuCumulative);
                        result.setIcuCurrent(null == stat.inIcuCurrently ? 0 : stat.inIcuCurrently);
                    }
                    catch (Exception ex)
                    {
                        Log.d("Errors occurred while setting HospitalizationStat", g.toJson(stat));
                        Log.e("CovidService.getUSStateHospitalization()",
                                CovidUtils.formatError(ex.getMessage(), ex.getStackTrace().toString()));
                    }


                    callback.onSuccess(result);
                },

                error ->
                {
                    VolleyLog.e("CovidService.getUSStateHospitalization()", error.toString());
                    Log.e("CovidService.getUSStateHospitalization()",
                            CovidUtils.formatError(error.getMessage(), error.getStackTrace().toString()));
                    callback.onError(error);
                }
        );

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
    }

    /**
     * Gets all hospitalization stats for a US state.  This would typically be called
     * when adding a new location.  This SHOULD NOT be called when looking for new or
     * updated hospitalization stats.
     *
     * @param abbreviation  Two character US state abbreviation
     * @param callback  The callback method(s) called after data is received from the service
     */
    public static void getUSStateHospitalizations(@NotNull final String abbreviation,
                                                  @NotNull final IServiceCallbackList callback)
    {
        // Create the URI to call
        String url = MessageFormat.format("{0}/states/{1}/daily.json",
                                            COVID_TRACKING_API_URL,
                                            abbreviation.toLowerCase());

        Log.d("COVID Tracking URI", url);

        JsonArrayRequest jor = new JsonArrayRequest(
                Request.Method.GET, url, null,

                response ->
                {
                    List<HospitalizationStat> result = new ArrayList<>();
                    try
                    {
                        // Transform response to service object
                        Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                        StateStat[] stateStats = g.fromJson(response.toString(), StateStat[].class);

                        // Create HospitalizationStat for looping
                        HospitalizationStat tempStat;
                        StateStat stat;
                        for (int i = 0; i < stateStats.length; i++)
                        {
                            // Get current stat
                            stat = stateStats[i];

                            // Initialize new HospitalizationStat
                            tempStat = new HospitalizationStat();
                            tempStat.setDate(stat.date);

                            tempStat.setHospitalizedTotal(null == stat.hospitalizedCumulative ? 0 : stat.hospitalizedCumulative);
                            tempStat.setHospitalizedCurrent(null == stat.hospitalizedCurrently ? 0 : stat.hospitalizedCurrently);
                            tempStat.setHospitalizedChange(null == stat.hospitalizedIncrease ? 0 : stat.hospitalizedIncrease);
                            tempStat.setIcuTotal(null == stat.inIcuCumulative ? 0 : stat.inIcuCumulative);
                            tempStat.setIcuCurrent(null == stat.inIcuCurrently ? 0 : stat.inIcuCurrently);

                            // Add stat to result List
                            result.add(tempStat);
                        }

                        // Loop through data array
                        /*for (StateStat stat : stateStats.statistics)
                        {
                            // Initialize new HospitalizationStat
                            tempStat = new HospitalizationStat();
                            tempStat.setDate(stat.date);

                            tempStat.setHospitalizedTotal(null == stat.hospitalizedCumulative ? 0 : stat.hospitalizedCumulative);
                            tempStat.setHospitalizedCurrent(null == stat.hospitalizedCurrently ? 0 : stat.hospitalizedCurrently);
                            tempStat.setHospitalizedChange(null == stat.hospitalizedIncrease ? 0 : stat.hospitalizedIncrease);
                            tempStat.setIcuTotal(null == stat.inIcuCumulative ? 0 : stat.inIcuCumulative);
                            tempStat.setIcuCurrent(null == stat.inIcuCurrently ? 0 : stat.inIcuCurrently);

                            // Add stat to result List
                            result.add(tempStat);
                        }*/
                    }
                    catch (Exception ex)
                    {
                        Log.e("CovidService.getUSStateHospitalization()",
                                CovidUtils.formatError(ex.getMessage(), ex.getStackTrace().toString()));
                        callback.onError(new VolleyError(ex));
                    }

                    callback.onSuccess(result);
                },

                error ->
                {
                    VolleyLog.e("CovidService.getUSStateHospitalization()", error.getStackTrace().toString());
                    Log.e("CovidService.getUSStateHospitalization()", CovidUtils.formatError(error.getMessage(), error.getStackTrace().toString()));
                    callback.onError(error);
                }
        );

        // Add request to queue
        CovidRequestQueue.getInstance(CovidApplication.getContext()).addToRequestQueue(jor);
    }
}



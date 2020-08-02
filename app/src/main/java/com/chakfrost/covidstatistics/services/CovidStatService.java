package com.chakfrost.covidstatistics.services;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.VolleyError;
import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.HospitalizationStat;
import com.chakfrost.covidstatistics.models.Location;

import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CovidStatService
{
    /**
     * Retrieves CovidStats information for a given Location
     * @param location  Location for which to get CovidStats
     * @param callback  Callback method used when operations are complete
     * @return          1 if processing, 0 if not
     */
    public static int getLocationStat(@NotNull Location location, @NotNull IServiceCallbackGeneric callback)
    {
        int retVal;

        Location refreshLocation = new Location();
        refreshLocation.setIso(location.getIso());
        refreshLocation.setCountry(location.getCountry());
        refreshLocation.setProvince(location.getProvince());
        refreshLocation.setMunicipality(location.getMunicipality());
        refreshLocation.setUsStateAbbreviation(location.getUsStateAbbreviation());
        refreshLocation.setRegion(location.getRegion());
        refreshLocation.setStatistics(new ArrayList<>());

        // Set date
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, -1);
        refreshLocation.setLastUpdated(new Date());

        // Load report
        retVal = 1;

        // if US state, ensure the state abbreviation is set
        if (CovidUtils.isUSState(refreshLocation))
        {
            if (TextUtils.isEmpty(refreshLocation.getUsStateAbbreviation()))
                refreshLocation.setUsStateAbbreviation(CovidUtils.getUSStateAbbreviation(refreshLocation));

            if (TextUtils.isEmpty(location.getUsStateAbbreviation()))
            {
                Log.d("CovidStatService.getLocationStat()",
                        MessageFormat.format("Unable to get state abbreviation for {0}",
                                CovidUtils.formatLocation(location)));
                callback.onError(new Error("Unable to determine state abbreviation"));
            }

            // Get stats for US State
            CovidService.getUSStateHospitalizations(location.getUsStateAbbreviation(), new IServiceCallbackList()
            {
                @Override
                public <T> void onSuccess(List<T> list)
                {
                    // Loop dates to back-fill
                    GetStatsForLocation(refreshLocation, startDate, (List<HospitalizationStat>)list, callback);
                }

                @Override
                public void onError(VolleyError err)
                {
                    Log.e("CovidStatService.getLocationStat()",
                            CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));
                }
            });
        }
        else if (CovidUtils.isUS(refreshLocation))
        {
            // Get stats for US
            CovidService.getUSHospitalizations(new IServiceCallbackList()
            {
                @Override
                public <T> void onSuccess(List<T> list)
                {
                    GetStatsForLocation(refreshLocation, startDate, (List<HospitalizationStat>)list, callback);
                }

                @Override
                public void onError(VolleyError err)
                {
                    Log.e("CovidStatService.getLocationStat()",
                            CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));

                    callback.onError(new Error(err));
                }
            });
        }
        else
        {
            GetStatsForLocation(refreshLocation, startDate, null, callback);
        }

        return retVal;
    }

    private static void manualHospitalizationStatBackFill(@NotNull Location location, @NotNull IServiceCallbackGeneric callback)
    {
        long occurrences = location.getStatistics().stream()
                .filter(s -> s.getHospitalizationsCurrent() != 0)
                .count();

        // Get Hospitalization Stats if List is null
        if (occurrences < 10)
        {
            Log.d("CovidStatService.manualHospitalizationStatBackFill()",
                    MessageFormat.format("Back-filling {0}", CovidUtils.formatLocation(location)));

            if (CovidUtils.isUS(location))
            {
                // Get stats for US
                CovidService.getUSHospitalizations(new IServiceCallbackList()
                {
                    @Override
                    public <T> void onSuccess(List<T> list)
                    {
                        // Loop dates to back-fill
                        PopulateHospitalizationStats(location, (List<HospitalizationStat>)list);

                        callback.onSuccess(location);
                    }

                    @Override
                    public void onError(VolleyError err)
                    {
                        Log.e("CovidStatService.manualHospitalizationStatBackFill()",
                                CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));

                        callback.onError(new Error(err));
                    }
                });
            }
            else if (CovidUtils.isUSState(location))
            {
                // Get US State abbreviation if not populated
                if (TextUtils.isEmpty(location.getUsStateAbbreviation()))
                {
                    location.setUsStateAbbreviation(CovidUtils.getUSStateAbbreviation(location));
                    if (TextUtils.isEmpty(location.getUsStateAbbreviation()))
                    {
                        Log.d("CovidStatService.manualHospitalizationStatBackFill()",
                                MessageFormat.format("Unable to get state abbreviation for {0}",
                                        CovidUtils.formatLocation(location)));
                        return;
                    }
                }

                // Get stats for US State
                CovidService.getUSStateHospitalizations(location.getUsStateAbbreviation(), new IServiceCallbackList()
                {
                    @Override
                    public <T> void onSuccess(List<T> list)
                    {
                        // Loop dates to back-fill
                        PopulateHospitalizationStats(location, (List<HospitalizationStat>)list);

                        callback.onSuccess(location);
                    }

                    @Override
                    public void onError(VolleyError err)
                    {
                        Log.e("CovidStatService.manualHospitalizationStatBackFill()",
                                CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));
                    }
                });
            }
        }
    }

    private static void GetStatsForLocation(Location location, Calendar dateToCheck,
                                     List<HospitalizationStat> hospitalizationStats,
                                     IServiceCallbackGeneric callback)
    {
        GetCovidStat(location, dateToCheck, hospitalizationStats, callback);
    }

    private static Location PopulateHospitalizationStats(@NotNull Location location, List<HospitalizationStat> stats)
    {
        // Set date to start getting report
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);

        location.getStatistics().forEach(covidStat ->
        {
            // Convert date to known int format
            String formattedDate = dateFormat.format(covidStat.getStatusDate().getTime());
            int dateAsInt = Integer.parseInt(formattedDate);

            HospitalizationStat hStat = CovidUtils.findHospitalizationStat(stats,  dateAsInt);

            if (null != hStat)
            {
                covidStat.setHospitalizationsTotal(hStat.getHospitalizedTotal());
                covidStat.setHospitalizationsDiff(hStat.getHospitalizedChange());
                covidStat.setHospitalizationsCurrent(hStat.getHospitalizedCurrent());
                covidStat.setICUCurrent(hStat.getIcuCurrent());
                covidStat.setICUTotal(hStat.getIcuTotal());
            }
        });

        return location;
    }

    private static void GetCovidStat(Location location, Calendar dateToCheck, List<HospitalizationStat> hospitalizationStats, IServiceCallbackGeneric callback)
    {
        CovidService.getCovidStat(location, dateToCheck, hospitalizationStats, new IServiceCallbackCovidStats()
        {
            @Override
            public void onSuccess(CovidStats stat)
            {
                ProcessCovidStat(location, stat, dateToCheck, hospitalizationStats, callback);
            }

            @Override
            public void onError(VolleyError err)
            {
                Log.d("CovidStatService.GetCovidStat()", CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));

                callback.onError(new Error(err));
            }
        });
    }

    private static void ProcessCovidStat(Location loc, CovidStats stat, Calendar dateToCheck,
                                  List<HospitalizationStat> hospitalizationStats, IServiceCallbackGeneric callback)
    {
        if (null != stat)
        {
            // Add CovidStat to Location
            loc.getStatistics().add(stat);

            // Get next date to check
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            dateToCheck.add(Calendar.DATE, -1);

            // Check if we already have stats for next dayToCheck
            // If so, stop.  Stats won't change.
            CovidStats found = loc.getStatistics().stream()
                    .filter(s -> dateFormat.format(s.getStatusDate().getTime()).equals(dateFormat.format(dateToCheck.getTimeInMillis())))
                    .findFirst()
                    .orElse(null);

            if (null != found)
            {
                callback.onSuccess(loc);
            }
            else
            {
                // Stat needed for next day
                GetStatsForLocation(loc, dateToCheck, hospitalizationStats, callback);
            }
        }
        else
        {
            callback.onSuccess(loc);
        }
    }
}

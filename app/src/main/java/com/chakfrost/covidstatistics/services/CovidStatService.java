package com.chakfrost.covidstatistics.services;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.VolleyError;
import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.HospitalizationStat;
import com.chakfrost.covidstatistics.models.Location;

import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CovidStatService
{
    static int processCounter = 0;
    static final CovidActNowService covidActNowServiceLocal = CovidActNowService.getInstance();
    static final CovidService covidService = CovidService.getInstance();

    private static CovidStatService single_instance = null;
    public static CovidStatService getInstance()
    {
        if (single_instance == null)
            single_instance = new CovidStatService();

        return single_instance;
    }

    /**
     * Retrieves CovidStats information for a given Location
     * @param placeholderLocation  Location for which to get CovidStats
     * @param callback  Callback method used when operations are complete
     * @return          1 if processing, 0 if not
     */
    public static int getAllLocationStats(@NotNull Location placeholderLocation, @NotNull IServiceCallbackGeneric callback)
    {
        int retVal;

        // Setup new Location object for refresh
        Location newLocation = new Location();
        newLocation.setFips(placeholderLocation.getFips());
        newLocation.setIso(placeholderLocation.getIso());
        newLocation.setCountry(placeholderLocation.getCountry());
        newLocation.setProvince(placeholderLocation.getProvince());
        newLocation.setMunicipality(placeholderLocation.getMunicipality());
        newLocation.setUsStateAbbreviation(placeholderLocation.getUsStateAbbreviation());
        newLocation.setRegion(placeholderLocation.getRegion());
        newLocation.setFips(placeholderLocation.getFips());
        newLocation.setStatistics(new ArrayList<>());

        // Set date
        Calendar startDate = Calendar.getInstance();

        // Set start date at beginning of 2020
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try
        {
            startDate.setTime(sdf.parse("2020-01-01"));
        }
        catch (ParseException e)
        {
            Log.d("CovidStatService.refreshLocationStats()", "Unable to set start date while refreshing a location");
            e.printStackTrace();
            return 0;
        }

        // Load report
        retVal = 1;

        // Use CovidActNowService for US or US States/Counties
        if (CovidUtils.isUSMunicipality(newLocation) || CovidUtils.isUSState(newLocation) || CovidUtils.isUS(newLocation))
        {
            // if US state, ensure the state abbreviation is set
            if (TextUtils.isEmpty(newLocation.getUsStateAbbreviation()) && CovidUtils.isUSState(newLocation))
            {
                newLocation.setUsStateAbbreviation(CovidUtils.getUSStateAbbreviation(newLocation));

                if (TextUtils.isEmpty(placeholderLocation.getUsStateAbbreviation()))
                {
                    Log.d("CovidStatService.refreshLocationStats()",
                            MessageFormat.format("Unable to get state abbreviation for {0}",
                                    CovidUtils.formatLocation(placeholderLocation)));
                    callback.onError(new Error("Unable to determine state abbreviation"));
                }
            }

            // Make sure municipality has fips value
            if (CovidUtils.isUSMunicipality(newLocation) && null == newLocation.getFips())
            {
                covidActNowServiceLocal.getCountyFips(newLocation.getUsStateAbbreviation(), newLocation.getMunicipality(), new IServiceCallbackVolleyGeneric()
                {
                    @Override
                    public <T> void onSuccess(T result)
                    {
                        newLocation.setFips((String)result);

                        GetLocationStats(newLocation, startDate, true, new IServiceCallbackGeneric()
                        {
                            @Override
                            public <T> void onSuccess(T result)
                            {
                                callback.onSuccess(result);
                            }

                            @Override
                            public void onError(Error err)
                            {
                                Log.e("CovidStatService.refreshLocationStats()",
                                        CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));

                                callback.onError(new Error(err));
                            }
                        });
                    }

                    @Override
                    public void onError(VolleyError err)
                    {
                        Log.d("CovidStatService.refreshLocationStats()",
                                MessageFormat.format("Unable to get fips for {0}",
                                        CovidUtils.formatLocation(placeholderLocation)));
                        callback.onError(new Error("Unable to determine fips"));

                    }
                });
            }

            GetLocationStats(newLocation, startDate, true, new IServiceCallbackGeneric()
            {
                @Override
                public <T> void onSuccess(T result)
                {
                    callback.onSuccess(result);
                }

                @Override
                public void onError(Error err)
                {
                    Log.e("CovidStatService.refreshLocationStats()",
                            CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));

                    callback.onError(new Error(err));
                }
            });
        }
        else
        {
            //startDate.add(Calendar.DATE, -1);
            GetStatsForLocation(newLocation, startDate, null, callback);
        }

        return retVal;
    }

    private static void GetStatsForLocation(Location location, Calendar dateToCheck, boolean timeseries,
                                            IServiceCallbackGeneric callback)
    {
        GetLocationStats(location, dateToCheck, timeseries, callback);
    }

    private static void GetStatsForLocation(Location location, Calendar dateToCheck,
                                     List<HospitalizationStat> hospitalizationStats,
                                     IServiceCallbackGeneric callback)
    {
        GetCovidStat(location, dateToCheck, hospitalizationStats, callback);
    }

    private static void GetCovidStat(Location location, Calendar dateToCheck, List<HospitalizationStat> hospitalizationStats, IServiceCallbackGeneric callback)
    {
        covidService.getCovidStat(location, dateToCheck, hospitalizationStats, new IServiceCallbackCovidStats()
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

    private static void ProcessCovidStat(Location loc, CovidStats stat, Calendar dateToCheck, boolean timeseries, IServiceCallbackGeneric callback)
    {
        Calendar current = Calendar.getInstance();
        current.add(Calendar.DAY_OF_YEAR, -1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        if (null != stat)
        {
            Calendar previousDate = Calendar.getInstance();
            previousDate.setTime(dateToCheck.getTime());
            previousDate.add(Calendar.DATE, -1);
            CovidStats previousStat = loc.getStatistics().stream()
                    .filter(s -> dateFormat.format(s.getStatusDate().getTime()).equals(dateFormat.format(previousDate.getTimeInMillis())))
                    .findFirst()
                    .orElse(null);

            // Populate "diff" properties for CovidStat
            if (null != previousStat)
            {
                stat.setDiffConfirmed(Math.abs(stat.getNewConfirmed()- previousStat.getNewConfirmed()));
                stat.setDiffDeaths(Math.abs(stat.getNewDeaths() - previousStat.getNewDeaths()));
                stat.setHospitalizationsDiff(Math.abs(stat.getHospitalizationsCurrent() - previousStat.getHospitalizationsCurrent()));
            }
            else
            {
                stat.setDiffConfirmed(0);
                stat.setDiffDeaths(0);
                stat.setHospitalizationsDiff(0);
            }

            // Add CovidStat to Location
            loc.AddStatistic(stat);

            // Get next date to check
            dateToCheck.add(Calendar.DATE, 1);

            // Check if date is in future.  If so, processing is done, send success
            if (dateToCheck.after(current))
            {
                callback.onSuccess(loc);
                return;
            }
            else
            {
                // Check if there is already a stats for next dayToCheck
                // If so, stop.  Stats won't change.
                CovidStats found = loc.getStatistics().stream()
                        .filter(s -> dateFormat.format(s.getStatusDate().getTime()).equals(dateFormat.format(dateToCheck.getTimeInMillis())))
                        .findFirst()
                        .orElse(null);

                if (null != found)
                {
                    ProcessCovidStat(loc, found, dateToCheck, timeseries, callback);
                }
                else
                {
                    // Stat needed for next day
                    GetStatsForLocation(loc, dateToCheck, timeseries, callback);
                }
            }
        }
        else
        {
            // Get next date to check
            dateToCheck.add(Calendar.DATE, 1);

            if (dateToCheck.after(current))
            {
                callback.onSuccess(loc);
                return;
            }
            else
            {
                // Check if there is already a stats for next dayToCheck
                // If so, stop.  Stats won't change.
                CovidStats found = loc.getStatistics().stream()
                        .filter(s -> dateFormat.format(s.getStatusDate().getTime()).equals(dateFormat.format(dateToCheck.getTimeInMillis())))
                        .findFirst()
                        .orElse(null);

                if (null != found)
                {
                    ProcessCovidStat(loc, found, dateToCheck, timeseries, callback);
                }
                else
                {
                    // Stat needed for next day
                    GetStatsForLocation(loc, dateToCheck, timeseries, callback);
                }
            }
        }
    }

    private static void ProcessCovidStat(Location loc, CovidStats stat, Calendar dateToCheck,
                                  List<HospitalizationStat> hospitalizationStats, IServiceCallbackGeneric callback)
    {
        Calendar current = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        if (null != stat)
        {
            // Add CovidStat to Location
            loc.AddStatistic(stat);
            //loc.getStatistics().add(stat);

            // Get next date to check
            dateToCheck.add(Calendar.DATE, 1);

            // Check if date is in future.  If so, processing is done, send success
            if (dateToCheck.after(current))
            {
                callback.onSuccess(loc);
            }



            // Check if we already have stats for next dayToCheck
            // If so, stop.  Stats won't change.
            CovidStats found = loc.getStatistics().stream()
                    .filter(s -> dateFormat.format(s.getStatusDate().getTime()).equals(dateFormat.format(dateToCheck.getTimeInMillis())))
                    .findFirst()
                    .orElse(null);

            if (null != found)
            {
                ProcessCovidStat(loc, found, dateToCheck, hospitalizationStats, callback);
            }
            else
            {
                // Stat needed for next day
                GetStatsForLocation(loc, dateToCheck, hospitalizationStats, callback);
            }
        }
        else
        {
            //callback.onSuccess(loc);
            dateToCheck.add(Calendar.DATE, 1);
            if (dateToCheck.after(current))
            {
                callback.onSuccess(loc);
            }
            else
            {
                // Check if there is already a stats for next dayToCheck
                // If so, stop.  Stats won't change.
                CovidStats found = loc.getStatistics().stream()
                        .filter(s -> dateFormat.format(s.getStatusDate().getTime()).equals(dateFormat.format(dateToCheck.getTimeInMillis())))
                        .findFirst()
                        .orElse(null);

                if (null != found)
                {
                    ProcessCovidStat(loc, found, dateToCheck, hospitalizationStats, callback);
                }
                else
                {
                    // Stat needed for next day
                    GetStatsForLocation(loc, dateToCheck, hospitalizationStats, callback);
                }
            }
        }
    }

    private static void GetLocationStats(@NotNull Location location, Calendar dateToCheck, @NotNull boolean timeseries, @NotNull IServiceCallbackGeneric callback)
    {
        // If date is not passed set to current date
        if (null == dateToCheck)
        {
            // Set date
            Calendar startDate = Calendar.getInstance();
            dateToCheck = startDate;
        }

        final Calendar dateToUse = dateToCheck;


        if (CovidUtils.isUSMunicipality(location))
        {
            if (timeseries)
            {
                covidActNowServiceLocal.getCountyHistoricalStat(location.getFips(), dateToUse, new IServiceCallbackCovidStats()
                {
                    @Override
                    public void onSuccess(CovidStats statistics)
                    {
                        ProcessCovidStat(location, statistics, dateToUse, true, callback);
                    }

                    @Override
                    public void onError(VolleyError err)
                    {
                        Log.d("CovidStatService.GetLocationStats()", CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));

                        callback.onError(new Error(err));
                    }
                });
            }
            else
            {
                covidActNowServiceLocal.getCountyStat(location.getFips(), new IServiceCallbackCovidStats()
                {
                    @Override
                    public void onSuccess(CovidStats statistics)
                    {
                        ProcessCovidStat(location, statistics, dateToUse, false, callback);
                    }

                    @Override
                    public void onError(VolleyError err)
                    {
                        Log.d("CovidStatService.GetLocationStats()", CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));

                        callback.onError(new Error(err));
                    }
                });
            }

        }
        else if (CovidUtils.isUSState(location))
        {
            if (timeseries)
            {
                covidActNowServiceLocal.getStateHistoryStat(location.getUsStateAbbreviation(), dateToUse, new IServiceCallbackCovidStats()
                {
                    @Override
                    public void onSuccess(CovidStats statistics)
                    {
                        ProcessCovidStat(location, statistics, dateToUse, true, callback);
                    }

                    @Override
                    public void onError(VolleyError err)
                    {
                        Log.d("CovidStatService.GetLocationStats()", CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));

                        callback.onError(new Error(err));
                    }
                });
            }
            else
            {
                covidActNowServiceLocal.getStateStat(location.getUsStateAbbreviation(), new IServiceCallbackCovidStats()
                {
                    @Override
                    public void onSuccess(CovidStats statistics)
                    {
                        ProcessCovidStat(location, statistics, dateToUse, false, callback);
                    }

                    @Override
                    public void onError(VolleyError err)
                    {
                        Log.d("CovidStatService.GetLocationStats()", CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));

                        callback.onError(new Error(err));
                    }
                });
            }

        }
        else if (CovidUtils.isUS(location))
        {
            if (timeseries)
            {
                covidActNowServiceLocal.getUSHistoricalStats(dateToUse, new IServiceCallbackCovidStats()
                {
                    @Override
                    public void onSuccess(CovidStats statistics)
                    {
                        ProcessCovidStat(location, statistics, dateToUse, true, callback);
                    }

                    @Override
                    public void onError(VolleyError err)
                    {
                        Log.d("CovidStatService.GetLocationStats()", CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));

                        callback.onError(new Error(err));
                    }
                });
            }
            else
            {
                covidActNowServiceLocal.getUSStats(new IServiceCallbackCovidStats()
                {
                    @Override
                    public void onSuccess(CovidStats statistics)
                    {
                        ProcessCovidStat(location, statistics, dateToUse, false, callback);
                    }

                    @Override
                    public void onError(VolleyError err)
                    {
                        Log.d("CovidStatService.GetLocationStats()", CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));

                        callback.onError(new Error(err));
                    }
                });
            }

        }
        else
        {
            covidService.getCovidStat(location, dateToUse, null, new IServiceCallbackCovidStats()
            {
                @Override
                public void onSuccess(CovidStats statistics)
                {
                    ProcessCovidStat(location, statistics, dateToUse, false, callback);
                }

                @Override
                public void onError(VolleyError err)
                {
                    Log.d("CovidStatService.GetLocationStats()", CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));

                    callback.onError(new Error(err));
                }
            });
        }
    }

    public static void updateLocation(@NotNull Location location, @NotNull Date currentDate, @NotNull CovidStats locStat, @NotNull IServiceCallbackGeneric callback)
    {

        // Find how many days stats are missing
        long diff = currentDate.getTime() - locStat.getStatusDate().getTime();
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        // Set calendar date to be passed for processing
        Calendar cal = Calendar.getInstance();
        cal.setTime(locStat.getStatusDate());

        // Get stats needed for location
        GetLocationStats(location, cal, true, callback);
    }

    public static void updateLocations(@NotNull List<Location> locations, @NotNull IServiceCallbackGeneric callback)
    {
        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();

        for(Location loc: locations)
        {
            // Sort stats
            List<CovidStats> locStats = loc.getStatistics().stream()
                    .sorted()
                    .collect(Collectors.toList());

            // Reverse sort to get latest first
            //Collections.reverse(locStats);

            // Get location's latest stat
            CovidStats locStat = locStats.stream().findFirst().orElse(null);

            // Throw error if unable to find stat.  This means there is either bad data
            // or somehow a location add is being processed as part of this update
            if (null == locStat)
            {
                callback.onError(new Error("Unable to find a stat from which to start"));
                return;
            }

            // Check dates
            if (locStat.getStatusDate().getTime() <= currentDate.getTime())
            {
                // Increase process counter to keep track of stat processing
                processCounter++;

                // Get statistics for location
                updateLocation(loc, currentDate, locStat, new IServiceCallbackGeneric()
                {
                    @Override
                    public <T> void onSuccess(T result)
                    {
                        // Location complete, decrease counter
                        processCounter--;
                        if (processCounter <= 0)
                        {
                            // Update the last update date
                            for(Location l: locations)
                            {
                                l.setLastUpdated(new Date());
                            }
                           // Return call
                            callback.onSuccess(locations);
                        }
                    }

                    @Override
                    public void onError(Error err)
                    {
                        // Location complete, decrease counter
                        processCounter--;
                        if (processCounter == 0)
                        {
                            // Save updated locations
                            CovidApplication.setLocations(locations);

                            // Return call
                            callback.onSuccess(locations);
                        }
                    }
                });
            }
        }

        // Nothing processing, return callback
        if (processCounter == 0)
            callback.onSuccess(locations);
    }


    /*    private static void manualHospitalizationStatBackFill(@NotNull Location location, @NotNull IServiceCallbackGeneric callback)
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
                covidService.getUSHospitalizations(new IServiceCallbackList()
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
                covidService.getUSStateHospitalizations(location.getUsStateAbbreviation(), new IServiceCallbackList()
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
    }*/

    /*    private static Location PopulateHospitalizationStats(@NotNull Location location, List<HospitalizationStat> stats)
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
                covidStat.setPositivityRate(hStat.getPositivityRate());
            }
        });

        return location;
    }*/
}

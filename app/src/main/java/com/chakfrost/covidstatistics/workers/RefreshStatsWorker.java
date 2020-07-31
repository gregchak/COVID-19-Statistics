package com.chakfrost.covidstatistics.workers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.R;
import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.GlobalStats;
import com.chakfrost.covidstatistics.models.HospitalizationStat;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.services.CovidRequestQueue;
import com.chakfrost.covidstatistics.services.CovidService;
import com.chakfrost.covidstatistics.services.IServiceCallbackCovidStats;
import com.chakfrost.covidstatistics.services.IServiceCallbackList;
import com.chakfrost.covidstatistics.services.IserviceCallbackGlobalStats;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class RefreshStatsWorker extends Worker
{
    private Context context;
    private List<Location> locations;
    private List<Location> newData;
    private int locationQueue;

    public RefreshStatsWorker(@NonNull Context context, @NonNull WorkerParameters params)
    {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork()
    {
        try
        {
            Log.d("RefreshStatsWorker.doWork()", "starting job...");

            // Instantiate collection of updated locations
            newData = new CopyOnWriteArrayList<>();
            locationQueue = 0;

            // Update stats
            RefreshGlobalStats();
            RefreshLocationStats();

            // Wait for RequestQueue to clear
            while (locationQueue > 0);

            // If there are location updates, send notification
            Log.d("RefreshStatsWorker.doWork()", "number of changes found: " + newData.size());
            if (newData.size() > 0)
            {
                SetLocation(newData);
                CovidApplication.sendNotification
                        ("Location Update",
                                "Your tracked locations have new COVID-19 stats available",
                                context);
            }

            Log.d("RefreshStatsWorker.doWork()", "finished");

            return Result.success();
        }
        catch (Exception ex)
        {
            Log.e("Errors occurred in RefreshStatsWorker.doWork()",
                    CovidUtils.formatError(ex.getMessage(), ex.getStackTrace().toString()));
            return Result.failure();
        }
    }

    private void RefreshGlobalStats()
    {
        GlobalStats global = CovidApplication.getGlobalStats();

        if (null == global)
        {
            RetrieveSummaryData();
        }
        else
        {
            Date currentDate = new Date();
            long diff, hours;

            // Account for statusDate being null
            if (null == global.getStatusDate())
                hours = 6;
            else
            {
                diff = currentDate.getTime() - global.getLastUpdate().getTime();
                hours = TimeUnit.MILLISECONDS.toHours(diff);
            }

            // Only refresh data if more than 11 hours old
            if (hours >= 6)
                RetrieveSummaryData();
        }
    }

    private void RetrieveSummaryData()
    {
        CovidService.summary(new IserviceCallbackGlobalStats()
         {
             @Override
             public void onSuccess(GlobalStats stats)
             {
                 CovidApplication.setGlobalStats(stats);
             }

             @Override
             public void onError(VolleyError error)
             {
                 VolleyLog.e("RefreshStatsWorker.RetrieveSummaryData()", error.toString());
             }
         }
        );
    }

    private void RefreshLocationStats()
    {
        locations = CovidApplication.getLocations();

        long diff;
        long hours;
        Date currentDate = new Date();

        for (Location loc : locations)
        {
            if (null == loc.getLastUpdated())
            {
                hours = 6;
            }
            else
            {
                diff = currentDate.getTime() - loc.getLastUpdated().getTime();
                hours = TimeUnit.MILLISECONDS.toHours(diff);
            }

            if (hours >= 6)
            {
                // Debugging
                //List<CovidStats> c = loc.getStatistics();
                //Collections.sort(c);
                //c.remove(0);
                //loc.setStatistics(c);

                if (!CovidUtils.statExists(loc.getStatistics()))
                {
                    // Increase location queue
                    locationQueue++;

                    // Set the date
                    Calendar startDate = Calendar.getInstance();
                    startDate.add(Calendar.DATE, -1);
                    loc.setLastUpdated(new Date());

                    // Get new stats
                    RefreshLocationStats(loc, startDate);
                }
            }
        }
    }

    private void RefreshLocationStats(Location location, Calendar dateToCheck)
    {
        if (location.getStatistics().size() > 0 && (CovidUtils.isUS(location) || CovidUtils.isUSState(location)))
        {
            long occurrences = location.getStatistics().stream()
                    .filter(s -> s.getHospitalizationsCurrent() != 0)
                    .count();

            // Get Hospitalization Stats if List is null
            if (occurrences < 10)
            {
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

                            // Now that we have the List<>, get report data
                            GetCovidStat(location, dateToCheck, (List<HospitalizationStat>)list);
                        }

                        @Override
                        public void onError(VolleyError err)
                        {
                            Log.e("RefreshStatusWorker.RefreshLocationStats()",
                                    CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));
                        }
                    });
                }
                else if (CovidUtils.isUSState(location))
                {
                    // Get US State abbreviation if not populated
                    if (TextUtils.isEmpty(location.getUsStateAbbreviation()))
                        location.setUsStateAbbreviation(CovidUtils.getUSStateAbbreviation(location));

                    // Get stats for US State
                    CovidService.getUSStateHospitalizations(location.getUsStateAbbreviation(), new IServiceCallbackList()
                    {
                        @Override
                        public <T> void onSuccess(List<T> list)
                        {
                            // Loop dates to back-fill
                            PopulateHospitalizationStats(location, (List<HospitalizationStat>)list);

                            // Now that we have the List<>, get report data
                            GetCovidStat(location, dateToCheck, (List<HospitalizationStat>)list);
                        }

                        @Override
                        public void onError(VolleyError err)
                        {
                            Log.e("RefreshStatusWorker.RefreshLocationStats()",
                                    CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));
                        }
                    });
                }
            }
            else
            {
                GetCovidStat(location, dateToCheck, null);
            }
        }
        else
        {
            GetCovidStat(location, dateToCheck, null);
        }
    }

    private Location PopulateHospitalizationStats(Location location, List<HospitalizationStat> stats)
    {
        // Set date to start getting report
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

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

    private void GetCovidStat(Location location, Calendar dateToCheck, List<HospitalizationStat> hospitalizationStats)
    {
        CovidService.getCovidStat(location, dateToCheck, hospitalizationStats, new IServiceCallbackCovidStats()
        {
            @Override
            public void onSuccess(CovidStats stat)
            {
                ProcessCovidStat(location, stat, dateToCheck);
            }

            @Override
            public void onError(VolleyError err)
            {
                Log.e("RefreshStatsWorker.GetCovidStat()",
                        CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));
            }
        });
    }

    private void ProcessCovidStat(Location loc, CovidStats stat, Calendar dateToCheck)
    {
        if (null != stat)
        {
            // Add CovidStat to Location
            loc.getStatistics().add(stat);

            // Get next date to check
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateToCheck.add(Calendar.DATE, -1);

            // Check if we already have stats for next dayToCheck
            // If so, stop.  Stats won't change.
            CovidStats found = loc.getStatistics().stream()
                    .filter(s -> dateFormat.format(s.getStatusDate().getTime()).equals(dateFormat.format(dateToCheck.getTimeInMillis())))
                    .findFirst()
                    .orElse(null);

            if (null != found)
            {
                loc.setLastUpdated(new Date());
                newData.add(loc);

                locationQueue--;
            }
            else
            {
                // Stat needed for next day
                RefreshLocationStats(loc, dateToCheck);
            }
        }
        else
        {
            Log.d("Finished refreshing location", Integer.toString(loc.getStatistics().size()));
            loc.setLastUpdated(new Date());
            newData.add(loc);

            locationQueue--;
        }
    }

    @Deprecated
    private void RetrieveLocationStats(Location loc, Calendar dateToCheck)
    {
        CovidService.report(loc.getIso(), loc.getProvince(), loc.getRegion(), loc.getMunicipality(), dateToCheck, new IServiceCallbackCovidStats()
                {
                    @Override
                    public void onSuccess(CovidStats stat)
                    {
                        if (null != stat)
                        {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            loc.getStatistics().add(stat);
                            dateToCheck.add(Calendar.DATE, -1);

                            // Check if we already have stats for next day to check
                            // If so, stop.  Stats won't change.
                            CovidStats found = loc.getStatistics().stream()
                                    .filter(s -> dateFormat.format(s.getStatusDate().getTime()).equals(dateFormat.format(dateToCheck.getTimeInMillis())))
                                    .findFirst()
                                    .orElse(null);

                            if (null != found)
                            {
                                loc.setLastUpdated(new Date());
                                newData.add(loc);

                                locationQueue--;
                            }
                            else
                            {
                                RetrieveLocationStats(loc, dateToCheck);
                            }
                        }
                        else
                        {
                            Log.d("Finished loading location", Integer.toString(loc.getStatistics().size()));
                            loc.setLastUpdated(new Date());
                            newData.add(loc);

                            locationQueue--;
                        }
                    }

                    @Override
                    public void onError(VolleyError error)
                    {
                        if(!TextUtils.isEmpty(error.getMessage()))
                            VolleyLog.e("GetReportData.onError()", error.getMessage());
                        else
                            VolleyLog.e("GetReportData.onError()", "Errors occurred while getting report.");

                        loc.setLastUpdated(new Date());
                        newData.add(loc);

                        locationQueue--;
                    }
                }
        );
    }

    private void SetLocation(List<Location> locationsToUpdate)
    {
        // Loop through each updated location
        // and update main List<Location>
        locationsToUpdate.forEach((loc) ->
        {
            Location found = locations.stream()
                .filter(l -> l.getCountry().equals(loc.getCountry())
                        && l.getProvince().equals(loc.getProvince())
                        && l.getMunicipality().equals(loc.getMunicipality()))
                .findFirst()
                .orElse(null);

            // If found, remove
            if (null != found)
            {
                Log.d("Removing location", CovidUtils.formatLocation(found));
                locations.remove(found);
            }

            // Add new/updated location to List<Location>
            locations.add(loc);
        });

        // Save updated Locations
        CovidApplication.setLocations(locations);
    }

}

package com.chakfrost.covidstatistics.workers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.GlobalStats;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.services.CovidService;
import com.chakfrost.covidstatistics.services.IServiceCallbackCovidStats;
import com.chakfrost.covidstatistics.services.IserviceCallbackGlobalStats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RefreshStatsWorker extends Worker
{
    private Context context;
    private List<Location> locations;
    private List<Location> newData;

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
            // Instantiate collection of updated locations
            newData = new ArrayList<>();

            // Update stats
            RefreshGlobalStats();
            RefreshLocationStats();

            // If there are location updates, send notification
            if (newData.size() > 0)
            {
                CovidApplication.sendNotification
                        ("Location Update",
                                "Your tracked locations have new COVID-19 stats available",
                                context);
            }

            return Result.success();
        }
        catch (Exception ex)
        {
            Log.e("Errors occurred in RefreshStatsWorker.doWork()", ex.toString());
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
                diff = currentDate.getTime() - global.getStatusDate().getTime();
                hours = TimeUnit.MILLISECONDS.toHours(diff);
            }

            // Only refresh data if more than 11 hours old
            if (hours > 5)
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
            if (null == loc.getStatusDate())
            {
                hours = 12;
            }
            else
            {
                diff = currentDate.getTime() - loc.getStatusDate().getTime();
                hours = TimeUnit.MILLISECONDS.toHours(diff);
            }

            if (hours > 11)
            {
                // Populate report information
                Calendar startDate = Calendar.getInstance();
                startDate.add(Calendar.DATE, -1);

                CovidStats found = loc.getStatistics().stream()
                        .filter(s -> s.getStatusDate().getTime() == startDate.getTimeInMillis())
                        .findFirst()
                        .orElse(null);

                if (null == found)
                {
                    loc.setStatusDate(new Date());
                    RetrieveLocationStats(loc, startDate);
                }
            }
        }
    }

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
                                SetLocation(loc);
                            }
                            else
                            {
                                RetrieveLocationStats(loc, dateToCheck);
                            }
                        }
                        else
                        {
                            Log.d("Finished loading location", Integer.toString(loc.getStatistics().size()));
                            SetLocation(loc);
                        }
                    }

                    @Override
                    public void onError(VolleyError error)
                    {
                        if(!TextUtils.isEmpty(error.getMessage()))
                            VolleyLog.e("GetReportData.onError()", error.getMessage());
                        else
                            VolleyLog.e("GetREportData.onError()", "Errors occurred while getting report.");
                    }
                }
        );
    }

    private void SetLocation(Location loc)
    {
        loc.setStatusDate(new Date());
        if (null == locations)
            locations = CovidApplication.getLocations();

        Location found = locations.stream()
                .filter(l -> l.getCountry().equals(loc.getCountry())
                        && l.getProvince().equals(loc.getProvince())
                        && l.getMunicipality().equals(loc.getMunicipality()))
                .findFirst()
                .orElse(null);

        if (null == found)
        {
            locations.add(loc);
        }
        else
        {
            locations.remove(found);
            locations.add(loc);
        }
        newData.add(loc);
        CovidApplication.setLocations(locations);
    }

}

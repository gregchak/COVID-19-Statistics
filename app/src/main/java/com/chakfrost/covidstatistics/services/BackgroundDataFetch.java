package com.chakfrost.covidstatistics.services;

import android.util.Log;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.models.Location;
import com.google.android.material.snackbar.Snackbar;
import com.techyourchance.threadposter.UiThreadPoster;
import com.techyourchance.threadposter.BackgroundThreadPoster;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BackgroundDataFetch
{
    public interface Listener {
        void onDataFetched(List<Location> data);
        void onDataFetchFailed();
    }

    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final UiThreadPoster mUiThreadPoster;

    private final Set<Listener> mListeners = Collections.newSetFromMap(
            new ConcurrentHashMap<Listener, Boolean>());


    public BackgroundDataFetch(BackgroundThreadPoster backgroundThreadPoster,
                            UiThreadPoster uiThreadPoster) {
        mBackgroundThreadPoster = backgroundThreadPoster;
        mUiThreadPoster = uiThreadPoster;
    }

    public void registerListener(Listener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        mListeners.remove(listener);
    }

    public void fetchAllLocations(List<Location> locations)
    {
        // offload work to background thread
        mBackgroundThreadPoster.post(() -> fetchAllLocationsSync(locations));
    }

    public void fetchLocation(Location location)
    {
        List<Location> locations = new ArrayList<>();
        locations.add(location);

        // offload work to background thread
        mBackgroundThreadPoster.post(() -> fetchAllLocationsSync(locations));
    }


    @WorkerThread
    private void fetchAllLocationsSync(List<Location> locations)
    {
        try
        {
            if (locations.size() == 0)
                locations = CovidApplication.getLocations();

            final List<Location> locationsToRefresh = locations;
            CovidStatService.updateLocations(locationsToRefresh, new IServiceCallbackGeneric()
            {
                @Override
                public <T> void onSuccess(T result)
                {
                    List<Location> updatedLocations = (List<Location>)result;

                    for (Location l: updatedLocations)
                    {
                        SetLocation(locationsToRefresh, l);
                    }

                    // notify listeners on UI thread
                    mUiThreadPoster.post(() -> notifySuccess(locationsToRefresh));

                    // Save updated locations
                    CovidApplication.setLocations(updatedLocations);
                }

                @Override
                public void onError(Error err)
                {
                    Log.d("StatisticsFragment.LoadReportData()", err.toString());
                    // notify listeners on UI thread
                    mUiThreadPoster.post(() -> notifyFailure());
                }
            });
        }
        catch (Exception ex)
        {
            // notify listeners on UI thread
            mUiThreadPoster.post(() -> notifyFailure());
        }
    }

    private void SetLocation(List<Location> locations, Location loc)
    {
        loc.setLastUpdated(new Date());
        if (null == locations)
            locations = new ArrayList<>();

        // Check if location is already part of the List
        Location found = locations.stream()
                .filter(l -> l.getCountry().equals(loc.getCountry())
                        && l.getProvince().equals(loc.getProvince())
                        && l.getMunicipality().equals(loc.getMunicipality()))
                .findFirst()
                .orElse(null);

        if (null != found)
        {
            Log.d("removing updated location", CovidUtils.formatLocation(found));
            // Location is being updated
            locations.remove(found);
        }

        // Save Location
        Log.d("adding updated location", "");
        locations.add(loc);
    }


    @UiThread
    private void notifyFailure() {
        for (Listener listener : mListeners) {
            listener.onDataFetchFailed();
        }
    }

    @UiThread
    private void notifySuccess(List<Location> data) {
        for (Listener listener : mListeners) {
            listener.onDataFetched(data);
        }
    }
}

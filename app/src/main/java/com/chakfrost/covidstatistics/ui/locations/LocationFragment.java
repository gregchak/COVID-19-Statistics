package com.chakfrost.covidstatistics.ui.locations;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.R;
import com.chakfrost.covidstatistics.adapters.LocationSimpleListRecyclerViewAdapter;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.models.OperationActions;
import com.chakfrost.covidstatistics.services.CovidStatService;
import com.chakfrost.covidstatistics.services.IServiceCallbackGeneric;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class LocationFragment extends Fragment
{
    //private LocationViewModel locationViewModel;

    private List<Location> locations;
    private RecyclerView locationsSimpleListView;
    private LocationSimpleListRecyclerViewAdapter locationsSimpleListAdapter;
    private ProgressBar locationsProgressBar;
    private int locationRefreshCount = 0;

    private final BackgroundThreadPoster backgroundThread = new BackgroundThreadPoster();
    private final UiThreadPoster uiThread = new UiThreadPoster();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //locationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_locations, container, false);
        //final TextView textView = root.findViewById(R.id.text_gallery);

        //locationViewModel.getText().observe(getViewLifecycleOwner(), s -> textView.setText(s));

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.hide();

        locationsProgressBar = root.findViewById(R.id.locations_progress_bar);
        locationsSimpleListView = root.findViewById(R.id.locations_recycler_view);
        locationsSimpleListView.addItemDecoration(new DividerItemDecoration(
                locationsSimpleListView.getContext(),
                DividerItemDecoration.VERTICAL
        ));

        loadLocations();
        return root;
    }

    private void loadLocations()
    {
        if (null == locations)
            locations = CovidApplication.getLocations();

        Collections.sort(locations);

        // Set the recycler layout
        locationsSimpleListView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Bind the adapter
        locationsSimpleListAdapter = new LocationSimpleListRecyclerViewAdapter(locations);
        locationsSimpleListView.setAdapter(locationsSimpleListAdapter);
        locationsSimpleListAdapter.setClickListener(this::locationSelected);
    }

    private void locationSelected(OperationActions action, View view, int position)
    {
        // Get Location to delete
        Location location = locations.get(position);

        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (action == OperationActions.DELETE)
        {
            // Set buttons
            builder.setPositiveButton("Remove", (dialog, id) -> RemoveLocation(view, location));
            builder.setNegativeButton("Cancel", (dialog, id) ->
            {
            });

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("Are you sure you want to remove " + CovidUtils.formatLocation(location) + "?")
                    .setTitle("Remove location?");
        }
        else if (action == OperationActions.REFRESH)
        {
            builder.setPositiveButton("Refresh", (dialog, id) -> RefreshLocationStats(view, location));
            builder.setNegativeButton("Cancel", (dialog, id) ->
            {
            });

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("Are you sure you want to refresh all stats for " + CovidUtils.formatLocation(location) + "?")
                    .setTitle("Refresh location?");
        }
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void RemoveLocation(View view, Location toDelete)
    {
        String locationName = CovidUtils.formatLocation(toDelete);

        // Delete selected Location
        locations.remove(toDelete);

        // Save Location List<> to storage
        CovidApplication.setLocations(locations);

        // Notify user
        Snackbar.make(view, "Removed " + locationName, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();

        // Reload list
        loadLocations();
    }

    private void RefreshLocationStats(View view, Location location)
    {
        String locationName = CovidUtils.formatLocation(location);

        locationsProgressBar.setVisibility(View.VISIBLE);

        // Notify user
        Snackbar.make(view, "Refreshing " + locationName, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();

        // Refresh stats in background
        backgroundThread.post(() -> getLocationStats(location));

    }

    @WorkerThread
    private void getLocationStats(Location location)
    {
        locationRefreshCount++;
        CovidStatService.getAllLocationStats(location, new IServiceCallbackGeneric()
        {
            @Override
            public <T> void onSuccess(T result)
            {
                Location updatedLocation = (Location)result;
                updatedLocation.setLastUpdated(new Date());

                Location found = locations.stream()
                        .filter(l -> l.getCountry().equals(location.getCountry())
                                && l.getProvince().equals(location.getProvince())
                                && l.getMunicipality().equals(location.getMunicipality()))
                        .findFirst()
                        .orElse(null);

                if (null != found)
                {
                    // Location is being updated
                    locations.remove(found);
                }

                // Save Location
                locations.add(updatedLocation);


                // Save to local storage
                CovidApplication.setLocations(locations);

                locationRefreshCount--;

                if (locationRefreshCount < 1)
                    locationsProgressBar.setVisibility(View.GONE);

                uiThread.post(() -> showRefreshResult(true, updatedLocation));
            }

            @Override
            public void onError(Error err)
            {
                if (locationRefreshCount < 1)
                    locationsProgressBar.setVisibility(View.GONE);

                uiThread.post(() -> showRefreshResult(false, location));
            }
        });
    }

    @UiThread
    private void showRefreshResult(boolean success, Location location)
    {
        if (success)
        {
            if(null != getView())
            {
                // Notify
                Snackbar.make(getView(),
                        MessageFormat.format("Stats refreshed for {0}", CovidUtils.formatLocation(location)),
                        Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        }
        else
        {
            if(null != getView())
            {
                // Notify
                Snackbar.make(getView(),
                        MessageFormat.format("Errors occurred while refreshing {0}", CovidUtils.formatLocation(location)),
                        Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        }

        // Hide progressBar
        locationsProgressBar.setVisibility(View.GONE);
    }
}

package com.chakfrost.covidstatistics.ui.statistics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.MainActivity;
import com.chakfrost.covidstatistics.R;
import com.chakfrost.covidstatistics.adapters.LocationStatsRecyclerViewAdapter;
import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.GlobalStats;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.services.CovidService;
import com.chakfrost.covidstatistics.services.IServiceCallbackCovidStats;
import com.chakfrost.covidstatistics.services.IserviceCallbackGlobalStats;
import com.chakfrost.covidstatistics.ui.LocationStatsDetail;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class StatisticsFragment extends Fragment
{
    private GlobalStats summary;
    private List<Location> locations;
    private StatisticsViewModel statisticsViewModel;
    private TextView globalLastUpdate;
    private TextView confirmedValue;
    private TextView confirmedDiff;
    private ImageView confirmedArrow;
    private TextView deathsValue;
    private TextView deathsDiff;
    private ImageView deathsArrow;
    private TextView recoveredValue;
    private TextView recoveredDiff;
    private ImageView recoveredArrow;
    private TextView activeValue;
    private TextView activeDiff;
    private ImageView activeArrow;
    private TextView fatalityValue;
    private TextView fatalityDiff;
    private ImageView fatalityArrow;

    private ProgressBar progressBar;
    private RecyclerView locationsView;
    private LocationStatsRecyclerViewAdapter locationsListAdapter;

    private FirebaseAnalytics firebaseAnalytics;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        statisticsViewModel = ViewModelProviders.of(requireActivity()).get(StatisticsViewModel.class);

        View root = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Instantiate UI elements
        globalLastUpdate = root.findViewById(R.id.stats_global_last_update);
        confirmedValue = root.findViewById(R.id.stats_global_confirmed_value);
        confirmedDiff = root.findViewById(R.id.stats_global_confirmed_diff);
        confirmedArrow = root.findViewById(R.id.stats_global_confirmed_image);

        deathsValue = root.findViewById(R.id.stats_global_deaths_value);
        deathsDiff = root.findViewById(R.id.stats_global_deaths_diff);
        deathsArrow = root.findViewById(R.id.stats_global_deaths_image);

        recoveredValue = root.findViewById(R.id.stats_global_recovered_value);
        recoveredDiff = root.findViewById(R.id.stats_global_recovered_diff);
        recoveredArrow = root.findViewById(R.id.stats_global_recovered_image);

        activeValue = root.findViewById(R.id.stats_global_active_value);
        activeDiff = root.findViewById(R.id.stats_global_active_diff);
        activeArrow = root.findViewById(R.id.stats_global_active_image);

        fatalityValue = root.findViewById(R.id.stats_global_fatality_value);
        fatalityDiff = root.findViewById(R.id.stats_global_fatality_diff);
        fatalityArrow = root.findViewById(R.id.stats_global_fatality_image);

        progressBar = root.findViewById(R.id.statistics_progress_bar);
        locationsView = root.findViewById(R.id.stats_global_location_recycler_view);

        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        // Show Floating button for Adding a new Location
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.show();

        // Load data
        loadGlobals();
        loadLocations(locationsView, true);

        // Set parent refresh listener
        ((MainActivity)getActivity()).setFragmentRefreshListener(this::parentOnRefresh);

//        Bundle bundle = new Bundle();
//        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "111");
//        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "test");
//        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
//        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        return root;
    }

    private void loadGlobals()
    {
        // Populate summary variable
        summary = CovidApplication.getGlobalStats();

        // If summary is null, get it
        if (null == summary)
        {
            retrieveGlobalStatsData();
        }
        else
        {
            Date currentDate = new Date();
            long diff, hours;

            // Account for statusDate being null
            if (null == summary.getLastUpdate())
                hours = 6;
            else
            {
                diff = currentDate.getTime() - summary.getLastUpdate().getTime();
                hours = TimeUnit.MILLISECONDS.toHours(diff);
            }

            // Only refresh data if 6+ hours old
            if (hours >= 6)
                retrieveGlobalStatsData();
            else
                populateGlobalStats();
        }
    }

    private void populateGlobalStats()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        globalLastUpdate.setText(MessageFormat.format("as of {0}", dateFormat.format(summary.getLastUpdate())));

        // Confirmed
        confirmedValue.setText(NumberFormat.getInstance().format(summary.getTotalConfirmed()));
        confirmedDiff.setText(NumberFormat.getInstance().format(summary.getNewConfirmed()));
        confirmedArrow.setImageResource(CovidUtils.determineArrow(summary.getTotalConfirmed(), summary.getNewConfirmed(), false));

        // Deaths
        deathsValue.setText(NumberFormat.getInstance().format(summary.getTotalDeaths()));
        deathsDiff.setText(NumberFormat.getInstance().format(summary.getNewDeaths()));
        deathsArrow.setImageResource(CovidUtils.determineArrow(summary.getTotalDeaths(), summary.getNewDeaths(), false));

        // Recovered
        recoveredValue.setText(NumberFormat.getInstance().format(summary.getTotalRecovered()));
        recoveredDiff.setText(NumberFormat.getInstance().format(summary.getNewRecovered()));
        recoveredArrow.setImageResource(CovidUtils.determineArrow(summary.getTotalRecovered(), summary.getNewRecovered(), true));

        // Active
        activeValue.setText(NumberFormat.getInstance().format(summary.getTotalActive()));
        activeDiff.setText(NumberFormat.getInstance().format(summary.getNewActive()));
        activeArrow.setImageResource(CovidUtils.determineArrow(summary.getTotalRecovered(), summary.getNewRecovered(), false));

        // Fatality
        fatalityValue.setText(MessageFormat.format("{0}%", NumberFormat.getInstance().format(summary.getFatalityRate() * 100)));
        double fatalityDifference = summary.getFatalityRate() - summary.getPreviousFatalityRate();
        fatalityDiff.setText(MessageFormat.format("{0}%", NumberFormat.getInstance().format(fatalityDifference * 100)));
        fatalityArrow.setImageResource(CovidUtils.determineArrow(summary.getFatalityRate(), summary.getPreviousFatalityRate(), false));
    }

    private void retrieveGlobalStatsData()
    {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "0");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "GlobalStatistics refresh");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        progressBar.setVisibility(View.VISIBLE);
        CovidService.summary(new IserviceCallbackGlobalStats()
                             {
                                 @Override
                                 public void onSuccess(GlobalStats stats)
                                 {
                                     // Set the previous fatality rate value based on stat date
                                     // since stats are only updated once a day
                                     if (summary.getStatusDate() != stats.getStatusDate())
                                         stats.setPreviousFatalityRate(summary.getFatalityRate());
                                     else
                                         stats.setPreviousFatalityRate(summary.getPreviousFatalityRate());

                                     // Set local variables
                                     summary = stats;
                                     summary.setLastUpdate(new Date());

                                     // Save Global stats
                                     CovidApplication.setGlobalStats(summary);

                                     // Hide progress
                                     progressBar.setVisibility(View.GONE);

                                     // Display
                                     populateGlobalStats();

                                     // Notify
                                     Snackbar.make(getView(), "Global stats updated", Snackbar.LENGTH_SHORT)
                                             .setAction("Action", null).show();
                                 }

                                 @Override
                                 public void onError(VolleyError error)
                                 {
                                     Log.e("LoadCountries.onError()", error.getStackTrace().toString());
                                     Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                                     Bundle bundle = new Bundle();
                                     bundle.putString(FirebaseAnalytics.Param.METHOD, "retrieveGlobalStatsData.onError");
                                     bundle.putString("Global_Stats_Error", error.getStackTrace().toString());
                                     firebaseAnalytics.logEvent("ERROR", bundle);
                                 }
                             }
        );
    }

    private void loadLocations(RecyclerView recyclerView, boolean refreshLocations)
    {
        // Get saved locations
        locations = CovidApplication.getLocations();
        Collections.sort(locations);

        // Set the layout
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Check for Location Statistics
        List<Location> locationsForDisplay = locations.stream()
                .filter(l -> l.getStatistics().size() > 0)
                .collect(Collectors.toList());

        // Set adapter if there are valid Locations
        if (locationsForDisplay.size() > 0)
        {
            // Set adapter
            locationsListAdapter = new LocationStatsRecyclerViewAdapter(getContext(), locationsForDisplay);
            locationsListAdapter.setClickListener(this::locationListAdapterClick);
            locationsListAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(locationsListAdapter);
        }

        // If there are Locations and refresh is true
        if (refreshLocations && locations.size() > 0)
            RefreshLocations();
/*        else
        {
            // Version 1.2.0.0 update: add fatality rate
            Date currentDate = new Date(2020,4,14);
            long diff;
            long hours;
            for (Location loc : locations)
            {
                diff = currentDate.getTime() - loc.getLastUpdated().getTime();
                hours = TimeUnit.MILLISECONDS.toHours(diff);

                long count = loc.getStatistics().stream()
                        .filter(s -> s.getFatalityRate() > 0 && s.getStatusDate().before(currentDate))
                        .count();

                if (count == 0)
                    LoadReportData(loc, startDate);
            }
        }*/
    }

    private void locationListAdapterClick(Location selectedLocation)
    {
        // Set selected Location
        statisticsViewModel.setSelectedLocation(selectedLocation);

        // Build Intent for LocationStatDetail Activity
        Intent details = new Intent(getActivity(), LocationStatsDetail.class);
        details.putExtra("location", selectedLocation);

        // Start LocationStatDetail Activity
        startActivity(details);


        // Load Fragment
//        Fragment locationDetails = new LocationDetailsFragment();
//        FragmentManager fm = getParentFragmentManager();
//        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
//
//        if (locationDetails.isAdded())
//        {
//            fm.beginTransaction()
//                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
//                    .show(locationDetails)
//                    .commit();
//        }
//        else
//        {
//            ft.add(locationDetails, "details");
//            ft.show(locationDetails);
//            ft.addToBackStack("details");
//            ft.commit();
//        }

    }

    private void RefreshLocations()
    {
        long diff;
        long hours;
        Date currentDate = new Date();

        // Loop through each Location checking for stale LastUpdated value
        for (Location loc : locations)
        {
            // Account for a null value
            if (null == loc.getLastUpdated())
            {
                hours = 6;
            }
            else
            {
                diff = currentDate.getTime() - loc.getLastUpdated().getTime();
                hours = TimeUnit.MILLISECONDS.toHours(diff);
            }

            // If 6+ hours stale, check for new stats
            if (hours >= 6)
            {
                // Check if Location's current date state is present
                if (!CovidUtils.statExists(loc.getStatistics()))
                {
                    // Set date
                    Calendar startDate = Calendar.getInstance();
                    startDate.add(Calendar.DATE, -1);
                    loc.setLastUpdated(new Date());

                    // Load report
                    LoadReportData(loc, startDate);
                }
            }
        }
    }

    private void LoadReportData(Location loc, Calendar dateToCheck)
    {
        progressBar.setVisibility(View.VISIBLE);
        CovidService.report(loc.getIso(), loc.getProvince(), loc.getRegion(), loc.getMunicipality(), dateToCheck, new IServiceCallbackCovidStats()
                {
                    @Override
                    public void onSuccess(CovidStats stat)
                    {
                        // If stat is null then there is no data for requested Location and Date
                        if (null != stat)
                        {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            loc.getStatistics().add(stat);
                            dateToCheck.add(Calendar.DATE, -1);

                            // Check if we already have stats for next dayToCheck
                            // If so, stop.  Stats won't change.
                            CovidStats found = loc.getStatistics().stream()
                                    .filter(s -> dateFormat.format(s.getStatusDate().getTime()).equals(dateFormat.format(dateToCheck.getTimeInMillis())))
                                    .findFirst()
                                    .orElse(null);

                            if (null != found)
                            {
                                // Stat for next day found, stop
                                SetLocation(loc);
                                loadLocations(locationsView, false);
                                progressBar.setVisibility(View.GONE);

                                // Notify
                                Snackbar.make(getView(), "Location stats updated ", Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            }
                            else
                            {
                                // Stat needed for next day
                                LoadReportData(loc, dateToCheck);
                            }
                        }
                        else
                        {
                            SetLocation(loc);
                            loadLocations(locationsView, false);
                            progressBar.setVisibility(View.GONE);

                            // Notify
                            Snackbar.make(getView(), "Location stats updated ", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                        }
                    }

                    @Override
                    public void onError(VolleyError error)
                    {
                        Log.e("GetReportData.onError()", error.getStackTrace().toString());
//                        if(!TextUtils.isEmpty(error.getMessage()))
//                            Log.e("GetReportData.onError()", error.getMessage());
//                        else
//                            Log.e("GetReportData.onError()", "Errors occurred while getting report.");
                        Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void SetLocation(Location loc)
    {
        loc.setLastUpdated(new Date());
        if (null == locations)
            locations = CovidApplication.getLocations();

        // Check if location is already part of the List
        Location found = locations.stream()
                .filter(l -> l.getCountry().equals(loc.getCountry())
                        && l.getProvince().equals(loc.getProvince())
                        && l.getMunicipality().equals(loc.getMunicipality()))
                .findFirst()
                .orElse(null);


        if (null == found)
        {
            // Location is new, add to List
            locations.add(loc);
        }
        else
        {
            // Location is being updated
            locations.remove(found);
            locations.add(loc);
        }

        // Save to local storage
        CovidApplication.setLocations(locations);
    }

    private void parentOnRefresh(int resultCode, Intent data)
    {
        // Adding Location
        if (resultCode == 110)
        {
            // Get Location from Intent
            Location loc = (Location)data.getSerializableExtra("location");

            // Log statistics to Firebase
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, CovidUtils.formatLocation(loc));
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "New Location");
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            // Notify
            Snackbar.make(getView(), "Retrieving location statistics...", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();

            // Set date to start getting report
            Calendar startDate = Calendar.getInstance();
            startDate.add(Calendar.DATE, -1);

            // Load report
            LoadReportData(loc, startDate);
        }
    }
}

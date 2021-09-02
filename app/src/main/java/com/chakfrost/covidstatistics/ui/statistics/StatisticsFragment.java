package com.chakfrost.covidstatistics.ui.statistics;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.CovidDataStore;
import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.MainActivity;
import com.chakfrost.covidstatistics.R;
import com.chakfrost.covidstatistics.adapters.LocationStatsRecyclerViewAdapter;
import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.GlobalStats;
import com.chakfrost.covidstatistics.models.HospitalizationStat;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.services.CovidService;
import com.chakfrost.covidstatistics.services.CovidStatService;
import com.chakfrost.covidstatistics.services.IServiceCallbackCovidStats;
import com.chakfrost.covidstatistics.services.IServiceCallbackGeneric;
import com.chakfrost.covidstatistics.services.IServiceCallbackList;
import com.chakfrost.covidstatistics.services.IServiceCallbackGlobalStats;
import com.chakfrost.covidstatistics.ui.LocationStatsDetail;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    private SwipeRefreshLayout swipeContainer;

    private static boolean globalRefreshComplete;
    private static boolean locationRefreshComplete;
    private static boolean locationsUpdated;
    private static boolean manualRefresh;
    private String retrieveLocationStatsText;
    private String completeLocationStatsText;

    private int locationRefreshCount;


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

        swipeContainer = root.findViewById(R.id.stats_global_swipe_refresh_layout);
        swipeContainer.setOnRefreshListener(this::refreshData);

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.colorPrimaryDark);

        // Instantiate Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        // Show Floating button for Adding a new Location; other fragments may have hidden it
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.show();

        // Default manualRefresh to false
        manualRefresh = false;
        globalRefreshComplete = true;
        locationRefreshComplete = true;

        // Load data
        loadGlobals();
        loadLocations(locationsView, true);

        // Set parent refresh listener
        ((MainActivity)getActivity()).setFragmentRefreshListener(this::parentOnRefresh);

        return root;
    }

    private void refreshData()
    {
        locationRefreshCount = 0;
        globalRefreshComplete = false;
        locationRefreshComplete = false;
        manualRefresh = true;
        progressBar.setVisibility(View.VISIBLE);

        new RefreshGlobalStatistics().execute(manualRefresh);
        new RefreshLocationStatistics().execute(manualRefresh);
    }

    private void loadGlobals()
    {
        // Populate summary variable
        summary = CovidApplication.getGlobalStats();

        // If summary is null, get it
        if (null == summary)
        {
            new RefreshGlobalStatistics().execute(false);
        }
        else
        {
            // Populate data
            populateGlobalStats();

            Date currentDate = new Date();
            long diff, hours;

            // Account for statusDate being null
            if (null == summary.getLastChecked())
                hours = 6;
            else
            {
                diff = currentDate.getTime() - summary.getLastChecked().getTime();
                hours = TimeUnit.MILLISECONDS.toHours(diff);
            }

            // Only refresh data if 6+ hours old
            if (hours >= 6)
                new RefreshGlobalStatistics().execute(false);
        }
    }

    private void populateGlobalStats()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US);
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

    private void retrieveGlobalStatsData(boolean manualRefresh)
    {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "0");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Refresh");
        firebaseAnalytics.logEvent("GLOBAL_STATS", bundle);

        if (!manualRefresh)
            progressBar.setVisibility(View.VISIBLE);

        CovidService.summary(new IServiceCallbackGlobalStats()
        {
             @Override
             public void onSuccess(GlobalStats stats)
             {
                 if (null != summary && stats.getLastUpdate().getTime() == summary.getLastUpdate().getTime())
                 {
                     // Update Last Checked date
                     summary.setLastChecked(new Date());

                     // Save Global stats
                     CovidApplication.setGlobalStats(summary);
                 }
                 else
                 {
                     if (null != summary)
                     {
                         // Set the previous fatality rate value based on stat date
                         // since stats are only updated once a day
                         stats.setPreviousFatalityRate(summary.getFatalityRate());
                     }
                     else
                     {
                         // No previous information, set to same rate as current
                         stats.setPreviousFatalityRate(stats.getFatalityRate());
                     }

                     // Set local variables
                     summary = stats;
                     summary.setLastChecked(new Date());

                     // Save Global stats
                     CovidApplication.setGlobalStats(summary);

                     // Display
                     populateGlobalStats();

                     // Notify
                     Snackbar.make(getView(), "Global stats updated", Snackbar.LENGTH_SHORT)
                             .setAction("Action", null).show();
                 }

                 // Dismiss progress indicator
                 //Log.d("refreshGlobals()", "manualRefresh: " + String.valueOf(manualRefresh) + "; locationRefreshComplete: " + String.valueOf(locationRefreshComplete));
                 globalRefreshComplete = true;
                 clearProgressIndicators();
             }

             @Override
             public void onError(VolleyError error)
             {
                 Log.e("LoadCountries.onError()", error.getStackTrace().toString());
                 Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                 Bundle bundle = new Bundle();
                 bundle.putString(FirebaseAnalytics.Param.METHOD, "retrieveGlobalStatsData.onError");
                 bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "0");
                 bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Refresh");
                 bundle.putString("Global_Stats_Error", error.getStackTrace().toString());
                 firebaseAnalytics.logEvent("ERROR", bundle);

                 // Dismiss progress indicator
                 clearProgressIndicators();
             }
        });
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
        {
            new RefreshLocationStatistics().execute(false);
            //RefreshLocations(false);
        }
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

    private void RefreshLocations(boolean manualRefresh)
    {
        long diff;
        long hours;
        Date currentDate = new Date();
        List<Location> locationsToRefresh = new ArrayList<>();

        // Reset refresh count
        locationRefreshCount = 0;

        // Loop through each Location checking for stale LastUpdated value
        for (Location location : locations)
        {
            // Account for a null value
            if (null == location.getLastUpdated())
            {
                hours = 6;
            }
            else
            {
                diff = currentDate.getTime() - location.getLastUpdated().getTime();
                hours = TimeUnit.MILLISECONDS.toHours(diff);
            }

            // If 6+ hours stale, check for new stats
            if (hours >= 6 || manualRefresh)                       // <- TODO: change to 6
            {
                // Check if Location's current date state is present
                if (!CovidUtils.statExists(location))     // <- TODO: change to !(not)
                {
                    // Set date
                    Calendar startDate = Calendar.getInstance();
                    startDate.add(Calendar.DATE, -1);
                    location.setLastUpdated(new Date());

                    // Log analytics
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, CovidUtils.formatLocation(location));
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Refresh From Statistics Fragment");
                    firebaseAnalytics.logEvent("LOCATION_STATS", bundle);

                    // Load report
                    locationRefreshCount++;

                    // if US state, ensure the state abbreviation is set
                    if (CovidUtils.isUSState(location) && TextUtils.isEmpty(location.getUsStateAbbreviation()))
                        location.setUsStateAbbreviation(CovidUtils.getUSStateAbbreviation(location));

                    // Refresh location data
                    //CovidStatService.UpdateLocation(location, startDate, );
                    locationsToRefresh.add(location);
                    //GetStatsForLocation(location, startDate, null, manualRefresh);
                }
            }
        }

        if (locationsToRefresh.size() > 0)
        {
            // Set update boolean for status
            locationsUpdated = true;

            CovidStatService.updateLocations(locationsToRefresh, new IServiceCallbackGeneric()
            {
                @Override
                public <T> void onSuccess(T result)
                {
                    //locationRefreshCount--;
                    List<Location> updatedLocations = (List<Location>)result;

                    for (Location l: updatedLocations)
                    {
                        SetLocation(l);
                    }
                    //SetLocation(((List<Location>)result).get(0));
                    //loadLocations(locationsView, false);
                    locationRefreshCount = 0;
                    if (globalRefreshComplete)
                    {
                        locationRefreshComplete = true;

                        // Dismiss progress indicator
                        //clearProgressIndicators();

                        // Save updated locations
                        CovidApplication.setLocations(updatedLocations);
                    }

                    // Notify
                    Snackbar.make(getView(), "Location stats updated ", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }

                @Override
                public void onError(Error err)
                {
                    Log.d("StatisticsFragment.LoadReportData()", err.toString());
                }
            });
        }
        //Log.d("RefreshLocations()", "locationRefreshCount: " + String.valueOf(locationRefreshCount) + "; globalRefreshComplete: " + String.valueOf(globalRefreshComplete));
//        if (locationRefreshCount == 0)
//            locationRefreshComplete = true;
//
//        if (locationRefreshCount == 0 && (manualRefresh && globalRefreshComplete))
//        {
//            locationRefreshComplete = true;
//            swipeContainer.setRefreshing(false);
//        }
    }

/*    private void ProcessCovidStat(Location loc, CovidStats stat, Calendar dateToCheck,
                                  List<HospitalizationStat> hospitalizationStats,
                                  boolean manualRefresh)
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
                locationRefreshCount--;

                // Stat for next day found, stop
                SetLocation(loc);
                loadLocations(locationsView, false);


                if (locationRefreshCount == 0)
                {
                    locationRefreshComplete = true;

                    // Dismiss progress indicator
                    clearProgressIndicators();
                }

                // Notify
                Snackbar.make(getView(), "Location stats updated ", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
            else
            {
                // Stat needed for next day
                GetStatsForLocation(loc, dateToCheck, hospitalizationStats, manualRefresh);
            }
        }
        else
        {
            locationRefreshCount--;

            SetLocation(loc);
            loadLocations(locationsView, false);

            if (locationRefreshCount == 0)
            {
                locationRefreshComplete = true;

                // Dismiss progress indicator
                clearProgressIndicators();
            }

            // Notify
            Snackbar.make(getView(), "Location stats updated ", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
    }*/

    private void GetStatsForLocation(Location location, Calendar dateToCheck, List<HospitalizationStat> hospitalizationStats, boolean manualRefresh)
    {
        if (!manualRefresh)
            progressBar.setVisibility(View.VISIBLE);

        // Set update boolean for status
        locationsUpdated = true;

        GetCovidStat(location, dateToCheck, hospitalizationStats, manualRefresh);
    }

    private void GetCovidStat(Location location, Calendar dateToCheck, List<HospitalizationStat> hospitalizationStats, boolean manualRefresh)
    {
        List<Location> locationToRefresh = new ArrayList<>();
        locationToRefresh.add(location);

        CovidStatService.updateLocations(locationToRefresh, new IServiceCallbackGeneric()
        {
            @Override
            public <T> void onSuccess(T result)
            {
                locationRefreshCount--;

                SetLocation(((List<Location>)result).get(0));
                loadLocations(locationsView, false);

                if (locationRefreshCount == 0)
                {
                    locationRefreshComplete = true;

                    // Dismiss progress indicator
                    clearProgressIndicators();
                }

                // Notify
                Snackbar.make(getView(), "Location stats updated ", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }

            @Override
            public void onError(Error err)
            {
                Log.d("StatisticsFragment.LoadReportData()", err.toString());
            }
        });
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
            firebaseAnalytics.logEvent("ADD_LOCATION", bundle);

            // Notify
            String[] texts = CovidUtils.getRandomLocationRetrieveCompletePair();
            retrieveLocationStatsText = texts[0];
            completeLocationStatsText = texts[1];

            Snackbar.make(getView(),
                    MessageFormat.format("{0}",
                            retrieveLocationStatsText),
                    Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();

            // Set date to start getting report
            Calendar startDate = Calendar.getInstance();
            startDate.add(Calendar.DATE, -1);

            // Set ProgressBar and refresh counter
            if (progressBar.getVisibility() == View.VISIBLE)
            {
                locationRefreshCount++;
            }
            else
            {
                progressBar.setVisibility(View.VISIBLE);
                locationRefreshCount = 1;
            }

            // Find 2 character state abbreviation, if applicable
            loc.setUsStateAbbreviation(CovidUtils.getUSStateAbbreviation(loc));

            // Get stats
            Log.d("StatisticsFragment.parentOnRefresh()", MessageFormat.format("Location: {0}", CovidUtils.formatLocation(loc)));
            CovidStatService.getAllLocationStats(loc, new IServiceCallbackGeneric()
            {
                @Override
                public <T> void onSuccess(T result)
                {
                    Log.d("StatisticsFragment.parentOnRefresh()", MessageFormat.format("Location callback for: {0}", CovidUtils.formatLocation(loc)));
                    Location populatedLocation = (Location)result;
                    populatedLocation.setLastUpdated(new Date());
                    locationRefreshCount--;

                    // Stat for next day found, stop
                    SetLocation(populatedLocation);
                    loadLocations(locationsView, false);


                    if (locationRefreshCount == 0)
                    {
                        locationRefreshComplete = true;

                        // Dismiss progress indicator
                        clearProgressIndicators();
                        progressBar.setVisibility(View.GONE);

                        CovidApplication.setLocations(locations);
                    }

                    // Notify
                    Snackbar.make(getView(), "Location stats updated ", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }

                @Override
                public void onError(Error err)
                {
                    Log.e("StatisticsFragment.parentOnRefresh()", err.toString());
                }
            });
        }
    }

    private void clearProgressIndicators()
    {
        // Dismiss progress indicator
        swipeContainer.setRefreshing(false);
        manualRefresh = false;
        progressBar.setVisibility(View.GONE);


//        if (manualRefresh && globalRefreshComplete)
//            swipeContainer.setRefreshing(false);
//        else if (!manualRefresh)
//            progressBar.setVisibility(View.GONE);
    }


    /**
     * Class for handling the fetching of Location stats
     * asynchronously, off the Main UI thread
     */
    public class RefreshLocationStatistics extends AsyncTask<Boolean, Integer, String>
    {
        @Override
        protected void onPreExecute()
        {
            progressBar.setVisibility(View.VISIBLE);
            locationsUpdated = false;
        }

        @Override
        protected String doInBackground(Boolean... refresh)
        {
            for (int i = 0; i < refresh.length; i++)
            {
                RefreshLocations(refresh[i]);
            }

            // Wait for refresh to finish
            while (locationRefreshCount != 0);


            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            locationRefreshComplete = true;
            loadLocations(locationsView, false);
            if (locationsUpdated && locationRefreshComplete)
            {
                // Notify
                Snackbar.make(getView(), "Your statistical awesome-sauce has been updated", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }


            // Dismiss progress indicator
            if (globalRefreshComplete)
            {
                clearProgressIndicators();
            }
        }
    }

    /**
     * Class for handling the fetching of Global stats
     * asynchronously, off the Main UI thread
     */
    public class RefreshGlobalStatistics extends AsyncTask<Boolean, Integer, String>
    {
        @Override
        protected void onPreExecute()
        {
            progressBar.setVisibility(View.VISIBLE);
            locationsUpdated = false;
        }

        @Override
        protected String doInBackground(Boolean... refresh)
        {
            for (int i = 0; i < refresh.length; i++)
                retrieveGlobalStatsData(refresh[i]);

            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            globalRefreshComplete = true;
            if (locationRefreshComplete)
            {
                clearProgressIndicators();
            }
        }
    }
}

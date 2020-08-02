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
import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.MainActivity;
import com.chakfrost.covidstatistics.R;
import com.chakfrost.covidstatistics.adapters.LocationStatsRecyclerViewAdapter;
import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.GlobalStats;
import com.chakfrost.covidstatistics.models.HospitalizationStat;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.services.CovidService;
import com.chakfrost.covidstatistics.services.IServiceCallbackCovidStats;
import com.chakfrost.covidstatistics.services.IServiceCallbackList;
import com.chakfrost.covidstatistics.services.IServiceCallbackGlobalStats;
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
        new RefreshGlobalStatistics().execute(true);
        new RefreshLocationStatistics().execute(false);
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

                     // Notify
                     Snackbar.make(getView(), "Global stats already up to date", Snackbar.LENGTH_SHORT)
                             .setAction("Action", null).show();
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
                if (!CovidUtils.statExists(location.getStatistics()))     // <- TODO: change to !(not)
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
                    GetStatsForLocation(location, startDate, null, manualRefresh);
                }
                else
                {
                    if (CovidUtils.isUS(location) || CovidUtils.isUSState(location))
                    {
                        // Ensure state has abbreviation set
                        if (CovidUtils.isUSState(location) && TextUtils.isEmpty(location.getUsStateAbbreviation()))
                            location.setUsStateAbbreviation(CovidUtils.getUSStateAbbreviation(location));

                        // Get hospitalization stats
                        manualHospitalizationStatBackFill(location);
                    }
                }
            }
            else
            {
                if (CovidUtils.isUS(location) || CovidUtils.isUSState(location))
                {
                    // Ensure state has abbreviation set
                    if (CovidUtils.isUSState(location) && TextUtils.isEmpty(location.getUsStateAbbreviation()))
                        location.setUsStateAbbreviation(CovidUtils.getUSStateAbbreviation(location));

                    // Get hospitalization stats
                    manualHospitalizationStatBackFill(location);
                }
            }
        }

        //Log.d("RefreshLocations()", "locationRefreshCount: " + String.valueOf(locationRefreshCount) + "; globalRefreshComplete: " + String.valueOf(globalRefreshComplete));
        if (locationRefreshCount == 0)
            locationRefreshComplete = true;

        if (locationRefreshCount == 0 && (manualRefresh && globalRefreshComplete))
        {
            locationRefreshComplete = true;
            swipeContainer.setRefreshing(false);
        }
    }

    private void ProcessCovidStat(Location loc, CovidStats stat, Calendar dateToCheck,
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
    }

/*    private void LoadHospitalizations(CovidStats stat, Location loc, Calendar dateToCheck, boolean manualRefresh, String stateAbbreviation)
    {
        CovidService.getUSStateHospitalizations(stateAbbreviation, dateToCheck, new IServiceCallbackHospitalizationStat()
        {
            @Override
            public void onSuccess(HospitalizationStat hStat)
            {
                // Set Hospitalization stats for the current stat day
                stat.setHospitalizationsTotal(hStat.getHospitalizedTotal());
                stat.setHospitalizationsDiff(hStat.getHospitalizedChange());
                stat.setHospitalizationsCurrent(hStat.getHospitalizedCurrent());
                stat.setICUCurrent(hStat.getIcuCurrent());
                stat.setICUTotal(hStat.getIcuTotal());
            }

            @Override
            public void onError(VolleyError error)
            {
                //locationRefreshCount--;

                Log.e("CovidService.provinceHospitalization.onError()", error.getStackTrace().toString());
                Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.METHOD, "LoadHospitalizations.onError");
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "0");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Refresh");
                bundle.putString("Location_Stats_Error", error.getStackTrace().toString());
                firebaseAnalytics.logEvent("ERROR", bundle);

                // Dismiss progress indicator
                if (manualRefresh && locationRefreshComplete)
                    swipeContainer.setRefreshing(false);
                else if (!manualRefresh)
                    progressBar.setVisibility(View.GONE);
            }
        });
    }*/

    private void GetStatsForLocation(Location location, Calendar dateToCheck, List<HospitalizationStat> hospitalizationStats, boolean manualRefresh)
    {
        if (!manualRefresh)
            progressBar.setVisibility(View.VISIBLE);

        // Set update boolean for status
        locationsUpdated = true;

        // Check to back-fill hospitalization stats
        if (null == hospitalizationStats && location.getStatistics().size() > 0 && (CovidUtils.isUS(location) || CovidUtils.isUSState(location)))
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
                            GetCovidStat(location, dateToCheck, (List<HospitalizationStat>)list, manualRefresh);
                        }

                        @Override
                        public void onError(VolleyError err)
                        {
                            Log.e("StatisticsFragment.GetStatsForLocation()",
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
                            GetCovidStat(location, dateToCheck, (List<HospitalizationStat>)list, manualRefresh);
                        }

                        @Override
                        public void onError(VolleyError err)
                        {
                            Log.e("StatisticsFragment.GetStatsForLocation()",
                                    CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));
                        }
                    });
                }
            }
            else
            {
                GetCovidStat(location, dateToCheck, null, manualRefresh);
            }
        }
        else
        {
            GetCovidStat(location, dateToCheck, hospitalizationStats, manualRefresh);
        }
    }

    private void manualHospitalizationStatBackFill(Location location)
    {
        long occurrences = location.getStatistics().stream()
                .filter(s -> s.getHospitalizationsCurrent() != 0)
                .count();

        // Get Hospitalization Stats if List is null
        if (occurrences < 10)
        {
            Log.d("StatisticsFragment.manualHospitalizationStatBackFill()",
                    MessageFormat.format("Back-filling {0}", CovidUtils.formatLocation(location)));

            if (CovidUtils.isUS(location))
            {
                locationRefreshCount++;
                // Get stats for US
                CovidService.getUSHospitalizations(new IServiceCallbackList()
                {
                    @Override
                    public <T> void onSuccess(List<T> list)
                    {
                        // Loop dates to back-fill
                        PopulateHospitalizationStats(location, (List<HospitalizationStat>)list);
                        locationRefreshCount--;

                        // Save Location
                        SetLocation(location);
                    }

                    @Override
                    public void onError(VolleyError err)
                    {
                        Log.e("StatisticsFragment.manualHospitalizationStatBackFill()",
                                CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));
                        locationRefreshCount--;
                    }
                });
            }
            else if (CovidUtils.isUSState(location))
            {
                locationRefreshCount++;
                // Get US State abbreviation if not populated
                if (TextUtils.isEmpty(location.getUsStateAbbreviation()))
                {
                    location.setUsStateAbbreviation(CovidUtils.getUSStateAbbreviation(location));
                    if (TextUtils.isEmpty(location.getUsStateAbbreviation()))
                    {
                        Log.d("StatisticsFragment.manualHospitalizationStatBackFill()",
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
                        locationRefreshCount--;

                        // Save Location
                        SetLocation(location);
                    }

                    @Override
                    public void onError(VolleyError err)
                    {
                        Log.e("StatisticsFragment.manualHospitalizationStatBackFill()",
                                CovidUtils.formatError(err.getMessage(), err.getStackTrace().toString()));
                        locationRefreshCount--;
                    }
                });
            }
        }
    }

    private Location PopulateHospitalizationStats(Location location, List<HospitalizationStat> stats)
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

    private void GetCovidStat(Location location, Calendar dateToCheck, List<HospitalizationStat> hospitalizationStats, boolean manualRefresh)
    {
        CovidService.getCovidStat(location, dateToCheck, hospitalizationStats, new IServiceCallbackCovidStats()
        {
            @Override
            public void onSuccess(CovidStats stat)
            {
                ProcessCovidStat(location, stat, dateToCheck, hospitalizationStats, manualRefresh);
            }

            @Override
            public void onError(VolleyError err)
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
        locations.add(loc);

        // Wait until all updates are done before saving
        if (locationRefreshCount == 0)
        {
            // Save to local storage
            CovidApplication.setLocations(locations);

            clearProgressIndicators();
        }
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
                locationRefreshCount++;
            else
                locationRefreshCount = 1;

            // Find 2 character state abbreviation, if applicable
            loc.setUsStateAbbreviation(CovidUtils.getUSStateAbbreviation(loc));

            // Get stats
            if (!CovidUtils.isUS(loc) && !CovidUtils.isUSState(loc))
            {
                // Get report data; not US or US State
                GetStatsForLocation(loc, startDate, null, false);
            }
            else
            {
                if (CovidUtils.isUSState(loc))
                {
                    // Grab all hospitalization stats for state to be passed rather
                    // than making individual calls for each date
                    CovidService.getUSStateHospitalizations(loc.getUsStateAbbreviation(), new IServiceCallbackList()
                    {
                        @Override
                        public <T> void onSuccess(List<T> list)
                        {
                            // Now that we have the List<>, get report data
                            GetStatsForLocation(loc, startDate, (List<HospitalizationStat>) list, false);
                        }

                        @Override
                        public void onError(VolleyError err)
                        {
                            Log.d("StatisticsFragment.parentOnRefresh()", err.toString());
                        }
                    });
                }
                else if (CovidUtils.isUS(loc))
                {
                    CovidService.getUSHospitalizations(new IServiceCallbackList()
                    {
                        @Override
                        public <T> void onSuccess(List<T> list)
                        {
                            // Loop dates to back-fill
                            PopulateHospitalizationStats(loc, (List<HospitalizationStat>)list);

                            // Now that we have the List<>, get report data
                            GetCovidStat(loc, startDate, (List<HospitalizationStat>)list, false);
                        }

                        @Override
                        public void onError(VolleyError err)
                        {
                            Log.e("StatisticsFragment.parentOnRefresh()", err.toString());
                        }
                    });
                }
                else
                {
                    Log.d("StatisticsFragment.parentOnRefresh()",
                            "unable to create new location; neither US or US State yet pre-qualified with previous check");

                    Snackbar.make(getView(), "Unable to get statistics for " + CovidUtils.formatLocation(loc), Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
            }
        }
    }

    private void clearProgressIndicators()
    {
        // Dismiss progress indicator
        if (manualRefresh && globalRefreshComplete)
            swipeContainer.setRefreshing(false);
        else if (!manualRefresh)
            progressBar.setVisibility(View.GONE);
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
            if(!manualRefresh)
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
            if (locationsUpdated && locationRefreshComplete)
            {
                // Notify
                Snackbar.make(getView(), "Your statistical awesome-sauce has been updated", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }

            // Dismiss progress indicator
            if (globalRefreshComplete)
            {
                swipeContainer.setRefreshing(false);
                manualRefresh = false;
            }

            progressBar.setVisibility(View.GONE);
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
            if (!manualRefresh)
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
            if (locationRefreshComplete)
            {
                swipeContainer.setRefreshing(false);
                manualRefresh = false;
            }

            progressBar.setVisibility(View.GONE);
        }
    }
}

package com.chakfrost.covidstatistics.ui.statistics;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.BundleCompat;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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

    private RecyclerView locationsView;
    private LocationStatsRecyclerViewAdapter locationsListAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        statisticsViewModel = ViewModelProviders.of(requireActivity()).get(StatisticsViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

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

        locationsView = root.findViewById(R.id.stats_global_location_recycler_view);

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.show();

        //((MainActivity)getActivity()).setAppBarText(R.id.nav_home);

        loadGlobals();
        loadLocations(locationsView, true);

        return root;
    }

    private void loadGlobals()
    {
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

            // Only refresh data if more than 11 hours old
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

        confirmedValue.setText(NumberFormat.getInstance().format(summary.getTotalConfirmed()));
        confirmedDiff.setText(NumberFormat.getInstance().format(summary.getNewConfirmed()));
        if (summary.getNewConfirmed() > 0)
        {
            confirmedArrow.setImageResource(R.drawable.ic_arrow_drop_up_yellow_24dp);
            confirmedArrow.setVisibility(View.VISIBLE);
        }
        else if (summary.getNewConfirmed() < 0)
        {
            confirmedArrow.setImageResource(R.drawable.ic_arrow_drop_down_green_24dp);
            confirmedArrow.setVisibility(View.VISIBLE);
        }
        else
            confirmedArrow.setVisibility(View.INVISIBLE);

        deathsValue.setText(NumberFormat.getInstance().format(summary.getTotalDeaths()));
        deathsDiff.setText(NumberFormat.getInstance().format(summary.getNewDeaths()));
        if (summary.getNewDeaths() > 0)
        {
            deathsArrow.setImageResource(R.drawable.ic_arrow_drop_up_yellow_24dp);
            deathsArrow.setVisibility(View.VISIBLE);
        }
        else if (summary.getNewDeaths() < 0)
        {
            deathsArrow.setImageResource(R.drawable.ic_arrow_drop_down_green_24dp);
            deathsArrow.setVisibility(View.VISIBLE);
        }
        else
            confirmedArrow.setVisibility(View.INVISIBLE);

        recoveredValue.setText(NumberFormat.getInstance().format(summary.getTotalRecovered()));
        recoveredDiff.setText(NumberFormat.getInstance().format(summary.getNewRecovered()));
        if (summary.getNewRecovered() > 0)
        {
            recoveredArrow.setImageResource(R.drawable.ic_arrow_drop_up_green_24dp);
            recoveredArrow.setVisibility(View.VISIBLE);
        }
        else if (summary.getNewRecovered() < 0)
        {
            confirmedArrow.setImageResource(R.drawable.ic_arrow_drop_down_yellow_24dp);
            confirmedArrow.setVisibility(View.VISIBLE);
        }
        else
            confirmedArrow.setVisibility(View.INVISIBLE);
    }

    private void retrieveGlobalStatsData()
    {
        CovidService.summary(new IserviceCallbackGlobalStats()
                             {
                                 @Override
                                 public void onSuccess(GlobalStats stats)
                                 {
                                     summary = stats;
                                     summary.setLastUpdate(new Date());
                                     CovidApplication.setGlobalStats(summary);

                                     populateGlobalStats();
                                     Snackbar.make(getView(), "Global stats updated", Snackbar.LENGTH_SHORT)
                                             .setAction("Action", null).show();
                                     //Toast.makeText(getActivity(), "Global stats have been updated", Toast.LENGTH_SHORT).show();
                                 }

                                 @Override
                                 public void onError(VolleyError error)
                                 {
                                     //Log.e("LoadCountries.onError()", error.getMessage());
                                     Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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

        List<Location> locationsForDisplay = locations.stream()
                .filter(l -> l.getStatistics().size() > 0)
                .collect(Collectors.toList());

        if (locationsForDisplay.size() > 0)
        {
            // Set adapter
            locationsListAdapter = new LocationStatsRecyclerViewAdapter(getContext(), locationsForDisplay);
            locationsListAdapter.setClickListener(this::locationListAdapterClick);
            recyclerView.setAdapter(locationsListAdapter);
        }

        if (refreshLocations && locations.size() > 0)
            RefreshLocations();
    }

    private void locationListAdapterClick(Location selectedLocation)
    {
        Log.d("locationListAdapterClick()", CovidUtils.formatLocation(selectedLocation));
        statisticsViewModel.setLocation(selectedLocation);

        Log.d("statisticsViewModel.Location", CovidUtils.formatLocation(statisticsViewModel.getLocation().getValue()));

        Intent details = new Intent(getActivity(), LocationStatsDetail.class);
        details.putExtra("location", selectedLocation);

        startActivity(details);

        //((MainActivity)getActivity()).displaySelectedFragment(R.id.nav_location_details);

//        LocationDetailFragment locationDetailFragment = new LocationDetailFragment();
//        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.fragment_container, locationDetailFragment);
//        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();
    }

    private void RefreshLocations()
    {
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
                if (!CovidUtils.statExists(loc.getStatistics()))
                {
                    Calendar startDate = Calendar.getInstance();
                    startDate.add(Calendar.DATE, -1);
                    loc.setLastUpdated(new Date());
                    LoadReportData(loc, startDate);
                }
            }
        }
    }

    private void LoadReportData(Location loc, Calendar dateToCheck)
    {
        CovidService.report(loc.getIso(), loc.getProvince(), loc.getRegion(), loc.getMunicipality(), dateToCheck, new IServiceCallbackCovidStats()
                {
                    @Override
                    public void onSuccess(CovidStats stat)
                    {
                        if (null != stat) // && loc.getStatistics().size() < 5)
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
                                loadLocations(locationsView, false);
                                Snackbar.make(getView(), "Location stats updated ", Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                                //Toast.makeText(getActivity(), "Location stats have been updated", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                LoadReportData(loc, dateToCheck);
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
                            Log.e("GetReportData.onError()", error.getMessage());
                        else
                            Log.e("GetReportData.onError()", "Errors occurred while getting report.");
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
        CovidApplication.setLocations(locations);
    }
}

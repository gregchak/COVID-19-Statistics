package com.chakfrost.covidstatistics.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.R;
import com.chakfrost.covidstatistics.adapters.LocationStatsDetailRecyclerViewAdapter;
import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.models.LocationStats;
import com.chakfrost.covidstatistics.models.StatDatePair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationStatsDetail extends AppCompatActivity
{
    private Location location;
    private LocationStats confirmed;
    private LocationStats deaths;
    private LocationStats recovered;
    private LocationStats active;
    private List<LocationStats> locationStats;

    private RecyclerView locationStatDetailView;
    private LocationStatsDetailRecyclerViewAdapter locationsListAdapter;

    private int DAYS_TO_DISPLAY = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_stats_detail);

        Intent i = getIntent();
        location = (Location)i.getSerializableExtra("location");

        locationStatDetailView = findViewById(R.id.location_stat_detail_recycler_view);

        setTitle(CovidUtils.formatLocation(location));
        DAYS_TO_DISPLAY = CovidApplication.DAYS_TO_DISPLAY_DETAILS;

        buildLocationStats();

        bindLocationStats();
    }

    private void buildLocationStats()
    {
        CovidStats statTemp;
        int confirmedZeroCount = 0;
        int deathZeroCount = 0;
        int recoveredZeroCount = 0;
        int activeZeroCount = 0;

        confirmed = new LocationStats("Confirmed");
        deaths = new LocationStats("Deaths");
        recovered = new LocationStats("Recovered");
        active = new LocationStats("Active");

        List<CovidStats> statsForDisplay = location.getStatistics();
        Collections.sort(statsForDisplay);

        // Loop through all the Location's statistics
        for (int i = 0; i < statsForDisplay.size() && i < DAYS_TO_DISPLAY; i++)
        {
            statTemp  = statsForDisplay.get(i);

            if (confirmedZeroCount < 5)
                confirmed.addValue(statTemp.getStatusDate(), statTemp.getTotalConfirmed());

            if (statTemp.getTotalConfirmed() == 0)
                confirmedZeroCount++;
            else if (statTemp.getTotalConfirmed() != 0 && confirmedZeroCount < 5)
                confirmedZeroCount = 0;


            if (deathZeroCount < 5)
                deaths.addValue(statTemp.getStatusDate(), statTemp.getTotalDeaths());

            if (statTemp.getTotalDeaths() == 0)
                deathZeroCount++;
            else if (statTemp.getTotalDeaths() != 0 && deathZeroCount < 5)
                deathZeroCount = 0;


            if(recoveredZeroCount < 5)
                recovered.addValue(statTemp.getStatusDate(), statTemp.getTotalRecovered());

            if (statTemp.getTotalRecovered() == 0)
                recoveredZeroCount++;
            else if (statTemp.getTotalRecovered() != 0 && recoveredZeroCount < 5)
                recoveredZeroCount = 0;


            if (activeZeroCount < 5)
            active.addValue(statTemp.getStatusDate(), statTemp.getTotalActive());

            if (statTemp.getTotalActive() == 0)
                activeZeroCount++;
            else if (statTemp.getTotalActive() != 0 && activeZeroCount < 5)
                activeZeroCount = 0;
        }

        // Loop through all the Location's statistics
//        for (CovidStats stat: location.getStatistics())
//        {
//            confirmed.addValue(stat.getStatusDate(), stat.getTotalConfirmed());
//            deaths.addValue(stat.getStatusDate(), stat.getTotalDeaths());
//
//            // Only add if Recovered has a value
//            if (stat.getTotalRecovered() != 0)
//                recovered.addValue(stat.getStatusDate(), stat.getTotalRecovered());
//
//            // Only add if Active has a value
//            if (stat.getTotalActive() != 0)
//            active.addValue(stat.getStatusDate(), stat.getTotalActive());
//        }

        // Populate LocationStats List<>
        locationStats = new ArrayList<>();
        locationStats.add(confirmed);
        locationStats.add(deaths);

        StatDatePair findRecovered = recovered.getValues().stream()
                .filter(s -> s.getValue() != 0)
                .findFirst()
                .orElse(null);

        // Only add if Recovered has values
        if (null != findRecovered)
            locationStats.add(recovered);

        StatDatePair findActive = recovered.getValues().stream()
                .filter(s -> s.getValue() != 0)
                .findFirst()
                .orElse(null);

        // Only add if Active hs values
        if (null != findActive)
            locationStats.add(active);
    }

    private void bindLocationStats()
    {
        // Set the layout
        locationStatDetailView.setLayoutManager(new LinearLayoutManager(this));

        if (locationStats.size() > 0)
        {
            // Set adapter
            locationsListAdapter = new LocationStatsDetailRecyclerViewAdapter(locationStats);
            //locationsListAdapter.setClickListener(this::locationListAdapterClick);
            locationStatDetailView.setAdapter(locationsListAdapter);
        }
    }
}

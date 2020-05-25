package com.chakfrost.covidstatistics.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
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
    private LocationStats fatalityRate;
    private List<LocationStats> locationStats;

    private RecyclerView locationStatDetailView;
    private LocationStatsDetailRecyclerViewAdapter locationsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_stats_detail);

        Intent i = getIntent();
        location = (Location)i.getSerializableExtra("location");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        locationStatDetailView = findViewById(R.id.location_stat_detail_recycler_view);

        // Set the title of activity to Location name
        setTitle(CovidUtils.formatLocation(location));

        // Build the data to be displayed
        buildLocationStats();

        // Bind stats to adapter for display
        bindLocationStats();
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    private void buildLocationStats()
    {
        CovidStats statTemp;
        int confirmedZeroCount = 0;
        int deathZeroCount = 0;
        int recoveredZeroCount = 0;
        int activeZeroCount = 0;
        int fatalityZeroCount = 0;

        confirmed = new LocationStats("Confirmed");
        deaths = new LocationStats("Deaths");
        recovered = new LocationStats("Recovered");
        active = new LocationStats("Active");
        fatalityRate = new LocationStats("Fatality Rate (%)");

        List<CovidStats> statsForDisplay = location.getStatistics();
        Collections.sort(statsForDisplay);

        // Loop through all the Location's statistics
        for (int i = 0; i < statsForDisplay.size(); i++)
        {
            statTemp  = statsForDisplay.get(i);

            // Set confirmed values
            if (confirmedZeroCount < 4)
                confirmed.addValue(statTemp.getStatusDate(), statTemp.getTotalConfirmed());

            if (statTemp.getTotalConfirmed() == 0)
                confirmedZeroCount++;
            else if (statTemp.getTotalConfirmed() != 0 && confirmedZeroCount < 4)
                confirmedZeroCount = 0;

            // Set death values
            if (deathZeroCount < 4)
                deaths.addValue(statTemp.getStatusDate(), statTemp.getTotalDeaths());

            if (statTemp.getTotalDeaths() == 0)
                deathZeroCount++;
            else if (statTemp.getTotalDeaths() != 0 && deathZeroCount < 4)
                deathZeroCount = 0;

            // Set recovered values
            if(recoveredZeroCount < 4)
                recovered.addValue(statTemp.getStatusDate(), statTemp.getTotalRecovered());

            if (statTemp.getTotalRecovered() == 0)
                recoveredZeroCount++;
            else if (statTemp.getTotalRecovered() != 0 && recoveredZeroCount < 4)
                recoveredZeroCount = 0;

            // Set active values
            if (activeZeroCount < 4)
                active.addValue(statTemp.getStatusDate(), statTemp.getTotalActive());

            if (statTemp.getTotalActive() == 0)
                activeZeroCount++;
            else if (statTemp.getTotalActive() != 0 && activeZeroCount < 4)
                activeZeroCount = 0;

            if (fatalityZeroCount < 4)
                fatalityRate.addValue(statTemp.getStatusDate(), statTemp.getFatalityRate() * 100);

            if (statTemp.getFatalityRate() == 0)
                fatalityZeroCount++;
            else if (statTemp.getFatalityRate() != 0 && fatalityZeroCount < 4)
                fatalityZeroCount = 0;

        }

        // Populate LocationStats List<>
        locationStats = new ArrayList<>();
        locationStats.add(confirmed);
        locationStats.add(deaths);

        // Not all locations have Recovered values, verify this has data
        StatDatePair findRecovered = recovered.getValues().stream()
                .filter(s -> s.getValue() != 0)
                .findFirst()
                .orElse(null);

        // Only add if Recovered has values
        if (null != findRecovered)
            locationStats.add(recovered);

        // Not all locations have Active values, verify this has data
        StatDatePair findActive = active.getValues().stream()
                .filter(s -> s.getValue() != 0)
                .findFirst()
                .orElse(null);

        // Only add if Active hs values
        if (null != findActive)
            locationStats.add(active);

        // Not all locations have fatality rate values, verify this has data
        StatDatePair findFatality = fatalityRate.getValues().stream()
                .filter(s -> s.getValue() != 0)
                .findFirst()
                .orElse(null);

        // Only add if Active hs values
        if (null != findFatality)
            locationStats.add(fatalityRate);
    }

    private void bindLocationStats()
    {
        // Set the layout
        locationStatDetailView.setLayoutManager(new LinearLayoutManager(this));

        if (locationStats.size() > 0)
        {
            // Set adapter
            locationsListAdapter = new LocationStatsDetailRecyclerViewAdapter(locationStats);
            locationStatDetailView.setAdapter(locationsListAdapter);
        }
    }
}

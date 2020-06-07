package com.chakfrost.covidstatistics.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private LocationStats newConfirmed;
    private LocationStats deaths;
    private LocationStats newDeaths;
    private LocationStats recovered;
    private LocationStats newRecovered;
    private LocationStats active;
    private LocationStats newActive;
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
        newConfirmed = new LocationStats("New Confirmed");
        deaths = new LocationStats("Deaths");
        newDeaths = new LocationStats("New Deaths");
        recovered = new LocationStats("Recovered");
        newRecovered = new LocationStats("New Recovered");
        active = new LocationStats("Active");
        newActive = new LocationStats("New Active");
        fatalityRate = new LocationStats("Fatality Rate (%)");

        List<CovidStats> statsForDisplay = location.getStatistics();
        Collections.sort(statsForDisplay);

        // Loop through all the Location's statistics
        for (int i = 0; i < statsForDisplay.size(); i++)
        {
            statTemp  = statsForDisplay.get(i);

            // Set confirmed values
            if (confirmedZeroCount < 3)
            {
                confirmed.addValue(statTemp.getStatusDate(), statTemp.getTotalConfirmed());
                if (statTemp.getDiffConfirmed() >= 0)
                    newConfirmed.addValue(statTemp.getStatusDate(), statTemp.getDiffConfirmed());
            }

            if (statTemp.getTotalConfirmed() == 0)
                confirmedZeroCount++;
            else if (statTemp.getTotalConfirmed() != 0 && confirmedZeroCount < 4)
                confirmedZeroCount = 0;

            // Set death values
            if (deathZeroCount < 3)
            {
                deaths.addValue(statTemp.getStatusDate(), statTemp.getTotalDeaths());
                if (statTemp.getDiffDeaths() >= 0)
                    newDeaths.addValue(statTemp.getStatusDate(), statTemp.getDiffDeaths());
            }

            if (statTemp.getTotalDeaths() == 0)
                deathZeroCount++;
            else if (statTemp.getTotalDeaths() != 0 && deathZeroCount < 4)
                deathZeroCount = 0;

            // Set recovered values
            if(recoveredZeroCount < 3)
            {
                recovered.addValue(statTemp.getStatusDate(), statTemp.getTotalRecovered());
                if (statTemp.getDiffRecovered() >= 0)
                    newRecovered.addValue(statTemp.getStatusDate(), statTemp.getDiffRecovered());
            }

            if (statTemp.getTotalRecovered() == 0)
                recoveredZeroCount++;
            else if (statTemp.getTotalRecovered() != 0 && recoveredZeroCount < 4)
                recoveredZeroCount = 0;

            // Set active values
            if (activeZeroCount < 3)
            {
                active.addValue(statTemp.getStatusDate(), statTemp.getTotalActive());
                if (statTemp.getDiffActive() >= 0)
                    newActive.addValue(statTemp.getStatusDate(), statTemp.getDiffActive());
            }

            if (statTemp.getTotalActive() == 0)
                activeZeroCount++;
            else if (statTemp.getTotalActive() != 0 && activeZeroCount < 4)
                activeZeroCount = 0;

            // Set fatality rates
            if (fatalityZeroCount < 3)
                fatalityRate.addValue(statTemp.getStatusDate(), statTemp.getFatalityRate() * 100);

            if (statTemp.getFatalityRate() == 0)
                fatalityZeroCount++;
            else if (statTemp.getFatalityRate() != 0 && fatalityZeroCount < 4)
                fatalityZeroCount = 0;

        }

        // Populate LocationStats List<>
        locationStats = new ArrayList<>();
        Collections.reverse(confirmed.getValues());
        locationStats.add(confirmed);
        Collections.reverse(newConfirmed.getValues());
        locationStats.add(newConfirmed);
        Collections.reverse(deaths.getValues());
        locationStats.add(deaths);
        Collections.reverse(newDeaths.getValues());
        locationStats.add(newDeaths);

        // Not all locations have Recovered values, verify this has data
        StatDatePair findRecovered = recovered.getValues().stream()
                .filter(s -> s.getValue() != 0)
                .findFirst()
                .orElse(null);

        // Only add if Recovered has values
        if (null != findRecovered)
        {
            Collections.reverse(recovered.getValues());
            locationStats.add(recovered);
            Collections.reverse(newRecovered.getValues());
            locationStats.add(newRecovered);
        }

        // Not all locations have Active values, verify this has data
        StatDatePair findActive = active.getValues().stream()
                .filter(s -> s.getValue() != 0)
                .findFirst()
                .orElse(null);

        // Only add if Active hs values
        if (null != findActive)
        {
            Collections.reverse(active.getValues());
            locationStats.add(active);
            Collections.reverse(newActive.getValues());
            locationStats.add(newActive);
        }

        // Not all locations have fatality rate values, verify this has data
        StatDatePair findFatality = fatalityRate.getValues().stream()
                .filter(s -> s.getValue() != 0)
                .findFirst()
                .orElse(null);

        // Only add if Active hs values
        if (null != findFatality)
        {
            Collections.reverse(fatalityRate.getValues());
            locationStats.add(fatalityRate);
        }
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

package com.chakfrost.covidstatistics.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.R;
import com.chakfrost.covidstatistics.adapters.LocationStatsCombinedDetailRecyclerViewAdapter;
import com.chakfrost.covidstatistics.adapters.LocationStatsDetailRecyclerViewAdapter;
import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.models.LocationInfoStat;
import com.chakfrost.covidstatistics.models.LocationMetric;
import com.chakfrost.covidstatistics.models.LocationStats;
import com.chakfrost.covidstatistics.models.StatDatePair;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private LocationStats hospitalizations;
    private LocationStats newHospitalizations;
    private LocationStats icu;
    private LocationStats positivityRate;
    private LocationStats caseDensity;
    private List<LocationStats> locationStats;
    private List<LocationInfoStat> locationInfoStats;
    private List<Object> allStats;

    private RecyclerView locationStatDetailView;
    private RecyclerView locationStatCombinedView;
    private LocationStatsDetailRecyclerViewAdapter locationsListAdapter;
    private LocationStatsCombinedDetailRecyclerViewAdapter locationStatCombinedAdapter;

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
        locationStatCombinedView = findViewById(R.id.location_stat_detail_combined_recycler_view);

        // Set the title of activity to Location name
        setTitle(CovidUtils.formatLocation(location));

        // Build the data to be displayed
        allStats = new ArrayList<>();
        bindLocationInfoStats();
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

    private void bindLocationInfoStats()
    {
        SimpleDateFormat statusDateFormat = new SimpleDateFormat("MM/dd/yy");
        LocationMetric metric;
        locationInfoStats = new ArrayList<>();
        List<LocationMetric> metrics = new ArrayList<>();

        // Vaccinations
        Optional<CovidStats> infoStat = location.getStatistics().stream()
                .filter(s -> !s.getVaccinationsInitiated().equals(null))
                .sorted()
                .findFirst();

        if (infoStat.isPresent())
        {
            locationInfoStats = new ArrayList<>();
            metrics = new ArrayList<>();
            metric = new LocationMetric("Initiated:", MessageFormat.format("{0} ({1}%)", infoStat.get().getVaccinationsInitiated(), infoStat.get().getVaccinationsInitiatedPercentage()));
            metrics.add((metric));
            metric = new LocationMetric("Completed:", MessageFormat.format("{0} ({1}%)", infoStat.get().getVaccinationsCompleted(), infoStat.get().getVaccinationsCompletedPercentage()));
            metrics.add((metric));

            LocationInfoStat stat = new LocationInfoStat(MessageFormat.format("Vaccinations ({0})",  statusDateFormat.format(infoStat.get().getStatusDate())), metrics);
            locationInfoStats.add(stat);
            allStats.add(stat);
        }

        // Risk Levels
        infoStat = location.getStatistics().stream()
                .filter(s -> !s.getCdcTransmissionLevel().equals(null))
                .sorted()
                .findFirst();

        Optional<CovidStats> caseDensity = location.getStatistics().stream()
                .filter(s -> s.getCaseDensity() != 0)
                .sorted()
                .findFirst();

        if (infoStat.isPresent() || caseDensity.isPresent())
        {
            metrics = new ArrayList<>();
            if (infoStat.isPresent())
            {
                metric = new LocationMetric("CDC Transmission Level:", infoStat.get().getCdcTransmissionLevel());
                metrics.add((metric));
            }

            if (caseDensity.isPresent())
            {
                metric = new LocationMetric("Case Density:", MessageFormat.format("{0} per 100k people", caseDensity.get().getCaseDensity()));
                metrics.add((metric));
            }

            LocationInfoStat stat = new LocationInfoStat(MessageFormat.format("Risk Levels ({0})",  statusDateFormat.format(infoStat.get().getStatusDate())), metrics);
            locationInfoStats.add(stat);
            allStats.add(stat);
        }

        // Hospitalizations
        infoStat = location.getStatistics().stream()
                .filter(s -> s.getHospitalizationsCurrent() != null)
                .sorted()
                .findFirst();

        if (infoStat.isPresent())
        {
            String message;
            double percentage;

            metrics = new ArrayList<>();
            message = MessageFormat.format("{0} in use", infoStat.get().getHospitalizationsCurrent());
            if (null != infoStat.get().getHospitalizationsCovidCurrent() && infoStat.get().getHospitalizationsCovidCurrent() != 0)
            {
                percentage = (double)infoStat.get().getHospitalizationsCovidCurrent() / infoStat.get().getHospitalizationsCurrent() * 100;
                message = MessageFormat.format("{0}, {1}% COVID", message, String.format("%.0f", percentage));
            }

            if (null != infoStat.get().getHospitalizationCapacity() && infoStat.get().getHospitalizationCapacity() != 0)
            {
                percentage = (double)infoStat.get().getHospitalizationsCurrent() / infoStat.get().getHospitalizationCapacity() * 100;
                message = MessageFormat.format("{0}, {1}% full", message, String.format("%.0f", percentage));
            }
            metric = new LocationMetric("Hospital Beds:",message);
            metrics.add((metric));

            message = MessageFormat.format("{0} in use", infoStat.get().getICUCurrent());
            if (null != infoStat.get().getICUCovidCurrent() && infoStat.get().getICUCovidCurrent() != 0)
            {
                percentage = (double)infoStat.get().getICUCovidCurrent() / infoStat.get().getICUCurrent() * 100;
                message = MessageFormat.format("{0}, {1}% COVID", message, String.format("%.0f", percentage));
            }

            if ((null != infoStat.get().getICUCapacity() && infoStat.get().getICUCapacity() != 0)
            && (null != infoStat.get().getICUCovidCurrent() && infoStat.get().getICUCovidCurrent() != 0))
            {
                percentage = (double)infoStat.get().getICUCovidCurrent() / infoStat.get().getICUCapacity() * 100;
                message = MessageFormat.format("{0}, {1}% full", message, String.format("%.0f", percentage));
            }
            metric = new LocationMetric("ICU Beds:",message);
            metrics.add((metric));

            LocationInfoStat stat = new LocationInfoStat(MessageFormat.format("Hospitalizations ({0})",  statusDateFormat.format(infoStat.get().getStatusDate())), metrics);
            locationInfoStats.add(stat);
            allStats.add(stat);
        }
    }

    private void buildLocationStats()
    {
        CovidStats statTemp;
        int confirmedZeroCount = 0;
        int deathZeroCount = 0;
        int recoveredZeroCount = 0;
        int activeZeroCount = 0;
        int fatalityZeroCount = 0;
        int hospitalizationZeroCount = 0;
        int icuZeroCount = 0;
        int positivityZeroCount = 0;
        int caseDensityCount = 0;

        confirmed = new LocationStats("Confirmed");
        newConfirmed = new LocationStats("Confirmed (7 day average)");
        deaths = new LocationStats("Deaths");
        newDeaths = new LocationStats("Deaths");
        recovered = new LocationStats("Recovered");
        newRecovered = new LocationStats("New Recovered");
        active = new LocationStats("Active");
        newActive = new LocationStats("New Active");
        fatalityRate = new LocationStats("Fatality Rate (%)");
        positivityRate = new LocationStats("Positivity Rate (%)");

        hospitalizations = new LocationStats("Hospitalizations");
        newHospitalizations = new LocationStats("New Hospitalizations");
        icu = new LocationStats("ICU");
        caseDensity = new LocationStats("Case Density (per 100k)");

        List<CovidStats> statsForDisplay = location.getStatistics();
        Collections.sort(statsForDisplay);

        // Loop through all the Location's statistics
        for (int i = 0; i < statsForDisplay.size(); i++)
        {
            statTemp  = statsForDisplay.get(i);

            // Set confirmed values
            if (confirmedZeroCount < 20)
            {
                confirmed.addValue(statTemp.getStatusDate(), statTemp.getTotalConfirmed());
                if (statTemp.getDiffConfirmed() >= 0)
                    newConfirmed.addValue(statTemp.getStatusDate(), statTemp.getAverageConfirmed());
            }

            if (statTemp.getAverageConfirmed() == 0)
                confirmedZeroCount++;
            else if (statTemp.getAverageConfirmed() != 0 && confirmedZeroCount < 4)
                confirmedZeroCount = 0;

            // Set death values
            if (deathZeroCount < 3)
            {
                deaths.addValue(statTemp.getStatusDate(), statTemp.getTotalDeaths());
                if (statTemp.getDiffDeaths() >= 0)
                    newDeaths.addValue(statTemp.getStatusDate(), statTemp.getNewDeaths());
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


            // Set hospitalizations
            if (null != statTemp.getHospitalizationsCovidCurrent())
            {
                if (statTemp.getHospitalizationsCovidCurrent() == 0)
                    hospitalizationZeroCount++;
                else if (statTemp.getHospitalizationsCovidCurrent() != 0 && hospitalizationZeroCount < 10)
                    hospitalizationZeroCount = 0;

                if (hospitalizationZeroCount < 10)
                    hospitalizations.addValue(statTemp.getStatusDate(), statTemp.getHospitalizationsCovidCurrent());
            }


            // Set ICU
            if (null != statTemp.getICUCovidCurrent())
            {
                if (statTemp.getICUCovidCurrent() == 0)
                    icuZeroCount++;
                else if (statTemp.getICUCovidCurrent() != 0 && icuZeroCount < 10)
                    icuZeroCount = 0;

                if (icuZeroCount < 10)
                    icu.addValue(statTemp.getStatusDate(), statTemp.getICUCovidCurrent());
            }

            // Set Case Density
            if (caseDensityCount < 3)
                caseDensity.addValue(statTemp.getStatusDate(), statTemp.getCaseDensity());

            if (statTemp.getCaseDensity() == 0)
                caseDensityCount++;
            else if (statTemp.getCaseDensity() != 0 && caseDensityCount < 4)
                caseDensityCount = 0;


            // Set positivity rates
            if (positivityZeroCount < 3)
                positivityRate.addValue(statTemp.getStatusDate(), statTemp.getPositivityRate());

            if (statTemp.getPositivityRate() == 0)
                positivityZeroCount++;
            else if (statTemp.getPositivityRate() != 0 && positivityZeroCount < 4)
                positivityZeroCount = 0;
        }

        // Populate LocationStats List<>
        locationStats = new ArrayList<>();
//        Collections.reverse(confirmed.getValues());
//        locationStats.add(confirmed);
        Collections.reverse(newConfirmed.getValues());
        locationStats.add(newConfirmed);
        allStats.add(newConfirmed);
//        Collections.reverse(deaths.getValues());
//        locationStats.add(deaths);
        Collections.reverse(newDeaths.getValues());
        locationStats.add(newDeaths);
        allStats.add(newDeaths);

        StatDatePair findCaseDensity = caseDensity.getValues().stream()
                .filter(s -> s.getValue() != 0)
                .findFirst()
                .orElse(null);


        // Only add if fatality rate hs values
        if (null != findCaseDensity)
        {
            Collections.reverse(caseDensity.getValues());
            locationStats.add(caseDensity);
            allStats.add(caseDensity);
        }

        // Not all locations have hospitalization and ICU values, verify this has data

        StatDatePair findHospitalization = hospitalizations.getValues().stream()
                .filter(s -> s.getValue() != 0)
                .sorted(Collections.reverseOrder())
                .findFirst()
                .orElse(null);

        // Only add if hospitalizations have values
        if (null != findHospitalization)
        {

            Collections.reverse(hospitalizations.getValues());
            locationStats.add(hospitalizations);
            allStats.add(hospitalizations);
        }

        // Not all locations have ICU values, verify this has data
        StatDatePair findICU = icu.getValues().stream()
                .filter(s -> s.getValue() != 0)
                .findFirst()
                .orElse(null);

        if (null != findICU)
        {
            Collections.reverse(icu.getValues());
            locationStats.add(icu);
            allStats.add(icu);
        }

//        // Not all locations have Recovered values, verify this has data
//        StatDatePair findRecovered = recovered.getValues().stream()
//                .filter(s -> s.getValue() != 0)
//                .findFirst()
//                .orElse(null);
//
//        // Only add if Recovered has values
//        if (null != findRecovered)
//        {
//            Collections.reverse(recovered.getValues());
//            locationStats.add(recovered);
//            allStats.add(recovered);
//            Collections.reverse(newRecovered.getValues());
//            locationStats.add(newRecovered);
//            allStats.add(newRecovered);
//        }
//
//        // Not all locations have Active values, verify this has data
//        StatDatePair findActive = active.getValues().stream()
//                .filter(s -> s.getValue() != 0)
//                .findFirst()
//                .orElse(null);
//
//        // Only add if Active hs values
//        if (null != findActive)
//        {
//            Collections.reverse(active.getValues());
//            locationStats.add(active);
//            allStats.add(active);
//            Collections.reverse(newActive.getValues());
//            locationStats.add(newActive);
//            allStats.add(newActive);
//        }
//
        // Not all locations have positivity rate values, verify this has data


        // Not all locations have positivity rate values, verify this has data
        StatDatePair findPositivity = positivityRate.getValues().stream()
                .filter(s -> s.getValue() != 0)
                .findFirst()
                .orElse(null);


        // Only add if fatality rate hs values
        if (null != findPositivity)
        {
            Collections.reverse(positivityRate.getValues());
            locationStats.add(positivityRate);
            allStats.add(positivityRate);
        }

        // Not all locations have fatality rate values, verify this has data
        StatDatePair findFatality = fatalityRate.getValues().stream()
                .filter(s -> s.getValue() != 0)
                .findFirst()
                .orElse(null);

        // Only add if fatality rate hs values
        if (null != findFatality)
        {
            Collections.reverse(fatalityRate.getValues());
            locationStats.add(fatalityRate);
            allStats.add(fatalityRate);
        }
    }

    private void bindLocationStats()
    {
        // Set the layout
        locationStatDetailView.setLayoutManager(new LinearLayoutManager(this));
        locationStatCombinedView.setLayoutManager(new LinearLayoutManager(this));

        if (allStats.size() > 0)
        {
            locationStatCombinedAdapter = new LocationStatsCombinedDetailRecyclerViewAdapter(allStats);
            locationStatCombinedView.setAdapter(locationStatCombinedAdapter);
        }
    }
}

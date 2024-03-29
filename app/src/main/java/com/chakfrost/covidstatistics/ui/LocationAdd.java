package com.chakfrost.covidstatistics.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.MainActivity;
import com.chakfrost.covidstatistics.R;
import com.chakfrost.covidstatistics.models.Country;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.models.Municipality;
import com.chakfrost.covidstatistics.models.Province;
import com.chakfrost.covidstatistics.services.CovidService;
import com.chakfrost.covidstatistics.services.IServiceCallbackList;
import com.google.android.material.button.MaterialButton;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LocationAdd extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private List<Country> countries;
    private List<Province> provinces;
    private List<String> emptyMunicipalityList;
    private List<String> municipalities;
    private List<Municipality> municipalitiesObj;
    private Country selectedCountry;
    private Spinner countrySpinner;
    private Province selectedProvince;
    private Spinner provinceSpinner;
    private String selectedMunicipality;
    private Spinner municipalitySpinner;
    private String selectedMunicipalityFips;
    private MaterialButton addLocation;

    private final String ALL_VALUE = "-ALL-";
    private FrameLayout progress;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_add);

        MaterialButton back = findViewById(R.id.back_to_main);
        //Button back = findViewById(R.id.back_to_main);
        back.setOnClickListener(v ->
        {
            Intent backToMain = new Intent(getApplicationContext(), MainActivity.class);
            setResult(100, backToMain);
            finish();
        });

        countrySpinner = findViewById(R.id.location_spCountries);
        countrySpinner.setOnItemSelectedListener(this);

        provinceSpinner = findViewById(R.id.location_spProvinces);
        provinceSpinner.setOnItemSelectedListener(this);

        municipalitySpinner = findViewById(R.id.location_spMunicipalities);
        municipalitySpinner.setOnItemSelectedListener(this);

        addLocation = findViewById(R.id.location_btnAdd);
        addLocation.setOnClickListener(this::btnAddClick);

        progress = findViewById(R.id.progress_bar);
        progressBar = progress.findViewById(R.id.progress_bar_top);

        emptyMunicipalityList = new ArrayList<>();
        emptyMunicipalityList.add(ALL_VALUE);

        LoadCountries();
    }

    private void LoadCountries()
    {
        if (CovidApplication.getCountries() == null)
        {
            CovidService.countries(new IServiceCallbackList()
            {
               @Override
               public <T> void onSuccess(List<T> list)
               {
                   countries = (ArrayList<Country>) list;
                   countries.add(new Country("-SELECT-", "-SELECT-"));
                   Collections.sort(countries, (s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));

                   Country us = countries.stream()
                           .filter(c -> c.getName().equals("US"))
                           .findFirst()
                           .orElse(null);

                   if (null != us)
                   {
                       //countries.remove(us);
                       countries.add(1, us);
                   }

                   CovidApplication.setCountries(countries);

                   PopulateCountries();
               }

               @Override
               public void onError(VolleyError error)
               {
                   Log.e("LoadCountries.onError()", error.getStackTrace().toString());
                   Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
               }
            });
        }
        else
        {
            countries = CovidApplication.getCountries();

            PopulateCountries();
        }
    }

    private void PopulateCountries()
    {
        try
        {
            // Setup adapter
            ArrayAdapter<Country> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, countries);

            // Set the adapter's resource
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Set the spinner's adapter
            countrySpinner.setAdapter(adapter);
        }
        catch (Exception ex)
        {
            Log.e("PopulateCountries", ex.getStackTrace().toString());
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void LoadProvinces(Country country)
    {

        CovidService.provinces(country, new IServiceCallbackList()
                {
                    @Override
                    public <T> void onSuccess(List<T> list)
                    {
                        provinces = (ArrayList<Province>) list;
                        List<Province> collection = new ArrayList<>();
                        if (provinces.size() == 1)
                        {
                            collection.add(new Province(ALL_VALUE));
                        }
                        else
                        {
                            collection = provinces.stream()
                                    .filter(p -> !p.getName().contains(","))
                                    .collect(Collectors.toList());

                            collection.add(new Province(ALL_VALUE));
                        }

                        Collections.sort(collection, (s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));
                        PopulateProvince(collection);
                    }

                    @Override
                    public void onError(VolleyError error)
                    {
                        Log.e("LoadProvinces.onError()", error.getStackTrace().toString());
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    private void PopulateProvince(List<Province> collection)
    {
        try
        {
            // Setup adapter
            ArrayAdapter<Province> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, collection);

            // Set the adapter's resource
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Set the spinner's adapter
            provinceSpinner.setAdapter(adapter);
        }
        catch (Exception ex)
        {
            Log.e("PopulateProvince", ex.getMessage());
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void LoadMunicipalities()
    {
        CovidService.municipalities(selectedCountry.getISO2(), selectedProvince.getName(), selectedCountry.getName(), new IServiceCallbackList()
                {
                    @Override
                    public <T> void onSuccess(List<T> list)
                    {
                        if (null == list)
                            municipalities = new ArrayList<>();
                        else
                            municipalitiesObj = (ArrayList<Municipality>)list;

                        // Populate municipalities array for spinner
                        municipalities = new ArrayList<>();
                        for (Municipality m: municipalitiesObj)
                            municipalities.add(m.getName());

                        // Add ALL value
                        municipalities.add(ALL_VALUE);

                        Collections.sort(municipalities, (s1, s2) -> s1.compareToIgnoreCase(s2));

                        // Populate spinner
                        PopulateMunicipalities();
                    }

                    @Override
                    public void onError(VolleyError err)
                    {
                        Log.e("LoadMunicipalities", err.getStackTrace().toString());
                        Toast.makeText(getApplicationContext(), err.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void PopulateMunicipalities()
    {
        try
        {
            // Setup adapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, municipalities);

            // Set the adapter's resource
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Set the spinner's adapter
            municipalitySpinner.setAdapter(adapter);
        }
        catch (Exception ex)
        {
            Log.e("PopulateMunicipalities", ex.getMessage());
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


/*    private void SetLocation(Location loc)
    {
        if (progressBar.getProgress() < 90)
            progressBar.setProgress(90);

        CopyOnWriteArrayList<Location> locations = CovidApplication.getLocations();

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
    }*/

    private void SaveLocation()
    {
        selectedMunicipality = (String)municipalitySpinner.getItemAtPosition(municipalitySpinner.getSelectedItemPosition());
        selectedMunicipality = (String)municipalitySpinner.getSelectedItem();

        // Initialize report data
        Location loc = new Location(selectedCountry.getName());
        loc.setIso(selectedCountry.getISO2());
        loc.setRegion(selectedCountry.getName());

        if (!selectedProvince.getName().equals(ALL_VALUE))
            loc.setProvince(selectedProvince.getName());

        if (!selectedMunicipality.equals(ALL_VALUE))
        {
            loc.setMunicipality(selectedMunicipality);

            // Get Fips value
            Optional<Municipality> municipality = municipalitiesObj.stream()
                    .filter(m -> m.getName().equals(selectedMunicipality))
                    .findFirst();

            if (municipality.isPresent())
            {
                //String formatted = String.format("%07s", municipality.get().getFips());
                String formatted = ("00000" + municipality.get().getFips()).substring(municipality.get().getFips().length());
                loc.setFips(formatted);
                Log.d("LocationAdd.SaveLocation()", MessageFormat.format("Municipality: {0}; fips: {1}", loc.getMunicipality(), loc.getFips()));

            }
        }

        // Check if specified Location is already saved
        List<Location> locations = CovidApplication.getLocations();

        Location found = locations.stream()
                .filter(l -> l.getCountry().equals(loc.getCountry())
                        && l.getProvince().equals(loc.getProvince())
                        && l.getMunicipality().equals(loc.getMunicipality()))
                .findFirst()
                .orElse(null);

        if (null != found)
        {
            Toast.makeText(this.getApplicationContext(), "This is already a saved location\n" + CovidUtils.formatLocation(found), Toast.LENGTH_LONG).show();
        }
        else
        {
//            SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
//            String inputString1 = "2020-01-01";

            try
            {
//                Date end = dtf.parse(inputString1);
//                Date now = new Date();
//                long diffMs = now.getTime() - end.getTime();
//
//                // Populate report information
//                Calendar startDate = Calendar.getInstance();
//                startDate.add(Calendar.DATE, -1);


                // Set result
                Intent backToMain = new Intent(getApplicationContext(), MainActivity.class);
                backToMain.putExtra("location", loc);
                setResult(110, backToMain);
                finish();
            }
            catch (Exception ex)
            {
                Log.e("SaveLocations()", ex.toString());
            }

        }
    }

/*    private void LoadReportData(Location loc, Calendar dateToCheck, final long totalDays)
    {
        progress.setVisibility(View.VISIBLE);

        CovidService.report(loc.getIso(), loc.getProvince(), loc.getRegion(), loc.getMunicipality(), dateToCheck, new IServiceCallbackCovidStats()
                {
                    @Override
                    public void onSuccess(CovidStats stat)
                    {
                        if (null != stat)
                        {
                            loc.getStatistics().add(stat);
                            dateToCheck.add(Calendar.DATE, -1);
                            updateProgressBar(totalDays, loc.getStatistics().size());
                            LoadReportData(loc, dateToCheck, totalDays);
                        }
                        else
                        {
                            Log.d("Report finished", Integer.toString(loc.getStatistics().size()));

                            SetLocation(loc);
                            progressBar.setProgress(95);
                            Intent backToMain = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(backToMain);
                            progressBar.setProgress(100);
                            Toast.makeText(getApplicationContext(), "Finished loading Location stats", Toast.LENGTH_LONG).show();
                            //progress.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(VolleyError error)
                    {
                        Log.e("GetReportData.onError()", error.getStackTrace().toString());
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }*/

/*    private void updateProgressBar(double total, double remaining)
    {
        int percentage = (int) ((remaining / total) *100);
        progressBar.setProgress(percentage, true);
    }*/

    public void btnAddClick(View view)
    {
        SaveLocation();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        switch (parent.getId())
        {
            case R.id.location_spCountries:
                Log.d("onSelectedItem", Long.toString(id));
                selectedCountry = (Country) parent.getItemAtPosition(position);
                Log.d("Country", selectedCountry.getName());

                // Populate provinces
                if (!selectedCountry.getISO2().equals("-SELECT-"))
                    LoadProvinces(selectedCountry);

                break;
            case R.id.location_spProvinces:
                selectedProvince = (Province)parent.getItemAtPosition(position);
                if (selectedProvince.getName().equals(ALL_VALUE) || selectedProvince.getName().equals(""))
                {
                    Button b = findViewById(R.id.location_btnAdd);
                    b.setEnabled(true);
                    b.setClickable(true);
                    municipalities = emptyMunicipalityList;
                    PopulateMunicipalities();
                }
                else
                {
                    LoadMunicipalities();
                }
                break;
            case R.id.location_spMunicipalities:
                selectedMunicipality = (String)parent.getItemAtPosition(position);
                //addLocation.setEnabled(true);
                addLocation.setClickable(true);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
}

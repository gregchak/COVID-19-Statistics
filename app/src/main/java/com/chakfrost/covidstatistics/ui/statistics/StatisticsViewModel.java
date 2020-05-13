package com.chakfrost.covidstatistics.ui.statistics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.chakfrost.covidstatistics.CovidDataStore;
import com.chakfrost.covidstatistics.models.GlobalStats;
import com.chakfrost.covidstatistics.models.Location;

import java.util.List;

public class StatisticsViewModel extends ViewModel
{

    private MutableLiveData<String> mText;
    private MutableLiveData<Location> selectedLocation;
    private MutableLiveData<GlobalStats> globalStatistics;
    private MutableLiveData<List<Location>> statisticLocations;

    public StatisticsViewModel()
    {
        mText = new MutableLiveData<>();
        selectedLocation = new MutableLiveData<>();
    }


    /** Getters **/
    public LiveData<String> getText() { return mText; }
    public LiveData<Location> getSelectedLocation() { return selectedLocation; }
    public LiveData<GlobalStats> getGlobalStats() { return globalStatistics; }
    public LiveData<List<Location>> getStatisticLocations() { return statisticLocations; }

    /** Setters **/
    public void setSelectedLocation(Location selectedLocation) { this.selectedLocation.setValue((selectedLocation)); }
    public void setGlobalStatistics(GlobalStats val) { this.globalStatistics.setValue(val); }
    public void setStatisticLocations(List<Location> val) { this.statisticLocations.setValue(val); }

}
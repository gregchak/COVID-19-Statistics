package com.chakfrost.covidstatistics.ui.statistics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.chakfrost.covidstatistics.models.Location;

public class StatisticsViewModel extends ViewModel
{

    private MutableLiveData<String> mText;

    private MutableLiveData<Location> location;

    public StatisticsViewModel()
    {
        mText = new MutableLiveData<>();
        location = new MutableLiveData<>();
    }


    /** Getters **/
    public LiveData<String> getText() { return mText; }

    public LiveData<Location> getLocation() { return location; }

    /** Setters **/
    public void setLocation(Location location) { this.location.setValue((location)); }


}
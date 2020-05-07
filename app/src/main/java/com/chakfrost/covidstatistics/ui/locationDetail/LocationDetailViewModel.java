package com.chakfrost.covidstatistics.ui.locationDetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LocationDetailViewModel extends ViewModel
{

    private MutableLiveData<String> mText;

    public LocationDetailViewModel()
    {
        mText = new MutableLiveData<>();
        mText.setValue("This is add location fragment");
    }

    public LiveData<String> getText()
    {
        return mText;
    }
}
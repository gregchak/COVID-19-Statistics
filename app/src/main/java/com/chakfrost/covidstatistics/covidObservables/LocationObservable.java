package com.chakfrost.covidstatistics.covidObservables;

import com.chakfrost.covidstatistics.enums.Actions;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.models.LocationObservableModel;

import java.util.Observable;

public class LocationObservable extends Observable
{
    private static LocationObservable INSTANCE = null;
    public static LocationObservable getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new LocationObservable();
        }
        return INSTANCE;
    }

    public void locationUpdated(Location location)
    {
        LocationObservableModel model = new LocationObservableModel(location, Actions.Update);
        setChanged();
        notifyObservers(model);
    }

    public void locationAdded(Location location)
    {
        LocationObservableModel model = new LocationObservableModel(location, Actions.Add);
        setChanged();
        notifyObservers(model);
    }
}

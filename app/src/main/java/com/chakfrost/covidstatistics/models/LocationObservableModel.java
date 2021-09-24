package com.chakfrost.covidstatistics.models;

import com.chakfrost.covidstatistics.enums.Actions;

public class LocationObservableModel
{
    public Location location;
    public Actions action;

    public LocationObservableModel (Location _loc, Actions _action)
    {
        location = _loc;
        action = _action;
    }
}



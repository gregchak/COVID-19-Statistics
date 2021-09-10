package com.chakfrost.covidstatistics.covidObservables;

import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.models.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.CompletableFuture;

public class LocationsRepository extends Observable
{
    private List<Location> locations = new ArrayList<>();
    private static LocationsRepository INSTANCE = null;
    public static LocationsRepository getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new LocationsRepository();
        }
        return INSTANCE;
    }

    private LocationsRepository()
    {
        // Get locations from storage
//        CompletableFuture.supplyAsync(() -> CovidApplication.getLocations())
//        .thenAcceptAsync(result -> {
//            locations = result;
//            setChanged();
//            notifyObservers(locations);
//        });
    }

    public void setLocations(List<Location> newLocations)
    {
        locations = newLocations;
        setChanged();
        notifyObservers(locations);

        CompletableFuture.runAsync(() -> CovidApplication.setLocations(locations));
    }

    public List<Location> getLocations() { return locations; }
}

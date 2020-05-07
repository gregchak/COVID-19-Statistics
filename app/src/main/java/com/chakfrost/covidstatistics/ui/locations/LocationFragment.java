package com.chakfrost.covidstatistics.ui.locations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.R;
import com.chakfrost.covidstatistics.adapters.LocationSimpleListRecyclerViewAdapter;
import com.chakfrost.covidstatistics.models.Location;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

public class LocationFragment extends Fragment
{
    //private LocationViewModel locationViewModel;

    private List<Location> locations;
    private RecyclerView locationsSimpleListView;
    private LocationSimpleListRecyclerViewAdapter locationsSimpleListAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //locationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_locations, container, false);
        //final TextView textView = root.findViewById(R.id.text_gallery);

        //locationViewModel.getText().observe(getViewLifecycleOwner(), s -> textView.setText(s));

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.hide();

        locationsSimpleListView = root.findViewById(R.id.locations_recycler_view);
        locationsSimpleListView.addItemDecoration(new DividerItemDecoration(
                locationsSimpleListView.getContext(),
                DividerItemDecoration.VERTICAL
        ));

        loadLocations();
        return root;
    }

    private void loadLocations()
    {
        if (null == locations)
            locations = CovidApplication.getLocations();

        Collections.sort(locations);

        // Set the recycler layout
        locationsSimpleListView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Bind the adapter
        locationsSimpleListAdapter = new LocationSimpleListRecyclerViewAdapter(locations);
        locationsSimpleListView.setAdapter(locationsSimpleListAdapter);
        locationsSimpleListAdapter.setClickListener(this::deleteLocation);
    }

    private void deleteLocation(View view, int position)
    {
        // Get Location to delete
        Location toDelete = locations.get(position);

        String locationName = CovidUtils.formatLocation(toDelete);

        // Delete selected Location
        locations.remove(toDelete);

        // Save Location List<> to storage
        CovidApplication.setLocations(locations);

        // Notify user
        Snackbar.make(view, "Removed " + locationName, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();

        // Reload list
        loadLocations();
    }
}

package com.chakfrost.covidstatistics.ui.locations;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.R;
import com.chakfrost.covidstatistics.adapters.LocationSimpleListRecyclerViewAdapter;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.models.OperationActions;
import com.chakfrost.covidstatistics.services.CovidStatService;
import com.chakfrost.covidstatistics.services.IServiceCallbackGeneric;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

public class LocationFragment extends Fragment
{
    //private LocationViewModel locationViewModel;

    private List<Location> locations;
    private RecyclerView locationsSimpleListView;
    private LocationSimpleListRecyclerViewAdapter locationsSimpleListAdapter;
    private ProgressBar locationsProgressBar;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //locationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_locations, container, false);
        //final TextView textView = root.findViewById(R.id.text_gallery);

        //locationViewModel.getText().observe(getViewLifecycleOwner(), s -> textView.setText(s));

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.hide();

        locationsProgressBar = root.findViewById(R.id.locations_progress_bar);
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
        locationsSimpleListAdapter.setClickListener(this::locationSelected);
    }

    private void locationSelected(OperationActions action, View view, int position)
    {
        // Get Location to delete
        Location location = locations.get(position);



        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (action == OperationActions.DELETE)
        {
            // Set buttons
            builder.setPositiveButton("Remove", (dialog, id) -> RemoveLocation(view, location));
            builder.setNegativeButton("Cancel", (dialog, id) ->
            {
            });

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("Are you sure you want to remove " + CovidUtils.formatLocation(location) + "?")
                    .setTitle("Remove location?");
        }
        else if (action == OperationActions.REFRESH)
        {
            builder.setPositiveButton("Refresh", (dialog, id) -> RefreshLocationStats(view, location));
            builder.setNegativeButton("Cancel", (dialog, id) ->
            {
            });

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("Are you sure you want to refresh all stats for " + CovidUtils.formatLocation(location) + "?")
                    .setTitle("Refresh location?");
        }
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void RemoveLocation(View view, Location toDelete)
    {
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

    private void RefreshLocationStats(View view, Location location)
    {
        String locationName = CovidUtils.formatLocation(location);

        new RefreshLocationStatistics().execute(location);

        // Notify user
        Snackbar.make(view, "Refreshing stats for " + locationName, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();

        // Reload list
        loadLocations();
    }

/*    public void setProgressDialog()
    {

        int llPadding = 30;
        LinearLayout ll = new LinearLayout(this.getActivity());
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        ProgressBar progressBar = new ProgressBar(this.getActivity());
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(llParam);

        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        TextView tvText = new TextView(this.getActivity());
        tvText.setText("Loading ...");
        tvText.setTextColor(Color.parseColor("#000000"));
        tvText.setTextSize(20);
        tvText.setLayoutParams(llParam);

        ll.addView(progressBar);
        ll.addView(tvText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setCancelable(true);
        builder.setView(ll);

        AlertDialog dialog = builder.create();
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }
    }*/

    /**
     * Class for handling the fetching of Location stats
     * asynchronously, off the Main UI thread
     */
    public class RefreshLocationStatistics extends AsyncTask<Location, Integer, Boolean>
    {
        private boolean isComplete;
        private Boolean isSuccessful;
        private Location currentLocation;
        @Override
        protected void onPreExecute()
        {
            // Show progressBar
            locationsProgressBar.setVisibility(View.VISIBLE);

            // Set progress variables
            isComplete = false;
            isSuccessful = false;
        }

        @Override
        protected Boolean doInBackground(Location... location)
        {
            for (int i = 0; i < location.length; i++)
            {
                currentLocation = location[i];
                CovidStatService.getAllLocationStats(currentLocation, new IServiceCallbackGeneric()
                {
                    @Override
                    public <T> void onSuccess(T result)
                    {
                        Location updatedLocation = (Location)result;

                        Location found = locations.stream()
                                .filter(l -> l.getCountry().equals(currentLocation.getCountry())
                                        && l.getProvince().equals(currentLocation.getProvince())
                                        && l.getMunicipality().equals(currentLocation.getMunicipality()))
                                .findFirst()
                                .orElse(null);

                        if (null != found)
                        {
                            // Location is being updated
                            locations.remove(found);
                        }

                        // Save Location
                        locations.add(updatedLocation);

                        // Save to local storage
                        CovidApplication.setLocations(locations);

                        isSuccessful = true;
                        isComplete = true;
                    }

                    @Override
                    public void onError(Error err)
                    {
                        isSuccessful = false;
                        isComplete = true;
                    }
                });
            }

            while (!isComplete) {}

            return isSuccessful;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            // Hide progressBar
            locationsProgressBar.setVisibility(View.GONE);

            if (result)
            {
                if(null != getView())
                {
                    // Notify
                    Snackbar.make(getView(), "Location stats updated ", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
            }
            else
            {
                if(null != getView())
                {
                    // Notify
                    Snackbar.make(getView(),
                            MessageFormat.format("Errors occurred while updating {0}", CovidUtils.formatLocation(currentLocation)),
                            Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
            }
        }
    }
}

package com.chakfrost.covidstatistics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.chakfrost.covidstatistics.interfaces.IFragmentRefreshListener;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.ui.LocationAdd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
{
    private AppBarConfiguration mAppBarConfiguration;
    private IFragmentRefreshListener fragmentRefreshListener;
    //private static final int RESULT_LOCATION_CANCEL = 100;
    private static final int RESULT_LOCATION_ADD = 110;
    private static final int REQUEST_CODE_LOCATION_ADD = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set click event for floating "+" to go to Add Location activity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v ->
        {
            Intent locationIntent = new Intent(getApplicationContext(), LocationAdd.class);
            startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_ADD);
        });

        // Get navigation views
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Set change event for Notifications switch in side nav bar
        //Menu menu = navigationView.getMenu();
        Switch notificationsSwitch = navigationView.getMenu().findItem(R.id.app_bar_switch)
                .getActionView().findViewById(R.id.switch_item);
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            CovidApplication.setReceiveNotifications(isChecked);
        });
        notificationsSwitch.setChecked(CovidApplication.getReceiveNotifications());


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_locations, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public IFragmentRefreshListener getFragmentRefreshListener() {
        return fragmentRefreshListener;
    }

    public void setFragmentRefreshListener(IFragmentRefreshListener fragmentRefreshListener) {
        this.fragmentRefreshListener = fragmentRefreshListener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_LOCATION_ADD)
        {
            if (resultCode == RESULT_LOCATION_ADD)
            {
                if (getFragmentRefreshListener() != null)
                    getFragmentRefreshListener().onRefresh(resultCode, data);
            }
        }
    }

    /*
    public void displaySelectedFragment(int itemId)
    {
        //creating fragment object
        Fragment fragment;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_location_details:
                fragment = new LocationDetailFragment();
                break;
            default:
                fragment = null;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            //ft.add(fragment, "location_details");
            //ft.add(R.id.fragment_container, fragment);
            ft.replace(R.id.fragment_container, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
    }
    public void setAppBarText(int itemId)
    {
        Toolbar tb = findViewById(R.id.toolbar);
        switch (itemId)
        {
            case R.id.nav_about:
                tb.setTitle(R.string.menu_about);
                break;
            case R.id.nav_home:
                tb.setTitle(R.string.menu_stats);
                break;
            case R.id.nav_locations:
                tb.setTitle(R.string.menu_locations);
        }
    }
    */
}

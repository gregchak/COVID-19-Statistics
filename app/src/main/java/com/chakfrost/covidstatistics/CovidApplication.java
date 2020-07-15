package com.chakfrost.covidstatistics;

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.chakfrost.covidstatistics.models.Country;
import com.chakfrost.covidstatistics.models.GlobalStats;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.models.Province;
import com.chakfrost.covidstatistics.ui.statistics.StatisticsFragment;
import com.chakfrost.covidstatistics.workers.RefreshStatsWorker;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CovidApplication extends Application
{
    private static Application instance;
    private static List<Country> countries;
    private static List<Province> provinces;
    private static List<Location> locations;
    private static GlobalStats globalStats;
    private static BooleanPreference receiveNotifications;
    private static String[] usStates;

    private final static String CHANNEL_ID = "COVID_STATISTICS";
    private final static String UNIQUE_WORK = "STATUS_REFRESH";
    private final static String PREFERENCES_NAME = "COVID_STATISTICS_PREFERENCES";
    public final static int DAYS_TO_DISPLAY_DETAILS = 90;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;

        receiveNotifications = BooleanPreference.unassigned;

        //SetupNotifications();
        createNotificationChannel();

        createWorker();
    }

    private void createWorker()
    {
        // Instantiate Constraint for PeriodicWorkRequest
        // REQUIRED: Network connection
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Instantiate PeriodicWorkRequest
        PeriodicWorkRequest refreshStatsRequest =
                new PeriodicWorkRequest.Builder(RefreshStatsWorker.class, 1, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .build();

        // Queue unique work request
        WorkManager.getInstance(instance)
                .enqueueUniquePeriodicWork(UNIQUE_WORK, ExistingPeriodicWorkPolicy.KEEP, refreshStatsRequest);
    }

    private void createNotificationChannel()
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static void saveNotificationPreference(boolean notify)
    {
        SharedPreferences preferences = instance.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(String.valueOf(R.string.prefs_receiveNotifications), notify);
        editor.apply();
    }

    private static boolean retrieveNotificationPreference()
    {
        SharedPreferences preferences = instance.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);

        if (preferences.contains(String.valueOf(R.string.prefs_receiveNotifications)))
            return preferences.getBoolean(String.valueOf(R.string.prefs_receiveNotifications), true);
        else
            return false;

    }


    public static void sendNotification(String title, String content, Context context)
    {
        // Only send notification if application is in background AND user wants it
        if (isAppIsInBackground() && receiveNotifications == BooleanPreference.yes)
        {
            // Create an explicit intent for an Activity in your app
            Intent intent = new Intent(instance, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Setup PendingIntent
            PendingIntent pendingIntent = PendingIntent.getActivity(instance, 0, intent, 0);

            // Build notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(instance, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_coronavirus_32)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            // Notify
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(instance);
            notificationManager.notify(1, builder.build());
        }
        else
        {
            Log.d("sendNotification()", "Not in background && receiveNotifications is false");
            Log.d("sendNotification()", Boolean.toString(isAppIsInBackground()));
            Log.d("sendNotification()", receiveNotifications.toString());
        }
    }

    public static boolean isAppIsInBackground()
    {
        boolean isInBackground = true;

        ActivityManager am = (ActivityManager) instance.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(instance.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        }
        else
        {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(instance.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }

    public static String[] getUSStates()
    {
        if (null == usStates)
            usStates = CovidApplication.getContext().getResources().getStringArray(R.array.us_states);

        return usStates;
    }

    /* Getters */
    public static Context getContext() { return instance.getBaseContext(); }
    public static List<Country> getCountries() { return countries; }
    public static List<Province> getProvinces() { return provinces; }
    public static List<Location> getLocations()
    {
        if (null == locations)
            locations = CovidDataStore.retrieveLocations(instance.getApplicationContext());

        return locations;
    }
    public static GlobalStats getGlobalStats()
    {
        if (null == globalStats)
            globalStats = CovidDataStore.retrieveGlobalStats(instance.getApplicationContext());

        return globalStats;
    }
    public static boolean getReceiveNotifications()
    {
        if (null == receiveNotifications || receiveNotifications == BooleanPreference.unassigned)
        {
            boolean notify = retrieveNotificationPreference();
            if (notify)
                receiveNotifications = BooleanPreference.yes;
            else
                receiveNotifications = BooleanPreference.no;
        }
        return receiveNotifications == BooleanPreference.no ? false : true;
    }

    /* Setters */
    public static void setCountries(List<Country> val) { countries = val; }
    public static void setProvinces(List<Province> val) { provinces = val; }
    public static void setLocations(List<Location> val)
    {
        locations = val;
        new WriteLocationsToFileSystem().execute(val);
        //CovidDataStore.saveLocations(instance.getApplicationContext(), val);
    }
    public static void setGlobalStats(GlobalStats val)
    {
        globalStats = val;
        new WriteGlobalStatsToFileSystem().execute(val);
        //CovidDataStore.saveGlobalStats(instance.getApplicationContext(), val);
    }
    public static void setReceiveNotifications(boolean val)
    {
        if (val)
            receiveNotifications = BooleanPreference.yes;
        else
            receiveNotifications = BooleanPreference.no;

        saveNotificationPreference(val);
    }

    public enum BooleanPreference
    {
        unassigned,
        yes,
        no
    }

    /**
     * Class for handling the writing of Location stats to the files system
     * asynchronously, off the Main UI thread
     */
    public static class WriteLocationsToFileSystem extends AsyncTask<List<Location>, Integer, String>
    {

        @Override
        protected String doInBackground(List<Location>... lists)
        {
            List<Location> locations = lists[0];
            CovidDataStore.saveLocations(instance.getApplicationContext(), locations);

            return null;
        }
    }

    /**
     * Class for handling the writing of Global stats to the files system
     * asynchronously, off the Main UI thread
     */
    public static class WriteGlobalStatsToFileSystem extends AsyncTask<GlobalStats, Integer, String>
    {

        @Override
        protected String doInBackground(GlobalStats... globalStats)
        {
            CovidDataStore.saveGlobalStats(instance.getApplicationContext(), globalStats[0]);

            return null;
        }
    }
}

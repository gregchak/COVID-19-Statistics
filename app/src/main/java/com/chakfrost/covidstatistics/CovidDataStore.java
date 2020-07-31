package com.chakfrost.covidstatistics;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.chakfrost.covidstatistics.models.GlobalStats;
import com.chakfrost.covidstatistics.models.Location;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.jr.ob.JSON;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

//import org.json.simple.parser.JSONParser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CovidDataStore
{
    private final static String LOCATION_FILENAME = "locations";
    private final static String SUMMARY_FILENAME = "summary";
    private static Context context;

    public CovidDataStore() { }

    /**
     * Writes JSON representation of List of Locations to disk
     *
     * @param mainContext   Application context
     * @param locations     List of Location
     */
    public static void saveLocations(@NotNull Context mainContext, List<Location> locations)
    {
        // Set Context
        context = mainContext;

        Gson gson = new Gson();
        String json = gson.toJson(locations);
        Log.d("CovidDataStore.SaveLocations()", "json = " + json);

        // Write contents
        WriteToFileSystemParams param = new WriteToFileSystemParams(json, StatType.Locations);
        new WriteToFileSystem().execute(param);
        //writeToDisk(context, json, StatType.Locations);
    }

    /**
     * Reads from Locations file on disk to return List of Locations
     *
     * @param context   Application Context
     * @return          List of Location
     */
    public static List<Location> retrieveLocations(@NotNull Context context)
    {
        // FOR DEBUGGING:
        //SaveLocations(context, new ArrayList<>());

        List<Location> locations = new ArrayList<>();

        try
        {
            // Get contents from disk
            String fileContents = readFileFromDisk(context, StatType.Locations);

            if (null != fileContents)
            {
                // Convert JSON to List<Location>
                Gson gson = new Gson();
                Type type = new TypeToken<CopyOnWriteArrayList<Location>>(){}.getType();
                locations = gson.fromJson(fileContents, type);
            }

/*            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            locations = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, Location.class));
            locations = mapper.readValue(json, new TypeReference<List<Location>>(){});

            Location[] loc = mapper.readValue(json, Location[].class);
            locations = objectMapper.readValue(sjson, ;


            // Convert JSON to List<Location> - Jackson Jr.
            // .with(JSON.Feature.WRITE_DATES_AS_TIMESTAMP)
            locations = JSON.std
                    .listOfFrom(Location.class, json);

            JSONParser parser = new JSONParser();

            Object o = parser.parse(json);
            locations = (List<Location>)o;

            locations = (List<Location>)parser.parse(json);
            locations = JSON.std.beanFrom(List.class, sb.toString());

            JSON.std.with(JSON.Feature.WRITE_DATES_AS_TIMESTAMP);*/
        }
        catch(Exception e)
        {
            Log.e("CovidDataStore.RetrieveLocations()",
                    CovidUtils.formatError(e.getMessage(), e.getStackTrace().toString()));
        }
        finally
        {
            return locations;
        }
    }

    /**
     * Writes JSON representation of GlobalStats to disk
     * @param mainContext   Application Context
     * @param stats     GlobalStats to be written to disk
     */
    public static void saveGlobalStats(@NotNull Context mainContext, GlobalStats stats)
    {
        // Set Context
        context = mainContext;

        // Convert a Map into JSON string.
        Gson gson = new Gson();
        String json = gson.toJson(stats);
        Log.d("CovidDataStore.SaveGlobalStats()", "json = " + json);

        // Write contents
        WriteToFileSystemParams param = new WriteToFileSystemParams(json, StatType.Global);
        new WriteToFileSystem().execute(param);
        //writeToDisk(mainContext, json, StatType.Global);
    }

    /**
     * Reds GlobalStats file on disk and returns GlobalStats object
     * @param context   Application Context
     * @return          GlobalStats object
     */
    @Nullable
    public static GlobalStats retrieveGlobalStats(@NotNull Context context)
    {
        GlobalStats stats = null;

        try
        {
            // Get contents from disk
            String fileContents = readFileFromDisk(context, StatType.Global);

            if (null != fileContents)
            {
                // Convert JSON to GlobalStats
                Gson gson = new Gson();
                Type type = new TypeToken<GlobalStats>(){}.getType();
                stats = gson.fromJson(fileContents, type);
            }
        }
        catch(Exception e)
        {
            Log.e("CovidDataStore.RetrieveGlobalStats()",
                    CovidUtils.formatError(e.getMessage(), e.getStackTrace().toString()));
        }
        finally
        {
            return stats;
        }
    }

    private static void writeToDisk(@NotNull Context context, String fileContents, StatType statType )
    {
        // Verify file/directory exists
        File file = context.getFilesDir();
        if (!file.exists())
        {
            file.mkdir();
        }

        try
        {
            // Write to disk
            File locationFile = new File(file, (statType == StatType.Global ? SUMMARY_FILENAME : LOCATION_FILENAME));
            FileWriter writer = new FileWriter(locationFile);
            writer.write(fileContents);
            writer.flush();
            writer.close();
        }
        catch (Exception e)
        {
            Log.e("CovidDataStore.writeToDisk()",
                    CovidUtils.formatError(e.getMessage(), e.getStackTrace().toString()));
        }
    }

    @Nullable
    private static String readFileFromDisk(@NotNull Context context, StatType statType)
    {
        String fileContents = null;
        try
        {
            File file = new File(context.getFilesDir(),(statType == StatType.Global ? SUMMARY_FILENAME : LOCATION_FILENAME));
            if (!file.exists())
                return null;

            // Read JSON file from internal storage
            FileInputStream fis = context.openFileInput((statType == StatType.Global ? SUMMARY_FILENAME : LOCATION_FILENAME));
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            String line;

            // Read each line to build String
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            // Close out stream and readers
            bufferedReader.close();
            isr.close();
            fis.close();

            // Populate return variable
            fileContents = sb.toString();

            Log.d("CovidDataStore.readFileFromDisk()", "json = " + fileContents);
        }
        catch(Exception e)
        {
            Log.e("CovidDataStore.readFileFromDisk()",
                    CovidUtils.formatError(e.getMessage(), e.getStackTrace().toString()));
        }
        finally
        {
            return fileContents;
        }
    }

    private enum StatType
    {
        Global,
        Locations
    }

    private static class WriteToFileSystemParams {
        String fileContents;
        StatType statType;

        WriteToFileSystemParams(String fileContents, StatType statType) {
            this.fileContents = fileContents;
            this.statType = statType;
        }
    }

    /**
     * Class for handling the writing of Location stats to the files system
     * asynchronously, off the Main UI thread
     */
    public static class WriteToFileSystem extends AsyncTask<WriteToFileSystemParams, Integer, String>
    {

        @Override
        protected String doInBackground(WriteToFileSystemParams... params)
        {
            WriteToFileSystemParams param = params[0];

            // Verify file/directory exists
            File file = context.getFilesDir();
            if (!file.exists())
            {
                file.mkdir();
            }

            try
            {
                // Write to disk
                File locationFile = new File(file, (param.statType == StatType.Global ? SUMMARY_FILENAME : LOCATION_FILENAME));
                FileWriter writer = new FileWriter(locationFile);
                writer.write(param.fileContents);
                writer.flush();
                writer.close();
            }
            catch (Exception e)
            {
                Log.e("CovidDataStore.WriteToFileSystem.doInBackground()",
                        CovidUtils.formatError(e.getMessage(), e.getStackTrace().toString()));
            }

            return null;
        }
    }
}

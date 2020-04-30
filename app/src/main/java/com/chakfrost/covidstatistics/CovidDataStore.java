package com.chakfrost.covidstatistics;

import android.content.Context;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CovidDataStore
{
    private final static String LOCATION_FILENAME = "locations";
    private final static String SUMMARY_FILENAME = "summary";

    public CovidDataStore() { }

    public static void saveLocations(Context context, List<Location> locations)
    {
        // Convert a Map into JSON string.
        Gson gson = new Gson();
        String json = gson.toJson(locations);
        Log.d("CovidDataStore.SaveLocations()", "json = " + json);
        System.out.println("json = " + json);

        File file = context.getFilesDir();
        if(!file.exists())
        {
            file.mkdir();
        }

        try
        {
            File locationFile = new File(file, LOCATION_FILENAME);
            FileWriter writer = new FileWriter(locationFile);
            writer.write(json);
            writer.flush();
            writer.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
    }

    public static List<Location> retrieveLocations(Context context)
    {
        //SaveLocations(context, new ArrayList<>());

        List<Location> locations = new ArrayList<>();
        try
        {
            // Read JSON file from internal storage
            FileInputStream fis = context.openFileInput(LOCATION_FILENAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            String json = sb.toString();

            Log.d("CovidDataStore.RetrieveLocations()", "json = " + json);

            //ObjectMapper mapper = new ObjectMapper();
            //mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            //locations = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, Location.class));
            //locations = mapper.readValue(json, new TypeReference<List<Location>>(){});

            //Location[] loc = mapper.readValue(json, Location[].class);
            //locations = objectMapper.readValue(sjson, ;


            // Convert JSON to List<Location> - Jackson Jr.
            // .with(JSON.Feature.WRITE_DATES_AS_TIMESTAMP)
//            locations = JSON.std
//                    .listOfFrom(Location.class, json);

//            JSONParser parser = new JSONParser();
//
//            Object o = parser.parse(json);
//            locations = (List<Location>)o;
//
//            locations = (List<Location>)parser.parse(json);




            //locations = JSON.std.beanFrom(List.class, sb.toString());

            //JSON.std.with(JSON.Feature.WRITE_DATES_AS_TIMESTAMP)

            Gson gson = new Gson();
            Type type = new TypeToken<List<Location>>(){}.getType();
            locations = gson.fromJson(json, type);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.e("CovidDataStore.RetrieveLocations()", e.toString());
        }
        finally
        {
            return locations;
        }
    }

    public static void saveGlobalStats(Context context, GlobalStats stats)
    {
        // Convert a Map into JSON string.
        Gson gson = new Gson();
        String json = gson.toJson(stats);
        Log.d("CovidDataStore.SaveGlobalStats()", "json = " + json);
        System.out.println("json = " + json);

        File file = context.getFilesDir();
        if(!file.exists())
        {
            file.mkdir();
        }

        try
        {
            File locationFile = new File(file, SUMMARY_FILENAME);
            FileWriter writer = new FileWriter(locationFile);
            writer.write(json);
            writer.flush();
            writer.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
    }

    public static GlobalStats retrieveGlobalStats(Context context)
    {
        GlobalStats stats = null;
        try
        {
            File file = new File(context.getFilesDir(),SUMMARY_FILENAME);
            if (!file.exists())
                return stats;

            // Read JSON file from internal storage
            FileInputStream fis = context.openFileInput(SUMMARY_FILENAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            Log.d("CovidDataStore.RetrieveGlobalStats()", "json = " + sb.toString());

            // Convert JSON to List<Location>
            Gson gson = new Gson();
            Type type = new TypeToken<GlobalStats>(){}.getType();
            stats = gson.fromJson(sb.toString(), type);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.e("CovidDataStore.RetrieveGlobalStats()", e.toString());
        }
        finally
        {
            return stats;
        }
    }
}

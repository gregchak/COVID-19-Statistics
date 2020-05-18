package com.chakfrost.covidstatistics.models;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

public class LocationStats
{
    private String name;
    private List<StatDatePair> values;
    public LocationStats()
    {
        this.values = new ArrayList<>();
    }
    public LocationStats(String name)
    {
        this.name = name;
        this.values = new ArrayList<>();
    }

    public void addValue(Date dateOfValue, double value)
    {
        StatDatePair p = new StatDatePair(dateOfValue, value);
        addValue(p);
    }
    public void addValue(StatDatePair p)
    {
        int index = values.indexOf(p);

        if (index < 0)
            values.add(p);
    }

    /** Getters **/
    public String getName()
    {
        return name;
    }
    public List<StatDatePair> getValues()
    {
        return values;
    }

    /** Setters **/
    public void setName(String name)
    {
        this.name = name;
    }
    public void setValues(List<StatDatePair> values)
    {
        this.values = values;
    }
}

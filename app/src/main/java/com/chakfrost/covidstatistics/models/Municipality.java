package com.chakfrost.covidstatistics.models;

public class Municipality
{
    private String Name;
    private String Fips;


    public String getName()
    {
        return Name;
    }

    public void setName(String name)
    {
        Name = name;
    }

    public String getFips()
    {
        return Fips;
    }

    public void setFips(String fips)
    {
        Fips = fips;
    }

    public Municipality(String name, String fips)
    {
        Name = name;
        Fips = fips;
    }
}

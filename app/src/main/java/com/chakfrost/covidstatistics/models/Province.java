package com.chakfrost.covidstatistics.models;

public class Province
{
    private Country Country;
    private String Name;

    public Province(String _name)
    {
        Name = _name;
    }

    public Province (String _name, Country _country)
    {
        Name = _name;
        Country = _country;
    }

    @Override
    public String toString() {
        return Name;
    }


    /* Getters */
    public Country getCountry() { return Country; }
    public String getName() { return Name; }

    /* Setters */
    public void setCountry(Country val) { Country = val; }
    public void setName(String val) { Name = val; }

}

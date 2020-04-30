package com.chakfrost.covidstatistics.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Country extends CovidStats
{
    @Expose
    @SerializedName("Country")
    private String Name;

    @Expose
    @SerializedName("Slug")
    private String Slug;

    @Expose
    @SerializedName("ISO2")
    private String ISO2;

    @Expose
    @SerializedName("CountryCode")
    private String CountryCode;


    public Country(String _name, String _iso2)
    {
        super();
        Name = _name;
        ISO2 = _iso2;
        CountryCode = _iso2;
    }

    public Country(String _name, String _slug, String _iso2)
    {
        super();
        Name = _name;
        Slug = _slug;
        ISO2 = _iso2;
        CountryCode = _iso2;
    }

    public Country(String _name, String _countryCode, String _slug, int _newConfirmed, int _totalConfirmed,
                   int _newDeaths, int _totalDeaths, int _newRecovered, int _totalRecovered, Date _statusDate)
    {
        super(_newConfirmed, _totalConfirmed, _newDeaths, _totalDeaths, _newRecovered, _totalRecovered, _statusDate);
        Name = _name;
        CountryCode = _countryCode;
        ISO2 = _countryCode;
        Slug = _slug;
    }


    @Override
    public String toString() {
        return Name;
    }


    /* Getters */
    public String getName() { return Name; }
    public String getSlug() { return Slug; }
    public String getISO2() { return ISO2; }
    public String getCountryCode() { return CountryCode; }

    /* Setters */
    public void setName(String val) { Name = val; }
    public void setSlug(String val) { Slug = val ; }
    public void setISO2(String val)
    {
        ISO2 = val;
        CountryCode = val;
    }
    public void setCountryCode(String val)
    {
        CountryCode = val;
        ISO2 = val;
    }

}

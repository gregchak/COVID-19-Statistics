package com.chakfrost.covidstatistics.models;

import android.text.TextUtils;

//import com.chakfrost.covidnotifier.deserializers.DateHandler;
//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.annotations.SerializedName;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Location implements Comparable<Location>, Serializable
{
    @SerializedName("country")
    private String country;
    private String province;
    private String municipality;
    private String iso;
    private String usStateAbbreviation;
    @SerializedName("Region")
    private String region;
    private List<CovidStats> statistics;
    private Date lastUpdated;

    public Location() {}

    public Location (String _country)
    {
        country = _country;
        statistics = new ArrayList<>();
    }

    public Location (String _country, String _province)
    {
        country = _country;
        province = _province;
        statistics = new ArrayList<>();
    }

    public Location (String _country, String _province, String _municipality)
    {
        country = _country;
        province = _province;
        municipality = _municipality;
        statistics = new ArrayList<>();
    }

    public Location (String _country, String _province, List<CovidStats> _statistics)
    {
        country = _country;
        province = _province;
        statistics = _statistics;
    }

    public Location (String _country, String _province, String _municipality, List<CovidStats> _statistics)
    {
        country = _country;
        province = _province;
        municipality = _municipality;
        statistics = _statistics;
    }

    /**
     * Determines if the Location is a Country
     * @return  True if Location is a Country, false if not
     */
    public boolean isCountry()
    {
        if (TextUtils.isEmpty(province) && TextUtils.isEmpty(municipality))
            return true;
        else
            return false;
    }

    /**
     * Determines if the Location is a Province
     * @return  True if Location is a Province, false if not
     */
    public boolean isProvince()
    {
        if (!TextUtils.isEmpty(province) && TextUtils.isEmpty(municipality))
            return true;
        else
            return false;
    }

    /**
     * Determines if the Location is a Municipality
     * @return  True if Location is a Municipality, false if not
     */
    public boolean isMunicipality()
    {
        if (!TextUtils.isEmpty(province) && !TextUtils.isEmpty(municipality))
            return true;
        else
            return false;
    }

    @Override
    public int compareTo(Location o)
    {
        if (country.equals(o.country))
        {
            if (TextUtils.isEmpty(province) || TextUtils.isEmpty(o.province))
            {
                if (TextUtils.isEmpty(province) && TextUtils.isEmpty(o.province))
                    return 0;
                else if (!TextUtils.isEmpty(province))
                    return province.compareTo("");
                else
                    return -(o.province.compareTo(""));
            }
            else
            {
                if (province.equals(o.province))
                {
                    if (TextUtils.isEmpty(municipality) || TextUtils.isEmpty(o.municipality))
                    {
                        if (TextUtils.isEmpty(municipality) && TextUtils.isEmpty(o.municipality))
                            return 0;
                        else if (!TextUtils.isEmpty(municipality))
                            return municipality.compareTo("");
                        else
                            return -(o.municipality.compareTo(""));
                    }
                    else
                        return municipality.compareTo(o.municipality);
                }
                else
                    return province.compareTo(o.province);
            }
        }
        else
            return country.compareTo(o.country);
    }

    /* Getters */
    public String getCountry() { return country; }
    public String getProvince()
    {
        if (TextUtils.isEmpty(province))
            return "";
        else
            return province;
    }
    public String getMunicipality()
    {
        if (TextUtils.isEmpty(municipality))
            return "";
        else
            return municipality;
    }
    public String getIso() { return iso; }
    public String getRegion() { return region; }
    public String getUsStateAbbreviation() { return usStateAbbreviation; }
    public List<CovidStats> getStatistics() { return statistics; }
    public Date getLastUpdated() { return lastUpdated; }

    /* Setters */
    public void setCountry(String val) { country = val; }
    public void setProvince(String val) { province = val; }
    public void setMunicipality(String val) { municipality = val; }
    public void setIso(String val) { iso = val; }
    public void setRegion(String val) { region = val; }
    public void setUsStateAbbreviation(String val) { usStateAbbreviation = val; }
    public void setStatistics(List<CovidStats> val) { statistics = val; }
    public void setLastUpdated(Date val) { lastUpdated = val; }


}

package com.chakfrost.covidstatistics.models;

import android.text.TextUtils;

//import com.chakfrost.covidnotifier.deserializers.DateHandler;
//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Location implements Comparable<Location>, Serializable, Cloneable
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
    private String fips;

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

    public Location (String _country, String _province, String _municipality, String _fips)
    {
        country = _country;
        province = _province;
        municipality = _municipality;
        fips = _fips;
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

    public Location (String _country, String _province, String _municipality, List<CovidStats> _statistics, String _fips)
    {
        country = _country;
        province = _province;
        municipality = _municipality;
        fips = _fips;
        statistics = _statistics;
    }

    /**
     * Determines if the Location is a Country
     * @return  True if Location is a Country, false if not
     */
    public boolean isCountry()
    {
        return TextUtils.isEmpty(province) && TextUtils.isEmpty(municipality);
    }

    /**
     * Determines if the Location is a Province
     * @return  True if Location is a Province, false if not
     */
    public boolean isProvince()
    {
        return !TextUtils.isEmpty(province) && TextUtils.isEmpty(municipality);
    }

    /**
     * Determines if the Location is a Municipality
     * @return  True if Location is a Municipality, false if not
     */
    public boolean isMunicipality()
    {
        return !TextUtils.isEmpty(province) && !TextUtils.isEmpty(municipality);
    }

    /**
     * Adds a stat to this location.  Will replace stat if its StatusDate is the same
     * as an existing statistic, otherwise it will add CovidStat to statistics
     * @param stat  CovidStat to add to Location Statistics
     */
    public void AddStatistic(CovidStats stat)
    {
        // Get next date to check
        SimpleDateFormat ymdFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        // Check if we already have stats for stat StatusDate
        CovidStats found = statistics.stream()
                .filter(s -> ymdFormatter.format(s.getStatusDate().getTime()).equals(ymdFormatter.format(stat.getStatusDate().getTime())))
                .findFirst()
                .orElse(null);

        // If there's an existing stat, remove it before adding parameter stat
        if (null != found)
            statistics.remove(found);

        statistics.add(stat);
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

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        return super.clone();
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
    public String getFips() { return fips; }

    /* Setters */
    public void setCountry(String val) { country = val; }
    public void setProvince(String val) { province = val; }
    public void setMunicipality(String val) { municipality = val; }
    public void setIso(String val) { iso = val; }
    public void setRegion(String val) { region = val; }
    public void setUsStateAbbreviation(String val) { usStateAbbreviation = val; }
    public void setStatistics(List<CovidStats> val) { statistics = val; }
    public void setLastUpdated(Date val) { lastUpdated = val; }
    public void setFips(String val) { fips = val; }


}

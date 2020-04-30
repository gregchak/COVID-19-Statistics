package com.chakfrost.covidstatistics.models;

//import com.chakfrost.covidnotifier.deserializers.DateHandler;
//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Date;

public class GlobalStats
{
    private int NewConfirmed;
    private int TotalConfirmed;
    private int NewDeaths;
    private int TotalDeaths;
    private int NewRecovered;
    private int TotalRecovered;
    //@JsonDeserialize(using = DateHandler.class)
    private Date StatusDate;


    /* Getters */
    public int getNewConfirmed() { return NewConfirmed; }
    public int getTotalConfirmed() { return TotalConfirmed; }
    public int getNewDeaths() { return NewDeaths; }
    public int getTotalDeaths() { return TotalDeaths; }
    public int getNewRecovered() { return NewRecovered; }
    public int getTotalRecovered() { return TotalRecovered; }
    public Date getStatusDate() { return StatusDate; }


    /* Setters */
    public void setNewConfirmed(int val) { NewConfirmed = val; }
    public void setTotalConfirmed(int val) { TotalConfirmed = val; }
    public void setNewDeaths(int val) { NewDeaths = val; }
    public void setTotalDeaths(int val) { TotalDeaths = val; }
    public void setNewRecovered(int val) { NewRecovered = val; }
    public void setTotalRecovered(int val) { TotalRecovered = val; }
    public void setStatusDate(Date val) { StatusDate = val; }
}

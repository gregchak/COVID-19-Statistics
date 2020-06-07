package com.chakfrost.covidstatistics.models;


import java.util.Date;

public class GlobalStats
{
    private int NewConfirmed;
    private int TotalConfirmed;
    private int NewDeaths;
    private int TotalDeaths;
    private int NewRecovered;
    private int TotalRecovered;
    private int NewActive;
    private int TotalActive;
    private Date StatusDate;
    private Date LastUpdate;
    private Date LastChecked;
    private double fatalityRate;
    private double previousFatalityRate;


    /* Getters */
    public int getNewConfirmed() { return NewConfirmed; }
    public int getTotalConfirmed() { return TotalConfirmed; }
    public int getNewDeaths() { return NewDeaths; }
    public int getTotalDeaths() { return TotalDeaths; }
    public int getNewRecovered() { return NewRecovered; }
    public int getTotalRecovered() { return TotalRecovered; }
    public Date getStatusDate() { return StatusDate; }
    public int getNewActive() { return NewActive; }
    public int getTotalActive() { return TotalActive; }
    public Date getLastUpdate() { return LastUpdate; }
    public double getFatalityRate() { return fatalityRate; }
    public double getPreviousFatalityRate() { return previousFatalityRate; }
    public Date getLastChecked() { return LastChecked; }

    /* Setters */
    public void setNewConfirmed(int val) { NewConfirmed = val; }
    public void setTotalConfirmed(int val) { TotalConfirmed = val; }
    public void setNewDeaths(int val) { NewDeaths = val; }
    public void setTotalDeaths(int val) { TotalDeaths = val; }
    public void setNewRecovered(int val) { NewRecovered = val; }
    public void setTotalRecovered(int val) { TotalRecovered = val; }
    public void setStatusDate(Date val) { StatusDate = val; }
    public void setNewActive(int newActive) { NewActive = newActive; }
    public void setTotalActive(int totalActive) { TotalActive = totalActive; }
    public void setLastUpdate(Date lastUpdate) { LastUpdate = lastUpdate; }
    public void setFatalityRate(double fatalityRate) { this.fatalityRate = fatalityRate; }
    public void setPreviousFatalityRate(double fatalityRate) { this.previousFatalityRate = fatalityRate; }
    public void setLastChecked(Date val) { this.LastChecked = val; }
}

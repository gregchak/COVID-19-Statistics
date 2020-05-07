package com.chakfrost.covidstatistics.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

//@JsonIgnoreProperties
public class CovidStats implements Comparable<CovidStats>
{
    @Expose
    @SerializedName("NewConfirmed")
    private int NewConfirmed;
    @Expose
    @SerializedName("TotalConfirmed")
    private int TotalConfirmed;
    @Expose
    @SerializedName("NewDeaths")
    private int NewDeaths;
    @Expose
    @SerializedName("TotalDeaths")
    private int TotalDeaths;
    @Expose
    @SerializedName("NewRecovered")
    private int NewRecovered;
    @Expose
    @SerializedName("TotalRecovered")
    private int TotalRecovered;

    @Expose
    @SerializedName("diffConfirmed")
    private int DiffConfirmed;
    @Expose
    @SerializedName("diffDeaths")
    private int DiffDeaths;
    @Expose
    @SerializedName("diffRecovered")
    private int DiffRecovered;
    @Expose
    @SerializedName("totalActive")
    private int TotalActive;
    @Expose
    @SerializedName("newActive")
    private int NewActive;
    @Expose
    @SerializedName("diffActive")
    private int DiffActive;
    @Expose
    @SerializedName("fatalityRate")
    private double FatalityRate;

    @Expose
    @SerializedName("Date")
    //@JsonDeserialize(using = DateHandler.class)
    private Date StatusDate;

    @Expose
    @SerializedName("last_update")
    //@JsonDeserialize(using = DateHandler.class)
    private Date LastUpdate;

    public CovidStats()
    {
        NewConfirmed = 0;
        TotalConfirmed = 0;
        DiffConfirmed = 0;
        NewDeaths = 0;
        TotalDeaths = 0;
        DiffDeaths = 0;
        NewRecovered = 0;
        TotalRecovered = 0;
        DiffRecovered = 0;
        TotalActive = 0;
        NewActive = 0;
        DiffActive = 0;
        FatalityRate = 0.0;
    }

    public CovidStats(int _newConfirmed, int _totalConfirmed, int _newDeaths, int _totalDeaths,
                      int _newRecovered, int _totalRecovered, Date _statusDate)
    {
        NewConfirmed = _newConfirmed;
        DiffConfirmed = _newConfirmed;
        TotalConfirmed = _totalConfirmed;
        NewDeaths = _newDeaths;
        DiffDeaths = _newDeaths;
        TotalDeaths = _totalDeaths;
        NewRecovered = _newRecovered;
        DiffRecovered = _newRecovered;
        TotalRecovered = _totalRecovered;
        if (null != _statusDate)
            StatusDate = _statusDate;
    }

    @Override
    public int compareTo(CovidStats o)
    {
        if (StatusDate.getTime() > o.getStatusDate().getTime())
            return -1;
        else if (o.getStatusDate().getTime() > StatusDate.getTime())
            return 1;
        else
            return 0;
    }

    /* Getters */
    public int getNewConfirmed() { return NewConfirmed; }
    public int getTotalConfirmed() { return TotalConfirmed; }
    public int getNewDeaths() { return NewDeaths; }
    public int getTotalDeaths() { return TotalDeaths; }
    public int getNewRecovered() { return NewRecovered; }
    public int getTotalRecovered() { return TotalRecovered; }
    public int getDiffConfirmed() { return DiffConfirmed; }
    public int getDiffDeaths() { return DiffDeaths; }
    public int getDiffRecovered() { return DiffRecovered; }
    public int getTotalactive() { return TotalActive; }
    public int getNewActive() { return NewActive; }
    public int getDiffActive() { return DiffActive; }
    public double getFatalityRate() { return FatalityRate; }

    public Date getStatusDate() { return StatusDate; }
    public Date getLastUpdate() { return LastUpdate; }

    /* Setters */
    public void settNewConfirmed(int val) { NewConfirmed = val; }
    public void setTotalConfirmed(int val) { TotalConfirmed = val; }
    public void setNewDeaths(int val) { NewDeaths = val; }
    public void setTotalDeaths(int val) { TotalDeaths = val; }
    public void setNewRecovered(int val) { NewRecovered = val; }
    public void setTotalRecovered(int val) { TotalRecovered = val; }
    public void setDiffConfirmed(int val) { DiffConfirmed = val; }
    public void setDiffDeaths(int val) { DiffDeaths = val; }
    public void setDiffRecovered(int val) { DiffRecovered = val; }
    public void setTotalactive(int val) { TotalActive = val; }
    public void setNewActive(int val) { NewActive = val; }
    public void setDiffActive(int val) { DiffActive = val; }
    public void setFatalityRate(double val) { FatalityRate = val; }

    public void setStatusDate(Date val) { StatusDate = val; }
    public void setLastUpdate(Date val) { LastUpdate = val; }


}

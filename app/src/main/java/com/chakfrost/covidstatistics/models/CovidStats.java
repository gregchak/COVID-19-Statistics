package com.chakfrost.covidstatistics.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

//@JsonIgnoreProperties
public class CovidStats implements Comparable<CovidStats>, Serializable
{
    @Expose
    @SerializedName("NewConfirmed")
    private int NewConfirmed;
    @Expose
    @SerializedName("TotalConfirmed")
    private int TotalConfirmed;
    @Expose
    @SerializedName("AverageConfirmed")
    private int AverageConfirmed;
    @Expose
    @SerializedName("NewDeaths")
    private int NewDeaths;
    @Expose
    @SerializedName("TotalDeaths")
    private int TotalDeaths;
    @Expose
    @SerializedName("AverageDeaths")
    private int AverageDeaths;
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
    @SerializedName("diffAverageConfirmed")
    private int DiffAverageConfirmed;
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

    @Expose(serialize = false)
    private Integer hospitalizationsTotal;
    @Expose(serialize = false)
    private Integer hospitalizationsDiff;
    @Expose(serialize = false)
    private Integer hospitalizationsCurrent;
    @Expose(serialize = false)
    private Integer hospitalizationsCovidTotal;
    @Expose(serialize = false)
    private Integer hospitalizationsCovidDiff;
    @Expose(serialize = false)
    private Integer hospitalizationsCovidCurrent;
    @Expose(serialize = false)
    private double hospitalizationsPercentCovid;
    @Expose(serialize = false)
    private double hospitalizationsPercentFull;
    @Expose(serialize = false)
    private Integer hospitalizationCapacity;

    @Expose(serialize = false)
    private Integer ICUTotal;
    @Expose(serialize = false)
    private Integer ICUDiff;
    @Expose(serialize = false)
    private Integer ICUCurrent;
    @Expose(serialize = false)
    private Integer ICUCovidTotal;
    @Expose(serialize = false)
    private Integer ICUCovidDiff;
    @Expose(serialize = false)
    private Integer ICUCovidCurrent;
    @Expose(serialize = false)
    private double ICUPercentCovid;
    @Expose(serialize = false)
    private double ICUPercentFull;
    @Expose(serialize = false)
    private Integer ICUCapacity;

    @Expose(serialize = false)
    private double PositivityRate;

    // Cases per 100k
    @Expose(serialize = false)
    private double CaseDensity;

    // 1 of 4 text values
    @Expose(serialize = false)
    private String CdcTransmissionLevel;
    @Expose(serialize = false)
    private double InfectionRate;
    @Expose(serialize = false)
    private double InfectionRateDiff;

    @Expose(serialize = false)
    private double TestPositivityPercentage;
    @Expose(serialize = false)
    private double TestPositivityPercentageDiff;
    @Expose(serialize = false)
    private double VaccinationsInitiatedPercentage;
    @Expose(serialize = false)
    private double VaccinationsCompletedPercentage;
    @Expose(serialize = false)
    private Integer VaccinationsInitiated;
    @Expose(serialize = false)
    private Integer VaccinationsCompleted;

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
    public int getTotalActive() { return TotalActive; }
    public int getNewActive() { return NewActive; }
    public int getDiffActive() { return DiffActive; }
    public double getFatalityRate() { return FatalityRate; }

    public Date getStatusDate() { return StatusDate; }
    public Date getLastUpdate() { return LastUpdate; }
    public double getPositivityRate()
    {
        return PositivityRate;
    }
    public double getCaseDensity() { return CaseDensity; }
    public String getCdcTransmissionLevel() { return CdcTransmissionLevel; }
    public double getInfectionRate() { return InfectionRate; }
    public double getTestPositivityPercentage() { return TestPositivityPercentage; }
    public double getVaccinationsInitiatedPercentage() { return VaccinationsInitiatedPercentage; }
    public double getVaccinationsCompletedPercentage() { return VaccinationsCompletedPercentage; }
    public Integer getHospitalizationsTotal()
    {
        return hospitalizationsTotal;
    }
    public Integer getHospitalizationsDiff()
    {
        return hospitalizationsDiff;
    }
    public Integer getHospitalizationsCurrent()
    {
        return hospitalizationsCurrent;
    }
    public Integer getICUTotal()
    {
        return ICUTotal;
    }
    public Integer getICUCurrent()
    {
        return ICUCurrent;
    }
    public Integer getHospitalizationsCovidTotal() { return hospitalizationsCovidTotal; }
    public Integer getHospitalizationsCovidDiff()  { return hospitalizationsCovidDiff; }
    public Integer getHospitalizationsCovidCurrent() { return hospitalizationsCovidCurrent;  }
    public Integer getICUDiff() { return ICUDiff; }
    public Integer getICUCovidTotal() { return ICUCovidTotal; }
    public Integer getICUCovidDiff() { return ICUCovidDiff; }
    public Integer getICUCovidCurrent() { return ICUCovidCurrent; }
    public double getHospitalizationsPercentCovid(){return hospitalizationsPercentCovid;}
    public double getICUPercentCovid() { return ICUPercentCovid; }
    public Integer getHospitalizationCapacity() {  return hospitalizationCapacity; }
    public Integer getICUCapacity() { return ICUCapacity; }


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
    public void setTotalActive(int val) { TotalActive = val; }
    public void setNewActive(int val) { NewActive = val; }
    public void setDiffActive(int val) { DiffActive = val; }
    public void setFatalityRate(double val) { FatalityRate = val; }

    public void setStatusDate(Date val) { StatusDate = val; }
    public void setLastUpdate(Date val) { LastUpdate = val; }

    public void setVaccinationsCompletedPercentage(double vaccinationsCompletedPercentage) { VaccinationsCompletedPercentage = vaccinationsCompletedPercentage; }
    public void setVaccinationsInitiatedPercentage(double vaccinationsInitiatedPercentage) { VaccinationsInitiatedPercentage = vaccinationsInitiatedPercentage; }
    public void setTestPositivityPercentage(double testPositivityPercentage) { TestPositivityPercentage = testPositivityPercentage; }
    public void setHospitalizationsTotal(Integer hospitalizationsTotal) { this.hospitalizationsTotal = hospitalizationsTotal; }
    public void setHospitalizationsDiff(Integer hospitalizationsDiff) { this.hospitalizationsDiff = hospitalizationsDiff; }
    public void setHospitalizationsCurrent(Integer hospitalizationsCurrent)  { this.hospitalizationsCurrent = hospitalizationsCurrent; }
    public void setICUTotal(Integer ICUTotal)
    {
        this.ICUTotal = ICUTotal;
    }
    public void setICUCurrent(Integer ICUCurrent)
    {
        this.ICUCurrent = ICUCurrent;
    }
    public void setPositivityRate(double positivityRate)
    {
        PositivityRate = positivityRate;
    }
    public void setCaseDensity(double caseDensity) { CaseDensity = caseDensity; }
    public void setCdcTransmissionLevel(String cdcTransmissionLevel) { CdcTransmissionLevel = cdcTransmissionLevel;  }
    public void setInfectionRate(double infectionRate) { InfectionRate = infectionRate;}
    public void setHospitalizationsCovidTotal(Integer hospitalizationsCovidTotal) { this.hospitalizationsCovidTotal = hospitalizationsCovidTotal; }
    public void setHospitalizationsCovidDiff(Integer hospitalizationsCovidDiff) { this.hospitalizationsCovidDiff = hospitalizationsCovidDiff; }
    public void setHospitalizationsCovidCurrent(Integer hospitalizationsCovidCurrent) { this.hospitalizationsCovidCurrent = hospitalizationsCovidCurrent; }
    public void setICUDiff(Integer ICUDiff) { this.ICUDiff = ICUDiff; }
    public void setICUCovidTotal(Integer ICUCovidTotal) { this.ICUCovidTotal = ICUCovidTotal; }
    public void setICUCovidDiff(Integer ICUCovidDiff) { this.ICUCovidDiff = ICUCovidDiff; }
    public void setICUCovidCurrent(Integer ICUCovidCurrent) { this.ICUCovidCurrent = ICUCovidCurrent; }
    public void setHospitalizationsPercentCovid(double hospitalizationsPercentCovid) { this.hospitalizationsPercentCovid = hospitalizationsPercentCovid; }
    public void setICUPercentCovid(double ICUPercentCovid) { this.ICUPercentCovid = ICUPercentCovid; }
    public void setHospitalizationCapacity(Integer hospitalizationCapacity) { this.hospitalizationCapacity = hospitalizationCapacity; }
    public void setICUCapacity(Integer ICUCapacity) { this.ICUCapacity = ICUCapacity; }

    public double getHospitalizationsPercentFull()
    {
        return hospitalizationsPercentFull;
    }

    public void setHospitalizationsPercentFull(double hospitalizationsPercentFull)
    {
        this.hospitalizationsPercentFull = hospitalizationsPercentFull;
    }

    public double getICUPercentFull()
    {
        return ICUPercentFull;
    }

    public void setICUPercentFull(double ICUPercentFull)
    {
        this.ICUPercentFull = ICUPercentFull;
    }

    public double getInfectionRateDiff()
    {
        return InfectionRateDiff;
    }

    public void setInfectionRateDiff(double infectionRateDiff)
    {
        InfectionRateDiff = infectionRateDiff;
    }

    public double getTestPositivityPercentageDiff()
    {
        return TestPositivityPercentageDiff;
    }

    public void setTestPositivityPercentageDiff(double testPositivityPercentageDiff)
    {
        TestPositivityPercentageDiff = testPositivityPercentageDiff;
    }

    public Integer getVaccinationsInitiated()
    {
        return VaccinationsInitiated;
    }

    public void setVaccinationsInitiated(Integer vaccinationsInitiated)
    {
        VaccinationsInitiated = vaccinationsInitiated;
    }

    public Integer getVaccinationsCompleted()
    {
        return VaccinationsCompleted;
    }

    public void setVaccinationsCompleted(Integer vaccinationsCompleted)
    {
        VaccinationsCompleted = vaccinationsCompleted;
    }

    public int getAverageConfirmed()
    {
        return AverageConfirmed;
    }

    public void setAverageConfirmed(int averageConfirmed)
    {
        AverageConfirmed = averageConfirmed;
    }

    public int getAverageDeaths()
    {
        return AverageDeaths;
    }

    public void setAverageDeaths(int averageDeaths)
    {
        AverageDeaths = averageDeaths;
    }

    public int getDiffAverageConfirmed()
    {
        return DiffAverageConfirmed;
    }

    public void setDiffAverageConfirmed(int diffAverageConfirmed)
    {
        DiffAverageConfirmed = diffAverageConfirmed;
    }
}

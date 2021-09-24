package com.chakfrost.covidstatistics.services.covidTracking;

/**
 * Class to handle results from http://covidtracking.com
 * queries for statistics about USA
 */
public class UnitedStatesStat
{
    public int date;

    @Deprecated
    public String dateChecked;
    public Integer death;
    public Integer deathIncrease;
    public String hash;

    @Deprecated
    public Integer hospitalized;
    public Integer hospitalizedCumulative;
    public Integer hospitalizedCurrently;
    public Integer hospitalizedIncrease;
    public Integer inIcuCumulative;
    public Integer inIcuCurrently;

    @Deprecated
    public String lastModified;
    public Integer negative;
    public Integer negativeIncrease;
    public Integer onVentilatorCumulative;
    public Integer onVentilatorCurrently;
    public Integer pending;

    @Deprecated
    public Integer posNeg;
    public Integer positive;
    public Integer positiveIncrease;
    public Integer recovered;
    public int states;

    @Deprecated
    public Integer total;
    public Integer totalTestResults;
    public Integer totalTestResultsIncrease;
}

package com.chakfrost.covidstatistics.services.covidTracking;

/**
 * Class to handle results from http://covidtracking.com
 * queries for statistics about USA
 */
public class UnitedStatesStat
{
    public int date;
    public String dateChecked;
    public Integer death;

    @Deprecated
    public Integer deathIncrease;
    public String hash;

    @Deprecated
    public Integer hospitalized;
    public Integer hospitalizedCumulative;
    public Integer hospitalizedCurrently;

    @Deprecated
    public Integer hospitalizedIncrease;
    public Integer inIcuCumulative;
    public Integer inIcuCurrently;

    @Deprecated
    public String lastModified;
    public Integer negative;

    @Deprecated
    public Integer negativeIncrease;
    public Integer onVentilatorCumulative;
    public Integer onVentilatorCurrently;
    public Integer pending;

    @Deprecated
    public Integer posNeg;
    public Integer positive;

    @Deprecated
    public Integer positiveIncrease;
    public Integer recovered;
    public int states;

    @Deprecated
    public Integer total;

    @Deprecated
    public Integer totalTestResults;

    @Deprecated
    public Integer totalTestResultsIncrease;
}

package com.chakfrost.covidstatistics.services.covidTracking;

/**
 * Class to handle results from http://covidtracking.com
 * queries for statistics about US states
 */
public class StateStat
{
    @Deprecated
    public String checkTimeEt;

    @Deprecated
    public Integer commercialScore;
    public String dataQualityGrade;
    public Integer date;

    @Deprecated
    public String dateChecked;

    @Deprecated
    public String dateModified;
    public Integer death;
    public Integer deathIncrease;
    public String fips;

    @Deprecated
    public String grade;

    @Deprecated
    public String hash;

    @Deprecated
    public Integer hospitalized;
    public Integer hospitalizedCumulative;
    public Integer hospitalizedCurrently;
    public Integer hospitalizedIncrease;
    public Integer inIcuCumulative;
    public Integer inIcuCurrently;
    public String lastUpdateEt;
    public Integer negative;

    @Deprecated
    public Integer negativeIncrease;

    @Deprecated
    public Integer negativeRegularScore;

    @Deprecated
    public Integer negativeScore;
    public Integer negativeTestsViral;
    public Integer onVentilatorCumulative;
    public Integer onVentilatorCurrently;
    public Integer pending;

    @Deprecated
    public Integer posNeg;
    public Integer positive;
    public Integer positiveCasesViral;
    public Integer positiveIncrease;

    @Deprecated
    public Integer positiveScore;
    public Integer positiveTestsViral;
    public Integer recovered;

    @Deprecated
    public Integer score;
    public String state;

    @Deprecated
    public Integer total;
    public Integer totalTestResults;
    public Integer totalTestResultsIncrease;

    @Deprecated
    public Integer totalTestsViral;
}

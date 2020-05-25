package com.chakfrost.covidstatistics.models;

import java.util.Date;

public class StatDatePair implements Comparable<StatDatePair>
{
    private Date date;
    private double value;

    public StatDatePair(Date date, double value)
    {
        this.date = date;
        this.value = value;
    }

    /** Getters **/
    public Date getDate()
    {
        return date;
    }
    public double getValue()
    {
        return value;
    }

    /** Setters **/
    public void setValue(double value)
    {
        this.value = value;
    }
    public void setDate(Date date)
    {
        this.date = date;
    }

    @Override
    public int compareTo(StatDatePair o)
    {
        if (date.getTime() > o.getDate().getTime())
            return -1;
        else if (o.getDate().getTime() > date.getTime())
            return 1;
        else
            return 0;
    }
}

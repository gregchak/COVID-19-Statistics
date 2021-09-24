package com.chakfrost.covidstatistics.models;

import java.util.List;

public class LocationInfoStat
{
    private String name;
    private List<LocationMetric> metrics;

    public LocationInfoStat(String _name, List<LocationMetric> _metrics)
    {
        this.name = _name;
        this.metrics = _metrics;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<LocationMetric> getMetrics()
    {
        return metrics;
    }

    public void setMetrics(List<LocationMetric> metrics)
    {
        this.metrics = metrics;
    }
}

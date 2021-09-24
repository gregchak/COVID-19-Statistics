package com.chakfrost.covidstatistics.models;

public class LocationMetric
{
    private String label;
    private String value;
    private String percentage;

    public LocationMetric(String _label, String _value)
    {
        this.label = _label;
        this.value = _value;
    }

    public LocationMetric(String _label, String _value, String _percentage)
    {
        this.label = _label;
        this.value = _value;
        this.percentage = _percentage;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getPercentage()
    {
        return percentage;
    }

    public void setPercentage(String percentage)
    {
        this.percentage = percentage;
    }
}

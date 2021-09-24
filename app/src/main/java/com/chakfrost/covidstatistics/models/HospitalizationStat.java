package com.chakfrost.covidstatistics.models;

public class HospitalizationStat
{
    private int date;
    private Integer hospitalizedTotal;
    private Integer hospitalizedCurrent;
    private Integer hospitalizedChange;
    private Integer icuTotal;
    private Integer icuCurrent;
    private Integer ventilatorTotal;
    private Integer ventilatorCurrent;
    private double positivityRate;

    /* Getters and Setters */
    public int getDate() { return date; }
    public void setDate(int val) { this.date = val; }

    public Integer getHospitalizedTotal()
    {
        return hospitalizedTotal;
    }
    public void setHospitalizedTotal(Integer hospitalizedTotal)
    {
        this.hospitalizedTotal = hospitalizedTotal;
    }

    public Integer getHospitalizedCurrent()
    {
        return hospitalizedCurrent;
    }
    public void setHospitalizedCurrent(Integer hospitalizedCurrent)
    {
        this.hospitalizedCurrent = hospitalizedCurrent;
    }

    public Integer getHospitalizedChange()
    {
        return hospitalizedChange;
    }
    public void setHospitalizedChange(Integer hospitalizedChange)
    {
        this.hospitalizedChange = hospitalizedChange;
    }

    public Integer getIcuTotal()
    {
        return icuTotal;
    }
    public void setIcuTotal(Integer icuTotal)
    {
        this.icuTotal = icuTotal;
    }

    public Integer getIcuCurrent()
    {
        return icuCurrent;
    }
    public void setIcuCurrent(Integer icuCurrent)
    {
        this.icuCurrent = icuCurrent;
    }

    public Integer getVentilatorTotal()
    {
        return ventilatorTotal;
    }
    public void setVentilatorTotal(Integer ventilatorTotal)
    {
        this.ventilatorTotal = ventilatorTotal;
    }

    public Integer getVentilatorCurrent()
    {
        return ventilatorCurrent;
    }
    public void setVentilatorCurrent(Integer ventilatorCurrent)
    {
        this.ventilatorCurrent = ventilatorCurrent;
    }

    public double getPositivityRate()
    {
        return positivityRate;
    }

    public void setPositivityRate(double positivityRate)
    {
        this.positivityRate = positivityRate;
    }
}

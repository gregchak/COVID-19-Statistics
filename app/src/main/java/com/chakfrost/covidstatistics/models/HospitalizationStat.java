package com.chakfrost.covidstatistics.models;

public class HospitalizationStat
{
    private int date;
    private int hospitalizedTotal;
    private int hospitalizedCurrent;
    private int hospitalizedChange;
    private int icuTotal;
    private int icuCurrent;
    private int ventilatorTotal;
    private int ventilatorCurrent;

    /* Getters and Setters */
    public int getDate() { return date; }
    public void setDate(int val) { this.date = val; }

    public int getHospitalizedTotal()
    {
        return hospitalizedTotal;
    }
    public void setHospitalizedTotal(int hospitalizedTotal)
    {
        this.hospitalizedTotal = hospitalizedTotal;
    }

    public int getHospitalizedCurrent()
    {
        return hospitalizedCurrent;
    }
    public void setHospitalizedCurrent(int hospitalizedCurrent)
    {
        this.hospitalizedCurrent = hospitalizedCurrent;
    }

    public int getHospitalizedChange()
    {
        return hospitalizedChange;
    }
    public void setHospitalizedChange(int hospitalizedChange)
    {
        this.hospitalizedChange = hospitalizedChange;
    }

    public int getIcuTotal()
    {
        return icuTotal;
    }
    public void setIcuTotal(int icuTotal)
    {
        this.icuTotal = icuTotal;
    }

    public int getIcuCurrent()
    {
        return icuCurrent;
    }
    public void setIcuCurrent(int icuCurrent)
    {
        this.icuCurrent = icuCurrent;
    }

    public int getVentilatorTotal()
    {
        return ventilatorTotal;
    }
    public void setVentilatorTotal(int ventilatorTotal)
    {
        this.ventilatorTotal = ventilatorTotal;
    }

    public int getVentilatorCurrent()
    {
        return ventilatorCurrent;
    }
    public void setVentilatorCurrent(int ventilatorCurrent)
    {
        this.ventilatorCurrent = ventilatorCurrent;
    }
}

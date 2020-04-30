package com.chakfrost.covidstatistics.models;

public class Province //extends CovidStats
{
    private Country Country;
    private String Name;

    public Province(String _name)
    {
        Name = _name;
    }

    public Province (String _name, Country _country)
    {
        Name = _name;
        Country = _country;
    }

    @Override
    public String toString() {
        return Name;
    }


    /* Getters */
    public Country getCountry() { return Country; }
    public String getName() { return Name; }

    /* Setters */
    public void setCountry(Country val) { Country = val; }
    public void setName(String val) { Name = val; }
//
//
//    /* Base class implementation */
//    public int getNewConfirmed() { return super.getNewConfirmed(); }
//    public int getTotalConfirmed() { return super.getTotalConfirmed(); }
//    public int getNewDeaths() { return super.getNewDeaths(); }
//    public int getTotalDeaths() { return super.getTotalDeaths(); }
//    public int getNewRecovered() { return super.getNewRecovered(); }
//    public int getTotalRecovered() { return super.getTotalRecovered(); }
//    public Date getStatusDate() { return super.getStatusDate(); }
//
//    public void settNewConfirmed(int val) { super.settNewConfirmed(val); }
//    public void setTotalConfirmed(int val) { super.setTotalConfirmed(val); }
//    public void setNewDeaths(int val) { super.setNewDeaths(val); }
//    public void setTotalDeaths(int val) { super.setTotalDeaths(val); }
//    public void setNewRecovered(int val) { super.setNewRecovered(val); }
//    public void setTotalRecovered(int val) { super.setTotalRecovered(val); }
//    public void setStatusDate(Date val) { super.setStatusDate(val); }
}

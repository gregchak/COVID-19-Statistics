package com.chakfrost.covidstatistics.services.covidActNow;


import java.util.List;

public class StateHistory extends State
{
    public List<MetricTimeseries> metricsTimeseries;
    public List<ActualTimeseries> actualsTimeseries;
    public List<CdcTransmissionLevelTimeseries> cdcTransmissionLevelTimeseries;
    public List<RiskLevel> riskLevelsTimeseries;
}

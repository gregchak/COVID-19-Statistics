package com.chakfrost.covidstatistics.services;

interface IServiceCallback
{
    void onSuccess(boolean allProcessesSuccessful);
    void onError(Error err);
}


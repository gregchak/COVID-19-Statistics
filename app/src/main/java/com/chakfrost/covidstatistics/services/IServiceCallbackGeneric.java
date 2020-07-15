package com.chakfrost.covidstatistics.services;

public interface IServiceCallbackGeneric
{
    <T> void onSuccess(T result);

    void onError(Error err);
}

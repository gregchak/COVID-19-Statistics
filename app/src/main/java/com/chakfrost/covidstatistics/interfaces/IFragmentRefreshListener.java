package com.chakfrost.covidstatistics.interfaces;

import android.content.Intent;

public interface IFragmentRefreshListener
{
    void onRefresh(int resultCode, Intent data);
}

package com.chakfrost.covidstatistics.ui.about;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chakfrost.covidstatistics.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AboutFragment extends Fragment
{

    private AboutViewModel mViewModel;

    public static AboutFragment newInstance()
    {
        return new AboutFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.hide();

        return inflater.inflate(R.layout.about_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(AboutViewModel.class);
        // TODO: Use the ViewModel
    }

}

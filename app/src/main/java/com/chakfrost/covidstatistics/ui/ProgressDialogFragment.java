package com.chakfrost.covidstatistics.ui;
import com.chakfrost.covidstatistics.R;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("message")
                .setPositiveButton("positive", (dialog, id) ->
                {
                    // FIRE ZE MISSILES!
                })
                .setNegativeButton("negative", (dialog, id) ->
                {
                    // User cancelled the dialog
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}

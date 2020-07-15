package com.chakfrost.covidstatistics.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chakfrost.covidstatistics.CovidUtils;
import com.chakfrost.covidstatistics.R;
import com.chakfrost.covidstatistics.models.Location;
import com.chakfrost.covidstatistics.models.OperationActions;

import java.util.List;

public class LocationSimpleListRecyclerViewAdapter extends RecyclerView.Adapter<LocationSimpleListRecyclerViewAdapter.LocationSimpleListViewHolder>
{
    private List<Location> data;
    private ItemClickListener mClickListener;

    public LocationSimpleListRecyclerViewAdapter(List<Location> data)
    {
        this.data = data;
    }
    @NonNull
    @Override
    public LocationSimpleListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_simple_list, parent, false);

        return new LocationSimpleListViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull LocationSimpleListViewHolder holder, int position)
    {
        Location location = data.get(position);

        try
        {
            holder.bind(location);
        }
        catch (Exception ex)
        {
            Log.e("onBindViewHolder()", ex.toString());
        }

        holder.getAdapterPosition();
    }

    @Override
    public int getItemCount()
    {
        return data.size();
    }


    // convenience method for getting data at click position
    public Location getItem(int id)
    {
        return data.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener)
    {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener
    {
        void onItemClick(OperationActions action, View view, int position);
    }

    /**
     * CLASS DECLARATION:
     * To store and recycle views as they are scrolled off screen
     * for LocationRecyclerViewAdapter.
     * Extends: RecyclerView.ViewHolder
     * Implements: View.OnClickListener
     */
    public class LocationSimpleListViewHolder extends RecyclerView.ViewHolder
    {
        private TextView countryName;
        private TextView provinceName;
        private TextView municipalityName;
        private ImageView refreshImage;
        private ImageView deleteImage;

        public LocationSimpleListViewHolder(View itemView)
        {
            super(itemView);
            countryName = itemView.findViewById(R.id.location_simple_list_country);
            //provinceName = itemView.findViewById(R.id.location_simple_list_province);
            //municipalityName = itemView.findViewById(R.id.location_simple_list_municipality);

            refreshImage = itemView.findViewById(R.id.location_simple_list_refresh);
            refreshImage.setOnClickListener(this::refreshClick);

            deleteImage = itemView.findViewById(R.id.location_simple_list_delete);
            deleteImage.setOnClickListener(this::deleteClick);
            //itemView.setOnClickListener(this);
        }

        //@Override
//        public void onClick(View view)
//        {
//            // Get position of row tapped
//            //int position = getAdapterPosition();
//
//            //Location loc = data.get(position);
//
//            // Add dialog?
//
//
//            if (mClickListener != null)
//                mClickListener.onItemClick(view, getAdapterPosition());
//        }

        /**
         * Starts the operation to remove a Location
         *
         * @param view  Calling view
         */
        public void refreshClick(View view)
        {
            // Fire ClickListener
            if (mClickListener != null)
                mClickListener.onItemClick(OperationActions.REFRESH, view, getAdapterPosition());

        }

        /**
         * Starts the operation to remove a Location
         *
         * @param view  Calling view
         */
        public void deleteClick(View view)
        {
            // Fire ClickListener
            if (mClickListener != null)
                mClickListener.onItemClick(OperationActions.DELETE, view, getAdapterPosition());

        }

        /**
         * Binds a Location object to the view
         *
         * @param location  Location object to bind to view
         */
        public void bind(Location location)
        {
            countryName.setText(CovidUtils.formatLocation(location));
        }
    }
}

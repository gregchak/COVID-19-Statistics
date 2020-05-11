package com.chakfrost.covidstatistics.adapters;

import android.content.Context;
import android.graphics.Color;
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
import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.Location;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class LocationStatsRecyclerViewAdapter extends RecyclerView.Adapter<LocationStatsRecyclerViewAdapter.LocationViewHolder>
{
    private Context context;
    private List<Location> data;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public LocationStatsRecyclerViewAdapter(Context context, List<Location> data)
    {
        this.context = context;
        //this.mInflater = LayoutInflater.from(this.context);
        this.data = data;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_stat_card_item, parent, false);

        //View view = mInflater.inflate(R.layout.location_statistics_row, parent, false);
        return new LocationViewHolder(view);

        /*
        NOTE: This method could return simply VieHolder as new VieHolder(view) as implemented
        in the LocationViewHolder constructor.
         */
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position)
    {
        //Location location = mData.get(holder.getAdapterPosition());
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
        //void onItemClick(View view, int position);
        void onItemClick(Location location);
    }


    /**
     * CLASS DECLARATION:
     * To store and recycle views as they are scrolled off screen
     * for LocationRecyclerViewAdapter.
     * Extends: RecyclerView.ViewHolder
     * Implements: View.OnClickListener
     */
    public class LocationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private TextView locationName;
        private TextView confirmed;
        private TextView confirmedDiff;
        private TextView deaths;
        private TextView deathsDiff;
        private TextView recovered;
        private TextView recoveredDiff;
        private TextView lastUpdated;

        private ImageView confirmedImage;
        private ImageView deathsImage;
        private ImageView recoveredImage;

        private LineChartView lineChartView;

        public LocationViewHolder(View itemView)
        {
            super(itemView);
            locationName = itemView.findViewById(R.id.stats_location_name);
            confirmed = itemView.findViewById(R.id.stats_location_confirmed_value);
            confirmedDiff = itemView.findViewById(R.id.stats_location_confirmed_diff);
            confirmedImage = itemView.findViewById(R.id.stats_location_confirmed_image);
            deaths = itemView.findViewById(R.id.stats_location_deaths_value);
            deathsDiff = itemView.findViewById(R.id.stats_location_deaths_diff);
            deathsImage = itemView.findViewById(R.id.stats_location_deaths_image);
            recovered = itemView.findViewById(R.id.stats_location_recovered_value);
            recoveredDiff = itemView.findViewById(R.id.stats_location_recovered_diff);
            recoveredImage = itemView.findViewById(R.id.stats_location_recovered_image);
            lastUpdated = itemView.findViewById(R.id.stats_location_last_updated);

            lineChartView = itemView.findViewById(R.id.stats_location_chart);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            // Get position of row tapped
            //int position = getAdapterPosition();

            //Location loc = data.get(position);

            // Add dialog?


            if (mClickListener != null)
                mClickListener.onItemClick(data.get(getAdapterPosition()));
                //mClickListener.onItemClick(view, getAdapterPosition());
        }

        /**
         * Binds a Location object to the view
         *
         * @param location  Location object to bind to view
         */
        public void bind(Location location)
        {
            List<CovidStats> stats = location.getStatistics();
            Collections.sort(stats, (s1, s2) -> s2.getStatusDate().compareTo(s1.getStatusDate()));
            CovidStats stat = stats.get(0);

            locationName.setText(CovidUtils.formatLocation(location));

            // Confirmed data
            confirmed.setText(NumberFormat.getInstance().format(stat.getTotalConfirmed()));
            confirmedDiff.setText(NumberFormat.getInstance().format(stat.getDiffConfirmed()));
            if (stat.getDiffConfirmed() > 0)
            {
                confirmedImage.setImageResource(R.drawable.ic_arrow_drop_up_yellow_24dp);
            }
            else if (stat.getDiffConfirmed() < 0)
            {
                confirmedImage.setImageResource(R.drawable.ic_arrow_drop_down_green_24dp);
            }
            else
                confirmedImage.setImageResource(R.drawable.ic_remove_black_24dp);

            // Death data
            deaths.setText(NumberFormat.getInstance().format(stat.getTotalDeaths()));
            deathsDiff.setText(NumberFormat.getInstance().format(stat.getDiffDeaths()));
            if (stat.getDiffDeaths() > 0)
            {
                deathsImage.setImageResource(R.drawable.ic_arrow_drop_up_yellow_24dp);
            }
            else if (stat.getDiffDeaths() < 0)
            {
                deathsImage.setImageResource(R.drawable.ic_arrow_drop_down_green_24dp);
            }
            else
                deathsImage.setImageResource(R.drawable.ic_remove_black_24dp);

            // Recovered data
            recovered.setText(NumberFormat.getInstance().format(stat.getTotalRecovered()));
            recoveredDiff.setText(NumberFormat.getInstance().format(stat.getDiffRecovered()));
            if (stat.getDiffRecovered() > 0)
            {
                recoveredImage.setImageResource(R.drawable.ic_arrow_drop_up_green_24dp);
            }
            else if (stat.getDiffRecovered() < 0)
            {
                recoveredImage.setImageResource(R.drawable.ic_arrow_drop_down_yellow_24dp);
            }
            else
                recoveredImage.setImageResource(R.drawable.ic_remove_black_24dp);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
            lastUpdated.setText(MessageFormat.format("as of {0}", dateFormat.format(stat.getLastUpdate())));

            buildChart(location.getStatistics());
        }

        private void buildChart(List<CovidStats> stats)
        {
            Collections.sort(stats);
            CovidStats stat;
            SimpleDateFormat dayAbv = new SimpleDateFormat("E");
            String[] axisData = new String[7];
            int[] yAxisData = new int[7];

            Collections.sort(stats);

/*            for (int i = 6; i >=0; i--)
            {
                // Account for when stats do not go 7 days back
                if (stats.size() < (i+1))
                    continue;

                stat = stats.get(i);
                axisData[i] = dayAbv.format(stat.getStatusDate());
                yAxisData[i] = stat.getDiffConfirmed();
            }*/

            for (int i = 0; i < 7 && i < stats.size(); i++)
            {
                stat = stats.get(i);
                axisData[i] = dayAbv.format(stat.getStatusDate());
                yAxisData[i] = stat.getDiffConfirmed();
            }

            List yAxisValues = new ArrayList();
            List axisValues = new ArrayList();

            //Line line = new Line(yAxisValues);
            Line line = new Line(yAxisValues).setColor(Color.parseColor("#6E1B09"));

            for(int i = 0; i < axisData.length; i++){
                axisValues.add(i, new AxisValue(i).setLabel(axisData[i]));
            }

            int counter = 0;
            for (int i = 6; i >= 0; i--)
            {
                yAxisValues.add(new PointValue(counter, yAxisData[i]));
                counter++;
            }

            //for (int i = 0; i < yAxisData.length; i++){
            //    yAxisValues.add(new PointValue(i, yAxisData[i]));
            //}

            List lines = new ArrayList();
            lines.add(line);

            LineChartData data = new LineChartData();
            data.setLines(lines);

            lineChartView.setLineChartData(data);
        }
    }
}

package com.chakfrost.covidstatistics.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Message;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

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

    // Clean all elements of the recycler
    public void clear()
    {
        data.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Location> list)
    {
        data.addAll(list);
        notifyDataSetChanged();
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
        private TextView hospitalization;
        private TextView hospitalizationDiff;
        private TextView active;
        private TextView activeDiff;
        private TextView lastUpdated;
        private TextView fatalityLabel;
        private TextView fatality;
        private TextView fatalityDiff;

        private ImageView confirmedImage;
        private ImageView deathsImage;
        private ImageView hospitalizationImage;
        private ImageView activeImage;
        private ImageView fatalityImage;

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
            hospitalization = itemView.findViewById(R.id.stats_location_hospitalization_value);
            hospitalizationDiff = itemView.findViewById(R.id.stats_location_hospitalization_diff);
            hospitalizationImage = itemView.findViewById(R.id.stats_location_hospitalization_image);
            active = itemView.findViewById(R.id.stats_location_active_value);
            activeDiff = itemView.findViewById(R.id.stats_location_active_diff);
            activeImage = itemView.findViewById(R.id.stats_location_active_image);
            fatalityLabel = itemView.findViewById(R.id.stats_location_fatality_label);
            fatality = itemView.findViewById(R.id.stats_location_fatality_value);
            fatalityDiff = itemView.findViewById(R.id.stats_location_fatality_diff);
            fatalityImage = itemView.findViewById(R.id.stats_location_fatality_image);

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
            List<CovidStats> stats = location.getStatistics().stream()
                    .filter(s -> s.getTotalConfirmed() > 0)
                    .collect(Collectors.toList());

            Collections.sort(stats, (s1, s2) -> s2.getStatusDate().compareTo(s1.getStatusDate()));
            CovidStats stat = stats.get(0);
            CovidStats previousStat = null;
            DecimalFormat df2 = new DecimalFormat("#.##");
            double fatalityRateCalculation;
            double previousFatalityRateCalculation;
            double fatalityDifference;

            if (stats.size() > 0)
                previousStat = stats.get(1);

            // For debugging
            Gson gson = new Gson();
            Type type = new TypeToken<CopyOnWriteArrayList<Location>>(){}.getType();
            //String tempJson = gson.toJson(location).toString();

//            Log.d("LocationViewHolder.bind()::location", gson.toJson(location));
//            Log.d("LocationViewHolder.bind()::stat", gson.toJson(stat));
//            Log.d("LocationViewHolder.bind()::previous", gson.toJson(previousStat));

            locationName.setText(CovidUtils.formatLocation(location));

            // Confirmed data
            confirmed.setText(NumberFormat.getInstance().format(stat.getTotalConfirmed()));
            confirmedDiff.setText(NumberFormat.getInstance().format(stat.getDiffConfirmed()));
            confirmedImage.setImageResource(CovidUtils.determineArrow(stat.getDiffConfirmed(), previousStat.getDiffConfirmed(), false));
//            Log.d("Confirm comparison", MessageFormat.format("{0} -> current: {1}; previous: {2}",
//                    CovidUtils.formatLocation(location),
//                    stat.getDiffConfirmed(),
//                    previousStat.getDiffConfirmed()));


            // Death data
            deaths.setText(NumberFormat.getInstance().format(stat.getTotalDeaths()));
            deathsDiff.setText(NumberFormat.getInstance().format(stat.getDiffDeaths()));
            deathsImage.setImageResource(CovidUtils.determineArrow(stat.getDiffDeaths(), previousStat.getDiffDeaths(), false));
//            Log.d("Death comparison", MessageFormat.format("{0} -> current: {1}; previous: {2}",
//                    CovidUtils.formatLocation(location),
//                    stat.getDiffDeaths(),
//                    previousStat.getDiffDeaths()));


            // Hospitalization data
//            if (null == stat.getHospitalizationsCurrent() || stat.getHospitalizationsCurrent() == 0)
//            {
                List<CovidStats> hospitalizationStatsTemp = stats.stream()
                        .filter(s -> s.getHospitalizationsCurrent() != null)
                        .collect(Collectors.toList());


                if (hospitalizationStatsTemp.size() > 1)
                {
                    CovidStats cur = hospitalizationStatsTemp.get(0);
                    CovidStats prev = hospitalizationStatsTemp.get(1);
                    hospitalization.setText(NumberFormat.getInstance().format(cur.getHospitalizationsCurrent()));
                    hospitalizationDiff.setText(NumberFormat.getInstance().format( (cur.getHospitalizationsCurrent() - prev.getHospitalizationsCurrent()) ));
//                    if (cur.getHospitalizationsDiff() == 0 && CovidUtils.isUSState(location))
//                    {
//                        if (cur.getHospitalizationsCurrent() == prev.getHospitalizationsCurrent())
//                            hospitalizationDiff.setText("0");
//                        else
//                            hospitalizationDiff.setText(NumberFormat.getInstance().format( (cur.getHospitalizationsCurrent() - prev.getHospitalizationsCurrent()) ));
//                    }
//                    else
//                    {
//                        hospitalizationDiff.setText(NumberFormat.getInstance().format(cur.getHospitalizationsDiff()));
//                    }
                    hospitalizationImage.setImageResource(CovidUtils.determineArrow(cur.getHospitalizationsCurrent(), prev.getHospitalizationsCurrent(), false));
                }
                else
                {
                    hospitalization.setText("N/R");
                    hospitalizationDiff.setText("N/R");
                    hospitalizationImage.setImageResource(R.drawable.ic_remove_black_24dp);
                }

//            }
//            else
//            {
//                hospitalization.setText(NumberFormat.getInstance().format(stat.getHospitalizationsCurrent()));
//                if (stat.getHospitalizationsDiff() == 0 && CovidUtils.isUSState(location))
//                {
//                    if (stat.getHospitalizationsCurrent() == previousStat.getHospitalizationsCurrent())
//                        hospitalizationDiff.setText("0");
//                    else
//                        hospitalizationDiff.setText(NumberFormat.getInstance().format( (stat.getHospitalizationsCurrent() - previousStat.getHospitalizationsCurrent()) ));
//                }
//                else
//                {
//                    hospitalizationDiff.setText(NumberFormat.getInstance().format(stat.getHospitalizationsDiff()));
//                }
//                hospitalizationImage.setImageResource(CovidUtils.determineArrow(stat.getHospitalizationsCurrent(), previousStat.getHospitalizationsCurrent(), false));
//            }

            // Active data
            if (stat.getTotalActive() == 0)
            {
                active.setText("N/R");
                activeDiff.setText("N/R");
                activeImage.setImageResource(R.drawable.ic_remove_black_24dp);
            }
            else
            {
                active.setText(NumberFormat.getInstance().format(stat.getTotalActive()));
                activeDiff.setText(NumberFormat.getInstance().format(stat.getDiffActive()));
                activeImage.setImageResource(CovidUtils.determineArrow(stat.getTotalActive(), previousStat.getTotalActive(), false));
            }

            // Fatality/Positivity rate
            if (stat.getPositivityRate() == 0)
            {
                fatalityLabel.setText(R.string.lbl_fatality);
                if (stat.getFatalityRate() == 0)
                {
                    fatalityRateCalculation = (double)stat.getTotalDeaths() / (double)stat.getTotalConfirmed();
                    previousFatalityRateCalculation = (double)previousStat.getTotalDeaths() / (double)previousStat.getTotalConfirmed();
                    fatalityDifference = fatalityRateCalculation - previousFatalityRateCalculation;

                    fatality.setText(MessageFormat.format("{0}%", df2.format(fatalityRateCalculation * 100)));
                    fatalityDiff.setText(MessageFormat.format("{0}%", df2.format(Math.abs(fatalityDifference * 100))));
                    fatalityImage.setImageResource(CovidUtils.determineArrow(fatalityRateCalculation, previousFatalityRateCalculation, false));
                }
                else
                {
                    fatality.setText(MessageFormat.format("{0}%", df2.format(stat.getFatalityRate() * 100)));
                    if (null != previousStat)
                    {
                        fatalityDifference = stat.getFatalityRate() - previousStat.getFatalityRate();
                        fatalityDiff.setText(MessageFormat.format("{0}%", df2.format(Math.abs(fatalityDifference * 100))));
                        fatalityImage.setImageResource(CovidUtils.determineArrow(stat.getFatalityRate(), previousStat.getFatalityRate(), false));
                    }
                }
            }
            else
            {
                fatalityDifference = stat.getPositivityRate() - previousStat.getPositivityRate();
                fatalityLabel.setText(R.string.lbl_positivity);
                fatality.setText(MessageFormat.format("{0}%", df2.format(stat.getPositivityRate())));
                fatalityDiff.setText(MessageFormat.format("{0}%", df2.format(Math.abs(fatalityDifference))));
                fatalityImage.setImageResource(CovidUtils.determineArrow(stat.getPositivityRate(), previousStat.getPositivityRate(), false));
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US);
            lastUpdated.setText(MessageFormat.format("as of {0}", dateFormat.format(stat.getLastUpdate())));

            buildChart(location.getStatistics());
        }

        private void buildChart(List<CovidStats> stats)
        {
            Collections.sort(stats);
            CovidStats stat;
            SimpleDateFormat dayAbv = new SimpleDateFormat("E", Locale.US);
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

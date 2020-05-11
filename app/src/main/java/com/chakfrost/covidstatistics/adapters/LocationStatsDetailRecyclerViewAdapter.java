package com.chakfrost.covidstatistics.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.R;
import com.chakfrost.covidstatistics.models.LocationStats;
import com.chakfrost.covidstatistics.models.StatDatePair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class LocationStatsDetailRecyclerViewAdapter
        extends RecyclerView.Adapter<LocationStatsDetailRecyclerViewAdapter.LocationStatsDetailHolder>
{
    //private Context context;
    private List<LocationStats> data;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public LocationStatsDetailRecyclerViewAdapter(List<LocationStats> data)
    {
        //this.context = context;
        //this.mInflater = LayoutInflater.from(this.context);
        this.data = data;
    }

    @NonNull
    @Override
    public LocationStatsDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_stat_detail_card, parent, false);

        //View view = mInflater.inflate(R.layout.location_statistics_row, parent, false);
        return new LocationStatsDetailHolder(view);

        /*
        NOTE: This method could return simply VieHolder as new VieHolder(view) as implemented
        in the LocationViewHolder constructor.
         */
    }

    @Override
    public void onBindViewHolder(@NonNull LocationStatsDetailHolder holder, int position)
    {
        LocationStats location = data.get(position);

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
    public LocationStats getItem(int id)
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
        void onItemClick(LocationStats location);
    }


    /**
     * CLASS DECLARATION:
     * To store and recycle views as they are scrolled off screen
     * for LocationStatsDetailRecyclerViewAdapter.
     * Extends: RecyclerView.ViewHolder
     * Implements: View.OnClickListener
     */
    public class LocationStatsDetailHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private TextView statName;
        private LineChartView lineChartView;

        public LocationStatsDetailHolder(View itemView)
        {
            super(itemView);
            statName = itemView.findViewById(R.id.location_stat_name);
            lineChartView = itemView.findViewById(R.id.location_stat_chart);

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
         * @param locationStat  Location object to bind to view
         */
        public void bind(LocationStats locationStat)
        {
            statName.setText(locationStat.getName());

            buildChart(locationStat.getValues());
        }

        private void buildChart(List<StatDatePair> stats)
        {
            Collections.sort(stats);
            StatDatePair stat;
            SimpleDateFormat dayAbv = new SimpleDateFormat("E");
            SimpleDateFormat dateAbv = new SimpleDateFormat("MM/dd");

            int numberOfDays = CovidApplication.DAYS_TO_DISPLAY_DETAILS;
            int[] yAxisData = new int[stats.size()];
            String[] axisData = new String[stats.size()];

//            Collections.sort(stats, (s1, s2) -> {
//               if (s1.getValue() > s2.getValue())
//                   return 1;
//               else if (s2.getValue() > s1.getValue())
//                   return -1;
//               else
//                   return 0;
//            });
//
//            int minValue = 0;
//            int maxvalue = stats.get(stats.size()-1).getValue();
//

            Collections.sort(stats);

            //Collections.reverse(stats);


            for (int i = 0; i < numberOfDays && i < stats.size(); i++)
            {
                stat = stats.get(i);
                axisData[i] = dateAbv.format(stat.getDate());
                yAxisData[i] = stat.getValue();
            }

            List yAxisValues = new ArrayList();
            List axisValues = new ArrayList();

            //Line line = new Line(yAxisValues);
            Line line = new Line(yAxisValues);
            line.setColor(Color.parseColor("#6E1B09"));

            int counter = 0;
            for (int i = axisData.length-1; i >= 0; i--)    //for(int i = 0; i < axisData.length; i++)
            {
                axisValues.add(counter, new AxisValue(counter).setLabel(axisData[i]));
                counter++;
            }

            counter = 0;
            for (int i = axisData.length-1; i >= 0; i--)
            {
                yAxisValues.add(new PointValue(counter, yAxisData[i]));
                counter++;
            }

//            for (int i = 0; i < yAxisData.length; i++){
//                yAxisValues.add(new PointValue(i, yAxisData[i]));
//            }

            List lines = new ArrayList();
            lines.add(line);

            LineChartData data = new LineChartData();
            data.setLines(lines);

            // Add data to chart
            lineChartView.setLineChartData(data);

            // Axis X
            Axis axisX = new Axis();
            axisX.setHasTiltedLabels(true);
            axisX.setTextColor(R.color.Onyx);
            axisX.setValues(axisValues);
            axisX.setTextSize(9);
            axisX.setMaxLabelChars(5);
            data.setAxisXBottom(axisX);

            // Axis Y
            Axis axisY = new Axis();
            axisY.setTextColor(R.color.Onyx);
            axisY.setTextSize(9);
            axisY.setMaxLabelChars(7);
            data.setAxisYLeft(axisY);

            final Viewport v = new Viewport(lineChartView.getMaximumViewport());
            v.top = (float) (v.top + (v.top * 0.15)); //example max value
            lineChartView.setMaximumViewport(v);
            lineChartView.setCurrentViewport(v);
            //Optional step: disable viewport recalculations, thanks to this animations will not change viewport automatically.
            lineChartView.setViewportCalculationEnabled(false);



        }

        private int determineExtraYValue(int currentValue)
        {
            if (currentValue < 10)
                return 2;
            else if (currentValue < 25)
                return 5;
            else if (currentValue < 100)
                return 10;
            else if (currentValue < 500)
                return 50;
            else if (currentValue < 1000)
                return 75;
            else if (currentValue < 5000)
                return 100;
            else
                return 100;
        }
    }
}

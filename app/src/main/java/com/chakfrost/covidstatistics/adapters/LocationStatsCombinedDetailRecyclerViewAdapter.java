package com.chakfrost.covidstatistics.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.chakfrost.covidstatistics.CovidApplication;
import com.chakfrost.covidstatistics.R;
import com.chakfrost.covidstatistics.models.LocationInfoStat;
import com.chakfrost.covidstatistics.models.LocationStats;
import com.chakfrost.covidstatistics.models.StatDatePair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

public class LocationStatsCombinedDetailRecyclerViewAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    //private Context context;
    private List<Object> data;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public LocationStatsCombinedDetailRecyclerViewAdapter(List<Object> data)
    {
        this.data = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = null;
        RecyclerView.ViewHolder vh = null;

        switch (viewType)
        {
            case 1:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.location_stat_detail_info_card, parent, false);
                vh = new LocationStatsDetailInfoHolder(v);
                break;
            case 2:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.location_stat_detail_card, parent, false);
                vh = new LocationStatsDetailHolder(v);
                break;
        }

        return vh;
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.location_stat_detail_info_card, parent, false);

        //View view = mInflater.inflate(R.layout.location_statistics_row, parent, false);
        //return new LocationStatsDetailInfoHolder(view);

        /*
        NOTE: This method could return simply VieHolder as new VieHolder(view) as implemented
        in the LocationViewHolder constructor.
         */
    }


    @Override
    public int getItemViewType(int position)
    {
        Object toBind = data.get(position);

        if (toBind instanceof LocationInfoStat)
        {
            return 1;
        }
        else if (toBind instanceof LocationStats)
        {
            return 2;
        }
        else
        {
            return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        switch(holder.getItemViewType())
        {
            case 1:
                LocationStatsDetailInfoHolder info = (LocationStatsDetailInfoHolder)holder;
                info.bind((LocationInfoStat)data.get(position));

                break;
            case 2:
                LocationStatsDetailHolder chart = (LocationStatsDetailHolder)holder;
                chart.bind((LocationStats)data.get(position));
                break;
        }

        holder.getAdapterPosition();
    }

    @Override
    public int getItemCount()
    {
        return data.size();
    }


    // convenience method for getting data at click position
    public Object getItem(int id)
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
        void onItemClick(LocationInfoStat location);
    }


    /**
     * CLASS DECLARATION:
     * To store and recycle views as they are scrolled off screen
     * for LocationStatsDetailInfoRecyclerViewAdapter.
     * Extends: RecyclerView.ViewHolder
     * Implements: View.OnClickListener
     */
    public class LocationStatsDetailInfoHolder extends RecyclerView.ViewHolder
    {
        private TextView statName;
        private ConstraintLayout constraintLayout;

        private TextView label1;
        private TextView value1;
        private TextView label2;
        private TextView value2;


        public LocationStatsDetailInfoHolder(View itemView)
        {
            super(itemView);
            statName = itemView.findViewById(R.id.location_stat_info_header);
            constraintLayout = itemView.findViewById(R.id.location_stat_detail_info_constraint_view);

            label1 = itemView.findViewById(R.id.location_stat_header_label1);
            value1 = itemView.findViewById(R.id.location_stat_header_value1);
            label2 = itemView.findViewById(R.id.location_stat_header_label2);
            value2 = itemView.findViewById(R.id.location_stat_header_value2);

            //itemView.setOnClickListener(this);
        }

//        @Override
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
//                mClickListener.onItemClick(data.get(getAdapterPosition()));
//                //mClickListener.onItemClick(view, getAdapterPosition());
//        }

        /**
         * Binds a Location object to the view
         *
         * @param infoStat  Location object to bind to view
         */
        public void bind(LocationInfoStat infoStat)
        {
            statName.setText(infoStat.getName());

            label1.setText(infoStat.getMetrics().get(0).getLabel());
            value1.setText(infoStat.getMetrics().get(0).getValue());
            label2.setText(infoStat.getMetrics().get(1).getLabel());
            value2.setText(infoStat.getMetrics().get(1).getValue());

//            ConstraintSet set = new ConstraintSet();
//            set.clone(constraintLayout);
//
//            List<String> viewIds = new ArrayList<>();
//
//            for (LocationMetric metric : infoStat.getMetrics())
//            {
//                TextView testView = new TextView(CovidApplication.getContext());
//                testView.setText("HELLO WOLD!!!!!!!!");
//                testView.setId(View.generateViewId());
//                constraintLayout.addView(testView);
//
//                set.connect(testView.getId(), ConstraintSet.TOP, statName.getId(), ConstraintSet.TOP, 16);
//                set.connect(testView.getId(),ConstraintSet.RIGHT,ConstraintSet.PARENT_ID,ConstraintSet.RIGHT,0);
//                set.connect(testView.getId(),ConstraintSet.LEFT,ConstraintSet.PARENT_ID,ConstraintSet.LEFT,0);
//            }
//            set.applyTo(constraintLayout);
        }

    }

    /**
     * CLASS DECLARATION:
     * To store and recycle views as they are scrolled off screen
     * for LocationStatsDetailRecyclerViewAdapter.
     * Extends: RecyclerView.ViewHolder
     * Implements: View.OnClickListener
     */
    public class LocationStatsDetailHolder extends RecyclerView.ViewHolder
    {
        private TextView statName;
        private LineChartView lineChartView;
        private ColumnChartView columnChartView;
        private ColumnChartData columnChartData;

        public LocationStatsDetailHolder(View itemView)
        {
            super(itemView);
            statName = itemView.findViewById(R.id.location_stat_name);
            lineChartView = itemView.findViewById(R.id.location_stat_line_chart);
            columnChartView = itemView.findViewById(R.id.location_stat_bar_chart);

            //itemView.setOnClickListener(this);
        }

//        @Override
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
//                mClickListener.onItemClick(data.get(getAdapterPosition()));
//            //mClickListener.onItemClick(view, getAdapterPosition());
//        }

        /**
         * Binds a Location object to the view
         *
         * @param locationStat  Location object to bind to view
         */
        public void bind(LocationStats locationStat)
        {
            statName.setText(locationStat.getName());

            //Log.d("Graphing", locationStat.getName());
            //if (locationStat.getName().contains("New"))
            //{
            //    lineChartView.setVisibility(View.VISIBLE);
            //    columnChartView.setVisibility(View.INVISIBLE);
            //    buildLineChart(locationStat.getValues());
            //}
            //else
            //{
            lineChartView.setVisibility(View.INVISIBLE);
            columnChartView.setVisibility(View.VISIBLE);
            buildColumnChart(locationStat.getValues());
            //}
        }

        /**
         * Creates a line chart
         * @param stats List of StatDatePair objects to be used as chart's data
         */
        private void buildLineChart(List<StatDatePair> stats)
        {
            StatDatePair stat;
            SimpleDateFormat dayAbv = new SimpleDateFormat("E", Locale.US);
            SimpleDateFormat dateAbv = new SimpleDateFormat("M/dd", Locale.US);

            int numberOfDays = CovidApplication.DAYS_TO_DISPLAY_DETAILS;
            double[] yAxisData = new double[stats.size()];
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
                yAxisValues.add(new PointValue(counter, (float)yAxisData[i]));
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

//            final Viewport v = new Viewport(lineChartView.getMaximumViewport());
//            v.top = (float) (v.top + (v.top * 0.15)); //example max value
//            lineChartView.setMaximumViewport(v);
//            lineChartView.setCurrentViewport(v);
//            //Optional step: disable viewport recalculations, thanks to this animations will not change viewport automatically.
//            lineChartView.setViewportCalculationEnabled(false);
        }

        /**
         * Creates a bar chart (column chart)
         * @param stats List of StatDatePair objects to be used as chart's data
         */
        private void buildColumnChart(List<StatDatePair> stats)
        {
            StatDatePair stat;
            double maxValForYAxis = -1;
            List<Column> columns = new ArrayList<>();
            List<SubcolumnValue> values;
            List axisValues = new ArrayList();
            String[] xAxisDataPoints = new String[stats.size()];
            SimpleDateFormat dateAbv = new SimpleDateFormat("M/dd", Locale.US);

            // Set number of columns based on stats size
            int numColumns = stats.size();

            // Displaying from past to present so sort list ASC
            //Collections.reverse(stats);

            // Loop stats to add columns
            for (int i = 0; i < numColumns; ++i)
            {
                stat = stats.get(i);
                if (stat.getValue() > maxValForYAxis)
                    maxValForYAxis = stat.getValue();

                xAxisDataPoints[i] = dateAbv.format(stat.getDate());
                axisValues.add(i, new AxisValue(i).setLabel(dateAbv.format(stat.getDate())));

                // Create 1 subcolumn, all columns need at least 1 subcolumn
                values = new ArrayList<>();
                values.add(new SubcolumnValue((float)stat.getValue(), Color.parseColor("#6E1B09")));

                //Log.d("",String.valueOf(stat.getValue()));

                // Setup column with subcolumn data
                Column column = new Column(values);
                column.setHasLabels(false);
                column.setHasLabelsOnlyForSelected(true);
                columns.add(column);
            }

            // Set data with Columns
            columnChartData = new ColumnChartData(columns);

            // Setup X axis
            Axis axisX = new Axis();
            axisX.setTextColor(R.color.Onyx);
            axisX.setTextSize(9);
            axisX.setMaxLabelChars(5);
            axisX.setValues(axisValues);
            axisX.setHasTiltedLabels(true);
            columnChartData.setAxisXBottom(axisX);

            // Setup Y axis
            Axis axisY = new Axis().setHasLines(true);
            axisY.setTextColor(R.color.Onyx);
            axisY.setTextSize(9);
            axisY.setMaxLabelChars(7);
            columnChartData.setAxisYLeft(axisY);

            // Apply data to chart
            columnChartView.setColumnChartData(columnChartData);

            // Update viewpoint to give Y axis some "height"
            //final Viewport v = new Viewport(columnChartView.getMaximumViewport());
            //v.top = (float) (v.top + (v.top * 0.15));
            //v.top = (float) (maxValForYAxis * 0.15);
            //columnChartView.setMaximumViewport(v);
            //columnChartView.setCurrentViewport(v);
            //columnChartView.setViewportCalculationEnabled(false);
        }
    }
}

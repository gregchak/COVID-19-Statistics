package com.chakfrost.covidstatistics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.text.TextUtils;
import android.view.View;

import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.HospitalizationStat;
import com.chakfrost.covidstatistics.models.Location;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CovidUtils
{
    /**
     * Shows/hides loading indicator
     *
     * @param view         View to animate
     * @param toVisibility Visibility at the end of animation
     * @param toAlpha      Alpha at the end of animation
     * @param duration     Animation duration in ms
     */
    public static void animateView(final View view, final int toVisibility, float toAlpha, int duration)
    {
        boolean show = toVisibility == View.VISIBLE;
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate()
                .setDuration(duration)
                .alpha(show ? toAlpha : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(toVisibility);
                    }
                });
    }

    /**
     * Formats a Location's country, province and municipality for display
     *
     * @param loc Location whose name is to be formatted
     * @return String formatted as Municipality, Province, Country
     */
    public static String formatLocation(Location loc)
    {
        String result = "";
        if (!TextUtils.isEmpty(loc.getMunicipality()))
            result = loc.getMunicipality();
        if (!TextUtils.isEmpty(loc.getProvince()))
        {
            if (!TextUtils.isEmpty(result))
                result += ", ";
            result += loc.getProvince();
        }

        if (!TextUtils.isEmpty(result))
            result += ", ";
        result += loc.getCountry();

//        StringBuilder sb = new StringBuilder();
//        sb.append(loc.getCountry());
//        if (!TextUtils.isEmpty(loc.getProvince()))
//            sb.append(", " + loc.getProvince());
//        if (!TextUtils.isEmpty(loc.getMunicipality()))
//            sb.append(", " + loc.getMunicipality());

        return result;
    }

    /**
     * Checks if a CovidStat exists in the list for today - 1 day
     *
     * @param stats List<> of CovidStat objects
     * @return boolean true if CovidStat exists for today - 1, false if not
     */
    public static boolean statExists(List<CovidStats> stats)
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date dte = cal.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = dateFormat.format(dte);

        CovidStats found = stats.stream()
                .filter(s -> dateFormat.format(s.getStatusDate()).equals(dateString))
                .findFirst()
                .orElse(null);

        return !(null == found);
    }

    /**
     * Determines the arrow that should be displayed by comparing current and previous values.
     * The parameter upIsGood determines whether increase or decrease is good or bad.
     *
     * @param current   Current value to be compared
     * @param previous  Previous value to be compared to
     * @param upIsGood  Whether an increase is considered "good" or not
     * @return  R.drawable ID of the determined arrow or dash
     */
    public static int determineArrow(double current, double previous, boolean upIsGood)
    {
        if (current > previous)
        {
            return upIsGood ? R.drawable.ic_arrow_drop_up_green_24dp : R.drawable.ic_arrow_drop_up_yellow_24dp;
        }
        else if (current < previous)
        {
            return upIsGood ? R.drawable.ic_arrow_drop_down_yellow_24dp : R.drawable.ic_arrow_drop_down_green_24dp;
        }
        else
        {
            return R.drawable.ic_remove_black_24dp;
        }
    }

    public static boolean isUS(Location location)
    {
        if (location.getIso().equals("USA") && location.getMunicipality() == "" && location.getProvince() == "")
            return true;
        else
            return false;
    }

    public static boolean isUSState(Location location)
    {
        if (location.getIso().equals("USA") && location.getMunicipality() == "" && location.getProvince() != "")
            return true;
        else
            return false;
    }

    public static CovidStats findCovidStat(List<CovidStats> stats, Calendar dateToCheck)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        CovidStats stat = stats.stream()
                .filter(s -> dateFormat.format(s.getStatusDate().getTime()).equals(dateFormat.format(dateToCheck.getTimeInMillis())))
                .findFirst()
                .orElse(null);

        return stat;
    }

    public static HospitalizationStat findHospitalizationStat(List<HospitalizationStat> stats, int dateAsInt)
    {
        HospitalizationStat stat = stats.stream()
                .filter(s -> s.getDate() == dateAsInt)
                .findFirst()
                .orElse(null);

        return stat;
    }

    /**
     * Finds 2 character US state abbreviation
     *
     * @param loc       Location to use for getting abbreviation
     * @return          String of 2 character abbreviation or null if not found
     */
    public static String getUSStateAbbreviation(Location loc)
    {
        String[] states = CovidApplication.getUSStates();

        String stateAbbreviation = null;
        if (isUSState(loc))
        {
            String province = loc.getProvince();
            for (int i = 0; i < states.length; i++)
            {
                if (states[i].contains("|" + province + "|"))
                {
                    String[] stateParts = states[i].split("\\|");
                    stateAbbreviation = stateParts[2];
                    break;
                }
            }
        }

        return stateAbbreviation;
    }

    public static String formatError(String message, String stackTrace)
    {
        return MessageFormat.format("{0}:\n{1}", message, stackTrace);
    }
}

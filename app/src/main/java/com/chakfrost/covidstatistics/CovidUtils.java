package com.chakfrost.covidstatistics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.text.TextUtils;
import android.view.View;

import com.chakfrost.covidstatistics.models.CovidStats;
import com.chakfrost.covidstatistics.models.HospitalizationStat;
import com.chakfrost.covidstatistics.models.Location;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    @NotNull
    public static String formatLocation(@NotNull Location loc)
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

        return result;
    }

    /**
     * Checks if a CovidStat exists in the list for today - 1 day
     *
     * @param stats List<> of CovidStat objects
     * @return boolean true if CovidStat exists for today - 1, false if not
     */
    public static boolean statExists(@NotNull List<CovidStats> stats)
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date dte = cal.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateString = dateFormat.format(dte);

        CovidStats found = stats.stream()
                .filter(s -> dateFormat.format(s.getStatusDate()).equals(dateString))
                .findFirst()
                .orElse(null);

        return !(null == found);
    }

    /**
     * Checks if a CovidStat exists in the list for today
     * @param location Location to check
     * @return boolean true if CovidStat exists for today - 1, false if not
     */
    public static boolean statExists(@NotNull Location location)
    {
        Calendar cal = Calendar.getInstance();

        // Only decrease the date by 1 if outside US
        if (!CovidUtils.isUS(location) && !CovidUtils.isUSState(location) && !CovidUtils.isUSMunicipality(location))
            cal.add(Calendar.DATE, -1);
        Date dte = cal.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateString = dateFormat.format(dte);

        CovidStats found = location.getStatistics().stream()
                .filter(s -> dateFormat.format(s.getStatusDate()).equals(dateString))
                .findFirst()
                .orElse(null);

        return !(null == found);
    }

    /**
     * Checks if a CovidStat exists in the list for a given date
     *
     * @param stats List<> of CovidStat objects
     * @param dateToCheck Date to check to see if stats are present
     * @return boolean true if CovidStat exists for today - 1, false if not
     */
    public static boolean statExists(@NotNull List<CovidStats> stats, Date dateToCheck)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateString = dateFormat.format(dateToCheck);

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

    /**
     * Determines if Location is United States
     * @param location  Location to check
     * @return          Boolean true if Location is United States, false if not
     */
    public static boolean isUS(@NotNull Location location)
    {
        return location.getIso().equals("USA") && location.getMunicipality() == "" && location.getProvince() == "";
    }

    /**
     * Determines if Location is a US State
     * @param location  Location to check
     * @return          Boolean true if Location is a US State, false if not
     */
    public static boolean isUSState(@NotNull Location location)
    {
        return location.getIso().equals("USA") && location.getMunicipality() == "" && location.getProvince() != "";
    }

    /**
     * Determines if Location is a US Municipality
     * i.e. metro area or county
     * @param location  Location to check
     * @return          Boolean true if Location is US Municipality, false if not
     */
    public static boolean isUSMunicipality(@NotNull Location location)
    {
        return location.getIso().equals("USA") && location.getMunicipality() != "" && location.getProvince() != "";
    }

    /**
     * Checks if a CovidStats exists in a List
     * @param stats         List of CovidStats
     * @param dateToCheck   Date for which to check if a CovidStat exists
     * @return              CovidStats if found, null if not found in List
     */
    @Nullable
    public static CovidStats findCovidStat(@NotNull List<CovidStats> stats, @NotNull Calendar dateToCheck)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        CovidStats stat = stats.stream()
                .filter(s -> dateFormat.format(s.getStatusDate().getTime()).equals(dateFormat.format(dateToCheck.getTimeInMillis())))
                .findFirst()
                .orElse(null);

        return stat;
    }

    /**
     * Checks if a HospitalizationStat exists in a List
     * @param stats     List of HospitalizationStat
     * @param dateAsInt Date for which to check if a CovidStat exists in YYYYMMDD format
     * @return          HospitalizationStat if found, null if not
     */
    @Nullable
    public static HospitalizationStat findHospitalizationStat(@NotNull List<HospitalizationStat> stats, @NotNull int dateAsInt)
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

    /**
     * Creates a string representation of an error using the error's message and stack trace
     * @param message       Error message
     * @param stackTrace    Error stack trace
     * @return              String representation of message and stack trace
     */
    public static String formatError(String message, String stackTrace)
    {
        return MessageFormat.format("{0}:\n{1}", message, stackTrace);
    }

    public static String[] getRandomLocationRetrieveCompletePair()
    {
        String[] retVal = new String[2];
        String[][] values = CovidApplication.getLocationRetrieveComplete();
        int r = (int) (Math.random() * (values.length/2));

        retVal[0] = values[r][0];
        retVal[1] = values[r][1];

        return retVal;
    }
}

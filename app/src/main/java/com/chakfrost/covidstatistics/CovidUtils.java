package com.chakfrost.covidstatistics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.text.TextUtils;
import android.view.View;

import com.chakfrost.covidstatistics.models.Location;

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
}

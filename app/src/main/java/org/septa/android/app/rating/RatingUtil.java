package org.septa.android.app.rating;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import org.septa.android.app.R;

/**
 * Rating Utilities
 */

public class RatingUtil {

    private static final int MIN_USES_TO_RATE = 2; // TODO: change to 20

    // increment this when wanting to re-ask user for a rating
    private static final int CURRENT_RATING_ID = 1;

    private RatingUtil() {

    }

    public static void showRatingDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.rating_title))
                .setMessage(R.string.rating_details)
                .setPositiveButton(R.string.rating_rate_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // link user to playstore
                        rateAppInPlayStore(context);

                        SharedPreferencesRatingUtil.setAppRated(context, true);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.rating_no_thanks_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferencesRatingUtil.setAppRated(context, true);
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(R.string.rating_remind_me_later_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    public static void rateAppInPlayStore(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

    public static boolean shouldShowDialog(final Context context) {
        // reset app rated tracker if rating ID incremented
        if (SharedPreferencesRatingUtil.getRatingId(context) < CURRENT_RATING_ID) {
            SharedPreferencesRatingUtil.setAppRated(context, false);
            SharedPreferencesRatingUtil.setNumberOfUses(context, 0);
            SharedPreferencesRatingUtil.setRatingId(context, CURRENT_RATING_ID);
        }

        return !hasRated(context) && hasUsedAppEnough(context);
    }

    private static boolean hasRated(final Context context) {
        return SharedPreferencesRatingUtil.getAppRated(context);
    }

    private static boolean hasUsedAppEnough(final Context context) {
        return MIN_USES_TO_RATE <= SharedPreferencesRatingUtil.getNumberOfUses(context);
    }
}


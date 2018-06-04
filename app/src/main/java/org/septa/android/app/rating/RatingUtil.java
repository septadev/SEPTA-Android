package org.septa.android.app.rating;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;

import org.septa.android.app.R;

/**
 * Rating Utilities
 */

public class RatingUtil {

    private static final int MIN_USES_TO_RATE = 2; // TODO: change to 20

    // increment this when wanting to re-ask user for a rating
    private static final int CURRENT_RATING_ID = 0;

    private RatingUtil() {

    }

    public static void showRatingDialog(final Context context) {
        final BottomSheetDialog ratingDialog = new BottomSheetDialog(context);
        ratingDialog.setContentView(R.layout.dialog_rating);

        final View closeDialog = ratingDialog.findViewById(R.id.rating_dialog_close),
                rateApp = ratingDialog.findViewById(R.id.button_rate_app),
                noThanks = ratingDialog.findViewById(R.id.button_rate_app_no_thanks),
                remindMeLater = ratingDialog.findViewById(R.id.button_rate_app_remind_me_later);

        if (closeDialog != null) {
            closeDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // force crash
                    throw new RuntimeException();

//                    ratingDialog.dismiss(); // TODO: put back later
                }
            });
        }
        if (rateApp != null) {
            rateApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // link user to playstore
                    rateAppInPlayStore(context);

                    // save that user rated the app
                    SharedPreferencesRatingUtil.setAppRated(context, true);

                    ratingDialog.dismiss();
                }
            });
        }
        if (noThanks != null) {
            noThanks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // save that user declined to rate app
                    SharedPreferencesRatingUtil.setAppRated(context, true);

                    ratingDialog.dismiss();
                }
            });
        }
        if (remindMeLater != null) {
            remindMeLater.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ratingDialog.dismiss();
                }
            });
        }

        ratingDialog.show();
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

        return !hasRated(context) && hasUsedAppEnough(context) && !didAppJustCrash(context) && hasAppRanOnceCrashFree(context);
    }

    private static boolean hasRated(final Context context) {
        return SharedPreferencesRatingUtil.getAppRated(context);
    }

    private static boolean hasUsedAppEnough(final Context context) {
        return MIN_USES_TO_RATE <= SharedPreferencesRatingUtil.getNumberOfUses(context);
    }

    private static boolean didAppJustCrash(Context context) {
        return SharedPreferencesRatingUtil.getAppJustCrashed(context);
    }

    private static boolean hasAppRanOnceCrashFree(Context context) {
        return SharedPreferencesRatingUtil.getAppRanOnceCrashFree(context);
    }
}
package org.septa.android.app.rating;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.R;

/**
 * Rating Utilities
 */

public abstract class RatingUtil {

    public static final int MIN_USES_TO_RATE = 5; // TODO: change to 20

    // TODO: change to 6 (must be one more than the number of uses)
    public static final int MIN_CRASH_FREE_USES_TO_RATE = 3; // must be one more than the number of uses wanted

    // increment this when wanting to re-ask user for a rating
    private static final int CURRENT_RATING_ID = 0;

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
                    // tapping the close button force crashes the app for debug / alpha
                    if (BuildConfig.FORCE_CRASH_ENABLED) {
                        // force crash the app
                        throw new RuntimeException("This is a forced crash");

                    } else {
                        ratingDialog.dismiss();
                    }
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

        return !hasRated(context) && hasUsedAppEnough(context) && !didAppJustCrash(context);
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
}
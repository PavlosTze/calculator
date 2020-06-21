/*
    Used for prompting user to rate the app in the play store. Also used for showing a message
    at first launch of the app.
*/

package com.tzegian.Calculator.dialogs;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;
import com.tzegian.Calculator.R;

import static com.tzegian.Calculator.MainActivity.mFirebaseAnalytics;
import static com.tzegian.Calculator.MainActivity.sharedPref;

public class AppRaterDialog extends AppCompatActivity {

    /* 
        Minimum 1 day and 4 launches for prompting the user.
    */
    private final static int DAYS_UNTIL_PROMPT_RATE = 2;
    private final static int LAUNCHES_UNTIL_PROMPT_RATE = 3;

    /*
        This function is run on every main activity creation. If the prompt has already
        shown 1 time and the user chose "not show again" then nothing happens.
        Else it increments launch counter and (if possible) days counter, saves these counters in SharedPreferences, and if conditions are met
        then prompts the user to rate the app.
        Also if it is the first launch, a welcome message is shown.
    */
    public static void app_launched(Context mContext) {

        if (sharedPref.getBoolean("dontshowagain", false)) { return ; }

        // Increment launch counter
        long launch_count = sharedPref.getLong("launch_count", 0) + 1;
        sharedPref.edit().putLong("launch_count", launch_count).apply();

        // Get date of first launch
        long date_firstLaunch = sharedPref.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            sharedPref.edit().putLong("date_firstlaunch", date_firstLaunch).apply();
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT_RATE) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT_RATE * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext);
            }
        }


        if (launch_count <=1) {
            showFirstLaunchDialog(mContext);
        }
    }

    /*
        An alertDialog used for showing a message at the first launch of the app.
    */
    private static void showFirstLaunchDialog(final Context mContext) {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(mContext.getString(R.string.firstLaunchTitle));
        alertDialog.setMessage(mContext.getString(R.string.firstLaunch));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextSize(18);
    }

    /*
        An alertDialog used for showing a message in order to prompt the user rate the app.
    */
    private static void showRateDialog(final Context mContext) {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(mContext.getString(R.string.ratedialog));
        alertDialog.setMessage(mContext.getString(R.string.textdialog));
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getString(R.string.nothanksdialog),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = new Bundle();
                        mFirebaseAnalytics.logEvent("rateDialogNo" , bundle);
                        sharedPref.edit().putBoolean("dontshowagain", true).apply();
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, mContext.getString(R.string.remindmelaterdialog),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = new Bundle();
                        mFirebaseAnalytics.logEvent("rateDialogLater" , bundle);
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.rateandreviewdialog),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = new Bundle();
                        mFirebaseAnalytics.logEvent("rateDialogYes" , bundle);

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        //Try Google play
                        intent.setData(Uri.parse("market://details?id=com.tzegian.Calculator"));
                        if (!rateActivity(intent,mContext)) {

                            //Market (Google play) app seems not installed, let's try to open a webbrowser
                            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.tzegian.Calculator"));
                            if (!rateActivity(intent,mContext)) {
                                //Well if this also fails, we have run out of options, inform the user.
                                Toast.makeText(mContext, "Could not open Google Play Store.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        sharedPref.edit().putBoolean("dontshowagain", true).apply();
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /*
        Used for starting the google play app for rating the app. Also catches the error when google play is not installed.
    */
    private static boolean rateActivity(Intent a, final Context mContext) {
        try {
            mContext.startActivity(a);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }
}
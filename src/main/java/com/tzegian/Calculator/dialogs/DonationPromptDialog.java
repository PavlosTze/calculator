/*
    Used for prompting user to rate the app in the play store. Also used for showing a message
    at first launch of the app.
*/

package com.tzegian.Calculator.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import com.tzegian.Calculator.DonationActivity;
import com.tzegian.Calculator.R;

import static com.tzegian.Calculator.MainActivity.mFirebaseAnalytics;
import static com.tzegian.Calculator.MainActivity.sharedPref;

public class DonationPromptDialog {

    /* 
        Minimum 7 days and 10 launches for prompting the user.
    */
    private final static int DAYS_UNTIL_PROMPT_DONATION = 7;
    private final static int LAUNCHES_UNTIL_PROMPT_DONATION = 10;

    /*
        This function is run on every main activity creation. If the prompt has already
        shown 1 time and the user chose "not show again" then nothing happens.
        Else it increments launch counter and (if possible) days counter, saves these counters in SharedPreferences, and if conditions are met
        then prompts the user to rate the app.
        Also if it is the first launch, a welcome message is shown.
    */
    public static void app_launched(Context mContext) {

        if (sharedPref.getBoolean("dontshowagaindonation", false)) { return ; }

        long launch_count = sharedPref.getLong("launch_count", 0) + 1;

        // Get date of first launch
        long date_firstLaunch = sharedPref.getLong("date_firstlaunch", 0);

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT_DONATION) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT_DONATION * 24 * 60 * 60 * 1000)) {
                showDonationPrompt(mContext);
            }
        }
    }

    /*
        An alertDialog used for showing a message in order to prompt the user rate the app.
    */
    private static void showDonationPrompt(final Context mContext) {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(mContext.getString(R.string.donate_title_dialog));
        alertDialog.setMessage(mContext.getString(R.string.donation_description_dialog));
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getString(R.string.nothanksdialog),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = new Bundle();
                        mFirebaseAnalytics.logEvent("donateDialogNo" , bundle);
                        sharedPref.edit().putBoolean("dontshowagaindonation", true).apply();
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, mContext.getString(R.string.remindmelaterdialog),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = new Bundle();
                        mFirebaseAnalytics.logEvent("donateDialogLater" , bundle);
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.donate_yes_dialog),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = new Bundle();
                        mFirebaseAnalytics.logEvent("donateDialogYes" , bundle);
                        Intent intent = new Intent(mContext, DonationActivity.class);
                        mContext.startActivity(intent);
                        sharedPref.edit().putBoolean("dontshowagaindonation", true).apply();
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
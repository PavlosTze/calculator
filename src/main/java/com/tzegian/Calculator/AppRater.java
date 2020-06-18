/*
    Used for prompting user to rate the app in the play store. Also used for showing a message
    at first launch of the app.
*/

package com.tzegian.Calculator;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.graphics.Color.parseColor;

public class AppRater extends AppCompatActivity {

    /* 
        Minimum 1 day and 4 launches for prompting the user.
    */
    private final static int DAYS_UNTIL_PROMPT = 1;
    private final static int LAUNCHES_UNTIL_PROMPT = 2;

    /*
        This function is run on every main activity creation. If the prompt has already
        shown 1 time and the user chose "not show again" then nothing happens.
        Else it increments launch counter and (if possible) days counter, saves these counters in SharedPreferences, and if conditions are met
        then prompts the user to rate the app.
        Also if it is the first launch, a welcome message is shown.
    */
    public static void app_launched(Context mContext) {

        if (MainActivity.sharedPref.getBoolean("dontshowagain", false)) { return ; }

        // Increment launch counter
        long launch_count = MainActivity.sharedPref.getLong("launch_count", 0) + 1;
        MainActivity.editor.putLong("launch_count", launch_count);

        // Get date of first launch
        long date_firstLaunch = MainActivity.sharedPref.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            MainActivity.editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext);
            }
        }


        if (launch_count <=1) {
            showFirstLaunchDialog(mContext);
        }

        MainActivity.editor.apply();
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

        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle(mContext.getString(R.string.ratedialog));

        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackgroundColor(parseColor("#ece9c3"));

        TextView tv = new TextView(mContext);
        tv.setText(mContext.getString(R.string.textdialog));
        tv.setTextColor(-12303292);
        tv.setBackgroundColor(parseColor("#ece9c3"));
        tv.setTextSize(18);
        tv.setWidth(240);
        tv.setPadding(4, 0, 4, 10);
        ll.addView(tv);

        /* 
            Actions for showing RATE APP button and actions if clicked.
        */
        Button b1 = new Button(mContext);
        b1.setText(mContext.getString(R.string.rateandreviewdialog));
        b1.setBackgroundColor(-16711936);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                MainActivity.editor.putBoolean("dontshowagain", true);
                MainActivity.editor.apply();
                dialog.dismiss();
            }
        });
        ll.addView(b1);

        /* 
            Actions for showing REMIND ME LATER button and actions if clicked.
        */
        Button b2 = new Button(mContext);
        b2.setText(mContext.getString(R.string.remindmelaterdialog));
        b2.setBackgroundColor(-3355444);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ll.addView(b2);

        /* 
            Actions for showing DON'T SHOW AGAIN button and actions if clicked.
        */
        Button b3 = new Button(mContext);
        b3.setText(mContext.getString(R.string.nothanksdialog));
        b3.setBackgroundColor(-65536);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.editor != null) {
                    MainActivity.editor.putBoolean("dontshowagain", true);
                    MainActivity.editor.apply();
                }
                dialog.dismiss();
            }
        });
        ll.addView(b3);

        dialog.setContentView(ll);
        dialog.show();
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
/*
    Used for implementing different themes and how to change them and save them.
    Current theme is saved using SharedPreferences so user chooses a theme, we save it there
    (so the user won't have to choose it everytime he launches the app).
    First we save the current SharedPreferences data as it contains some more data for vibration setting, how many days
    user has the app and how many times the app is used. Then we clear the SharedPreferences data, we save the new theme
    the user wants to use and we save the other settings as mentionted above also.
*/

package com.tzegian.Calculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class ChangeTheme extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_theme);
    }

    public void onClick(View view) {
        ImageButton but = (ImageButton) view;
        int buttonText = but.getId();
        long launches = MainActivity.sharedPref.getLong("launch_count", 0);
        long days = MainActivity.sharedPref.getLong("date_firstlaunch", 0);
        boolean dontshowagain = MainActivity.sharedPref.getBoolean("dontshowagain", false);
        double memorynumber = MainActivity.getDouble(MainActivity.sharedPref,getString(R.string.memory), 0);

        if (buttonText == R.id.themeGreyBlue) {
            MainActivity.editor.clear();
            MainActivity.editor.apply();
            MainActivity.editor.putBoolean(getString(R.string.vibrate), MainActivity.isCheckedVibrator);
            MainActivity.editor.putInt(getString(R.string.activityMain), R.layout.activity_main);
            MainActivity.editor.putString(MainActivity.displayFormat, MainActivity.displayFormat);
            MainActivity.editor.putLong("date_firstlaunch", days);
            MainActivity.editor.putLong("launch_count", launches);
            MainActivity.editor.putBoolean("dontshowagain", dontshowagain);
            if (memorynumber != 0)
            {
                MainActivity.putDouble(MainActivity.editor,getString(R.string.memory), memorynumber);
            }
            MainActivity.editor.apply();
        } else if (buttonText == R.id.themeGreyGreen) {
            MainActivity.editor.clear();
            MainActivity.editor.apply();
            MainActivity.editor.putBoolean(getString(R.string.vibrate), MainActivity.isCheckedVibrator);
            MainActivity.editor.putInt(getString(R.string.activityMainGreen), R.layout.activity_main_green);
            MainActivity.editor.putString(MainActivity.displayFormat, MainActivity.displayFormat);
            MainActivity.editor.putLong("date_firstlaunch", days);
            MainActivity.editor.putLong("launch_count", launches);
            MainActivity.editor.putBoolean("dontshowagain", dontshowagain);
            if (memorynumber != 0)
            {
                MainActivity.putDouble(MainActivity.editor,getString(R.string.memory), memorynumber);
            }
            MainActivity.editor.apply();
        } else if (buttonText == R.id.themeDarkModern) {
            MainActivity.editor.clear();
            MainActivity.editor.apply();
            MainActivity.editor.putBoolean(getString(R.string.vibrate), MainActivity.isCheckedVibrator);
            MainActivity.editor.putInt(getString(R.string.activityMainDarkModern), R.layout.activity_main_dark_modern);
            MainActivity.editor.putString(MainActivity.displayFormat, MainActivity.displayFormat);
            MainActivity.editor.putLong("date_firstlaunch", days);
            MainActivity.editor.putLong("launch_count", launches);
            MainActivity.editor.putBoolean("dontshowagain", dontshowagain);
            if (memorynumber != 0)
            {
                MainActivity.putDouble(MainActivity.editor,getString(R.string.memory), memorynumber);
            }
            MainActivity.editor.apply();
        } else if(buttonText == R.id.themeLightModern)
        {
            MainActivity.editor.clear();
            MainActivity.editor.apply();
            MainActivity.editor.putBoolean(getString(R.string.vibrate), MainActivity.isCheckedVibrator);
            MainActivity.editor.putInt(getString(R.string.activityMainLightModern), R.layout.activity_main_light_modern);
            MainActivity.editor.putString(MainActivity.displayFormat, MainActivity.displayFormat);
            MainActivity.editor.putLong("date_firstlaunch", days);
            MainActivity.editor.putLong("launch_count", launches);
            MainActivity.editor.putBoolean("dontshowagain", dontshowagain);
            if (memorynumber != 0)
            {
                MainActivity.putDouble(MainActivity.editor,getString(R.string.memory), memorynumber);
            }
            MainActivity.editor.apply();
        } else if(buttonText == R.id.themePinkModern)
        {
            MainActivity.editor.clear();
            MainActivity.editor.apply();
            MainActivity.editor.putBoolean(getString(R.string.vibrate), MainActivity.isCheckedVibrator);
            MainActivity.editor.putInt(getString(R.string.activityMainPinkModern), R.layout.activity_main_pink_modern);
            MainActivity.editor.putString(MainActivity.displayFormat, MainActivity.displayFormat);
            MainActivity.editor.putLong("date_firstlaunch", days);
            MainActivity.editor.putLong("launch_count", launches);
            MainActivity.editor.putBoolean("dontshowagain", dontshowagain);
            if (memorynumber != 0)
            {
                MainActivity.putDouble(MainActivity.editor,getString(R.string.memory), memorynumber);
            }
            MainActivity.editor.apply();
        } else if(buttonText == R.id.themeGreyPink) {
            MainActivity.editor.clear();
            MainActivity.editor.apply();
            MainActivity.editor.putBoolean(getString(R.string.vibrate), MainActivity.isCheckedVibrator);
            MainActivity.editor.putInt(getString(R.string.activityMainPink), R.layout.activity_main_pink);
            MainActivity.editor.putString(MainActivity.displayFormat, MainActivity.displayFormat);
            MainActivity.editor.putLong("date_firstlaunch", days);
            MainActivity.editor.putLong("launch_count", launches);
            MainActivity.editor.putBoolean("dontshowagain", dontshowagain);
            if (memorynumber != 0)
            {
                MainActivity.putDouble(MainActivity.editor,getString(R.string.memory), memorynumber);
            }
            MainActivity.editor.apply();
        }
        finish();
    }

}

/*
    Used for implementing different themes and how to change them and save them.
    Current theme is saved using SharedPreferences so user chooses a theme, we save it there
    (so the user won't have to choose it every time he launches the app).
    First we save the current SharedPreferences data as it contains some more data for vibration setting, how many days
    user has the app and how many times the app is used. Then we clear the SharedPreferences data, we save the new theme
    the user wants to use and we save the other settings as mentioned above also.
*/

package com.tzegian.Calculator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import static com.tzegian.Calculator.MainActivity.CURRENT_THEME;
import static com.tzegian.Calculator.MainActivity.mFirebaseAnalytics;
import static com.tzegian.Calculator.MainActivity.sharedPref;

public class ChangeThemeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_theme);

        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent("changeThemeView" , bundle);
    }

    public void onClickChangeTheme(View view) {
        ImageButton but = (ImageButton) view;
        int buttonText = but.getId();

        Bundle bundle = new Bundle();

        switch (buttonText) {
            case R.id.themeGreyBlue:
                mFirebaseAnalytics.logEvent("activity_main" , bundle);
                sharedPref.edit().putInt(CURRENT_THEME, R.layout.activity_main).apply();
                break;
            case R.id.themeGreyGreen:
                mFirebaseAnalytics.logEvent("activity_main_green" , bundle);
                sharedPref.edit().putInt(CURRENT_THEME, R.layout.activity_main_green).apply();
                break;
            case R.id.themeDarkModern:
                mFirebaseAnalytics.logEvent("activity_main_dark_modern" , bundle);
                sharedPref.edit().putInt(CURRENT_THEME, R.layout.activity_main_dark_modern).apply();
                break;
            case R.id.themeLightModern:
                mFirebaseAnalytics.logEvent("activity_main_light_modern" , bundle);
                sharedPref.edit().putInt(CURRENT_THEME, R.layout.activity_main_light_modern).apply();
                break;
            case R.id.themePinkModern:
                mFirebaseAnalytics.logEvent("activity_main_pink_modern" , bundle);
                sharedPref.edit().putInt(CURRENT_THEME, R.layout.activity_main_pink_modern).apply();
                break;
            case R.id.themeGreyPink:
                mFirebaseAnalytics.logEvent("activity_main_pink" , bundle);
                sharedPref.edit().putInt(CURRENT_THEME, R.layout.activity_main_pink).apply();
                break;
            default:
                break;
        }
        finish();
    }

}

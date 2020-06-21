/*
    Just a different activity used when user wants to read the "about" section which contains
    some basic information about the Developer and the app.
*/

package com.tzegian.Calculator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import static com.tzegian.Calculator.MainActivity.mFirebaseAnalytics;

public class DisplayAboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_about);

        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent("displayAbout" , bundle);
    }
}

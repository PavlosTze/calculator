/*
    Just a different activity used when user wants to read the "about" section which contains
    some basic information about the Developer and the app.
*/

package com.tzegian.Calculator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class DisplayAbout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_about);
    }
}

/*
    Used for implementing history's activity. Its creation, defines how to show the
    history's entries and provides appropriate actions when the user wants to clear the history database.
*/

package com.tzegian.Calculator;

import android.database.Cursor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import java.util.ArrayList;

public class History extends AppCompatActivity {

    private LinearLayout linearLayout;
    private ArrayList<String> historyList;
    private LayoutParams layoutParams;

    /*
        Creates the content view, initiliazes historyList as an ArrayList
        and gets a copy of the database in order to read the entries from it when the UI will be created.
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        linearLayout = (LinearLayout) findViewById(R.id.LinLayHistory);

        layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        historyList = new ArrayList<>();

        try {
            DBHelper.datab = MainActivity.databaseHelper.getReadableDatabase();
            updateUI();
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("An error has happened and history cannot be displayed. Please send an email to pavlostze@gmail.com to describe what happened.")

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNeutralButton(R.string.OK, null)
                .show();
        }
    }

    
    /*
        Responsible for updating history UI, reading from the database the entries and showing them.
    */
    private void updateUI() {
        Cursor cursor = DBHelper.datab.query(DBHelper.TABLE,
                new String[]{DBHelper.COLUMN_ID, DBHelper.COLUMN_CONTENT},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(DBHelper.COLUMN_CONTENT);
            historyList.add(cursor.getString(idx));
        }

        for (int i = 0; i < historyList.size(); i++) {
            TextView textView1 = new TextView(this);
            textView1.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            textView1.setText(historyList.get(i));
            textView1.setLayoutParams(layoutParams);
            textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            layoutParams.gravity = Gravity.RIGHT;
            layoutParams.setMargins(10, 10, 10, 10); // (left, top, right, bottom)
            linearLayout.addView(textView1);
        }

        cursor.close();
    }

    /*
        When user clicks the "clear history" button, this function runs.
    */
    public void clearHistory(View view)
    {
        DBHelper.datab.execSQL("DELETE FROM "+ DBHelper.TABLE);
        DBHelper.datab.execSQL("vacuum");
        finish();
    }

}
/*
Author: Pavlos Tzegiannakis

Provides all the functions needed for launching the app successfully and working as supposed to. 
This is the main activity which runs at every app's launch.
*/

package com.tzegian.Calculator;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private TextView result;            /* the textview related to result. when result changes, this variable changes too */
    private TextView ops;               /* textview related with current operation */
    private TextView opsFull;           /* textview related with all operations the user has made till now without clicking to clear the screen */
    private Vibrator vibrator;          /* used for vibrate feature-setting */
    private double res = 0D;            /* result stored as a double */
    private double op1 = 0D;            /* first operator stored as a double */
    private double op2 = 0D;            /* second operator stored as a double */
    
    private int whenToLookForOp2 = 0;   /* describes whether on the current click the user has typed a ready calculation 
                                           so we should find the second operator(such as 5+5 and not something like 5+)   
                                           and increments-decrements this variable depending of the current operation */
    
    private boolean starting;           /* describes if user has already done some calculation or this will be the first one */
    private boolean error;              /* describes if user's calculations have an error (for example division with zero) */
    private boolean checkAtResult;      /* needed for making some extra actions when result button is clicked depending on some conditions if met or not */
    private boolean foundPerOp2 = false;    /* if second operator is a percentage */
    private boolean foundSqrtOp2 = false;   /* if second operator is a square root of a number */
    private History history;            /* needed to implement history */

    private String results;             /* string related to result. we use this variable to change the appropriate textview */
    private String operationsFull;      /* string related with all operations the user has made till now. we use this variable to change the appropriate textview */
    private String operations;          /* string related with current operation. we use this variable to change the appropriate textview */

    protected static SharedPreferences sharedPref;      /* used for keeping settings, desired theme etc. saved at SharedPreferences */
    protected static SharedPreferences.Editor editor;   /* editor used for changing the sharedPref variable */
    protected static boolean isCheckedVibrator = true;  /* used for knowing whether user has vibration enabled or not */
    protected static String displayFormat;              /* used for knowing which display format user has chosen */
    public static DBHelper databaseHelper;              /* used making changes in history database, mainly adding data */
    
    
    /* 
        Function running at activity-app launch. We initialize our basic variables for settings, for history and 
        its database, our 3 TextViews and some other basic variables needed for calculations.
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        databaseHelper = new DBHelper(this);
        DBHelper.datab = databaseHelper.getWritableDatabase();

        history = new History();

        sharedPref = this.getPreferences(MODE_PRIVATE);

        editor = sharedPref.edit();

        /* 
            theme setting initialization 
        */
        dialogThemeChanger();

        /* 
            displayFormat setting initialization 
        */
        if (sharedPref.contains(getString(R.string.dotComma))) {
            displayFormat = getString(R.string.dotComma);
        } else if (sharedPref.contains(getString(R.string.commaDot))) {
            displayFormat = getString(R.string.commaDot);
        } else if (sharedPref.contains(getString(R.string.spaceDot))) {
            displayFormat = getString(R.string.spaceDot);
        } else if(sharedPref.contains(getString(R.string.spaceComma))){
            displayFormat = getString(R.string.spaceComma);
        } else
        {
            displayFormat = getString(R.string.dotComma);
            editor.putInt(getString(R.string.dotComma), R.string.dotComma);
            editor.apply();
        }

        /* 
            vibration setting initialization 
        */
        if (!sharedPref.contains(getString(R.string.vibrate))) {
            editor.putBoolean(getString(R.string.vibrate), true);
            editor.apply();
        } else {
            isCheckedVibrator = sharedPref.getBoolean(getString(R.string.vibrate), true);
        }

        result = (TextView) findViewById(R.id.resultText);
        opsFull = (TextView) findViewById(R.id.operationsFull);
        ops = (TextView) findViewById(R.id.operationsRuntime);
        starting = true;
        error = false;
        checkAtResult = false;

        AppRater.app_launched(this);

        /*
            Screen orientation is by default at PORTRAIT.
        */
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /*
        Creates the options' menu
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /*
        Modifies the options' menu at vibration entry of enabled or not
    */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.vibrator);
        checkable.setChecked(isCheckedVibrator);
        return true;
    }

    /* 
        Describes actions that should be done at each click on an option.
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /*
            Save the textView's data into variables.
        */
        results = result.getText().toString();
        operationsFull = opsFull.getText().toString();
        operations = ops.getText().toString();

        sharedPref = this.getPreferences(MODE_PRIVATE);
        editor = sharedPref.edit();

        final int id = item.getItemId();

        switch (id) {
            case R.id.themeChanger:
                vibrate();
                Intent intenttheme = new Intent(this, ChangeTheme.class);
                startActivity(intenttheme);
                break;
            case R.id.about:
                vibrate();
                Intent intent = new Intent(this, DisplayAbout.class);
                startActivity(intent);
                break;
            case R.id.rate:
                vibrate();
                btnRateAppOnClick(null);
                break;
            case R.id.vibrator:
                isCheckedVibrator = !item.isChecked();
                item.setChecked(isCheckedVibrator);
                editor.remove(getString(R.string.vibrate));
                editor.putBoolean(getString(R.string.vibrate), isCheckedVibrator);
                editor.apply();
                break;
            case R.id.history:
                vibrate();
                Intent intent1 = new Intent(this, History.class);
                startActivity(intent1);
                break;
            case R.id.displayFormat:
                vibrate();
                onCreateDialogSingleChoice();
                Dialog dialog = onCreateDialogSingleChoice();
                dialog.show();
                break;
            default:
                break;
        }

        /*
            Restores the TextView's data
        */
        saveTexts();

        return super.onOptionsItemSelected(item);
    }

    /*
        If app pauses, save the textView's data into variables.
    */
    @Override
    public void onPause() {
        super.onPause();

        results = result.getText().toString();
        operationsFull = opsFull.getText().toString();
        operations = ops.getText().toString();
    }

    /*
        If app restarts, restore the textView's data from the saved variables as also as the current theme.
    */
    @Override
    public void onRestart() {
        super.onRestart();
        dialogThemeChanger();
        saveTexts();
    }

    /*
        Used for starting a new activity-app such as Google Play when the user wants to rate the app.
    */
    private boolean rateActivity(Intent a) {
        try {
            startActivity(a);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    /*
        Actions done when user clicks the rate button on the app.
    */
    public void btnRateAppOnClick(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //Try Google play
        intent.setData(Uri.parse("market://details?id=com.tzegian.Calculator"));
        if (!rateActivity(intent)) {
            //Market (Google play) app seems not installed, let's try to open a webbrowser
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.tzegian.Calculator"));
            if (!rateActivity(intent)) {
                //Well if this also fails, we have run out of options, inform the user.
                Toast.makeText(this, "Could not open Android market, please install the market app.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
        Actions done when the user clicks the clear button. TextView's get emptyString
        and some other variables initialize as the app has just started.
    */
    public void clearTexts(View view) {
        vibrate();
        ops.setText(R.string.emptyString);
        opsFull.setText(R.string.emptyString);
        result.setText(R.string.zero);
        op1 = 0D;
        res = 0D;
        op2 = 0D;
        whenToLookForOp2 = 0;
        starting = true;
        error = false;
        checkAtResult = false;
        foundPerOp2 = false;
    }

    /*
        Custom method for putting a double at SharedPreferences.
    */
    public static void putDouble(final SharedPreferences.Editor editor, final String key, final double value) {
        editor.putLong(key, Double.doubleToRawLongBits(value)).apply();
    }
    
    /*
        Custom method for getting a double from SharedPreferences.
    */
    public static double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    /*
        Describes the actions that should be done if the M+ memory button get clicked.
        Finds the last char at our calculation depending on what calculation has been typed.
    */
    public void memoryAdd(View view) {
        if(!error)
        {
            double number;
            double numberFromMem;
            String opsText = ops.getText().toString();
            String lastChar = "";
            
            /*
                Finds last char
            */
            if (ops.length() > 0) {
                lastChar = opsText.substring(opsText.length() - 1);
            }
            
            /*
                If there is no ongoing calculation, then we have nothing to save.
            */
            if(opsText.length() < 1)
            {
                return;
            }

            /*
                If last char is square root or result, memory adding can not be processed.
                If last char is an operation such as +,- etc. then we find the number before that operation.
                Else if last char is a number, we find that full number.
                We parse the numbers as double and save them like these.
            */
            if (lastChar.equals(getString(R.string.sqrt))  ||
                    lastChar.equals(getString(R.string.result))) {
                return;
            } else if(lastChar.equals(getString(R.string.add)) ||
                    lastChar.equals(getString(R.string.subtract)) ||
                    lastChar.equals(getString(R.string.multiply)) ||
                    lastChar.equals(getString(R.string.divide)))
            {
                fixTextsDotAndComma(opsText);
                opsText = ops.getText().toString();
                number = Double.parseDouble(opsText.substring(0, opsText.length()-1));
            } else
            {
                if(opsText.contains(getString(R.string.add)) ||
                        opsText.contains(getString(R.string.subtract)) ||
                        opsText.contains(getString(R.string.multiply)) ||
                        opsText.contains(getString(R.string.divide)))
                {
                    int i;
                    for (i = opsText.length() - 1; i >= 0; i--) {
                        if (opsText.charAt(i) == getString(R.string.add).charAt(0) ||
                                opsText.charAt(i) == getString(R.string.subtract).charAt(0) ||
                                opsText.charAt(i) == getString(R.string.multiply).charAt(0) ||
                                opsText.charAt(i) == getString(R.string.divide).charAt(0)) {
                            break;
                        }
                        if(opsText.charAt(i) == getString(R.string.percentage).charAt(0) ||
                                opsText.charAt(i) == getString(R.string.sqrt).charAt(0))
                        {
                            return;
                        }
                    }
                    fixTextsDotAndComma(opsText);
                    number = Double.parseDouble(opsText.substring(i+1));
                } else
                {
                    if(opsText.contains(getString(R.string.percentage)) ||
                            opsText.contains(getString(R.string.sqrt)))
                    {
                        return;
                    }
                    number = Double.parseDouble(opsText);
                }

            }

            /*
                If memory is empty just add it. Else, retrieve the saved number, 
                add in that the number we currently have and save that result back in the memory.
            */
            if (!sharedPref.contains(getString(R.string.memory))) {
                putDouble(editor,getString(R.string.memory),number);
            } else {
                numberFromMem = getDouble(sharedPref,getString(R.string.memory),0);
                numberFromMem = numberFromMem + number;
                putDouble(editor,getString(R.string.memory),numberFromMem);
            }
        }
    }

    /*
        Describes the actions that should be done if the M- memory button get clicked.
    */
    public void memorySub(View view) {
        if(!error)
        {
            double number;
            double numberFromMem;
            String opsText = ops.getText().toString();
            String lastChar = "";
            
            /*
                Finds last char
            */
            if (ops.length() > 0) {
                lastChar = opsText.substring(opsText.length() - 1);
            }
            
            /*
                If there is no ongoing calculation, then we have nothing to save.
            */
            if(opsText.length() < 1)
            {
                return;
            }

            /*
                If last char is square root or result, memory adding can not be proccessed.
                If last char is an operation such as +,- etc. then we find the number before that operation.
                Else if last char is a number, we find that full number.
                We parse the numbers as double and save them like these.
            */
            if (lastChar.equals(getString(R.string.sqrt))  ||
                    lastChar.equals(getString(R.string.result))) {
                return;
            } else if(lastChar.equals(getString(R.string.add)) ||
                    lastChar.equals(getString(R.string.subtract)) ||
                    lastChar.equals(getString(R.string.multiply)) ||
                    lastChar.equals(getString(R.string.divide)))
            {
                fixTextsDotAndComma(opsText);
                opsText = ops.getText().toString();
                number = Double.parseDouble(opsText.substring(0, opsText.length()-1));
            } else
            {
                if(opsText.contains(getString(R.string.add)) ||
                        opsText.contains(getString(R.string.subtract)) ||
                        opsText.contains(getString(R.string.multiply)) ||
                        opsText.contains(getString(R.string.divide)))
                {
                    int i;
                    for (i = opsText.length() - 1; i >= 0; i--) {
                        if (opsText.charAt(i) == getString(R.string.add).charAt(0) ||
                                opsText.charAt(i) == getString(R.string.subtract).charAt(0) ||
                                opsText.charAt(i) == getString(R.string.multiply).charAt(0) ||
                                opsText.charAt(i) == getString(R.string.divide).charAt(0)) {
                            break;
                        }
                        if(opsText.charAt(i) == getString(R.string.percentage).charAt(0) ||
                                opsText.charAt(i) == getString(R.string.sqrt).charAt(0))
                        {
                            return;
                        }
                    }
                    number = Double.parseDouble(opsText.substring(i+1));
                } else
                {
                    if(opsText.contains(getString(R.string.percentage)) ||
                            opsText.contains(getString(R.string.sqrt)))
                    {
                        return;
                    }
                    number = Double.parseDouble(opsText);
                }

            }

            /*
                If memory is empty just put the -number there (as we do 0-number). Else, retrieve the saved number, 
                subtract from that the number we currently have and save that result back in the memory.
            */
            if (!sharedPref.contains(getString(R.string.memory))) {
                putDouble(editor,getString(R.string.memory),-number);
            } else {
                numberFromMem = getDouble(sharedPref,getString(R.string.memory),0);
                numberFromMem = numberFromMem - number;
                putDouble(editor,getString(R.string.memory),numberFromMem);
            }
        }
    }

    /*
        Describes the actions that should be done if the MR memory button get clicked.
        Reads from the memory and shows that saved number.
    */
    public void memoryRead(View view) {
        if(!error)
        {
            double number;
            vibrate();
            
            /*
                If there is a saved number
            */
            if (sharedPref.contains(getString(R.string.memory))) {
                number = getDouble(sharedPref,getString(R.string.memory),0);
            } else
            {
                return;
            }
            
            if (number !=0)
            {
                String opsText = ops.getText().toString();
                String lastChar = "";
                
                /*
                    Finds the last char from current operation TextView 
                */
                if (ops.length() > 0) {
                    lastChar = opsText.substring(opsText.length() - 1);
                }
                
                /*
                    Looking what is that last char we found.
                    If it is a "=" then nothing should happen.
                    If it is an operation (such as +,- etc) then add in current operation's TextView the number
                    saved at the memory.
                    If it is something different (such as a number or just empty-nothing), then replace that number (if there is one)
                    with the number that is saved at the memory. If we have no lastChar (so our TextView) is empty just add that saved number there and show it.
                */
                if (lastChar.equals(getString(R.string.result))) {
                    return;
                } else if(lastChar.equals(getString(R.string.sqrt))  ||
                        lastChar.equals(getString(R.string.add)) ||
                        lastChar.equals(getString(R.string.subtract)) ||
                        lastChar.equals(getString(R.string.multiply)) ||
                        lastChar.equals(getString(R.string.divide)))
                {
                    operations = opsText + Double.toString(number);
                    operationsFull = opsFull.getText().toString() + Double.toString(number);
                    ops.setText(operations);
                    opsFull.setText(operationsFull);
                } else
                {
                    /* 
                        If there is a number on the screen
                    */
                    if(!lastChar.equals(""))
                    {
                        int i;
                        
                        /*
                            Fix the current calculation's TextView.
                        */
                        for (i = opsText.length() - 1; i >= 0; i--) {
                            if (opsText.charAt(i) == getString(R.string.add).charAt(0) ||
                                    opsText.charAt(i) == getString(R.string.subtract).charAt(0) ||
                                    opsText.charAt(i) == getString(R.string.multiply).charAt(0) ||
                                    opsText.charAt(i) == getString(R.string.divide).charAt(0)) {
                                break;
                            }
                        }
                        opsText = opsText.substring(0, i+1);
                        operations = opsText + Double.toString(number);
                        ops.setText(operations);
                        
                        /*
                            Fix TextView that contains all calculations that have been done till now.
                        */
                        for(i = opsFull.getText().toString().length() - 1; i>= 0; i--)
                        {
                            if (opsFull.getText().toString().charAt(i) == getString(R.string.add).charAt(0) ||
                                    opsFull.getText().toString().charAt(i) == getString(R.string.subtract).charAt(0) ||
                                    opsFull.getText().toString().charAt(i) == getString(R.string.multiply).charAt(0) ||
                                    opsFull.getText().toString().charAt(i) == getString(R.string.divide).charAt(0)) {
                                break;
                            }
                        }
                        opsText = opsFull.getText().toString();
                        opsText = opsText.substring(0,i+1);

                        operationsFull = opsText + Double.toString(number);
                        opsFull.setText(operationsFull);
                    } 
                    else /* If screen is EMPTY just add this saved number */ 
                    {
                        operations = opsText + Double.toString(number);
                        operationsFull = opsFull.getText().toString() + Double.toString(number);
                        ops.setText(operations);
                        opsFull.setText(operationsFull);
                    }
                }
            }
        }
    }

    /*
        Describes the actions that should be done if the MC memory button get clicked.
        Clears the memory.
    */
    public void memoryClear(View view) {
            vibrate();
            if (sharedPref.contains(getString(R.string.memory))) {
                editor.remove(getString(R.string.memory));
                editor.apply();
            }
    }

    /*
        Describes the actions that should be done when some buttons (who don't have a different onClick method) get clicked.
        First we find the last char at the calculation (if there is one).
        If inside the calculation the result button "=" has not been clicked, then just call another method to figure out the next steps.
        Else if inside the calculation has been a result "=" button and the user has just clicked something else,
        -which that means that "=" is the last char/symbol at the calculation-, then the calculation process restores at starting point,
        the last char ("=") gets deleted, we find again the last char (the char before the result button) and then as above another method gets
        called to figure out the next steps.
    */
    public void onClick(View view) {
        vibrate();

        if (!error) {
            Button but = (Button) view;
            String buttonText = but.getText().toString();
            String opsText = ops.getText().toString();
            String opsTextFull = opsFull.getText().toString();

            String lastChar = "";
            if (ops.length() > 0) {
                lastChar = opsText.substring(opsText.length() - 1);
            }

            if (!opsText.contains(getString(R.string.result))) {
                helperForOnClickChooseAction(buttonText, opsText, lastChar, opsTextFull);
            } else if(!buttonText.equals(getString(R.string.result))) {
                starting = true;
                fixTextsDotAndComma(opsText);
                whenToLookForOp2 = 0;
                deleteLastChar();
                opsText = ops.getText().toString();
                opsTextFull = opsFull.getText().toString();
                lastChar = "";
                if (ops.length() > 0) {
                    lastChar = opsText.substring(opsText.length() - 1);
                }
                helperForOnClickChooseAction(buttonText, opsText, lastChar, opsTextFull);
            }
        }
    }

    /*
        Helper function that calls after onClick function in order to figure out the next steps after a button gets clicked.
        If last char is "-" meaning that number will be negative, and the button that gets clicked is an operation (such as +,- etc)
        then nothing happens (for example, we cannot do something like this 5*-+).
        Otherwise based on the operation button that gets clicked, the appropriate function gets called for next steps.
        If the result button gets clicked then based on some conditions we make some actions, and we save the result and operations at history database.
        Otherwise if the button is a number, we just check the last char so it is not a % or = and then we print that number.
    */    
    public void helperForOnClickChooseAction(String buttonText, String opsText, String lastChar, String opsTextFull) {
        if(lastChar.equals("-") && (
                buttonText.equals(getString(R.string.percentage)) || buttonText.equals(getString(R.string.comma)) ||
                buttonText.equals(getString(R.string.posNeg)) || buttonText.equals(getString(R.string.add)) ||
                buttonText.equals(getString(R.string.divide)) || buttonText.equals(getString(R.string.subtract)) ||
                buttonText.equals(getString(R.string.multiply)) || buttonText.equals(getString(R.string.sqrt)) ||
                buttonText.equals(getString(R.string.x2)) || buttonText.equals(getString(R.string.result))))
        {
            return;
        }

        if (buttonText.equals(getString(R.string.percentage))) {
            percentageTextChanger(opsText, lastChar, opsTextFull);
        } else if (buttonText.equals(getString(R.string.comma))) {
            commaTextChanger(opsText, lastChar);
        } else if (buttonText.equals(getString(R.string.posNeg))) {
            posNegTextChanger(opsText, lastChar, opsTextFull);
        } else if (buttonText.equals(getString(R.string.add))) {
            addTextChanger(opsText, lastChar, buttonText);
        } else if (buttonText.equals(getString(R.string.divide))) {
            divTextChanger(opsText, lastChar, buttonText);
        } else if (buttonText.equals(getString(R.string.subtract))) {
            subTextChanger(opsText, lastChar, buttonText);
        } else if (buttonText.equals(getString(R.string.multiply))) {
            mulTextChanger(opsText, lastChar, buttonText);
        } else if (buttonText.equals(getString(R.string.sqrt))) {
            sqrtTextChanger(opsText, lastChar, opsTextFull);
        } else if (buttonText.equals(getString(R.string.x2))) {
            x2TextChanger(opsText, lastChar, opsTextFull);
        } else if (buttonText.equals(getString(R.string.result)) && opsText.length() > 0) {     /* For result button */
            if(!starting) {                                                                     /* If calculation is not in starting position */
                if (checkAtResult) {                          /* If needed to make some additional actions to find the result before printing it */
                    resultFix_NotOpBefore(opsText);           /* call the appropriate function */
                } else {                                      /* otherwise we have the result already so just */
                    printTextsRight(result, res);             /* print it */
                    deleteLastChar();                         /* and delete the last char so operations final string is not something like 5+5+ */
                    operations = ops.getText().toString() + buttonText; /* but 5+5= */
                    ops.setText(operations);
                }
            } else                                                                              /* Calculation is in starting position */
            {
                if(opsText.contains(getString(R.string.sqrt)) && opsText.length()>1)            /* If it is something like sqrt(4) just print the result in the right format */
                {
                    res = Math.sqrt(Double.parseDouble(opsText.substring(1)));
                    printTextsRight(result, res);
                    operations = ops.getText().toString() + buttonText;
                    ops.setText(operations);
                } else                                                                          /* If it is something like 4 just print in result "4="*/
                {
                    operations = ops.getText().toString() + buttonText;
                    ops.setText(operations);
                    result.setText(ops.getText().toString());
                }
            }
            String toBeSaved = opsFull.getText().toString() + " = " + result.getText().toString();      /* We get the full string of operations and result */
            operationsFull = opsFull.getText().toString() + buttonText;                                 /* put a "=" at the end of the string with all operations */
            opsFull.setText(operationsFull);
            saveHistory(toBeSaved);                                                                     /* And save it at our history database */
        }
        else {                                      /* If button is a number just make a simple check and print it */
                if (!lastChar.equals(getString(R.string.percentage)) && !buttonText.equals(getString(R.string.result))) {
                    checkAtResult = true;
                    operations = opsText + buttonText;
                    operationsFull = opsTextFull + buttonText;
                    ops.setText(operations);
                    opsFull.setText(operationsFull);
                }
        }
    }

    /*  
        Function responsible for handling actions when DEL button gets clicked.
        If in either of operations TextView there is not a result char ("="), then
        do some actions and change some variables in order for calculation to proceed correctly,
        and then delete the last char.
        Otherwise, if there is a result char ("="), change some variables so the calculation process can 
        continue, and delete last char (actually the last char will be the "=")
    */
    public void onClickDel(View view) {
        vibrate();
        if (!error) {
            String opsText = ops.getText().toString();
            if (!opsText.contains(getString(R.string.result))) {
                if (ops.length() > 0) {
                    deletion(opsText);
                    opsText = ops.getText().toString();
                    ops.setText(opsText.substring(0, opsText.length() - 1));
                }
            }

            String opsTextFull = opsFull.getText().toString();
            if (!opsTextFull.contains(getString(R.string.result))) {
                if (opsFull.length() > 0) {
                    opsFull.setText(opsTextFull.substring(0, opsTextFull.length() - 1));
                }
            }

            if(opsText.contains(getString(R.string.result)))
            {
                starting = true;
                fixTextsDotAndComma(opsText);
                whenToLookForOp2 = 0;
                deleteLastChar();
            }
        }
    }

    /* 
        Deletes last char at current calculation string and at the full calculation string 
    */
    private void deleteLastChar() {
        ops.setText(ops.getText().toString().substring(0, ops.getText().toString().length() - 1));
        opsFull.setText(opsFull.getText().toString().substring(0, opsFull.getText().toString().length() - 1));
    }

    /* 
        If last char at current calculation is a symbol/operation such as +,- etc then change some variables and
        fix the format of the text/numbers to be in correct form so the calculation process can continue with no mistake
    */
    private void deletion(String opsText) {
        if (opsText.charAt(opsText.length()-1) == getString(R.string.add).charAt(0) ||
                opsText.charAt(opsText.length() - 1) == getString(R.string.subtract).charAt(0) ||
                opsText.charAt(opsText.length() - 1) == getString(R.string.multiply).charAt(0) ||
                opsText.charAt(opsText.length() - 1) == getString(R.string.divide).charAt(0)) {
            whenToLookForOp2 = 0;
            starting = true;
            fixTextsDotAndComma(opsText);
        }
    }

    /*
        First we have a counter to count commas in current number as we cannot have a number with something
        like this "5,,1".
        Then we find the last char at current calculation, if it is a symbol such as +,- etc then nothing happens
        (we cannot have something like 5+,), same happens if screen is empty.
        Then we count the commas at current number going backwards, and we stop either at a comma or at a symbol.
        If we find no other commas, just add the one that has been clicked.
    */
    private void commaTextChanger(String opsText, String lastChar) {
        int commaNum = 0;
        boolean startedWithComma = false;

        int length = opsText.length() - 1;
        if (length >= 0) {
            if (lastChar.equals(getString(R.string.add)) ||
                    lastChar.equals(getString(R.string.subtract)) ||
                    lastChar.equals(getString(R.string.multiply)) ||
                    lastChar.equals(getString(R.string.divide)) ||
                    lastChar.equals(getString(R.string.percentage)) ||
                    lastChar.equals(getString(R.string.sqrt))) {
                return;
            }
        } else {
            startedWithComma = true;
        }

        if(startedWithComma) {
            operations = getString(R.string.zero) + getString(R.string.comma);
            operationsFull = getString(R.string.zero) + getString(R.string.comma);
            ops.setText(operations);
            opsFull.setText(operationsFull);
        } else {
            for (int i = opsText.length() - 1; i >= 0; i--) {
                if (opsText.charAt(i) == getString(R.string.comma).charAt(0)) {
                    commaNum++;
                    break;
                }
                if (opsText.charAt(i) == getString(R.string.add).charAt(0) ||
                        opsText.charAt(i) == getString(R.string.subtract).charAt(0) ||
                        opsText.charAt(i) == getString(R.string.multiply).charAt(0) ||
                        opsText.charAt(i) == getString(R.string.divide).charAt(0)) {
                    break;
                }
            }

            if (commaNum == 0) {
                operations = opsText + getString(R.string.comma);
                operationsFull = opsFull.getText().toString() + getString(R.string.comma);
                ops.setText(operations);
                opsFull.setText(operationsFull);
            }
        }
    }

    /*
        First we find the last char at current calculation, if it is a symbol such as +,- etc then nothing happens
        (we cannot have something like 5+,), same happens if screen is empty.
        If last char is a number, just add the percentage symbol ("%").
        If last char is a dot/comma (such as , or . depending the display format), then delete that dot/comma and then add the percentage
        symbol.
        Otherwise nothing happens (for example if last symbol is a +, we cannot have something like 5+%)
    */
    private void percentageTextChanger(String opsText, String lastChar, String opsTextFull) {
        for (int i = opsText.length() - 1; i >= 0; i--) {
            if (opsText.charAt(i) == getString(R.string.add).charAt(0) ||
                    opsText.charAt(i) == getString(R.string.subtract).charAt(0) ||
                    opsText.charAt(i) == getString(R.string.multiply).charAt(0) ||
                    opsText.charAt(i) == getString(R.string.divide).charAt(0)) {
                break;
            }
            if (opsText.charAt(i) == getString(R.string.sqrt).charAt(0)) {
                return;
            }
        }
        switch (lastChar) {
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
                operations = opsText + getString(R.string.percentage);
                operationsFull = opsTextFull + getString(R.string.percentage);
                ops.setText(operations);
                opsFull.setText(operationsFull);
                break;
            case ".":
                deleteLastChar();
                operations = ops.getText().toString() + getString(R.string.percentage);
                operationsFull = opsFull.getText().toString() + getString(R.string.percentage);
                ops.setText(operations);
                opsFull.setText(operationsFull);
                break;
            default:
                break;
        }
    }

    /*
        Function that does the actions when the button responsible for opposite number gets clicked.
        First we find the last char at current calculation, if it is a symbol such as +,- etc then nothing happens
        (we cannot have something like 5+,), same happens if screen is empty.
        Then we start checking the last number we have backwards, if we find at the last position a comma/dot, we delete it.
        If we find a sqrt, then nothing happens, we return.
        If we find a symbol/operation such as +,- etc we break there and continue for next actions.
        Samme happens at the TextView that contains all the calculations till now.
        Then we find the number (based on where we stopped before at the break), change it to opposite and if it is an integer,
        print it as an integer otherwise as a double.
        Also we make some changes at view, because if it is a large number or a double, we can handle it correctly 
        only if decimal seperator is a dot and not a comma. Otherwise errors arise.
    */
    private void posNegTextChanger(String opsText, String lastChar, String opsTextFull) {
        double numberChanged;
        int i, j;

        int length = opsText.length();
        if (length > 0) {
            if (lastChar.equals(getString(R.string.add)) ||
                    lastChar.equals(getString(R.string.subtract)) ||
                    lastChar.equals(getString(R.string.multiply)) ||
                    lastChar.equals(getString(R.string.divide)) ||
                    lastChar.equals(getString(R.string.percentage)) ||
                    lastChar.equals(getString(R.string.sqrt))) {
                return;
            }
        } else {
            return;
        }

        for (i = opsText.length() - 1; i >= 0; i--) {
            if (opsText.charAt(opsText.length() - 1) == getString(R.string.comma).charAt(0)) {
                deleteLastChar();
                opsText = ops.getText().toString();
                opsTextFull = opsFull.getText().toString();
                continue;
            }
            if (opsText.charAt(i) == getString(R.string.add).charAt(0) ||
                    opsText.charAt(i) == getString(R.string.subtract).charAt(0) ||
                    opsText.charAt(i) == getString(R.string.multiply).charAt(0) ||
                    opsText.charAt(i) == getString(R.string.divide).charAt(0)) {
                break;
            }
            if (opsText.charAt(i) == getString(R.string.sqrt).charAt(0))
            {
                return;
            }
        }

        for (j = opsTextFull.length() - 1; j >= 0; j--) {
            if (opsTextFull.charAt(opsTextFull.length() - 1) == getString(R.string.comma).charAt(0)) {
                deleteLastChar();
            }
            if (opsTextFull.charAt(j) == getString(R.string.add).charAt(0) ||
                    opsTextFull.charAt(j) == getString(R.string.subtract).charAt(0) ||
                    opsTextFull.charAt(j) == getString(R.string.multiply).charAt(0) ||
                    opsTextFull.charAt(j) == getString(R.string.divide).charAt(0)) {
                break;
            }
        }

        numberChanged = Double.parseDouble(opsText.substring(i + 1));
        opsText = opsText.substring(0, i + 1);
        opsTextFull = opsTextFull.substring(0, j + 1);
        numberChanged = -numberChanged;
        if (numberChanged % 1 != 0) {
            operationsFull = opsTextFull + Double.toString(numberChanged);
            opsFull.setText(operationsFull);
        } else {
            operationsFull = opsTextFull + Integer.toString((int) numberChanged);
            opsFull.setText(operationsFull);
        }

        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        df.setMaximumFractionDigits(8);
        String dfFormat = df.format(numberChanged);
        dfFormat = dfFormat.replace(",", "");
        operations = opsText + dfFormat;
        ops.setText(operations);
    }
    
    /*
        First we check if screen is empty, if it is, we just add the sqrt symbol and return
        If last char is a symbol/operation (+,-,÷,*), just add the sqrt symbol
        Otherwise nothing happens (for example if last symbol is a number)
    */
    private void sqrtTextChanger(String opsText, String lastChar, String opsTextFull) {
        if(opsText.equals(getString(R.string.emptyString)))
        {
            operations = opsText + getString(R.string.sqrt);
            operationsFull = opsTextFull + getString(R.string.sqrt);
            ops.setText(operations);
            opsFull.setText(operationsFull);
            return;
        }
        switch (lastChar) {
            case "+":
            case "−":
            case "÷":
            case "×":
                operations = opsText + getString(R.string.sqrt);
                operationsFull = opsTextFull + getString(R.string.sqrt);
                ops.setText(operations);
                opsFull.setText(operationsFull);
                break;
            default:
                break;
        }
    }

    /*
        Function that does the actions when the button responsible for x^2 (with x being user's number) gets clicked.
        First we find the last char at current calculation, if it is a symbol such as +,- etc then nothing happens
        (we cannot have something like 5+,), same happens if screen is empty.
        We move on only if it is a number or a dot/comma.
        If last char is a comma/dot, we delete it.
        Then we start checking the last number we have backwards
        If we find a sqrt, then nothing happens, we return.
        If we find a symbol/operation such as +,- etc we break there and continue for next actions.
        Samme happens at the TextView that contains all the calculations till now.
        Then we find the number (based on where we stopped before at the break), change it to x^2 (same as x*x) and if it is an integer,
        print it as an integer otherwise as a double.
        On TextView related with all calculations, print something like this 5^2 etc instead of 25 etc.
        Also we make some changes at view, because if it is a large number or a double, we can handle it correctly 
        only if decimal seperator is a dot and not a comma. Otherwise errors arise.
    */
    private void x2TextChanger(String opsText, String lastChar, String opsTextFull) {
        double numberChanged;
        int i,j;

        if(opsText.equals(getString(R.string.emptyString))) {
            return;
        }
        if(lastChar.equals("1") || lastChar.equals("2") || lastChar.equals("3") ||
                lastChar.equals("4") || lastChar.equals("5") || lastChar.equals("6")
                || lastChar.equals("7") || lastChar.equals("8") || lastChar.equals("9")
                || lastChar.equals("0") || lastChar.equals("."))
        {
            if(lastChar.equals("."))
            {
                deleteLastChar();
                opsText = ops.getText().toString();
                opsTextFull = opsFull.getText().toString();
            }
            for (i = opsText.length() - 1; i >= 0; i--) {
                if (opsText.charAt(i) == getString(R.string.add).charAt(0) ||
                        opsText.charAt(i) == getString(R.string.subtract).charAt(0) ||
                        opsText.charAt(i) == getString(R.string.multiply).charAt(0) ||
                        opsText.charAt(i) == getString(R.string.divide).charAt(0)) {
                    break;
                }
                if (opsText.charAt(i) == getString(R.string.sqrt).charAt(0))
                {
                    return;
                }
            }

            for (j = opsTextFull.length() - 1; j >= 0; j--) {
                if (opsTextFull.charAt(opsTextFull.length() - 1) == getString(R.string.comma).charAt(0)) {
                    deleteLastChar();
                }
                if (opsTextFull.charAt(j) == getString(R.string.add).charAt(0) ||
                        opsTextFull.charAt(j) == getString(R.string.subtract).charAt(0) ||
                        opsTextFull.charAt(j) == getString(R.string.multiply).charAt(0) ||
                        opsTextFull.charAt(j) == getString(R.string.divide).charAt(0)) {
                    break;
                }
            }

            numberChanged = Double.parseDouble(opsText.substring(i + 1));
            opsText = opsText.substring(0, i + 1);
            opsTextFull = opsTextFull.substring(0, j + 1);
            if (numberChanged % 1 != 0) {
                operationsFull = opsTextFull + Double.toString(numberChanged) + "^2";
                opsFull.setText(operationsFull);
            } else {
                operationsFull = opsTextFull + Integer.toString((int)numberChanged) + "^2";
                opsFull.setText(operationsFull);
            }
            numberChanged = numberChanged*numberChanged;

            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.US);
            DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
            symbols.setGroupingSeparator(',');
            symbols.setDecimalSeparator('.');
            df.setDecimalFormatSymbols(symbols);
            df.setMaximumFractionDigits(8);
            String dfFormat = df.format(numberChanged);
            dfFormat = dfFormat.replace(",", "");

            operations = opsText + dfFormat;
            ops.setText(operations);
        }
    }

    /*
        Function related with actions when the add "+" button gets clicked.
        If screen is empty, then returns and does nothing because we cannot start a calculation with the + symbol.
        If we are at a starting point at our calculation, then call the appropriate function and return, otherwise,
        call a more generic function that handles actions when symbol buttons get clicked and we are not starting a calculation.
    */
    private void addTextChanger(String opsText, String lastChar, String buttonText) {
        if ((opsText.length()) < 1) {
            return;
        }

        if (starting)
        {
            startingTextChanger(opsText, lastChar, buttonText);
            return;
        }

        /*
            Call of generic function
        */
        addOpsTexts(lastChar, buttonText, getString(R.string.subtract),getString(R.string.multiply),
                getString(R.string.divide), getString(R.string.add));
    }

    /*
        Function related with actions when the subtract "-" button gets clicked.
        If screen is empty, then returns and does nothing because we cannot start a calculation with the - symbol.
        If we are at a starting point at our calculation, then call the appropriate function and return, otherwise,
        call a more generic function that handles actions when symbol buttons get clicked and we are not starting a calculation.
    */
    private void subTextChanger(String opsText, String lastChar, String buttonText) {
        if ((opsText.length()) < 1) {
            return;
        }

        if (starting)
        {
            startingTextChanger(opsText, lastChar, buttonText);
            return;
        }

        /*
            Call of generic function
        */
        addOpsTexts(lastChar, buttonText, getString(R.string.add),getString(R.string.multiply),
                getString(R.string.divide), getString(R.string.subtract));
    }

    /*
        Function related with actions when the add "*" button gets clicked.
        If screen is empty, then returns and does nothing because we cannot start a calculation with the * symbol.
        If we are at a starting point at our calculation, then call the appropriate function and return, otherwise,
        call a more generic function that handles actions when symbol buttons get clicked and we are not starting a calculation.
    */
    private void mulTextChanger(String opsText, String lastChar, String buttonText) {
        if ((opsText.length()) < 1) {
            return;
        }

        if (starting)
        {
            startingTextChanger(opsText, lastChar, buttonText);
            return;
        }

        /*
            Call of generic function
        */
        addOpsTexts(lastChar, buttonText, getString(R.string.add),getString(R.string.subtract),
                getString(R.string.divide), getString(R.string.multiply));
    }

    /*
        Function related with actions when the divide "÷" button gets clicked.
        If screen is empty, then returns and does nothing because we cannot start a calculation with the ÷ symbol.
        If we are at a starting point at our calculation, then call the appropriate function and return, otherwise,
        call a more generic function that handles actions when symbol buttons get clicked and we are not starting a calculation.
    */
    private void divTextChanger(String opsText, String lastChar, String buttonText) {
        if ((opsText.length()) < 1) {
            return;
        }

        if (starting)
        {
            startingTextChanger(opsText, lastChar, buttonText);
            return;
        }

        /*
            Call of generic function
        */
        addOpsTexts(lastChar, buttonText, getString(R.string.add),getString(R.string.subtract),
                getString(R.string.multiply), getString(R.string.divide));
    }

    /*
        Generic function that handles actions when symbol (+,-,÷,*) buttons get clicked and we ARE NOT starting a calculation.
        In arguments: firstEqual,secondEqual,thirdEqual,elseIfEqual are the (+,-,÷,*) symbols each time based on which function calls this function,
        the "who is who" of the arguments-symbol change. These arguments are used to figure out at each time, if the last char is one of these symbols
        and if so if we should change that last char with the new one that got clicked or not.
        This process happens at the start of the function. If last char needs to be replaced, some variables change also in order for the calculation
        process to continue with no errors and then we print the new symbol and return. An exlusion is made if last char was a comma, at this condition,
        we follow the above process but also we find the result from based on the number before the comma and anything other there is by before.
        If last char/symbol is the same one as this symbol that got clicked, then nothing happens.
        If last char is a sqrt, that means we need a number after it and not a symbol, so we decrement a counter we use that we have incremented at the
        start, and return.
        In all other cases (for example when last char is a number), we print the symbol and move forward to calculation process in order to find the second
        operator and get a result.
    */
    private void addOpsTexts(String lastChar, String buttonText, String firstEqual,
                             String secondEqual, String thirdEqual, String elseIfEqual) {
        whenToLookForOp2++;
        if (lastChar.equals(firstEqual) || lastChar.equals(secondEqual) ||
                lastChar.equals(thirdEqual) || lastChar.equals(getString(R.string.comma))) {
            deleteLastChar();
            whenToLookForOp2 = 1;

            operations = ops.getText().toString() + buttonText;
            operationsFull = opsFull.getText().toString() + buttonText;
            ops.setText(operations);
            opsFull.setText(operationsFull);
            if (lastChar.equals(getString(R.string.comma))) {
                if (starting) {
                    whenToLookForOp2 = 1;
                } else {
                    whenToLookForOp2 = 2;
                }
                findOp2(ops.getText().toString());
            }
        } else if (lastChar.equals(elseIfEqual)) {
            return;
        } else if(lastChar.equals(getString(R.string.sqrt))) {
            whenToLookForOp2--;
        } else
        {
            operations = ops.getText().toString() + buttonText;
            operationsFull = opsFull.getText().toString() + buttonText;
            ops.setText(operations);
            opsFull.setText(operationsFull);
            findOp2(ops.getText().toString());
        }
    }

    /*
        Generic function that handles actions when symbol (+,-,÷,*) buttons get clicked and we ARE starting a calculation.
        If last char is a - (meaning negative number and NOT subtraction) or a sqrt, then return and nothing happens.
        If last char/symbol a percentage "%", then convert that percent in to Double type and store it as the first operator.
        If last char is a sqrt, then convert that sqrt to Double type and store it as the first operator.
        
        In other cases, just get that first number as Double type and store it as the first operator. If last was a comma delete it. 
        Also if that number was something like 2^2, in the TextView related with all calculations,
        print it in that format.
        
        At the end of all cases (except the first one that returns), print the result, current calculation 
        and full calculation (containing all calculations till now) in the right format and change some variables for moving one
        (1 of those variables is "starting" variable setted to false meaning that we are no longer at the start of a calculation)
    */
    private void startingTextChanger(String opsText, String lastChar, String buttonText) {
        if(lastChar.equals("-") || lastChar.equals(getString(R.string.sqrt)))
        {
            return;
        }
        if (opsText.contains(getString(R.string.percentage))) {
            op1 = Double.parseDouble(opsText.substring(0, opsText.length() - 1)) / 100;
        } else if(opsText.contains(getString(R.string.sqrt))) {
            op1 = Math.sqrt(Double.parseDouble(opsText.substring(1)));
        } else {
            if (lastChar.equals(getString(R.string.comma)))
            {
                ops.setText(opsText.substring(0, opsText.length() - 1));
            }
            op1 = Double.parseDouble(opsText);
            if(!opsFull.getText().toString().contains("^"))
            {
                printTextsRight(opsFull, op1);
            }
        }
        printTextsRight(result, op1);
        printTextsRight(ops, op1);

        operations = ops.getText().toString() + buttonText;
        operationsFull = opsFull.getText().toString() + buttonText;
        ops.setText(operations);
        opsFull.setText(operationsFull);
        starting = false;
        whenToLookForOp2++;
    }

    /*
        Function that finds second operator and helps with calculation process.
        
        If counter "whenToLookForOp2" is >1, that means there IS a second operator and we should look for him.
        Otherwise there IS NOT a second operator and we should not look for him and we set him zero.
        
        For the first case, we are looking backwards from the end of the current calculation string,
        for either an operation symbol (such as +,- etc), either a sqrt symbol either a percentage symbol.
        Whichever we find first, we break and we hold that position. Based on which we found first 
        (if a sqrt or a percentage exists then we will find it before an operation symbol, so finding an operation symbol means we
        don't have either of the other two), we make some actions to find that second operator.
        
        After that we have found that second operator, we call the calculate method to do the calculation between the 2 operators.
    */
    private void findOp2(String opsText) {
        int perPos = -2;                /* the position of a percentage symbol (if exists) */
        int sqrtPos = -2;               /* the position of a sqrt symbol (if exists) */
        if (whenToLookForOp2 > 1) {
            int i;
            for (i = opsText.length() - 2; i >= 0; i--) {
                if (opsText.charAt(i) == getString(R.string.add).charAt(0) ||
                        opsText.charAt(i) == getString(R.string.subtract).charAt(0) ||
                        opsText.charAt(i) == getString(R.string.multiply).charAt(0) ||
                        opsText.charAt(i) == getString(R.string.divide).charAt(0)) {
                    break;
                }
                if (opsText.charAt(i) == getString(R.string.percentage).charAt(0)) {
                    foundPerOp2 = true;
                    perPos = i;
                    break;
                }
                if (opsText.charAt(i) == getString(R.string.sqrt).charAt(0)) {
                    foundSqrtOp2 = true;
                    sqrtPos = i;
                    break;
                }
            }

            if (i != sqrtPos) {
                foundSqrtOp2 = false;
            } else
            {
                op2 = Math.sqrt(Double.parseDouble(opsText.substring(i + 1, opsText.length() - 1)));
                i--;
            }

            if (i != perPos) {
                foundPerOp2 = false;
            } else {
                i = findPercentage(perPos, opsText, 1);
            }
            checkAtResult = false;
            if (!foundPerOp2 && !foundSqrtOp2) {
                op2 = Double.parseDouble(opsText.substring(i + 1, opsText.length() - 1));
            }
            calculate(opsText.substring(i, i + 1), opsText.substring(opsText.length() - 1));
            whenToLookForOp2 = 1;
        } else {
            op2 = 0;
        }
    }

    /*
        Function that takes part in calculation process with checking some conditions, call other functions and 
        change some variables.
        ARGUMENTS:
        operation: the calculation/operation that should be DONE between the 2 operators.
        operationNext: the calculation/operation that should be printed AFTER the current calculation/operation prints its result.
      
        If second operator IS NOT zero, then just pass call 2 other functions, the first one for doing the calculation and second one
        for printing the result in the right format, and change operator one as to result and operator to as zero.
        
        If second operator IS zero, then check if the operation that should be done is a division or a multiply.
        In first case, that is an error as we cannot divide with zero, inform the user and return (5/0=ERROR).
        In second case, multiplying with zero, gets us zero so just set the result as zero (5*0=0).
        In all other cases addition/subtraction just result is the same as the first operator (5+0=5).
        
        At the end, we print at the current operation's TextView in the right format, we print the next operation
        that got clicked and we set starting variable to false.
    */
    private void calculate(String operation, String operationNext) {
        if (op2 != 0) {
            switchForCalculateMethod(operation);
            printTextsRight(result, res);
            op1 = res;
            op2 = 0D;
        } else {
            if (operation.equals(getString(R.string.divide))) {
                result.setText(getString(R.string.divisionZero));
                error = true;
                return;

            } else if (operation.equals(getString(R.string.multiply))) {
                res = 0D;
                op1 = res;
            } else {
                res = op1;
            }
            printTextsRight(result, res);
        }
        printTextsRight(ops, res);

        operations = ops.getText().toString() + operationNext;
        ops.setText(operations);
        starting = false;
    }

    /* 
        Function responsible for doing the right calculation between the 2 operators, such as adding them, subtracting them etc.
    */
    private void switchForCalculateMethod(String operation) {
        switch (operation) {
            case "+":
                res = op1 + op2;
                break;
            case "−":
                res = op1 - op2;
                break;
            case "÷":
                res = op1 / op2;
                break;
            case "×":
                res = op1 * op2;
                break;
            default:
                break;
        }
    }

    /*
        Function related with finding the percentage of a number.
        ARGUMENTS: 
        perPos is the position of the percentage symbol, 
        i is a counter, 
        opsText is the current calculation string,
        fix is an argument needed to fix the length of the opsText substring in order to find the number/percent.
        
        First we find the position of an operation symbol, going backwards from the percentage position and we stop at that place.
        
        If that symbol is a multiply or divide symbol, then that percentage is not related with the first operator so just convert it to 
        Double type and store it as the second operator.
        
        Otherwise if that symbol is an add/subtract symbol, that means that the percentage we have IS related with the first operator and must
        be applied at it (for example 100+50%=150 but 100*50%=100*0.5=50) so we make the appropriate calculation. 
        If we are into a starting point then we shouldfirst find the first operator and then apply the percentage at it.
        We return i, which is the position of the operation symbol.
    */
    private int findPercentage(int perPos, String opsText, int fix) {
        int i;
        for (i = perPos; i >= 0; i--) {
            if (opsText.charAt(i) == getString(R.string.add).charAt(0) ||
                    opsText.charAt(i) == getString(R.string.subtract).charAt(0) ||
                    opsText.charAt(i) == getString(R.string.multiply).charAt(0) ||
                    opsText.charAt(i) == getString(R.string.divide).charAt(0)) {
                break;
            }
        }
        if (opsText.substring(i, i + 1).equals(getString(R.string.multiply)) ||
                opsText.substring(i, i + 1).equals(getString(R.string.divide))) {
            op2 = Double.parseDouble(opsText.substring(i + 1, opsText.length() - 1 - fix)) / 100;
        } else {
            if (starting) {
                op1 = Double.parseDouble(opsText.substring(0, i));
            }
            op2 = op1 * Double.parseDouble(opsText.substring(i + 1, opsText.length() - 1 - fix)) / 100;
        }
        return i;
    }

    /*
        We use this function if the result button gets clicked but we have not found the result yet, so with this function we find it.
       
        We are looking backwards from the end of the current calculation string, for either an operation symbol (such as +,- etc), 
        either a sqrt symbol either a percentage symbol. Whichever we find first, 
        we break and we hold that position. Based on which we found first (if a sqrt or a percentage exists then 
        we will find it before an operation symbol, so finding an operation symbol means we
        don't have either of the other two), we make some actions to find that second operator.
        
        After that we have found that second operator, we call the calculate method to do the calculation between the 2 operators.
    */
    private void resultFix_NotOpBefore(String opsText) {
        int perPos = -2;                /* the position of a percentage symbol (if exists) */
        int sqrtPos = -2;               /* the position of a sqrt symbol (if exists) */
        if (opsText.length() > 1) {         /* screen has at least one digit */
            int i;
            for (i = opsText.length() - 1; i >= 0; i--) {
                if (opsText.charAt(i) == getString(R.string.add).charAt(0) ||
                        opsText.charAt(i) == getString(R.string.subtract).charAt(0) ||
                        opsText.charAt(i) == getString(R.string.multiply).charAt(0) ||
                        opsText.charAt(i) == getString(R.string.divide).charAt(0)) {
                    break;
                }
                if (opsText.charAt(i) == getString(R.string.percentage).charAt(0)) {
                    foundPerOp2 = true;
                    perPos = i;
                    break;
                }

                if (opsText.charAt(i) == getString(R.string.sqrt).charAt(0)) {
                    foundSqrtOp2 = true;
                    sqrtPos = i;
                    break;
                }
            }

            if (i != sqrtPos) {
                foundSqrtOp2 = false;
            } else
            {
                op2 = Math.sqrt(Double.parseDouble(opsText.substring(i + 1)));
                i--;
            }

            if (i != perPos) {
                foundPerOp2 = false;
            } else {
                i = findPercentage(perPos, opsText, 0);
            }

            if (i < 0 && !foundPerOp2 && !foundSqrtOp2) {
                op2 = Double.parseDouble(opsText.substring(i + 1, opsText.length() - 1));
            } else if (i < opsText.length() - 1 && !foundPerOp2 && !foundSqrtOp2) {
                op2 = Double.parseDouble(opsText.substring(i + 1));
            }
            calculate(opsText.substring(i, i + 1), "=");
        }
    }

    /*
        Function responsible for printing the number mResult at id TextView at the right format based
        on what is the preffered display format for the user and based if the number is an integer or a double.
    */
    private void printTextsRight(TextView id, double mResult) {
        if (mResult % 1 != 0) {
            mResult = (double) Math.round(mResult * 10000d) / 10000d;
            DecimalFormat df = setDecimalFormat();
            id.setText(df.format(mResult));
        } else {
            DecimalFormat df = setDecimalFormat();
            id.setText(df.format(mResult));
        }
    }

    /*
        Based on what is the preffered display format for the user, change the decimal format of the number,
        its decimal and grouping seperators and return it
    */
    private DecimalFormat setDecimalFormat() {
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
        if(sharedPref.contains(getString(R.string.dotComma)))
        {
            symbols.setGroupingSeparator('.');
            symbols.setDecimalSeparator(',');
        } else if(sharedPref.contains(getString(R.string.commaDot)))
        {
            symbols.setGroupingSeparator(',');
            symbols.setDecimalSeparator('.');
        } else if(sharedPref.contains(getString(R.string.spaceComma)))
        {
            symbols.setGroupingSeparator(' ');
            symbols.setDecimalSeparator(',');
        } else if(sharedPref.contains(getString(R.string.spaceDot)))
        {
            symbols.setGroupingSeparator(' ');
            symbols.setDecimalSeparator('.');
        } else
        {
            symbols.setGroupingSeparator('.');
            symbols.setDecimalSeparator(',');
        }
        df.setDecimalFormatSymbols(symbols);
        df.setMaximumFractionDigits(8);
        return df;
    }

    /*
        Calculations between numbers can be done correctly only if there is no grouping seperator and decimal seperator is a dot.
        Otherwise errors and bugs arise. So at this function depending on the user's preffered display format we call the appropriate
        function to change that seperators to default as stated above in order to do the calculation correctly.
    */
    private void fixTextsDotAndComma(String opsText) {
        if(sharedPref.contains(getString(R.string.dotComma)))
        {
            fixTextsDotComma(opsText);
        } else if(sharedPref.contains(getString(R.string.commaDot)))
        {
            fixTextsCommaDot(opsText);
        } else if(sharedPref.contains(getString(R.string.spaceComma)))
        {
            fixTextSpaceComma(opsText);
        } else if(sharedPref.contains(getString(R.string.spaceDot)))
        {
            fixTextsSpaceDot(opsText);
        } else
        {
            fixTextsDotComma(opsText);
        }
    }

    /*
        Example in which this function will run: 1.000,5
        We replace the dot with an empty string "" and decimal seperator (comma) with a dot.
    */
    private void fixTextsDotComma(String opsText) {
        if(opsText.contains(","))
        {
            opsText = opsText.replace(".", "");
            opsText = opsText.replace(",", ".");
            ops.setText(opsText);
        }
        else if(!opsText.contains(",") && opsText.contains("."))
        {
            int counter = 0;
            for(int i=0; i < opsText.length(); i++ ) {
                if( opsText.charAt(i) == '.' ) {
                    counter++;
                }
            }
            if(counter >= 1)
            {
                opsText = opsText.replace(".", "");
                ops.setText(opsText);
            }
        }
    }

    /*
        Example in which this function will run: 1,000.5
        We replace the comma with an empty string "", decimal seperator is already in correct format.
    */
    private void fixTextsCommaDot(String opsText) {
        if(opsText.contains("."))
        {
            opsText = opsText.replace(",", "");
            ops.setText(opsText);
        }
        else if(!opsText.contains(".") && opsText.contains(","))
        {
            int counter = 0;
            for(int i=0; i < opsText.length(); i++ ) {
                if( opsText.charAt(i) == ',' ) {
                    counter++;
                }
            }
            if(counter >= 1)
            {
                opsText = opsText.replace(",", "");
                ops.setText(opsText);
            }
            if(op1 >= 1000)
            {
                opsText = opsText.replace(",", "");
                ops.setText(opsText);
            }
        }
    }

    /*
        Example in which this function will run: 1 000,5
        We replace the space with an empty string "" and decimal seperator, comma, with a dot.
    */
    private void fixTextSpaceComma(String opsText) {
        if(opsText.contains(","))
        {
            opsText = opsText.replace(" ", "");
            opsText = opsText.replace(",", ".");
            ops.setText(opsText);
        }
        else if(!opsText.contains(",") && opsText.contains(" "))
        {
            int counter = 0;
            for(int i=0; i < opsText.length(); i++ ) {
                if( opsText.charAt(i) == ' ' ) {
                    counter++;
                }
            }
            if(counter >= 1)
            {
                opsText = opsText.replace(" ", "");
                ops.setText(opsText);
            }
        }
    }

    /*
        Example in which this function will run: 1 000.5
        We replace the space with an empty string "", decimal seperator is already in correct format.
    */
    private void fixTextsSpaceDot(String opsText) {
        if(opsText.contains("."))
        {
            opsText = opsText.replace(" ", "");
            ops.setText(opsText);
        }
        else if(!opsText.contains(".") && opsText.contains(" "))
        {
            int counter = 0;
            for(int i=0; i < opsText.length(); i++ ) {
                if( opsText.charAt(i) == ' ' ) {
                    counter++;
                }
            }
            if(counter >= 1)
            {
                opsText = opsText.replace(" ", "");
                ops.setText(opsText);
            }
        }
    }

    /*
        Vibrates the phone.
    */
    private void vibrate() {
        if (isCheckedVibrator)
            this.vibrator.vibrate(30);
    }

    /*
        Sets the app's theme the user has chosen to use.
    */
    private void dialogThemeChanger() {
        if (sharedPref.contains(getString(R.string.activityMain))) {
            setContentView(R.layout.activity_main);
        } else if (sharedPref.contains(getString(R.string.activityMainDarkModern))) {
            setContentView(R.layout.activity_main_dark_modern);
        } else if (sharedPref.contains(getString(R.string.activityMainLightModern))) {
            setContentView(R.layout.activity_main_light_modern);
        } else if(sharedPref.contains(getString(R.string.activityMainGreen))){
            setContentView(R.layout.activity_main_green);
        } else if(sharedPref.contains(getString(R.string.activityMainPink))){
            setContentView(R.layout.activity_main_pink);
        } else if(sharedPref.contains(getString(R.string.activityMainPinkModern))){
            setContentView(R.layout.activity_main_pink_modern);
        } else
        {
            setContentView(R.layout.activity_main);
            editor.putInt(getString(R.string.activityMain), R.layout.activity_main);
            editor.apply();
        }
    }

    
    /*
        Describes the alert dialog used for showing the setting for different display formats, shows current format
        and if user wants to change saves the new format into SharedPreferences and uses that from such on.
    */
    private Dialog onCreateDialogSingleChoice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final int[] chosen = new int[1];
        int savedFormat = 0;
        if(sharedPref.contains(getString(R.string.commaDot)))
        {
            savedFormat = 1;
        } else if(sharedPref.contains(getString(R.string.spaceComma)))
        {
            savedFormat = 2;
        } else if(sharedPref.contains(getString(R.string.spaceDot)))
        {
            savedFormat = 3;
        }
        CharSequence[] array = {getString(R.string.dotComma),getString(R.string.commaDot),getString(R.string.spaceComma),getString(R.string.spaceDot)};
        builder.setTitle(getString(R.string.selectDisplay)).setSingleChoiceItems(array, savedFormat, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chosen[0] = which;
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        switch (chosen[0]) {
                            case 0:
                                editor.remove(getString(R.string.commaDot));
                                editor.remove(getString(R.string.spaceComma));
                                editor.remove(getString(R.string.spaceDot));
                                editor.apply();
                                editor.putInt(getString(R.string.dotComma), R.string.dotComma);
                                editor.apply();
                                displayFormat = getString(R.string.dotComma);
                                break;
                            case 1:
                                editor.remove(getString(R.string.dotComma));
                                editor.remove(getString(R.string.spaceComma));
                                editor.remove(getString(R.string.spaceDot));
                                editor.apply();
                                editor.putInt(getString(R.string.commaDot), R.string.commaDot);
                                editor.apply();
                                displayFormat = getString(R.string.commaDot);
                                break;
                            case 2:
                                editor.remove(getString(R.string.commaDot));
                                editor.remove(getString(R.string.dotComma));
                                editor.remove(getString(R.string.spaceDot));
                                editor.apply();
                                editor.putInt(getString(R.string.spaceComma), R.string.spaceComma);
                                editor.apply();
                                displayFormat = getString(R.string.spaceComma);
                                break;
                            case 3:
                                editor.remove(getString(R.string.commaDot));
                                editor.remove(getString(R.string.spaceComma));
                                editor.remove(getString(R.string.dotComma));
                                editor.apply();
                                editor.putInt(getString(R.string.spaceDot), R.string.spaceDot);
                                editor.apply();
                                displayFormat = getString(R.string.spaceDot);
                                break;
                            default:
                                break;
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel), null);

        return builder.create();
    }

    /*
        Initializes our TextViews variables, and get their saved data back on screen.
        This method helps if user scrolls in options for example so his calculation is saved and restored and not
        having to type it again.
    */
    private void saveTexts() {
        result = (TextView) findViewById(R.id.resultText);
        ops = (TextView) findViewById(R.id.operationsRuntime);
        opsFull = (TextView) findViewById(R.id.operationsFull);

        opsFull.setText(operationsFull);
        result.setText(results);
        ops.setText(operations);
    }

    /*
        Insert into history's database an entry with the current date and the calculation we want to save.
    */
    private void saveHistory(String toBeSaved) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        String strDate = sdf.format(c.getTime());
        toBeSaved = strDate.concat("     " + toBeSaved);
        databaseHelper.insert(toBeSaved);
    }

}





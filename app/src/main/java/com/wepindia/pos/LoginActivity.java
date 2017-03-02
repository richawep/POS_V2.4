/***************************************************************************
 * Project Name		:	VAJRA
 * <p>
 * File Name		:	DatabaseHandler
 * <p>
 * DateOfCreation	:	13-October-2012
 * <p>
 * Author			:	Balasubramanya Bharadwaj B S
 **************************************************************************/
package com.wepindia.pos;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mswipetech.wisepad.sdktest.view.ApplicationData;
import com.wep.common.app.Database.BillSetting;
import com.wep.common.app.Database.DatabaseHandler;
import com.wep.common.app.utils.Preferences;
import com.wepindia.pos.GenericClasses.MessageDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("Since15")
public class LoginActivity extends Activity {

    // Context object
    Context myContext;

    // DatabaseHandler object
    DatabaseHandler dbLogin = new DatabaseHandler(LoginActivity.this);
    // MessageDialog Object
    MessageDialog MsgBox;

    // View handling variables
    EditText txtUserId, txtPassword;
    Button btnDateDisplay, btnMonthDisplay, btnYearDisplay;

    // Class Variables
    private static final int HOME_RESULT = 1;
    //private static final String FILE_SHARED_PREFERENCE = "WeP_FnB";
    Calendar calDate;
    // Variables - BillSettings object
    BillSetting objBillSettings = new BillSetting();

    @TargetApi(9)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myContext = this;

        try {
            MsgBox = new MessageDialog(this);

            calDate = Calendar.getInstance();

            txtUserId = (EditText) findViewById(R.id.txtUserId);
            txtPassword = (EditText) findViewById(R.id.txtPassword);
            btnDateDisplay = (Button) findViewById(R.id.btnDateDisplay);
            btnMonthDisplay = (Button) findViewById(R.id.btnMonthDisplay);
            btnYearDisplay = (Button) findViewById(R.id.btnYearDisplay);

            btnDateDisplay.setText(String.valueOf(calDate.get(Calendar.DAY_OF_MONTH)));
            btnMonthDisplay.setText(calDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US));
            btnYearDisplay.setText(String.valueOf(calDate.get(Calendar.YEAR)));

            dbLogin.CreateDatabase();
            dbLogin.OpenDatabase();

            Cursor crsrBillNoResetSetting = null;
            crsrBillNoResetSetting = dbLogin.getBillNoResetSetting();
            if (crsrBillNoResetSetting.moveToFirst()) {
                if (crsrBillNoResetSetting.getString(crsrBillNoResetSetting.getColumnIndex("Period")).equalsIgnoreCase("Enable")) {
                    Date d = new Date();
                    CharSequence s = android.text.format.DateFormat.format("yyyy-MM-dd", d.getTime());
                    int iResult = 0;
                    if (crsrBillNoResetSetting.getString(crsrBillNoResetSetting.getColumnIndex("PeriodDate")).equalsIgnoreCase(s.toString())) {
                        String strPeriod = "Enable";
                        iResult = dbLogin.UpdateBillNoReset(strPeriod);
                    } /*else {
                        String strPeriod = "Disable";
                        iResult = dbLogin.UpdateBillNoResetPeriod(strPeriod);
                    }*/
                }
            } else {
                Log.d("OtherSettings", "No data in BillNoResetSettings table");
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        initSinglePrinter();
    }

    private void initSinglePrinter() {
        SharedPreferences sharedPreferences = Preferences.getSharedPreferencesForPrint(LoginActivity.this); // getSharedPreferences("PrinterConfigurationActivity", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("kot","Heyday");
        editor.putString("bill","Heyday");
        editor.putString("report","Heyday");
        editor.putString("receipt","Heyday");
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Login Activity", "OnStart() Event");
    }

    // About button
    public void About(View v) {
        String strAboutMsg = "WeP Touch POS\nVersion:1.1.0\n\nAbout WeP Solutions Limited." +
                "\n\n\tWeP group of companies is into design, manufacturing and distribution of various types of printers & IT peripherals. " +
                "The group companies also offer managed printing solutions, UPS & energy solutions, information security software products." +
                "\n\n\tWeP Group of companies has an interesting past and even more exciting future. " +
                "It is one of the top Small Giants in the Indian IT space and features at rank 175 in the Data Quest (Sept 2012) " +
                "listing of IT companies in India with a group turnover of over Rs 3000 mn( USD 60Mn) . " +
                "WeP Group have been re-inventing itself every few years, from the last 12 years, " +
                "by bringing in new disruptive products in the market and is poised to make significant change in next few years.";
        AlertDialog.Builder PickUpTender = new AlertDialog.Builder(myContext);
        PickUpTender
                .setIcon(R.drawable.ic_launcher)
                .setTitle("About")
                .setMessage(strAboutMsg)
                .setNeutralButton("OK", null)
                .show();
    }

    // Login button event

    public void onLogin(View view) {
        Cursor User = dbLogin.getUser(txtUserId.getText().toString(), txtPassword.getText().toString());
        if (User != null) {
            if (User.moveToFirst()) {
                Intent intentHomeScreen = new Intent(myContext, HomeActivity.class);

                String userId = User.getString(User.getColumnIndex("UserId"));
                String userName = User.getString(User.getColumnIndex("Name"));
                String userRole = User.getString(User.getColumnIndex("RoleId"))+"";

                ApplicationData.savePreference(this,ApplicationData.USER_ID,userId);
                ApplicationData.savePreference(this,ApplicationData.USER_NAME,userName);
                ApplicationData.savePreference(this,ApplicationData.USER_ROLE,userRole);

                startActivity(intentHomeScreen);

                /*objBillSettings.setDateAndTime(1);
                long iResult = dbLogin.updateDateAndTime(objBillSettings);*/

                finish();

            } else {
                MsgBox.Show("Login", "Wrong user id or password");
            }
        } else {
            MsgBox.Show("Login", "Login DB Error");
        }

    }

    // Close button event
    public void Close(View v) {
        // Close Database connection
        dbLogin.CloseDatabase();

        // Close the application
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            if (requestCode == HOME_RESULT) {
                this.finish();
            } else {
                this.txtUserId.setText("");
                this.txtPassword.setText("");
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            AlertDialog.Builder AuthorizationDialog = new AlertDialog.Builder(myContext);
            LayoutInflater UserAuthorization = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View vwAuthorization = UserAuthorization.inflate(R.layout.user_authorization, null);
            final EditText txtUserId = (EditText) vwAuthorization.findViewById(R.id.etAuthorizationUserId);
            final EditText txtPassword = (EditText) vwAuthorization.findViewById(R.id.etAuthorizationUserPassword);
            final TextView tvAuthorizationUserId= (TextView) vwAuthorization.findViewById(R.id.tvAuthorizationUserId);
            final TextView tvAuthorizationUserPassword= (TextView) vwAuthorization.findViewById(R.id.tvAuthorizationUserPassword);
            tvAuthorizationUserId.setVisibility(View.GONE);
            tvAuthorizationUserPassword.setVisibility(View.GONE);
            txtUserId.setVisibility(View.GONE);
            txtPassword.setVisibility(View.GONE);
            AuthorizationDialog
                    .setTitle("Are you sure you want to exit ?")
                    .setView(vwAuthorization)
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            /*Intent returnIntent =new Intent();
                            setResult(Activity.RESULT_OK,returnIntent);*/
                            finish();
                        }
                    })
                    .show();
        }

        return super.onKeyDown(keyCode, event);
    }
}

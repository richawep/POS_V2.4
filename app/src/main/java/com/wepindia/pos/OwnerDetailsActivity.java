package com.wepindia.pos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.mswipetech.wisepad.sdktest.view.ApplicationData;
import com.wep.common.app.Database.DatabaseHandler;
import com.wep.common.app.WepBaseActivity;
import com.wepindia.pos.GenericClasses.MessageDialog;
import com.wepindia.pos.utils.ActionBarUtils;

import java.util.Date;

public class OwnerDetailsActivity extends WepBaseActivity {

    private com.wep.common.app.views.WepButton btnAdd,btnClear,btnClose;
    private EditText Name,Gstin,Phone,Email,Address;
    private DatabaseHandler dbHelper;
    private Toolbar toolbar;
    MessageDialog MsgBox;
    Context myContext;
    String strUserName;
    Spinner spinner1,spinner2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_details);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        myContext = this;
        MsgBox = new MessageDialog(myContext);
        strUserName = ApplicationData.getUserName(this);

        Date d = new Date();
        CharSequence s = DateFormat.format("dd-MM-yyyy", d.getTime());
        com.wep.common.app.ActionBarUtils.setupToolbar(OwnerDetailsActivity.this, toolbar, getSupportActionBar(), "Owner Details", strUserName, " Date:" + s.toString());

        dbHelper = new DatabaseHandler(OwnerDetailsActivity.this);
        btnAdd=(com.wep.common.app.views.WepButton)findViewById(R.id.btnAdd);
        btnClear=(com.wep.common.app.views.WepButton)findViewById(R.id.btnClear);
        btnClose=(com.wep.common.app.views.WepButton)findViewById(R.id.btnClose);
        Name=(EditText)findViewById(R.id.ownerName);
        Gstin=(EditText)findViewById(R.id.ownerGstin);
        Phone=(EditText)findViewById(R.id.ownerContact);
        Email=(EditText)findViewById(R.id.ownerEmail);
        spinner1=(Spinner)findViewById(R.id.ownerPos);
        spinner2=(Spinner)findViewById(R.id.ownerOffice);
        spinner2.setSelection(1);
        Address=(EditText)findViewById(R.id.ownerAddress);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

        if(Name.getText().toString().equalsIgnoreCase("")||
                Email.getText().toString().equalsIgnoreCase("")||
                Phone.getText().toString().equalsIgnoreCase("")||
                spinner1.getSelectedItem().toString().equalsIgnoreCase("")||
                spinner1.getSelectedItem().toString().equalsIgnoreCase("Select")||
                Address.getText().toString().equalsIgnoreCase(""))
        {
            Toast.makeText(OwnerDetailsActivity.this, "detail not completed", Toast.LENGTH_SHORT).show();
        }
        else
        {
            try
            {
                dbHelper.CreateDatabase();
                dbHelper.OpenDatabase();
                dbHelper.deleteOwnerDetails();
                updateDetails();
            }
            catch(Exception ex)
            {
                MsgBox.Show("Error", ex.getMessage());
            }
        }

            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v)
           {
               close(v);
           }
        });
        btnClear.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Name.setText("");
                Gstin.setText("");
                Phone.setText("");
                Email.setText("");
                Address.setText("");
                spinner1.setSelection(0);
                spinner2.setSelection(1);
            }
        });

    }
    private void updateDetails()
    {
        long Status;
        String str = spinner1.getSelectedItem().toString();
        int length = str.length();
        String sub = "";
        if (length > 0) {
            sub = str.substring(length - 2, length);
        }
           Status=dbHelper.addOwnerDetails(Name.getText().toString(),Gstin.getText().toString(),
                   Phone.getText().toString(),Email.getText().toString(),
                   Address.getText().toString(),sub,
                   spinner2.getSelectedItem().toString());
        if(Status>0)
            Toast.makeText(OwnerDetailsActivity.this, "Details Successfully Added", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(OwnerDetailsActivity.this, "Could Not Add Details", Toast.LENGTH_SHORT).show();
    }
    private void close(View v)
    {
        dbHelper.close();
        Intent intentHomeScreen = new Intent(this, HomeActivity.class);
        startActivity(intentHomeScreen);
        this.finish();
    }
    @Override
    public void onHomePressed() {
        ActionBarUtils.navigateHome(this);
    }
}

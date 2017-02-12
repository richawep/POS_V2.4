package com.wepindia.pos.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

import com.wep.common.app.Database.BillSetting;
import com.wep.common.app.Database.DatabaseHandler;
import com.wepindia.pos.GenericClasses.DateTime;
import com.wepindia.pos.GenericClasses.EditTextInputHandler;
import com.wepindia.pos.GenericClasses.MessageDialog;
import com.wepindia.pos.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FragmentSettingsMiscellaneous extends Fragment {

    Context myContext;
    DatabaseHandler dbMiscellaneousSettings ;
    DateTime objDate;
    MessageDialog MsgBox;// = new MessageDialog(MiscellaneousSettingsActivity.this);
    EditText txtPOSNumber, txtTIN, txtSubUdfText, txtServiceTaxPercent, txtBusinessDate;
    DatePicker dateBusinessDate;
    RadioButton rbItemPriceSeriviceTax, rbBillAmountServiceTax;
    CheckBox chkWeighScale;
    BillSetting objBillSettings = new BillSetting();
    String strBusinessDate = "";
    private Button btnApplyMiscSettings,btnCloseMiscSettings,btn_BusinessDate;

    int DateChange =0;
    Calendar Calobj;

    public FragmentSettingsMiscellaneous() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            dbMiscellaneousSettings = new DatabaseHandler(getActivity());
            dbMiscellaneousSettings.OpenDatabase();
        }catch (Exception e){

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_settings_miscellaneous, container, false);
        myContext = getActivity();
        MsgBox = new MessageDialog(myContext);
        EditTextInputHandler etInputValidate =  new EditTextInputHandler();
        btnCloseMiscSettings = (Button) view.findViewById(R.id.btnCloseMiscSettings);
        btnCloseMiscSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Close();
            }
        });
        btnApplyMiscSettings = (Button) view.findViewById(R.id.btnApplyMiscSettings);
        btnApplyMiscSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Apply();
            }
        });

        btn_BusinessDate = (Button) view.findViewById(R.id.btn_BusinessDate);
        btn_BusinessDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateSelection();
            }
        });
        txtPOSNumber = (EditText)view.findViewById(R.id.etPOSNumber);
        txtTIN = (EditText)view.findViewById(R.id.etTIN);
        txtSubUdfText = (EditText)view.findViewById(R.id.etSubUdfText);
        txtBusinessDate = (EditText)view.findViewById(R.id.etBusinessDate);
        txtServiceTaxPercent = (EditText)view.findViewById(R.id.etServiceTaxPercent);
        etInputValidate.ValidateDecimalInput(txtServiceTaxPercent);
        rbItemPriceSeriviceTax = (RadioButton)view.findViewById(R.id.rbItemPriceServiceTax);
        rbBillAmountServiceTax = (RadioButton)view.findViewById(R.id.rbBillAmountServiceTax);
        chkWeighScale = (CheckBox)view.findViewById(R.id.chkUseWeighScale);

        try{
            dbMiscellaneousSettings.CloseDatabase();
            dbMiscellaneousSettings.CreateDatabase();
            dbMiscellaneousSettings.OpenDatabase();
            DisplaySettings();
        }
        catch(Exception exp){
            exp.printStackTrace();
            MsgBox.Show("Exception", exp.getMessage());
        }
        return view;
    }


    public void DateSelection(){
        try {
            //DateChange -> 1 - auto, 0 = manual
            if(DateChange == 1)
            {
                MsgBox = new MessageDialog(myContext);
                MsgBox.Show("Information", " Date Selection is in auto mode. Please change it to manual mode.");
                return;
            }
            AlertDialog.Builder dlgReportDate = new AlertDialog.Builder(myContext);
            final DatePicker dateReportDate = new DatePicker(myContext);
            /*if (DateType ==1)
            {
                startDate_date =   new Date(dateReportDate.getYear() - 1900, dateReportDate.getMonth(), dateReportDate.getDayOfMonth());
            }
            else if (DateType ==2)
            {
                endDate_date =   new Date(dateReportDate.getYear() - 1900, dateReportDate.getMonth(), dateReportDate.getDayOfMonth());
            }*/
            // Initialize date picker value to business date
            //dateReportDate.updateDate(objDate.getYear(), objDate.getMonth(), objDate.getDay());


            int year = Calobj.get(Calendar.YEAR);
            int month = Calobj.get(Calendar.MONTH);
            int day = Calobj.get(Calendar.DAY_OF_MONTH);

            dateReportDate.updateDate(year, month,day);

            String strMessage = "";


            dlgReportDate
                    .setIcon(R.drawable.ic_launcher)
                    .setTitle("Date Selection")
                    .setMessage(strMessage)
                    .setView(dateReportDate)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            // richa date format change

                            //strDate = String.valueOf(dateReportDate.getYear()) + "-";
                            String strDate = "";
                            if (dateReportDate.getDayOfMonth() < 10) {
                                strDate = "0" + String.valueOf(dateReportDate.getDayOfMonth())+"-";
                            } else {
                                strDate = String.valueOf(dateReportDate.getDayOfMonth())+"-";
                            }
                            if (dateReportDate.getMonth() < 9) {
                                strDate += "0" + String.valueOf(dateReportDate.getMonth() + 1) + "-";
                            } else {
                                strDate += String.valueOf(dateReportDate.getMonth() + 1) + "-";
                            }

                            strDate += String.valueOf(dateReportDate.getYear());
                            txtBusinessDate.setText(strDate);

                            Log.d("MiscellaneousSetting", "Business Date Date:" + strDate);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub

                        }
                    })
                    .show();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void DisplaySettings(){

        Cursor crsrMiscSetting = null;

        // Read the settings from database and display it in views.
        crsrMiscSetting = dbMiscellaneousSettings.getBillSetting();

        if(crsrMiscSetting.moveToFirst()){

            DateChange = (crsrMiscSetting.getInt(crsrMiscSetting.getColumnIndex("DateAndTime")));
            txtPOSNumber.setText(crsrMiscSetting.getString(crsrMiscSetting.getColumnIndex("POSNumber")));
            txtSubUdfText.setText(crsrMiscSetting.getString(crsrMiscSetting.getColumnIndex("SubUdfText")));
            txtTIN.setText(crsrMiscSetting.getString(crsrMiscSetting.getColumnIndex("TIN")));
            objDate = new DateTime(crsrMiscSetting.getString(crsrMiscSetting.getColumnIndex("BusinessDate")));
            txtBusinessDate.setText(crsrMiscSetting.getString(crsrMiscSetting.getColumnIndex("BusinessDate")));
            String date_str = crsrMiscSetting.getString(crsrMiscSetting.getColumnIndex("BusinessDate"));
            Calobj = convertDate(date_str,"dd-MM-yyyy");


            int iServiceTaxType = crsrMiscSetting.getInt(crsrMiscSetting.getColumnIndex("ServiceTaxType"));
            if(iServiceTaxType == 1){
                rbItemPriceSeriviceTax.setChecked(true);
                txtServiceTaxPercent.setEnabled(false);
            } else if(iServiceTaxType == 2){
                rbBillAmountServiceTax.setChecked(true);
                txtServiceTaxPercent.setEnabled(true);
            }
            txtServiceTaxPercent.setText(crsrMiscSetting.getString(crsrMiscSetting.getColumnIndex("ServiceTaxPercent")));

            if(crsrMiscSetting.getInt(crsrMiscSetting.getColumnIndex("WeighScale")) == 1){
                chkWeighScale.setChecked(true);
            } else {
                chkWeighScale.setChecked(false);
            }

        }
        else{
            Log.d("Display Misc Settings","No data in BillSettings table");
        }
    }

    private Calendar convertDate(String startDateString, String format)
    {

        SimpleDateFormat df = new SimpleDateFormat(format);
        Calendar startDate= null;
        try {
            startDate =  Calendar.getInstance();
            startDate.setTime(df.parse(startDateString));
            //String newDateString = df.format(startDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return startDate;
    }

    private void ReadSettings(){

        // Local variables
        String strPOSNumber, strTIN, strSubUdfText, BusinessDate;
        float fServiceTaxPercent = 0;
        int iServiceTaxType = 0;

        strPOSNumber = txtPOSNumber.getText().toString();
        strTIN = txtTIN.getText().toString();
        strSubUdfText = txtSubUdfText.getText().toString();
        fServiceTaxPercent = Float.parseFloat(txtServiceTaxPercent.getText().toString());
        BusinessDate = txtBusinessDate.getText().toString();

        if(rbItemPriceSeriviceTax.isChecked()){
            iServiceTaxType = 1;
        } else {
            iServiceTaxType = 2;
        }

        if(strPOSNumber.equalsIgnoreCase("") || strTIN.equalsIgnoreCase("") ||
                BusinessDate.equalsIgnoreCase("")){
            MsgBox.Show("Warning", "Please fill all text boxes");
        }
        else{
            objBillSettings.setPOSNumber(Integer.parseInt(strPOSNumber));
            objBillSettings.setTIN(strTIN);
            objBillSettings.setSubUdfText(strSubUdfText);
            objBillSettings.setBusinessDate(BusinessDate);
            objBillSettings.setServiceTaxType(iServiceTaxType);
            objBillSettings.setServiceTaxPercent(fServiceTaxPercent);
            if(chkWeighScale.isChecked()){
                objBillSettings.setWeighScale(1);
            } else {
                objBillSettings.setWeighScale(0);
            }
        }
        int iDateAndTime = 0;
        Date d = new Date();
        CharSequence currentdate = DateFormat.format("yyyy-MM-dd", d.getTime());
        Cursor crsrBillDate = dbMiscellaneousSettings.getBillSetting();
        if(crsrBillDate.moveToFirst())
        {
            if(currentdate.toString().equalsIgnoreCase(crsrBillDate.getString(crsrBillDate.getColumnIndex("BusinessDate"))))
            {
                objBillSettings.setDateAndTime(1);
                long iResult = dbMiscellaneousSettings.updateDateAndTime(objBillSettings);
            }
            else
            {
                objBillSettings.setDateAndTime(0);
                long iResult = dbMiscellaneousSettings.updateDateAndTime(objBillSettings);
            }
        }
    }

    private void SaveMiscSettings(){
        int iResult = 0;

        // Update new settings in database
        iResult = dbMiscellaneousSettings.updateMiscellaneousBillsettings(objBillSettings);

        if(iResult > 0){
            MsgBox.Show("Information", "Settings saved");
        }
        else{
            MsgBox.Show("Information", "Failed to save settings");
        }
    }

    public void ServiceTaxClick(View v){
        if(rbBillAmountServiceTax.isChecked()){
            txtServiceTaxPercent.setEnabled(true);
        } else {
            txtServiceTaxPercent.setEnabled(false);
        }
    }

    public void Apply(){
        ReadSettings();
        SaveMiscSettings();
    }

    public void Close(){
        dbMiscellaneousSettings.CloseDatabase();
        getActivity().finish();
    }
}

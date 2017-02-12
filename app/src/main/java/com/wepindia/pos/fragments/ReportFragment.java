package com.wepindia.pos.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.nfc.FormatException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.wep.common.app.Database.DatabaseHandler;
import com.wep.common.app.gst.B2Csmall;
import com.wep.common.app.gst.Model_reconcile;
import com.wep.common.app.utils.Preferences;
import com.wep.common.app.views.WepButton;
import com.wepindia.pos.GenericClasses.DateTime;
import com.wepindia.pos.GenericClasses.MessageDialog;
import com.wepindia.pos.GenericClasses.ReportHelper;
import com.wepindia.pos.R;
import com.wepindia.pos.TabbedReportActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class ReportFragment extends Fragment implements View.OnClickListener {

    Context myContext;
    DatabaseHandler dbReport ;
    DateTime objDate;
    MessageDialog MsgBox;
    TextView lblPersonName, txtReportPerson;
    EditText txtReportDateStart, txtReportDateEnd, txtPersonId;
    WepButton btnPrint, btnExport, btnView;
    Spinner spnrReportType;
    TableRow rowPersonId;//, rowReportColumnCaption;
    public TableLayout tblReport;
    Date startDate_date, endDate_date;
    ArrayAdapter<String> adapReportNames;
    String strDate = "", strBusinessDate = "";
    String[] ReportNames;
    String ReportType = "";
    String strReportsId, strReportName;
    Spinner spnrUsers, spnrCustomers;
    private List<String> labelUsers, labelCustomers;
    String GSTEnable = "";
    Cursor billsettingcursor = null;
    private View view;

    private Button btn_ReportDateFrom,btn_ReportDateTo,
            btn_ReportPrint,btn_ReportExport,btn_ReportView,btn_ReportClose;

    public ReportFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_report, container, false);
        btn_ReportDateFrom = (Button) view.findViewById(R.id.btn_ReportDateFrom);
        btn_ReportDateFrom.setOnClickListener(this);
        btn_ReportDateTo = (Button) view.findViewById(R.id.btn_ReportDateTo);
        btn_ReportDateTo.setOnClickListener(this);
        btn_ReportPrint = (Button) view.findViewById(R.id.btn_ReportPrint);
        btn_ReportPrint.setOnClickListener(this);
        btn_ReportExport = (Button) view.findViewById(R.id.btn_ReportExport);
        btn_ReportExport.setOnClickListener(this);
        btn_ReportView = (Button) view.findViewById(R.id.btn_ReportView);
        btn_ReportView.setOnClickListener(this);
        btn_ReportClose = (Button) view.findViewById(R.id.btn_ReportClose);
        btn_ReportClose.setOnClickListener(this);
        onInit();
        return view;
    }

    public void onInit() {
        myContext = getActivity();
        dbReport = new DatabaseHandler(getActivity());
        ReportType = getArguments().getString("REPORT_TYPE");

        try {
            InitializeViews();
            ResetAll();
            dbReport.CreateDatabase();
            dbReport.OpenDatabase();
            billsettingcursor = dbReport.getBillSetting();
            if ((billsettingcursor != null) && billsettingcursor.moveToFirst()) {
                GSTEnable = billsettingcursor.getString(billsettingcursor.getColumnIndex("GSTEnable"));
                if (GSTEnable == null) {
                    GSTEnable = "0";
                }
                // Setting Report type as per GST
                if (GSTEnable.equals("1") && ReportType.equals("1")) {
                    ReportType = "4";
                }
            }

            Cursor BusinessDate = dbReport.getCurrentDate();
            if (BusinessDate.moveToFirst()) {
                objDate = new DateTime(BusinessDate.getString(0));
                txtReportDateStart.setText(BusinessDate.getString(0));
                txtReportDateEnd.setText(BusinessDate.getString(0));
            } else {
                //objDate = new DateTime();
            }
            Date d = new Date();
            CharSequence currentdate = DateFormat.format("yyyy-MM-dd", d.getTime());
            objDate = new DateTime(currentdate.toString());

            loadSpinnerData(ReportType);
            spnrReportType.setOnItemSelectedListener(ReportSelect);


        } catch (Exception e) {
            e.printStackTrace();
            MsgBox.Show("Error", e.getMessage());
        }
    }

    private void loadSpinnerData(String ReportsType) {

        // Spinner Drop down cursor
        Cursor servicesCursor = dbReport.getReportsNameCursor(ReportsType);

        // map the cursor column names to the TextView ids in the layout
        String[] from = {"ReportsName"};
        int[] to = {android.R.id.text1};

        // Creating adapter for spinner
        SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                servicesCursor, from, to, 0);

        // attaching data adapter to spinner
        spnrReportType.setAdapter(dataAdapter);
    }

    private void InitializeViews() {

        MsgBox = new MessageDialog(myContext);

        spnrReportType = (Spinner) view.findViewById(R.id.spnrReportNameSelection);

        lblPersonName = (TextView) view.findViewById(R.id.lblName);
        txtReportPerson = (TextView) view.findViewById(R.id.tvReportPersonId);
        txtPersonId = (EditText) view.findViewById(R.id.etReportPersonId);
        txtReportDateStart = (EditText) view.findViewById(R.id.etReportDateStart);
        txtReportDateEnd = (EditText) view.findViewById(R.id.etReportDateEnd);

        btnPrint = (WepButton) view.findViewById(R.id.btn_ReportPrint);
        btnExport = (WepButton) view.findViewById(R.id.btn_ReportExport);
        btnView = (WepButton) view.findViewById(R.id.btn_ReportView);

        rowPersonId = (TableRow) view.findViewById(R.id.rowReportPersonId);

        tblReport = (TableLayout) view.findViewById(R.id.tblReportView);
        spnrUsers = (Spinner) view.findViewById(R.id.spnrUsers);
        spnrUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spnrUsers.getSelectedItem().toString().equalsIgnoreCase("Select")) {
                    txtPersonId.setText("");
                } else {
                    int userid = dbReport.getUsersIdByName(labelUsers.get(spnrUsers.getSelectedItemPosition()));
                    txtPersonId.setText(String.valueOf(userid));
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnrCustomers = (Spinner) view.findViewById(R.id.spnrCustomers);
        spnrCustomers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spnrCustomers.getSelectedItem().toString().equalsIgnoreCase("Select")) {
                    txtPersonId.setText("");
                } else {
                    int customerid = dbReport.getCustomersIdByName(labelCustomers.get(spnrCustomers.getSelectedItemPosition()));
                    txtPersonId.setText(String.valueOf(customerid));
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * This Event will get triggered only when report selection is changed in Report spinner
     */
    AdapterView.OnItemSelectedListener ReportSelect = new AdapterView.OnItemSelectedListener() {

        public void onItemSelected(AdapterView<?> adapter, View v, int position,
                                   long id) {
            // TODO Auto-generated method stub
//            String ReportName = (String) adapter.getSelectedItem();
            Cursor crsrReport = (Cursor) adapter.getSelectedItem();
            strReportName = crsrReport.getString(crsrReport.getColumnIndex("ReportsName"));
            ResetAll();
            if (strReportName.equalsIgnoreCase("Waiter Detailed Report") ||
                    strReportName.equalsIgnoreCase("Rider Detailed Report") ||
                    strReportName.equalsIgnoreCase("User Detailed Report") ||
                    strReportName.equalsIgnoreCase("Customer Detailed Report")) {

                rowPersonId.setVisibility(View.VISIBLE);

                if (!strReportName.equalsIgnoreCase("User Detailed Report")) {
                    txtPersonId.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                if (strReportName.equalsIgnoreCase("Waiter Detailed Report")) {
                    loadSpinnerUsers(3);
                    txtReportPerson.setText("Select Waiter : ");
                    spnrUsers.setVisibility(View.VISIBLE);
                    spnrCustomers.setVisibility(View.GONE);
                } else if (strReportName.equalsIgnoreCase("Rider Detailed Report")) {
                    loadSpinnerUsers(4);
                    txtReportPerson.setText("Select Rider : ");
                    spnrUsers.setVisibility(View.VISIBLE);
                    spnrCustomers.setVisibility(View.GONE);
                } else {
                    loadSpinnerUsers();
                    txtReportPerson.setText("Select User : ");
                    spnrUsers.setVisibility(View.VISIBLE);
                    spnrCustomers.setVisibility(View.GONE);
                }

                if (strReportName.equalsIgnoreCase("Customer Detailed Report")) {
                    loadSpinnerCustomers();
                    txtReportPerson.setText("Select Customer : ");
                    spnrCustomers.setVisibility(View.VISIBLE);
                    spnrUsers.setVisibility(View.GONE);
                }

            } else {

                rowPersonId.setVisibility(View.INVISIBLE);
            }
            strReportsId = String.valueOf(id);

            //MsgBox.Show("", String.valueOf(id));
        }

        public void onNothingSelected(AdapterView<?> adapter) {
            // TODO Auto-generated method stub
            rowPersonId.setVisibility(View.INVISIBLE);
        }
    };

    private void DateSelection(final int DateType) {        // StartDate: DateType = 1 EndDate: DateType = 2
        try {
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
            dateReportDate.updateDate(objDate.getYear(), objDate.getMonth(), objDate.getDay());
            String strMessage = "";
            if (DateType == 1) {
                strMessage = "Select report Start date";
            } else {
                strMessage = "Select report End date";
            }

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

                            if (DateType == 1) {
                                txtReportDateStart.setText(strDate);
                                startDate_date =   new Date(dateReportDate.getYear() - 1900, dateReportDate.getMonth(), dateReportDate.getDayOfMonth());
                            } else {
                                txtReportDateEnd.setText(strDate);
                                endDate_date =   new Date(dateReportDate.getYear() - 1900, dateReportDate.getMonth(), dateReportDate.getDayOfMonth()+1);

                            }
                            Log.d("ReportDate", "Selected Date:" + strDate);
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

    private boolean DateValidation(String StartDate, String EndDate) {
        if (StartDate.equalsIgnoreCase("") ||
                EndDate.equalsIgnoreCase("")) {
            MsgBox.Show("Warning", "Please select report date range properly");
            return false;
        } else {
            return true;
        }
    }

    private void ResetAll() {
        //spnrReportType.setSelection(0);

        txtReportDateStart.setText("");
        txtReportDateEnd.setText("");

        lblPersonName.setText("");
        txtPersonId.setText("");

        btnExport.setEnabled(false);
        btnPrint.setEnabled(false);

        tblReport.removeAllViews();

    }

    public void From() {
        DateSelection(1);
    }

    public void To() {
        if (!txtReportDateStart.getText().toString().equalsIgnoreCase("")) {
            DateSelection(2);

        } else {
            MsgBox.Show("Warning", "Please select report FROM date");
        }
    }

    public void ViewReport() {

        // Clear the table
        tblReport.removeAllViews();
        if (spnrReportType.getSelectedItemPosition() != 5 ||
                spnrReportType.getSelectedItemPosition() != 6) {
            if (DateValidation(txtReportDateStart.getText().toString(),
                    txtReportDateEnd.getText().toString()) != true) {
                return;
            }
        }
        // ReportHelper object
        ReportHelper objReportColumn = new ReportHelper(myContext);
        objReportColumn.setReportColumnCaptions(myContext, strReportName, tblReport);

        //switch (spnrReportType.getSelectedItemPosition() + 1) {
        switch (Integer.valueOf(strReportsId)) {
            case 1:    // Bill wise Report
                BillwiseReport();
                break;

            case 2:    // Transaction Report
                TransactionReport();
                break;

            case 3:    // Sales Tax Report
                TaxReport();
                break;

            case 4:    // Service Tax Report
                /*int iTaxType = 0;
                String strServiceTaxPercent = "";
                Cursor Settings = dbReport.getBillSetting();
                if (Settings.moveToFirst()) {
                    iTaxType = Settings.getInt(Settings.getColumnIndex("ServiceTaxType"));
                    strServiceTaxPercent = Settings.getString(Settings.getColumnIndex("ServiceTaxPercent"));
                }
                if (iTaxType == 1) {
                    ItemServiceTaxReport();
                } else if (iTaxType == 2) {
                    BillServiceTaxReport(strServiceTaxPercent);
                }
                break;*/

            case 5:    // Void Bill Report
                VoidBillReport();
                break;

            case 6:    // Duplicate Bill Report
                DuplicateBillReport();
                break;

            case 7:    // KOT Pending Report
                KOTPendingReport();
                break;

            case 8:    // KOT Deleted Report
                KOTDeletedReport();
                break;

            case 9:    // Item wise Report
                ItemwiseReport();
                break;

            case 10: // Day wise Report
                int count = tblReport.getChildCount();
                DaywiseReport(count);
                break;

            case 11: // Month wise Report
                try {
                    MonthwiseReport();
                } catch (NumberFormatException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;

            case 12: // Department wise Report
                DepartmentwiseReport();
                break;

            case 13: // Category wise Report
                CategorywiseReport();
                break;

            case 14: // Kitchen wise Report
                KitchenwiseReport();
                break;

            case 15: // Waiter wise Report
                WaiterwiseReport();
                break;

            case 16: // Waiter Detailed Report
                WaiterDetailedReport();
                break;

            case 17: // Rider wise Report
                RiderwiseReport();
                break;

            case 18: // Rider Detailed Report
                RiderDetailedReport();
                break;

            case 19: // User wise Report
                UserwiseReport();
                break;

            case 20: // User Detailed Report
                UserDetailedReport();
                break;

            case 21: // Customer wise Report
                CustomerwiseReport();
                break;

            case 22: // Customer Detailed Report
                CustomerDetailedReport();
                break;

            case 23: // Payments Report
                PaymentReport();
                break;

            case 24: // Receipts Report
                ReceiptReport();
                break;

            case 25: // Fast Sellin Itemwise Report
                FastSellingItemwiseReport();
                break;

            case 26: //// GSTR1-B2B
                GSTR1_B2B();
                break;
            case 27: // GSTR1-B2BA
                break;
            case 28:// GSTR1-B2C
                GSTR1_B2Cs();
                break;
            case 29:// GSTR1-B2ClA
                break;
            case 30:// GSTR1-B2Cl
                GSTR1_B2Cl();
                break;
            case 31:// GSTR1-B2CsA
                break;
            case 32: // GSTR2-B2B
                GSTR2_registered();
                break;
            case 33:// GSTR2-B2BA
                break;
            case 34: // GTR2-B2C
                GSTR2_unregistered();
                break;
            case 35: // GTR2-B2cA
                break;
            case 36: //2A
                break;
            case 37:// modified 2a
                break;
            case 38://2A validation
                reconcile2();
                break;
            case 39://1A validation
                reconcile1();
                break;
            case 40: //Supplier wise
                SupplierwiseReport();
                break;
            case 41: //Cummulative Payment-Reciept-Sales Report
                Cummulate_payment();
                break;
            // richa_2012
            case 42:  // Cummulative Billing Report
                Cummulate_Billing();
                break;

        }
    }

    public void ExportReport() {
        boolean isSuccess = false;
        // ReportHelper object
        ReportHelper objReportExport = new ReportHelper(myContext);
        isSuccess = objReportExport.ExportReportToCSV(myContext, tblReport, strReportName,
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());
        if (isSuccess) {
            MsgBox.Show("Information", "Report exported successfully");
        }
    }


    public void Close() {
        // Close database connection
        dbReport.CloseDatabase();

        // finish the activity
        getActivity().finish();
    }

    //richa_2012 starts
    private void Cummulate_Billing()
    {
        int cummulativeHeadingEnable =1;
        String HomeCaption1="";
        String HomeCaption2="";
        String HomeCaption3="";
        String HomeCaption4="";

        Cursor billSettingCursor = null;
        try
        {
            billSettingCursor = dbReport.getBillSetting();
            if (billSettingCursor!= null && billSettingCursor.moveToFirst())
            {
                cummulativeHeadingEnable = billSettingCursor.getInt(billSettingCursor.getColumnIndex("CummulativeHeadingEnable"));
                if (cummulativeHeadingEnable ==1)
                {
                    HomeCaption1 = billSettingCursor.getString(billSettingCursor.getColumnIndex("HomeDineInCaption"));
                    HomeCaption2 = billSettingCursor.getString(billSettingCursor.getColumnIndex("HomeCounterSalesCaption"));
                    HomeCaption3 = billSettingCursor.getString(billSettingCursor.getColumnIndex("HomeTakeAwayCaption"));
                    HomeCaption4 = billSettingCursor.getString(billSettingCursor.getColumnIndex("HomeHomeDeliveryCaption"));

                }
            }

            // population HomeCaption1 data
            if (cummulativeHeadingEnable == 1)
            {
                TableRow rowReport1 = new TableRow(myContext);
                rowReport1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                rowReport1.setBackgroundColor(getResources().getColor(R.color.khadiyelow));
                TextView Type = new TextView(myContext);
                Type.setWidth(100);
                Type.setTextSize(15);
                Type.setTextColor(Color.BLACK);
                Type.setText(HomeCaption1);
                Type.setGravity(Gravity.CENTER);
                rowReport1.addView(Type);
                tblReport.addView(rowReport1);
                CummulativeBillingCaption();
                int count = tblReport.getChildCount();
                BillingReport(1);

                TableRow rowReport2 = new TableRow(myContext);
                rowReport2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                rowReport2.setBackgroundColor(getResources().getColor(R.color.khadiyelow));
                TextView Type2 = new TextView(myContext);
                Type2.setWidth(100);
                Type2.setTextSize(15);
                Type2.setTextColor(Color.BLACK);
                Type2.setText(HomeCaption2);
                Type2.setGravity(Gravity.CENTER);
                rowReport2.addView(Type2);
                tblReport.addView(rowReport2);
                CummulativeBillingCaption();
                BillingReport(2);

                TableRow rowReport3 = new TableRow(myContext);
                rowReport3.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                rowReport3.setBackgroundColor(getResources().getColor(R.color.khadiyelow));
                TextView Type3 = new TextView(myContext);
                Type3.setWidth(100);
                Type3.setTextSize(15);
                Type3.setTextColor(Color.BLACK);
                Type3.setText(HomeCaption3);
                Type3.setGravity(Gravity.CENTER);
                rowReport3.addView(Type3);
                tblReport.addView(rowReport3);
                CummulativeBillingCaption();
                BillingReport(3);

                TableRow rowReport4 = new TableRow(myContext);
                rowReport4.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                rowReport4.setBackgroundColor(getResources().getColor(R.color.khadiyelow));
                TextView Type4 = new TextView(myContext);
                Type4.setWidth(100);
                Type4.setTextSize(15);
                Type4.setTextColor(Color.BLACK);
                Type4.setText(HomeCaption4);
                Type4.setGravity(Gravity.CENTER);
                rowReport4.addView(Type4);
                tblReport.addView(rowReport4);
                CummulativeBillingCaption();
                BillingReport(4);
            }
            else
            {
                CummulativeBillingCaption();
                BillwiseReport();
            }

        }
        catch(Exception e) {
            MsgBox.setMessage(e.getMessage())
                    .setPositiveButton("OK",null)
                    .show();
        }


    }

    private  void CummulativeBillingCaption()
    {
        TableRow rowReport = new TableRow(myContext);
        rowReport.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        rowReport.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));

        TextView Date = new TextView(myContext);
        Date.setWidth(120);
        Date.setTextSize(15);
        Date.setTextColor(Color.WHITE);
        Date.setText("Date");

        TextView BillNumber = new TextView(myContext);
        BillNumber.setWidth(100);
        BillNumber.setTextSize(15);
        BillNumber.setTextColor(Color.WHITE);
        BillNumber.setText("Bill Number");


        TextView TotalItems = null;
        TotalItems = new TextView(myContext);
        TotalItems.setWidth(100);
        TotalItems.setTextSize(15);
        TotalItems.setTextColor(Color.WHITE);
        TotalItems.setText("Items");

        TextView Amount = new TextView(myContext);
        Amount.setWidth(100);
        Amount.setTextSize(15);
        Amount.setTextColor(Color.WHITE);
        Amount.setText("Amount");

        TextView SalesTax = new TextView(myContext);
        SalesTax.setWidth(100);
        SalesTax.setTextSize(15);
        SalesTax.setTextColor(Color.WHITE);
        SalesTax.setText("Sales Tax");

        TextView ServiceTax = new TextView(myContext);
        ServiceTax.setWidth(100);
        ServiceTax.setTextSize(15);
        ServiceTax.setTextColor(Color.WHITE);
        ServiceTax.setText("Service Tax");

        TextView Discount = new TextView(myContext);
        Discount.setWidth(100);
        Discount.setTextSize(15);
        Discount.setTextColor(Color.WHITE);
        Discount.setText("Discount");


        // Add views to row
        rowReport.addView(Date);
        rowReport.addView(BillNumber);
        rowReport.addView(TotalItems);
        rowReport.addView(Discount);
        rowReport.addView(SalesTax);
        rowReport.addView(ServiceTax);
        rowReport.addView(Amount);

        tblReport.addView(rowReport);

    }

    private void BillingReport(int billingMode) {

        Cursor Report = dbReport.getBillingReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()), billingMode);

        if (Report.moveToFirst()) {

            TextView Date, BillNumber, TotalItems, Amount, SalesTax, ServiceTax, Discount;
            TableRow rowReport;
            float totbillAmt=0,totSalesTax=0,totServiceTax =0, totDisc =0;

            do {
                rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                Date = new TextView(myContext);
                String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                String dateString = formatter.format(Long.parseLong(dateInMillis));
                Date.setText(dateString);

                BillNumber = new TextView(myContext);
                BillNumber.setText(Report.getString(Report.getColumnIndex("InvoiceNo")));

                TotalItems = new TextView(myContext);
                TotalItems.setText(Report.getString(Report.getColumnIndex("TotalItems")));

                Amount = new TextView(myContext);
                Amount.setText(Report.getString(Report.getColumnIndex("BillAmount")));
                totbillAmt += Float.parseFloat(Amount.getText().toString());

                SalesTax = new TextView(myContext);
                SalesTax.setText(Report.getString(Report.getColumnIndex("TotalTaxAmount")));
                totSalesTax += Float.parseFloat(SalesTax.getText().toString());

                ServiceTax = new TextView(myContext);
                ServiceTax.setText(Report.getString(Report.getColumnIndex("TotalServiceTaxAmount")));
                totServiceTax += Float.parseFloat(ServiceTax.getText().toString());

                Discount = new TextView(myContext);
                Discount.setText(Report.getString(Report.getColumnIndex("TotalDiscountAmount")));
                totDisc += Float.parseFloat(Discount.getText().toString());

                rowReport.addView(Date);
                rowReport.addView(BillNumber);
                rowReport.addView(TotalItems);
                rowReport.addView(Discount);
                rowReport.addView(SalesTax);
                rowReport.addView(ServiceTax);
                rowReport.addView(Amount);

                tblReport.addView(rowReport,
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));

            } while (Report.moveToNext());


            rowReport = new TableRow(myContext);
            rowReport.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowReport.setBackgroundColor(myContext.getResources().getColor(R.color.colorPrimaryLight));

            Date = new TextView(myContext);
            Date.setText("Total");
            Date.setTextColor(Color.WHITE);
            Date.setTextSize(15);

            BillNumber = new TextView(myContext);


            TotalItems = new TextView(myContext);

            Amount = new TextView(myContext);
            Amount.setText(String.valueOf(totbillAmt));
            Amount.setTextColor(Color.WHITE);
            Amount.setTextSize(15);


            SalesTax = new TextView(myContext);
            SalesTax.setText(String.valueOf(totSalesTax));
            SalesTax.setTextColor(Color.WHITE);
            SalesTax.setTextSize(15);


            ServiceTax = new TextView(myContext);
            ServiceTax.setText(String.valueOf(totServiceTax));
            ServiceTax.setTextColor(Color.WHITE);
            ServiceTax.setTextSize(15);

            Discount = new TextView(myContext);
            Discount.setText(String.valueOf(totDisc));
            Discount.setTextColor(Color.WHITE);
            Discount.setTextSize(15);


            rowReport.addView(Date);
            rowReport.addView(BillNumber);
            rowReport.addView(TotalItems);
            rowReport.addView(Discount);
            rowReport.addView(SalesTax);
            rowReport.addView(ServiceTax);
            rowReport.addView(Amount);

            tblReport.addView(rowReport,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done for Billing Mode "+ billingMode);
        }
    }

    // richa_2012 ends

    private void Cummulate_payment() {
        // payment
        {

            TableRow rowReport1 = new TableRow(myContext);
            rowReport1.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowReport1.setBackgroundColor(getResources().getColor(R.color.khadiyelow));
            TextView Type = new TextView(myContext);
            Type.setWidth(100);
            Type.setTextSize(15);
            Type.setTextColor(Color.BLACK);
            Type.setText("Payment");
            Type.setGravity(Gravity.CENTER);
            rowReport1.addView(Type);

            TableRow rowReport = new TableRow(myContext);
            rowReport.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowReport.setBackgroundColor(getResources().getColor(R.color.black));
            TextView Date = new TextView(myContext);
            Date.setWidth(120);
            Date.setTextSize(15);
            Date.setTextColor(Color.WHITE);
            Date.setText("Date");

            TextView Description = new TextView(myContext);
            Description.setWidth(150);
            Description.setTextSize(15);
            Description.setTextColor(Color.WHITE);
            Description.setText("Description");

            TextView Reason = new TextView(myContext);
            Reason.setWidth(130);
            Reason.setTextSize(15);
            Reason.setTextColor(Color.WHITE);
            Reason.setText("Reason");

            TextView Amount = new TextView(myContext);
            Amount.setWidth(100);
            Amount.setTextSize(15);
            Amount.setTextColor(Color.WHITE);
            Amount.setText("Amount");

            // Add views to row
            rowReport.addView(Date);
            rowReport.addView(Description);
            rowReport.addView(Amount);
            rowReport.addView(Reason);
            tblReport.addView(rowReport1);
            tblReport.addView(rowReport);
        }
        PaymentReport();
        // receipt
        {
            TableRow rowReport1 = new TableRow(myContext);
            rowReport1.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowReport1.setBackgroundColor(getResources().getColor(R.color.khadiyelow));
            TextView Type = new TextView(myContext);
            Type.setWidth(100);
            Type.setTextSize(15);
            Type.setTextColor(Color.BLACK);
            Type.setText("Receipt");
            Type.setGravity(Gravity.CENTER);
            rowReport1.addView(Type);

            TableRow rowReport = new TableRow(myContext);
            rowReport.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            rowReport.setBackgroundColor(getResources().getColor(R.color.black));


            TextView Date = new TextView(myContext);
            Date.setWidth(120);
            Date.setTextSize(15);
            Date.setTextColor(Color.WHITE);
            Date.setText("Date");

            TextView Description = new TextView(myContext);
            Description.setWidth(150);
            Description.setTextSize(15);
            Description.setTextColor(Color.WHITE);
            Description.setText("Description");

            TextView Reason = new TextView(myContext);
            Reason.setWidth(130);
            Reason.setTextSize(15);
            Reason.setTextColor(Color.WHITE);
            Reason.setText("Reason");

            TextView Amount = new TextView(myContext);
            Amount.setWidth(100);
            Amount.setTextSize(15);
            Amount.setTextColor(Color.WHITE);
            Amount.setText("Amount");

            // Add views to row
            rowReport.addView(Date);
            rowReport.addView(Description);
            rowReport.addView(Amount);
            rowReport.addView(Reason);
            tblReport.addView(rowReport1);
            tblReport.addView(rowReport);
        }
        ReceiptReport();
        // sales
        {
            TableRow rowReport1 = new TableRow(myContext);
            rowReport1.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowReport1.setBackgroundColor(getResources().getColor(R.color.khadiyelow));
            TextView Type = new TextView(myContext);
            Type.setWidth(100);
            Type.setTextSize(15);
            Type.setTextColor(Color.BLACK);
            Type.setText("Sales");
            Type.setGravity(Gravity.CENTER);
            rowReport1.addView(Type);

            TableRow rowReport = new TableRow(myContext);
            rowReport.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            rowReport.setBackgroundColor(getResources().getColor(R.color.black));

            TextView Date = new TextView(myContext);
            Date.setWidth(120);
            Date.setTextSize(15);
            Date.setTextColor(Color.WHITE);
            Date.setText("Date");

            TextView BillNumber = new TextView(myContext);
            BillNumber.setWidth(100);
            BillNumber.setTextSize(15);
            BillNumber.setTextColor(Color.WHITE);
            BillNumber.setText("Total Bills");

            TextView Amount = new TextView(myContext);
            Amount.setWidth(100);
            Amount.setTextSize(15);
            Amount.setTextColor(Color.WHITE);
            Amount.setText("Amount");

            TextView SalesTax = new TextView(myContext);
            SalesTax.setWidth(100);
            SalesTax.setTextSize(15);
            SalesTax.setTextColor(Color.WHITE);
            SalesTax.setText("Sales Tax");

            TextView ServiceTax = new TextView(myContext);
            ServiceTax.setWidth(100);
            ServiceTax.setTextSize(15);
            ServiceTax.setTextColor(Color.WHITE);
            ServiceTax.setText("Service Tax");

            TextView Discount = new TextView(myContext);
            Discount.setWidth(100);
            Discount.setTextSize(15);
            Discount.setTextColor(Color.WHITE);
            Discount.setText("Discount");

            TextView ReprintCount = new TextView(myContext);
            ReprintCount.setWidth(100);
            ReprintCount.setTextSize(15);
            ReprintCount.setTextColor(Color.WHITE);
            ReprintCount.setText("Reprint Count");

            // Add views to row
            rowReport.addView(Date);
            rowReport.addView(BillNumber);
            rowReport.addView(Discount);
            rowReport.addView(SalesTax);
            rowReport.addView(ServiceTax);
            rowReport.addView(Amount);
            tblReport.addView(rowReport1);
            tblReport.addView(rowReport);
        }
        int count = tblReport.getChildCount();
        DaywiseReport(count);
    }


    private void BillwiseReport() {
        /*Cursor Report = dbReport.getBillwiseReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/

        Cursor Report = dbReport.getBillwiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));

        if (Report.moveToFirst()) {

            TextView Date, BillNumber, TotalItems, Amount, SalesTax, ServiceTax, Discount;
            TableRow rowReport;
            float totSalesTax =0, totServiceTax =0, totbillAmt =0,totDisc =0;

            do {
                rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                Date = new TextView(myContext);
                String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                String dateString = formatter.format(Long.parseLong(dateInMillis));
                Date.setText(dateString);

                BillNumber = new TextView(myContext);
                BillNumber.setText(Report.getString(Report.getColumnIndex("InvoiceNo")));

                TotalItems = new TextView(myContext);
                TotalItems.setText(Report.getString(Report.getColumnIndex("TotalItems")));

                Amount = new TextView(myContext);
                double amt_s = (Report.getDouble(Report.getColumnIndex("BillAmount")));
                String amt_str = getFormatedCharacterForPrint_init(String.format("%.2f",amt_s),8,1);
                Amount.setText(amt_str);
                totbillAmt+= Float.parseFloat(Amount.getText().toString());

                SalesTax = new TextView(myContext);
                SalesTax.setText(Report.getString(Report.getColumnIndex("TotalTaxAmount")));
                totSalesTax+= Float.parseFloat(SalesTax.getText().toString());

                ServiceTax = new TextView(myContext);
                ServiceTax.setText(Report.getString(Report.getColumnIndex("TotalServiceTaxAmount")));
                totServiceTax+= Float.parseFloat(ServiceTax.getText().toString());

                Discount = new TextView(myContext);
                Discount.setText(Report.getString(Report.getColumnIndex("TotalDiscountAmount")));
                totDisc+= Float.parseFloat(Discount.getText().toString());

                rowReport.addView(Date);
                rowReport.addView(BillNumber);
                rowReport.addView(TotalItems);
                rowReport.addView(Discount);
                rowReport.addView(SalesTax);
                rowReport.addView(ServiceTax);
                rowReport.addView(Amount);

                tblReport.addView(rowReport,
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));

            } while (Report.moveToNext());

            rowReport = new TableRow(myContext);
            rowReport.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowReport.setBackgroundColor(myContext.getResources().getColor(R.color.colorPrimaryLight));

            Date = new TextView(myContext);
            Date.setText("Total");
            Date.setTextColor(Color.WHITE);
            Date.setTextSize(15);

            BillNumber = new TextView(myContext);


            TotalItems = new TextView(myContext);

            Amount = new TextView(myContext);
            Amount.setText(String.valueOf(totbillAmt));
            Amount.setTextColor(Color.WHITE);
            Amount.setTextSize(15);


            SalesTax = new TextView(myContext);
            SalesTax.setText(String.valueOf(totSalesTax));
            SalesTax.setTextColor(Color.WHITE);
            SalesTax.setTextSize(15);


            ServiceTax = new TextView(myContext);
            ServiceTax.setText(String.valueOf(totServiceTax));
            ServiceTax.setTextColor(Color.WHITE);
            ServiceTax.setTextSize(15);

            Discount = new TextView(myContext);
            Discount.setText(String.valueOf(totDisc));
            Discount.setTextColor(Color.WHITE);
            Discount.setTextSize(15);


            rowReport.addView(Date);
            rowReport.addView(BillNumber);
            rowReport.addView(TotalItems);
            rowReport.addView(Discount);
            rowReport.addView(SalesTax);
            rowReport.addView(ServiceTax);
            rowReport.addView(Amount);

            tblReport.addView(rowReport,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));



            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void TransactionReport() {
        /*Cursor Report = dbReport.getBillwiseReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/
        Cursor Report = dbReport.getBillwiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


        if (Report.moveToFirst()) {

            TextView Date, BillNumber, TotalItems, Amount, Cash, Card, Coupon, PettyCash, Wallet;
            TableRow rowReport;
            float totAmt =0, totCash =0, totCoupon =0,totWallet =0,totPetty =0,totCard =0;

            do {
                rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                Date = new TextView(myContext);
                String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                String dateString = formatter.format(Long.parseLong(dateInMillis));
                Date.setText(dateString);

                BillNumber = new TextView(myContext);
                BillNumber.setText(Report.getString(Report.getColumnIndex("InvoiceNo")));

                TotalItems = new TextView(myContext);
                TotalItems.setText(Report.getString(Report.getColumnIndex("TotalItems")));

                Amount = new TextView(myContext);
                Amount.setText(Report.getString(Report.getColumnIndex("BillAmount")));
                totAmt += Float.parseFloat(Amount.getText().toString());

                Cash = new TextView(myContext);
                Cash.setText(Report.getString(Report.getColumnIndex("CashPayment")));
                totCash += Float.parseFloat(Cash.getText().toString());

                Card = new TextView(myContext);
                Card.setText(Report.getString(Report.getColumnIndex("CardPayment")));
                totCard += Float.parseFloat(Card.getText().toString());

                Coupon = new TextView(myContext);
                Coupon.setText(Report.getString(Report.getColumnIndex("CouponPayment")));
                totCoupon += Float.parseFloat(Coupon.getText().toString());

                PettyCash = new TextView(myContext);
                PettyCash.setText(Report.getString(Report.getColumnIndex("PettyCashPayment")));
                totPetty += Float.parseFloat(PettyCash.getText().toString());

                Wallet = new TextView(myContext);
                Wallet.setText(Report.getString(Report.getColumnIndex("WalletPayment")));
                totWallet += Float.parseFloat(Wallet.getText().toString());

                rowReport.addView(Date);
                rowReport.addView(BillNumber);
                rowReport.addView(TotalItems);
                rowReport.addView(Amount);
                rowReport.addView(Cash);
                rowReport.addView(Card);
                rowReport.addView(Coupon);
                rowReport.addView(PettyCash);
                rowReport.addView(Wallet);

                tblReport.addView(rowReport,
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));

            } while (Report.moveToNext());


            rowReport = new TableRow(myContext);
            rowReport.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowReport.setBackgroundColor(myContext.getResources().getColor(R.color.colorPrimaryLight));

            Date = new TextView(myContext);
            Date.setText("Total");
            Date.setTextColor(Color.WHITE);
            Date.setTextSize(15);

            BillNumber = new TextView(myContext);
            TotalItems = new TextView(myContext);

            Amount = new TextView(myContext);
            Amount.setText(String.valueOf(totAmt));
            Amount.setTextColor(Color.WHITE);
            Amount.setTextSize(15);

            Cash = new TextView(myContext);
            Cash.setText(String.valueOf(totCash));
            Cash.setTextColor(Color.WHITE);
            Cash.setTextSize(15);

            Coupon = new TextView(myContext);
            Coupon.setText(String.valueOf(totCoupon));
            Coupon.setTextColor(Color.WHITE);
            Coupon.setTextSize(15);

            Card = new TextView(myContext);
            Card.setText(String.valueOf(totCard));
            Card.setTextColor(Color.WHITE);
            Card.setTextSize(15);

            PettyCash = new TextView(myContext);
            PettyCash.setText(String.valueOf(totPetty));
            PettyCash.setTextColor(Color.WHITE);
            PettyCash.setTextSize(15);

            Wallet = new TextView(myContext);
            Wallet.setText(String.valueOf(totWallet));
            Wallet.setTextColor(Color.WHITE);
            Wallet.setTextSize(15);

            rowReport.addView(Date);
            rowReport.addView(BillNumber);
            rowReport.addView(TotalItems);
            rowReport.addView(Amount);
            rowReport.addView(Cash);
            rowReport.addView(Card);
            rowReport.addView(Coupon);
            rowReport.addView(PettyCash);
            rowReport.addView(Wallet);

            tblReport.addView(rowReport,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void TaxReport() {
        /*Cursor Report = dbReport.getTaxReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());
        */
        Cursor Report = dbReport.getTaxReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


        Log.d("TaxReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isTaxExists = false;

            TextView Percent, Description, TaxAmount, TotalAmount;
            TableRow rowReport;
            float  totbillAmt =0, totSalesTax =0;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView TaxPercent = (TextView) rowItem.getChildAt(0);

                        if (Double.parseDouble(TaxPercent.getText().toString()) ==
                                Report.getDouble(Report.getColumnIndex("TaxPercent"))) {
                            TextView TaxDes = (TextView) rowItem.getChildAt(1);

                            if (TaxDes.getText().toString().equalsIgnoreCase("Sales")) {
                                // Sales Tax
                                TextView Tax = (TextView) rowItem.getChildAt(2);
                                Tax.setText(String.format("%.2f",
                                        Double.parseDouble(Tax.getText().toString()) +
                                                Report.getDouble(Report.getColumnIndex("TaxAmount"))));

                                // Amount
                                TextView Amt = (TextView) rowItem.getChildAt(3);
                                Amt.setText(String.format("%.2f",
                                        Double.parseDouble(Amt.getText().toString()) +
                                                Report.getDouble(Report.getColumnIndex("BillAmount"))));
                                isTaxExists = true;
                                break;
                            }
                            /*TextView Percent1= (TextView) rowItem.getChildAt(1);
                            if (Percent1.getText().toString().equalsIgnoreCase(Report.getString(Report.getColumnIndex("TaxDescription"))))
                            {
                                // Sales Tax
                                TextView Tax = (TextView) rowItem.getChildAt(2);
                                Tax.setText(String.format("%.2f",
                                        Double.parseDouble(Tax.getText().toString()) +
                                                Report.getDouble(Report.getColumnIndex("TaxAmount"))));

                                // Amount
                                TextView Amt = (TextView) rowItem.getChildAt(3);
                                Amt.setText(String.format("%.2f",
                                        Double.parseDouble(Amt.getText().toString()) +
                                                Report.getDouble(Report.getColumnIndex("BillAmount"))));
                                isTaxExists = true;
                                break;

                            }*/

                        }
                    }
                }

                if (isTaxExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    Percent = new TextView(myContext);
                    Percent.setText(Report.getString(Report.getColumnIndex("TaxPercent")));

                    /*Cursor cc = dbReport.getPercentFromItemLedger(Report.getString(Report.getColumnIndex("InvoiceNo")),
                            Report.getString(Report.getColumnIndex("InvoiceDate")));
                    if(cc !=null && cc.moveToNext())
                    {
                        Percent.setText(cc.getString(cc
                                .getColumnIndex("TaxPercent")));
                    }
                    else
                    {
                        Percent.setText("-");
                    }*/


                    Description = new TextView(myContext);
                    Description.setText("Sales");

                    TaxAmount = new TextView(myContext);
                    TaxAmount.setText(String.format("%.2f",Float.parseFloat(Report.getString(Report
                            .getColumnIndex("TaxAmount")))));

                    TotalAmount = new TextView(myContext);
                    TotalAmount.setText(Report.getString(Report
                            .getColumnIndex("BillAmount")));

                    rowReport.addView(Percent);
                    rowReport.addView(Description);
                    rowReport.addView(TaxAmount);
                    rowReport.addView(TotalAmount);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }


                isTaxExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
        ServiceTaxReport();

        int count = tblReport.getChildCount();
        if(count>1)
        {
            float totTax =0, totAmt =0;
            for(int i =1;i<count;i++)
            {
                TableRow row = (TableRow) tblReport.getChildAt(i);
                TextView tax = (TextView) row.getChildAt(2);
                TextView amt = (TextView) row.getChildAt(3);

                totTax += Float.parseFloat(tax.getText().toString());
                totAmt += Float.parseFloat(amt.getText().toString());
            }

            TableRow rowReport = new TableRow(myContext);
            rowReport.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowReport.setBackgroundColor(myContext.getResources().getColor(R.color.colorPrimaryLight));

            TextView Percent = new TextView(myContext);
            Percent.setText("Total");
            Percent.setTextColor(Color.WHITE);
            Percent.setTextSize(15);

            TextView Description = new TextView(myContext);

            TextView TotalAmount = new TextView(myContext);
            TotalAmount.setText(String.valueOf(totAmt));
            TotalAmount.setTextColor(Color.WHITE);
            TotalAmount.setTextSize(15);


            TextView TaxAmount = new TextView(myContext);
            TaxAmount.setText(String.valueOf(totTax));
            TaxAmount.setTextColor(Color.WHITE);
            TaxAmount.setTextSize(15);


            rowReport.addView(Percent);
            rowReport.addView(Description);
            rowReport.addView(TaxAmount);
            rowReport.addView(TotalAmount);

            tblReport.addView(rowReport,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
        }

    }

    private void ServiceTaxReport() {

        Cursor Report = dbReport.getTaxReport_Service(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


        Log.d("TaxReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isTaxExists = false;

            TextView Percent, Description, TaxAmount, TotalAmount;
            TableRow rowReport;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView TaxPercent = (TextView) rowItem.getChildAt(0);

                        if (Double.parseDouble(TaxPercent.getText().toString()) ==
                                Report.getDouble(Report.getColumnIndex("ServiceTaxPercent"))) {

                            TextView TaxDes = (TextView) rowItem.getChildAt(1);

                            if (TaxDes.getText().toString().equalsIgnoreCase("Service")) {

                                // Service Tax
                                TextView Tax = (TextView) rowItem.getChildAt(2);
                                Tax.setText(String.format("%.2f",
                                        Double.parseDouble(Tax.getText().toString()) +
                                                Report.getDouble(Report.getColumnIndex("ServiceTaxAmount"))));

                                // Amount
                                TextView Amt = (TextView) rowItem.getChildAt(3);
                                Amt.setText(String.format("%.2f",
                                        Double.parseDouble(Amt.getText().toString()) +
                                                Report.getDouble(Report.getColumnIndex("BillAmount"))));
                                isTaxExists = true;
                                break;
                            }
                        }
                    }
                }

                if (isTaxExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    Percent = new TextView(myContext);
                    Percent.setText(Report.getString(Report.getColumnIndex("ServiceTaxPercent")));

                    /*Cursor cc = dbReport.getPercentFromItemLedger(Report.getString(Report.getColumnIndex("InvoiceNo")),
                            Report.getString(Report.getColumnIndex("InvoiceDate")));
                    if(cc !=null && cc.moveToNext())
                    {
                        Percent.setText(cc.getString(cc
                                .getColumnIndex("TaxPercent")));
                    }
                    else
                    {
                        Percent.setText("-");
                    }*/


                    Description = new TextView(myContext);
                    Description.setText("Service");

                    TaxAmount = new TextView(myContext);
                    TaxAmount.setText(String.format("%.2f",Float.parseFloat(Report.getString(Report
                            .getColumnIndex("ServiceTaxAmount")))));

                    TotalAmount = new TextView(myContext);
                    TotalAmount.setText(Report.getString(Report
                            .getColumnIndex("BillAmount")));

                    rowReport.addView(Percent);
                    rowReport.addView(Description);
                    rowReport.addView(TaxAmount);
                    rowReport.addView(TotalAmount);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }


                isTaxExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }

    }

    private void BillServiceTaxReport(String ServiceTaxPercent) {

        /*Cursor Report = dbReport.getBillwiseReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());
*/
        Cursor Report = dbReport.getBillwiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


        Log.d("ServiceTaxReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isTaxExists = false;

            TextView Percent, Description, TaxAmount, TotalAmount;
            TableRow rowReport;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        // Service Tax
                        TextView Tax = (TextView) rowItem.getChildAt(2);
                        Tax.setText(String.format("%.2f",
                                Double.parseDouble(Tax.getText().toString()) +
                                        Report.getDouble(Report.getColumnIndex("TotalServiceTaxAmount"))));

                        // Amount
                        TextView Amt = (TextView) rowItem.getChildAt(3);
                        Amt.setText(String.format("%.2f",
                                Double.parseDouble(Amt.getText().toString()) +
                                        Report.getDouble(Report.getColumnIndex("BillAmount"))));

                        isTaxExists = true;
                        break;
                    }
                }

                if (isTaxExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    Percent = new TextView(myContext);
                    Percent.setText(ServiceTaxPercent);

                    Description = new TextView(myContext);
                    Description.setText("Tax @ " + ServiceTaxPercent);

                    TaxAmount = new TextView(myContext);
                    TaxAmount.setText(Report.getString(Report
                            .getColumnIndex("TotalServiceTaxAmount")));

                    TotalAmount = new TextView(myContext);
                    TotalAmount.setText(Report.getString(Report
                            .getColumnIndex("BillAmount")));

                    rowReport.addView(Percent);
                    rowReport.addView(Description);
                    rowReport.addView(TaxAmount);
                    rowReport.addView(TotalAmount);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isTaxExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void ItemServiceTaxReport() {
        Cursor crsrTaxDetail = null;

        /*Cursor Report = dbReport.getServiceTaxReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/

        Cursor Report = dbReport.getServiceTaxReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));



        Log.d("ServiceTaxReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isTaxExists = false;

            TextView Percent, Description, TaxAmount, TotalAmount;
            TableRow rowReport;

            crsrTaxDetail = dbReport.getTaxConfig(Report.getDouble(Report
                    .getColumnIndex("ServiceTaxPercent")));

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView TaxPercent = (TextView) rowItem.getChildAt(0);

                        if (Double.parseDouble(TaxPercent.getText().toString()) ==
                                Report.getDouble(Report.getColumnIndex("ServiceTaxPercent"))) {

                            // Sales Tax
                            TextView Tax = (TextView) rowItem.getChildAt(2);
                            Tax.setText(String.format("%.2f",
                                    Double.parseDouble(Tax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("ServiceTaxAmount"))));

                            // Amount
                            TextView Amt = (TextView) rowItem.getChildAt(3);
                            Amt.setText(String.format("%.2f",
                                    Double.parseDouble(Amt.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("BillAmount"))));

                            isTaxExists = true;
                            break;
                        }
                    }
                }

                if (isTaxExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    Percent = new TextView(myContext);
                    Percent.setText(Report.getString(Report
                            .getColumnIndex("ServiceTaxPercent")));

                    Description = new TextView(myContext);
                    if (crsrTaxDetail.moveToFirst()) {
                        Description.setText(crsrTaxDetail.getString
                                (crsrTaxDetail.getColumnIndex("TaxDescription")));
                    } else {
                        Description.setText("Tax @ " + Report.getString(Report
                                .getColumnIndex("ServiceTaxPercent")));
                    }

                    TaxAmount = new TextView(myContext);
                    TaxAmount.setText(Report.getString(Report
                            .getColumnIndex("TaxAmount")));

                    TotalAmount = new TextView(myContext);
                    TotalAmount.setText(Report.getString(Report
                            .getColumnIndex("BillAmount")));

                    rowReport.addView(Percent);
                    rowReport.addView(Description);
                    rowReport.addView(TaxAmount);
                    rowReport.addView(TotalAmount);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isTaxExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void VoidBillReport() {
        /*Cursor Report = dbReport.getVoidBillsReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/

        Cursor Report = dbReport.getVoidBillsReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));



        if (Report.moveToFirst()) {

            TextView Date, BillNumber, TotalItems, Amount, SalesTax, ServiceTax, Discount;
            TableRow rowReport;
            float totbillAmt =0, totSalesTax =0, totServiceTax =0,totDisc =0;

            do {
                rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                Date = new TextView(myContext);
                String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                String dateString = formatter.format(Long.parseLong(dateInMillis));
                Date.setText(dateString);

                BillNumber = new TextView(myContext);
                BillNumber.setText(Report.getString(Report.getColumnIndex("InvoiceNo")));

                TotalItems = new TextView(myContext);
                TotalItems.setText(Report.getString(Report.getColumnIndex("TotalItems")));

                Amount = new TextView(myContext);
                Amount.setText(Report.getString(Report.getColumnIndex("BillAmount")));
                totbillAmt += Float.parseFloat(Amount.getText().toString());

                SalesTax = new TextView(myContext);
                SalesTax.setText(Report.getString(Report.getColumnIndex("TotalTaxAmount")));
                totSalesTax += Float.parseFloat(SalesTax.getText().toString());

                ServiceTax = new TextView(myContext);
                ServiceTax.setText(Report.getString(Report.getColumnIndex("TotalServiceTaxAmount")));
                totServiceTax += Float.parseFloat(ServiceTax.getText().toString());

                Discount = new TextView(myContext);
                Discount.setText(Report.getString(Report.getColumnIndex("TotalDiscountAmount")));
                totDisc += Float.parseFloat(Discount.getText().toString());

                rowReport.addView(Date);
                rowReport.addView(BillNumber);
                rowReport.addView(TotalItems);
                rowReport.addView(Discount);
                rowReport.addView(SalesTax);
                rowReport.addView(ServiceTax);
                rowReport.addView(Amount);

                tblReport.addView(rowReport,
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));

            } while (Report.moveToNext());


            rowReport = new TableRow(myContext);
            rowReport.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowReport.setBackgroundColor(myContext.getResources().getColor(R.color.colorPrimaryLight));

            Date = new TextView(myContext);
            Date.setText("Total");
            Date.setTextColor(Color.WHITE);
            Date.setTextSize(15);

            BillNumber = new TextView(myContext);
            TotalItems = new TextView(myContext);

            Amount = new TextView(myContext);
            Amount.setText(String.valueOf(totbillAmt));
            Amount.setTextColor(Color.WHITE);
            Amount.setTextSize(15);


            SalesTax = new TextView(myContext);
            SalesTax.setText(String.valueOf(totSalesTax));
            SalesTax.setTextColor(Color.WHITE);
            SalesTax.setTextSize(15);


            ServiceTax = new TextView(myContext);
            ServiceTax.setText(String.valueOf(totServiceTax));
            ServiceTax.setTextColor(Color.WHITE);
            ServiceTax.setTextSize(15);

            Discount = new TextView(myContext);
            Discount.setText(String.valueOf(totDisc));
            Discount.setTextColor(Color.WHITE);
            Discount.setTextSize(15);


            rowReport.addView(Date);
            rowReport.addView(BillNumber);
            rowReport.addView(TotalItems);
            rowReport.addView(Discount);
            rowReport.addView(SalesTax);
            rowReport.addView(ServiceTax);
            rowReport.addView(Amount);

            tblReport.addView(rowReport,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));


            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void DuplicateBillReport() {
        /*Cursor Report = dbReport.getDuplicateBillsReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/

        Cursor Report = dbReport.getDuplicateBillsReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));



        if (Report.moveToFirst()) {

            TextView Date, BillNumber, TotalItems, Amount, SalesTax, ServiceTax, Discount, ReprintCount;
            TableRow rowReport;
            float totDis=0, totSalesTax =0, totServiceTax =0, totbillAmt =0;

            do {
                rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                Date = new TextView(myContext);
                String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                String dateString = formatter.format(Long.parseLong(dateInMillis));
                Date.setText(dateString);

                BillNumber = new TextView(myContext);
                BillNumber.setText(Report.getString(Report.getColumnIndex("InvoiceNo")));

                TotalItems = new TextView(myContext);
                TotalItems.setText(Report.getString(Report.getColumnIndex("TotalItems")));

                Amount = new TextView(myContext);
                Amount.setText(Report.getString(Report.getColumnIndex("BillAmount")));
                totbillAmt += Float.parseFloat(Amount.getText().toString());

                SalesTax = new TextView(myContext);
                SalesTax.setText(Report.getString(Report.getColumnIndex("TotalTaxAmount")));
                totSalesTax += Float.parseFloat(SalesTax.getText().toString());

                ServiceTax = new TextView(myContext);
                ServiceTax.setText(Report.getString(Report.getColumnIndex("TotalServiceTaxAmount")));
                totServiceTax += Float.parseFloat(ServiceTax.getText().toString());

                Discount = new TextView(myContext);
                Discount.setText(Report.getString(Report.getColumnIndex("TotalDiscountAmount")));
                totDis += Float.parseFloat(Discount.getText().toString());

                ReprintCount = new TextView(myContext);
                ReprintCount.setText(Report.getString(Report.getColumnIndex("ReprintCount")));

                rowReport.addView(Date);
                rowReport.addView(BillNumber);
                rowReport.addView(TotalItems);
                rowReport.addView(Discount);
                rowReport.addView(SalesTax);
                rowReport.addView(ServiceTax);
                rowReport.addView(Amount);
                rowReport.addView(ReprintCount);

                tblReport.addView(rowReport,
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));

            } while (Report.moveToNext());

            rowReport = new TableRow(myContext);
            rowReport.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowReport.setBackgroundColor(myContext.getResources().getColor(R.color.colorPrimaryLight));

            Date = new TextView(myContext);
            Date.setText("Total");
            Date.setTextColor(Color.WHITE);
            Date.setTextSize(15);

            BillNumber = new TextView(myContext);
            TotalItems = new TextView(myContext);
            ReprintCount = new TextView(myContext);

            Discount = new TextView(myContext);
            Discount.setText(String.valueOf(totDis));
            Discount.setTextColor(Color.WHITE);
            Discount.setTextSize(15);


            SalesTax = new TextView(myContext);
            SalesTax.setText(String.valueOf(totSalesTax));
            SalesTax.setTextColor(Color.WHITE);
            SalesTax.setTextSize(15);

            ServiceTax = new TextView(myContext);
            ServiceTax.setText(String.valueOf(totServiceTax));
            ServiceTax.setTextColor(Color.WHITE);
            ServiceTax.setTextSize(15);

            Amount = new TextView(myContext);
            Amount.setText(String.valueOf(totbillAmt));
            Amount.setTextColor(Color.WHITE);
            Amount.setTextSize(15);

            rowReport.addView(Date);
            rowReport.addView(BillNumber);
            rowReport.addView(TotalItems);
            rowReport.addView(Discount);
            rowReport.addView(SalesTax);
            rowReport.addView(ServiceTax);
            rowReport.addView(Amount);
            rowReport.addView(ReprintCount);



            tblReport.addView(rowReport,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void KOTPendingReport() {
        Cursor Report = dbReport.getKOTPendingReport();

        Log.d("KOTPendingReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isItemExists = false;

            TextView Token, Table, Items, Time, Waiter;
            TableRow rowReport;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView TokenNumber = (TextView) rowItem.getChildAt(0);

                        if (TokenNumber.getText().toString().equalsIgnoreCase(Report.getString(Report
                                .getColumnIndex("TokenNumber")))) {

                            // Total Items
                            TextView TotalItems = (TextView) rowItem.getChildAt(4);
                            TotalItems.setText(String.format("%d",
                                    Integer.parseInt(TotalItems.getText().toString()) + 1));

                            isItemExists = true;
                            break;
                        }
                    }
                }

                if (isItemExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    Token = new TextView(myContext);
                    Token.setText(Report.getString(Report
                            .getColumnIndex("TokenNumber")));

                    Table = new TextView(myContext);
                    Table.setText(Report.getString(Report
                            .getColumnIndex("TableNumber")));

                    Items = new TextView(myContext);
                    Items.setText("1");

                    Waiter = new TextView(myContext);
                    Waiter.setText(Report.getString(Report
                            .getColumnIndex("EmployeeId")));

                    Time = new TextView(myContext);
                    Time.setText(Report.getString(Report
                            .getColumnIndex("Time")));

                    rowReport.addView(Token);
                    rowReport.addView(Table);
                    rowReport.addView(Time);
                    rowReport.addView(Waiter);
                    rowReport.addView(Items);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isItemExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void KOTDeletedReport() {
        Cursor Report = dbReport.getKOTDeletedReport();

        if (Report.moveToFirst()) {

            TextView Token, Table, Time, Waiter, Reason;
            TableRow rowReport;

            do {
                rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                Token = new TextView(myContext);
                Token.setText(Report.getString(Report.getColumnIndex("TokenNumber")));

                Table = new TextView(myContext);
                Table.setText(Report.getString(Report.getColumnIndex("TableNumber")));

                Time = new TextView(myContext);
                Time.setText(Report.getString(Report.getColumnIndex("Time")));

                Waiter = new TextView(myContext);
                Waiter.setPadding(5,0,0,0);
                String waiterId = Report.getString(Report.getColumnIndex("EmployeeId"));
                Cursor crsr_waiter = dbReport.getUsers(waiterId);
                if(crsr_waiter!=null && crsr_waiter.moveToFirst())
                    Waiter.setText(crsr_waiter.getString(crsr_waiter.getColumnIndex("Name")));
                else
                    Waiter.setText(waiterId);

                Reason = new TextView(myContext);
                Reason.setText(Report.getString(Report.getColumnIndex("Reason")));

                rowReport.addView(Token);
                rowReport.addView(Table);
                rowReport.addView(Time);
                rowReport.addView(Waiter);
                rowReport.addView(Reason);

                tblReport.addView(rowReport,
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void ItemwiseReport() {
        /*Cursor Report = dbReport.getItemwiseReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/

        Cursor Report = dbReport.getItemwiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));



        Log.d("ItemwiseReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isItemExists = false;

            TextView Number, Name, SoldQty, Amount, SalesTax, ServiceTax, Discount, ModifierAmount;
            TableRow rowReport;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView ItemNumber = (TextView) rowItem.getChildAt(0);

                        if (Integer.parseInt(ItemNumber.getText().toString()) == (Report.getInt(Report
                                .getColumnIndex("ItemNumber")))) {

                            // Quantity
                            TextView Qty = (TextView) rowItem.getChildAt(2);
                            Qty.setText(String.format("%.2f",
                                    Double.parseDouble(Qty.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("Quantity"))));

                            // Amount
                            TextView Amt = (TextView) rowItem.getChildAt(6);
                            /*Amt.setText(String.format("%.2f",
                                    Double.parseDouble(Amt.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TaxableValue"))));*/
                            float amt_f = Float.parseFloat(Amt.getText().toString());
                            float rate_f = Float.parseFloat(Report.getString(Report.getColumnIndex("Value")));
                            float quant_f = Float.parseFloat(Report.getString(Report.getColumnIndex("Quantity")));
                            amt_f += (rate_f*quant_f);
                            Amt.setText(String.format("%.2f", amt_f));

                            // Sales Tax
                            TextView Tax = (TextView) rowItem.getChildAt(4);
                            Tax.setText(String.format("%.2f",
                                    Double.parseDouble(Tax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TaxAmount"))));

                            // Service Tax
                            TextView ServTax = (TextView) rowItem.getChildAt(5);
                            ServTax.setText(String.format("%.2f",
                                    Double.parseDouble(ServTax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("ServiceTaxAmount"))));

                            // Discount
                            TextView Disc = (TextView) rowItem.getChildAt(3);
                            Disc.setText(String.format("%.2f",
                                    Double.parseDouble(Disc.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("DiscountAmount"))));

                            // Modifier Amount
                            TextView ModAmt = (TextView) rowItem.getChildAt(7);
                            ModAmt.setText(String.format("%.2f",
                                    Double.parseDouble(ModAmt.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("ModifierAmount"))));

                            isItemExists = true;
                            break;
                        }
                    }
                }

                if (isItemExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    Number = new TextView(myContext);
                    Number.setText(Report.getString(Report
                            .getColumnIndex("ItemNumber")));

                    Name = new TextView(myContext);
                    Name.setText(Report.getString(Report
                            .getColumnIndex("ItemName")));

                    SoldQty = new TextView(myContext);
                    SoldQty.setText(String.format("%.2f", Report.getDouble(Report
                            .getColumnIndex("Quantity"))));

                    Amount = new TextView(myContext);
                    /*Amount.setText(String.format("%.2f", Report.getDouble(Report
                            .getColumnIndex("TaxableValue"))));*/

                    float rate_f = Float.parseFloat(Report.getString(Report.getColumnIndex("Value")));
                    float quant_f = Float.parseFloat(Report.getString(Report.getColumnIndex("Quantity")));
                    Amount.setText(String.format("%.2f", rate_f*quant_f));

                    SalesTax = new TextView(myContext);
                    SalesTax.setText(String.format("%.2f", Report.getDouble(Report
                            .getColumnIndex("TaxAmount"))));

                    ServiceTax = new TextView(myContext);
                    ServiceTax.setText(String.format("%.2f", Report.getDouble(Report
                            .getColumnIndex("ServiceTaxAmount"))));

                    Discount = new TextView(myContext);
                    Discount.setText(String.format("%.2f", Report.getDouble(Report
                            .getColumnIndex("DiscountAmount"))));

                    ModifierAmount = new TextView(myContext);
                    ModifierAmount.setText(String.format("%.2f", Report.getDouble(Report
                            .getColumnIndex("ModifierAmount"))));

                    rowReport.addView(Number);
                    rowReport.addView(Name);
                    rowReport.addView(SoldQty);
                    rowReport.addView(Discount);
                    rowReport.addView(SalesTax);
                    rowReport.addView(ServiceTax);
                    rowReport.addView(Amount);
                    rowReport.addView(ModifierAmount);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isItemExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void DaywiseReport(int count) {
        /*Cursor Report = dbReport.getDaywiseReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/

        Cursor Report = dbReport.getDaywiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


        Log.d("DaywiseReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isItemExists = false;

            TextView BillDate, TotalBills, Amount, SalesTax, ServiceTax, Discount;
            TableRow rowReport;
            float totbillAmt =0, totSalesTax =0, totServiceTax=0,totDis =0;

            do {
                for (int iPosition = count; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView Date = (TextView) rowItem.getChildAt(0);
                        String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                        String dateString = formatter.format(Long.parseLong(dateInMillis));
                        if (Date.getText().toString().equalsIgnoreCase(dateString)) {

                            // Total Bills
                            TextView Bills = (TextView) rowItem.getChildAt(1);
                            Bills.setText(String.valueOf(Integer
                                    .parseInt(Bills.getText().toString()) + 1));

                            // Amount
                            TextView Amt = (TextView) rowItem.getChildAt(5);
                            Amt.setText(String.format("%.2f",
                                    Double.parseDouble(Amt.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("BillAmount"))));

                            // Sales Tax
                            TextView Tax = (TextView) rowItem.getChildAt(3);
                            Tax.setText(String.format("%.2f",
                                    Double.parseDouble(Tax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TotalTaxAmount"))));

                            // Service Tax
                            TextView ServTax = (TextView) rowItem.getChildAt(4);
                            ServTax.setText(String.format("%.2f",
                                    Double.parseDouble(ServTax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TotalServiceTaxAmount"))));

                            // Discount
                            TextView Disc = (TextView) rowItem.getChildAt(2);
                            Disc.setText(String.format("%.2f",
                                    Double.parseDouble(Disc.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TotalDiscountAmount"))));


                            isItemExists = true;
                            break;
                        }
                    }
                }

                if (isItemExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    BillDate = new TextView(myContext);
                    String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    String dateString = formatter.format(Long.parseLong(dateInMillis));
                    BillDate.setText(dateString);

                    TotalBills = new TextView(myContext);
                    TotalBills.setText("1");

                    Amount = new TextView(myContext);
                    Amount.setText(Report.getString(Report
                            .getColumnIndex("BillAmount")));


                    SalesTax = new TextView(myContext);
                    SalesTax.setText(Report.getString(Report
                            .getColumnIndex("TotalTaxAmount")));


                    ServiceTax = new TextView(myContext);
                    ServiceTax.setText(Report.getString(Report
                            .getColumnIndex("TotalServiceTaxAmount")));


                    Discount = new TextView(myContext);
                    Discount.setText(Report.getString(Report
                            .getColumnIndex("TotalDiscountAmount")));


                    rowReport.addView(BillDate);//0
                    rowReport.addView(TotalBills);//1
                    rowReport.addView(Discount);//2
                    rowReport.addView(SalesTax);//3
                    rowReport.addView(ServiceTax);//4
                    rowReport.addView(Amount);//5

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isItemExists = false;

            } while (Report.moveToNext());
            int count1 = tblReport.getChildCount();
            for(int i=count;i<count1;i++)
            {
                TableRow row = (TableRow) tblReport.getChildAt(i);
                Discount = (TextView) row.getChildAt(2);
                SalesTax = (TextView) row.getChildAt(3);
                ServiceTax = (TextView) row.getChildAt(4);
                Amount = (TextView) row.getChildAt(5);

                totDis += Float.parseFloat(Discount.getText().toString());
                totSalesTax += Float.parseFloat(SalesTax.getText().toString());
                totServiceTax += Float.parseFloat(ServiceTax.getText().toString());
                totbillAmt += Float.parseFloat(Amount.getText().toString());
            }
            rowReport = new TableRow(myContext);
            rowReport.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowReport.setBackgroundColor(myContext.getResources().getColor(R.color.colorPrimaryLight));

            TextView Date = new TextView(myContext);
            Date.setText("Total");
            Date.setTextColor(Color.WHITE);
            Date.setTextSize(15);

            TotalBills = new TextView(myContext);

            Discount = new TextView(myContext);
            Discount.setText(String.valueOf(totDis));
            Discount.setTextColor(Color.WHITE);
            Discount.setTextSize(15);


            SalesTax = new TextView(myContext);
            SalesTax.setText(String.valueOf(totSalesTax));
            SalesTax.setTextColor(Color.WHITE);
            SalesTax.setTextSize(15);

            ServiceTax = new TextView(myContext);
            ServiceTax.setText(String.valueOf(totServiceTax));
            ServiceTax.setTextColor(Color.WHITE);
            ServiceTax.setTextSize(15);

            Amount = new TextView(myContext);
            Amount.setText(String.valueOf(totbillAmt));
            Amount.setTextColor(Color.WHITE);
            Amount.setTextSize(15);

            rowReport.addView(Date);
            rowReport.addView(TotalBills);
            rowReport.addView(Discount);
            rowReport.addView(SalesTax);
            rowReport.addView(ServiceTax);
            rowReport.addView(Amount);

            tblReport.addView(rowReport,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }


    private void MonthwiseReport() throws NumberFormatException, FormatException {
        DateTime objPrevDate, objNxtDate;

/*Cursor Report = dbReport.getDaywiseReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/


        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy");

        //Create a calendar object that will convert the date and time value in milliseconds to date.




        Cursor Report = dbReport.getDaywiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


        Log.d("DaywiseReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isItemExists = false;

            TextView BillDate, TotalBills, Amount, SalesTax, ServiceTax, Discount;
            TableRow rowReport;
            float totDis =0, totSalesTax =0, totServiceTax =0, totbillAmt =0;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView Date = (TextView) rowItem.getChildAt(0);

                        String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));

//                        Calendar c1 = Calendar.getInstance();
                        Calendar c2 = Calendar.getInstance();

//                        c1.setTimeInMillis(Long.parseLong(Date.getText().toString()));
                        c2.setTimeInMillis(Long.parseLong(dateInMillis));



                        int pre1 = Integer.parseInt(Date.getText().toString());
                        int next1 = c2.get(Calendar.MONTH);

                        if (pre1 == next1) {

                            // Total Bills
                            TextView Bills = (TextView) rowItem.getChildAt(1);
                            Bills.setText(String.valueOf(Integer
                                    .parseInt(Bills.getText().toString()) + 1));

                            // Amount
                            TextView Amt = (TextView) rowItem.getChildAt(5);
                            Amt.setText(String.format("%.2f",
                                    Double.parseDouble(Amt.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("BillAmount"))));


                            // Sales Tax
                            TextView Tax = (TextView) rowItem.getChildAt(3);
                            Tax.setText(String.format("%.2f",
                                    Double.parseDouble(Tax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TotalTaxAmount"))));

                            // Service Tax
                            TextView ServTax = (TextView) rowItem.getChildAt(4);
                            ServTax.setText(String.format("%.2f",
                                    Double.parseDouble(ServTax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TotalServiceTaxAmount"))));

                            // Discount
                            TextView Disc = (TextView) rowItem.getChildAt(2);
                            Disc.setText(String.format("%.2f",
                                    Double.parseDouble(Disc.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TotalDiscountAmount"))));

                            isItemExists = true;
                            break;
                        }
                    }
                }

                if (isItemExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    BillDate = new TextView(myContext);
                    String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                    //SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    /*String dateString = formatter.format(Long.parseLong(dateInMillis));*/
                    Calendar c1 = Calendar.getInstance();
                    c1.setTimeInMillis(Long.parseLong(dateInMillis));
                    int mon = c1.get(Calendar.MONTH);
                    BillDate.setText(String.valueOf(mon));

                    TotalBills = new TextView(myContext);
                    TotalBills.setText("1");

                    Amount = new TextView(myContext);
                    Amount.setText(Report.getString(Report
                            .getColumnIndex("BillAmount")));

                    SalesTax = new TextView(myContext);
                    SalesTax.setText(Report.getString(Report
                            .getColumnIndex("TotalTaxAmount")));

                    ServiceTax = new TextView(myContext);
                    ServiceTax.setText(Report.getString(Report
                            .getColumnIndex("TotalServiceTaxAmount")));

                    Discount = new TextView(myContext);
                    Discount.setText(Report.getString(Report
                            .getColumnIndex("TotalDiscountAmount")));

                    rowReport.addView(BillDate);
                    rowReport.addView(TotalBills);
                    rowReport.addView(Discount);
                    rowReport.addView(SalesTax);
                    rowReport.addView(ServiceTax);
                    rowReport.addView(Amount);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isItemExists = false;

            } while (Report.moveToNext());
            String []months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
            for(int i =1; i<tblReport.getChildCount();i++)
            {
                TableRow row = (TableRow) tblReport.getChildAt(i);
                TextView date = (TextView)row.getChildAt(0);
                int month = Integer.parseInt(date.getText().toString());
                date.setText(months[month]);

                TextView Discount1 = (TextView)row.getChildAt(2);
                TextView SalesTax1 = (TextView)row.getChildAt(3);
                TextView ServiceTax1 = (TextView)row.getChildAt(4);
                TextView Amount1 = (TextView)row.getChildAt(5);
                totbillAmt += Float.parseFloat(Amount1.getText().toString());
                totSalesTax += Float.parseFloat(SalesTax1.getText().toString());
                totServiceTax += Float.parseFloat(ServiceTax1.getText().toString());
                totDis += Float.parseFloat(Discount1.getText().toString());
            }

            rowReport = new TableRow(myContext);
            rowReport.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowReport.setBackgroundColor(myContext.getResources().getColor(R.color.colorPrimaryLight));

            BillDate = new TextView(myContext);
            BillDate.setText("Total");
            BillDate.setTextColor(Color.WHITE);
            BillDate.setTextSize(15);

            TotalBills = new TextView(myContext);


            Discount = new TextView(myContext);
            Discount.setText(String.valueOf(totDis));
            Discount.setTextColor(Color.WHITE);
            Discount.setTextSize(15);


            SalesTax = new TextView(myContext);
            SalesTax.setText(String.valueOf(totSalesTax));
            SalesTax.setTextColor(Color.WHITE);
            SalesTax.setTextSize(15);

            ServiceTax = new TextView(myContext);
            ServiceTax.setText(String.valueOf(totServiceTax));
            ServiceTax.setTextColor(Color.WHITE);
            ServiceTax.setTextSize(15);

            Amount = new TextView(myContext);
            Amount.setText(String.valueOf(totbillAmt));
            Amount.setTextColor(Color.WHITE);
            Amount.setTextSize(15);

            rowReport.addView(BillDate);
            rowReport.addView(TotalBills);
            rowReport.addView(Discount);
            rowReport.addView(SalesTax);
            rowReport.addView(ServiceTax);
            rowReport.addView(Amount);


            tblReport.addView(rowReport,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }







/*
 private void MonthwiseReport() throws NumberFormatException, FormatException {
        DateTime objPrevDate, objNxtDate;

        */
/*Cursor Report = dbReport.getDaywiseReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*//*


        //SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy");

        // Create a calendar object that will convert the date and time value in milliseconds to date.




        Cursor Report = dbReport.getDaywiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


        Log.d("DaywiseReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isItemExists = false;

            TextView BillDate, TotalBills, Amount, SalesTax, ServiceTax, Discount;
            TableRow rowReport;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView Date = (TextView) rowItem.getChildAt(0);

                        objPrevDate = new DateTime(Date.getText().toString());

                        objNxtDate = new DateTime(Report.getString(Report
                                .getColumnIndex("InvoiceDate")));

                        if (objPrevDate.getMonth() == objNxtDate.getMonth()) {

                            // Total Bills
                            TextView Bills = (TextView) rowItem.getChildAt(1);
                            Bills.setText(String.valueOf(Integer
                                    .parseInt(Bills.getText().toString()) + 1));

                            // Amount
                            TextView Amt = (TextView) rowItem.getChildAt(3);
                            Amt.setText(String.format("%.2f",
                                    Double.parseDouble(Amt.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("BillAmount"))));

                            // Sales Tax
                            TextView Tax = (TextView) rowItem.getChildAt(4);
                            Tax.setText(String.format("%.2f",
                                    Double.parseDouble(Tax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TotalTaxAmount"))));

                            // Service Tax
                            TextView ServTax = (TextView) rowItem.getChildAt(5);
                            ServTax.setText(String.format("%.2f",
                                    Double.parseDouble(ServTax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TotalServiceTaxAmount"))));

                            // Discount
                            TextView Disc = (TextView) rowItem.getChildAt(5);
                            Disc.setText(String.format("%.2f",
                                    Double.parseDouble(Disc.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TotalDiscountAmount"))));

                            isItemExists = true;
                            break;
                        }
                    }
                }

                if (isItemExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    BillDate = new TextView(myContext);
                    String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    String dateString = formatter.format(Long.parseLong(dateInMillis));
                    BillDate.setText(dateString);

                    TotalBills = new TextView(myContext);
                    TotalBills.setText("1");

                    Amount = new TextView(myContext);
                    Amount.setText(Report.getString(Report
                            .getColumnIndex("BillAmount")));

                    SalesTax = new TextView(myContext);
                    SalesTax.setText(Report.getString(Report
                            .getColumnIndex("TotalTaxAmount")));

                    ServiceTax = new TextView(myContext);
                    ServiceTax.setText(Report.getString(Report
                            .getColumnIndex("TotalServiceTaxAmount")));

                    Discount = new TextView(myContext);
                    Discount.setText(Report.getString(Report
                            .getColumnIndex("TotalDiscountAmount")));

                    rowReport.addView(BillDate);
                    rowReport.addView(TotalBills);
                    rowReport.addView(Discount);
                    rowReport.addView(SalesTax);
                    rowReport.addView(ServiceTax);
                    rowReport.addView(Amount);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isItemExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }
*/

    private void DepartmentwiseReport() {
        /*Cursor Report = dbReport.getDepartmentwiseReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());
        */

        Cursor Report = dbReport.getDepartmentwiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


        Log.d("DepartmentwiseReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isDeptExists = false;

            TextView DeptCode, DeptName, TotalItems, Discount, SalesTax, ServiceTax, Amount;
            TableRow rowReport;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView Code = (TextView) rowItem.getChildAt(0);

                        if (Code.getText().toString().equalsIgnoreCase(Report.getString(Report
                                .getColumnIndex("DeptCode")))) {

                            // Total Items
                            TextView Items = (TextView) rowItem.getChildAt(2);
                            Items.setText(String.valueOf(Integer
                                    .parseInt(Items.getText().toString()) + Integer
                                    .parseInt(Report.getString(Report.getColumnIndex("Quantity")))));

                            // Discount
                            TextView Disc = (TextView) rowItem.getChildAt(3);
                            Disc.setText(String.format("%.2f",
                                    Double.parseDouble(Disc.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("DiscountAmount"))));

                            // Sales Tax
                            TextView Tax = (TextView) rowItem.getChildAt(4);
                            Tax.setText(String.format("%.2f",
                                    Double.parseDouble(Tax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TaxAmount"))));

                            // Service Tax
                            TextView ServTax = (TextView) rowItem.getChildAt(5);
                            ServTax.setText(String.format("%.2f",
                                    Double.parseDouble(ServTax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("ServiceTaxAmount"))));

                            // Amount
                            TextView Amt = (TextView) rowItem.getChildAt(6);
                            float rate = Float.parseFloat(Report.getString(Report.getColumnIndex("Value")));
                            float quant = Float.parseFloat(Report.getString(Report.getColumnIndex("Quantity")));
                            float amt = Float.parseFloat(Amt.getText().toString());
                            amt += (rate*quant);
                            Amt.setText(String.format("%.2f",(amt)));
                            /*Amt.setText(String.format("%.2f",
                                    Double.parseDouble(Amt.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TaxableValue"))));*/

                            isDeptExists = true;
                            break;
                        }
                    }
                }

                if (isDeptExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    DeptCode = new TextView(myContext);
                    DeptCode.setText(Report.getString(Report
                            .getColumnIndex("DeptCode")));

                    DeptName = new TextView(myContext);
                    DeptName.setText(Report.getString(Report
                            .getColumnIndex("DeptName")));

                    TotalItems = new TextView(myContext);
                    TotalItems.setText(Report.getString(Report
                            .getColumnIndex("Quantity")));

                    Discount = new TextView(myContext);
                    Discount.setText(Report.getString(Report
                            .getColumnIndex("DiscountAmount")));

                    SalesTax = new TextView(myContext);
                    SalesTax.setText(Report.getString(Report
                            .getColumnIndex("TaxAmount")));

                    ServiceTax = new TextView(myContext);
                    ServiceTax.setText(Report.getString(Report
                            .getColumnIndex("ServiceTaxAmount")));

                    Amount = new TextView(myContext);
                    float rate = Float.parseFloat(Report.getString(Report.getColumnIndex("Value")));
                    float quant = Float.parseFloat(Report.getString(Report.getColumnIndex("Quantity")));
                    Amount.setText(String.format("%.2f",(rate*quant)));

                    rowReport.addView(DeptCode);
                    rowReport.addView(DeptName);
                    rowReport.addView(TotalItems);
                    rowReport.addView(Discount);
                    rowReport.addView(SalesTax);
                    rowReport.addView(ServiceTax);
                    rowReport.addView(Amount);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isDeptExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void CategorywiseReport() {
       /* Cursor Report = dbReport.getCategorywiseReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/

        Cursor Report = dbReport.getCategorywiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


        Log.d("CategorywiseReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isCategExists = false;

            TextView CategCode, CategName, TotalItems, Discount, SalesTax, ServiceTax, Amount;
            TableRow rowReport;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView Code = (TextView) rowItem.getChildAt(0);

                        if (Code.getText().toString().equalsIgnoreCase(Report.getString(Report
                                .getColumnIndex("CategCode")))) {

                            // Total Items
                            TextView Items = (TextView) rowItem.getChildAt(2);
                            Items.setText(String.valueOf(Integer
                                    .parseInt(Items.getText().toString()) + Integer.parseInt(Report.getString(Report.getColumnIndex("Quantity")))));

                            // Discount
                            TextView Disc = (TextView) rowItem.getChildAt(3);
                            Disc.setText(String.format("%.2f",
                                    Double.parseDouble(Disc.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("DiscountAmount"))));

                            // Sales Tax
                            TextView Tax = (TextView) rowItem.getChildAt(4);
                            Tax.setText(String.format("%.2f",
                                    Double.parseDouble(Tax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TaxAmount"))));

                            // Service Tax
                            TextView ServTax = (TextView) rowItem.getChildAt(5);
                            ServTax.setText(String.format("%.2f",
                                    Double.parseDouble(ServTax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("ServiceTaxAmount"))));

                            // Amount
                            TextView Amt = (TextView) rowItem.getChildAt(6);
                            float amt_f = Float.parseFloat(Amt.getText().toString());
                            float rate_f = Float.parseFloat(Report.getString(Report.getColumnIndex("Value")));
                            float quant_f = Float.parseFloat(Report.getString(Report.getColumnIndex("Quantity")));
                            amt_f += (rate_f*quant_f);
                            Amt.setText(String.format("%.2f", amt_f));

                            isCategExists = true;
                            break;
                        }
                    }
                }

                if (isCategExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    CategCode = new TextView(myContext);
                    CategCode.setText(Report.getString(Report
                            .getColumnIndex("CategCode")));

                    CategName = new TextView(myContext);
                    CategName.setText(Report.getString(Report
                            .getColumnIndex("CategName")));

                    TotalItems = new TextView(myContext);
                    TotalItems.setText(Report.getString(Report
                            .getColumnIndex("Quantity")));

                    Discount = new TextView(myContext);
                    Discount.setText(Report.getString(Report
                            .getColumnIndex("DiscountAmount")));

                    SalesTax = new TextView(myContext);
                    SalesTax.setText(Report.getString(Report
                            .getColumnIndex("TaxAmount")));

                    ServiceTax = new TextView(myContext);
                    ServiceTax.setText(Report.getString(Report
                            .getColumnIndex("ServiceTaxAmount")));

                    Amount = new TextView(myContext);
                    float rate_f = Float.parseFloat(Report.getString(Report.getColumnIndex("Value")));
                    float quant_f = Float.parseFloat(Report.getString(Report.getColumnIndex("Quantity")));
                    Amount.setText(String.format("%.2f",(rate_f*quant_f)));

                    rowReport.addView(CategCode);
                    rowReport.addView(CategName);
                    rowReport.addView(TotalItems);
                    rowReport.addView(Discount);
                    rowReport.addView(SalesTax);
                    rowReport.addView(ServiceTax);
                    rowReport.addView(Amount);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isCategExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void KitchenwiseReport() {
        /*Cursor Report = dbReport.getKitchenwiseReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/
        Cursor Report = dbReport.getKitchenwiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));

        Log.d("KitchenwiseReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isKitchenExists = false;

            TextView KitchenCode, KitchenName, TotalItems, Discount, SalesTax, ServiceTax, Amount;
            TableRow rowReport;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView Code = (TextView) rowItem.getChildAt(0);

                        if (Code.getText().toString().equalsIgnoreCase(Report.getString(Report
                                .getColumnIndex("KitchenCode")))) {

                            // Total Items
                            TextView Items = (TextView) rowItem.getChildAt(2);
                            Items.setText(String.valueOf(Integer
                                    .parseInt(Items.getText().toString()) + 1));

                            // Discount
                            TextView Disc = (TextView) rowItem.getChildAt(3);
                            Disc.setText(String.format("%.2f",
                                    Double.parseDouble(Disc.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("DiscountAmount"))));

                            // Sales Tax
                            TextView Tax = (TextView) rowItem.getChildAt(4);
                            Tax.setText(String.format("%.2f",
                                    Double.parseDouble(Tax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("TaxAmount"))));

                            // Service Tax
                            TextView ServTax = (TextView) rowItem.getChildAt(5);
                            ServTax.setText(String.format("%.2f",
                                    Double.parseDouble(ServTax.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("ServiceTaxAmount"))));

                            // Amount
                            TextView Amt = (TextView) rowItem.getChildAt(6);
                            Amt.setText(String.format("%.2f",
                                    Double.parseDouble(Amt.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("BillAmount"))));

                            isKitchenExists = true;
                            break;
                        }
                    }
                }

                if (isKitchenExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    KitchenCode = new TextView(myContext);
                    KitchenCode.setText(Report.getString(Report
                            .getColumnIndex("KitchenCode")));

                    KitchenName = new TextView(myContext);
                    KitchenName.setText(Report.getString(Report
                            .getColumnIndex("KitchenName")));

                    TotalItems = new TextView(myContext);
                    TotalItems.setText("1");

                    Discount = new TextView(myContext);
                    Discount.setText(Report.getString(Report
                            .getColumnIndex("DiscountAmount")));

                    SalesTax = new TextView(myContext);
                    SalesTax.setText(Report.getString(Report
                            .getColumnIndex("TaxAmount")));

                    ServiceTax = new TextView(myContext);
                    ServiceTax.setText(Report.getString(Report
                            .getColumnIndex("ServiceTaxAmount")));

                    Amount = new TextView(myContext);
                    Amount.setText(Report.getString(Report
                            .getColumnIndex("BillAmount")));

                    rowReport.addView(KitchenCode);
                    rowReport.addView(KitchenName);
                    rowReport.addView(TotalItems);
                    rowReport.addView(Discount);
                    rowReport.addView(SalesTax);
                    rowReport.addView(ServiceTax);
                    rowReport.addView(Amount);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isKitchenExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void SupplierwiseReport() {

        Cursor Report = dbReport.getSupplierwiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


        Log.d("SupplierwiseReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isSupplierExists = false;

            TextView EmpId, Name, TotalBills, Amount;
            TableRow rowReport;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView Id = (TextView) rowItem.getChildAt(0);

						/*if(Id.getText().toString().equalsIgnoreCase(Report.getString(Report
                                .getColumnIndex("EmployeeId")))){*/
                        if (Integer.parseInt(Id.getText().toString()) == Report.getInt(Report
                                .getColumnIndex("SupplierCode"))) {
                            // Total Bills
                            TextView Bills = (TextView) rowItem.getChildAt(2);
                            Bills.setText(String.valueOf(Integer
                                    .parseInt(Bills.getText().toString()) + 1));

                            // Amount
                            TextView Amt = (TextView) rowItem.getChildAt(3);
                            Amt.setText(String.format("%.2f",
                                    Double.parseDouble(Amt.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("Amount"))));

                            isSupplierExists = true;
                            break;
                        }
                    }
                }

                if (isSupplierExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    EmpId = new TextView(myContext);
                    EmpId.setText(Report.getString(Report
                            .getColumnIndex("SupplierCode")));

                    Name = new TextView(myContext);
                    //Cursor crsrEmployee = dbReport.getUsers(EmpId.getText().toString());
                    //if(crsrEmployee!=null && crsrEmployee.moveToFirst())
                    Name.setText(Report.getString(Report.getColumnIndex("SupplierName")));

                    TotalBills = new TextView(myContext);
                    TotalBills.setText("1");

                    Amount = new TextView(myContext);
                    float amt = Float.parseFloat(Report.getString(Report.getColumnIndex("Amount")));
                    //amt += Float.parseFloat(Report.getString(Report.getColumnIndex("AdditionalChargeAmount")));
                    Amount.setText(String.valueOf(amt));

                    rowReport.addView(EmpId);
                    rowReport.addView(Name);
                    rowReport.addView(TotalBills);
                    rowReport.addView(Amount);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isSupplierExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void WaiterwiseReport() {
        /*Cursor Report = dbReport.getWaiterwiseReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/
        Cursor Report = dbReport.getWaiterwiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


        Log.d("WaiterwiseReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isWaiterExists = false;

            TextView EmpId, Name, TotalBills, Amount;
            TableRow rowReport;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView Id = (TextView) rowItem.getChildAt(0);

						/*if(Id.getText().toString().equalsIgnoreCase(Report.getString(Report
                                .getColumnIndex("EmployeeId")))){*/
                        if (Integer.parseInt(Id.getText().toString()) == Report.getInt(Report
                                .getColumnIndex("EmployeeId"))) {
                            // Total Bills
                            TextView Bills = (TextView) rowItem.getChildAt(2);
                            Bills.setText(String.valueOf(Integer
                                    .parseInt(Bills.getText().toString()) + 1));

                            // Amount
                            TextView Amt = (TextView) rowItem.getChildAt(3);
                            Amt.setText(String.format("%.2f",
                                    Double.parseDouble(Amt.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("BillAmount"))));

                            isWaiterExists = true;
                            break;
                        }
                    }
                }

                if (isWaiterExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    EmpId = new TextView(myContext);
                    EmpId.setText(Report.getString(Report
                            .getColumnIndex("EmployeeId")));

                    Name = new TextView(myContext);
                    Cursor crsrEmployee = dbReport.getUsers(EmpId.getText().toString());
                    if(crsrEmployee!=null && crsrEmployee.moveToFirst())
                        Name.setText(crsrEmployee.getString(crsrEmployee.getColumnIndex("Name")));

                    TotalBills = new TextView(myContext);
                    TotalBills.setText("1");

                    Amount = new TextView(myContext);
                    Amount.setText(Report.getString(Report
                            .getColumnIndex("BillAmount")));

                    rowReport.addView(EmpId);
                    rowReport.addView(Name);
                    rowReport.addView(TotalBills);
                    rowReport.addView(Amount);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isWaiterExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void WaiterDetailedReport() {
        if (txtPersonId.getText().toString().equalsIgnoreCase("")) {
            MsgBox.Show("Warning", "Please Select Waiter");
        } else {
            /*Cursor crsrWaiterDetail = dbReport.getEmployee(Integer
                    .parseInt(txtPersonId.getText().toString()));

            if (crsrWaiterDetail.moveToFirst()) {
                lblPersonName.setText(crsrWaiterDetail.getString(crsrWaiterDetail.getColumnIndex("EmployeeName")));
            } else {
                MsgBox.Show("Warning", "No waiter found for above Id");
                return;
            }*/

            /*Cursor Report = dbReport.getWaiterDetailedReport(Integer
                            .parseInt(txtPersonId.getText().toString()),
                    txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());
            */

            Cursor Report = dbReport.getWaiterDetailedReport(Integer
                            .parseInt(txtPersonId.getText().toString()),
                    String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));



            if (Report.moveToFirst()) {

                TextView Date, BillNumber, TotalItems, Discount, SalesTax, ServiceTax, Amount;
                TableRow rowReport;

                do {
                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams
                            (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    Date = new TextView(myContext);
                    String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    String dateString = formatter.format(Long.parseLong(dateInMillis));
                    Date.setText(dateString);

                    BillNumber = new TextView(myContext);
                    BillNumber.setText(Report.getString(Report.getColumnIndex("InvoiceNo")));

                    TotalItems = new TextView(myContext);
                    TotalItems.setText(Report.getString(Report.getColumnIndex("TotalItems")));

                    Discount = new TextView(myContext);
                    Discount.setText(Report.getString(Report.getColumnIndex("TotalDiscountAmount")));

                    SalesTax = new TextView(myContext);
                    SalesTax.setText(Report.getString(Report.getColumnIndex("TotalTaxAmount")));

                    ServiceTax = new TextView(myContext);
                    ServiceTax.setText(Report.getString(Report.getColumnIndex("TotalServiceTaxAmount")));

                    Amount = new TextView(myContext);
                    Amount.setText(Report.getString(Report.getColumnIndex("BillAmount")));

                    rowReport.addView(Date);
                    rowReport.addView(BillNumber);
                    rowReport.addView(TotalItems);
                    rowReport.addView(Discount);
                    rowReport.addView(SalesTax);
                    rowReport.addView(ServiceTax);
                    rowReport.addView(Amount);

                    tblReport.addView(rowReport,
                            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));

                } while (Report.moveToNext());

                btnPrint.setEnabled(true);
                btnExport.setEnabled(true);

            } else {
                MsgBox.Show("Warning", "No transaction has been done");
            }
        }
    }

    private void RiderwiseReport() {
        /*Cursor Report = dbReport.getRiderwiseReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/

        Cursor Report = dbReport.getRiderwiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));



        Log.d("RiderwiseReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isDeptExists = false;

            TextView EmpId, Name, TotalBills, Amount;
            TableRow rowReport;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView Id = (TextView) rowItem.getChildAt(0);

                        if (Id.getText().toString().equalsIgnoreCase(Report.getString(Report
                                .getColumnIndex("EmployeeId")))) {

                            // Total Bills
                            TextView Bills = (TextView) rowItem.getChildAt(2);
                            Bills.setText(String.valueOf(Integer
                                    .parseInt(Bills.getText().toString()) + 1));

                            // Amount
                            TextView Amt = (TextView) rowItem.getChildAt(3);
                            Amt.setText(String.format("%.2f",
                                    Double.parseDouble(Amt.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("BillAmount"))));

                            isDeptExists = true;
                            break;
                        }
                    }
                }

                if (isDeptExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    EmpId = new TextView(myContext);
                    EmpId.setText(Report.getString(Report
                            .getColumnIndex("EmployeeId")));

                    Name = new TextView(myContext);
                    Name.setText(Report.getString(Report
                            .getColumnIndex("Name")));

                    TotalBills = new TextView(myContext);
                    TotalBills.setText("1");

                    Amount = new TextView(myContext);
                    Amount.setText(Report.getString(Report
                            .getColumnIndex("BillAmount")));

                    rowReport.addView(EmpId);
                    rowReport.addView(Name);
                    rowReport.addView(TotalBills);
                    rowReport.addView(Amount);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isDeptExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void RiderDetailedReport() {
        if (txtPersonId.getText().toString().equalsIgnoreCase("")) {
            MsgBox.Show("Warning", "Please Select Rider");
        } else {
            /*Cursor crsrRiderDetail = dbReport.getEmployee(Integer
                    .parseInt(txtPersonId.getText().toString()));

            if (crsrRiderDetail.moveToFirst()) {
                lblPersonName.setText(crsrRiderDetail.getString(crsrRiderDetail.getColumnIndex("EmployeeName")));
            } else {
                MsgBox.Show("Warning", "No rider found for above Id");
                return;
            }*/

            /*Cursor Report = dbReport.getRiderDetailedReport(Integer
                            .parseInt(txtPersonId.getText().toString()),
                    txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/

            Cursor Report = dbReport.getRiderDetailedReport(Integer
                            .parseInt(txtPersonId.getText().toString()),
                    String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));



            if (Report.moveToFirst()) {

                TextView Date, BillNumber, TotalItems, Discount, SalesTax, ServiceTax, Amount;
                TableRow rowReport;

                do {
                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams
                            (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    Date = new TextView(myContext);
                    String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    String dateString = formatter.format(Long.parseLong(dateInMillis));
                    Date.setText(dateString);

                    BillNumber = new TextView(myContext);
                    BillNumber.setText(Report.getString(Report.getColumnIndex("InvoiceNo")));

                    TotalItems = new TextView(myContext);
                    TotalItems.setText(Report.getString(Report.getColumnIndex("TotalItems")));

                    Discount = new TextView(myContext);
                    Discount.setText(Report.getString(Report.getColumnIndex("TotalDiscountAmount")));

                    SalesTax = new TextView(myContext);
                    SalesTax.setText(Report.getString(Report.getColumnIndex("TotalTaxAmount")));

                    ServiceTax = new TextView(myContext);
                    ServiceTax.setText(Report.getString(Report.getColumnIndex("TotalServiceTaxAmount")));

                    Amount = new TextView(myContext);
                    Amount.setText(Report.getString(Report.getColumnIndex("BillAmount")));

                    rowReport.addView(Date);
                    rowReport.addView(BillNumber);
                    rowReport.addView(TotalItems);
                    rowReport.addView(Discount);
                    rowReport.addView(SalesTax);
                    rowReport.addView(ServiceTax);
                    rowReport.addView(Amount);

                    tblReport.addView(rowReport,
                            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));

                } while (Report.moveToNext());

                btnPrint.setEnabled(true);
                btnExport.setEnabled(true);

            } else {
                MsgBox.Show("Warning", "No transaction has been done");
            }
        }
    }

    private void UserwiseReport() {
        /*Cursor Report = dbReport.getUserwiseReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/

        Cursor Report = dbReport.getUserwiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));



        Log.d("UserwiseReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isDeptExists = false;

            TextView UserId, Name, TotalBills, Amount;
            TableRow rowReport;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView Id = (TextView) rowItem.getChildAt(0);

                        if (Id.getText().toString().equalsIgnoreCase(Report.getString(Report
                                .getColumnIndex("UserId")))) {

                            // Total Bills
                            TextView Bills = (TextView) rowItem.getChildAt(2);
                            Bills.setText(String.valueOf(Integer
                                    .parseInt(Bills.getText().toString()) + 1));

                            // Amount
                            TextView Amt = (TextView) rowItem.getChildAt(3);
                            Amt.setText(String.format("%.2f",
                                    Double.parseDouble(Amt.getText().toString()) +
                                            Report.getDouble(Report.getColumnIndex("BillAmount"))));

                            isDeptExists = true;
                            break;
                        }
                    }
                }

                if (isDeptExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    UserId = new TextView(myContext);
                    UserId.setText(Report.getString(Report
                            .getColumnIndex("UserId")));

                    Name = new TextView(myContext);
                    Name.setText(Report.getString(Report
                            .getColumnIndex("Name")));

                    TotalBills = new TextView(myContext);
                    TotalBills.setText("1");

                    Amount = new TextView(myContext);
                    Amount.setText(Report.getString(Report
                            .getColumnIndex("BillAmount")));

                    rowReport.addView(UserId);
                    rowReport.addView(Name);
                    rowReport.addView(TotalBills);
                    rowReport.addView(Amount);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isDeptExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    /***********************************************************************************************************************************************
     *
     ***********************************************************************************************************************************************/
    private void UserDetailedReport() {
        if (txtPersonId.getText().toString().equalsIgnoreCase("")) {
            MsgBox.Show("Warning", "Please Select User");
        } else {
            /*Cursor crsrUserDetail = dbReport.getUsers(txtPersonId.getText().toString());

            if (crsrUserDetail.moveToFirst()) {
                lblPersonName.setText(crsrUserDetail.getString(crsrUserDetail.getColumnIndex("Name")));
            } else {
                MsgBox.Show("Warning", "No user found for above Id");
                return;
            }*/

            /*Cursor Report = dbReport.getUserDetailedReport(txtPersonId.getText().toString(),
                    txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/
            Cursor Report = dbReport.getUserDetailedReport(txtPersonId.getText().toString(),
                    String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


            if (Report.moveToFirst()) {

                TextView Date, BillNumber, TotalItems, Discount, SalesTax, ServiceTax, Amount;
                TableRow rowReport;

                do {
                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams
                            (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    Date = new TextView(myContext);
                    String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    String dateString = formatter.format(Long.parseLong(dateInMillis));
                    Date.setText(dateString);

                    BillNumber = new TextView(myContext);
                    BillNumber.setText(Report.getString(Report.getColumnIndex("InvoiceNo")));

                    TotalItems = new TextView(myContext);
                    TotalItems.setText(Report.getString(Report.getColumnIndex("TotalItems")));

                    Discount = new TextView(myContext);
                    Discount.setText(Report.getString(Report.getColumnIndex("TotalDiscountAmount")));

                    SalesTax = new TextView(myContext);
                    SalesTax.setText(Report.getString(Report.getColumnIndex("TotalTaxAmount")));

                    ServiceTax = new TextView(myContext);
                    ServiceTax.setText(Report.getString(Report.getColumnIndex("TotalServiceTaxAmount")));

                    Amount = new TextView(myContext);
                    Amount.setText(Report.getString(Report.getColumnIndex("BillAmount")));

                    rowReport.addView(Date);
                    rowReport.addView(BillNumber);
                    rowReport.addView(TotalItems);
                    rowReport.addView(Discount);
                    rowReport.addView(SalesTax);
                    rowReport.addView(ServiceTax);
                    rowReport.addView(Amount);

                    tblReport.addView(rowReport,
                            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));

                } while (Report.moveToNext());

                btnPrint.setEnabled(true);
                btnExport.setEnabled(true);

            } else {
                MsgBox.Show("Warning", "No transaction has been done");
            }
        }
    }

    private void CustomerwiseReport() {
        /*Cursor Report = dbReport.getCustomerwiseReport(txtReportDateStart.getText().toString(),
                txtReportDateEnd.getText().toString());
*/
        Cursor Report = dbReport.getCustomerwiseReport(String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));



        Log.d("CustomerwiseReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {
            boolean isCustomerExists = false;

            TextView CustId, CustName, TotalBills, LastTransaction, TotalTransaction;
            TableRow rowReport;

            do {
                for (int iPosition = 1; iPosition < tblReport.getChildCount(); iPosition++) {

                    TableRow rowItem = (TableRow) tblReport.getChildAt(iPosition);
                    if (rowItem.getChildAt(0) != null) {

                        TextView Id = (TextView) rowItem.getChildAt(0);

                        if (Id.getText().toString().equalsIgnoreCase(Report.getString(Report
                                .getColumnIndex("CustId")))) {

                            // Total Bills
                            TextView Bills = (TextView) rowItem.getChildAt(2);
                            Bills.setText(String.format("%d",
                                    Integer.parseInt(Bills.getText().toString()) + 1));

                            isCustomerExists = true;
                            break;
                        }
                    }
                }

                if (isCustomerExists == false) {

                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    CustId = new TextView(myContext);
                    CustId.setText(Report.getString(Report
                            .getColumnIndex("CustId")));

                    CustName = new TextView(myContext);
                    CustName.setText(Report.getString(Report
                            .getColumnIndex("CustName")));

                    TotalBills = new TextView(myContext);
                    TotalBills.setText("1");

                    LastTransaction = new TextView(myContext);
                    LastTransaction.setText(String.format("%.2f", Report.getDouble(Report
                            .getColumnIndex("LastTransaction"))));

                    TotalTransaction = new TextView(myContext);
                    TotalTransaction.setText(String.format("%.2f", Report.getDouble(Report
                            .getColumnIndex("TotalTransaction"))));

                    rowReport.addView(CustId);
                    rowReport.addView(CustName);
                    rowReport.addView(TotalBills);
                    rowReport.addView(LastTransaction);
                    rowReport.addView(TotalTransaction);

                    tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                isCustomerExists = false;

            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void CustomerDetailedReport() {
        if (txtPersonId.getText().toString().equalsIgnoreCase("")) {
            MsgBox.Show("Warning", "Please Select Customer");
        } else {
            Cursor crsrCustomerDetail = dbReport.getCustomer(Integer
                    .parseInt(txtPersonId.getText().toString()));

            if (crsrCustomerDetail.moveToFirst()) {
                lblPersonName.setText(crsrCustomerDetail.getString(crsrCustomerDetail.getColumnIndex("CustName")));
            } else {
                MsgBox.Show("Warning", "No customer found for above Id");
                return;
            }

            /*Cursor Report = dbReport.getCustomerDetailedReport(Integer
                            .parseInt(txtPersonId.getText().toString()),
                    txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/
            Cursor Report = dbReport.getCustomerDetailedReport(Integer
                            .parseInt(txtPersonId.getText().toString()),
                    String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


            if (Report.moveToFirst()) {

                TextView Date, BillNumber, TotalItems, Discount, SalesTax, ServiceTax, Amount;
                TableRow rowReport;

                do {
                    rowReport = new TableRow(myContext);
                    rowReport.setLayoutParams(new ViewGroup.LayoutParams
                            (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    Date = new TextView(myContext);
                    String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    String dateString = formatter.format(Long.parseLong(dateInMillis));
                    Date.setText(dateString);

                    BillNumber = new TextView(myContext);
                    BillNumber.setText(Report.getString(Report.getColumnIndex("InvoiceNo")));

                    TotalItems = new TextView(myContext);
                    TotalItems.setText(Report.getString(Report.getColumnIndex("TotalItems")));

                    Discount = new TextView(myContext);
                    Discount.setText(Report.getString(Report.getColumnIndex("TotalDiscountAmount")));

                    SalesTax = new TextView(myContext);
                    SalesTax.setText(Report.getString(Report.getColumnIndex("TotalTaxAmount")));

                    ServiceTax = new TextView(myContext);
                    ServiceTax.setText(Report.getString(Report.getColumnIndex("TotalServiceTaxAmount")));

                    Amount = new TextView(myContext);
                    Amount.setText(Report.getString(Report.getColumnIndex("BillAmount")));

                    rowReport.addView(Date);
                    rowReport.addView(BillNumber);
                    rowReport.addView(TotalItems);
                    rowReport.addView(Discount);
                    rowReport.addView(SalesTax);
                    rowReport.addView(ServiceTax);
                    rowReport.addView(Amount);

                    tblReport.addView(rowReport,
                            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));

                } while (Report.moveToNext());

                btnPrint.setEnabled(true);
                btnExport.setEnabled(true);

            } else {
                MsgBox.Show("Warning", "No transaction has been done");
            }
        }
    }

    private void PaymentReport() {
        /*Cursor Report = dbReport.getPaymentReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/
        Cursor Report = dbReport.getPaymentReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));



        Log.d("PaymentReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {

            TextView Date, Description, Amount, Reason;
            TableRow rowReport;
            float totbillAmt=0;

            do {

                rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                Date = new TextView(myContext);
                String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                String dateString = formatter.format(Long.parseLong(dateInMillis));
                Date.setText(dateString);

                Description = new TextView(myContext);
                Description.setText(Report.getString(Report
                        .getColumnIndex("DescriptionText")));

                Reason = new TextView(myContext);
                Reason.setText(Report.getString(Report
                        .getColumnIndex("Reason")));

                Amount = new TextView(myContext);
                Amount.setText(Report.getString(Report
                        .getColumnIndex("Amount")));
                totbillAmt += Float.parseFloat(Amount.getText().toString());

                rowReport.addView(Date);
                rowReport.addView(Description);
                rowReport.addView(Amount);
                rowReport.addView(Reason);

                tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

            } while (Report.moveToNext());

            rowReport = new TableRow(myContext);
            rowReport.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowReport.setBackgroundColor(myContext.getResources().getColor(R.color.colorPrimaryLight));

            Date = new TextView(myContext);
            Date.setText("Total");
            Date.setTextColor(Color.WHITE);
            Date.setTextSize(15);

            Description = new TextView(myContext);
            Reason = new TextView(myContext);

            Amount = new TextView(myContext);
            Amount.setText(String.valueOf(totbillAmt));
            Amount.setTextColor(Color.WHITE);
            Amount.setTextSize(15);

            rowReport.addView(Date);
            rowReport.addView(Description);
            rowReport.addView(Amount);
            rowReport.addView(Reason);

            tblReport.addView(rowReport,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void ReceiptReport() {
        /*Cursor Report = dbReport.getReceiptReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());
*/
        Cursor Report = dbReport.getReceiptReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


        Log.d("ReceiptReport", "Rows Count:" + Report.getCount());

        if (Report.moveToFirst()) {

            TextView Date, Description, Amount, Reason;
            TableRow rowReport;
            float totbillAmt =0;
            do {

                rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                Date = new TextView(myContext);
                String dateInMillis = Report.getString(Report.getColumnIndex("InvoiceDate"));
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                String dateString = formatter.format(Long.parseLong(dateInMillis));
                Date.setText(dateString);

                Description = new TextView(myContext);
                Description.setText(Report.getString(Report
                        .getColumnIndex("DescriptionText")));

                Reason = new TextView(myContext);
                Reason.setText(Report.getString(Report
                        .getColumnIndex("Reason")));

                Amount = new TextView(myContext);
                Amount.setText(Report.getString(Report
                        .getColumnIndex("Amount")));
                totbillAmt += Float.parseFloat(Amount.getText().toString());

                rowReport.addView(Date);
                rowReport.addView(Description);
                rowReport.addView(Amount);
                rowReport.addView(Reason);

                tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

            } while (Report.moveToNext());

            rowReport = new TableRow(myContext);
            rowReport.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rowReport.setBackgroundColor(myContext.getResources().getColor(R.color.colorPrimaryLight));

            Date = new TextView(myContext);
            Date.setText("Total");
            Date.setTextColor(Color.WHITE);
            Date.setTextSize(15);

            Description = new TextView(myContext);
            Reason = new TextView(myContext);

            Amount = new TextView(myContext);
            Amount.setText(String.valueOf(totbillAmt));
            Amount.setTextColor(Color.WHITE);
            Amount.setTextSize(15);

            rowReport.addView(Date);
            rowReport.addView(Description);
            rowReport.addView(Amount);
            rowReport.addView(Reason);

            tblReport.addView(rowReport,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    private void FastSellingItemwiseReport() {
        /*Cursor Report = dbReport.getFastSellingItemwiseReport(
                txtReportDateStart.getText().toString(), txtReportDateEnd.getText().toString());*/
        Cursor Report = dbReport.getFastSellingItemwiseReport(
                String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));


        Log.d("ItemwiseReport", "Rows Count:" + Report.getCount());
        TextView SNo, DeptCode, CategCode, ItemName, SoldQty, Price;
        TableRow rowReport;
        int i = 1;
        if (Report.moveToFirst()) {
            do {
                rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                SNo = new TextView(myContext);
                //SNo.setText(Report.getString(Report.getColumnIndex("ItemNumber")));
                SNo.setText(String.valueOf(i));

                DeptCode = new TextView(myContext);
                int dept = Report.getInt(Report.getColumnIndex("DeptCode"));
                if(dept ==0)
                    DeptCode.setText("-");
                else
                {
                    Cursor dd = dbReport.getDepartment(dept);
                    if(dd!=null && dd.moveToFirst())
                        DeptCode.setText(dd.getString(dd.getColumnIndex("DeptName")));
                    else
                        DeptCode.setText("-");

                }

                CategCode = new TextView(myContext);
                int categ = Report.getInt(Report.getColumnIndex("CategCode"));
                if(categ== 0)
                    CategCode.setText("-");
                else
                {
                    Cursor cc = dbReport.getCategory(categ);
                    if(cc!=null &&  cc.moveToFirst())
                        CategCode.setText(cc.getString(cc.getColumnIndex("CategName")));
                    else
                        CategCode.setText("-");
                }


                ItemName = new TextView(myContext);
                ItemName.setText(Report.getString(Report.getColumnIndex("ItemName")));

                SoldQty = new TextView(myContext);
                SoldQty.setText(Report.getString(Report.getColumnIndex("Qty")));
                SoldQty.setPadding(5,0,0,0);

                Price = new TextView(myContext);
                float rate = Float.parseFloat(Report.getString(Report.getColumnIndex("Value")));
                float quant = Float.parseFloat(SoldQty.getText().toString());
                Price.setText(String.format("%.2f",rate*quant));

                rowReport.addView(SNo);
                rowReport.addView(DeptCode);
                rowReport.addView(CategCode);
                rowReport.addView(ItemName);
                rowReport.addView(SoldQty);
                rowReport.addView(Price);

                tblReport.addView(rowReport, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                i++;
            } while (Report.moveToNext());

            btnPrint.setEnabled(true);
            btnExport.setEnabled(true);

        } else {
            MsgBox.Show("Warning", "No transaction has been done");
        }
    }

    public ArrayList<ArrayList<String>> printReport() {
        //This will iterate through your table layout and get the total amount of cells.
        ArrayList<ArrayList<String>> arrayListRows = new ArrayList<ArrayList<String>>();
        for (int i = 0; i < tblReport.getChildCount(); i++) {
            //Remember that .getChildAt() method returns a View, so you would have to cast a specific control.
            TableRow row = (TableRow) tblReport.getChildAt(i);
            //This will iterate through the table row.
            ArrayList<String> arrayListColumns = new ArrayList<String>();
            for (int j = 0; j < row.getChildCount(); j++) {
                TextView btn = (TextView) row.getChildAt(j);
                String name = btn.getText().toString();
                //String name1 = btn.getText().toString();
                arrayListColumns.add(name);
            }
            arrayListRows.add(arrayListColumns);
        }

        return arrayListRows;
    }

    public void PrintReport() {
        String prf = Preferences.getSharedPreferencesForPrint(getActivity()).getString("report", "--Select--");
        if (prf.equalsIgnoreCase("Sohamsa"))
        {
            ArrayList<ArrayList<String>> list = printReport();
            /*Intent intent = new Intent(getApplicationContext(), PrinterSohamsaActivity.class);
            intent.putExtra("printType", "REPORT");
            intent.putExtra("reportName", strReportName);
            intent.putExtra("printData", list);
            startActivity(intent);*/
            // ((TabbedReportActivity)getActivity()).printSohamsaReport(list,strReportName,"REPORT");
        }
        else if (prf.equalsIgnoreCase("Heyday"))
        {
            ArrayList<ArrayList<String>> list = printReport();
            /*Intent intent = new Intent(getApplicationContext(), PrinterFragment.class);
            intent.putExtra("printType", "REPORT");
            intent.putExtra("reportName", strReportName);
            intent.putExtra("printData", list);
            startActivity(intent);*/
            if(((TabbedReportActivity)getActivity()).isPrinterAvailable)
            {
                ((TabbedReportActivity)getActivity()).printHeydeyReport(list,strReportName,"REPORT");
            }
            else
            {
                ((TabbedReportActivity)getActivity()).askForConfig();
            }
        }
        else
        {
            Toast.makeText(myContext, "Printer not configured", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSpinnerUsers() {
        labelUsers = dbReport.getAllUsersforReport();
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, labelUsers);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spnrUsers.setAdapter(dataAdapter);
    }

    private void loadSpinnerUsers(int RoleId) {
        labelUsers = dbReport.getAllUsersforReport(RoleId);
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, labelUsers);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spnrUsers.setAdapter(dataAdapter);
    }

    private void loadSpinnerCustomers() {
        labelCustomers = dbReport.getAllCustomersforReport();
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, labelCustomers);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spnrCustomers.setAdapter(dataAdapter);
    }


    public void GSTR2_registered( ) {

        /*String StartDate = txtReportDateStart.getText().toString();
        String EndDate =txtReportDateEnd.getText().toString();*/
        try {

            Cursor cursor = dbReport.getInward_taxed(String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));
            if (cursor == null) {
                AlertDialog.Builder MsgBox = new AlertDialog.Builder(myContext);
                MsgBox.setMessage("No data for " + startDate_date + " - " + endDate_date)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            //@Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            } else {
                if (cursor.moveToFirst()) {

                    TextView GSTIN, InvoiceNo, InvoiceDate, SupplyType, HSNCode, Value, TaxableValue;
                    TextView CGSTRate, CGSTAmount, SGSTRate, SGSTAmount, IGSTRate, IGSTAmount, Pos, Eligible, Total_IGST, Tital_CGST, Total_SGST;
                    TextView Total_ITC_CGST, Total_ITC_IGST, Total_ITC_SGST;
                    TextView SubTotal, ITC;

                    int count = 1;
                    TableRow rowcursor;

                        /*String taxationtype = cursor.getString(cursor.getColumnIndex("TaxationType"));
                        if ((suppliertype.equalsIgnoreCase("registered") && taxationtype.equalsIgnoreCase("GST")) ||
                                ((suppliertype.equalsIgnoreCase("unregistered"))&&(revercharge.equalsIgnoreCase("yes"))))*/

                    do {
                        String suppliertype = cursor.getString(cursor.getColumnIndex("SupplierType"));
                        String revercharge = cursor.getString(cursor.getColumnIndex("AttractsReverseCharge"));
                        if (revercharge == null) {
                            revercharge = "no";
                        }

                        if ((suppliertype.equalsIgnoreCase("registered") ||
                                ((suppliertype.equalsIgnoreCase("unregistered")) && (revercharge.equalsIgnoreCase("yes"))))) {

                            rowcursor = new TableRow(myContext);
                            rowcursor.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                            TextView SNo = new TextView(myContext);
                            SNo.setLeft(20);
                            SNo.setText(String.valueOf(count));
                            SNo.setWidth(52);
                            SNo.setTextSize(12);
                            SNo.setBackgroundResource(R.drawable.border);
                            count++;

                            GSTIN = new TextView(myContext);
                            GSTIN.setLeft(10);
                            String gstin = null;
                            gstin = cursor.getString(cursor.getColumnIndex("GSTIN"));
                            if (gstin == null || gstin.equals(""))
                                gstin = cursor.getString(cursor.getColumnIndex("SupplierName"));
                            /*else
                                gstin = gstin + "-" + cursor.getString(cursor.getColumnIndex("SupplierName"));
                            */
                            GSTIN.setText(gstin);
                            GSTIN.setWidth(100);
                            GSTIN.setTextSize(12);
                            GSTIN.setBackgroundResource(R.drawable.border);


                            InvoiceNo = new TextView(myContext);
                            InvoiceNo.setLeft(10);
                            InvoiceNo.setWidth(70);
                            InvoiceNo.setTextSize(12);
                            //Richa to do
                            String no = cursor.getString(cursor.getColumnIndex("InvoiceNo"));
                            no.trim();
                            InvoiceNo.setText(no);
                            InvoiceNo.setBackgroundResource(R.drawable.border);

                            InvoiceDate = new TextView(myContext);
                            InvoiceDate.setLeft(10);
                            InvoiceDate.setWidth(50);

                           /* String date_temp = cursor.getString(cursor.getColumnIndex("InvoiceDate"));
                            date_temp.trim();
                            InvoiceDate.setText(date_temp);*/
                            String dateInMillis = cursor.getString(cursor.getColumnIndex("InvoiceDate"));
                            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                            String date_temp = formatter.format(Long.parseLong(dateInMillis));
                            InvoiceDate.setText(date_temp);
                            InvoiceDate.setTextSize(12);
                            InvoiceDate.setBackgroundResource(R.drawable.border);

                            SupplyType = new TextView(myContext);
                            //SupplyType.setText(cursor.getString(cursor.getColumnIndex("SupplyType")));
                            SupplyType.setLeft(10);
                            SupplyType.setWidth(25);
                            SupplyType.setTextSize(12);
                            SupplyType.setBackgroundResource(R.drawable.border);


                            HSNCode = new TextView(myContext);
                            HSNCode.setLeft(10);
                            HSNCode.setWidth(70);
                            HSNCode.setTextSize(12);
                            HSNCode.setBackgroundResource(R.drawable.border);

                            Value = new TextView(myContext);
                            Value.setText(cursor.getString(cursor.getColumnIndex("Value")));
                            Value.setLeft(10);
                            Value.setWidth(70);
                            Value.setTextSize(12);
                            Value.setBackgroundResource(R.drawable.border);

                            TaxableValue = new TextView(myContext);
                            TaxableValue.setText(cursor.getString(cursor.getColumnIndex("TaxableValue")));
                            TaxableValue.setLeft(10);
                            TaxableValue.setWidth(70);
                            TaxableValue.setTextSize(12);
                            TaxableValue.setBackgroundResource(R.drawable.border);

                            CGSTRate = new TextView(myContext);
                            //                    CGSTRate.setText(cursor.getString(cursor.getColumnIndex("CGSTRate")));
                            CGSTRate.setLeft(10);
                            CGSTRate.setWidth(50);
                            CGSTRate.setTextSize(12);
                            CGSTRate.setBackgroundResource(R.drawable.border);


                            CGSTAmount = new TextView(myContext);
                            CGSTAmount.setText(cursor.getString(cursor.getColumnIndex("CGSTAmount")));
                            CGSTAmount.setLeft(10);
                            CGSTAmount.setWidth(50);
                            CGSTAmount.setTextSize(12);
                            CGSTAmount.setBackgroundResource(R.drawable.border);


                            SGSTRate = new TextView(myContext);
                            //                SGSTRate.setText(cursor.getString(cursor.getColumnIndex("SGSTRate")));
                            SGSTRate.setLeft(10);
                            SGSTRate.setWidth(50);
                            SGSTRate.setTextSize(12);
                            SGSTRate.setBackgroundResource(R.drawable.border);

                            SGSTAmount = new TextView(myContext);
                            SGSTAmount.setText(cursor.getString(cursor.getColumnIndex("SGSTAmount")));
                            SGSTAmount.setLeft(10);
                            SGSTAmount.setWidth(50);
                            SGSTAmount.setTextSize(12);
                            SGSTAmount.setBackgroundResource(R.drawable.border);

                            IGSTRate = new TextView(myContext);
                            //            IGSTRate.setText(cursor.getString(cursor.getColumnIndex("IGSTRate")));
                            IGSTRate.setLeft(10);
                            IGSTRate.setWidth(50);
                            IGSTRate.setTextSize(12);
                            IGSTRate.setBackgroundResource(R.drawable.border);

                            IGSTAmount = new TextView(myContext);
                            IGSTAmount.setText(cursor.getString(cursor.getColumnIndex("IGSTAmount")));
                            IGSTAmount.setLeft(10);
                            IGSTAmount.setWidth(50);
                            IGSTAmount.setTextSize(12);
                            IGSTAmount.setBackgroundResource(R.drawable.border);

                            SubTotal = new TextView(myContext);
                            SubTotal.setText(cursor.getString(cursor.getColumnIndex("SubTotal")));
                            SubTotal.setLeft(10);
                            SubTotal.setWidth(50);
                            SubTotal.setTextSize(12);
                            SubTotal.setBackgroundResource(R.drawable.border);


                            Pos = new TextView(myContext);
                            Pos.setText(cursor.getString(cursor.getColumnIndex("POS")));
                            Pos.setLeft(10);
                            Pos.setWidth(60);
                            Pos.setTextSize(12);
                            Pos.setBackgroundResource(R.drawable.border);

                            ITC = new TextView(myContext);
                            float igstamt = 0, cgstamt = 0, sgstamt = 0;
                            String igstamount = cursor.getString(cursor.getColumnIndex("IGSTAmount"));
                            String cgstamount = cursor.getString(cursor.getColumnIndex("CGSTAmount"));
                            String sgstamount = cursor.getString(cursor.getColumnIndex("SGSTAmount"));
                            if (igstamount == null || igstamount.equals(""))
                                igstamt = 0;
                            else
                                igstamt = Float.parseFloat(igstamount);

                            if (cgstamount == null || cgstamount.equals(""))
                                cgstamt = 0;
                            else
                                cgstamt = Float.parseFloat(cgstamount);
                            if (sgstamount == null || sgstamount.equals(""))
                                sgstamt = 0;
                            else
                                sgstamt = Float.parseFloat(sgstamount);

                            float itc = igstamt + cgstamt + sgstamt;
                            ITC.setText(String.valueOf(itc));
                            ITC.setLeft(10);
                            ITC.setWidth(60);
                            ITC.setTextSize(12);
                            ITC.setBackgroundResource(R.drawable.border);


                            //rowcursor.addView(SNo);
                            rowcursor.addView(GSTIN);
                            rowcursor.addView(InvoiceNo);
                            rowcursor.addView(InvoiceDate);
                            rowcursor.addView(SupplyType);
                            rowcursor.addView(HSNCode);
                            rowcursor.addView(Value);
                            rowcursor.addView(TaxableValue);
                            rowcursor.addView(IGSTRate);
                            rowcursor.addView(IGSTAmount);
                            rowcursor.addView(CGSTRate);
                            rowcursor.addView(CGSTAmount);
                            rowcursor.addView(SGSTRate);
                            rowcursor.addView(SGSTAmount);
                            rowcursor.addView(SubTotal);
                            rowcursor.addView(Pos);
                            rowcursor.addView(ITC);

                            View v1 = new View(getActivity());
                            v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                            v1.setBackgroundColor(getResources().getColor(R.color.orange));
                            tblReport.addView(v1);
                            tblReport.addView(rowcursor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


                            View v = new View(getActivity());
                            v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                            v.setBackgroundColor(getResources().getColor(R.color.orange));
                            tblReport.addView(v);
                            String gstin1 = cursor.getString(cursor.getColumnIndex("GSTIN"));
                            String sup_name = cursor.getString(cursor.getColumnIndex("SupplierName"));
                            add_gstr2_items(no, date_temp, gstin1, sup_name, suppliertype);
                            View v11 = new View(getActivity());
                            v11.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 5));
                            v11.setBackgroundColor(getResources().getColor(R.color.orange));
                            tblReport.addView(v11);

                        }

                    } while (cursor.moveToNext()) ;

                }
            }
        }// end try
        catch (Exception e) {
            AlertDialog.Builder MsgBox = new AlertDialog.Builder(myContext);
            MsgBox.setMessage(e.getMessage())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        //@Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }


    }

    public void add_gstr2_items( String No, String Date,String gstin, String supplierName, String supplierType )
    {
        try {


            Cursor cursor = dbReport.getitems_inward_taxed(No, Date,gstin, supplierName, supplierType);
            if (cursor == null) {
                AlertDialog.Builder  MsgBox = new AlertDialog.Builder(myContext);
                MsgBox.setMessage("No items for Invoice No : " + No + " & Invoice Date : " + Date)
                        .setPositiveButton("OK", null)
                        .show();
            } else {


                if (cursor.moveToFirst()) {

                    TextView GSTIN, InvoiceNo, InvoiceDate, SupplyType, HSNCode, Value, TaxableValue;
                    TextView CGSTRate, CGSTAmount, SGSTRate, SGSTAmount, IGSTRate, IGSTAmount, Pos, Eligible;
                    TextView Total_ITC_IGST, Total_ITC_CGST, Total_ITC_SGST;
                    TextView SubTotal, ITC;

                    int count = 1;
                    TableRow rowcursor;

                    do {
                        rowcursor = new TableRow(myContext);
                        rowcursor.setLayoutParams(new TableRow.LayoutParams
                                (TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                        TextView SNo = new TextView(myContext);
                        SNo.setLeft(20);
                        SNo.setText(String.valueOf(count));
                        SNo.setWidth(52);
                        SNo.setTextSize(12);
                        SNo.setBackgroundResource(R.drawable.border_item);
                        count++;

                        GSTIN = new TextView(myContext);
                        GSTIN.setLeft(10);
                        // GSTIN.setText("GSTIN12345678AB");
                        GSTIN.setWidth(100);
                        GSTIN.setTextSize(12);
                        GSTIN.setBackgroundResource(R.drawable.border_item);


                        InvoiceNo = new TextView(myContext);
                        InvoiceNo.setLeft(10);
                        InvoiceNo.setWidth(70);
                        InvoiceNo.setTextSize(12);
                        InvoiceNo.setBackgroundResource(R.drawable.border_item);

                        InvoiceDate = new TextView(myContext);
                        InvoiceDate.setLeft(10);
                        InvoiceDate.setWidth(50);
                        /*String date_temp = cursor.getString(cursor.getColumnIndex("InvoiceDate"));
                        date_temp.trim();*/
                        InvoiceDate.setTextSize(12);
                        InvoiceDate.setBackgroundResource(R.drawable.border_item);

                        SupplyType = new TextView(myContext);
                        SupplyType.setText(cursor.getString(cursor.getColumnIndex("SupplyType")));
                        SupplyType.setLeft(10);
                        SupplyType.setWidth(25);
                        SupplyType.setTextSize(12);
                        SupplyType.setBackgroundResource(R.drawable.border_item);

                        HSNCode = new TextView(myContext);
                        //HSNCode.setText(cursor.getString(cursor.getColumnIndex("HSNCode")));
                        String HSN = cursor.getString(cursor.getColumnIndex("HSNCode"));
                        String desc = cursor.getString(cursor.getColumnIndex("ItemName"));
                        if(HSN==null || HSN.equals(""))
                        {
                            HSN= "_"+desc;
                        }
                        //HSN = HSN + "-" + desc;
                        HSNCode.setText(HSN);
                        HSNCode.setLeft(10);
                        HSNCode.setWidth(70);
                        HSNCode.setTextSize(12);
                        HSNCode.setBackgroundResource(R.drawable.border_item);

                        Value = new TextView(myContext);
                        Value.setText(cursor.getString(cursor.getColumnIndex("Value")));
                        Value.setLeft(10);
                        Value.setWidth(70);
                        Value.setTextSize(12);
                        Value.setBackgroundResource(R.drawable.border_item);

                        TaxableValue = new TextView(myContext);
                        TaxableValue.setText(cursor.getString(cursor.getColumnIndex("TaxableValue")));
                        TaxableValue.setLeft(10);
                        TaxableValue.setWidth(70);
                        TaxableValue.setTextSize(12);
                        TaxableValue.setBackgroundResource(R.drawable.border_item);

                        CGSTRate = new TextView(myContext);
                        CGSTRate.setText(cursor.getString(cursor.getColumnIndex("CGSTRate")));
                        CGSTRate.setLeft(10);
                        CGSTRate.setWidth(50);
                        CGSTRate.setTextSize(12);
                        CGSTRate.setBackgroundResource(R.drawable.border_item);


                        CGSTAmount = new TextView(myContext);
                        CGSTAmount.setText(cursor.getString(cursor.getColumnIndex("CGSTAmount")));
                        CGSTAmount.setLeft(10);
                        CGSTAmount.setWidth(50);
                        CGSTAmount.setTextSize(12);
                        CGSTAmount.setBackgroundResource(R.drawable.border_item);


                        SGSTRate = new TextView(myContext);
                        SGSTRate.setText(cursor.getString(cursor.getColumnIndex("SGSTRate")));
                        SGSTRate.setLeft(10);
                        SGSTRate.setWidth(50);
                        SGSTRate.setTextSize(12);
                        SGSTRate.setBackgroundResource(R.drawable.border_item);

                        SGSTAmount = new TextView(myContext);
                        SGSTAmount.setText(cursor.getString(cursor.getColumnIndex("SGSTAmount")));
                        SGSTAmount.setLeft(10);
                        SGSTAmount.setWidth(50);
                        SGSTAmount.setTextSize(12);
                        SGSTAmount.setBackgroundResource(R.drawable.border_item);

                        IGSTRate = new TextView(myContext);
                        IGSTRate.setText(cursor.getString(cursor.getColumnIndex("IGSTRate")));
                        IGSTRate.setLeft(10);
                        IGSTRate.setWidth(50);
                        IGSTRate.setTextSize(12);
                        IGSTRate.setBackgroundResource(R.drawable.border_item);

                        IGSTAmount = new TextView(myContext);
                        IGSTAmount.setText(cursor.getString(cursor.getColumnIndex("IGSTAmount")));
                        IGSTAmount.setLeft(10);
                        IGSTAmount.setWidth(50);
                        IGSTAmount.setTextSize(12);
                        IGSTAmount.setBackgroundResource(R.drawable.border_item);

                        SubTotal = new TextView(myContext);
                        float subtotal = Float.parseFloat(cursor.getString(cursor.getColumnIndex("SubTotal")));
                        SubTotal.setText(String.format("%.2f",subtotal));
                        SubTotal.setLeft(10);
                        SubTotal.setWidth(60);
                        SubTotal.setTextSize(12);
                        SubTotal.setBackgroundResource(R.drawable.border_item);

                        Pos = new TextView(myContext);
                        //Pos.setText(cursor.getString(cursor.getColumnIndex("POS")));
                        Pos.setLeft(10);
                        Pos.setWidth(60);
                        Pos.setTextSize(12);
                        Pos.setBackgroundResource(R.drawable.border_item);

                        ITC = new TextView(myContext);
                        float itc = Float.parseFloat(IGSTAmount.getText().toString()) +
                                Float.parseFloat(CGSTAmount.getText().toString()) +
                                Float.parseFloat(SGSTAmount.getText().toString());
                        ITC.setText(String.valueOf(itc));
                        ITC.setLeft(10);
                        ITC.setWidth(60);
                        ITC.setTextSize(12);
                        ITC.setBackgroundResource(R.drawable.border_item);


                        //rowcursor.addView(SNo);
                        rowcursor.addView(GSTIN);
                        rowcursor.addView(InvoiceNo);
                        rowcursor.addView(InvoiceDate);
                        rowcursor.addView(SupplyType);
                        rowcursor.addView(HSNCode);
                        rowcursor.addView(Value);
                        rowcursor.addView(TaxableValue);
                        rowcursor.addView(IGSTRate);
                        rowcursor.addView(IGSTAmount);
                        rowcursor.addView(CGSTRate);
                        rowcursor.addView(CGSTAmount);
                        rowcursor.addView(SGSTRate);
                        rowcursor.addView(SGSTAmount);
                        rowcursor.addView(SubTotal);
                        rowcursor.addView(Pos);
                        rowcursor.addView(ITC);


                        tblReport.addView(rowcursor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        //add a new line to the TableLayout:
                        /*final View vline = new View(myContext);
                        vline.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 2));
                        tblReport.addView(vline);*/


                    } while (cursor.moveToNext());

                }
            }
        }
        catch (Exception e)
        {
            AlertDialog.Builder MsgBox = new AlertDialog.Builder(myContext);
            MsgBox .setTitle("Error while fetching items details")
                    .setMessage(e.getMessage())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        //@Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }

    }

    public void GSTR2_unregistered( ) {

        /*String StartDate = txtReportDateStart.getText().toString();
        String EndDate =txtReportDateEnd.getText().toString();*/
        String HSNEnable = "0";
        ArrayList<B2Csmall> datalist_s = new ArrayList<B2Csmall>();
        try
        {
            Cursor billsettingcursor = dbReport.getBillSetting();
            if (billsettingcursor != null && billsettingcursor.moveToFirst())
            {
                GSTEnable = billsettingcursor.getString(billsettingcursor.getColumnIndex("GSTEnable"));
                if (GSTEnable !=null && GSTEnable.equals("1"))
                {
                    HSNEnable = billsettingcursor.getString(billsettingcursor.getColumnIndex("HSNCode"));
                    if (HSNEnable==null)
                    {
                        HSNEnable= "0";
                    }
                }
            }


            Cursor cursor = dbReport.getInward_taxed(String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));
            if (cursor == null )
            {
                AlertDialog.Builder MsgBox = new AlertDialog.Builder(myContext);
                MsgBox. setMessage("No data for "+startDate_date+" - "+endDate_date)
                        .setPositiveButton("OK",null)
                        .show();
            }
            else {

                if (cursor.moveToFirst()) {

                    do {
                        String suppliertype = cursor.getString(cursor.getColumnIndex("SupplierType"));
                        String revercharge = cursor.getString(cursor.getColumnIndex("AttractsReverseCharge"));
                        String pos_str = cursor.getString(cursor.getColumnIndex("POS"));
                        if (pos_str == null)
                        {
                            pos_str = "";
                        }

                        if (revercharge == null) {
                            revercharge = "no";
                        }

                        if ((suppliertype.equalsIgnoreCase("registered") ||
                                ((suppliertype.equalsIgnoreCase("unregistered")) && (revercharge.equalsIgnoreCase("yes"))))) {
                            //  B2b inward supplies
                            // do nothing
                        }

                        else { // table 8

                            String suppliername_str = cursor.getString(cursor.getColumnIndex("SupplierName"));
                            String InvNo = cursor.getString(cursor.getColumnIndex("InvoiceNo"));
                            /*String InvDate = cursor.getString(cursor.getColumnIndex("InvoiceDate"));*/
                            String dateInMillis = cursor.getString(cursor.getColumnIndex("InvoiceDate"));
                            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                            String InvDate = formatter.format(Long.parseLong(dateInMillis));

                            Cursor  billitemcursor = dbReport.getitems_inward_untaxed(InvNo, InvDate,suppliername_str, suppliertype);
                            if (billitemcursor.moveToFirst())
                            {
                                B2Csmall obj;// = new B2Csmall();

                                do {
                                    int found_in_list = 0;
                                    //if (datalist_s.size() >0){

                                    for (B2Csmall s : datalist_s) {
                                        String hsn_temp = billitemcursor.getString(billitemcursor.getColumnIndex("HSNCode"));
                                        if (hsn_temp == null) {
                                            hsn_temp = "";
                                        }
                                        String pos_temp = cursor.getString(cursor.getColumnIndex("POS"));
                                        String description_temp = "";
                                        if (pos_temp == null || pos_temp.equals(""))
                                            description_temp = "IntraState";
                                        else
                                            description_temp = "InterState";

                                        if (s.getHSNCode().equals(hsn_temp) && s.getDescription().equalsIgnoreCase(description_temp)) {
                                            // found in list
                                            float taxableval_bill = Float.parseFloat(billitemcursor.getString(billitemcursor.getColumnIndex("TaxableValue")));

                                            if (suppliertype.equalsIgnoreCase("compounding")) {
                                                float taxableval_listitem = s.getUnregisteredValue();
                                                s.setCompoundingValue(taxableval_bill+taxableval_listitem);
                                            } else if (suppliertype.equalsIgnoreCase("unregistered")) {

                                                String taxationtype_str = billitemcursor.getString(billitemcursor.getColumnIndex("TaxationType"));
                                                if (taxationtype_str == null) {
                                                    taxationtype_str = "";
                                                }
                                                if (taxationtype_str.equalsIgnoreCase("Exempt")) {
                                                    float taxableval_listitem = s.getExemptedValue();
                                                    s.setExemptedValue(taxableval_bill+taxableval_listitem);
                                                } else if (taxationtype_str.equalsIgnoreCase("NilRate")) {
                                                    float taxableval_listitem = s.getNilRatedValue();
                                                    s.setNilRatedValue(taxableval_bill+taxableval_listitem);
                                                } else if (taxationtype_str.equalsIgnoreCase("NonGST")) {
                                                    float taxableval_listitem = s.getNonGSTValue();
                                                    s.setNonGSTValue(taxableval_bill+taxableval_listitem);
                                                }else
                                                {
                                                    float taxableval_listitem = s.getUnregisteredValue();
                                                    s.setUnregisteredValue(taxableval_bill+taxableval_listitem);
                                                }
                                            }

                                            found_in_list = 1;
                                            break;
                                        }
                                    }
                                    //}
                                    if (found_in_list ==0){
                                        obj = new B2Csmall();
                                        String hsn_str = billitemcursor.getString(billitemcursor.getColumnIndex("HSNCode"));
                                        if (hsn_str == null)
                                        {
                                            hsn_str="";
                                        }
                                        if (pos_str.equals("")) {
                                            obj.setDescription("IntraState");
                                        }
                                        else {
                                            obj.setDescription("InterState");
                                        }
                                        obj.setHSNCode(hsn_str);
                                        if (suppliertype.equalsIgnoreCase("compounding"))
                                        {
                                            obj.setCompoundingValue(Float.parseFloat(billitemcursor.getString(billitemcursor.getColumnIndex("TaxableValue"))));
                                        }else if (suppliertype.equalsIgnoreCase("unregistered"))
                                        {
                                            obj.setUnregisteredValue(Float.parseFloat(billitemcursor.getString(billitemcursor.getColumnIndex("TaxableValue"))));
                                        } else if (suppliertype.equalsIgnoreCase("others"))
                                        {
                                            String taxationtype_str = billitemcursor.getString(billitemcursor.getColumnIndex("TaxationType"));
                                            if (taxationtype_str== null)
                                            {
                                                taxationtype_str="";
                                            }
                                            if (taxationtype_str.equalsIgnoreCase("Exempt"))
                                            {
                                                obj.setExemptedValue(Float.parseFloat(billitemcursor.getString(billitemcursor.getColumnIndex("TaxableValue"))));
                                            }else if (taxationtype_str.equalsIgnoreCase("NilRate"))
                                            {
                                                obj.setNilRatedValue(Float.parseFloat(billitemcursor.getString(billitemcursor.getColumnIndex("TaxableValue"))));
                                            } else if (taxationtype_str.equalsIgnoreCase("NonGST"))
                                            {
                                                obj.setNonGSTValue(Float.parseFloat(billitemcursor.getString(billitemcursor.getColumnIndex("TaxableValue"))));
                                            }

                                        }
                                        datalist_s.add(obj);

                                    } // end of new entry

                                }while(billitemcursor.moveToNext());
                            }



                        }

                    } while (cursor.moveToNext()) ;

                    // display content
                    int count =1;
                    for (B2Csmall s : datalist_s)
                    {
                        TableRow rowcursor = new TableRow(myContext);
                        rowcursor.setLayoutParams(new TableRow.LayoutParams
                                (TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));



                        TextView Description = new TextView(myContext);
                        if (s.getDescription().equalsIgnoreCase("IntraState")) {
                            Description.setText("Intrastate Supplies");
                            Description.setBackgroundResource(R.color.green);
                        } else {
                            Description.setText("InterState Supplies");
                            Description.setBackgroundResource(R.color.greenyellow);
                        }
                        Description.setLeft(10);
                        Description.setWidth(150);
                        Description.setTextSize(12);


                        TextView SNo = new TextView(myContext);
                        SNo.setLeft(10);
                        SNo.setText(String.valueOf(count));
                        SNo.setWidth(100);
                        SNo.setTextSize(12);
                        SNo.setBackgroundResource(R.drawable.border);
                        count++;

                        TextView HSNCode = new TextView(myContext);
                        HSNCode.setText(s.getHSNCode());
                        HSNCode.setLeft(10);
                        HSNCode.setWidth(100);
                        HSNCode.setTextSize(12);
                        HSNCode.setBackgroundResource(R.drawable.border);

                        TextView Compounding = new TextView(myContext);
                        Compounding.setText(String.valueOf(s.getCompoundingValue()));
                        Compounding.setLeft(10);
                        Compounding.setWidth(100);
                        Compounding.setTextSize(12);
                        Compounding.setBackgroundResource(R.drawable.border);

                        TextView Unregistered = new TextView(myContext);
                        Unregistered.setText(String.valueOf(s.getUnregisteredValue()));
                        Unregistered.setLeft(10);
                        Unregistered.setWidth(100);
                        Unregistered.setTextSize(12);
                        Unregistered.setBackgroundResource(R.drawable.border);

                        TextView Exempt = new TextView(myContext);
                        Exempt.setText(String.valueOf(s.getExemptedValue()));
                        Exempt.setLeft(10);
                        Exempt.setWidth(100);
                        Exempt.setTextSize(12);
                        Exempt.setBackgroundResource(R.drawable.border);

                        TextView Nil = new TextView(myContext);
                        Nil.setText(String.valueOf(s.getNilRatedValue()));
                        Nil.setLeft(10);
                        Nil.setWidth(100);
                        Nil.setTextSize(12);
                        Nil.setBackgroundResource(R.drawable.border);

                        TextView NonGst = new TextView(myContext);
                        NonGst.setText(String.valueOf(s.getNonGSTValue()));
                        NonGst.setLeft(10);
                        NonGst.setWidth(100);
                        NonGst.setTextSize(12);
                        NonGst.setBackgroundResource(R.drawable.border);

                        rowcursor.addView(SNo);
                        rowcursor.addView(Description);
                        rowcursor.addView(HSNCode);
                        rowcursor.addView(Compounding);
                        rowcursor.addView(Unregistered);
                        rowcursor.addView(Exempt);
                        rowcursor.addView(Nil);
                        rowcursor.addView(NonGst);

                        View v1 = new View(getActivity());
                        v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                        v1.setBackgroundColor(getResources().getColor(R.color.orange));
                        tblReport.addView(v1);
                        tblReport.addView(rowcursor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


                        View v = new View(getActivity());
                        v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                        v.setBackgroundColor(getResources().getColor(R.color.orange));
                        tblReport.addView(v);
                    }// end of for

                }
            }
        }// end try
        catch(Exception e)
        {
            AlertDialog.Builder MsgBox = new AlertDialog.Builder(myContext);
            MsgBox. setMessage(e.getMessage())
                    .setPositiveButton("OK",null)
                    .show();
        }
    }


    void GSTR1_B2B()
    {
        String GSTEnable="0", POSEnable="0",HSNEnable="0",ReverseChargeEnabe="0";
        try
        {
            Cursor billsettingcursor = dbReport.getBillSetting();
            if (billsettingcursor!=null && billsettingcursor.moveToFirst())
            {
                GSTEnable = billsettingcursor.getString(billsettingcursor.getColumnIndex("GSTEnable"));
                if (GSTEnable == null )
                {
                    GSTEnable="0";
                }
                else if (GSTEnable.equals("1"))
                {
                    HSNEnable= billsettingcursor.getString(billsettingcursor.getColumnIndex("HSNCode_Out"));
                    if (HSNEnable==null)
                    {
                        HSNEnable="0";
                    }
                    POSEnable = billsettingcursor.getString(billsettingcursor.getColumnIndex("POS_Out"));
                    if (POSEnable== null)
                    {
                        POSEnable= "0";
                    }
                    ReverseChargeEnabe = billsettingcursor.getString(billsettingcursor.getColumnIndex("ReverseCharge_Out"));
                    if (ReverseChargeEnabe == null)
                    {
                        ReverseChargeEnabe="0";
                    }
                }
            }
           /* String StartDate = txtReportDateStart.getText().toString();
            String EndDate = txtReportDateEnd.getText().toString();*/
            Cursor cursor = dbReport.getOutwardB2b(String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));
            if (cursor == null )
            {
                //MsgBox = new AlertDialog.Builder(myContext);
                MsgBox. setMessage("No data for entered period B2B")
                        .setPositiveButton("OK", null)
                        .show();
            }
            else {
                if (cursor.moveToFirst()) {

                    TextView GSTIN, InvoiceNo, InvoiceDate, SupplyType, Value, HSNCode,GSTRate, Description, Price, Quantity,Units,SubValue,DiscountRate,TaxableValue;
                    TextView CGSTRate, CGSTAmount, SGSTRate, SGSTAmount,IGSTRate, IGSTAmount,AdditionalChargeName,AdditionalChargeAmount;

                    int count =1;
                    TableRow rowcursor;

                    do {
                        rowcursor = new TableRow(myContext);
                        rowcursor.setLayoutParams(new TableRow.LayoutParams
                                (TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                        TextView SNo = new TextView(myContext);
                        SNo.setLeft(10);
                        SNo.setText(String.valueOf(count));
                        SNo.setWidth(42);
                        SNo.setGravity(Gravity.CENTER);
                        SNo.setTextSize(12);
                        SNo.setBackgroundResource(R.drawable.border);
                        count++;

                        GSTIN = new TextView(myContext);
                        GSTIN.setLeft(10);
                        GSTIN.setText(cursor.getString(cursor.getColumnIndex("GSTIN")));
                        GSTIN.setWidth(155);
                        GSTIN.setTextSize(12);
                        GSTIN.setBackgroundResource(R.drawable.border);

                        InvoiceNo = new TextView(myContext);
                        InvoiceNo.setLeft(10);
                        InvoiceNo.setWidth(70);
                        InvoiceNo.setTextSize(12);
                        String no = cursor.getString(cursor.getColumnIndex("InvoiceNo"));
                        no.trim();
                        InvoiceNo.setText(no);
                        InvoiceNo.setBackgroundResource(R.drawable.border);

                        InvoiceDate = new TextView(myContext);
                        InvoiceDate.setLeft(10);
                        InvoiceDate.setWidth(60);
                        String dateInMillis = cursor.getString(cursor.getColumnIndex("InvoiceDate"));
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                        String dateString = formatter.format(Long.parseLong(dateInMillis));
                        InvoiceDate.setText(dateString);
                        InvoiceDate.setTextSize(12);
                        InvoiceDate.setBackgroundResource(R.drawable.border);

                        SupplyType = new TextView(myContext);
                        //SupplyType.setText(cursor.getString(cursor.getColumnIndex("SupplyType")));
                        SupplyType.setLeft(10);
                        SupplyType.setWidth(25);
                        SupplyType.setTextSize(12);
                        SupplyType.setBackgroundResource(R.drawable.border);



                        HSNCode = new TextView(myContext);
                        //HSNCode.setText(cursor.getString(cursor.getColumnIndex("HSNCode")));
                        HSNCode.setLeft(10);
                        HSNCode.setWidth(70);
                        HSNCode.setTextSize(12);
                        HSNCode.setBackgroundResource(R.drawable.border);


                        Value = new TextView(myContext);
                        //Value.setText(cursor.getString(cursor.getColumnIndex("Value")));
                        Value.setLeft(10);
                        Value.setWidth(100);
                        Value.setTextSize(12);
                        Value.setBackgroundResource(R.drawable.border);

                        Quantity = new TextView(myContext);
                        //Quantity.setText(cursor.getString(cursor.getColumnIndex("Value")));
                        Quantity.setLeft(10);
                        Quantity.setWidth(50);
                        Quantity.setTextSize(12);
                        Quantity.setBackgroundResource(R.drawable.border);


                        TaxableValue = new TextView(myContext);
                        TaxableValue.setText(cursor.getString(cursor.getColumnIndex("TaxableValue")));
                        TaxableValue.setLeft(10);
                        TaxableValue.setWidth(100);
                        TaxableValue.setTextSize(12);
                        TaxableValue.setBackgroundResource(R.drawable.border);

                        GSTRate = new TextView(myContext);
                        GSTRate.setLeft(10);
                        GSTRate.setWidth(60);
                        GSTRate.setTextSize(12);
                        GSTRate.setBackgroundResource(R.drawable.border);


                        CGSTAmount = new TextView(myContext);
                        CGSTAmount.setText(cursor.getString(cursor.getColumnIndex("CGSTAmount")));
                        CGSTAmount.setLeft(10);
                        CGSTAmount.setWidth(60);
                        CGSTAmount.setTextSize(12);
                        CGSTAmount.setBackgroundResource(R.drawable.border);


                       /* SGSTRate = new TextView(myContext);
                        SGSTRate.setText(cursor.getString(cursor.getColumnIndex("SGSTRate")));
                        SGSTRate.setLeft(10);
                        SGSTRate.setWidth(60);
                        SGSTRate.setTextSize(12);
                        SGSTRate.setBackgroundResource(R.drawable.border);*/

                        SGSTAmount = new TextView(myContext);
                        SGSTAmount.setText(cursor.getString(cursor.getColumnIndex("SGSTAmount")));
                        SGSTAmount.setLeft(10);
                        SGSTAmount.setWidth(60);
                        SGSTAmount.setTextSize(12);
                        SGSTAmount.setBackgroundResource(R.drawable.border);

                        /*IGSTRate = new TextView(myContext);
                        IGSTRate.setText(cursor.getString(cursor.getColumnIndex("IGSTRate")));
                        IGSTRate.setLeft(10);
                        IGSTRate.setWidth(60);
                        IGSTRate.setTextSize(12);
                        IGSTRate.setBackgroundResource(R.drawable.border);*/

                        IGSTAmount = new TextView(myContext);
                        IGSTAmount.setText(cursor.getString(cursor.getColumnIndex("IGSTAmount")));
                        IGSTAmount.setLeft(10);
                        IGSTAmount.setWidth(60);
                        IGSTAmount.setTextSize(12);
                        IGSTAmount.setBackgroundResource(R.drawable.border);

                        TextView SubTotal = new TextView(myContext);
                        SubTotal.setText(cursor.getString(cursor.getColumnIndex("SubTotal")));
                        SubTotal.setLeft(10);
                        SubTotal.setWidth(60);
                        SubTotal.setTextSize(12);
                        SubTotal.setBackgroundResource(R.drawable.border);

                        TextView Pos  = new TextView(myContext);
                        Pos.setText(cursor.getString(cursor.getColumnIndex("POS")));
                        Pos.setLeft(10);
                        Pos.setWidth(60);
                        Pos.setTextSize(12);
                        Pos.setBackgroundResource(R.drawable.border);

                        TextView RevCh = new TextView(myContext);
                        //                      RevCh.setText(cursor.getString(cursor.getColumnIndex("ReverseCharge")));
                        RevCh.setLeft(10);
                        RevCh.setWidth(60);
                        RevCh.setTextSize(12);
                        RevCh.setBackgroundResource(R.drawable.border);

                        TextView ProAss = new TextView(myContext);
//                        ProAss.setText(cursor.getString(cursor.getColumnIndex("ProvisionalAssess")));
                        ProAss.setLeft(10);
                        ProAss.setWidth(60);
                        ProAss.setTextSize(12);
                        ProAss.setBackgroundResource(R.drawable.border);

/*
                        TextView EGSTIN = new TextView(myContext);
                        // richa to do
                        EGSTIN.setText(cursor.getString(cursor.getColumnIndex("EcommerceGSTIN")));
                        EGSTIN.setLeft(90);
                        EGSTIN.setWidth(155);
                        EGSTIN.setTextSize(12);
                        EGSTIN.setBackgroundResource(R.drawable.border);
*/


                        rowcursor.addView(SNo);
                        rowcursor.addView(GSTIN);
                        rowcursor.addView(InvoiceNo);
                        rowcursor.addView(InvoiceDate);
                        rowcursor.addView(SupplyType);
                        if (HSNEnable.equals("1"))
                        {
                            rowcursor.addView(HSNCode);
                        }
                        rowcursor.addView(Value);
                        rowcursor.addView(Quantity);
                        rowcursor.addView(TaxableValue);
                        rowcursor.addView(GSTRate);
                        rowcursor.addView(IGSTAmount);
                        //rowcursor.addView(CGSTRate);
                        rowcursor.addView(CGSTAmount);
                        //rowcursor.addView(SGSTRate);
                        rowcursor.addView(SGSTAmount);
                        rowcursor.addView(SubTotal);
                        if (POSEnable.equals("1"))
                        {
                            rowcursor.addView(Pos);
                        }
                        if (ReverseChargeEnabe.equals("1"))
                        {
                            rowcursor.addView(RevCh);
                            rowcursor.addView(ProAss);
                        }

                        View v1 = new View(getActivity());
                        v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 5));
                        v1.setBackgroundColor(getResources().getColor(R.color.orange));
                        tblReport.addView(v1);
                        tblReport.addView(rowcursor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        View v = new View(getActivity());
                        v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 5));
                        v.setBackgroundColor(getResources().getColor(R.color.orange));
                        tblReport.addView(v);

                        String gstin1 = cursor.getString(cursor.getColumnIndex("GSTIN"));
                        if (gstin1 == null)
                        {
                            gstin1="";
                        }

                        //String sup_name = cursor.getString(cursor.getColumnIndex("SupplierName"));

                        //tblReport.addView(rowcursor, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                        //add a new line to the TableLayout:

                        addb2b_items(no,dateString,gstin1, HSNEnable, POSEnable, ReverseChargeEnabe);
                        View v11 = new View(getActivity());
                        v11.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 2));
                        v11.setBackgroundColor(getResources().getColor(R.color.orange));
                        tblReport.addView(v11);


                    } while (cursor.moveToNext());
                }
            }
        }// end try
        catch(Exception e)
        {
            //MsgBox = new AlertDialog.Builder(myContext);
            MsgBox. setMessage(e.getMessage())
                    .setPositiveButton("OK", null)
                    .show();
        }


    }

    void addb2b_items(String No, String Date, String supp_gstin, String HSNEnable, String POSEnable, String ReverseChargeEnabe)
    {
        // No.replaceAll("[\n\r]", "");
        //String.replaceAll("[\n\r]", "");
        String count_str[]= {"i","ii","iii","iv","v","vi","vii","viii","ix","x","xi","xii","xiii","xiv","xv","xvi","xvii","xviii","xix","xx"};
        Cursor cursor = dbReport.getitems_b2b(No, Date, supp_gstin);
        if (cursor == null)
        {
            //MsgBox = new AlertDialog.Builder(myContext);
            MsgBox. setMessage("No items for Invoice No : "+No+" & Invoice Date : "+Date)
                    .setPositiveButton("OK",null)
                    .show();
        }
        else
        {

            try{

                if (cursor.moveToFirst()) {

                    TextView GSTIN, InvoiceNo, InvoiceDate, SupplyType, TaxationType, HSNCode, Description, Price, Quantity,Units,SubValue,DiscountRate,TaxableValue;
                    TextView CGSTRate, CGSTAmount, SGSTRate, SGSTAmount,IGSTRate, IGSTAmount,AdditionalChargeName,AdditionalChargeAmount;

                    int count =0;
                    TableRow rowcursor;

                    do {
                        rowcursor = new TableRow(myContext);
                        rowcursor.setLayoutParams(new TableRow.LayoutParams
                                (TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                        TextView SNo = new TextView(myContext);
                        SNo.setLeft(10);
                        SNo.setText(count_str[count]+" ");
                        SNo.setWidth(42);
                        SNo.setGravity(Gravity.RIGHT);
                        SNo.setTextSize(12);
                        SNo.setBackgroundResource(R.drawable.border_item);
                        count++;

                        GSTIN = new TextView(myContext);
                        GSTIN.setLeft(10);
                        // GSTIN.setText("GSTIN12345678AB");
                        GSTIN.setWidth(155);
                        GSTIN.setTextSize(12);
                        GSTIN.setBackgroundResource(R.drawable.border_item);


                        InvoiceNo = new TextView(myContext);
                        InvoiceNo.setLeft(10);
                        InvoiceNo.setWidth(70);
                        InvoiceNo.setTextSize(12);
                        //Richa to do
                        //String no = cursor.getString(cursor.getColumnIndex("InvoiceNo"));
                        //no.trim();
                        //InvoiceNo.setText(no);
                        InvoiceNo.setBackgroundResource(R.drawable.border_item);

                        InvoiceDate = new TextView(myContext);
                        InvoiceDate.setLeft(10);
                        InvoiceDate.setWidth(60);
                        /*String date_temp = cursor.getString(cursor.getColumnIndex("InvoiceDate"));
                        date_temp.trim();*/
                        //InvoiceDate.setText(substr);
                        String dateInMillis = cursor.getString(cursor.getColumnIndex("InvoiceDate"));
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                        String date_temp = formatter.format(Long.parseLong(dateInMillis));

                        InvoiceDate.setTextSize(12);
                        InvoiceDate.setBackgroundResource(R.drawable.border_item);

                        SupplyType = new TextView(myContext);
                        SupplyType.setText(cursor.getString(cursor.getColumnIndex("SupplyType")));
                        SupplyType.setLeft(10);
                        SupplyType.setWidth(25);
                        SupplyType.setTextSize(12);
                        SupplyType.setBackgroundResource(R.drawable.border_item);


                        //TaxationType = new TextView(myContext);
                        //TaxationType.setText(cursor.getString(cursor.getColumnIndex("TaxationType")));

                        HSNCode = new TextView(myContext);
                        //HSNCode.setText(cursor.getString(cursor.getColumnIndex("HSNCode")));
                        String HSN = cursor.getString(cursor.getColumnIndex("HSNCode"));
                        String desc = cursor.getString(cursor.getColumnIndex("ItemName"));
                        if (HSN==null || HSN.equals(""))
                        {
                            HSN = "_"+desc;
                        }

                        //HSN =  "h_" + desc;
                        HSNCode.setText(HSN);
                        HSNCode.setLeft(10);
                        HSNCode.setWidth(70);
                        HSNCode.setTextSize(12);
                        HSNCode.setBackgroundResource(R.drawable.border_item);

                        TextView Value = new TextView(myContext);
                        Value.setText(cursor.getString(cursor.getColumnIndex("Value")));
                        Value.setLeft(10);
                        Value.setWidth(100);
                        Value.setTextSize(12);
                        Value.setBackgroundResource(R.drawable.border_item);

                        Quantity = new TextView(myContext);
                        Quantity.setText(cursor.getString(cursor.getColumnIndex("Quantity")));
                        Quantity.setLeft(10);
                        Quantity.setWidth(50);
                        Quantity.setTextSize(12);
                        Quantity.setBackgroundResource(R.drawable.border_item);



                        TaxableValue = new TextView(myContext);
                        TaxableValue.setText(cursor.getString(cursor.getColumnIndex("TaxableValue")));
                        TaxableValue.setLeft(10);
                        TaxableValue.setWidth(100);
                        TaxableValue.setTextSize(12);
                        TaxableValue.setBackgroundResource(R.drawable.border_item);

                        /*CGSTRate = new TextView(myContext);
                        CGSTRate.setText(cursor.getString(cursor.getColumnIndex("CGSTRate")));
                        CGSTRate.setLeft(10);
                        CGSTRate.setWidth(60);
                        CGSTRate.setTextSize(12);
                        CGSTRate.setBackgroundResource(R.drawable.border_item);
*/

                        CGSTAmount = new TextView(myContext);
                        CGSTAmount.setText(cursor.getString(cursor.getColumnIndex("CGSTAmount")));
                        CGSTAmount.setLeft(10);
                        CGSTAmount.setWidth(60);
                        CGSTAmount.setTextSize(12);
                        CGSTAmount.setBackgroundResource(R.drawable.border_item);


                        /*SGSTRate = new TextView(myContext);
                        SGSTRate.setText(cursor.getString(cursor.getColumnIndex("SGSTRate")));
                        SGSTRate.setLeft(10);
                        SGSTRate.setWidth(60);
                        SGSTRate.setTextSize(12);
                        SGSTRate.setBackgroundResource(R.drawable.border_item);*/

                        SGSTAmount = new TextView(myContext);
                        SGSTAmount.setText(cursor.getString(cursor.getColumnIndex("SGSTAmount")));
                        SGSTAmount.setLeft(10);
                        SGSTAmount.setWidth(60);
                        SGSTAmount.setTextSize(12);
                        SGSTAmount.setBackgroundResource(R.drawable.border_item);

                       /* IGSTRate = new TextView(myContext);
                        IGSTRate.setText(cursor.getString(cursor.getColumnIndex("IGSTRate")));
                        IGSTRate.setLeft(10);
                        IGSTRate.setWidth(60);
                        IGSTRate.setTextSize(12);
                        IGSTRate.setBackgroundResource(R.drawable.border_item);*/

                        TextView GSTRate = new TextView(myContext);
                        String gstrate = cursor.getString(cursor.getColumnIndex("IGSTRate"));
                        if (gstrate== null || gstrate.equals("0"))
                        {
                            gstrate = cursor.getString(cursor.getColumnIndex("CGSTRate"));
                            if (gstrate==null )
                            {
                                gstrate="0";
                            }
                            float taxrate = Float.parseFloat(gstrate);
                            taxrate *=2;
                            gstrate = String.valueOf(taxrate);
                        }
                        GSTRate.setText(gstrate);
                        GSTRate.setLeft(10);
                        GSTRate.setWidth(60);
                        GSTRate.setTextSize(12);
                        GSTRate.setBackgroundResource(R.drawable.border_item);

                        IGSTAmount = new TextView(myContext);
                        IGSTAmount.setText(cursor.getString(cursor.getColumnIndex("IGSTAmount")));
                        IGSTAmount.setLeft(10);
                        IGSTAmount.setWidth(60);
                        IGSTAmount.setTextSize(12);
                        IGSTAmount.setBackgroundResource(R.drawable.border_item);

                        TextView SubTotal = new TextView(myContext);
                        SubTotal.setText(cursor.getString(cursor.getColumnIndex("SubTotal")));
                        SubTotal.setLeft(10);
                        SubTotal.setWidth(60);
                        SubTotal.setTextSize(12);
                        SubTotal.setBackgroundResource(R.drawable.border_item);

                        TextView Pos  = new TextView(myContext);
                        //Pos.setText(cursor.getString(cursor.getColumnIndex("POS")));
                        Pos.setLeft(10);
                        Pos.setWidth(60);
                        Pos.setTextSize(12);
                        Pos.setBackgroundResource(R.drawable.border_item);

                        TextView RevCh = new TextView(myContext);
                        //RevCh.setText(cursor.getString(cursor.getColumnIndex("ReverseCharge")));
                        RevCh.setLeft(10);
                        RevCh.setWidth(60);
                        RevCh.setTextSize(12);
                        RevCh.setBackgroundResource(R.drawable.border_item);

                        TextView ProAss = new TextView(myContext);
                        //ProAss.setText(cursor.getString(cursor.getColumnIndex("ProvisionalAssess")));
                        ProAss.setLeft(10);
                        ProAss.setWidth(60);
                        ProAss.setTextSize(12);
                        ProAss.setBackgroundResource(R.drawable.border_item);

                        TextView EGSTIN = new TextView(myContext);
                        //EGSTIN.setText(cursor.getString(cursor.getColumnIndex("EcommerceGSTIN")));
                        EGSTIN.setLeft(90);
                        EGSTIN.setWidth(155);
                        EGSTIN.setTextSize(12);
                        EGSTIN.setBackgroundResource(R.drawable.border_item);



                        rowcursor.addView(SNo);
                        rowcursor.addView(GSTIN);
                        rowcursor.addView(InvoiceNo);
                        rowcursor.addView(InvoiceDate);
                        rowcursor.addView(SupplyType);
                        if (HSNEnable.equals("1"))
                        {
                            rowcursor.addView(HSNCode);
                        }
                        rowcursor.addView(Value);
                        rowcursor.addView(Quantity);
                        rowcursor.addView(TaxableValue);
                        rowcursor.addView(GSTRate);
                        rowcursor.addView(IGSTAmount);
                        rowcursor.addView(CGSTAmount);
                        rowcursor.addView(SGSTAmount);
                        rowcursor.addView(SubTotal);
                        if (POSEnable.equals("1"))
                        {
                            rowcursor.addView(Pos);
                        }
                        if (ReverseChargeEnabe.equals("1"))
                        {
                            rowcursor.addView(RevCh);
                            rowcursor.addView(ProAss);
                        }


                        /*View v1 = new View(this);
                        v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                        v1.setBackgroundColor(getResources().getColor(R.color.orange));
                        tblReport.addView(v1);*/
                        tblReport.addView(rowcursor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


                        /*View v = new View(this);
                        v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                        v.setBackgroundColor(getResources().getColor(R.color.orange));
                        tblReport.addView(v);*/


                        //tblReport.addView(rowcursor, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                        //add a new line to the TableLayout:
                        /*final View vline = new View(this);
                        vline.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,2));
                        tb_b2b.addView(vline);*/


                    } while (cursor.moveToNext());

                }

            }// end try
            catch (Exception e)
            {
                //MsgBox = new AlertDialog.Builder(myContext);
                MsgBox .setTitle("Error while fetching items details")
                        .setMessage(e.getMessage())
                        .setPositiveButton("OK",null)
                        .show();
            }
        } // end else
    }

    void GSTR1_B2Cl()
    {
        String GSTEnable="0", POSEnable="0",HSNEnable="0",ReverseChargeEnabe="0";
        try
        {
            Cursor billsettingcursor = dbReport.getBillSetting();
            if (billsettingcursor!=null && billsettingcursor.moveToFirst())
            {
                GSTEnable = billsettingcursor.getString(billsettingcursor.getColumnIndex("GSTEnable"));
                if (GSTEnable == null )
                {
                    GSTEnable="0";
                }
                else if (GSTEnable.equals("1"))
                {
                    HSNEnable= billsettingcursor.getString(billsettingcursor.getColumnIndex("HSNCode_Out"));
                    if (HSNEnable==null)
                    {
                        HSNEnable="0";
                    }
                    POSEnable = billsettingcursor.getString(billsettingcursor.getColumnIndex("POS_Out"));
                    if (POSEnable== null)
                    {
                        POSEnable= "0";
                    }
                    ReverseChargeEnabe = billsettingcursor.getString(billsettingcursor.getColumnIndex("ReverseCharge_Out"));
                    if (ReverseChargeEnabe == null)
                    {
                        ReverseChargeEnabe="0";
                    }
                }
            }
            /*String StartDate = txtReportDateStart.getText().toString();
            String EndDate = txtReportDateEnd.getText().toString();*/
            Cursor cursor = dbReport.getOutwardB2Cl(String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));
            if (cursor == null )
            {
                //MsgBox = new AlertDialog.Builder(myContext);
                MsgBox. setMessage("No data for entered period B2C-L")
                        .setPositiveButton("OK", null)
                        .show();
            }
            else {
                if (cursor.moveToFirst()) {

                    TextView CustStateCode, CustName, InvoiceNo, InvoiceDate, SupplyType, Value, HSNCode,GSTRate, Description, Price, Quantity,Units,SubValue,DiscountRate,TaxableValue;
                    TextView IGSTRate, IGSTAmount,AdditionalChargeName,AdditionalChargeAmount;

                    int count =1;
                    TableRow rowcursor;

                    do {
                        String POS_str = cursor.getString(cursor.getColumnIndex("POS"));
                        if (POS_str.equals("")== false) { // for interstate only + >2.5l
                            rowcursor = new TableRow(myContext);
                            rowcursor.setLayoutParams(new TableRow.LayoutParams
                                    (TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                            TextView SNo = new TextView(myContext);
                            SNo.setLeft(10);
                            SNo.setText(String.valueOf(count));
                            SNo.setWidth(42);
                            SNo.setGravity(Gravity.CENTER);
                            SNo.setTextSize(12);
                            SNo.setBackgroundResource(R.drawable.border);
                            count++;

                            CustStateCode = new TextView(myContext);
                            CustStateCode.setLeft(10);
                            CustStateCode.setText(cursor.getString(cursor.getColumnIndex("CustStateCode")));
                            CustStateCode.setWidth(155);
                            CustStateCode.setTextSize(12);
                            CustStateCode.setBackgroundResource(R.drawable.border);

                            CustName = new TextView(myContext);
                            CustName.setLeft(10);
                            CustName.setText(cursor.getString(cursor.getColumnIndex("CustName")));
                            CustName.setWidth(155);
                            CustName.setTextSize(12);
                            CustName.setBackgroundResource(R.drawable.border);

                            InvoiceNo = new TextView(myContext);
                            InvoiceNo.setLeft(10);
                            InvoiceNo.setWidth(70);
                            InvoiceNo.setTextSize(12);
                            String no = cursor.getString(cursor.getColumnIndex("InvoiceNo"));
                            no.trim();
                            InvoiceNo.setText(no);
                            InvoiceNo.setBackgroundResource(R.drawable.border);

                            InvoiceDate = new TextView(myContext);
                            InvoiceDate.setLeft(10);
                            InvoiceDate.setWidth(60);
                            String dateInMillis = cursor.getString(cursor.getColumnIndex("InvoiceDate"));
                            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                            String dateString = formatter.format(Long.parseLong(dateInMillis));
                            InvoiceDate.setText(dateString);
                            InvoiceDate.setTextSize(12);
                            InvoiceDate.setBackgroundResource(R.drawable.border);

                            SupplyType = new TextView(myContext);
                            //SupplyType.setText(cursor.getString(cursor.getColumnIndex("SupplyType")));
                            SupplyType.setLeft(10);
                            SupplyType.setWidth(25);
                            SupplyType.setTextSize(12);
                            SupplyType.setBackgroundResource(R.drawable.border);


                            HSNCode = new TextView(myContext);
                            //HSNCode.setText(cursor.getString(cursor.getColumnIndex("HSNCode")));
                            HSNCode.setLeft(10);
                            HSNCode.setWidth(70);
                            HSNCode.setTextSize(12);
                            HSNCode.setBackgroundResource(R.drawable.border);


                            Value = new TextView(myContext);
                            //Value.setText(cursor.getString(cursor.getColumnIndex("Value")));
                            Value.setLeft(10);
                            Value.setWidth(100);
                            Value.setTextSize(12);
                            Value.setBackgroundResource(R.drawable.border);

                            Quantity = new TextView(myContext);
                            //Quantity.setText(cursor.getString(cursor.getColumnIndex("Value")));
                            Quantity.setLeft(10);
                            Quantity.setWidth(50);
                            Quantity.setTextSize(12);
                            Quantity.setBackgroundResource(R.drawable.border);


                            TaxableValue = new TextView(myContext);
                            TaxableValue.setText(cursor.getString(cursor.getColumnIndex("TaxableValue")));
                            TaxableValue.setLeft(10);
                            TaxableValue.setWidth(100);
                            TaxableValue.setTextSize(12);
                            TaxableValue.setBackgroundResource(R.drawable.border);


                            IGSTRate = new TextView(myContext);
                            IGSTRate.setText(cursor.getString(cursor.getColumnIndex("IGSTRate")));
                            IGSTRate.setLeft(10);
                            IGSTRate.setWidth(60);
                            IGSTRate.setTextSize(12);
                            IGSTRate.setBackgroundResource(R.drawable.border);

                            IGSTAmount = new TextView(myContext);
                            IGSTAmount.setText(cursor.getString(cursor.getColumnIndex("IGSTAmount")));
                            IGSTAmount.setLeft(10);
                            IGSTAmount.setWidth(60);
                            IGSTAmount.setTextSize(12);
                            IGSTAmount.setBackgroundResource(R.drawable.border);

                            TextView SubTotal = new TextView(myContext);
                            SubTotal.setText(cursor.getString(cursor.getColumnIndex("SubTotal")));
                            SubTotal.setLeft(10);
                            SubTotal.setWidth(60);
                            SubTotal.setTextSize(12);
                            SubTotal.setBackgroundResource(R.drawable.border);

                            TextView Pos = new TextView(myContext);
                            Pos.setText(cursor.getString(cursor.getColumnIndex("POS")));
                            Pos.setLeft(10);
                            Pos.setWidth(60);
                            Pos.setTextSize(12);
                            Pos.setBackgroundResource(R.drawable.border);


                            TextView ProAss = new TextView(myContext);
                            //                        ProAss.setText(cursor.getString(cursor.getColumnIndex("ProvisionalAssess")));
                            ProAss.setLeft(10);
                            ProAss.setWidth(60);
                            ProAss.setTextSize(12);
                            ProAss.setBackgroundResource(R.drawable.border);


                            rowcursor.addView(SNo);
                            rowcursor.addView(CustStateCode);
                            rowcursor.addView(CustName);
                            rowcursor.addView(InvoiceNo);
                            rowcursor.addView(InvoiceDate);
                            rowcursor.addView(SupplyType);
                            if (HSNEnable.equals("1")) {
                                rowcursor.addView(HSNCode);
                            }
                            rowcursor.addView(Value);
                            rowcursor.addView(Quantity);
                            rowcursor.addView(TaxableValue);
                            rowcursor.addView(IGSTRate);
                            rowcursor.addView(IGSTAmount);
                            rowcursor.addView(SubTotal);
                            if (POSEnable.equals("1")) {
                                rowcursor.addView(Pos);
                            }
                            if (ReverseChargeEnabe.equals("1")) {
                                rowcursor.addView(ProAss);
                            }

                            View v1 = new View(getActivity());
                            v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 5));
                            v1.setBackgroundColor(getResources().getColor(R.color.orange));
                            tblReport.addView(v1);
                            tblReport.addView(rowcursor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                            View v = new View(getActivity());
                            v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 5));
                            v.setBackgroundColor(getResources().getColor(R.color.orange));
                            tblReport.addView(v);


                            //String sup_name = cursor.getString(cursor.getColumnIndex("SupplierName"));

                            //tblReport.addView(rowcursor, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                            //add a new line to the TableLayout:
                            String custname_str = CustName.getText().toString();
                            String statecode_str = CustStateCode.getText().toString();

                            addb2cl_items(no, dateString, custname_str, statecode_str, HSNEnable, POSEnable, ReverseChargeEnabe);
                            View v11 = new View(getActivity());
                            v11.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 2));
                            v11.setBackgroundColor(getResources().getColor(R.color.orange));
                            tblReport.addView(v11);

                        }
                    } while (cursor.moveToNext());

                }
            }
        }// end try
        catch(Exception e)
        {
            //MsgBox = new AlertDialog.Builder(myContext);
            MsgBox. setMessage(e.getMessage())
                    .setPositiveButton("OK", null)
                    .show();
        }


    }


    void addb2cl_items(String No, String Date, String custname_str, String statecode_str, String HSNEnable, String POSEnable, String ReverseChargeEnabe)
    {
        // No.replaceAll("[\n\r]", "");
        //String.replaceAll("[\n\r]", "");

        String count_str[]= {"i","ii","iii","iv","v","vi","vii","viii","ix","x","xi","xii","xiii","xiv","xv","xvi","xvii","xviii","xix","xx"};
        Cursor cursor = dbReport.getitems_b2cl(No, Date, custname_str,statecode_str);
        if (cursor == null)
        {
            //MsgBox = new AlertDialog.Builder(myContext);
            MsgBox. setMessage("No items for Invoice No : "+No+" & Invoice Date : "+Date)
                    .setPositiveButton("OK",null)
                    .show();
        }
        else
        {

            try{

                if (cursor.moveToFirst()) {

                    TextView CustStateCode,CustName, InvoiceNo, InvoiceDate, SupplyType, TaxationType, HSNCode, Description, Price, Quantity,Units,SubValue,DiscountRate,TaxableValue;
                    TextView CGSTRate, CGSTAmount, SGSTRate, SGSTAmount,IGSTRate, IGSTAmount,AdditionalChargeName,AdditionalChargeAmount;

                    int count =0;
                    TableRow rowcursor;

                    do {
                        rowcursor = new TableRow(myContext);
                        rowcursor.setLayoutParams(new TableRow.LayoutParams
                                (TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                        TextView SNo = new TextView(myContext);
                        SNo.setLeft(10);
                        SNo.setText(count_str[count]+" ");
                        SNo.setWidth(42);
                        SNo.setGravity(Gravity.RIGHT);
                        SNo.setTextSize(12);
                        SNo.setBackgroundResource(R.drawable.border_item);
                        count++;

                        CustStateCode = new TextView(myContext);
                        CustStateCode.setLeft(10);
                        // GSTIN.setText("GSTIN12345678AB");
                        CustStateCode.setWidth(155);
                        CustStateCode.setTextSize(12);
                        CustStateCode.setBackgroundResource(R.drawable.border_item);

                        CustName = new TextView(myContext);
                        CustName.setLeft(10);
                        // GSTIN.setText("GSTIN12345678AB");
                        CustName.setWidth(155);
                        CustName.setTextSize(12);
                        CustName.setBackgroundResource(R.drawable.border_item);


                        InvoiceNo = new TextView(myContext);
                        InvoiceNo.setLeft(10);
                        InvoiceNo.setWidth(70);
                        InvoiceNo.setTextSize(12);
                        InvoiceNo.setBackgroundResource(R.drawable.border_item);

                        InvoiceDate = new TextView(myContext);
                        InvoiceDate.setLeft(10);
                        InvoiceDate.setWidth(60);
                        /*String date_temp = cursor.getString(cursor.getColumnIndex("InvoiceDate"));
                        date_temp.trim();*/
                        InvoiceDate.setTextSize(12);
                        InvoiceDate.setBackgroundResource(R.drawable.border_item);

                        SupplyType = new TextView(myContext);
                        SupplyType.setText(cursor.getString(cursor.getColumnIndex("SupplyType")));
                        SupplyType.setLeft(10);
                        SupplyType.setWidth(25);
                        SupplyType.setTextSize(12);
                        SupplyType.setBackgroundResource(R.drawable.border_item);

                        HSNCode = new TextView(myContext);
                        //HSNCode.setText(cursor.getString(cursor.getColumnIndex("HSNCode")));
                        String HSN = cursor.getString(cursor.getColumnIndex("HSNCode"));
                        String desc = cursor.getString(cursor.getColumnIndex("ItemName"));
                        if (HSN==null || HSN.equals(""))
                        {
                            HSN = "_"+desc;
                        }
                        // HSN = HSN + "-" + desc;
                        HSNCode.setText(HSN);
                        HSNCode.setLeft(10);
                        HSNCode.setWidth(70);
                        HSNCode.setTextSize(12);
                        HSNCode.setBackgroundResource(R.drawable.border_item);

                        TextView Value = new TextView(myContext);
                        Value.setText(cursor.getString(cursor.getColumnIndex("Value")));
                        Value.setLeft(10);
                        Value.setWidth(100);
                        Value.setTextSize(12);
                        Value.setBackgroundResource(R.drawable.border_item);

                        Quantity = new TextView(myContext);
                        Quantity.setText(cursor.getString(cursor.getColumnIndex("Quantity")));
                        Quantity.setLeft(10);
                        Quantity.setWidth(50);
                        Quantity.setTextSize(12);
                        Quantity.setBackgroundResource(R.drawable.border_item);


                        TaxableValue = new TextView(myContext);
                        TaxableValue.setText(cursor.getString(cursor.getColumnIndex("TaxableValue")));
                        TaxableValue.setLeft(10);
                        TaxableValue.setWidth(100);
                        TaxableValue.setTextSize(12);
                        TaxableValue.setBackgroundResource(R.drawable.border_item);


                        IGSTRate = new TextView(myContext);
                        IGSTRate.setText(cursor.getString(cursor.getColumnIndex("IGSTRate")));
                        IGSTRate.setLeft(10);
                        IGSTRate.setWidth(60);
                        IGSTRate.setTextSize(12);
                        IGSTRate.setBackgroundResource(R.drawable.border_item);


                        IGSTAmount = new TextView(myContext);
                        IGSTAmount.setText(cursor.getString(cursor.getColumnIndex("IGSTAmount")));
                        IGSTAmount.setLeft(10);
                        IGSTAmount.setWidth(60);
                        IGSTAmount.setTextSize(12);
                        IGSTAmount.setBackgroundResource(R.drawable.border_item);

                        TextView SubTotal = new TextView(myContext);
                        SubTotal.setText(cursor.getString(cursor.getColumnIndex("SubTotal")));
                        SubTotal.setLeft(10);
                        SubTotal.setWidth(60);
                        SubTotal.setTextSize(12);
                        SubTotal.setBackgroundResource(R.drawable.border_item);

                        TextView Pos  = new TextView(myContext);
                        //Pos.setText(cursor.getString(cursor.getColumnIndex("POS")));
                        Pos.setLeft(10);
                        Pos.setWidth(60);
                        Pos.setTextSize(12);
                        Pos.setBackgroundResource(R.drawable.border_item);


                        TextView ProAss = new TextView(myContext);
                        //ProAss.setText(cursor.getString(cursor.getColumnIndex("ProvisionalAssess")));
                        ProAss.setLeft(10);
                        ProAss.setWidth(60);
                        ProAss.setTextSize(12);
                        ProAss.setBackgroundResource(R.drawable.border_item);



                        rowcursor.addView(SNo);
                        rowcursor.addView(CustStateCode);
                        rowcursor.addView(CustName);
                        rowcursor.addView(InvoiceNo);
                        rowcursor.addView(InvoiceDate);
                        rowcursor.addView(SupplyType);
                        if (HSNEnable.equals("1"))
                        {
                            rowcursor.addView(HSNCode);
                        }
                        rowcursor.addView(Value);
                        rowcursor.addView(Quantity);
                        rowcursor.addView(TaxableValue);
                        rowcursor.addView(IGSTRate);
                        rowcursor.addView(IGSTAmount);
                        rowcursor.addView(SubTotal);
                        if (POSEnable.equals("1"))
                        {
                            rowcursor.addView(Pos);
                        }
                        if (ReverseChargeEnabe.equals("1"))
                        {
                            rowcursor.addView(ProAss);
                        }

                        tblReport.addView(rowcursor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    } while (cursor.moveToNext());

                }

            }// end try
            catch (Exception e)
            {
                //MsgBox = new AlertDialog.Builder(myContext);
                MsgBox .setTitle("Error while fetching items details")
                        .setMessage(e.getMessage())
                        .setPositiveButton("OK",null)
                        .show();
            }
        } // end else
    }


    void GSTR1_B2Cs()
    {
        String GSTEnable="0", POSEnable="0",HSNEnable="0",ReverseChargeEnabe="0";
        ArrayList<B2Csmall> datalist_s = new ArrayList<B2Csmall>();
        try
        {
            Cursor billsettingcursor = dbReport.getBillSetting();
            if (billsettingcursor!=null && billsettingcursor.moveToFirst())
            {
                GSTEnable = billsettingcursor.getString(billsettingcursor.getColumnIndex("GSTEnable"));
                if (GSTEnable == null )
                {
                    GSTEnable="0";
                }
                else if (GSTEnable.equals("1"))
                {
                    HSNEnable= billsettingcursor.getString(billsettingcursor.getColumnIndex("HSNCode_Out"));
                    if (HSNEnable==null)
                    {
                        HSNEnable="0";
                    }
                    POSEnable = billsettingcursor.getString(billsettingcursor.getColumnIndex("POS_Out"));
                    if (POSEnable== null)
                    {
                        POSEnable= "0";
                    }
                    ReverseChargeEnabe = billsettingcursor.getString(billsettingcursor.getColumnIndex("ReverseCharge_Out"));
                    if (ReverseChargeEnabe == null)
                    {
                        ReverseChargeEnabe="0";
                    }
                }
            }
/*            String StartDate = txtReportDateStart.getText().toString();
            String EndDate = txtReportDateEnd.getText().toString();*/
            Cursor cursor = dbReport.getOutwardB2Cs(String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));
            if (cursor == null )
            {
                //MsgBox = new AlertDialog.Builder(myContext);
                MsgBox. setMessage("No data for entered period B2C-S")
                        .setPositiveButton("OK", null)
                        .show();
            }
            else {
                if (cursor.moveToFirst()) {

                    int count =1;

                    do {// item_detail table
                        String POS_str = cursor.getString(cursor.getColumnIndex("POS"));
                        float TaxableValue_f = Float.parseFloat(cursor.getString(cursor.getColumnIndex("TaxableValue")));
                        if ((POS_str.equals("")) || (POS_str.equals("")== false  && TaxableValue_f <=250000 )) {
                            // for interstate + interstate only + <=2.5l
                            String InvNo =cursor.getString(cursor.getColumnIndex("InvoiceNo"));
                            String InvDate1 = cursor.getString(cursor.getColumnIndex("InvoiceDate"));

                            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                            String InvDate = formatter.format(Long.parseLong(InvDate1));

                            String custname_str = cursor.getString(cursor.getColumnIndex("CustName"));
                            String statecode_str = cursor.getString(cursor.getColumnIndex("CustStateCode"));

                            Cursor rowcursor = dbReport.getitems_b2cl(InvNo, InvDate,custname_str,statecode_str);
                            if (rowcursor == null)
                            {
                                //MsgBox = new AlertDialog.Builder(myContext);
                                MsgBox. setMessage("No items for Invoice No : "+InvNo+" & Invoice Date : "+InvDate)
                                        .setPositiveButton("OK",null)
                                        .show();
                            }
                            else { // bill level

                                try{
                                    while(rowcursor.moveToNext()) {

                                        String SupplyType_str;
                                        float subtotal_f,taxablevalue_f,CGSTRate_f, CGSTAmount_f, SGSTRate_f;
                                        float SGSTAmount_f,IGSTRate_f, IGSTAmount_f;
                                        int found  =0;

                                        // supply type (g/s)
                                        SupplyType_str = rowcursor.getString(rowcursor.getColumnIndex("SupplyType"));

                                        String HSN = rowcursor.getString(rowcursor.getColumnIndex("HSNCode"));
                                        String desc = rowcursor.getString(rowcursor.getColumnIndex("ItemName"));
                                        if (HSN==null || HSN.equals(""))
                                        {
                                            HSN = "_"+desc;
                                        }
                                        //HSN = HSN + "-" + desc;

                                        //HSNCode_str = HSN;


                                        //CustStateCode_str  = rowcursor.getString(rowcursor.getColumnIndex("CustStateCode"));
                                        //String POS_str1 = rowcursor.getString(rowcursor.getColumnIndex("POS"));


                                        taxablevalue_f = Float.parseFloat(rowcursor.getString(rowcursor.getColumnIndex("TaxableValue")));

                                        String igstrate_str = rowcursor.getString(rowcursor.getColumnIndex("IGSTRate"));
                                        if (igstrate_str== null )
                                        {
                                            IGSTRate_f =0;
                                        }
                                        else
                                        {
                                            IGSTRate_f = Float.parseFloat(igstrate_str);
                                        }

                                        String igstamt_str = rowcursor.getString(rowcursor.getColumnIndex("IGSTAmount"));
                                        if (igstamt_str== null )
                                        {
                                            IGSTAmount_f =0;
                                        }
                                        else
                                        {
                                            IGSTAmount_f = Float.parseFloat(igstamt_str);
                                        }

                                        String cgstrate_str = rowcursor.getString(rowcursor.getColumnIndex("CGSTRate"));
                                        if (cgstrate_str== null )
                                        {
                                            CGSTRate_f =0;
                                        }
                                        else
                                        {
                                            CGSTRate_f = Float.parseFloat(cgstrate_str);
                                        }

                                        String cgstamt_str = rowcursor.getString(rowcursor.getColumnIndex("CGSTAmount"));
                                        if (cgstamt_str== null )
                                        {
                                            CGSTAmount_f =0;
                                        }
                                        else
                                        {
                                            CGSTAmount_f = Float.parseFloat(cgstamt_str);
                                        }

                                        String sgstrate_str = rowcursor.getString(rowcursor.getColumnIndex("SGSTRate"));
                                        if (sgstrate_str== null )
                                        {
                                            SGSTRate_f =0;
                                        }
                                        else
                                        {
                                            SGSTRate_f = Float.parseFloat(sgstrate_str);
                                        }

                                        String sgstamt_str = rowcursor.getString(rowcursor.getColumnIndex("SGSTAmount"));
                                        if (sgstamt_str== null )
                                        {
                                            SGSTAmount_f =0;
                                        }
                                        else
                                        {
                                            SGSTAmount_f = Float.parseFloat(sgstamt_str);
                                        }


                                        subtotal_f = Float.parseFloat(rowcursor.getString(rowcursor.getColumnIndex("SubTotal")));

                                        String ProAss = "";

                                        B2Csmall obj = new B2Csmall();
                                        obj.setSupplyType(SupplyType_str);
                                        obj.setHSNCode(HSN);
                                        obj.setPlaceOfSupply(POS_str);
                                        obj.setTaxableValue(taxablevalue_f);
                                        obj.setIGSTRate(IGSTRate_f);
                                        obj.setIGSTAmt(IGSTAmount_f);
                                        obj.setCGSTRate(CGSTRate_f);
                                        obj.setCGSTAmt(CGSTAmount_f);
                                        obj.setSGSTRate(SGSTRate_f);
                                        obj.setSGSTAmt(SGSTAmount_f);
                                        obj.setProAss(ProAss);
                                        obj.setSubTotal(subtotal_f);
                                        if (datalist_s.size() == 0) // empty list
                                        {
                                            datalist_s.add(obj);
                                        }
                                        else
                                        {
                                            found = 0;
                                            for(B2Csmall data_s: datalist_s){
                                                if(data_s.getHSNCode().equalsIgnoreCase(HSN) && data_s.getPlaceOfSupply().equalsIgnoreCase(POS_str) ){
                                                    // taxval
                                                    float taxableval = data_s.getTaxableValue();
                                                    taxableval += taxablevalue_f;
                                                    data_s.setTaxableValue(taxableval);

                                                    // IGST Amt
                                                    float igstamt_temp = data_s.getIGSTAmt();
                                                    igstamt_temp += IGSTAmount_f;
                                                    data_s.setIGSTAmt(igstamt_temp);

                                                    // CGST Amt
                                                    float cgstamt_temp = data_s.getCGSTAmt();
                                                    cgstamt_temp += CGSTAmount_f;
                                                    data_s.setCGSTAmt(cgstamt_temp);

                                                    // SGST Amt
                                                    float sgstamt_temp = data_s.getSGSTAmt();
                                                    sgstamt_temp += SGSTAmount_f;
                                                    data_s.setSGSTAmt(sgstamt_temp);

                                                    //SubTotal
                                                    float subtot = data_s.getSubTotal();
                                                    subtot += subtotal_f;
                                                    data_s.setSubTotal(subtot);

                                                    found =1;
                                                    break;

                                                }
                                            }  // end of for loop
                                            if (found ==0) // not in list
                                            {
                                                datalist_s.add(obj);
                                            }
                                        } // end of else


                                        // } while (cursor.moveToNext());

                                    }

                                }// end try
                                catch (Exception e)
                                {
                                    //MsgBox = new AlertDialog.Builder(myContext);
                                    MsgBox .setTitle("Error while fetching items details")
                                            .setMessage(e.getMessage())
                                            .setPositiveButton("OK",null)
                                            .show();
                                }
                            } // end else bill level


                        }
                    } while (cursor.moveToNext());

                }
                // now displaying the content
                // richa b2cs
                int count =1;
                TableRow rowcursor;
                for (B2Csmall obj : datalist_s)
                {
                    rowcursor = new TableRow(myContext);
                    rowcursor.setLayoutParams(new TableRow.LayoutParams
                            (TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                    TextView SNo = new TextView(myContext);
                    SNo.setLeft(10);
                    SNo.setText(String.valueOf(count));
                    SNo.setWidth(42);
                    SNo.setGravity(Gravity.CENTER);
                    SNo.setTextSize(12);
                    SNo.setBackgroundResource(R.drawable.border);
                    count++;

                    TextView SupplyType = new TextView(myContext);
                    SupplyType.setText(obj.getSupplyType());
                    SupplyType.setLeft(10);
                    SupplyType.setWidth(25);
                    SupplyType.setTextSize(12);
                    SupplyType.setBackgroundResource(R.drawable.border);

                    TextView HSNCode = new TextView(myContext);
                    HSNCode.setText(obj.getHSNCode());
                    HSNCode.setLeft(10);
                    HSNCode.setWidth(70);
                    HSNCode.setTextSize(12);
                    HSNCode.setBackgroundResource(R.drawable.border);

                    TextView POS = new TextView(myContext);
                    POS.setText(obj.getPlaceOfSupply());
                    POS.setLeft(10);
                    POS.setWidth(100);
                    POS.setTextSize(12);
                    POS.setBackgroundResource(R.drawable.border);

                    TextView TaxableValue = new TextView(myContext);
                    TaxableValue.setText(String.valueOf(obj.getTaxableValue()));
                    TaxableValue.setLeft(10);
                    TaxableValue.setWidth(100);
                    TaxableValue.setTextSize(12);
                    TaxableValue.setBackgroundResource(R.drawable.border);


                    TextView IGSTRate = new TextView(myContext);
                    IGSTRate.setText(String.valueOf(obj.getIGSTRate()));
                    IGSTRate.setLeft(10);
                    IGSTRate.setWidth(60);
                    IGSTRate.setTextSize(12);
                    IGSTRate.setBackgroundResource(R.drawable.border);


                    TextView IGSTAmount = new TextView(myContext);
                    IGSTAmount.setText(String.valueOf(obj.getIGSTAmt()));
                    IGSTAmount.setLeft(10);
                    IGSTAmount.setWidth(60);
                    IGSTAmount.setTextSize(12);
                    IGSTAmount.setBackgroundResource(R.drawable.border);

                    TextView CGSTRate = new TextView(myContext);
                    CGSTRate.setText(String.valueOf(obj.getCGSTRate()));
                    CGSTRate.setLeft(10);
                    CGSTRate.setWidth(60);
                    CGSTRate.setTextSize(12);
                    CGSTRate.setBackgroundResource(R.drawable.border);


                    TextView CGSTAmount = new TextView(myContext);
                    CGSTAmount.setText(String.valueOf(obj.getCGSTAmt()));
                    CGSTAmount.setLeft(10);
                    CGSTAmount.setWidth(60);
                    CGSTAmount.setTextSize(12);
                    CGSTAmount.setBackgroundResource(R.drawable.border);

                    TextView SGSTRate = new TextView(myContext);
                    SGSTRate.setText(String.valueOf(obj.getSGSTRate()));
                    SGSTRate.setLeft(10);
                    SGSTRate.setWidth(60);
                    SGSTRate.setTextSize(12);
                    SGSTRate.setBackgroundResource(R.drawable.border);


                    TextView SGSTAmount = new TextView(myContext);
                    SGSTAmount.setText(String.valueOf(obj.getSGSTAmt()));
                    SGSTAmount.setLeft(10);
                    SGSTAmount.setWidth(60);
                    SGSTAmount.setTextSize(12);
                    SGSTAmount.setBackgroundResource(R.drawable.border);

                    TextView SubTotal = new TextView(myContext);
                    SubTotal.setText(String.valueOf(obj.getSubTotal()));
                    SubTotal.setLeft(10);
                    SubTotal.setWidth(60);
                    SubTotal.setTextSize(12);
                    SubTotal.setBackgroundResource(R.drawable.border);


                    TextView ProAss = new TextView(myContext);
                    ProAss.setText(obj.getProAss());
                    ProAss.setLeft(10);
                    ProAss.setWidth(60);
                    ProAss.setTextSize(12);
                    ProAss.setBackgroundResource(R.drawable.border);



                    rowcursor.addView(SNo);
                    rowcursor.addView(SupplyType);
                    if (POSEnable.equals("1"))
                    {
                        rowcursor.addView(POS);
                    }
                    if (HSNEnable.equals("1"))
                    {
                        rowcursor.addView(HSNCode);
                    }
                    rowcursor.addView(TaxableValue);
                    rowcursor.addView(IGSTRate);
                    rowcursor.addView(IGSTAmount);
                    rowcursor.addView(CGSTRate);
                    rowcursor.addView(CGSTAmount);
                    rowcursor.addView(SGSTRate);
                    rowcursor.addView(SGSTAmount);
                    rowcursor.addView(SubTotal);
                    if (ReverseChargeEnabe.equals("1"))
                    {
                        rowcursor.addView(ProAss);
                    }

                    View v = new View(getActivity());
                    v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                    v.setBackgroundColor(getResources().getColor(R.color.orange));
                    tblReport.addView(v);

                    tblReport.addView(rowcursor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    View v1 = new View(getActivity());
                    v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                    v1.setBackgroundColor(getResources().getColor(R.color.orange));
                    tblReport.addView(v1);
                    //tblReport.addView(rowcursor, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

                }
            }
        }// end try
        catch(Exception e)
        {
            //MsgBox = new AlertDialog.Builder(myContext);
            MsgBox. setMessage(e.getMessage())
                    .setPositiveButton("OK", null)
                    .show();
        }


    }

    void reconcile2()
    {
        ArrayList<String> supplier_list = new ArrayList<String>();
        // richa to do - supplier details entry module
        // Cursor cursorSupplierList = dbGSTR_reconcile.getGSTINListOnCategory("Supplier");

        ArrayList<Model_reconcile> present_only_in_2A = new ArrayList<Model_reconcile>();
        ArrayList<Model_reconcile> present_only_in_2 = new ArrayList<Model_reconcile>();
        ArrayList<Model_reconcile> Invoicelist_2 = new ArrayList<Model_reconcile>(); // for keeping track of bills in GSTR2

        try {
            {
                TextView t1 = new TextView(myContext);
                t1.setText("GSTR2");
                t1.setTextSize(18);
                //t1.setWidth(500);
                t1.setBackgroundColor(getResources().getColor(R.color.greenyellow));

                TextView t2 = new TextView(myContext);
                t2.setText("GSTR2A");
                t2.setTextSize(18);
                // t2.setWidth(500);
                t2.setBackgroundColor(getResources().getColor(R.color.greenyellow));

                TextView t3 = new TextView(myContext);
                t3.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                t3.setTextSize(18);

                TextView t4 = new TextView(myContext);
                t4.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                t4.setTextSize(18);
                TextView t5 = new TextView(myContext);
                t5.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                t5.setTextSize(18);
                TextView t6 = new TextView(myContext);
                t6.setTextSize(18);
                t6.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                TextView t7 = new TextView(myContext);
                t7.setTextSize(18);
                t7.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                TextView t8 = new TextView(myContext);
                t8.setTextSize(18);
                t8.setBackgroundColor(getResources().getColor(R.color.orange));
                TextView t9 = new TextView(myContext);
                t9.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                t9.setTextSize(18);
                TextView t10 = new TextView(myContext);
                t10.setTextSize(18);
                t10.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                TextView t11 = new TextView(myContext);
                t11.setTextSize(18);
                t11.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                TextView t12 = new TextView(myContext);
                t12.setTextSize(18);
                t12.setBackgroundColor(getResources().getColor(R.color.greenyellow));

                TableRow rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                rowReport.addView(t3);
                rowReport.addView(t1);
                rowReport.addView(t4);
                rowReport.addView(t5);
                rowReport.addView(t6);
                //rowReport.addView(t7);
                rowReport.addView(t8);
                rowReport.addView(t9);
                rowReport.addView(t2);
                rowReport.addView(t10);
                rowReport.addView(t11);
                //rowReport.addView(t12);
                View v = new View(getActivity());
                v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v.setBackgroundColor(getResources().getColor(R.color.orange));
                tblReport.addView(v);

                tblReport.addView(rowReport, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                View v1 = new View(getActivity());
                v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v1.setBackgroundColor(getResources().getColor(R.color.orange));
                tblReport.addView(v1);

            }
            {
                TextView t1 = new TextView(myContext);
                t1.setText("Mismatch  Invoices  In  GSTR");
                //t1.setWidth(500);
                //   t1.setTextSize(18);
                t1.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TextView t2 = new TextView(myContext);
                //t2.setText("GSTR2A");
                // t2.setWidth(500);
                t2.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TextView t3 = new TextView(myContext);
                t3.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t4 = new TextView(myContext);
                t4.setText("2  &  GSTR2A");
                t4.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t5 = new TextView(myContext);
                t5.setText("");
                t5.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t6 = new TextView(myContext);
                t6.setText("");
                t6.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t7 = new TextView(myContext);
                t7.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t8 = new TextView(myContext);
                t8.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t9 = new TextView(myContext);
                t9.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t10 = new TextView(myContext);
                t10.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t11 = new TextView(myContext);
                t11.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t12 = new TextView(myContext);
                t12.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TableRow rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                rowReport.addView(t3);
                rowReport.addView(t1);
                rowReport.addView(t4);
                rowReport.addView(t5);
                rowReport.addView(t6);
                //rowReport.addView(t7);
                rowReport.addView(t8);
                rowReport.addView(t9);
                rowReport.addView(t2);
                rowReport.addView(t10);
                rowReport.addView(t11);
                //rowReport.addView(t12);

                View v = new View(getActivity());
                v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v.setBackgroundColor(getResources().getColor(R.color.black));
                tblReport.addView(v);

                tblReport.addView(rowReport, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                View v1 = new View(getActivity());
                v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v1.setBackgroundColor(getResources().getColor(R.color.black));
                tblReport.addView(v1);


            }
            int paddingPixel = 15;
            float density = myContext.getResources().getDisplayMetrics().density;
            int paddingDp = (int)(paddingPixel * density);
            //SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            //String date = format.format(Date.parse("Your date string"));
            //String StartDate = format.format(Date.parse(txtReportDateStart.getText().toString()));

            //String EndDate = format.format(Date.parse(txtReportDateEnd.getText().toString()));

           /* String StartDate = txtReportDateStart.getText().toString();
            String EndDate = txtReportDateEnd.getText().toString();*/
            int count =1;
            Cursor billcursor = dbReport.getInward_taxed_reconcile(String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));
            while(billcursor!=null && (billcursor.moveToNext()))
            {
                String Invno = billcursor.getString(billcursor.getColumnIndex("InvoiceNo"));
                //String InvDate = billcursor.getString(billcursor.getColumnIndex("InvoiceDate"));
                String dateInMillis = billcursor.getString(billcursor.getColumnIndex("InvoiceDate"));
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                String InvDate = formatter.format(Long.parseLong(dateInMillis));

                String gstin = billcursor.getString(billcursor.getColumnIndex("GSTIN"));
                if (gstin==null || gstin.equals(""))
                {
                    gstin= billcursor.getString(billcursor.getColumnIndex("SupplierName"));
                }
                //String Taxval_2 = billcursor.getString(billcursor.getColumnIndex("TaxableValue"));
                String IGSTAmt_2 = billcursor.getString(billcursor.getColumnIndex("IGSTAmount"));
                String CGSTAmt_2 = billcursor.getString(billcursor.getColumnIndex("CGSTAmount"));
                String SGSTAmt_2 = billcursor.getString(billcursor.getColumnIndex("SGSTAmount"));
                float taxval = Float.parseFloat(billcursor.getString(billcursor.getColumnIndex("TaxableValue")));
                String strDouble = String.format("%.2f", taxval);
                if (IGSTAmt_2==null || IGSTAmt_2.equals(""))
                    IGSTAmt_2="0.0";
                if (SGSTAmt_2== null || SGSTAmt_2.equals(""))
                    SGSTAmt_2="0.0";
                if (CGSTAmt_2== null || CGSTAmt_2.equals(""))
                    CGSTAmt_2="0.0";

                if (gstin==null || gstin.equalsIgnoreCase(""))
                {
                    gstin = billcursor.getString(billcursor.getColumnIndex("SupplierName"));
                }

                Model_reconcile invoice_2 = new Model_reconcile();
                invoice_2.setGstin(gstin);
                invoice_2.setInvoiceNo(Invno);
                invoice_2.setInvoiceDate(InvDate);
                Invoicelist_2.add(invoice_2);

                Cursor cursordata2a = dbReport.getBillsforGSTIN_2A(Invno, InvDate, gstin);
                float taxablevalue_2a =0, igstamount_2a =0, cgstamount_2a =0, sgsatamount_2a=0;
                int found =0;
                while (cursordata2a!=null && cursordata2a.moveToNext())
                {
                    found =1;
                    String taxval_str = cursordata2a.getString(cursordata2a.getColumnIndex("TaxableValue"));
                    String iamt_str = cursordata2a.getString(cursordata2a.getColumnIndex("IGSTAmount"));
                    String camt_str = cursordata2a.getString(cursordata2a.getColumnIndex("CGSTAmount"));
                    String samt_str = cursordata2a.getString(cursordata2a.getColumnIndex("SGSTAmount"));

                    if(taxval_str!=null && taxval_str.equals("")==false)
                    {
                        taxablevalue_2a += Float.parseFloat(taxval_str);
                    }
                    if(iamt_str!=null && iamt_str.equals("")==false)
                    {
                        igstamount_2a += Float.parseFloat(iamt_str);
                    }
                    if(camt_str!=null && camt_str.equals("")==false)
                    {
                        cgstamount_2a += Float.parseFloat(camt_str);
                    }
                    if(samt_str!=null && samt_str.equals("")==false)
                    {
                        sgsatamount_2a += Float.parseFloat(samt_str);
                    }

                } // end of while (cursordata2a)
/*
                if (!(Taxval_2.equals(String.valueOf(taxablevalue_2a))) || !(IGSTAmt_2.equals(String.valueOf(igstamount_2a))) ||
                        !(CGSTAmt_2.equals(String.valueOf(cgstamount_2a))) || !(SGSTAmt_2.equals(String.valueOf(sgsatamount_2a))))
*/

                if (taxval != taxablevalue_2a )
                {
                    if (found ==1)
                    {
                        // mismatch values
                        TextView Sno = new TextView(myContext);
                        Sno.setText(String.valueOf(count));
                        count++;
                        Sno.setPadding(paddingDp,0,0,0);
                        Sno.setBackgroundResource(R.drawable.border);

                        TextView GSTIN =  new TextView(myContext);
                        GSTIN.setText(gstin);
                        GSTIN.setPadding(paddingDp,0,0,0);
                        GSTIN.setBackgroundResource(R.drawable.border);

                        TextView InvoiceNo =  new TextView(myContext);
                        InvoiceNo.setText(Invno);
                        InvoiceNo.setPadding(paddingDp,0,0,0);
                        InvoiceNo.setBackgroundResource(R.drawable.border);

                        TextView InvoiceDate =  new TextView(myContext);
                        InvoiceDate.setText(InvDate);
                        InvoiceDate.setPadding(paddingDp,0,0,0);
                        InvoiceDate.setBackgroundResource(R.drawable.border);

                        TextView TaxableValue =  new TextView(myContext);
                        TaxableValue.setText(String.valueOf(taxval));
                        TaxableValue.setPadding(0,0,paddingDp,0);
                        //    TaxableValue.setGravity(Gravity.RIGHT| Gravity.END);
                        TaxableValue.setBackgroundResource(R.drawable.border);

                        TextView TaxAmount =  new TextView(myContext);
                        float tax = Float.parseFloat(IGSTAmt_2) + Float.parseFloat(CGSTAmt_2) + Float.parseFloat(SGSTAmt_2);
                        TaxAmount.setText(String.valueOf(tax));
                        TaxAmount.setBackgroundResource(R.drawable.border);

                        TextView divider =  new TextView(myContext);
                        divider.setBackgroundResource(R.color.orange);

                        TextView GSTIN_2A =  new TextView(myContext);
                        GSTIN_2A.setText(gstin);
                        GSTIN_2A.setPadding(paddingDp,0,0,0);
                        GSTIN_2A.setBackgroundResource(R.drawable.border);

                        TextView InvoiceNo_2A =  new TextView(myContext);
                        InvoiceNo_2A.setText(Invno);
                        InvoiceNo_2A.setPadding(paddingDp,0,0,0);
                        InvoiceNo_2A.setBackgroundResource(R.drawable.border);

                        TextView InvoiceDate_2A =  new TextView(myContext);
                        InvoiceDate_2A.setText(InvDate);
                        InvoiceDate_2A.setPadding(paddingDp,0,0,0);
                        InvoiceDate_2A.setBackgroundResource(R.drawable.border);

                        TextView TaxableValue_2A =  new TextView(myContext);
                        TaxableValue_2A.setText(String.valueOf(taxablevalue_2a));
                        TaxableValue_2A.setGravity(Gravity.RIGHT| Gravity.END);
                        TaxableValue_2A.setPadding(0,0,paddingDp,0);
                        TaxableValue_2A.setBackgroundResource(R.drawable.border);

                        TextView TaxAmount_2A =  new TextView(myContext);
                        float tax_2A = igstamount_2a + cgstamount_2a +sgsatamount_2a;
                        TaxAmount_2A.setText(String.valueOf(tax_2A));
                        TaxAmount_2A.setBackgroundResource(R.drawable.border);


                        TableRow rowcursor = new TableRow(myContext);
                        rowcursor.setLayoutParams(new ViewGroup.LayoutParams
                                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        rowcursor.addView(Sno);
                        rowcursor.addView(GSTIN);
                        rowcursor.addView(InvoiceNo);
                        rowcursor.addView(InvoiceDate);
                        rowcursor.addView(TaxableValue);
                        //rowcursor.addView(TaxAmount);
                        rowcursor.addView(divider);
                        rowcursor.addView(GSTIN_2A);
                        rowcursor.addView(InvoiceNo_2A);
                        rowcursor.addView(InvoiceDate_2A);
                        rowcursor.addView(TaxableValue_2A);
                        //rowcursor.addView(TaxAmount_2A);

                        View v = new View(getActivity());
                        v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                        v.setBackgroundColor(getResources().getColor(R.color.orange));
                        tblReport.addView(v);

                        tblReport.addView(rowcursor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        View v1 = new View(getActivity());
                        v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                        v1.setBackgroundColor(getResources().getColor(R.color.orange));
                        tblReport.addView(v1);
                    }
                    else{ // missing from GSTR2A
                        // to list of missing_from gstr2a
                        Model_reconcile missingInvoice_2A = new Model_reconcile();
                        missingInvoice_2A.setGstin(gstin);
                        missingInvoice_2A.setInvoiceNo(Invno);
                        missingInvoice_2A.setInvoiceDate(InvDate);
                        missingInvoice_2A.setTaxable_value(String.valueOf(taxval));
                        float tax = Float.parseFloat(IGSTAmt_2) + Float.parseFloat(CGSTAmt_2) + Float.parseFloat(SGSTAmt_2);
                        missingInvoice_2A.setIgst_amt(String.valueOf(tax));
                        present_only_in_2.add(missingInvoice_2A);
                    }
                }
            }
            // now finding bills missing from GSTR2 but present in GSTR2A
            Cursor billcursor_2A = dbReport.getInwardtaxed_2A(String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));
            while (billcursor_2A!= null && billcursor_2A.moveToNext())
            {
                int found =0;
                String Invno = billcursor_2A.getString(billcursor_2A.getColumnIndex("InvoiceNo"));
               /* String InvDate = billcursor_2A.getString(billcursor_2A.getColumnIndex("InvoiceDate"));*/

                String dateInMillis = billcursor_2A.getString(billcursor_2A.getColumnIndex("InvoiceDate"));
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                String InvDate = formatter.format(Long.parseLong(dateInMillis));

                String gstin = billcursor_2A.getString(billcursor_2A.getColumnIndex("GSTIN"));
                for (Model_reconcile obj_2A : present_only_in_2A) // check whether already entered in present_only_in_2A
                {
                    if (gstin.equalsIgnoreCase(obj_2A.getGstin()) && Invno.equalsIgnoreCase(obj_2A.getInvoiceNo())
                            && InvDate.equalsIgnoreCase(obj_2A.getInvoiceDate()))
                    {
                        // already present , update the content
                        float iamt_f =0, camt_f =0, samt_f =0,taxval_f =0;
                        String taxval_str  = billcursor_2A.getString(billcursor_2A.getColumnIndex("TaxableValue"));
                        String iamt_str  = billcursor_2A.getString(billcursor_2A.getColumnIndex("IGSTAmount"));
                        String camt_str  = billcursor_2A.getString(billcursor_2A.getColumnIndex("CGSTAmount"));
                        String samt_str  = billcursor_2A.getString(billcursor_2A.getColumnIndex("SGSTAmount"));

                        if (taxval_str!=null || taxval_str.equals("") ==false)
                            taxval_f = Float.parseFloat(taxval_str);
                        if (iamt_str!=null && iamt_str.equals("") ==false)
                            iamt_f = Float.parseFloat(iamt_str);
                        if (camt_str!=null && camt_str.equals("") ==false)
                            camt_f = Float.parseFloat(camt_str);
                        if (samt_str!=null && samt_str.equals("") ==false)
                            samt_f = Float.parseFloat(samt_str);

                        String obj_taxval_str = obj_2A.getTaxable_value();
                        String obj_taxamt_str = obj_2A.getIgst_amt();
                        if (obj_taxval_str!=null && obj_taxval_str.equals("")== false)
                            taxval_f += Float.parseFloat(obj_taxval_str);
                        if (obj_taxamt_str!=null && obj_taxamt_str.equals("")== false)
                            iamt_f += Float.parseFloat(obj_taxamt_str);

                        iamt_f += camt_f + samt_f;

                        obj_2A.setTaxable_value(String.valueOf(taxval_f));
                        obj_2A.setIgst_amt(String.valueOf(iamt_f));

                        found =1;
                        break;
                    }
                }

                if (found ==0) // yet not entered in present_only_in_2A
                {
                    for (Model_reconcile obj_2 : Invoicelist_2) // first check with GSTR2 invoice list
                    {
                        if (gstin.equalsIgnoreCase(obj_2.getGstin()) && Invno.equalsIgnoreCase(obj_2.getInvoiceNo())
                                && InvDate.equalsIgnoreCase(obj_2.getInvoiceDate()))
                        {
                            // already taken care above in GSTR2
                            found =1;
                            break;
                        }
                    }

                    if (found==0) // neither in GSTR2, nor in present_only_in_2A
                    {
                        // add to present_only_in_2A
                        float totaltax =0;
                        String taxval = billcursor_2A.getString(billcursor_2A.getColumnIndex("TaxableValue"));
                        String iamt_str = billcursor_2A.getString(billcursor_2A.getColumnIndex("IGSTAmount"));
                        String camt_str = billcursor_2A.getString(billcursor_2A.getColumnIndex("CGSTAmount"));
                        String samt_str = billcursor_2A.getString(billcursor_2A.getColumnIndex("SGSTAmount"));
                        if (taxval==null || taxval.equals(""))
                            taxval="0.0";
                        else
                            taxval = String.valueOf(Float.parseFloat(taxval));
                        if (iamt_str!= null && iamt_str.equals("")==false)
                            totaltax += Float.parseFloat(iamt_str);
                        if (camt_str!= null && camt_str.equals("")==false)
                            totaltax += Float.parseFloat(camt_str);
                        if (samt_str!= null && samt_str.equals("")==false)
                            totaltax += Float.parseFloat(samt_str);

                        Model_reconcile missingInvoice_in2 = new Model_reconcile();
                        missingInvoice_in2.setGstin(gstin);
                        missingInvoice_in2.setInvoiceNo(Invno);
                        missingInvoice_in2.setInvoiceDate(InvDate);
                        missingInvoice_in2.setTaxable_value(String.valueOf(taxval));
                        missingInvoice_in2.setIgst_amt(String.valueOf(totaltax));
                        present_only_in_2A.add(missingInvoice_in2);
                    }
                }
            }

            // now display all missing invoices in GSTR2A and only present in GSTR2
            {

                TextView t1 = new TextView(myContext);
                t1.setText("Missing  Invoices  In GSTR2A");
                //t1.setWidth(500);
                t1.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TextView t2 = new TextView(myContext);
                //t2.setText("GSTR2A");
                // t2.setWidth(500);
                t2.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TextView t3 = new TextView(myContext);
                t3.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t4 = new TextView(myContext);
                t4.setText(" ");
                t4.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t5 = new TextView(myContext);
                t5.setText("");
                t5.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t6 = new TextView(myContext);
                t6.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t7 = new TextView(myContext);
                t7.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t8 = new TextView(myContext);
                t8.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t9 = new TextView(myContext);
                t9.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t10 = new TextView(myContext);
                t10.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t11 = new TextView(myContext);
                t11.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t12 = new TextView(myContext);
                t12.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TableRow rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                rowReport.addView(t3);
                rowReport.addView(t1);
                rowReport.addView(t4);
                rowReport.addView(t5);
                rowReport.addView(t6);
                //rowReport.addView(t7);
                rowReport.addView(t8);
                rowReport.addView(t9);
                rowReport.addView(t2);
                rowReport.addView(t10);
                rowReport.addView(t11);
                //rowReport.addView(t12);
                View v = new View(getActivity());
                v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v.setBackgroundColor(getResources().getColor(R.color.black));
                tblReport.addView(v);

                tblReport.addView(rowReport, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                View v1 = new View(getActivity());
                v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v1.setBackgroundColor(getResources().getColor(R.color.black));
                tblReport.addView(v1);
                //tblReport.addView(rowReport, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            }
            for (Model_reconcile obj_2 : present_only_in_2)
            {
                TextView Sno = new TextView(myContext);
                Sno.setText(String.valueOf(count));
                count++;
                Sno.setPadding(paddingDp,0,0,0);
                Sno.setBackgroundResource(R.drawable.border);

                TextView GSTIN =  new TextView(myContext);
                GSTIN.setText(obj_2.getGstin());
                GSTIN.setPadding(paddingDp,0,0,0);
                GSTIN.setBackgroundResource(R.drawable.border);

                TextView InvoiceNo =  new TextView(myContext);
                InvoiceNo.setText(obj_2.getInvoiceNo());
                InvoiceNo.setPadding(paddingDp,0,0,0);
                InvoiceNo.setBackgroundResource(R.drawable.border);

                TextView InvoiceDate =  new TextView(myContext);
                InvoiceDate.setText(obj_2.getInvoiceDate());
                //InvoiceDate.setGravity(Gravity.CENTER);
                InvoiceDate.setPadding(paddingDp,0,0,0);
                InvoiceDate.setBackgroundResource(R.drawable.border);

                TextView TaxableValue =  new TextView(myContext);
                TaxableValue.setText(String.valueOf(Float.parseFloat(obj_2.getTaxable_value())));
                TaxableValue.setGravity(Gravity.RIGHT| Gravity.END);
                TaxableValue.setPadding(0,0,paddingDp,0);
                TaxableValue.setBackgroundResource(R.drawable.border);

                TextView TaxAmount =  new TextView(myContext);
                TaxAmount.setText(obj_2.getIgst_amt());
                TaxAmount.setBackgroundResource(R.drawable.border);

                TextView divider =  new TextView(myContext);
                divider.setBackgroundResource(R.color.orange);

                TextView GSTIN_2A =  new TextView(myContext);
                GSTIN_2A.setPadding(paddingDp,0,0,0);
                GSTIN_2A.setBackgroundResource(R.drawable.border_item);

                TextView InvoiceNo_2A =  new TextView(myContext);
                //InvoiceNo_2A.setText(Invno);
                InvoiceNo_2A.setBackgroundResource(R.drawable.border_item);
                InvoiceNo_2A.setPadding(paddingDp,0,0,0);

                TextView InvoiceDate_2A =  new TextView(myContext);
                //InvoiceDate_2A.setText(InvDate);
                InvoiceDate_2A.setBackgroundResource(R.drawable.border_item);
                InvoiceDate_2A.setPadding(paddingDp,0,0,0);

                TextView TaxableValue_2A =  new TextView(myContext);
                //TaxableValue_2A.setText(String.valueOf(taxablevalue_2a));
                TaxableValue_2A.setBackgroundResource(R.drawable.border_item);

                TextView TaxAmount_2A =  new TextView(myContext);
                //float tax_2A = igstamount_2a + cgstamount_2a +sgsatamount_2a;
                //TaxAmount_2A.setText(String.valueOf(tax_2A));
                TaxAmount_2A.setBackgroundResource(R.drawable.border_item);


                TableRow rowcursor = new TableRow(myContext);
                rowcursor.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                rowcursor.addView(Sno);
                rowcursor.addView(GSTIN);
                rowcursor.addView(InvoiceNo);
                rowcursor.addView(InvoiceDate);
                rowcursor.addView(TaxableValue);
                //rowcursor.addView(TaxAmount);
                rowcursor.addView(divider);
                rowcursor.addView(GSTIN_2A);
                rowcursor.addView(InvoiceNo_2A);
                rowcursor.addView(InvoiceDate_2A);
                rowcursor.addView(TaxableValue_2A);
                //rowcursor.addView(TaxAmount_2A);

                View v = new View(getActivity());
                v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v.setBackgroundColor(getResources().getColor(R.color.orange));
                tblReport.addView(v);

                tblReport.addView(rowcursor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                View v1 = new View(getActivity());
                v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v1.setBackgroundColor(getResources().getColor(R.color.orange));
                tblReport.addView(v1);
            }


            // present only in GSTR2A
            // now display all missing invoices in GSTR2 and only present in GSTR2A
            {
                TextView t1 = new TextView(myContext);
                t1.setText("Missing  Invoices In GSTR2");
                //t1.setWidth(500);
                t1.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TextView t2 = new TextView(myContext);
                //t2.setText("GSTR2A");
                // t2.setWidth(500);
                t2.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TextView t3 = new TextView(myContext);
                t3.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t4 = new TextView(myContext);
                t4.setText("");
                t4.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t5 = new TextView(myContext);
                t5.setText("");
                t5.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t6 = new TextView(myContext);
                t6.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t7 = new TextView(myContext);
                t7.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t8 = new TextView(myContext);
                t8.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t9 = new TextView(myContext);
                t9.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t10 = new TextView(myContext);
                t10.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t11 = new TextView(myContext);
                t11.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t12 = new TextView(myContext);
                t12.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TableRow rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                rowReport.addView(t3);
                rowReport.addView(t1);
                rowReport.addView(t4);
                rowReport.addView(t5);
                rowReport.addView(t6);
                //rowReport.addView(t7);
                rowReport.addView(t8);
                rowReport.addView(t9);
                rowReport.addView(t2);
                rowReport.addView(t10);
                rowReport.addView(t11);
                //rowReport.addView(t12);
                View v = new View(getActivity());
                v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v.setBackgroundColor(getResources().getColor(R.color.black));
                tblReport.addView(v);

                tblReport.addView(rowReport, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                View v1 = new View(getActivity());
                v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v1.setBackgroundColor(getResources().getColor(R.color.black));
                tblReport.addView(v1);
                //tblReport.addView(rowReport, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            }
            for (Model_reconcile obj_2 : present_only_in_2A)
            {
                TextView Sno = new TextView(myContext);
                Sno.setText(String.valueOf(count));
                count++;
                Sno.setPadding(paddingDp,0,0,0);
                Sno.setBackgroundResource(R.drawable.border);

                TextView GSTIN =  new TextView(myContext);
                GSTIN.setText(obj_2.getGstin());
                GSTIN.setPadding(paddingDp,0,0,0);
                GSTIN.setBackgroundResource(R.drawable.border);

                TextView InvoiceNo =  new TextView(myContext);
                InvoiceNo.setText(obj_2.getInvoiceNo());
                InvoiceNo.setPadding(paddingDp,0,0,0);
                InvoiceNo.setBackgroundResource(R.drawable.border);

                TextView InvoiceDate =  new TextView(myContext);
                InvoiceDate.setText(obj_2.getInvoiceDate());
                InvoiceDate.setPadding(paddingDp,0,0,0);
                InvoiceDate.setBackgroundResource(R.drawable.border);

                TextView TaxableValue =  new TextView(myContext);
                TaxableValue.setText(obj_2.getTaxable_value());
                TaxableValue.setPadding(0,0,paddingDp,0);
                TaxableValue.setBackgroundResource(R.drawable.border);

                TextView TaxAmount =  new TextView(myContext);
                TaxAmount.setText(obj_2.getIgst_amt());
                TaxableValue.setGravity(Gravity.RIGHT| Gravity.END);
                TaxAmount.setBackgroundResource(R.drawable.border);

                TextView divider =  new TextView(myContext);
                divider.setBackgroundResource(R.color.orange);

                TextView GSTIN_2A =  new TextView(myContext);
                //GSTIN_2A.setText(gstin);
                GSTIN_2A.setBackgroundResource(R.drawable.border_item);

                TextView InvoiceNo_2A =  new TextView(myContext);
                //InvoiceNo_2A.setText(Invno);
                InvoiceNo_2A.setBackgroundResource(R.drawable.border_item);

                TextView InvoiceDate_2A =  new TextView(myContext);
                //InvoiceDate_2A.setText(InvDate);
                InvoiceDate_2A.setBackgroundResource(R.drawable.border_item);

                TextView TaxableValue_2A =  new TextView(myContext);
                //TaxableValue_2A.setText(String.valueOf(taxablevalue_2a));
                TaxableValue_2A.setBackgroundResource(R.drawable.border_item);

                TextView TaxAmount_2A =  new TextView(myContext);
                //float tax_2A = igstamount_2a + cgstamount_2a +sgsatamount_2a;
                //TaxAmount_2A.setText(String.valueOf(tax_2A));
                TaxAmount_2A.setBackgroundResource(R.drawable.border_item);


                TableRow rowcursor = new TableRow(myContext);
                rowcursor.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                rowcursor.addView(Sno);

                rowcursor.addView(GSTIN_2A);
                rowcursor.addView(InvoiceNo_2A);
                rowcursor.addView(InvoiceDate_2A);
                rowcursor.addView(TaxableValue_2A);
                //rowcursor.addView(TaxAmount_2A);
                rowcursor.addView(divider);
                rowcursor.addView(GSTIN);
                rowcursor.addView(InvoiceNo);
                rowcursor.addView(InvoiceDate);
                rowcursor.addView(TaxableValue);
                //rowcursor.addView(TaxAmount);
                View v = new View(getActivity());
                v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v.setBackgroundColor(getResources().getColor(R.color.orange));
                tblReport.addView(v);

                tblReport.addView(rowcursor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                View v1 = new View(getActivity());
                v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v1.setBackgroundColor(getResources().getColor(R.color.orange));
                tblReport.addView(v1);
            }

        } // end try
        catch(Exception e)
        {
            MsgBox.setMessage(e.getMessage())
                    .show();
            Log.d("Reconcile 2 : ",e.getMessage());
        }


    } // end of reconcile2()

    void reconcile1()
    {
        ArrayList<String> supplier_list = new ArrayList<String>();
        // richa to do - supplier details entry module
        // Cursor cursorSupplierList = dbGSTR_reconcile.getGSTINListOnCategory("Supplier");

        ArrayList<Model_reconcile> present_only_in_1A = new ArrayList<Model_reconcile>();
        //ArrayList<Model_reconcile> present_only_in_2 = new ArrayList<Model_reconcile>();
        ArrayList<Model_reconcile> Invoicelist_1A = new ArrayList<Model_reconcile>(); // for keeping track of bills in GSTR2

        try {

            {   TextView t1 = new TextView(myContext);
                t1.setText("GSTR1");
                //t1.setWidth(500);
                t1.setBackgroundColor(getResources().getColor(R.color.greenyellow));

                TextView t2 = new TextView(myContext);
                t2.setText("GSTR1A");
                // t2.setWidth(500);
                t2.setBackgroundColor(getResources().getColor(R.color.greenyellow));

                TextView t3 = new TextView(myContext);
                t3.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                TextView t4 = new TextView(myContext);
                t4.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                TextView t5 = new TextView(myContext);
                t5.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                TextView t6 = new TextView(myContext);
                t6.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                TextView t7 = new TextView(myContext);
                t7.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                TextView t8 = new TextView(myContext);
                t8.setBackgroundColor(getResources().getColor(R.color.orange));
                TextView t9 = new TextView(myContext);
                t9.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                TextView t10 = new TextView(myContext);
                t10.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                TextView t11 = new TextView(myContext);
                t11.setBackgroundColor(getResources().getColor(R.color.greenyellow));
                TextView t12 = new TextView(myContext);
                t12.setBackgroundColor(getResources().getColor(R.color.greenyellow));

                TableRow rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                rowReport.addView(t3);
                rowReport.addView(t1);
                rowReport.addView(t4);
                rowReport.addView(t5);
                rowReport.addView(t6);
                //rowReport.addView(t7);
                rowReport.addView(t8);
                rowReport.addView(t9);
                rowReport.addView(t2);
                rowReport.addView(t10);
                rowReport.addView(t11);
                //rowReport.addView(t12);
                View v = new View(getActivity());
                v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v.setBackgroundColor(getResources().getColor(R.color.orange));
                tblReport.addView(v);

                tblReport.addView(rowReport, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                View v1 = new View(getActivity());
                v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v1.setBackgroundColor(getResources().getColor(R.color.orange));
                tblReport.addView(v1);
            }


            {
                TextView t1 = new TextView(myContext);
                t1.setText("Mismatch Invoi");
                //t1.setWidth(500);
                t1.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TextView t2 = new TextView(myContext);
                //t2.setText("GSTR2A");
                // t2.setWidth(500);
                t2.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TextView t3 = new TextView(myContext);
                t3.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t4 = new TextView(myContext);
                t4.setText("ces In GST1 &");
                t4.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t5 = new TextView(myContext);
                t5.setText(" GSTR1A");
                t5.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t6 = new TextView(myContext);
                t6.setText("");
                t6.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t7 = new TextView(myContext);
                t7.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t8 = new TextView(myContext);
                t8.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t9 = new TextView(myContext);
                t9.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t10 = new TextView(myContext);
                t10.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t11 = new TextView(myContext);
                t11.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t12 = new TextView(myContext);
                t12.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TableRow rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                rowReport.addView(t3);
                rowReport.addView(t1);
                rowReport.addView(t4);
                rowReport.addView(t5);
                rowReport.addView(t6);
                //rowReport.addView(t7);
                rowReport.addView(t8);
                rowReport.addView(t9);
                rowReport.addView(t2);
                rowReport.addView(t10);
                rowReport.addView(t11);
                //rowReport.addView(t12);

                View v = new View(getActivity());
                v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v.setBackgroundColor(getResources().getColor(R.color.black));
                tblReport.addView(v);

                tblReport.addView(rowReport, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                View v1 = new View(getActivity());
                v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v1.setBackgroundColor(getResources().getColor(R.color.black));
                tblReport.addView(v1);


            }
        /*String StartDate = txtReportDateStart.getText().toString();
        String EndDate = txtReportDateEnd.getText().toString();*/
            int count =1;
            Cursor billcursor = dbReport.getBillsforGSTIN_1A(String.valueOf(startDate_date.getTime()), String.valueOf(endDate_date.getTime()));
            while(billcursor!=null && (billcursor.moveToNext())) {
                // retrieving data from table GSTR1A and saving in list as aggregate value
                String Invno = billcursor.getString(billcursor.getColumnIndex("InvoiceNo"));
           /* String InvDate = billcursor.getString(billcursor.getColumnIndex("InvoiceDate"));*/
                String dateInMillis = billcursor.getString(billcursor.getColumnIndex("InvoiceDate"));
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                String InvDate = formatter.format(Long.parseLong(dateInMillis));

                String gstin = billcursor.getString(billcursor.getColumnIndex("GSTIN"));
                String Taxval_1A = billcursor.getString(billcursor.getColumnIndex("TaxableValue"));
                if (Taxval_1A == null || Taxval_1A.equals("")) {
                    Taxval_1A = "0";
                }

                if (gstin == null) {
                    gstin = "";
                }


                int found = 0;
                for (Model_reconcile obj_1A : Invoicelist_1A) {
                    if (obj_1A.getGstin().equalsIgnoreCase(gstin) && obj_1A.getInvoiceNo().equalsIgnoreCase(Invno) &&
                            obj_1A.getInvoiceDate().equalsIgnoreCase(InvDate)) {
                        float taxval_temp = Float.parseFloat(obj_1A.getTaxable_value());
                        taxval_temp += Float.parseFloat(Taxval_1A);
                        obj_1A.setTaxable_value(String.valueOf(taxval_temp));
                        found = 1;
                        break;
                    }
                }// end of for
                if (found == 0) {
                    Model_reconcile invoice_1A = new Model_reconcile();
                    invoice_1A.setGstin(gstin);
                    invoice_1A.setInvoiceNo(Invno);
                    invoice_1A.setInvoiceDate(InvDate);
                    invoice_1A.setTaxable_value(Taxval_1A);
                    Invoicelist_1A.add(invoice_1A);
                }
            }
            // now checking for mismatch and missing invoices with GSTR1
            for (Model_reconcile obj_1A : Invoicelist_1A) {

                String InvoiceNo = obj_1A.getInvoiceNo();
                String InvoiceDate = obj_1A.getInvoiceDate();
                String gstin = obj_1A.getGstin();

                Cursor billcursor_1 = dbReport.getBillsforGSTIN_1(InvoiceNo, InvoiceDate,gstin);
                if (billcursor_1!=null && billcursor_1.moveToFirst()) {
                    // bill is present, check for taxable value

                    String taxval_1_str  = billcursor_1.getString(billcursor_1.getColumnIndex("TaxableValue"));
                    if (!obj_1A.getTaxable_value().equals(taxval_1_str)) {
                        // display content having difference

                        TextView Sno = new TextView(myContext);
                        Sno.setText(String.valueOf(count));
                        count++;
                        Sno.setBackgroundResource(R.drawable.border);

                        TextView GSTIN =  new TextView(myContext);
                        GSTIN.setText(gstin);
                        GSTIN.setBackgroundResource(R.drawable.border);

                        TextView InvoiceNo_1 =  new TextView(myContext);
                        InvoiceNo_1.setText(InvoiceNo);
                        InvoiceNo_1.setBackgroundResource(R.drawable.border);

                        TextView InvoiceDate_1 =  new TextView(myContext);
                        InvoiceDate_1.setText(InvoiceDate);
                        InvoiceDate_1.setBackgroundResource(R.drawable.border);

                        TextView TaxableValue =  new TextView(myContext);
                        TaxableValue.setText(taxval_1_str);
                        TaxableValue.setBackgroundResource(R.drawable.border);

                        TextView divider =  new TextView(myContext);
                        divider.setBackgroundResource(R.color.orange);

                        TextView GSTIN_1A =  new TextView(myContext);
                        GSTIN_1A.setText(gstin);
                        GSTIN_1A.setBackgroundResource(R.drawable.border);

                        TextView InvoiceNo_1A =  new TextView(myContext);
                        InvoiceNo_1A.setText(InvoiceNo);
                        InvoiceNo_1A.setBackgroundResource(R.drawable.border);

                        TextView InvoiceDate_1A =  new TextView(myContext);
                        InvoiceDate_1A.setText(InvoiceDate);
                        InvoiceDate_1A.setBackgroundResource(R.drawable.border);

                        TextView TaxableValue_1A =  new TextView(myContext);
                        TaxableValue_1A.setText(String.valueOf(obj_1A.getTaxable_value()));
                        TaxableValue_1A.setBackgroundResource(R.drawable.border);


                        TableRow rowcursor = new TableRow(myContext);
                        rowcursor.setLayoutParams(new ViewGroup.LayoutParams
                                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        rowcursor.addView(Sno);
                        rowcursor.addView(GSTIN);
                        rowcursor.addView(InvoiceNo_1);
                        rowcursor.addView(InvoiceDate_1);
                        rowcursor.addView(TaxableValue);
                        rowcursor.addView(divider);
                        rowcursor.addView(GSTIN_1A);
                        rowcursor.addView(InvoiceNo_1A);
                        rowcursor.addView(InvoiceDate_1A);
                        rowcursor.addView(TaxableValue_1A);

                        View v = new View(getActivity());
                        v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                        v.setBackgroundColor(getResources().getColor(R.color.orange));
                        tblReport.addView(v);

                        tblReport.addView(rowcursor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        View v1 = new View(getActivity());
                        v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                        v1.setBackgroundColor(getResources().getColor(R.color.orange));
                        tblReport.addView(v1);
                    }
                    // else , no mismatch , do nothing. Ideally this situation is not possible
                }
                else // invoice missing from outward supply ledger
                {
                    // add to list
                    present_only_in_1A.add(obj_1A);
                }

            }

            // now display all missing invoices in GSTR1 and only present in GSTR1A
            {
                TextView t1 = new TextView(myContext);
                t1.setText("Missing Invoices");
                //t1.setWidth(500);
                t1.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TextView t2 = new TextView(myContext);
                //t2.setText("GSTR2A");
                // t2.setWidth(500);
                t2.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TextView t3 = new TextView(myContext);
                t3.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t4 = new TextView(myContext);
                t4.setText("In GSTR1");
                t4.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t5 = new TextView(myContext);
                t5.setText("");
                t5.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t6 = new TextView(myContext);
                t6.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t7 = new TextView(myContext);
                t7.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t8 = new TextView(myContext);
                t8.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t9 = new TextView(myContext);
                t9.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t10 = new TextView(myContext);
                t10.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t11 = new TextView(myContext);
                t11.setBackgroundColor(getResources().getColor(R.color.lightcoral));
                TextView t12 = new TextView(myContext);
                t12.setBackgroundColor(getResources().getColor(R.color.lightcoral));

                TableRow rowReport = new TableRow(myContext);
                rowReport.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                rowReport.addView(t3);
                rowReport.addView(t1);
                rowReport.addView(t4);
                rowReport.addView(t5);
                rowReport.addView(t6);
                //rowReport.addView(t7);
                rowReport.addView(t8);
                rowReport.addView(t9);
                rowReport.addView(t2);
                rowReport.addView(t10);
                rowReport.addView(t11);
                //rowReport.addView(t12);
                View v = new View(getActivity());
                v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v.setBackgroundColor(getResources().getColor(R.color.black));
                tblReport.addView(v);

                tblReport.addView(rowReport, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                View v1 = new View(getActivity());
                v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v1.setBackgroundColor(getResources().getColor(R.color.black));
                tblReport.addView(v1);
                //tblReport.addView(rowReport, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            }
            for (Model_reconcile obj_1A : present_only_in_1A)
            {
                TextView Sno = new TextView(myContext);
                Sno.setText(String.valueOf(count));
                count++;
                Sno.setBackgroundResource(R.drawable.border);

                TextView GSTIN =  new TextView(myContext);
                GSTIN.setText(obj_1A.getGstin());
                GSTIN.setBackgroundResource(R.drawable.border);

                TextView InvoiceNo =  new TextView(myContext);
                InvoiceNo.setText(obj_1A.getInvoiceNo());
                InvoiceNo.setBackgroundResource(R.drawable.border);

                TextView InvoiceDate =  new TextView(myContext);
                InvoiceDate.setText(obj_1A.getInvoiceDate());
                InvoiceDate.setBackgroundResource(R.drawable.border);

                TextView TaxableValue =  new TextView(myContext);
                TaxableValue.setText(obj_1A.getTaxable_value());
                TaxableValue.setBackgroundResource(R.drawable.border);

                TextView TaxAmount =  new TextView(myContext);
                TaxAmount.setText(obj_1A.getIgst_amt());
                TaxAmount.setBackgroundResource(R.drawable.border);

                TextView divider =  new TextView(myContext);
                divider.setBackgroundResource(R.color.orange);

                TextView GSTIN_2A =  new TextView(myContext);
                //GSTIN_2A.setText(gstin);
                GSTIN_2A.setBackgroundResource(R.drawable.border_item);

                TextView InvoiceNo_2A =  new TextView(myContext);
                //InvoiceNo_2A.setText(Invno);
                InvoiceNo_2A.setBackgroundResource(R.drawable.border_item);

                TextView InvoiceDate_2A =  new TextView(myContext);
                //InvoiceDate_2A.setText(InvDate);
                InvoiceDate_2A.setBackgroundResource(R.drawable.border_item);

                TextView TaxableValue_2A =  new TextView(myContext);
                //TaxableValue_2A.setText(String.valueOf(taxablevalue_2a));
                TaxableValue_2A.setBackgroundResource(R.drawable.border_item);

                TextView TaxAmount_2A =  new TextView(myContext);
                //float tax_2A = igstamount_2a + cgstamount_2a +sgsatamount_2a;
                //TaxAmount_2A.setText(String.valueOf(tax_2A));
                TaxAmount_2A.setBackgroundResource(R.drawable.border_item);


                TableRow rowcursor = new TableRow(myContext);
                rowcursor.setLayoutParams(new ViewGroup.LayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                rowcursor.addView(Sno);

                rowcursor.addView(GSTIN_2A);
                rowcursor.addView(InvoiceNo_2A);
                rowcursor.addView(InvoiceDate_2A);
                rowcursor.addView(TaxableValue_2A);
                rowcursor.addView(divider);
                rowcursor.addView(GSTIN);
                rowcursor.addView(InvoiceNo);
                rowcursor.addView(InvoiceDate);
                rowcursor.addView(TaxableValue);

                View v = new View(getActivity());
                v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v.setBackgroundColor(getResources().getColor(R.color.orange));
                tblReport.addView(v);

                tblReport.addView(rowcursor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                View v1 = new View(getActivity());
                v1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 3));
                v1.setBackgroundColor(getResources().getColor(R.color.orange));
                tblReport.addView(v1);
            }

        } // end try
        catch(Exception e)
        {
            MsgBox.setMessage(e.getMessage())
                    .show();
            Log.d("Reconcile 1 : ",e.getMessage());
        }

    } // end of reconcile1()

    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.btn_ReportDateFrom)
        {
            From();
        }
        else if(id == R.id.btn_ReportDateTo)
        {
            To();
        }

        else if(id == R.id.btn_ReportPrint)
        {
            PrintReport();
        }
        else if(id == R.id.btn_ReportExport)
        {
            ExportReport();
        }
        else if(id == R.id.btn_ReportView)
        {
            ViewReport();
        }
        else if(id == R.id.btn_ReportClose)
        {
            Close();
        }
    }

    private String getFormatedCharacterForPrint_init(String txt, int limit,int type) {
        if(txt.length()<limit){
            return getSpaces(limit-txt.length(),type)+txt;
        }else {
            return txt.substring(0,limit);
        }
    }

    public String getSpaces(int num,int type)
    {
        StringBuffer sb = new StringBuffer();
        if(type==0)
        {
            for (int i=0;i<num;i++)
            {
                sb.append(myContext.getResources().getString(com.wepindia.printers.R.string.superSpace));
            }
        }
        else
        {
            for (int i=0;i<num;i++)
            {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}

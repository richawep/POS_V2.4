package com.wepindia.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mswipetech.wisepad.sdktest.view.ApplicationData;
import com.wep.common.app.Database.BillDetail;
import com.wep.common.app.Database.BillItem;
import com.wep.common.app.Database.ComplimentaryBillDetail;
import com.wep.common.app.Database.Customer;
import com.wep.common.app.Database.DatabaseHandler;
import com.wep.common.app.WepBaseActivity;
import com.wep.common.app.models.Items;
import com.wep.common.app.print.BillKotItem;
import com.wep.common.app.print.BillServiceTaxItem;
import com.wep.common.app.print.BillSubTaxItem;
import com.wep.common.app.print.BillTaxItem;
import com.wep.common.app.print.PrintKotBillItem;
import com.wep.common.app.utils.Preferences;
import com.wep.common.app.views.WepButton;
import com.wepindia.pos.GenericClasses.EditTextInputHandler;
import com.wepindia.pos.GenericClasses.MessageDialog;
import com.wepindia.pos.adapters.ItemsAdapter;
import com.wepindia.pos.utils.ActionBarUtils;
import com.wepindia.printers.HeyDeyBaseActivity;
import com.wepindia.printers.WepPrinterBaseActivity;
import com.wepindia.printers.utils.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class BillingCounterSalesActivity extends WepPrinterBaseActivity implements View.OnClickListener {

    private static final String TAG = BillingCounterSalesActivity.class.getSimpleName();
    private Toolbar toolbar;
    private String userId, userName;
    private DatabaseHandler db;
    private ItemsAdapter itemsAdapter;
    private GridView gridViewItems;
    private ListView listViewDept,listViewCat;
    private MessageDialog messageDialog;
    Date d;
    Calendar Time; // Time variable
    private WepButton btn_PrintBill,btn_PayBill,btn_Clear,btn_DeleteBill,btn_Reprint,btn_DineInAddCustomer;
    private EditText editTextName,editTextMobile,editTextAddress,editTextOrderNo;
    EditText tvWaiterNumber;
    EditText  edtCustName, edtCustPhoneNo, edtCustAddress, edtCustDineInPhoneNo, etCustGSTIN;
    private AutoCompleteTextView autoCompleteTextViewSearchItem, autoCompleteTextViewSearchMenuCode;
    private RelativeLayout boxDept,boxCat,boxItem;
    private Button btnDept,btnCat,btnItems;
    Spinner spnr_pos;
    String strUserId = "", strUserName = "", strDate = "";
    CheckBox chk_interstate = null;
    private byte jBillingMode = 2, jWeighScale = 0;
    private TableLayout tblOrderItems;
    private String GSTEnable = "", HSNEnable_out = "", POSEnable = "";
    private Cursor crsrSettings = null;
    private TextView textViewOtherCharges,tvTaxTotal,tvServiceTaxTotal,tvSubTotal,tvBillAmount,tvDate;
    private String fastBillingMode;
    private String customerId = "0";
    public boolean isPrinterAvailable = false;
    private String strPaymentStatus;
    private int PrintBillPayment = 0;
    private String CounterSalesCaption;
    float fTotalsubTaxPercent = 0;
    int iTaxType = 0, iTotalItems = 0, iCustId = 0, iTokenNumber = 0;
    float fChangePayment = 0;
    float fWalletPayment = 0;
    float fTotalDiscount = 0, fCashPayment = 0, fCardPayment = 0, fCouponPayment = 0, fPettCashPayment = 0, fPaidTotalPayment = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing_counter_sales);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        messageDialog = new MessageDialog(this);
        userId = ApplicationData.getUserId(this);
        userName = ApplicationData.getUserName(this);
        d = new Date();
        CharSequence s = DateFormat.format("dd-MM-yyyy", d.getTime());
        com.wep.common.app.ActionBarUtils.setupToolbar(this,toolbar,getSupportActionBar(),"Counter Sales",userName," Date:"+s.toString());
        iCustId = getIntent().getIntExtra("CUST_ID", 0);
        db = new DatabaseHandler(this);
        gridViewItems = (GridView) findViewById(R.id.listViewFilter3);
        gridViewItems.setOnItemClickListener(itemsClick);
        listViewDept = (ListView) findViewById(R.id.listViewFilter2);
        listViewDept.setOnItemClickListener(deptClick);
        listViewCat = (ListView) findViewById(R.id.listViewFilter1);
        listViewCat.setOnItemClickListener(catClick);
        tblOrderItems = (TableLayout) findViewById(R.id.tblOrderItems);
        crsrSettings = db.getBillSettings();
        initViews();
        init();
        textViewOtherCharges = (TextView) findViewById(R.id.txtOthercharges);
        tvTaxTotal = (TextView) findViewById(R.id.tvTaxTotalValue);
        tvServiceTaxTotal = (TextView) findViewById(R.id.tvServiceTaxValue);
        tvSubTotal = (TextView) findViewById(R.id.tvSubTotalValue);
        tvBillAmount = (TextView) findViewById(R.id.tvBillTotalValue);
        new AsyncTask<Void, Void, ArrayList<Items>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected ArrayList<Items> doInBackground(Void... params) {
                return db.getItemItems();
            }

            @Override
            protected void onPostExecute(ArrayList<Items> list) {
                super.onPostExecute(list);
                editTextOrderNo.setText(String.valueOf(db.getNewBillNumber()));
                if(list!=null)
                    setItemsAdapter(list);
            }
        }.execute();
    }

    @Override
    public void onClick(View v)
    {
        super.onClick(v);
        int id = v.getId();
        if(id == R.id.btn_Clear)
        {
            if(isValidCustmerid())
                ClearAll();
        }
        else if(id == R.id.btn_DineInAddCustomer)
        {
            if(isValidCustmerid())
                addCustomer();
        }
        else if(id == R.id.btn_PrintBill)
        {
            //if(isValidCustmerid())
                printBILL();
        }
        else if(id == R.id.btn_PayBill)
        {
            Tender1();
        }
    }

    public boolean isValidCustmerid(){
        if(customerId==null)
            return false;
        else
            return true;
    }

    public void addCustomer()
    {
        try {
            if (editTextName.getText().toString().equalsIgnoreCase("") || editTextMobile.getText().toString().equalsIgnoreCase("") || editTextAddress.getText().toString().equalsIgnoreCase(""))
            {
                messageDialog.Show("Warning", "Please fill all details before adding customer");
            }
            else
            {
                Cursor crsrCust = db.getFnbCustomer(editTextMobile.getText().toString());
                if (crsrCust.moveToFirst())
                {
                    messageDialog.Show("", "Customer Already Exists");
                }
                else
                {
                    String gstin = null; // etCustGSTIN.getText().toString();
                    if (gstin == null) {
                        gstin = "";
                    }
                    insertCustomer(editTextAddress.getText().toString(), editTextMobile.getText().toString(), editTextName.getText().toString(), 0, 0, 0, gstin);
                    //ResetCustomer();
                    //MsgBox.Show("", "Customer Added Successfully");
                    Toast.makeText(getApplicationContext(), "Customer Added Successfully", Toast.LENGTH_SHORT).show();
                    ControlsSetEnabled();

                }
            }
        } catch (Exception ex) {
            messageDialog.Show("Error", ex.getMessage());
        }
    }

    private void insertCustomer(String strAddress, String strContactNumber, String strName, float fLastTransaction, float fTotalTransaction, float fCreditAmount, String gstin)
    {
        long lRowId;
        Customer objCustomer = new Customer(strAddress, strName, strContactNumber, fLastTransaction, fTotalTransaction, fCreditAmount, gstin);
        lRowId = db.addCustomers(objCustomer);
        if (editTextMobile.getText().toString().length() == 10)
        {
            Cursor crsrCust = db.getFnbCustomer(editTextMobile.getText().toString());
            if (crsrCust.moveToFirst())
            {
                //edtCustId.setText(crsrCust.getString(crsrCust.getColumnIndex("CustId")));
                customerId = crsrCust.getString(crsrCust.getColumnIndex("CustId"));
            }
        }
        Log.d("Customer", "Row Id: " + String.valueOf(lRowId));
    }

    private void ClearAll()
    {
        editTextName.setText("");
        editTextMobile.setText("");
        editTextAddress.setText("");
        editTextOrderNo.setText("");
        autoCompleteTextViewSearchItem.setText("");
        autoCompleteTextViewSearchMenuCode.setText("");
        tblOrderItems.removeAllViews();
        editTextOrderNo.setText(String.valueOf(db.getNewBillNumber()));
    }

    private void initViews() {
        btn_PrintBill = (WepButton) findViewById(R.id.btn_PrintBill);
        btn_PrintBill.setOnClickListener(this);
        btn_PayBill = (WepButton) findViewById(R.id.btn_PayBill);
        btn_PayBill.setOnClickListener(this);
        btn_Clear = (WepButton) findViewById(R.id.btn_Clear);
        btn_Clear.setOnClickListener(this);
        btn_DeleteBill = (WepButton) findViewById(R.id.btn_DeleteBill);
        btn_DeleteBill.setOnClickListener(this);
        btn_Reprint = (WepButton) findViewById(R.id.btn_Reprint);
        btn_Reprint.setOnClickListener(this);
        btn_DineInAddCustomer = (WepButton) findViewById(R.id.btn_DineInAddCustomer);
        btn_DineInAddCustomer.setOnClickListener(this);
        chk_interstate = (CheckBox) findViewById(R.id.checkbox_interstate);
        btnDept = (Button) findViewById(R.id.btnLabel1);
        btnCat = (Button) findViewById(R.id.btnLabel2);
        btnItems = (Button) findViewById(R.id.btnLabel3);
        spnr_pos = (Spinner) findViewById(R.id.spnr_pos);
        editTextName = (EditText) findViewById(R.id.edtCustName);
        editTextMobile = (EditText) findViewById(R.id.edtCustPhoneNo);
        editTextAddress = (EditText) findViewById(R.id.edtCustAddress);
        etCustGSTIN = (EditText) findViewById(R.id.etCustGSTIN);
        autoCompleteTextViewSearchItem = (AutoCompleteTextView) findViewById(R.id.aCTVSearchItem);
        autoCompleteTextViewSearchMenuCode = (AutoCompleteTextView) findViewById(R.id.aCTVSearchMenuCode);
        editTextOrderNo = (EditText) findViewById(R.id.tvBillNumberValue);
        boxDept = (RelativeLayout) findViewById(R.id.boxDept);
        boxCat = (RelativeLayout) findViewById(R.id.boxCat);
        boxItem = (RelativeLayout) findViewById(R.id.boxItem);
        tvDate = (TextView) findViewById(R.id.tvBillDateValue);
        tvWaiterNumber = (EditText) findViewById(R.id.tvWaiterNoValue);
        editTextMobile.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                try {
                    if (editTextMobile.getText().toString().length() == 10)
                    {
                        Cursor crsrCust = db.getFnbCustomer(editTextMobile.getText().toString());
                        if (crsrCust.moveToFirst())
                        {
                            customerId = crsrCust.getString(crsrCust.getColumnIndex("CustId"));
                            editTextName.setText(crsrCust.getString(crsrCust.getColumnIndex("CustName")));
                            editTextAddress.setText(crsrCust.getString(crsrCust.getColumnIndex("CustAddress")));
                            ControlsSetEnabled();
                            btn_DineInAddCustomer.setEnabled(false);
                            if(jBillingMode!=2)
                            {
                                btn_PrintBill.setEnabled(false);
                                btn_PayBill.setEnabled(false);
                            }
                            else
                            {
                                btn_PrintBill.setEnabled(true);
                                btn_PayBill.setEnabled(true);
                            }
                        }
                        else
                        {
                            messageDialog.Show("", "Customer is not Found, Please Add Customer before Order");
                            btn_DineInAddCustomer.setVisibility(View.VISIBLE);
                            ControlsSetDisabled();
                            btn_DineInAddCustomer.setEnabled(true);
                        }
                    }
                    else
                    {
                        btn_DineInAddCustomer.setEnabled(true);
                    }
                } catch (Exception ex) {
                    messageDialog.Show("Error "+ex.toString(), ex.getMessage());
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void init() {
        if (crsrSettings.moveToFirst())
        {
            CounterSalesCaption = crsrSettings.getString(crsrSettings.getColumnIndex("HomeCounterSalesCaption"));
            if (crsrSettings.getInt(crsrSettings.getColumnIndex("DateAndTime")) == 1)
            {
                Date date1 = new Date();
                CharSequence sdate = DateFormat.format("dd-MM-yyyy", date1.getTime());
                String strDate = sdate.toString();
                Date strDate_date = null;
                try {
                    strDate_date = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(strDate);
                    tvDate.setText(String.valueOf(strDate_date.getTime()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                String strDate = crsrSettings.getString(crsrSettings.getColumnIndex("BusinessDate"));
                Date strDate_date;
                try {
                    strDate_date = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(strDate);
                    tvDate.setText(String.valueOf(strDate_date.getTime()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            iTaxType = crsrSettings.getInt(crsrSettings.getColumnIndex("TaxType"));
        }
        fastBillingMode = crsrSettings.getString(crsrSettings.getColumnIndex("FastBillingMode"));
        if (fastBillingMode.equalsIgnoreCase("1"))
        {
            gridViewItems.setNumColumns(6);
            //GetItemDetails();
            boxDept.setVisibility(View.GONE);
            boxCat.setVisibility(View.GONE);
        }
        else if (crsrSettings.getString(crsrSettings.getColumnIndex("FastBillingMode")).equalsIgnoreCase("2"))
        {
            gridViewItems.setNumColumns(4);
            //GetItemDetailswithoutDeptCateg();
            boxCat.setVisibility(View.GONE);
        }
        else
        {
            /*GetItemDetailswithoutDeptCateg();
            lstvwDepartment.setAdapter(null);
            lstvwCategory.setAdapter(null);
            grdItems.setAdapter(null);*/
        }
    }

    public void setItemsAdapter(ArrayList<Items> list)
    {
        if(itemsAdapter==null){
            itemsAdapter = new ItemsAdapter(this,list);
            gridViewItems.setAdapter(itemsAdapter);
        }
        else
            itemsAdapter.notifyDataSetChanged(list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"Opening Activity Ended");
    }

    @Override
    public void onConfigurationRequired() {

    }

    public void onPrinterAvailable() {
        isPrinterAvailable = true;
    }

    @Override
    public void onHomePressed() {
        ActionBarUtils.navigateHome(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.wep.common.app.R.menu.menu_main, menu);
        for (int j = 0; j < menu.size(); j++) {
            MenuItem item = menu.getItem(j);
            item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            finish();
        }
        else if (id == com.wep.common.app.R.id.action_home)
        {
            finish();
        }
        else if (id == com.wep.common.app.R.id.action_screen_shot)
        {
            com.wep.common.app.ActionBarUtils.takeScreenshot(this,findViewById(android.R.id.content).getRootView());
        }
        return super.onOptionsItemSelected(item);
    }

    private AdapterView.OnItemClickListener itemsClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Items items = (Items) itemsAdapter.getItem(position);
            Cursor cursor = db.getItemss(items.getItemCode());
            //btnClear.setEnabled(true);
            AddItemToOrderTable(cursor);
        }
    };

    private AdapterView.OnItemClickListener deptClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    };
    private AdapterView.OnItemClickListener catClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    };

    private void AddItemToOrderTable(Cursor crsrItem)
    {
        EditTextInputHandler etInputValidate = new EditTextInputHandler();
        String strQty = "0";
        double dRate = 0, dTaxPercent = 0, dDiscPercent = 0, dTaxAmt = 0, dDiscAmt = 0, dTempAmt = 0, dServiceTaxPercent = 0;
        double dServiceTaxAmt = 0;
        int iTaxId = 0, iServiceTaxId = 0, iDiscId = 0;
        boolean bItemExists = false;
        TableRow rowItem = null;
        Cursor crsrTax, crsrDiscount;
        TextView tvName, tvAmount, tvTaxPercent, tvTaxAmt, tvDiscPercent, tvDiscAmt, tvDeptCode, tvCategCode, tvKitchenCode, tvTaxType, tvModifierCharge, tvServiceTaxPercent, tvServiceTaxAmt;
        EditText etQty, etRate;
        TextView tvHSn;
        CheckBox chkNumber;
        Cursor crsrSettings = db.getBillSettings();
        int crsrSettingsCount = crsrSettings.getCount();
        int crsrItemCount = crsrItem.getCount();
        TextView HSNCode;
        if (crsrItem.moveToFirst() && crsrSettings.moveToFirst())
        {
            do {
                tvServiceTaxPercent = new TextView(this);
                if (iTaxType == 1)
                {
                    String txtServiceTaxPercentage = crsrItem.getString(crsrItem.getColumnIndex("ServiceTaxPercent"));
                    tvServiceTaxPercent.setText(txtServiceTaxPercentage);
                }
                else
                {
                    tvServiceTaxPercent.setText("0");
                }
                // Check for the item in Order table, if present update quantity
                // and amounts
                for (int iRow = 0; iRow < tblOrderItems.getChildCount(); iRow++)
                {
                    TableRow Row = (TableRow) tblOrderItems.getChildAt(iRow);
                    // Check against item number present in table
                    if (Row.getChildAt(0) != null)
                    {
                        CheckBox Number = (CheckBox) Row.getChildAt(0);
                        TextView ItemName = (TextView) Row.getChildAt(1);
                        TextView PrintKOTStatus = (TextView) Row.getChildAt(21);
                        // Check for item number and name, if name is not same
                        // add new
                        if (Number.getText().toString().equalsIgnoreCase(crsrItem.getString(crsrItem.getColumnIndex("MenuCode"))) && ItemName.getText().toString().equalsIgnoreCase(crsrItem.getString(crsrItem.getColumnIndex("ItemName"))))
                        {
                            if (PrintKOTStatus.getText().toString().equalsIgnoreCase("0"))
                            {
                                EditText Qty = (EditText) Row.getChildAt(3);
                                Qty.setEnabled(false);
                            }
                            else
                            {
                                // Quantity
                                EditText Qty = (EditText) Row.getChildAt(3);
                                Qty.setSelectAllOnFocus(true);
                                strQty = Qty.getText().toString().equalsIgnoreCase("") ? "0" : Qty.getText().toString(); // Temp
                                int BillwithStock = crsrSettings.getInt(crsrSettings.getColumnIndex("BillwithStock"));
                                if (BillwithStock == 1)
                                {
                                    String availableqty = crsrItem.getString(crsrItem.getColumnIndex("Quantity"));
                                    if (crsrItem.getFloat(crsrItem.getColumnIndex("Quantity")) < (Float.valueOf(strQty) + 1))
                                    {
                                        messageDialog.Show("Warning", "Stock is less, present stock quantity is " + availableqty);
                                        Qty.setText(String.format("%.2f", Double.parseDouble(availableqty)) );
                                        return;
                                    }
                                    else
                                    {
                                        Qty.setText(String.format("%.2f", Double.parseDouble(strQty) + 1));
                                    }
                                }
                                else
                                {
                                    Qty.setText(String.format("%.2f", Double.parseDouble(strQty) + 1));
                                }

                                // Amount
                                EditText Rate = (EditText) Row.getChildAt(4);
                                Rate.setSelectAllOnFocus(true);
                                TextView Amount = (TextView) Row.getChildAt(5);
                                dRate = Double.parseDouble(Rate.getText().toString().equalsIgnoreCase("") ? "0" : Rate.getText().toString()); // Temp
                                Amount.setText(String.format("%.2f", (Double.parseDouble(Qty.getText().toString()) * dRate)));
                                // Tax and Discount Amount
                                TextView TaxPer = (TextView) Row.getChildAt(6);
                                TextView TaxAmt = (TextView) Row.getChildAt(7);
                                TextView DiscPer = (TextView) Row.getChildAt(8);
                                TextView DiscAmt = (TextView) Row.getChildAt(9);
                                TextView ServiceTax = (TextView) Row.getChildAt(15);
                                TextView ServiceTaxAmt = (TextView) Row.getChildAt(16);
                                // TextView Delete = (TextView) Row.getChildAt(16);

                                dTaxPercent = Double.parseDouble(TaxPer.getText().toString().equalsIgnoreCase("") ? "0"
                                        : TaxPer.getText().toString()); // Temp
                                dDiscPercent = Double.parseDouble(DiscPer.getText().toString().equalsIgnoreCase("") ? "0"
                                        : DiscPer.getText().toString()); // Temp

                                if (crsrSettings.getInt(crsrSettings.getColumnIndex("Tax")) == 1) {
                                    // Discount
                                    dDiscAmt = dRate * (dDiscPercent / 100);
                                    dTempAmt = dDiscAmt;
                                    dDiscAmt = dDiscAmt * Double.parseDouble(Qty.getText().toString());

                                    // Tax
                                    dTaxAmt = (dRate - dTempAmt) * (dTaxPercent / 100);
                                    dTaxAmt = dTaxAmt * Double.parseDouble(Qty.getText().toString());

                                    TaxAmt.setText(String.format("%.2f", dTaxAmt));
                                    DiscAmt.setText(String.format("%.2f", dDiscAmt));

                                } else {
                                    double dBasePrice = 0;
                                    dBasePrice = dRate / (1 + (dTaxPercent / 100));

                                    // Discount
                                    dDiscAmt = dBasePrice * (dDiscPercent / 100);
                                    dTempAmt = dDiscAmt;
                                    dDiscAmt = dDiscAmt * Double.parseDouble(Qty.getText().toString());

                                    // Tax
                                    dTaxAmt = (dBasePrice - dTempAmt) * (dTaxPercent / 100);
                                    dTaxAmt = dTaxAmt * Double.parseDouble(Qty.getText().toString());

                                    TaxAmt.setText(String.format("%.2f", dTaxAmt));
                                    DiscAmt.setText(String.format("%.2f", dDiscAmt));
                                }

                                // Service Tax charge
                                dTaxAmt = dRate * (Double.parseDouble(ServiceTax.getText().toString()) / 100);
                                ServiceTaxAmt.setText(
                                        String.format("%.2f", (Double.parseDouble(Qty.getText().toString()) * dTaxAmt)));

                                // // delete
                                // Delete.setText("Hi");

                                // Clear all variables and set ItemExists to TRUE
                                // and break from the loop
                                dRate = 0;
                                dTaxPercent = 0;
                                dDiscPercent = 0;
                                dTaxAmt = 0;
                                dDiscAmt = 0;
                                dTempAmt = 0;
                                bItemExists = true;

                                break;
                            }


                        }
                    }
                }


                if (bItemExists == false) {

                    rowItem = new TableRow(BillingCounterSalesActivity.this);
                    rowItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    //crsrSettings = db.getBillSetting();

                    int DineInRate = crsrSettings.getInt(crsrSettings.getColumnIndex("DineInRate"));
                    int CounterSalesRate = crsrSettings.getInt(crsrSettings.getColumnIndex("CounterSalesRate"));
                    int PickUpRate = crsrSettings.getInt(crsrSettings.getColumnIndex("PickUpRate"));
                    int HomeDeliveryRate = crsrSettings.getInt(crsrSettings.getColumnIndex("HomeDeliveryRate"));
                    if (GSTEnable!=null && GSTEnable.equalsIgnoreCase("1")) {
                        //int rate_dummy = crsrItem.getString(crsrItem.getColumnIndex("Rate"));
                        dRate = crsrItem.getInt(crsrItem.getColumnIndex("Rate"));
                    } else { // gst disable
                        if (jBillingMode == 1) {
                            if (DineInRate == 1) {
                                dRate = crsrItem.getInt(crsrItem.getColumnIndex("DineInPrice1"));
                            } else if (DineInRate == 2) {
                                dRate = crsrItem.getInt(crsrItem.getColumnIndex("DineInPrice2"));
                            } else if (DineInRate == 3) {
                                dRate = crsrItem.getInt(crsrItem.getColumnIndex("DineInPrice3"));
                            }
                        }
                        if (jBillingMode == 2) {
                            if (CounterSalesRate == 1) {
                                dRate = crsrItem.getInt(crsrItem.getColumnIndex("DineInPrice1"));
                            } else if (CounterSalesRate == 2) {
                                dRate = crsrItem.getInt(crsrItem.getColumnIndex("DineInPrice2"));
                            } else if (CounterSalesRate == 3) {
                                dRate = crsrItem.getInt(crsrItem.getColumnIndex("DineInPrice3"));
                            }
                        }
                        if (jBillingMode == 3) {
                            if (PickUpRate == 1) {
                                dRate = crsrItem.getInt(crsrItem.getColumnIndex("DineInPrice1"));
                            } else if (PickUpRate == 2) {
                                dRate = crsrItem.getInt(crsrItem.getColumnIndex("DineInPrice2"));
                            } else if (PickUpRate == 3) {
                                dRate = crsrItem.getInt(crsrItem.getColumnIndex("DineInPrice3"));
                            }
                        }
                        if (jBillingMode == 4) {
                            if (HomeDeliveryRate == 1) {
                                dRate = crsrItem.getInt(crsrItem.getColumnIndex("DineInPrice1"));
                            } else if (HomeDeliveryRate == 2) {
                                dRate = crsrItem.getInt(crsrItem.getColumnIndex("DineInPrice2"));
                            } else if (HomeDeliveryRate == 3) {
                                dRate = crsrItem.getInt(crsrItem.getColumnIndex("DineInPrice3"));
                            }
                        }
                    }
                    // Menu Code
                    chkNumber = new CheckBox(BillingCounterSalesActivity.this);
                    chkNumber.setWidth(40); // 57px ~= 85dp
                    chkNumber.setTextSize(0);
                    chkNumber.setTextColor(Color.TRANSPARENT);
                    chkNumber.setText(crsrItem.getString(crsrItem.getColumnIndex("MenuCode")));
                    Toast.makeText(getApplicationContext(), chkNumber.getText().toString(), Toast.LENGTH_SHORT).show();

                    // Item Name
                    tvName = new TextView(BillingCounterSalesActivity.this);
                    tvName.setWidth(135); // 154px ~= 230dp
                    tvName.setTextSize(11);
                    tvName.setText(crsrItem.getString(crsrItem.getColumnIndex("ItemName")));

                    //hsn code
                    tvHSn = new TextView(BillingCounterSalesActivity.this);
                    tvHSn.setWidth(67); // 154px ~= 230dp
                    tvHSn.setTextSize(11);
                    if (GSTEnable.equalsIgnoreCase("1") && HSNEnable_out.equals("1")) {
                        tvHSn.setText(crsrItem.getString(crsrItem.getColumnIndex("HSNCode")));
                    }

                    // Quantity
                    etQty = new EditText(BillingCounterSalesActivity.this);
                    etQty.setWidth(55); // 57px ~= 85dp
                    etQty.setTextSize(11);
                    etQty.setSelectAllOnFocus(true);
                    etQty.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    // Read quantity from weighing scale if read from weigh
                    // scale is set in settings
                    if (jWeighScale == 0) {
                        etQty.setText("1.00");
                    } else {
                        etQty.setText(String.format("%.2f", getQuantityFromWeighScale()));
                    }
                    etQty.setTag("QTY_RATE");
                    etQty.setOnClickListener(Qty_Rate_Click);
                    etQty.setOnKeyListener(Qty_Rate_KeyPressEvent);
                    etInputValidate.ValidateDecimalInput(etQty);
                    etQty.addTextChangedListener(new TextWatcher() {
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        public void afterTextChanged(Editable s) {
                            Qty_Rate_Edit();
                        }
                    });

                    int BillwithStock = crsrSettings.getInt(crsrSettings.getColumnIndex("BillwithStock"));
                    if (BillwithStock == 1) {
                        if (crsrItem.getFloat(crsrItem.getColumnIndex("Quantity")) < Float.valueOf(etQty.getText().toString())) {
                            String availableQty = crsrItem.getString(crsrItem.getColumnIndex("Quantity")) ;
                            messageDialog.Show("Warning", "Stock is less, present stock quantity is "
                                    + availableQty);
                            etQty.setText(String.format("%.2f", Double.parseDouble(availableQty)));
                            return;
                        }
                    }

                    // Rate
                    etRate = new EditText(BillingCounterSalesActivity.this);
                    etRate.setWidth(70); // 74px ~= 110dp
                    etRate.setTextSize(11);
                    etRate.setSelectAllOnFocus(true);
                    etRate.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    etRate.setText(String.format("%.2f", dRate));
                    etRate.setTag("QTY_RATE");
                    // Check whether Price change is enabled for the item, if
                    // not set Rate text box click able property to false
                    if (crsrSettings.getInt(crsrSettings.getColumnIndex("PriceChange")) == 0) {
                        etRate.setEnabled(false);
                    } else {
                        etRate.setOnClickListener(Qty_Rate_Click);
                        etRate.setOnKeyListener(Qty_Rate_KeyPressEvent);
                        etInputValidate.ValidateDecimalInput(etRate);
                    }

                    // Amount
                    tvAmount = new TextView(BillingCounterSalesActivity.this);
                    tvAmount.setWidth(60); // 97px ~= 145dp
                    tvAmount.setTextSize(11);
                    tvAmount.setGravity(Gravity.RIGHT | Gravity.END);
                    tvAmount.setText(String.format("  %.2f", dRate));


                    // Discount Percent - Check whether Discount is enabled for
                    // the item,
                    // if enabled get discount percentage from discount table
                    if (crsrItem.getInt(crsrItem.getColumnIndex("DiscountEnable")) == 1) {
                        iDiscId = crsrItem.getInt(crsrItem.getColumnIndex("DiscId"));
                        crsrDiscount = db.getDiscountConfig(iDiscId);
                        if (!crsrDiscount.moveToFirst()) {
                            messageDialog.Show("Warning", "Failed to read Discount from crsrDiscount");
                            return;
                        } else {

                            dDiscPercent = crsrDiscount.getDouble(crsrDiscount.getColumnIndex("DiscPercentage"));
                        }
                    }
                    tvDiscPercent = new TextView(BillingCounterSalesActivity.this);
                    tvDiscPercent.setWidth(50);
                    tvDiscPercent.setText(String.format("%.2f", dDiscPercent));

                    // Discount Amount
                    if (crsrSettings.getInt(crsrSettings.getColumnIndex("Tax")) == 1) { // Forward
                        // Tax
                        dDiscAmt = dRate * (dDiscPercent / 100);
                    } else { // Reverse Tax
                        double dBasePrice = 0;
                        dBasePrice = dRate / (1 + (dTaxPercent / 100));
                        dDiscAmt = dBasePrice * (dDiscPercent / 100);
                    }
                    tvDiscAmt = new TextView(BillingCounterSalesActivity.this);
                    tvDiscAmt.setWidth(50);
                    tvDiscAmt.setText(String.format("%.2f", dDiscAmt));


                    // Tax Percent
                    if (GSTEnable.equalsIgnoreCase("1")) {
                        dTaxPercent = crsrItem.getDouble(crsrItem.getColumnIndex("CGSTRate"));
                    } else {
                        /*iTaxId = crsrItem.getInt(crsrItem.getColumnIndex("SalesTaxId"));
                        crsrTax = dbBillScreen.getTaxConfig(iTaxId);
                        // Return back, If tax table data is returned empty.
                        if (!crsrTax.moveToFirst()) {
                            MsgBox.Show("Warning", "Failed to read Tax from crsrTax");
                            return;
                        } else {

                            dTaxPercent = crsrTax.getDouble(crsrTax.getColumnIndex("TotalPercentage"));
                        }*/
                        dTaxPercent = crsrItem.getDouble(crsrItem.getColumnIndex("SalesTaxPercent"));
                    }
                    tvTaxPercent = new TextView(BillingCounterSalesActivity.this);
                    tvTaxPercent.setWidth(50);
                    tvTaxPercent.setText(String.format("%.2f", dTaxPercent));


                    // Tax Amount
                    if (crsrSettings.getInt(crsrSettings.getColumnIndex("Tax")) == 1) { // Forward
                        // Tax
                        dTaxAmt = (dRate - dDiscAmt) * (dTaxPercent / 100);
                    } else { // Reverse Tax
                        double dBasePrice = 0;
                        dBasePrice = dRate / (1 + (dTaxPercent / 100));
                        dTaxAmt = (dBasePrice - dDiscAmt) * (dTaxPercent / 100);
                    }
                    tvTaxAmt = new TextView(BillingCounterSalesActivity.this);
                    tvTaxAmt.setWidth(50);
                    tvTaxAmt.setText(String.format("%.2f", dTaxAmt));
//                    MsgBox.Show("", tvTaxAmt.getText().toString());

                    // Service Tax charge
                    if (GSTEnable.equalsIgnoreCase("1")) {
                        dServiceTaxPercent = crsrItem.getDouble(crsrItem.getColumnIndex("SGSTRate"));
                    } else {
                        /*iServiceTaxId = crsrItem.getInt(crsrItem.getColumnIndex("AdditionalTaxId"));
                        crsrTax = dbBillScreen.getTaxConfig(iServiceTaxId);
                        // Return back, If tax table data is returned empty.
                        if (!crsrTax.moveToFirst()) {
                            MsgBox.Show("Warning", "Failed to read Tax from crsrTax");
                            return;
                        } else {

                            dServiceTaxPercent = crsrTax.getDouble(crsrTax.getColumnIndex("TotalPercentage"));
                        }*/
                        dServiceTaxPercent = crsrItem.getDouble(crsrItem.getColumnIndex("ServiceTaxPercent"));
                    }
                    // Service Tax Amount
                    if (crsrSettings.getInt(crsrSettings.getColumnIndex("Tax")) == 1) { // Forward
                        // Tax
                        dServiceTaxAmt = (dRate - dDiscAmt) * (dServiceTaxPercent / 100);
                    } else { // Reverse Tax
                        double dBasePrice = 0;
                        dBasePrice = dRate / (1 + (dServiceTaxPercent / 100));
                        dServiceTaxAmt = (dBasePrice - dDiscAmt) * (dServiceTaxPercent / 100);
                    }
                    tvServiceTaxAmt = new TextView(BillingCounterSalesActivity.this);
                    tvServiceTaxAmt.setWidth(50);
                    tvServiceTaxAmt.setText(String.format("%.2f", dServiceTaxAmt));
//                    MsgBox.Show("", tvServiceTaxAmt.getText().toString());


                    // Department Code
                    tvDeptCode = new TextView(BillingCounterSalesActivity.this);
                    tvDeptCode.setWidth(50);
                    tvDeptCode.setText(crsrItem.getString(crsrItem.getColumnIndex("DeptCode")));

                    // Category Code
                    tvCategCode = new TextView(BillingCounterSalesActivity.this);
                    tvCategCode.setWidth(50);
                    tvCategCode.setText(crsrItem.getString(crsrItem.getColumnIndex("CategCode")));

                    // Kitchen Code
                    tvKitchenCode = new TextView(BillingCounterSalesActivity.this);
                    tvKitchenCode.setWidth(50);
                    tvKitchenCode.setText(crsrItem.getString(crsrItem.getColumnIndex("KitchenCode")));

                    // Tax Type [Forward - 1/ Reverse - 2]
                    tvTaxType = new TextView(BillingCounterSalesActivity.this);
                    tvTaxType.setWidth(50);
                    tvTaxType.setText(crsrItem.getString(crsrItem.getColumnIndex("TaxType")));

                    // Modifier Charge
                    tvModifierCharge = new TextView(BillingCounterSalesActivity.this);
                    tvModifierCharge.setWidth(50);
                    tvModifierCharge.setText("0.0");

                    TextView tvUOM = new TextView(BillingCounterSalesActivity.this);
                    tvUOM.setWidth(50);
                    tvUOM.setText(crsrItem.getString(crsrItem.getColumnIndex("UOM")));

                    // SupplyType
                    TextView SupplyType = new TextView(BillingCounterSalesActivity.this);
                    SupplyType.setText(crsrItem.getString(crsrItem.getColumnIndex("SupplyType")));
                    SupplyType.setWidth(30);

                    TextView tvSpace = new TextView(BillingCounterSalesActivity.this);
                    tvSpace.setText("        ");

                    // Delete
                    int res = getResources().getIdentifier("delete", "drawable", this.getPackageName());
                    ImageButton ImgDelete = new ImageButton(BillingCounterSalesActivity.this);
                    ImgDelete.setImageResource(res);
                    // btnDelete.setText(crsrItem.getString(crsrItem.getColumnIndex("MenuCode")));
                    ImgDelete.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            final View v1 = v;
                            AlertDialog.Builder AuthorizationDialog = new AlertDialog.Builder(BillingCounterSalesActivity.this);
                            LayoutInflater UserAuthorization = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View vwAuthorization = UserAuthorization.inflate(R.layout.deleteconfirmation, null);
                            AuthorizationDialog
                                    .setTitle("Confimation")
                                    .setView(vwAuthorization)
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            View row = (View) v1.getParent();
                                            ViewGroup container = ((ViewGroup) row.getParent());
                                            container.removeView(row);
                                            container.invalidate();
                                            CalculateTotalAmount();

                                        }
                                    }).show();
                        }
                    });
                    TextView tvSpace1 = new TextView(BillingCounterSalesActivity.this);
                    tvSpace1.setText("       ");
                    TextView tvPrintKOTStatus = new TextView(BillingCounterSalesActivity.this);
                    tvPrintKOTStatus.setText("1");
                    // Add all text views and edit text to Item Row
                    rowItem.addView(chkNumber);//0
                    rowItem.addView(tvName);//1
                    rowItem.addView(tvHSn);//2
                    rowItem.addView(etQty);//3
                    rowItem.addView(etRate);//4
                    rowItem.addView(tvAmount);//5
                    rowItem.addView(tvTaxPercent);//6
                    rowItem.addView(tvTaxAmt);//7
                    rowItem.addView(tvDiscPercent);//8
                    rowItem.addView(tvDiscAmt);//9
                    rowItem.addView(tvDeptCode);//10
                    rowItem.addView(tvCategCode);//11
                    rowItem.addView(tvKitchenCode);//12
                    rowItem.addView(tvTaxType);//13
                    rowItem.addView(tvModifierCharge);//14
                    rowItem.addView(tvServiceTaxPercent);//15
                    rowItem.addView(tvServiceTaxAmt);//16
                    rowItem.addView(SupplyType);//17
                    rowItem.addView(tvSpace);//18
                    rowItem.addView(ImgDelete);//19
                    rowItem.addView(tvSpace1);//20
                    rowItem.addView(tvPrintKOTStatus);//21
                    rowItem.addView(tvUOM);//22
                    tblOrderItems.addView(rowItem, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                bItemExists = false;
            } while (crsrItem.moveToNext());
            CalculateTotalAmount();
        }
        else
        {
            Log.d("AddItemToOrderTable", "ItemNotFound Exception");
        }
    }


    /*************************************************************************************************************************************
     * Calculates all values such as Tax, Discount, Sub Total and grand total
     * whenever Quantity or Rate text value changed
     *************************************************************************************************************************************/
    private void Qty_Rate_Edit() {

        String strQty = "0";
        double dTaxPercent = 0, dDiscPercent = 0, dDiscAmt = 0, dTempAmt = 0, dTaxAmt = 0;
        double dRate;
        try {
            for (int iRow = 0; iRow < tblOrderItems.getChildCount(); iRow++) {

                TableRow Row = (TableRow) tblOrderItems.getChildAt(iRow);

                // Check against item number present in table
                if (Row.getChildAt(0) != null) {

                    CheckBox Number = (CheckBox) Row.getChildAt(0);
                    TextView ItemName = (TextView) Row.getChildAt(1);


                    // Quantity
                    EditText Qty = (EditText) Row.getChildAt(3);
                    Qty.setSelectAllOnFocus(true);
                    strQty = Qty.getText().toString().equalsIgnoreCase("") ? "0" : Qty.getText().toString(); // Temp
                    Cursor crsrSett = db.getBillSettings();
                    if(crsrSett!=null && crsrSett.moveToFirst())
                    {
                        int BillwithStock = crsrSett.getInt(crsrSett.getColumnIndex("BillwithStock"));
                        if (BillwithStock == 1) {
                            Cursor ItemCrsr = db.getItemDetails(ItemName.getText().toString());
                            if(ItemCrsr!=null && ItemCrsr.moveToFirst())
                            {
                                double availableStock = ItemCrsr.getDouble(ItemCrsr.getColumnIndex("Quantity"));
                                if ( availableStock < Float.valueOf(Qty.getText().toString())) {
                                    messageDialog.Show("Warning", "Stock is less, present stock quantity is "
                                            + String.valueOf(availableStock));
                                    Qty.setText(String.format("%.2f", availableStock));

                                    return;
                                }
                            }

                        }
                    }



                    // Amount
                    EditText Rate = (EditText) Row.getChildAt(4);
                    Rate.setSelectAllOnFocus(true);
                    TextView Amount = (TextView) Row.getChildAt(5);
                    dRate = Double.parseDouble(
                            Rate.getText().toString().equalsIgnoreCase("") ? "0" : Rate.getText().toString()); // Temp
                    Amount.setText(
                            String.format("%.2f", (Double.parseDouble(Qty.getText().toString()) * dRate)));

                    // Tax and Discount Amount
                    TextView TaxPer = (TextView) Row.getChildAt(6);
                    TextView TaxAmt = (TextView) Row.getChildAt(7);
                    TextView DiscPer = (TextView) Row.getChildAt(8);
                    TextView DiscAmt = (TextView) Row.getChildAt(9);
                    TextView ServiceTax = (TextView) Row.getChildAt(15);
                    TextView ServiceTaxAmt = (TextView) Row.getChildAt(16);
                    // TextView Delete = (TextView) Row.getChildAt(16);

                    dTaxPercent = Double.parseDouble(TaxPer.getText().toString().equalsIgnoreCase("") ? "0" : TaxPer.getText().toString()); // Temp
                    dDiscPercent = Double.parseDouble(DiscPer.getText().toString().equalsIgnoreCase("") ? "0"
                            : DiscPer.getText().toString()); // Temp

                    if (crsrSettings.getInt(crsrSettings.getColumnIndex("Tax")) == 1) {
                        // Discount
                        dDiscAmt = dRate * (dDiscPercent / 100);
                        dTempAmt = dDiscAmt;
                        dDiscAmt = dDiscAmt * Double.parseDouble(Qty.getText().toString());

                        // Tax
                        dTaxAmt = (dRate - dTempAmt) * (dTaxPercent / 100);
                        dTaxAmt = dTaxAmt * Double.parseDouble(Qty.getText().toString());

                        TaxAmt.setText(String.format("%.2f", dTaxAmt));
                        DiscAmt.setText(String.format("%.2f", dDiscAmt));

                    } else {
                        double dBasePrice = 0;
                        dBasePrice = dRate / (1 + (dTaxPercent / 100));

                        // Discount
                        dDiscAmt = dBasePrice * (dDiscPercent / 100);
                        dTempAmt = dDiscAmt;
                        dDiscAmt = dDiscAmt * Double.parseDouble(Qty.getText().toString());

                        // Tax
                        dTaxAmt = (dBasePrice - dTempAmt) * (dTaxPercent / 100);
                        dTaxAmt = dTaxAmt * Double.parseDouble(Qty.getText().toString());

                        TaxAmt.setText(String.format("%.2f", dTaxAmt));
                        DiscAmt.setText(String.format("%.2f", dDiscAmt));
                    }

                    // Service Tax charge
                    dTaxAmt = dRate * (Double.parseDouble(ServiceTax.getText().toString()) / 100);
                    ServiceTaxAmt.setText(
                            String.format("%.2f", (Double.parseDouble(Qty.getText().toString()) * dTaxAmt)));

                    // // delete
                    // Delete.setText("Hi");

                    // Clear all variables and set ItemExists to TRUE
                    // and break from the loop
                    dRate = 0;
                    dTaxPercent = 0;
                    dDiscPercent = 0;
                    dTaxAmt = 0;
                    dDiscAmt = 0;
                    dTempAmt = 0;
                    //bItemExists = true;

                }

            }
            CalculateTotalAmount();
        } catch (Exception e) {
            messageDialog.setMessage("Error while changing quantity directly :" + e.getMessage()).setPositiveButton("OK", null).show();
        }
    }

    /*************************************************************************************************************************************
     * Calculates bill sub total, sales tax amount, service tax amount and Bill
     * total amount.
     ************************************************************************************************************************************/

    /*************************************************************************************************************************************
     * Quantity / Rate column text box click event listener which selects all
     * the text present in text box
     *************************************************************************************************************************************/
    View.OnClickListener Qty_Rate_Click = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            ((EditText) v).setSelection(0, ((EditText) v).getText().length());
        }

    };

    /*************************************************************************************************************************************
     * Quantity / Rate column text key press event listener
     *************************************************************************************************************************************/
    View.OnKeyListener Qty_Rate_KeyPressEvent = new View.OnKeyListener() {

        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (v.getTag().toString().equalsIgnoreCase("QTY_RATE"))
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(((EditText) v).getWindowToken(), 0);
                    Qty_Rate_Edit();
                }
            }
            return false;
        }
    };

    private String getQuantityFromWeighScale() {
        return "1";
    }

    /*************************************************************************************************************************************
     * Calculates bill sub total, sales tax amount, service tax amount and Bill
     * total amount.
     ************************************************************************************************************************************/
    private void CalculateTotalAmount()
    {
        double dSubTotal = 0, dTaxTotal = 0, dModifierAmt = 0, dServiceTaxAmt = 0, dOtherCharges = 0, dTaxAmt = 0, dSerTaxAmt = 0;
        float dTaxPercent = 0, dSerTaxPercent = 0;
        // Item wise tax calculation ----------------------------
        for (int iRow = 0; iRow < tblOrderItems.getChildCount(); iRow++)
        {
            TableRow RowItem = (TableRow) tblOrderItems.getChildAt(iRow);
            if (RowItem.getChildAt(0) != null)
            {
                TextView ColTaxType = (TextView) RowItem.getChildAt(13);
                TextView ColAmount = (TextView) RowItem.getChildAt(5);
                TextView ColDisc = (TextView) RowItem.getChildAt(9);
                TextView ColTax = (TextView) RowItem.getChildAt(7);
                TextView ColModifierAmount = (TextView) RowItem.getChildAt(14);
                TextView ColServiceTaxAmount = (TextView) RowItem.getChildAt(16);
                dTaxTotal += Double.parseDouble(ColTax.getText().toString());
                dServiceTaxAmt += Double.parseDouble(ColServiceTaxAmount.getText().toString());
                dSubTotal += Double.parseDouble(ColAmount.getText().toString());
            }
        }
        // ------------------------------------------
        // Bill wise tax Calculation -------------------------------
        Cursor crsrtax = db.getTaxConfigs(1);
        if (crsrtax.moveToFirst()) {
            dTaxPercent = crsrtax.getFloat(crsrtax.getColumnIndex("TotalPercentage"));
            dTaxAmt += dSubTotal * (dTaxPercent / 100);
        }
        Cursor crsrtax1 = db.getTaxConfigs(2);
        if (crsrtax1.moveToFirst()) {
            dSerTaxPercent = crsrtax1.getFloat(crsrtax1.getColumnIndex("TotalPercentage"));
            dSerTaxAmt += dSubTotal * (dSerTaxPercent / 100);
        }
        // -------------------------------------------------

        dOtherCharges = Double.valueOf(textViewOtherCharges.getText().toString());
        //String strTax = crsrSettings.getString(crsrSettings.getColumnIndex("Tax"));
        if (crsrSettings.moveToFirst()) {
            if (crsrSettings.getString(crsrSettings.getColumnIndex("Tax")).equalsIgnoreCase("1")) {
                if (crsrSettings.getString(crsrSettings.getColumnIndex("TaxType")).equalsIgnoreCase("1"))
                {
                    if (/*chk_interstate.isChecked()*/false) // interstate
                    {
                        /*tvTaxTotal.setText(String.format("%.2f", dTaxTotal + dServiceTaxAmt));
                        //tvServiceTaxTotal.setText(String.format("%.2f", dServiceTaxAmt));
                        tvServiceTaxTotal.setText("");*/
                    }
                    else
                    {
                        tvTaxTotal.setText(String.format("%.2f", dTaxTotal));
                        tvServiceTaxTotal.setText(String.format("%.2f", dServiceTaxAmt));
                    }
                    tvSubTotal.setText(String.format("%.2f", dSubTotal));
                    tvBillAmount.setText(String.format("%.2f", dSubTotal + dTaxTotal + dServiceTaxAmt + dOtherCharges));
                }
                else
                {
                    if (/*chk_interstate.isChecked()*/false)
                    {
                        /*tvTaxTotal.setText(String.format("%.2f", dTaxAmt + dSerTaxAmt));
                        //tvServiceTaxTotal.setText(String.format("%.2f", dSerTaxAmt));
                        tvServiceTaxTotal.setText("");*/
                    }
                    else
                    {
                        tvTaxTotal.setText(String.format("%.2f", dTaxAmt));
                        tvServiceTaxTotal.setText(String.format("%.2f", dSerTaxAmt));
                    }
                    tvSubTotal.setText(String.format("%.2f", dSubTotal));
                    tvBillAmount.setText(String.format("%.2f", dSubTotal + dTaxAmt + dSerTaxAmt + dOtherCharges));
                }
            }
            else
            {
                if (crsrSettings.getString(crsrSettings.getColumnIndex("TaxType")).equalsIgnoreCase("1"))
                {
                    if (/*chk_interstate.isChecked()*/false)
                    {
                        /*tvTaxTotal.setText(String.format("%.2f", dTaxTotal + dServiceTaxAmt));
                        // tvServiceTaxTotal.setText(String.format("%.2f", dServiceTaxAmt));
                        tvServiceTaxTotal.setText("");*/
                    }
                    else
                    {
                        tvTaxTotal.setText(String.format("%.2f", dTaxTotal));
                        tvServiceTaxTotal.setText(String.format("%.2f", dServiceTaxAmt));
                    }
                    tvSubTotal.setText(String.format("%.2f", dSubTotal));
                    tvBillAmount.setText(String.format("%.2f", dSubTotal + dOtherCharges));

                }
                else
                {
                    tvSubTotal.setText(String.format("%.2f", dSubTotal));
                    if (/*chk_interstate.isChecked()*/false)
                    {
                        /*tvTaxTotal.setText(String.format("%.2f", dTaxAmt + dSerTaxAmt));
                        // tvServiceTaxTotal.setText(String.format("%.2f", dSerTaxAmt));
                        tvServiceTaxTotal.setText("");*/
                    }
                    else
                    {
                        tvTaxTotal.setText(String.format("%.2f", dTaxAmt));
                        tvServiceTaxTotal.setText(String.format("%.2f", dSerTaxAmt));
                    }
                    tvTaxTotal.setText(String.format("%.2f", dTaxAmt));
                    tvServiceTaxTotal.setText(String.format("%.2f", dSerTaxAmt));
                    tvBillAmount.setText(String.format("%.2f", dSubTotal + dOtherCharges));
                }
            }
        }
    }

    public void ControlsSetEnabled() {
        btn_DineInAddCustomer.setVisibility(View.VISIBLE);
        textViewOtherCharges.setEnabled(true);
        listViewDept.setEnabled(true);
        listViewCat.setEnabled(true);
        gridViewItems.setEnabled(true);
        //btnSplitBill.setEnabled(true);
        //btnPayBill.setEnabled(true);
        //btnSaveKOT.setEnabled(true);
        //btnKOTStatus.setEnabled(true);
        if(tblOrderItems.getChildCount()>0)
        {
            btn_PayBill.setEnabled(true);
            btn_PrintBill.setEnabled(true);
        }else
        {
            btn_PayBill.setEnabled(false);
            btn_PrintBill.setEnabled(false);
        }
        tblOrderItems.setEnabled(true);
        btn_Clear.setEnabled(true);
        btn_Reprint.setEnabled(true);
        autoCompleteTextViewSearchItem.setEnabled(true);
        autoCompleteTextViewSearchMenuCode.setEnabled(true);
        btnDept.setEnabled(true);
        btnCat.setEnabled(true);
        btnItems.setEnabled(true);
        if(jBillingMode==2) {
            btn_PrintBill.setEnabled(true);
            btn_PayBill.setEnabled(true);
        }
    }

    public void ControlsSetDisabled() {
        btn_DineInAddCustomer.setVisibility(View.VISIBLE);
        //tvHSNCode_out.setEnabled(false);
        autoCompleteTextViewSearchMenuCode.setEnabled(false);
        textViewOtherCharges.setEnabled(false);
        //btn_item_fastBillingMode.setEnabled(false);
        listViewDept.setEnabled(false);
        listViewCat.setEnabled(false);
        gridViewItems.setEnabled(false);
        //btnSplitBill.setEnabled(false);
        btn_PayBill.setEnabled(false);
        //btnSaveKOT.setEnabled(false);
        //btnKOTStatus.setEnabled(false);
        //btnPrintKOT.setEnabled(false);
        btn_PrintBill.setEnabled(false);
        tblOrderItems.setEnabled(false);
        btn_Clear.setEnabled(false);
        btn_Reprint.setEnabled(false);
        autoCompleteTextViewSearchItem.setEnabled(false);
        //spnr_pos.setEnabled(false);
        autoCompleteTextViewSearchMenuCode.setEnabled(false);
        btnDept.setEnabled(false);
        btnCat.setEnabled(false);
        btnItems.setEnabled(false);
        if(jBillingMode==2) {
            btn_PrintBill.setEnabled(true);
            btn_PayBill.setEnabled(true);
        }
    }

    /*************************************************************************************************************************************
     * Opens tender window in dine in and take away billing mode
     *************************************************************************************************************************************/
    private void Tender()
    {
        if (jBillingMode == 2 && Double.parseDouble(tvBillAmount.getText().toString()) <= 0)
        {
            messageDialog.Show("Warning", "Empty bill can not be tendered");
            return;
        }

        if (jBillingMode == Byte.parseByte("2"))
        {
            Intent intTender = new Intent(getApplicationContext(), PayBillActivity.class);
            Log.v("Debug", "Total Amount:" + tvBillAmount.getText().toString());
            intTender.putExtra("TotalAmount", tvBillAmount.getText().toString());
            intTender.putExtra("phone", editTextMobile.getText().toString());
            intTender.putExtra("USER_NAME", userName);
            startActivityForResult(intTender, 1);

        }
    }

    public void Tender1() {

        if (tvBillAmount.getText().toString().equals("") || tvBillAmount.getText().toString().equals("0.00"))
        {
            messageDialog.Show("Warning", "Please add item to make bill");
        }

        else
        {
            Intent intentTender = new Intent(getApplicationContext(), PayBillActivity.class);
            intentTender.putExtra("TotalAmount", tvBillAmount.getText().toString());
            intentTender.putExtra("CustId", customerId);
            intentTender.putExtra("phone", editTextMobile.getText().toString());
            intentTender.putExtra("USER_NAME", userName);
            startActivityForResult(intentTender, 1);
        }
    }


    /*************************************************************************************************************************************
     * Loads KOT order items to billing table
     *
     * @param crsrBillItems : Cursor with KOT order item details
     *************************************************************************************************************************************/
    private void LoadKOTItems(Cursor crsrBillItems) {
        EditTextInputHandler etInputValidate = new EditTextInputHandler();
        TableRow rowItem;
        TextView tvName, tvHSn, tvAmount, tvTaxPercent, tvTaxAmt, tvDiscPercent, tvDiscAmt, tvDeptCode, tvCategCode, tvKitchenCode, tvTaxType, tvModifierCharge, tvServiceTaxPercent, tvServiceTaxAmt;
        EditText etQty, etRate;
        CheckBox Number;
        ImageButton ImgDelete;
        if (crsrBillItems.moveToFirst())
        {
            /*iTokenNumber = crsrBillItems.getInt(crsrBillItems.getColumnIndex("TokenNumber"));
            tvWaiterNumber.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("EmployeeId")));
            // Get Table number
            tvTableNumber.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("TableNumber")));
            // Get Table Split No
            tvTableSplitNo.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("TableSplitNo")));
            // Get Sub Udf number
            tvSubUdfValue.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("SubUdfNumber")));*/
            // Get Cust Id
            customerId = crsrBillItems.getString(crsrBillItems.getColumnIndex("CustId"));
            Cursor crsrCustomer = db.getCustomerById(crsrBillItems.getInt(crsrBillItems.getColumnIndex("CustId")));
            if (crsrCustomer.moveToFirst())
            {
                editTextMobile.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustContactNumber")));
                editTextName.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustName")));
                editTextAddress.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustAddress")));
            }

            // Display items in table
            do {
                rowItem = new TableRow(BillingCounterSalesActivity.this);
                rowItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                // Item Number
                Number = new CheckBox(BillingCounterSalesActivity.this);
                Number.setWidth(40);
                Number.setTextSize(0);
                Number.setTextColor(Color.TRANSPARENT);
                Number.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("ItemNumber")));

                // Item Name
                tvName = new TextView(BillingCounterSalesActivity.this);
                tvName.setWidth(135);
                tvName.setTextSize(11);
                tvName.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("ItemName")));

                //hsn code
                tvHSn = new TextView(BillingCounterSalesActivity.this);
                tvHSn.setWidth(67); // 154px ~= 230dp
                tvHSn.setTextSize(11);
                if (GSTEnable.equalsIgnoreCase("1") && (HSNEnable_out != null) && HSNEnable_out.equals("1")) {
                    tvHSn.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("HSNCode")));
                }
                // Quantity
                etQty = new EditText(BillingCounterSalesActivity.this);
                etQty.setWidth(55);
                etQty.setTextSize(11);
                if (crsrBillItems.getString(crsrBillItems.getColumnIndex("PrintKOTStatus")).equalsIgnoreCase("1")) {
                    etQty.setEnabled(true);
                } else {
                    etQty.setEnabled(false);
                }
                etQty.setText(String.format("%.2f", crsrBillItems.getDouble(crsrBillItems.getColumnIndex("Quantity"))));
                etQty.setSelectAllOnFocus(true);
                etQty.setTag("QTY_RATE");
                if(jBillingMode ==2 || jBillingMode ==3 || jBillingMode ==4)
                {
                    etQty.setOnClickListener(Qty_Rate_Click);
                    etQty.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    etQty.setOnKeyListener(Qty_Rate_KeyPressEvent);
                    etInputValidate.ValidateDecimalInput(etQty);
                    etQty.addTextChangedListener(new TextWatcher() {
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        public void afterTextChanged(Editable s) {
                            Qty_Rate_Edit();
                        }
                    });
                }


                // Rate
                etRate = new EditText(BillingCounterSalesActivity.this);
                etRate.setWidth(70);
                etRate.setEnabled(false);
                etRate.setTextSize(11);
                etRate.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                etRate.setText(String.format("%.2f", crsrBillItems.getDouble(crsrBillItems.getColumnIndex("Rate"))));

                // Amount
                tvAmount = new TextView(BillingCounterSalesActivity.this);
                tvAmount.setWidth(60);
                tvAmount.setTextSize(11);
                tvAmount.setGravity(Gravity.RIGHT | Gravity.END);
                tvAmount.setText(
                        String.format("%.2f", crsrBillItems.getDouble(crsrBillItems.getColumnIndex("Amount"))));

                // Sales Tax%
                tvTaxPercent = new TextView(BillingCounterSalesActivity.this);
                tvTaxPercent.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("TaxPercent")));

                // Sales Tax Amount
                tvTaxAmt = new TextView(BillingCounterSalesActivity.this);
                tvTaxAmt.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("TaxAmount")));

                // Discount %
                tvDiscPercent = new TextView(BillingCounterSalesActivity.this);
                tvDiscPercent.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("DiscountPercent")));

                // Discount Amount
                tvDiscAmt = new TextView(BillingCounterSalesActivity.this);
                tvDiscAmt.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("DiscountAmount")));

                // Dept Code
                tvDeptCode = new TextView(BillingCounterSalesActivity.this);
                tvDeptCode.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("DeptCode")));

                // Categ Code
                tvCategCode = new TextView(BillingCounterSalesActivity.this);
                tvCategCode.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("CategCode")));

                // Kitchen Code
                tvKitchenCode = new TextView(BillingCounterSalesActivity.this);
                tvKitchenCode.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("KitchenCode")));

                // Tax Type
                tvTaxType = new TextView(BillingCounterSalesActivity.this);
                tvTaxType.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("TaxType")));

                // Modifier Amount
                tvModifierCharge = new TextView(BillingCounterSalesActivity.this);
                tvModifierCharge.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("ModifierAmount")));

                // Service Tax %
                tvServiceTaxPercent = new TextView(BillingCounterSalesActivity.this);
                tvServiceTaxPercent.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("ServiceTaxPercent")));

                // Service Tax Amount
                tvServiceTaxAmt = new TextView(BillingCounterSalesActivity.this);
                tvServiceTaxAmt.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("ServiceTaxAmount")));

                // Service Tax Amount
                TextView tvSupplyType = new TextView(BillingCounterSalesActivity.this);
                tvSupplyType.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("SupplyType")));


                // Delete
                int res = getResources().getIdentifier("delete", "drawable", this.getPackageName());
                ImgDelete = new ImageButton(BillingCounterSalesActivity.this);
                ImgDelete.setImageResource(res);
                ImgDelete.setVisibility(View.INVISIBLE);



                TextView tvSpace = new TextView(BillingCounterSalesActivity.this);
                tvSpace.setText("        ");

                TextView tvSpace1 = new TextView(BillingCounterSalesActivity.this);
                tvSpace1.setText("       ");

                TextView tvPrintKOTStatus = new TextView(BillingCounterSalesActivity.this);
                /*if(REPRINT_KOT == 1)
                    tvPrintKOTStatus.setText("1");
                else
                    tvPrintKOTStatus.setText(crsrBillItems.getString(crsrBillItems.getColumnIndex("PrintKOTStatus")));*/



                // Add all text views and edit text to Item Row
                // rowItem.addView(tvNumber);
                rowItem.addView(Number);
                rowItem.addView(tvName);
                rowItem.addView(tvHSn);
                rowItem.addView(etQty);
                rowItem.addView(etRate);
                rowItem.addView(tvAmount);
                rowItem.addView(tvTaxPercent);
                rowItem.addView(tvTaxAmt);
                rowItem.addView(tvDiscPercent);
                rowItem.addView(tvDiscAmt);
                rowItem.addView(tvDeptCode);
                rowItem.addView(tvCategCode);
                rowItem.addView(tvKitchenCode);
                rowItem.addView(tvTaxType);
                rowItem.addView(tvModifierCharge);
                rowItem.addView(tvServiceTaxPercent);
                rowItem.addView(tvServiceTaxAmt);
                rowItem.addView(tvSupplyType);
                rowItem.addView(tvSpace);
                rowItem.addView(ImgDelete);
                rowItem.addView(tvSpace1);
                rowItem.addView(tvPrintKOTStatus);

                // Add row to table
                tblOrderItems.addView(rowItem, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            } while (crsrBillItems.moveToNext());

            //REPRINT_KOT =0;

            CalculateTotalAmount();
            Log.d("LoadKOTItems", "Items loaded successfully");
        } else {
            Log.d("LoadKOTItems", "No rows in cursor");
        }
    }

    // -----Print Bill Code Started-----

    public void printBILL()
    {
        int proceed =1;
        if (tblOrderItems.getChildCount() < 1)
        {
            messageDialog.Show("Warning", "Add Item before Printing Bill");
            proceed =0;
        }
        else if (jBillingMode==4 )
        {
            String tempCustId = customerId;
            if (tempCustId.equalsIgnoreCase("") || tempCustId.equalsIgnoreCase("0")) {
                messageDialog.Show("Warning", "Please Select Customer for Billing");
                proceed =0;
            }
        }
        if(proceed == 0)
            return;
        if (isPrinterAvailable)
        {
            strPaymentStatus = "Paid";
            PrintBillPayment = 1;
            // Print Bill with Save Bill
            if (tblOrderItems.getChildCount() < 1)
            {
                messageDialog.Show("Warning", "Insert item before Print Bill");
                return;
            }
            else
            {
                l(2, true);
                PrintNewBill();
                Toast.makeText(getApplicationContext(), "Bill Saved Successfully", Toast.LENGTH_SHORT).show();
                if (jBillingMode == 2)
                {
                    ClearAll();
                    btn_PrintBill.setEnabled(true);
                }
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Printer is not ready", Toast.LENGTH_SHORT).show();
            askForConfig();
        }
    }

    private void l(int TenderType, boolean isPrintBill) { // TenderType:
        InsertBillItems();
        InsertBillDetail(TenderType);
    }

    /*************************************************************************************************************************************
     * Insert each bill item to bill items database table
     *************************************************************************************************************************************/
    private void InsertBillItems() {

        // Inserted Row Id in database table
        long lResult = 0;

        // Bill item object
        BillItem objBillItem;

        // Reset TotalItems count
        iTotalItems = 0;

        Cursor crsrUpdateItemStock = null;

        for (int iRow = 0; iRow < tblOrderItems.getChildCount(); iRow++) {
            objBillItem = new BillItem();

            TableRow RowBillItem = (TableRow) tblOrderItems.getChildAt(iRow);

            // Increment Total item count if row is not empty
            if (RowBillItem.getChildCount() > 0) {
                iTotalItems++;
            }

            // Bill Number
            objBillItem.setBillNumber(editTextOrderNo.getText().toString());
            Log.d("InsertBillItems", "InvoiceNo:" + editTextOrderNo.getText().toString());

            // richa_2012
            //BillingMode
            objBillItem.setBillingMode(String.valueOf(jBillingMode));
            Log.d("InsertBillItems", "Billing Mode :" + String.valueOf(jBillingMode));

            // Item Number
            if (RowBillItem.getChildAt(0) != null) {
                CheckBox ItemNumber = (CheckBox) RowBillItem.getChildAt(0);
                objBillItem.setItemNumber(Integer.parseInt(ItemNumber.getText().toString()));
                Log.d("InsertBillItems", "Item Number:" + ItemNumber.getText().toString());

                crsrUpdateItemStock = db.getItemss(Integer.parseInt(ItemNumber.getText().toString()));
            }

            // Item Name
            if (RowBillItem.getChildAt(1) != null) {
                TextView ItemName = (TextView) RowBillItem.getChildAt(1);
                objBillItem.setItemName(ItemName.getText().toString());
                Log.d("InsertBillItems", "Item Name:" + ItemName.getText().toString());
            }

            if (RowBillItem.getChildAt(2) != null) {
                TextView HSN = (TextView) RowBillItem.getChildAt(2);
                objBillItem.setHSNCode(HSN.getText().toString());
                Log.d("InsertBillItems", "Item HSN:" + HSN.getText().toString());
            }

            // Quantity
            if (RowBillItem.getChildAt(3) != null) {
                EditText Quantity = (EditText) RowBillItem.getChildAt(3);
                objBillItem.setQuantity(Float.parseFloat(Quantity.getText().toString()));
                Log.d("InsertBillItems", "Quantity:" + Quantity.getText().toString());

                if (crsrUpdateItemStock!=null && crsrUpdateItemStock.moveToFirst()) {
                    // Check if item's bill with stock enabled update the stock
                    // quantity
                    Cursor billsettingCursor = db.getBillSettings();
                    if(billsettingCursor!= null && billsettingCursor.moveToFirst())
                    {
                        //String i = billsettingCursor.getString(billsettingCursor.getColumnIndex("BillwithStock"));
                        if (billsettingCursor.getInt(billsettingCursor.getColumnIndex("BillwithStock")) == 1) {
                            UpdateItemStock(crsrUpdateItemStock, Float.parseFloat(Quantity.getText().toString()));
                        }
                    }

                }
            }

            // Rate
            if (RowBillItem.getChildAt(4) != null) {
                EditText Rate = (EditText) RowBillItem.getChildAt(4);
                objBillItem.setValue(Float.parseFloat(Rate.getText().toString()));
                Log.d("InsertBillItems", "Rate:" + Rate.getText().toString());
            }

            // Amount
            if (RowBillItem.getChildAt(5) != null) {
                TextView Amount = (TextView) RowBillItem.getChildAt(5);
                objBillItem.setAmount(Float.parseFloat(Amount.getText().toString()));
                Log.d("InsertBillItems", "Taxable Value:" + Amount.getText().toString());
            }

            // Sales Tax %
            if (RowBillItem.getChildAt(6) != null) {
                TextView SalesTaxPercent = (TextView) RowBillItem.getChildAt(6);
                if (GSTEnable.equals("1")) {
                    String taxName = tvTaxTotal.getText().toString();
                    if (taxName.equalsIgnoreCase("IGST TAX")) {
                        objBillItem.setIGSTRate((Float.parseFloat(SalesTaxPercent.getText().toString())) * 2);
                        Log.d("InsertBillItems", " IGST Tax %:" + objBillItem.getIGSTRate());
                    } else {
                        objBillItem.setCGSTRate(Float.parseFloat(SalesTaxPercent.getText().toString()));
                        Log.d("InsertBillItems", " CGST Tax %:" + SalesTaxPercent.getText().toString());
                    }

                } else {
                    objBillItem.setTaxPercent(Float.parseFloat(SalesTaxPercent.getText().toString()));
                    Log.d("InsertBillItems", "Tax %:" + SalesTaxPercent.getText().toString());
                }
            }

            // Sales Tax Amount
            if (RowBillItem.getChildAt(7) != null) {
                TextView SalesTaxAmount = (TextView) RowBillItem.getChildAt(7);
                if (GSTEnable.equals("1")) {
                    String taxName = tvTaxTotal.getText().toString();
                    if (taxName.equalsIgnoreCase("IGST TAX")) {
                        objBillItem.setIGSTAmount((Float.parseFloat(SalesTaxAmount.getText().toString()) * 2));
                        Log.d("InsertBillItems", "IGST Amt:" + objBillItem.getIGSTAmount());
                    } else {
                        objBillItem.setCGSTAmount(Float.parseFloat(SalesTaxAmount.getText().toString()));
                        Log.d("InsertBillItems", "CGST Amt:" + SalesTaxAmount.getText().toString());
                    }

                } else {
                    objBillItem.setTaxAmount(Float.parseFloat(SalesTaxAmount.getText().toString()));
                    Log.d("InsertBillItems", "Tax Amt:" + SalesTaxAmount.getText().toString());
                }
            }

            // Discount %
            if (RowBillItem.getChildAt(8) != null) {
                TextView DiscountPercent = (TextView) RowBillItem.getChildAt(8);
                objBillItem.setDiscountPercent(Float.parseFloat(DiscountPercent.getText().toString()));
                Log.d("InsertBillItems", "Disc %:" + DiscountPercent.getText().toString());
            }

            // Discount Amount
            if (RowBillItem.getChildAt(9) != null) {
                TextView DiscountAmount = (TextView) RowBillItem.getChildAt(9);
                objBillItem.setDiscountAmount(Float.parseFloat(DiscountAmount.getText().toString()));
                Log.d("InsertBillItems", "Disc Amt:" + DiscountAmount.getText().toString());

                // fTotalDiscount += Float.parseFloat(DiscountAmount.getText().toString());
            }

            // Department Code
            if (RowBillItem.getChildAt(10) != null) {
                TextView DeptCode = (TextView) RowBillItem.getChildAt(10);
                objBillItem.setDeptCode(Integer.parseInt(DeptCode.getText().toString()));
                Log.d("InsertBillItems", "Dept Code:" + DeptCode.getText().toString());
            }

            // Category Code
            if (RowBillItem.getChildAt(11) != null) {
                TextView CategCode = (TextView) RowBillItem.getChildAt(11);
                objBillItem.setCategCode(Integer.parseInt(CategCode.getText().toString()));
                Log.d("InsertBillItems", "Categ Code:" + CategCode.getText().toString());
            }

            // Kitchen Code
            if (RowBillItem.getChildAt(12) != null) {
                TextView KitchenCode = (TextView) RowBillItem.getChildAt(12);
                objBillItem.setKitchenCode(Integer.parseInt(KitchenCode.getText().toString()));
                Log.d("InsertBillItems", "Kitchen Code:" + KitchenCode.getText().toString());
            }

            // Tax Type
            if (RowBillItem.getChildAt(13) != null) {
                TextView TaxType = (TextView) RowBillItem.getChildAt(13);
                objBillItem.setTaxType(Integer.parseInt(TaxType.getText().toString()));
                Log.d("InsertBillItems", "Tax Type:" + TaxType.getText().toString());
            }

            // Modifier Amount
            if (RowBillItem.getChildAt(14) != null) {
                TextView ModifierAmount = (TextView) RowBillItem.getChildAt(14);
                objBillItem.setModifierAmount(Float.parseFloat(ModifierAmount.getText().toString()));
                Log.d("InsertBillItems", "Modifier Amt:" + ModifierAmount.getText().toString());
            }

            // Service Tax Percent
            if (RowBillItem.getChildAt(15) != null) {
                TextView ServiceTaxPercent = (TextView) RowBillItem.getChildAt(15);
                if (GSTEnable.equals("1")) {
                    String taxName = tvServiceTaxTotal.getText().toString();
                    if (taxName.equalsIgnoreCase("SGST TAX")) {
                        objBillItem.setSGSTRate(Float.parseFloat(ServiceTaxPercent.getText().toString()));
                        Log.d("InsertBillItems", "SGST Tax %:" + ServiceTaxPercent.getText().toString());
                    }

                } else {
                    objBillItem.setServiceTaxPercent(Float.parseFloat(ServiceTaxPercent.getText().toString()));
                    Log.d("InsertBillItems", "Service Tax %:" + ServiceTaxPercent.getText().toString());
                }
            }

            // Service Tax Amount
            if (RowBillItem.getChildAt(16) != null) {
                TextView ServiceTaxAmount = (TextView) RowBillItem.getChildAt(16);
                if (GSTEnable.equals("1")) {
                    String taxName = tvServiceTaxTotal.getText().toString();
                    if (taxName.equalsIgnoreCase("SGST TAX")) {
                        objBillItem.setSGSTAmount(Float.parseFloat(ServiceTaxAmount.getText().toString()));
                        Log.d("InsertBillItems", "SGST Amount :" + ServiceTaxAmount.getText().toString());
                    }

                } else {

                    objBillItem.setServiceTaxAmount(Float.parseFloat(ServiceTaxAmount.getText().toString()));
                    Log.d("InsertBillItems", "Service Tax Amt:" + ServiceTaxAmount.getText().toString());
                }
            }

            if (RowBillItem.getChildAt(17) != null) {
                TextView SupplyType = (TextView) RowBillItem.getChildAt(17);
                objBillItem.setSupplyType(SupplyType.getText().toString());
                Log.d("InsertBillItems", "SupplyType:" + SupplyType.getText().toString());
                /*if (GSTEnable.equals("1")) {
                    objBillItem.setSupplyType(SupplyType.getText().toString());
                    Log.d("InsertBillItems", "SupplyType:" + SupplyType.getText().toString());
                } else {
                    objBillItem.setSupplyType("");
                }*/
            }
            if (RowBillItem.getChildAt(22) != null) {
                TextView UOM = (TextView) RowBillItem.getChildAt(22);
                objBillItem.setUom(UOM.getText().toString());
                Log.d("InsertBillItems", "UOM:" + UOM.getText().toString());

            }

            // subtotal
            float subtotal = objBillItem.getAmount() + objBillItem.getIGSTAmount() + objBillItem.getCGSTAmount() + objBillItem.getSGSTAmount();
            objBillItem.setSubTotal(subtotal);
            Log.d("InsertBillItems", "Sub Total :" + subtotal);

            // Date
            objBillItem.setInvoiceDate(String.valueOf(d.getTime()));

            // cust name
            String custname = editTextName.getText().toString();
            objBillItem.setCustName(custname);
            Log.d("InsertBillDetail", "CustName :" + custname);

            // cust StateCode
            //String custStateCode =spnr_pos.getSelectedItem().toString();
            //String str = spnr_pos.getSelectedItem().toString();
            //int length = str.length();
            String custStateCode = "";
            /*if (length > 0) {
                custStateCode = str.substring(length - 2, length);
            }*/
            objBillItem.setCustStateCode(custStateCode);
            Log.d("InsertBillDetail", "CustStateCode :" + custStateCode);

            // BusinessType
            /*if (etCustGSTIN.getText().toString().equals("")) {
            if (etCustGSTIN.getText().toString().equals(""))
            {
                objBillItem.setBusinessType("B2C");
            }
            else // gstin present means b2b bussiness
            {
                objBillItem.setBusinessType("B2B");
            }*/
            objBillItem.setBusinessType("B2C");
            Log.d("InsertBillDetail", "BusinessType : " + objBillItem.getBusinessType());

            // richa to do - hardcoded b2b bussinies type
            //objBillItem.setBusinessType("B2B");
            lResult = db.addBillItems(objBillItem);
            Log.d("InsertBillItem", "Bill item inserted at position:" + lResult);
        }
    }

    /*************************************************************************************************************************************
     * Inserts bill details to bill detail database table
     *
     * @param TenderType : Type of tender, 1 - Pay cash, 2 - Tender Screen payment
     *************************************************************************************************************************************/
    private void InsertBillDetail(int TenderType) {

        // Inserted Row Id in database table
        long lResult = 0;

        // BillDetail object
        BillDetail objBillDetail;

        objBillDetail = new BillDetail();

        // Date
        //objBillDetail.setDate(String.valueOf(d.getTime()));
        objBillDetail.setDate(tvDate.getText().toString());
        Log.d("InsertBillDetail", "Date:" + d.getTime());

        // Time
        objBillDetail.setTime(String.format("%tR", Time));
        Log.d("InsertBillDetail", "Time:" + String.format("%tR", Time));

        // Bill Number
        objBillDetail.setBillNumber(Integer.parseInt(editTextOrderNo.getText().toString()));
        Log.d("InsertBillDetail", "Bill Number:" + editTextOrderNo.getText().toString());

        // richa_2012
        //BillingMode
        objBillDetail.setBillingMode(String.valueOf(jBillingMode));
        Log.d("InsertBillDetail", "Billing Mode :" + String.valueOf(jBillingMode));


        // pos
        /*if (chk_interstate.isChecked()) {
            String str = spnr_pos.getSelectedItem().toString();
            int length = str.length();
            String sub = "";
            if (length > 0) {
                sub = str.substring(length - 2, length);
            }
            objBillDetail.setPOS(sub);
            Log.d("InsertBillDetail", "POS :" + sub);
        } else {
            objBillDetail.setPOS("");
            Log.d("InsertBillDetail", "POS :");
        }*/


        // Total Items
        objBillDetail.setTotalItems(iTotalItems);
        Log.d("InsertBillDetail", "Total Items:" + iTotalItems);

        // Bill Amount
        String billamt_temp = String.format("%.2f",Float.parseFloat(tvBillAmount.getText().toString()));
        objBillDetail.setBillAmount(Float.parseFloat(billamt_temp));
        Log.d("InsertBillDetail", "Bill Amount:" + tvBillAmount.getText().toString());

        // Discount Amount
        objBillDetail.setTotalDiscountAmount(fTotalDiscount);
        Log.d("InsertBillDetail", "Total Discount:" + fTotalDiscount);

        // Sales Tax Amount
        if (GSTEnable.equals("1")) {
            if (chk_interstate.isChecked()) {
                objBillDetail.setIGSTAmount(Float.parseFloat(tvTaxTotal.getText().toString()));
                objBillDetail.setCGSTAmount(0);
                objBillDetail.setSGSTAmount(0);
            } else {
                objBillDetail.setIGSTAmount(0);
                objBillDetail.setCGSTAmount(Float.parseFloat(tvTaxTotal.getText().toString()));
                objBillDetail.setSGSTAmount(Float.parseFloat(tvServiceTaxTotal.getText().toString()));
            }
            Log.d("InsertBillDetail", "IGSTAmount : " + objBillDetail.getIGSTAmount());
            Log.d("InsertBillDetail", "CGSTAmount : " + objBillDetail.getCGSTAmount());
            Log.d("InsertBillDetail", "SGSTAmount : " + objBillDetail.getSGSTAmount());
        } else {
            objBillDetail.setTotalTaxAmount(Float.parseFloat(tvTaxTotal.getText().toString()));
            Log.d("InsertBillDetail", "Total Tax:" + tvTaxTotal.getText().toString());

            // Service Tax Amount
            objBillDetail.setTotalServiceTaxAmount(Float.parseFloat(tvServiceTaxTotal.getText().toString()));
            Log.d("InsertBillDetail", "Service Tax:" + tvServiceTaxTotal.getText().toString());

        }


        // Delivery Charge
        objBillDetail.setDeliveryCharge(Float.parseFloat(textViewOtherCharges.getText().toString()));
        Log.d("InsertBillDetail", "Delivery Charge:0");


        // Taxable Value
        float taxval_f = Float.parseFloat(tvSubTotal.getText().toString());
        objBillDetail.setAmount(String.valueOf(taxval_f));
        Log.d("InsertBillDetail", "Taxable Value:" + taxval_f);

        float cgstamt_f = 0, sgstamt_f = 0;
        if (tvTaxTotal.getText().toString().equals("") == false) {
            cgstamt_f = Float.parseFloat(tvTaxTotal.getText().toString());
        }
        if (tvServiceTaxTotal.getText().toString().equals("") == false) {
            sgstamt_f = Float.parseFloat(tvServiceTaxTotal.getText().toString());
        }


        float subtot_f = taxval_f + cgstamt_f + sgstamt_f;
        objBillDetail.setSubTotal(subtot_f);
        Log.d("InsertBillDetail", "Sub Total :" + subtot_f);

        // cust name
        String custname = editTextName.getText().toString();
        objBillDetail.setCustname(custname);
        Log.d("InsertBillDetail", "CustName :" + custname);

        // cust StateCode
        //String custStateCode =spnr_pos.getSelectedItem().toString();
        //String str = spnr_pos.getSelectedItem().toString();
        /*int length = str.length();
        String custStateCode = "";
        if (length > 0) {
            custStateCode = str.substring(length - 2, length);
        }
        objBillDetail.setCustStateCode(custStateCode);
        Log.d("InsertBillDetail", "CustStateCode :" + custStateCode);*/

        // BusinessType
        /*if (etCustGSTIN.getText().toString().equals("")) {
            objBillDetail.setBusinessType("B2C");
        } else // gstin present means b2b bussiness
        {
            objBillDetail.setBusinessType("B2B");
        }*/
        objBillDetail.setBusinessType("");
        //objBillDetail.setBusinessType("B2C");
        Log.d("InsertBillDetail", "BusinessType : " + objBillDetail.getBusinessType());
        // Payment types
        if (TenderType == 1) {
            // Cash Payment
            objBillDetail.setCashPayment(Float.parseFloat(tvBillAmount.getText().toString()));
            Log.d("InsertBillDetail", "Cash:" + tvBillAmount.getText().toString());

            // Card Payment
            objBillDetail.setCardPayment(fCardPayment);
            Log.d("InsertBillDetail", "Card:" + fCardPayment);

            // Coupon Payment
            objBillDetail.setCouponPayment(fCouponPayment);
            Log.d("InsertBillDetail", "Coupon:" + fCouponPayment);

            // PettyCash Payment
            objBillDetail.setPettyCashPayment(fPettCashPayment);
            Log.d("InsertBillDetail", "PettyCash:" + fPettCashPayment);

            // Wallet Payment
            objBillDetail.setWalletAmount(fWalletPayment);
            Log.d("InsertBillDetail", "Wallet:" + fWalletPayment);

            // PaidTotal Payment
            objBillDetail.setPaidTotalPayment(fPaidTotalPayment);

            // Change Payment
            objBillDetail.setChangePayment(fChangePayment);

        } else if (TenderType == 2) {

            if (PrintBillPayment == 1) {
                // Cash Payment
                objBillDetail.setCashPayment(Float.parseFloat(tvBillAmount.getText().toString()));
                Log.d("InsertBillDetail", "Cash:" + Float.parseFloat(tvBillAmount.getText().toString()));

                // Card Payment
                objBillDetail.setCardPayment(fCardPayment);
                Log.d("InsertBillDetail", "Card:" + fCardPayment);

                // Coupon Payment
                objBillDetail.setCouponPayment(fCouponPayment);
                Log.d("InsertBillDetail", "Coupon:" + fCouponPayment);

                // PettyCash Payment
                objBillDetail.setPettyCashPayment(fPettCashPayment);
                Log.d("InsertBillDetail", "PettyCash:" + fPettCashPayment);

                // Wallet Payment
                objBillDetail.setWalletAmount(fWalletPayment);
                Log.d("InsertBillDetail", "Wallet:" + fWalletPayment);

                // PaidTotal Payment
                objBillDetail.setPaidTotalPayment(Float.parseFloat(tvBillAmount.getText().toString()));

                // Change Payment
                objBillDetail.setChangePayment(fChangePayment);
            } else {
                // Cash Payment
                objBillDetail.setCashPayment(fCashPayment);
                Log.d("InsertBillDetail", "Cash:" + fCashPayment);

                // Card Payment
                objBillDetail.setCardPayment(fCardPayment);
                Log.d("InsertBillDetail", "Card:" + fCardPayment);

                // Coupon Payment
                objBillDetail.setCouponPayment(fCouponPayment);
                Log.d("InsertBillDetail", "Coupon:" + fCouponPayment);

                // PettyCash Payment
                objBillDetail.setPettyCashPayment(fPettCashPayment);
                Log.d("InsertBillDetail", "PettyCash:" + fPettCashPayment);

                // Wallet Payment
                objBillDetail.setWalletAmount(fWalletPayment);
                Log.d("InsertBillDetail", "Wallet:" + fWalletPayment);

                // PaidTotal Payment
                objBillDetail.setPaidTotalPayment(fPaidTotalPayment);

                // Change Payment
                objBillDetail.setChangePayment(fChangePayment);
            }
        }

        // Reprint Count
        objBillDetail.setReprintCount(0);
        Log.d("InsertBillDetail", "Reprint Count:0");

        // Bill Status
        if (jBillingMode == 4) {
            objBillDetail.setBillStatus(2);
            Log.d("InsertBillDetail", "Bill Status:2");
        } else {
            objBillDetail.setBillStatus(1);
            Log.d("InsertBillDetail", "Bill Status:1");
        }

        // Employee Id (Waiter / Rider)
        if (jBillingMode == 1 ) {
            objBillDetail.setEmployeeId(Integer.parseInt(tvWaiterNumber.getText().toString()));
            Log.d("InsertBillDetail", "EmployeeId:" + tvWaiterNumber.getText().toString());
        } else {
            objBillDetail.setEmployeeId(0);
            Log.d("InsertBillDetail", "EmployeeId:0");
        }

        // Customer Id
        objBillDetail.setCustId(Integer.valueOf(customerId));
        Log.d("InsertBillDetail", "Customer Id:" + customerId);

        // User Id
        objBillDetail.setUserId(strUserId);
        Log.d("InsertBillDetail", "UserID:" + strUserId);

        lResult = db.addBilll(objBillDetail, "");
        Log.d("InsertBill", "Bill inserted at position:" + lResult);
        //lResult = dbBillScreen.updateBill(objBillDetail);

        if (String.valueOf(iCustId).equalsIgnoreCase("") || String.valueOf(iCustId).equalsIgnoreCase("0")) {
        } else {
            float fTotalTransaction = db.getCustomerTotalTransaction(iCustId);
            float fCreditAmount = db.getCustomerCreditAmount(iCustId);
            fCreditAmount = fCreditAmount - Float.parseFloat(tvBillAmount.getText().toString());
            fTotalTransaction += Float.parseFloat(tvBillAmount.getText().toString());

            long lResult1 = db.updateCustomerTransaction(iCustId,
                    Float.parseFloat(tvBillAmount.getText().toString()), fTotalTransaction, fCreditAmount);
        }

        // Bill No Reset Configuration
        long Result2 = db.UpdateBillNoResetInvoiceNos(Integer.parseInt(editTextOrderNo.getText().toString()));
    }

    protected void PrintNewBill() {
        if (isPrinterAvailable) {
            if (tblOrderItems.getChildCount() < 1)
            {
                messageDialog.Show("Warning", "Insert item before Print Bill");
                return;
            }
            else
            {
                int tableId = 0, waiterId = 0, orderId = 0;
                if ((!editTextOrderNo.getText().toString().trim().equalsIgnoreCase("")))
                {
                    tableId = 0;
                    waiterId = 0;
                    orderId = Integer.parseInt(editTextOrderNo.getText().toString().trim());
                    ArrayList<BillKotItem> billKotItems = billPrint();
                    ArrayList<BillTaxItem> billTaxItems = taxPrint();
                    ArrayList<BillTaxItem> billOtherChargesItems = otherChargesPrint();
                    ArrayList<BillServiceTaxItem> billServiceTaxItems = servicetaxPrint();

                    ArrayList<BillSubTaxItem> billSubTaxItems = subtaxPrint();
                    PrintKotBillItem item = new PrintKotBillItem();

                    Cursor crsrCustomer = db.getFnbCustomer(customerId);
                    if (crsrCustomer.moveToFirst()) {
                        item.setCustomerName(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustName")));
                    } else {
                        item.setCustomerName(" - - - ");
                    }
                    item.setBillKotItems(billKotItems);
                    item.setBillOtherChargesItems(billOtherChargesItems);
                    item.setBillTaxItems(billTaxItems);
                    item.setBillServiceTaxItems(billServiceTaxItems);
                    item.setBillSubTaxItems(billSubTaxItems);
                    item.setSubTotal(Double.parseDouble(tvSubTotal.getText().toString().trim()));
                    item.setNetTotal(Double.parseDouble(tvBillAmount.getText().toString().trim()));
                    item.setTableNo(tableId);
                    item.setWaiterNo(waiterId);
                    item.setBillNo(String.valueOf(orderId));
                    item.setOrderBy(userName);
                    item.setBillingMode(String.valueOf(jBillingMode));
                    if (strPaymentStatus.equalsIgnoreCase("")) {
                        item.setPaymentStatus("");
                    } else {
                        item.setPaymentStatus(strPaymentStatus);
                    }
                    item.setDate(TimeUtil.getDate());
                    item.setTime(TimeUtil.getTime());

                    item.setTotalsubTaxPercent(fTotalsubTaxPercent);
                    item.setTotalSalesTaxAmount(tvTaxTotal.getText().toString());
                    item.setTotalServiceTaxAmount(tvServiceTaxTotal.getText().toString());

                    String billingmode= "";
                    billingmode = CounterSalesCaption;
                    /*switch (jBillingMode)
                    {
                        case 1 : billingmode = DineInCaption;
                            break;
                        case 2 :
                            break;
                        case 3 : billingmode = TakeAwayCaption;
                            break;
                        case 4 : billingmode = HomeDeliveryCaption;
                            break;
                    }*/
                    billingmode = CounterSalesCaption;
                    item.setStrBillingModeName(billingmode);
                    String prf = Preferences.getSharedPreferencesForPrint(BillingCounterSalesActivity.this).getString("bill", "--Select--");
                /*Intent intent = new Intent(getApplicationContext(), PrinterSohamsaActivity.class);*/
                    Intent intent = null;
                    if (prf.equalsIgnoreCase("Sohamsa")) {
                        String[] tokens = new String[3];
                        tokens[0] = "";
                        tokens[1] = "";
                        tokens[2] = "";
                        Cursor crsrHeaderFooterSetting = null;
                        crsrHeaderFooterSetting = db.getBillSettings();
                        if (crsrHeaderFooterSetting.moveToFirst()) {
                            try {
                                tokens = crsrHeaderFooterSetting.getString(crsrHeaderFooterSetting.getColumnIndex("HeaderText")).split(Pattern.quote("|"));
                            } catch (Exception e) {
                                tokens[0] = "";
                                tokens[1] = "";
                                tokens[2] = "";
                            }
                            if (!tokens[0].equalsIgnoreCase(""))
                                item.setAddressLine1(tokens[0]);
                            if (!tokens[1].equalsIgnoreCase(""))
                                item.setAddressLine2(tokens[1]);
                            if (!tokens[2].equalsIgnoreCase(""))
                                item.setAddressLine3(tokens[2]);
                            item.setFooterLine(crsrHeaderFooterSetting.getString(crsrHeaderFooterSetting.getColumnIndex("FooterText")));
                        } else {
                            Log.d(TAG, "DisplayHeaderFooterSettings No data in BillSettings table");
                        }

                        //printSohamsaBILL(item, "BILL");
                    } else if (prf.equalsIgnoreCase("Heyday")) {
                        String[] tokens = new String[3];
                        tokens[0] = "";
                        tokens[1] = "";
                        tokens[2] = "";
                        Cursor crsrHeaderFooterSetting = null;
                        crsrHeaderFooterSetting = db.getBillSettings();
                        if (crsrHeaderFooterSetting.moveToFirst()) {
                            try {
                                tokens = crsrHeaderFooterSetting.getString(crsrHeaderFooterSetting.getColumnIndex("HeaderText")).split(Pattern.quote("|"));
                            } catch (Exception e) {
                                tokens[0] = "";
                                tokens[1] = "";
                                tokens[2] = "";
                            }
                            if (!tokens[0].equalsIgnoreCase(""))
                                item.setAddressLine1(tokens[0]);
                            if (!tokens[1].equalsIgnoreCase(""))
                                item.setAddressLine2(tokens[1]);
                            if (!tokens[2].equalsIgnoreCase(""))
                                item.setAddressLine3(tokens[2]);
                            item.setFooterLine(crsrHeaderFooterSetting.getString(crsrHeaderFooterSetting.getColumnIndex("FooterText")));
                        } else {
                            Log.d(TAG, "DisplayHeaderFooterSettings No data in BillSettings table");
                        }
                        //startActivity(intent);
                        if (isPrinterAvailable) {
                            printHeydeyBILL(item, "BILL");
                        } else {
                            askForConfig();
                        }
                    } else {
                        Toast.makeText(BillingCounterSalesActivity.this, "Printer not configured", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(BillingCounterSalesActivity.this, "Please Enter Bill, Waiter, Table Number", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(BillingCounterSalesActivity.this, "Printer is not ready", Toast.LENGTH_SHORT).show();
            askForConfig();
        }
    }

    public ArrayList<BillKotItem> billPrint() {
        ArrayList<BillKotItem> billKotItems = new ArrayList<BillKotItem>();
        int count = 1;
        for (int iRow = 0; iRow < tblOrderItems.getChildCount(); iRow++) {
            TableRow row = (TableRow) tblOrderItems.getChildAt(iRow);
            CheckBox itemId = (CheckBox) row.getChildAt(0);
            TextView itemName = (TextView) row.getChildAt(1);
            EditText itemQty = (EditText) row.getChildAt(3);
            EditText itemRate = (EditText) row.getChildAt(4);
            TextView itemAmount = (TextView) row.getChildAt(5);
            TextView printstatus = (TextView) row.getChildAt(21);
            int id = Integer.parseInt(itemId.getText().toString().trim());
            int sno = count;
            String name = itemName.getText().toString().trim();
            Double qty = Double.parseDouble(itemQty.getText().toString().trim());
            double rate = Double.parseDouble(itemRate.getText().toString().trim());
            double amount = Double.parseDouble(itemAmount.getText().toString().trim());
            BillKotItem billKotItem = new BillKotItem(sno, name, qty.intValue(), rate, amount);
            billKotItems.add(billKotItem);
            count++;

        }
        return billKotItems;
    }

    public ArrayList<BillTaxItem> taxPrint() {
        ArrayList<BillTaxItem> billTaxItems = new ArrayList<BillTaxItem>();

        Cursor crsrTax = db.getItemsForSalesTaxPrints(Integer.valueOf(editTextOrderNo.getText().toString()));
        if (crsrTax.moveToFirst()) {
            do {
                String taxname = "Sales Tax"; //crsrTax.getString(crsrTax.getColumnIndex("TaxDescription"));
                String taxpercent = crsrTax.getString(crsrTax.getColumnIndex("TaxPercent"));
                Double taxvalue = Double.parseDouble(crsrTax.getString(crsrTax.getColumnIndex("TaxAmount")));

                BillTaxItem taxItem = new BillTaxItem(taxname, Double.parseDouble(taxpercent), Double.parseDouble(String.format("%.2f", taxvalue)));
                billTaxItems.add(taxItem);
            } while (crsrTax.moveToNext());
        }
        return billTaxItems;
    }

    public ArrayList<BillTaxItem> otherChargesPrint() {
        ArrayList<BillTaxItem> billOtherChargesItems = new ArrayList<BillTaxItem>();
        String billingmode= "";
        billingmode = CounterSalesCaption;
        Cursor crsrTax = db.getItemsForOtherChargesPrints(billingmode);
        if (crsrTax.moveToFirst()) {
            do {
                String taxname = crsrTax.getString(crsrTax.getColumnIndex("ModifierDescription"));
                String taxpercent = "0";
                Double taxvalue = Double.parseDouble(crsrTax.getString(crsrTax.getColumnIndex("ModifierAmount")));

                BillTaxItem taxItem = new BillTaxItem(taxname, Double.parseDouble(taxpercent), Double.parseDouble(String.format("%.2f", taxvalue)));
                billOtherChargesItems.add(taxItem);
            } while (crsrTax.moveToNext());
        }
        return billOtherChargesItems;
    }

    public ArrayList<BillServiceTaxItem> servicetaxPrint() {
        ArrayList<BillServiceTaxItem> billServiceTaxItems = new ArrayList<BillServiceTaxItem>();
        Cursor crsrTax = db.getItemsForServiceTaxPrints(Integer.valueOf(editTextOrderNo.getText().toString()));
        if (crsrTax.moveToFirst()) {
            //do {
            BillServiceTaxItem ServicetaxItem = new BillServiceTaxItem("Service Tax", Double.parseDouble(crsrTax.getString(crsrTax.getColumnIndex("ServiceTaxPercent"))), Double.parseDouble(String.format("%.2f", Double.parseDouble(tvServiceTaxTotal.getText().toString()))));
            billServiceTaxItems.add(ServicetaxItem);
            //} while (crsrTax.moveToNext());
        }
        return billServiceTaxItems;
    }
    public ArrayList<BillSubTaxItem> subtaxPrint() {
        ArrayList<BillSubTaxItem> billSubTaxItems = new ArrayList<BillSubTaxItem>();
        Cursor crsrSubTax = db.getAllSubTaxConfigs("2");
        if (crsrSubTax.moveToFirst()) {
            do {
                String subtaxname = crsrSubTax.getString(crsrSubTax.getColumnIndex("SubTaxDescription"));
                String subtaxpercent = crsrSubTax.getString(crsrSubTax.getColumnIndex("SubTaxPercent"));
                double subtaxvalue = Double.parseDouble(tvSubTotal.getText().toString().trim()) * (Double.parseDouble(subtaxpercent) / 100);
                BillSubTaxItem taxSubItem = new BillSubTaxItem(subtaxname, Double.parseDouble(subtaxpercent), Double.parseDouble(String.format("%.2f", subtaxvalue)));
                billSubTaxItems.add(taxSubItem);
                fTotalsubTaxPercent += Float.parseFloat(subtaxpercent);

            } while (crsrSubTax.moveToNext());
        }
        return billSubTaxItems;
    }
    // -----Print Bill Code Ended-----

    private void UpdateItemStock(Cursor crsrUpdateStock, float Quantity) {
        int iResult = 0;
        float fCurrentStock = 0, fNewStock = 0;

        // Get current stock of item
        fCurrentStock = crsrUpdateStock.getFloat(crsrUpdateStock.getColumnIndex("Quantity"));

        // New Stock
        fNewStock = fCurrentStock - Quantity;

        // Update new stock for item
        iResult = db.updateItemStock(crsrUpdateStock.getInt(crsrUpdateStock.getColumnIndex("MenuCode")),
                fNewStock);

        Log.d("UpdateItemStock", "Updated Rows:" + iResult);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {

        }
        if (resultCode == RESULT_OK) {
            switch (requestCode)
            {
                case 1: // PayBill Activity Result
                    boolean isComplimentaryBill, isDiscounted, isPrintBill = false;
                    float dDiscPercent;
                    String strComplimentaryReason = "";

                    isComplimentaryBill = data.getBooleanExtra(PayBillActivity.IS_COMPLIMENTARY_BILL, false);
                    isDiscounted = data.getBooleanExtra(PayBillActivity.IS_DISCOUNTED, false);
                    isPrintBill = data.getBooleanExtra(PayBillActivity.IS_PRINT_BILL, true);
                    strComplimentaryReason = data.getStringExtra(PayBillActivity.COMPLIMENTARY_REASON);
                    dDiscPercent = data.getFloatExtra(PayBillActivity.DISCOUNT_PERCENT, 0);
                    fCashPayment = data.getFloatExtra(PayBillActivity.TENDER_CASH_VALUE, 0);
                    fCardPayment = data.getFloatExtra(PayBillActivity.TENDER_CARD_VALUE, 0);
                    fCouponPayment = data.getFloatExtra(PayBillActivity.TENDER_COUPON_VALUE, 0);
                    fTotalDiscount = data.getFloatExtra(PayBillActivity.DISCOUNT_PERCENT, 0);

                    fPettCashPayment = data.getFloatExtra(PayBillActivity.TENDER_PETTYCASH_VALUE, 0);
                    fPaidTotalPayment = data.getFloatExtra(PayBillActivity.TENDER_PAIDTOTAL_VALUE, 0);
                    fWalletPayment = data.getFloatExtra(PayBillActivity.TENDER_WALLET_VALUE, 0);
                    fChangePayment = data.getFloatExtra(PayBillActivity.TENDER_CHANGE_AMOUNT, 0);

                    iCustId = data.getIntExtra("CUST_ID", 1);

                    if (isDiscounted == true) {
                        Log.v("Tender Result", "Discounted:" + isDiscounted);
                        Log.v("Tender Result", "Discount Percent:" + dDiscPercent);
                        OverAllDiscount(dDiscPercent);
                    }

                    l(2, isPrintBill);
                    Toast.makeText(getApplicationContext(), "Bill saved Successfully", Toast.LENGTH_SHORT).show();
                    if (isComplimentaryBill == true) {
                        // Save complimentary bill details
                        SaveComplimentaryBill(Integer.parseInt(editTextOrderNo.getText().toString()), (fCashPayment + fCardPayment + fCouponPayment), strComplimentaryReason);
                    }
                    if (isPrintBill == true) {
                        strPaymentStatus = "Paid";
                        PrintNewBill();
                    }
                    if (jBillingMode == 2) {
                        int iResult = db.deleteKOTItem(iCustId, String.valueOf(jBillingMode));
                        ClearAll();
                        btn_PrintBill.setEnabled(true);
                    }
                    break;
            }
        }
        else if (resultCode == RESULT_CANCELED)
        {
            try {
                if (data.getBooleanExtra("isCancelled", false)) {
                    finish();
                }
            } catch (Exception e) {

            }
        }
    }

    /*************************************************************************************************************************************
     * Calculates all the amount after giving overall discount in tender window
     * and updates the new values in text boxes
     *
     * @param dDiscountPercent : Discount percent
     *************************************************************************************************************************************/
    private void OverAllDiscount(double dDiscountPercent) {
        double dRate = 0, dTaxPercent = 0, dTaxAmt = 0, dDiscAmt = 0, dTempAmt = 0;
        TableRow rowItem;
        TextView DiscAmt, DiscPercent, Qty, Rate, TaxAmt, TaxPercent, TaxType;

        for (int i = 0; i < tblOrderItems.getChildCount(); i++) {

            // Get Item row
            rowItem = (TableRow) tblOrderItems.getChildAt(i);
            if (rowItem.getChildAt(0) != null) {
                // Get Discount percent
                Qty = (TextView) rowItem.getChildAt(3);
                Rate = (TextView) rowItem.getChildAt(4);
                DiscPercent = (TextView) rowItem.getChildAt(8);
                DiscAmt = (TextView) rowItem.getChildAt(9);
                TaxPercent = (TextView) rowItem.getChildAt(6);
                TaxAmt = (TextView) rowItem.getChildAt(7);
                TaxType = (TextView) rowItem.getChildAt(13);
                DiscPercent.setText(String.format("%.2f", dDiscountPercent));

                dRate = Double.parseDouble(Rate.getText().toString());
                dTaxPercent = Double.parseDouble(TaxPercent.getText().toString());

                if (TaxType.getText().toString().equalsIgnoreCase("1")) {
                    // Discount
                    dDiscAmt = dRate * (dDiscountPercent / 100);
                    dTempAmt = dDiscAmt;
                    dDiscAmt = dDiscAmt * Double.parseDouble(Qty.getText().toString());

                    // Tax
                    dTaxAmt = (dRate - dTempAmt) * (dTaxPercent / 100);
                    dTaxAmt = dTaxAmt * Double.parseDouble(Qty.getText().toString());

                    TaxAmt.setText(String.format("%.2f", dTaxAmt));
                    DiscAmt.setText(String.format("%.2f", dDiscAmt));

                } else {
                    double dBasePrice = 0;
                    dBasePrice = dRate / (1 + (dTaxPercent / 100));

                    // Discount
                    dDiscAmt = dBasePrice * (dDiscountPercent / 100);
                    dTempAmt = dDiscAmt;
                    dDiscAmt = dDiscAmt * Double.parseDouble(Qty.getText().toString());

                    // Tax
                    dTaxAmt = (dBasePrice - dTempAmt) * (dTaxPercent / 100);
                    dTaxAmt = dTaxAmt * Double.parseDouble(Qty.getText().toString());

                    TaxAmt.setText(String.format("%.2f", dTaxAmt));
                    DiscAmt.setText(String.format("%.2f", dDiscAmt));
                }
            }
        }

        CalculateTotalAmount();
    }

    /*************************************************************************************************************************************
     * Updates complimentary bill details in database
     *
     * @param BillNumber          : Complimentary bill number
     * @param PaidAmount          : Amount paid for the complimentary bill
     * @param ComplimentaryReason : Reason for giving complimentary bill
     *************************************************************************************************************************************/
    private void SaveComplimentaryBill(int BillNumber, float PaidAmount, String ComplimentaryReason) {
        long lResult = 0;

        ComplimentaryBillDetail objComplimentaryBillDetail = new ComplimentaryBillDetail();

        // Set bill number
        objComplimentaryBillDetail.setBillNumber(BillNumber);

        // Set complimentary reason
        objComplimentaryBillDetail.setComplimentaryReason(ComplimentaryReason);

        // Set paid amount
        objComplimentaryBillDetail.setPaidAmount(PaidAmount);

        lResult = db.addComplimentaryBillDetails(objComplimentaryBillDetail);

        Log.v("SaveComplimentaryBill", "Complimentary Bill inserted at Row:" + lResult);
    }
}

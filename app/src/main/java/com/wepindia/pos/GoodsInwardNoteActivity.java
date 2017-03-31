package com.wepindia.pos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.wep.common.app.Database.BillItem;
import com.wep.common.app.Database.DatabaseHandler;
import com.wep.common.app.Database.Item;
import com.wep.common.app.WepBaseActivity;
import com.wep.common.app.views.WepButton;
import com.wepindia.pos.GenericClasses.DateTime;
import com.wepindia.pos.GenericClasses.MessageDialog;
import com.wepindia.pos.utils.ActionBarUtils;
import com.wepindia.pos.utils.StockInwardMaintain;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GoodsInwardNoteActivity extends WepBaseActivity {


    Context myContext;
    // DatabaseHandler_gst object
    DatabaseHandler dbGoodsInwardNote;
    //Message dialog object
    public MessageDialog MsgBox;
    Spinner spnr_inwalrd_item_list;
    EditText et_inward_item_quantity, et_inward_sub_total,tx_inward_supply_invoice_number,et_inward_grand_total, et_inward_additionalchargename,
            et_inward_additionalchargeamount,et_supplier_address,et_supplier_phone,et_supplier_code;

    TextView tx_inward_invoice_date;

    ImageButton btnimage_new_item ;
    AutoCompleteTextView autocompletetv_suppliername, autocompletetv_invoiceno,autocompletetv_itemlist, autocompletetv_purchase_order;
    TableLayout tbl_inward_item_details;
    CheckBox chk_inward_additional_charge;
    WepButton btnSubmitItem,btnSaveItem,btnAddSupplier, btnClearItem, btnCloseItem;


    ArrayList<String> labelsSupplierName;
    int count =0;
    long lRowId = 0;
    String Item_name , Item_rate, Item_quantity , Item_supplytype , Item_uom, Item_saletax, Item_servicetax ;
    String strDate= "";
    String strUserName="", strUserId ="";
    Date d;

    Cursor purchase_crsr;
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_inward_note);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*TextView tvTitleText = (TextView) findViewById(R.id.tvTitleBarCaption);
        TextView tvTitleUserName = (TextView) findViewById(R.id.tvTitleBarUserName);
        TextView tvTitleDate = (TextView) findViewById(R.id.tvTitleBarDate);
        ActionBarUtils.goBack(this, findViewById(R.id.imgTitleBackIcon));
        ActionBarUtils.goHome(this, findViewById(R.id.imgTitleHomeIcon));
        ActionBarUtils.takeScreenshot(this, findViewById(R.id.imgTitleScreenshotIcon), findViewById(R.id.lnrPaymentReceipt));
        tvTitleText.setText(" Goods Inward Note ");*/

        //setContentView(R.layout.activity_inward_invoice_entry);

        dbGoodsInwardNote = new DatabaseHandler(GoodsInwardNoteActivity.this);
        myContext = this;
        MsgBox =  new MessageDialog(myContext);
        purchase_crsr = null;


        try {
            strUserName = getIntent().getStringExtra("USER_NAME");
            strUserId = getIntent().getStringExtra("USER_ID");


            //tvTitleUserName.setText(strUserName.toUpperCase());
            d = new Date();
            CharSequence s = DateFormat.format("dd-MM-yyyy", d.getTime());
            //tvTitleDate.setText("Date : " + s);

            com.wep.common.app.ActionBarUtils.setupToolbar(GoodsInwardNoteActivity.this,toolbar,getSupportActionBar(),"Goods Inward Note",strUserName," Date:"+s.toString());

            InitializeViews();
            dbGoodsInwardNote.CreateDatabase();
            dbGoodsInwardNote.OpenDatabase();
            reset_inward(0);

            autocompletetv_suppliername.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event){
                    autocompletetv_suppliername.showDropDown();
                    return false;
                }
            });
            autocompletetv_suppliername.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int suppliercode = -1;
                    String suppliername_str = autocompletetv_suppliername.getText().toString().toUpperCase();
                    Cursor supplierdetail_cursor = dbGoodsInwardNote.getSupplierDetails(suppliername_str);
                    if (supplierdetail_cursor!=null && supplierdetail_cursor.moveToFirst())
                    {
                        et_supplier_phone.setText(supplierdetail_cursor.getString(supplierdetail_cursor.getColumnIndex("SupplierPhone")));
                        et_supplier_address.setText(supplierdetail_cursor.getString(supplierdetail_cursor.getColumnIndex("SupplierAddress")));
                        suppliercode= supplierdetail_cursor.getInt(supplierdetail_cursor.getColumnIndex("SupplierCode"));
                        et_supplier_code.setText(String.valueOf(suppliercode));
                        loadAutoCompleteData_item(suppliercode);
                        loadAutoCompleteData_purchaseOrder(suppliercode);
                        autocompletetv_purchase_order.setText("");
                        autocompletetv_itemlist.setText("");
                        btnAddSupplier.setEnabled(false);

                    }
                }
            });

            /*autocompletetv_invoiceno.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event){
                    autocompletetv_invoiceno.showDropDown();
                    return false;
                }
            });

            autocompletetv_invoiceno.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    loadTableonInvoiceSelected();

                }

                public void onNothingSelected(AdapterView<?> parent) {

                }
            });*/

            /*autocompletetv_purchase_order.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event){
                    int suppliercode = Integer.parseInt(et_supplier_code.getText().toString());
                    if(suppliercode == 0)
                    {
                        autocompletetv_purchase_order.setInputType(InputType.TYPE_NULL);
                        Toast.makeText(myContext, "Please Select the Supplier First", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        autocompletetv_purchase_order.setInputType(InputType.TYPE_CLASS_NUMBER);
                        autocompletetv_purchase_order.showDropDown();

                    }
                    return false;
                }
            });*/

            autocompletetv_purchase_order.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event){
                    autocompletetv_purchase_order.showDropDown();
                    return false;
                }
            });


            autocompletetv_purchase_order.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                     btnSubmitItem.setEnabled(true);
                    loadTableonPurchaseOrderSelected();

                }
            });


            /*autocompletetv_itemlist.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event){
                    int suppliercode = Integer.parseInt(et_supplier_code.getText().toString());
                    if(suppliercode == 0)
                    {
                        autocompletetv_itemlist.setInputType(InputType.TYPE_NULL);
                        Toast.makeText(myContext, "Please Select the Supplier First", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {

                        autocompletetv_itemlist.showDropDown();
                        autocompletetv_itemlist.setInputType(InputType.TYPE_CLASS_TEXT);
                    }

                    return false;
                }
            });*/

            autocompletetv_itemlist.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event){
                    autocompletetv_itemlist.showDropDown();
                    return false;
                }
            });


            autocompletetv_itemlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String itemname = autocompletetv_itemlist.getText().toString();
                    if (itemname.equalsIgnoreCase("Not in list") || itemname.equalsIgnoreCase("Add new item") ) {
                        et_inward_item_quantity.setBackgroundColor(getResources().getColor(R.color.grey));
                        et_inward_item_quantity.setEnabled(false);
                        //autocompletetv_itemlist.setText("");
                        //input_window();
                        MsgBox.Show("Warning","Kindly goto \"Inward Supply Item\" and add the desired item." +
                                "\nPlease save your data , if any , before leaving this screen");


                    }else if (itemname.equalsIgnoreCase("Select/Add Supplier"))
                    {
                        //MsgBox.Show("Insufficient Information", "Please Select/Add the Supplier");
                        et_inward_item_quantity.setEnabled(false);
                        et_inward_item_quantity.setBackgroundColor(getResources().getColor(R.color.grey));
                    }
                    else {
                        et_inward_item_quantity.requestFocus();
                        et_inward_item_quantity.setEnabled(true);
                        et_inward_item_quantity.setBackgroundColor(getResources().getColor(R.color.white));
                        // btnimage_new_item.setEnabled(true);
                    }
                }
            });


            chk_inward_additional_charge.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(chk_inward_additional_charge.isChecked() == false)
                    {
                        et_inward_additionalchargeamount.setText("");
                        et_inward_additionalchargename.setText("");
                        et_inward_additionalchargeamount.setEnabled(false);
                        et_inward_additionalchargename.setEnabled(false);

                    }
                    else
                    {
                        et_inward_additionalchargeamount.setText("");
                        et_inward_additionalchargename.setText("");
                        et_inward_additionalchargeamount.setEnabled(true);
                        et_inward_additionalchargename.setEnabled(true);
                    }
                }
            });

            et_inward_additionalchargeamount.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                public void afterTextChanged(Editable s) {
                    calculateSubTotal();

                }
            });


            btnimage_new_item.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    MsgBox = new MessageDialog(myContext);
                    String item = autocompletetv_itemlist.getText().toString();
                    int suppliercode = Integer.parseInt(et_supplier_code.getText().toString());
                    if ( suppliercode <1 || item.equalsIgnoreCase("Select/Add Supplier") || item.equals(""))
                    {

                        if(suppliercode <1)
                        {
                            MsgBox.setTitle("Insufficient Information")
                                    .setMessage(" Please Select/Add Supplier ")
                                    .setPositiveButton("OK", null)
                                    .show();
                        } else
                        {
                            MsgBox.setTitle("Error")
                                    .setMessage(" Please Select Item ")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }

                    } else if (item.equalsIgnoreCase("Not in list") ||  item.equalsIgnoreCase("Add new item"))
                    {
                        /*MsgBox.setTitle("Error")
                                .setMessage(" Please Select Item  ")
                                .setPositiveButton("OK", null)
                                .show();*/
                        //input_window();
                        MsgBox.Show("Warning","Kindly goto \"Inward Supply Item\" and add the desired item." +
                                "\nPlease save your data , if any , before leaving this screen");
                    }else   if (et_inward_item_quantity.getText().toString().equals(""))
                    {
                        et_inward_item_quantity.setBackgroundColor(Color.WHITE);
                        et_inward_item_quantity.setEnabled(true);
                        MsgBox.setTitle("Error")
                                .setMessage(" Please Enter the Quantity ")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                    else {
                        // check whether user has entered new item without going through inputwindow()
                        Cursor itemdetails = dbGoodsInwardNote.getItemdetailsforSupplier(suppliercode,item);
                        if (itemdetails!=null && itemdetails.moveToFirst()) {
                            populate(2);
                        }
                        else
                        {
                            /*MsgBox.setTitle(" Insufficient Information")
                                    .setMessage(" Item not found in database for this Supplier. Do you want add it.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            input_window();
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();*/
                            MsgBox.setTitle(" Insufficient Information")
                                    .setMessage(" Item not found in database for this Supplier." +
                                            "\nKindly goto \"Inward Supply Item\" and add the desired item" +
                                            "\nPlease save your data , if any , before leaving this screen")
                                    .setPositiveButton("Ok",null)
                                    .show();
                        }
                    }
                }
            });

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            AlertDialog.Builder msg = new AlertDialog.Builder(myContext);
            msg.setMessage(""+e.getMessage())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    void loadTableonPurchaseOrderSelected()
    {
        int suppliercode = Integer.parseInt(et_supplier_code.getText().toString());
        String purchaseOrder = autocompletetv_purchase_order.getText().toString();
        try
        {
            if (purchaseOrder.equalsIgnoreCase("Select/Add Supplier")) {
                //MsgBox.Show("Insufficient Information", "Please Select the Supplier");
                //autocompletetv_purchase_order.setText("");
                //Toast.makeText(myContext, "Insufficient Information : Please Select the Supplier", Toast.LENGTH_SHORT).show();

            }else if (purchaseOrder.equalsIgnoreCase(""))
            {
                MsgBox.Show("Insufficient Information", "Please Select the Purchase Order");
                return;
            }else if (purchaseOrder.equalsIgnoreCase("Make New Purchase Order")) {
                Cursor crsr = dbGoodsInwardNote.getMaxPurchaseOrderNo();
                int Max =1;
                if (crsr != null && crsr.moveToFirst())
                {
                    Max = crsr.getInt(0);
                    Max++;
                }
                autocompletetv_purchase_order.setText(String.valueOf(Max));
                btnSubmitItem.setEnabled(true);
                //loadAutoCompleteData_item(suppliercode);
            }else if (purchaseOrder.equalsIgnoreCase("NA")) {
                btnSaveItem.setEnabled(false);
                btnSubmitItem.setEnabled(true);
            } else {
                // valid Purchase Order No is selected
                Cursor purchaseorder_crsr = dbGoodsInwardNote.getPurchaseOrderDetails(suppliercode,Integer.parseInt(purchaseOrder));
                int flag =0;
                count =0;
                while (purchaseorder_crsr != null &&  purchaseorder_crsr.moveToNext())
                {
                    if (flag ==0)
                    {
                        ClearTable();

                        String invoiceno =purchaseorder_crsr.getString(purchaseorder_crsr.getColumnIndex("InvoiceNo"));
                        if(invoiceno!=null && !invoiceno.equals(""))
                            tx_inward_supply_invoice_number.setText(invoiceno);
                        else
                            tx_inward_supply_invoice_number.setText("");

                        String date_temp = purchaseorder_crsr.getString(purchaseorder_crsr.getColumnIndex("InvoiceDate"));
                        if(date_temp !=null && !date_temp.equals(""))
                        {
                            String dateformat = "dd-MM-yyyy";
                            String invoicedate = getDate(Long.parseLong(date_temp), dateformat);
                            tx_inward_invoice_date.setText(invoicedate);
                        }
                        else
                        {
                            tx_inward_invoice_date.setText("");
                        }


                        String additionalamount = purchaseorder_crsr.getString(purchaseorder_crsr.getColumnIndex("AdditionalChargeAmount"));
                        if(additionalamount== null || additionalamount.equals("") || additionalamount.equals("0"))
                        {
                            chk_inward_additional_charge.setChecked(false);
                            et_inward_additionalchargename.setText("");
                            et_inward_additionalchargeamount.setText("");
                        }
                        else {
                            // some value of additional charge
                            chk_inward_additional_charge.setChecked(true);
                            et_inward_additionalchargename.setText(purchaseorder_crsr.getString(purchaseorder_crsr.getColumnIndex("AdditionalChargeName")));
                            et_inward_additionalchargeamount.setText(additionalamount);
                        }
                        ;                        flag = 1;
                    }
                    purchase_crsr = null;
                    purchase_crsr = purchaseorder_crsr;
                    populate(3);
                    btnSubmitItem.setEnabled(true);
                }
                calculateSubTotal();
            }
        }
        catch (Exception e)
        {
            Log.e( "GoodsInwardNote",""+e.getMessage());
            Toast.makeText(myContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
    void input_window()
    {
        AlertDialog.Builder AuthorizationDialog = new AlertDialog.Builder(myContext);

        LayoutInflater UserAuthorization = (LayoutInflater) myContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View vwAuthorization = UserAuthorization.inflate(R.layout.activity_new_item_window, null);


        //final TextView tv_attracts_reverse_charge = (TextView) findViewById(R.id.tv_attracts_reverse_charge);
        //final Spinner spnr_attracts_reverse_charge = (Spinner) findViewById(R.id.spnr_attracts_reverse_charge);

        final Spinner spnr_input_supplytype = (Spinner)vwAuthorization.findViewById(R.id.spnr_input_supplytype);
        final EditText et_input_name = (EditText) vwAuthorization.findViewById(R.id.et_input_item);
        final EditText et_input_rate = (EditText) vwAuthorization.findViewById(R.id.et_input_rate);
        final EditText et_input_quantity = (EditText) vwAuthorization.findViewById(R.id.et_input_quantity);
        final Spinner spnr_input_uom = (Spinner) vwAuthorization.findViewById(R.id.spnr_input_uom);
        final EditText et_input_salestax = (EditText) vwAuthorization.findViewById(R.id.et_input_salestax);
        final EditText et_input_servicetax = (EditText) vwAuthorization.findViewById(R.id.et_input_servicetax);



        AuthorizationDialog
                .setTitle("New Item")
                .setView(vwAuthorization)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        et_inward_item_quantity.setBackgroundColor(Color.WHITE);
                        et_inward_item_quantity.setEnabled(true);
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Item_name = et_input_name.getText().toString().toUpperCase();
                        Item_supplytype  = spnr_input_supplytype.getSelectedItem().toString();
                        String rate = et_input_rate.getText().toString();
                        String quantity = et_input_quantity.getText().toString();
                        Item_uom = spnr_input_uom.getSelectedItem().toString();
                        String saletax_str = et_input_salestax.getText().toString();
                        String servicetax_str = et_input_servicetax.getText().toString();

                        if (rate == null || rate.equals(""))
                            Item_rate= "0.00";
                        else
                            Item_rate = String.format("%.2f",Float.parseFloat(et_input_rate.getText().toString()));
                        if (quantity== null || quantity.equals(""))
                            Item_quantity = "0.00";
                        else
                            Item_quantity = String.format("%.2f",Float.parseFloat(et_input_quantity.getText().toString()));
                        if (saletax_str == null || saletax_str.equals(""))
                            Item_saletax= "0.00";
                        else {
                            double saletax_d = Double.parseDouble(saletax_str);
                            if(saletax_d <0 || saletax_d > 99.99)
                            {
                                MsgBox.Show("Warning", "Please enter sales tax % between 0 and 99.99");
                                return;
                            }
                            Item_saletax = String.format("%.2f", saletax_d);
                        }
                        if (servicetax_str== null || servicetax_str.equals(""))
                            Item_servicetax = "0.00";
                        else{
                            double servicetax_d = Double.parseDouble(servicetax_str);
                            if(servicetax_d <0 || servicetax_d > 99.99)
                            {
                                MsgBox.Show("Warning", "Please enter service tax % between 0 and 99.99");
                                return;
                            }
                            Item_servicetax = String.format("%.2f",servicetax_d);
                        }


                        if (Item_name.equals("") ||  quantity.equals("") || rate.equals(""))
                        {
                            AlertDialog.Builder MsgBox = new AlertDialog.Builder(myContext);
                            MsgBox. setMessage("Item Name , value and Quantity cannot be  blank. Please all details")
                                    .setPositiveButton("OK", null)
                                    .show();
                            set_list_spnr();
                        }
                        else if (Item_uom.equals("") ||  Item_uom.equalsIgnoreCase("Unit"))
                        {
                            AlertDialog.Builder MsgBox = new AlertDialog.Builder(myContext);
                            MsgBox. setMessage("Please Select the UOM")
                                    .setPositiveButton("OK", null)
                                    .show();
                            set_list_spnr();
                        }
                        else
                        {
                            String mou_temp = Item_uom;
                            int length = mou_temp.length();
                            String mou = mou_temp.substring(length-3, length-1);
                            int suppliercode = Integer.parseInt(et_supplier_code.getText().toString());
                            if (suppliercode>0)
                            {
                                Cursor item_crsr = dbGoodsInwardNote.getItemdetailsforSupplier(suppliercode, Item_name);
                                if (item_crsr != null && item_crsr.moveToNext()) {
                                    //Toast.makeText(myContext, "Warning Item already present", Toast.LENGTH_SHORT).show();
                                    MsgBox.Show("Warning", "Item already present");
                                }
                                else {
                                    String suppliername = autocompletetv_suppliername.getText().toString();
                                    lRowId = 0;

                                    InsertItem( suppliercode,  suppliername, Item_name,  ""/*barcode*/, Float.parseFloat(Item_rate),
                                            Float.parseFloat(Item_quantity), mou, ""/*ImageUri*/, Float.parseFloat(Item_saletax),
                                            Float.parseFloat(Item_servicetax),Item_supplytype);
                                    if(lRowId>0)
                                    {
                                        autocompletetv_itemlist.setText(Item_name);
                                        et_inward_item_quantity.setEnabled(true);
                                        et_inward_item_quantity.setBackgroundColor(Color.WHITE);
                                        et_inward_item_quantity.setText(Item_quantity);
                                        populate(2); // new entry
                                        loadAutoCompleteData_item(suppliercode);
                                    }
                                    else
                                    {
                                        Toast.makeText(myContext, " Error in adding the item to database", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                })
                .show();
    }

    public boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

    public  void SaveNote(View v)
    {
        AlertDialog.Builder msg = new AlertDialog.Builder(myContext);
        msg.setPositiveButton("OK", null);


        String suppliername_str = autocompletetv_suppliername.getText().toString(),
                supplieraddress_str= et_supplier_address.getText().toString(),
                supplierphn = et_supplier_phone.getText().toString();
        int suppliercode = Integer.parseInt(et_supplier_code.getText().toString());

        //String invno = autocompletetv_invoiceno.getText().toString(),
        String invno = tx_inward_supply_invoice_number.getText().toString(),
                invodate = tx_inward_invoice_date.getText().toString();
        String purchaseorderno = autocompletetv_purchase_order.getText().toString();

        if(suppliername_str.equals("") || supplieraddress_str.equals("") || supplierphn.equals("")){
            MsgBox.Show(" Insufficient Information ","Please fill Supplier Details");
        } else if (suppliercode< 0) { // details are filled but supplier not in database
            MsgBox.Show("Error ", " Supplier is not in Database. Please add supplier");
        }else  if  (purchaseorderno.equals(""))
        {
            MsgBox.Show("Insufficient Information ", " Please add Purchase Order");
        }else if(!isNumeric(purchaseorderno))
        {
            MsgBox.Show("Error ", " Please enter Purchase Order in numbers only");
        }
        /*else if (invno.equals("") || invodate.equals(""))  {
            MsgBox.Show(" Insufficient Information ","Please fill Invoice Details");
        }*/
        else if (tbl_inward_item_details.getChildCount() < 1){
            MsgBox.Show("Insufficient Information ", " Please add item");
        }else {

            int saved = savePurchaseOrder(0);
            if (saved ==1)// successfully added bill items
            {
                Toast.makeText(myContext, "Purchase Order Saved Successfully ", Toast.LENGTH_SHORT).show();
                reset_inward(0);
            }
        }
    }

    int savePurchaseOrder(int isGoodInward)
    {
        // Inserted Row Id in database table
        long lResult = 0;


        // Bill item object
        BillItem objBillItem;
        int result = 1;
        int menucode = 0;
        String additionalchargename = "", additionalCharge = "";

        try
        {
            //String invoiceno= autocompletetv_invoiceno.getText().toString();

            String purchaseorderno = autocompletetv_purchase_order.getText().toString();
            String invoiceno= tx_inward_supply_invoice_number.getText().toString();
            String invodate = tx_inward_invoice_date.getText().toString();

            if (!invodate.equals(""))
            {
                Date date = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(invodate);
                long milliseconds = date.getTime();
                invodate = String.valueOf(milliseconds);
            }


            String supp_name = autocompletetv_suppliername.getText().toString();
            int suppliercode  = Integer.parseInt(et_supplier_code.getText().toString());
            String sup_phone = et_supplier_phone.getText().toString();
            String sup_address = et_supplier_address.getText().toString();

            if(!purchaseorderno.equalsIgnoreCase("NA"))
            {
                int del = dbGoodsInwardNote.deletePurchaseOrder(suppliercode, Integer.parseInt(purchaseorderno));
                String info = " No of rows deleted for purchase order "+purchaseorderno+" : "+del;
                Log.d("GoodsInwardNote :",info);
            }


            for (int iRow = 0; iRow < tbl_inward_item_details.getChildCount(); iRow++) {
                objBillItem = new BillItem();

                TableRow RowBillItem = (TableRow) tbl_inward_item_details.getChildAt(iRow);


                // Bill Number

                objBillItem.setBillNumber(invoiceno);
                Log.d("InsertPurchaseOrder", "Bill Number:" + invoiceno);

                objBillItem.setInvoiceDate(invodate);
                Log.d("InsertPurchaseOrder", "Bill Date:" + tx_inward_invoice_date.getText().toString()+"("+invodate+" milliseconds)");

                objBillItem.setPurchaseOrderNo(Integer.parseInt(purchaseorderno));
                Log.d("InsertPurchaseOrder", "Purchase Order No :" + purchaseorderno);




                objBillItem.setSuppliercode(suppliercode);
                Log.d("InsertPurchaseOrder", "SupplierCode : " + suppliercode);
                objBillItem.setSupplierName(supp_name);
                Log.d("InsertPurchaseOrder", "SupplierName : " + supp_name);
                objBillItem.setSupplierPhone(sup_phone);
                Log.d("InsertPurchaseOrder", "SupplierPhone : " + sup_phone);
                objBillItem.setSupplierAddress(sup_address);
                Log.d("InsertPurchaseOrder", "SupplierAddress : " + sup_address);

                // Item Number
                if (RowBillItem.getChildAt(1) != null) {
                    TextView Supplytype = (TextView) RowBillItem.getChildAt(1);
                    objBillItem.setSupplyType(Supplytype.getText().toString());
                    Log.d("InsertPurchaseOrder", "Supply Type:" + Supplytype.getText().toString());
                }

                // Menu Code
                if (RowBillItem.getChildAt(11) != null) {
                    TextView MenuCode = (TextView) RowBillItem.getChildAt(11);
                    objBillItem.setItemNumber(Integer.parseInt(MenuCode.getText().toString()));
                    Log.d("InsertPurchaseOrder", "Menu Code:" + MenuCode.getText().toString());
                    menucode = Integer.parseInt(MenuCode.getText().toString());
                }

                // Item Name

                if (RowBillItem.getChildAt(2) != null) {
                    TextView ItemName = (TextView) RowBillItem.getChildAt(2);
                    objBillItem.setItemName(ItemName.getText().toString());
                    Log.d("InsertPurchaseOrder", "Item Name:" + ItemName.getText().toString());
                }


                // Rate
                if (RowBillItem.getChildAt(3) != null) {
                    TextView Value = (TextView) RowBillItem.getChildAt(3);
                    objBillItem.setValue(Float.parseFloat(Value.getText().toString()));
                    Log.d("InsertPurchaseOrder", "Rate :" + Value.getText().toString());
                }

                // Quantity
                if (RowBillItem.getChildAt(4) != null) {
                    TextView Quantity = (TextView) RowBillItem.getChildAt(4);
                    objBillItem.setQuantity(Float.parseFloat(Quantity.getText().toString()));
                    Log.d("InsertPurchaseOrder", "Quantity:" + Quantity.getText().toString());
                }


                // UoM
                if (RowBillItem.getChildAt(5) != null) {
                    TextView uom = (TextView) RowBillItem.getChildAt(5);
                    objBillItem.setUom(uom.getText().toString());
                    Log.d("InsertPurchaseOrder", "UoM:" + uom.getText().toString());
                }


                // taxable value
                if (RowBillItem.getChildAt(6) != null) {
                    TextView TaxValue = (TextView) RowBillItem.getChildAt(6);
                    objBillItem.setTaxableValue(Float.parseFloat(TaxValue.getText().toString()));
                    Log.d("InsertPurchaseOrder", "Taxable Value:" + TaxValue.getText().toString());
                }


                // SalesTax Amt
                if (RowBillItem.getChildAt(7) != null) {
                    TextView SalesTax = (TextView) RowBillItem.getChildAt(7);
                    objBillItem.setTaxAmount(Float.parseFloat(SalesTax.getText().toString()));
                    Log.d("InsertPurchaseOrder", "SalesTax:" + SalesTax.getText().toString());
                }
                // ServiceTax Amt
                if (RowBillItem.getChildAt(8) != null) {
                    TextView ServiceTax = (TextView) RowBillItem.getChildAt(8);
                    objBillItem.setServiceTaxAmount(Float.parseFloat(ServiceTax.getText().toString()));
                    Log.d("InsertPurchaseOrder", "ServiceTax :" + ServiceTax.getText().toString());
                }
                // Amount
                if (RowBillItem.getChildAt(9) != null) {
                    TextView Amount = (TextView) RowBillItem.getChildAt(9);
                    objBillItem.setAmount(Float.parseFloat(Amount.getText().toString()));
                    Log.d("InsertPurchaseOrder", "Amount:" + Amount.getText().toString());
                }
                if (chk_inward_additional_charge.isChecked())
                {
                    additionalchargename = et_inward_additionalchargename.getText().toString();
                    additionalCharge = et_inward_additionalchargeamount.getText().toString();

                    objBillItem.setAdditionalChargeName(additionalchargename);
                    Log.d("InsertPurchaseOrder", "Additional Charge Name :" + additionalchargename);
                    objBillItem.setAdditionalChargeAmount(Float.parseFloat(additionalCharge));
                    Log.d("InsertPurchaseOrder", "Additional Charge Amount :" + additionalCharge);
                }
                if(isGoodInward==1)
                {
                    objBillItem.setIsGoodInwarded(1);
                }
                else
                {
                    objBillItem.setIsGoodInwarded(0);
                }


                //richa to do purchase order


                Cursor duplicacy_crsr = dbGoodsInwardNote.checkDuplicatePurchaseOrder(suppliercode, menucode,Integer.parseInt(purchaseorderno));
                if (duplicacy_crsr == null || !duplicacy_crsr.moveToFirst())
                {
                    // item not present for that purchase order
                    lResult = dbGoodsInwardNote.InsertPurchaseOrder(objBillItem);
                    Log.d("InsertPurchaseOrder", " item inserted at position:" + lResult);
                }
                else
                { // already present
                    lResult = dbGoodsInwardNote.UpdatePurchaseOrder(objBillItem);
                    Log.d("InsertPurchaseOrder", "item updated at position:" + lResult);
                }
            }
        } catch (Exception e) {
            Toast.makeText(myContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("log", e.getMessage(), e);
            result = -1;
        }
        return result;
    }


    void goodsinward() {
        int suppliercode = Integer.parseInt(et_supplier_code.getText().toString());
        String purchaseorderno = autocompletetv_purchase_order.getText().toString();
        String invoiceno = tx_inward_supply_invoice_number.getText().toString();
        String invoicedate = tx_inward_invoice_date.getText().toString();

        if (suppliercode == -1) {
            MsgBox.Show(" Insufficent Information ", " Please fill Supplier Details ");
            return;
        }
        if (purchaseorderno.equals(""))
        {
            MsgBox.Show(" Insufficent Information ", " Please Select/Add Purchase Order ");
            return;
        }else if(!purchaseorderno.equalsIgnoreCase("NA") && !isNumeric(purchaseorderno))
        {
            MsgBox.Show("Error ", " Please enter Purchase Order in numbers only");
            return;
        }
        if(invoiceno.equals("")|| invoicedate.equals(""))
        {
            MsgBox.Show(" Insufficent Information ", " Please Enter Invoice Details ");
            return;
        }

        long l =0;
        try {

            for (int i = 0; i < tbl_inward_item_details.getChildCount(); i++) {
                TableRow row = (TableRow) tbl_inward_item_details.getChildAt(i);
                TextView ItemName = (TextView) row.getChildAt(2);
                TextView Rate = (TextView) row.getChildAt(3);
                TextView Quantity = (TextView) row.getChildAt(4);
                TextView UOM = (TextView) row.getChildAt(5);

                String itemname_str = ItemName.getText().toString();
                float quantity_f = Float.parseFloat(Quantity.getText().toString());
                String qty = String.format("%.2f",quantity_f);
                quantity_f = Float.parseFloat(qty);

                String uom_str = UOM.getText().toString();

                Cursor item_present_crsr = dbGoodsInwardNote.getItem_GoodsInward(itemname_str);
                if (item_present_crsr != null && item_present_crsr.moveToFirst()) {
                    // already present , needs to update
                    String qty_str = item_present_crsr.getString(item_present_crsr.getColumnIndex("Quantity"));
                    float qty_temp = Float.parseFloat(qty_str);
                    quantity_f += qty_temp;
                    l = dbGoodsInwardNote.updateIngredient(itemname_str, quantity_f, 0,0); // richa_todo
                    if (l > 0) {
                        Log.d(" GoodsInwardNote ", itemname_str + " updated  successfully at " + l);
                        // updating stock inward

                        double openingStock =0, closingStock =0;
                        StockInwardMaintain stock_inward = new StockInwardMaintain(myContext, dbGoodsInwardNote);
                        Cursor crsr_inward_stock = dbGoodsInwardNote.getInwardStock(itemname_str);
                        int menuCode =0;
                        if(crsr_inward_stock!=null && crsr_inward_stock.moveToFirst())
                        {
                            openingStock = crsr_inward_stock.getDouble(crsr_inward_stock.getColumnIndex("OpeningStock"));
                            closingStock = crsr_inward_stock.getDouble(crsr_inward_stock.getColumnIndex("ClosingStock"));
                            menuCode = crsr_inward_stock.getInt(crsr_inward_stock.getColumnIndex("MenuCode"));
                        }
                        double additionalQty = Double.parseDouble(Quantity.getText().toString());
                        stock_inward.updateOpeningStock_Inward(invoicedate,menuCode,itemname_str,
                                openingStock+additionalQty,Double.parseDouble(Rate.getText().toString()));
                        stock_inward.updateClosingStock_Inward(invoicedate,menuCode,itemname_str,
                                closingStock+additionalQty);
                    }

                }else
                {
                    // new entry
                    //richa_todo
                    l = dbGoodsInwardNote.addIngredient(itemname_str, quantity_f, uom_str,0,0);
                    if (l > 0) {
                        Log.d(" GoodsInwardNote ", itemname_str + " added  successfully at " + l);

                        // updating Stock inward
                        int menuCode =0;
                        StockInwardMaintain stock_inward = new StockInwardMaintain(myContext, dbGoodsInwardNote);
                        Cursor goodsInward_cursor = dbGoodsInwardNote.getItem_GoodsInward(itemname_str);
                        if(goodsInward_cursor!=null && goodsInward_cursor.moveToFirst()) {
                            menuCode = goodsInward_cursor.getInt(goodsInward_cursor.getColumnIndex("MenuCode"));}
                        stock_inward.addIngredientToStock_Inward(invoicedate,menuCode,itemname_str,
                                Double.parseDouble(qty),Double.parseDouble(Rate.getText().toString()));

                    }
                }
            }
            if(l>0)
            {
                if(purchaseorderno.equalsIgnoreCase("NA"))
                {
                    autocompletetv_purchase_order.setText("-100");
                    savePurchaseOrder(1);
                }
                savePurchaseOrder(1);
                Toast.makeText(myContext, " Item added Successfully", Toast.LENGTH_SHORT).show();
            }
            reset_inward(0);
        } catch (Exception e)
        {
            Toast.makeText(myContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("GoodsInwardNote", " "+e.getMessage());
        }
    }

    public  void GoodsInward(View v)
    {
        goodsinward();
    }

    void InitializeViews()
    {
        tbl_inward_item_details = (TableLayout) findViewById(R.id.tbl_inward_item_details);
        et_supplier_code = (EditText) findViewById(R.id.et_supplier_code);
        autocompletetv_suppliername = (AutoCompleteTextView)findViewById(R.id.autocompletetv_suppliername);
        et_supplier_address = (EditText) findViewById(R.id.et_supplier_address);
        et_supplier_phone = (EditText) findViewById(R.id.et_supplier_phone);

        autocompletetv_invoiceno = (AutoCompleteTextView)findViewById(R.id.autocompletetv_invoiceno);
        tx_inward_supply_invoice_number = (EditText) findViewById(R.id.tx_inward_supply_invoice_number);
        tx_inward_invoice_date = (TextView) findViewById(R.id.tx_inward_invoice_date);
        autocompletetv_purchase_order = (AutoCompleteTextView)findViewById(R.id.autocompletetv_purchase_order);
        autocompletetv_itemlist = (AutoCompleteTextView)findViewById(R.id.autocompletetv_itemlist);
        et_inward_item_quantity = (EditText) findViewById(R.id.et_inward_item_quantity);
        btnimage_new_item = (ImageButton) findViewById(R.id.btnimage_new_item);

        btnSubmitItem = (com.wep.common.app.views.WepButton) findViewById (R.id.btnSubmitItem);
        btnSaveItem = (com.wep.common.app.views.WepButton) findViewById (R.id.btnSaveItem);
        btnAddSupplier = (com.wep.common.app.views.WepButton) findViewById (R.id.btnAddSupplier);
        btnClearItem = (com.wep.common.app.views.WepButton) findViewById (R.id.btnClearItem);
        btnCloseItem = (com.wep.common.app.views.WepButton) findViewById (R.id.btnCloseItem);

        btnAddSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddSupplier(v);
            }
        });

        btnSaveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveNote(v);
            }
        });

        btnSubmitItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoodsInward(v);
            }
        });
        btnClearItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Clear_inward(v);
            }
        });

        btnCloseItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Close_inward(v);
            }
        });
        chk_inward_additional_charge = (CheckBox) findViewById(R.id.chk_inward_additional_charge);
        et_inward_additionalchargeamount = (EditText) findViewById(R.id.et_inward_additionalchargeamount);
        et_inward_additionalchargename  = (EditText) findViewById(R.id.et_inward_additionalchargename);
        et_inward_sub_total = (EditText) findViewById(R.id.et_inward_sub_total);
        et_inward_grand_total = (EditText) findViewById(R.id.et_inward_grand_total);

    }

    private void loadAutoCompleteData_purchaseOrder(int suppliercode) {
        // loading Purchase order for that supplier
        List<String> invoicelist = dbGoodsInwardNote.getPurchaseOrderlist_inward_nonGST(suppliercode);
        ArrayAdapter<String> dataAdapter11 = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,invoicelist);
        dataAdapter11.setDropDownViewResource(android.R.layout.simple_list_item_1);
        // attaching data adapter to spinner
        autocompletetv_purchase_order.setAdapter(dataAdapter11);



        // loading invoice no for that supplier

        /*List<String> invoicelist = dbGoodsInwardNote.getinvoicelist_inward_nonGST(suppliercode);
        //itemlist.add("Not in list");

        ArrayAdapter<String> dataAdapter11 = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,invoicelist);
        dataAdapter11.setDropDownViewResource(android.R.layout.simple_list_item_1);

        // attaching data adapter to spinner
        autocompletetv_invoiceno.setAdapter(dataAdapter11);*/

    }

    private void loadAutoCompleteData_item(int suppliercode) {

        String suppliername_str = autocompletetv_suppliername.getText().toString().toUpperCase();
        List<String> itemlist = dbGoodsInwardNote.getitemlist_inward_nonGST_Goods(suppliername_str, suppliercode);
        //itemlist.add("Not in list");

        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,itemlist);
        dataAdapter1.setDropDownViewResource(android.R.layout.simple_list_item_1);

        // attaching data adapter to spinner
        autocompletetv_itemlist.setAdapter(dataAdapter1);




    }




    void populate(int type)
    {
        //1 - new item entry
        //2 - existing item in database
        // 3 - popualte table from saved purchase order
        boolean flag_pos =true;

        TableRow tr = new TableRow(this);

        TextView sn = new TextView(myContext);
        sn.setWidth(55);
        sn.setBackgroundResource(R.drawable.border_itemdatabase);
        count++;
        sn.setPadding(10,0,0,0);
        sn.setTextSize(20);
        sn.setText(String.valueOf(count));


        TextView Supplytype = new TextView(myContext);
        Supplytype.setWidth(55);
        Supplytype.setPadding(10,0,0,0);
        Supplytype.setTextSize(20);
        Supplytype.setBackgroundResource(R.drawable.border_itemdatabase);

        TextView name = new TextView(myContext);
        name.setBackgroundResource(R.drawable.border_itemdatabase);
        name.setWidth(225);
        name.setTextSize(20);
        name.setPadding(10,0,0,0);

        TextView rate = new TextView(myContext);
        rate.setWidth(110);
        rate.setPadding(0,0,10,0);
        rate.setGravity(Gravity.RIGHT);
        rate.setTextSize(20);
        rate.setBackgroundResource(R.drawable.border_itemdatabase);

        TextView qty = new TextView(myContext);
        qty.setWidth(90);
        qty.setPadding(0,0,10,0);
        qty.setGravity(Gravity.RIGHT);
        qty.setTextSize(20);
        qty.setBackgroundResource(R.drawable.border_itemdatabase);

        TextView uom = new TextView(myContext);
        uom.setWidth(110);
        uom.setPadding(10,0,0,0);
        uom.setGravity(Gravity.CENTER);
        uom.setTextSize(20);
        uom.setBackgroundResource(R.drawable.border_itemdatabase);

        TextView taxvalue = new TextView(myContext);
        taxvalue.setWidth(170);
        taxvalue.setPadding(0,0,10,0);
        taxvalue.setGravity(Gravity.RIGHT);
        taxvalue.setTextSize(20);
        taxvalue.setBackgroundResource(R.drawable.border_itemdatabase);

        TextView Salestax = new TextView(myContext);
        Salestax.setWidth(110);
        Salestax.setPadding(0,0,10,0);
        Salestax.setGravity(Gravity.RIGHT);
        Salestax.setTextSize(20);
        Salestax.setBackgroundResource(R.drawable.border_itemdatabase);

        TextView Servicetax = new TextView(myContext);
        Servicetax.setWidth(110);
        Servicetax.setPadding(0,0,10,0);
        Servicetax.setGravity(Gravity.RIGHT);
        Servicetax.setTextSize(20);
        Servicetax.setBackgroundResource(R.drawable.border_itemdatabase);

        TextView Amount = new TextView(myContext);
        Amount.setWidth(155);
        Amount.setPadding(0,0,10,0);
        Amount.setGravity(Gravity.RIGHT);
        Amount.setTextSize(20);
        Amount.setBackgroundResource(R.drawable.border_itemdatabase);

        TextView MenuCode = new TextView(myContext);
        MenuCode.setWidth(130);
        MenuCode.setBackgroundResource(R.drawable.border_itemdatabase);


        // Delete
       /* int res = getResources().getIdentifier("deletered1", "drawable", this.getPackageName());
        ImageButton ImgDelete = new ImageButton(myContext);
        //ImgDelete.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        ImgDelete.setImageResource(res);
        // btnDelete.setText(crsrItem.getString(crsrItem.getColumnIndex("MenuCode")));
        ImgDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                View row = (View) v.getParent();
                ViewGroup container = ((ViewGroup) row.getParent());
                container.removeView(row);
                container.invalidate();
                calculateSubTotal();
            }
        });*/

        TextView spc = new TextView(myContext);
        spc.setWidth(5);

        /*Button btndel = new Button(myContext);
        btndel.setBackground(getResources().getDrawable(R.drawable.deletered1));
        btndel.setPadding(5,0,0,0);*/

        Button ImgDelete = new Button(myContext);
        ImgDelete.setBackground(getResources().getDrawable(R.drawable.delete_icon_border));
        ImgDelete.setLayoutParams(new TableRow.LayoutParams(40, 35));
        ImgDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final View v1 = v;
                MsgBox = new MessageDialog(myContext);
                MsgBox.setTitle("Confirm")
                        .setMessage("Do you want to Delete this item ")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                View row = (View) v1.getParent();
                                ViewGroup container = ((ViewGroup) row.getParent());
                                container.removeView(row);
                                container.invalidate();
                                calculateSubTotal();
                                int child = tbl_inward_item_details.getChildCount();
                                for(int i =0;i< child ;i++)
                                {
                                    if(i==0)
                                        count =0;

                                    TableRow row1 = (TableRow) tbl_inward_item_details.getChildAt(i);
                                    TextView Sn = (TextView) row1.getChildAt(0);
                                    count++;
                                    Sn.setText(String.valueOf(count));
                                }
                            }
                        })
                        .setNegativeButton("No",null)
                        .show();
            }
        });



        try {


            if (type == 3)
            {
                if (purchase_crsr ==null)
                {
                    return;
                }

                String supplytype = purchase_crsr.getString(purchase_crsr.getColumnIndex("SupplyType"));
                Supplytype.setText(supplytype);

                String itemname = purchase_crsr.getString(purchase_crsr.getColumnIndex("ItemName"));
                name.setText(itemname);

                String rate_str = purchase_crsr.getString(purchase_crsr.getColumnIndex("Value"));
                rate.setText(String.format("%.2f", Float.parseFloat(rate_str)));

                String quantity_str = purchase_crsr.getString(purchase_crsr.getColumnIndex("Quantity"));
                qty.setText(String.format("%.2f", Float.parseFloat(quantity_str)));

                String uom_str = purchase_crsr.getString(purchase_crsr.getColumnIndex("UOM"));
                uom.setText(uom_str);

                String taxval_str = purchase_crsr.getString(purchase_crsr.getColumnIndex("TaxableValue"));
                taxvalue.setText(String.format("%.2f", Float.parseFloat(taxval_str)));

                String salestax_str = purchase_crsr.getString(purchase_crsr.getColumnIndex("SalesTax"));
                Salestax.setText(String.format("%.2f", Float.parseFloat(salestax_str)));

                String servicetax_str = purchase_crsr.getString(purchase_crsr.getColumnIndex("ServiceTaxAmount"));
                Servicetax.setText(String.format("%.2f", Float.parseFloat(servicetax_str)));

                String amount = purchase_crsr.getString(purchase_crsr.getColumnIndex("Amount"));
                Amount.setText(String.format("%.2f", Float.parseFloat(amount)));

                String menucode = purchase_crsr.getString(purchase_crsr.getColumnIndex("MenuCode"));
                MenuCode.setText(menucode);

                tr.addView(sn);//0
                tr.addView(Supplytype);//1
                tr.addView(name);//2
                tr.addView(rate);//3
                tr.addView(qty);//4
                tr.addView(uom);//5
                tr.addView(taxvalue);//6
                tr.addView(Salestax);//7
                tr.addView(Servicetax);//8
                tr.addView(Amount);//9
                tr.addView(spc);//10
                tr.addView(MenuCode);//11
                tr.addView(ImgDelete);//12
                tbl_inward_item_details.addView(tr);

            }
            if(type ==1) // new item
            {
                /*name.setText(Item_name);
                if(GSTEnable.equals("1")&& HSNEnable.equals("1"))
                {
                    hsn.setText(Item_HSn);
                }
                else
                {
                    hsn.setBackgroundResource(R.color.grey);
                }

                value.setText(Item_value);
                qty.setText(Item_quantity);
                float value_f = Float.parseFloat(Item_value);
                float qty_f = Float.parseFloat(Item_quantity);
                float taxval_f = value_f * qty_f;
                taxvalue.setText(String.valueOf(taxval_f));
                if(Item_taxation.equalsIgnoreCase("nonGST") || Item_taxation.equalsIgnoreCase("Exempt") ||
                        Item_taxation.equalsIgnoreCase("NilRate") )
                {
                    // no tax
                    Item_gst = "0";
                }


                float igstrate_f = 0, igstamt_f =0, cgstrate_f = 0, cgstamt_f =0, sgstrate_f = 0, sgstamt_f =0 ,gstrate_f =0;
                if(chk_pos.isChecked() )
                {
                    String pos_str = et_inward_pos.getText().toString();
                    if (pos_str.equals(""))
                    {
                        flag_pos =false;
                        Toast.makeText(myContext, "Please mention Supplier POS first", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        igstrate_f = Float.parseFloat(Item_gst);
                        igstamt_f = igstrate_f*taxval_f/100;
                        gstrate_f = igstrate_f;

                    }
                }
                else
                {
                    gstrate_f = Float.parseFloat(Item_gst);
                    if (gstrate_f >0) {
                        cgstrate_f = gstrate_f / 2;
                        sgstrate_f = gstrate_f / 2;
                    }
                    sgstamt_f = sgstrate_f*taxval_f/100;
                    cgstamt_f = cgstrate_f*taxval_f/100;

                }
                GSTRate.setText(String.valueOf(gstrate_f));
                IGSTAmt.setText(String.format("%.2f", igstamt_f));
                CGSTAmt.setText(String.format("%.2f", cgstamt_f));
                SGSTAmt.setText(String.format("%.2f", sgstamt_f));
                IGSTRate.setText(String.valueOf(igstrate_f));
                CGSTRate.setText(String.valueOf(cgstrate_f));
                SGSTRate.setText(String.valueOf(sgstrate_f));


                float sub_total_f = taxval_f+igstamt_f + cgstamt_f + sgstamt_f;
                SubTotal.setText(String.valueOf(sub_total_f));*/

            }// end of if(type ==1) // new item
            else if(type ==2) // item existing in database
            {
                float salestaxamount =0 , servicetaxamount =0;
                Item_name = autocompletetv_itemlist.getText().toString().toUpperCase();
                int suppliercode = Integer.parseInt(et_supplier_code.getText().toString());
                if (suppliercode <0)
                {
                    MsgBox = new MessageDialog(myContext);
                    MsgBox.Show("Insufficient Information ", " Please fill Supplier Details ");
                    return;
                }
                Cursor itemdetails = dbGoodsInwardNote.getItemdetailsforSupplier(suppliercode,Item_name);
                if (itemdetails!=null && itemdetails.moveToFirst()) {
                    name.setText(Item_name);

                    String supplytype = itemdetails.getString(itemdetails.getColumnIndex("SupplyType"));
                    Supplytype.setText(supplytype);

                    String Rate = itemdetails.getString(itemdetails.getColumnIndex("Rate"));
                    if (Rate == null || Rate.equals(""))
                        rate.setText("0.00");
                    else
                        rate.setText(String.format("%.2f", Float.parseFloat(Rate)));


                    Item_quantity = et_inward_item_quantity.getText().toString();
                    if (Item_quantity == null || Item_quantity.equals(""))
                        qty.setText("0.00");
                    else
                        qty.setText(String.format("%.2f", Float.parseFloat(Item_quantity)));


                    float rate_f = Float.parseFloat(Rate);
                    float qty_f = Float.parseFloat(Item_quantity);
                    float taxval_f = rate_f * qty_f;
                    taxvalue.setText(String.format("%.2f", taxval_f));

                    String uom_str = itemdetails.getString(itemdetails.getColumnIndex("UOM"));
                    uom.setText(uom_str);

                    float salestax_f =0, servicetax_f =0;

                    String salestax_str = itemdetails.getString(itemdetails.getColumnIndex("SalesTaxPercent"));
                    if (salestax_str== null || salestax_str.equals(""))
                        Salestax.setText("0.00");
                    else
                    {
                        salestax_f = Float.parseFloat(salestax_str);
                        salestaxamount = salestax_f*taxval_f/100;
                        Salestax.setText(String.format("%.2f", salestaxamount));
                    }

                    String servicetax_str = itemdetails.getString(itemdetails.getColumnIndex("ServiceTaxPercent"));
                    if (servicetax_str== null || servicetax_str.equals(""))
                        Servicetax.setText("0.00");
                    else
                    {
                        servicetax_f = Float.parseFloat(servicetax_str);
                        servicetaxamount = servicetax_f *taxval_f/100;
                        Servicetax.setText(String.format("%.2f", servicetaxamount));

                    }

                    float amount_f = taxval_f + salestaxamount +servicetaxamount;
                    Amount.setText(String.format("%.2f", amount_f));

                    int menucode  = itemdetails.getInt(itemdetails.getColumnIndex("MenuCode"));
                    MenuCode.setText(String.valueOf(menucode));
                }

                tr.addView(sn);
                tr.addView(Supplytype);
                tr.addView(name);
                tr.addView(rate);
                tr.addView(qty);
                tr.addView(uom);
                tr.addView(taxvalue);
                tr.addView(Salestax);
                tr.addView(Servicetax);
                tr.addView(Amount);
                tr.addView(spc);
                tr.addView(MenuCode);
                tr.addView(ImgDelete);


                tbl_inward_item_details.addView(tr);
                set_list_spnr();
                calculateSubTotal();

            }// end of if(type ==2)


        }
        catch (Exception e)
        {
            Toast.makeText(myContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    void calculateSubTotal()
    {
        float grandtotal_f =0;
        float subtotal_f =0;
        float additionalcharge_f = 0;
        TextView Amount;
        String amount_str = "0";
        for (int i =0;i<tbl_inward_item_details.getChildCount(); i++)
        {
            TableRow row = (TableRow) tbl_inward_item_details.getChildAt(i);
            Amount = (TextView) row.getChildAt(9);
            if (Amount != null)
            {
                amount_str  = Amount.getText().toString();
            }

            subtotal_f += Float.parseFloat(amount_str);

        }
        et_inward_sub_total.setText(String.valueOf(subtotal_f));

        // addtional charge
        if (chk_inward_additional_charge.isChecked())
            if( et_inward_additionalchargeamount.getText().toString().equals(""))
            {
                additionalcharge_f =0;
            }
            else
            {
                additionalcharge_f = Float.parseFloat(et_inward_additionalchargeamount.getText().toString());
            }

        grandtotal_f = subtotal_f+ additionalcharge_f;
        et_inward_grand_total.setText(String.valueOf(grandtotal_f));

    }

    void set_list_spnr()
    {
        autocompletetv_itemlist.setText("");
        //autocompletetv_purchase_order.setText("");
        et_inward_item_quantity.setBackgroundColor(Color.WHITE);
        et_inward_item_quantity.setText("");
        et_inward_item_quantity.setEnabled(true);
    }

    public void dateSelection_inward (View v)
    {
        String currentdate = DateFormat.format("yyyy-MM-dd", (new Date()).getTime()).toString();

        DateTime objDate = new DateTime(currentdate);
        try {
            AlertDialog.Builder dlgReportDate = new AlertDialog.Builder(myContext);
            final DatePicker dateReportDate = new DatePicker(myContext);
            // Initialize date picker value to business date
            dateReportDate.updateDate(objDate.getYear(), objDate.getMonth(), objDate.getDay());
            String strMessage = " Select the date";


            dlgReportDate
                    .setIcon(R.drawable.ic_launcher)
                    .setTitle("Date Selection")
                    .setMessage(strMessage)
                    .setView(dateReportDate)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
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

                            tx_inward_invoice_date.setText(strDate);

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

    public void Clear_inward(View v)
    {
        for (int i = tbl_inward_item_details.getChildCount(); i >0;  i--) {
            View Row = tbl_inward_item_details.getChildAt(i-1);
            if (Row instanceof TableRow) {
                ((TableRow) Row).removeAllViews();
            }
            ViewGroup container = ((ViewGroup) Row.getParent());
            container.removeView(Row);
            container.invalidate();
            count--;
        }
        count=0;
        set_list_spnr();

        et_inward_additionalchargename.setText("");
        et_inward_additionalchargeamount.setText("");
        chk_inward_additional_charge.setChecked(false);
        et_inward_sub_total.setText("");
        et_inward_grand_total.setText("");

        autocompletetv_purchase_order.setText("");
        tx_inward_supply_invoice_number.setText("");
        tx_inward_invoice_date.setText("");
        autocompletetv_itemlist.setText("");
        et_inward_item_quantity.setText("");
        //String suppliercode = et_supplier_code.getText().toString();
        //if(!(suppliercode.equals("-1") || suppliercode.equals("0")))
        {
            AlertDialog.Builder msg = new AlertDialog.Builder(myContext);
            msg.setMessage(" Do you want to clear supplier details ")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    reset_inward(1);
                                }

                            }
                    )
                    .show();
        }

    }

    void loadAutoCompleteDate()
    {
        try
        {
            labelsSupplierName = dbGoodsInwardNote.getAllSupplierName();
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    labelsSupplierName);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
            autocompletetv_suppliername.setAdapter(dataAdapter);

            ArrayList<String> labelsItemName = new ArrayList<String>();
            //labelsItemName.add(" ");
            labelsItemName.add("Select/Add Supplier");
            ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    labelsItemName);
            dataAdapter1.setDropDownViewResource(android.R.layout.simple_list_item_1);
            autocompletetv_itemlist.setAdapter(dataAdapter1);


            ArrayList<String> labelsPurchaseOrder = new ArrayList<String>();
            //labelsItemName.add(" ");
            labelsPurchaseOrder.add("Select/Add Supplier");
            ArrayAdapter<String> dataAdapter11 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    labelsPurchaseOrder);
            dataAdapter11.setDropDownViewResource(android.R.layout.simple_list_item_1);
            autocompletetv_purchase_order.setAdapter(dataAdapter11);


           /* ArrayList<String> labelsInvoiceNo = new ArrayList<String>();
            labelsInvoiceNo.add("Select Supplier");
            ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    labelsInvoiceNo);
            dataAdapter2.setDropDownViewResource(android.R.layout.simple_list_item_1);
            autocompletetv_invoiceno.setAdapter(dataAdapter2);*/
        }
        catch(Exception e )
        {
            MsgBox.setMessage(e.getMessage())
                    .setNeutralButton("OK", null)
                    .show();
        }

    }

    void reset_inward(int type)
    {
        et_supplier_code.setText("0");
        autocompletetv_suppliername.setText("");
        et_supplier_address.setText("");
        et_supplier_phone.setText("");

        autocompletetv_invoiceno.setText("");
        tx_inward_supply_invoice_number.setText("");
        autocompletetv_purchase_order.setText("");
        tx_inward_invoice_date.setText("");
        et_inward_item_quantity.setEnabled(false);
        //btnimage_new_item.setEnabled(true);

        btnAddSupplier.setEnabled(true);
       // btnSubmitItem.setEnabled(false);
        btnSaveItem.setEnabled(true);
        if (type ==0)
        {
            ClearTable();
            set_list_spnr();

            et_inward_additionalchargename.setText("");
            et_inward_additionalchargeamount.setText("");
            chk_inward_additional_charge.setChecked(false);
            et_inward_grand_total.setText("");
            et_inward_sub_total.setText("");
        }
        loadAutoCompleteDate();
    }

    public  void ClearTable()
    {
        for (int i = tbl_inward_item_details.getChildCount(); i >0;  i--) {
            View Row = tbl_inward_item_details.getChildAt(i-1);
            if (Row instanceof TableRow) {
                ((TableRow) Row).removeAllViews();
            }
            ViewGroup container = ((ViewGroup) Row.getParent());
            container.removeView(Row);
            container.invalidate();
            count--;
        }
        count=0;
    }
    public void Close_inward(View v) {

        dbGoodsInwardNote.CloseDatabase();
        this.finish();
    }


    public void AddSupplier(View v)
    {
        //String suppliercode = et_supplier_code.getText().toString();
        String suppliername = autocompletetv_suppliername.getText().toString().toUpperCase();
        String supplierphone = et_supplier_phone.getText().toString();
        String supplieradress = et_supplier_address.getText().toString();

        if (suppliername.equals("") || supplierphone.equals("") || supplieradress.equals(""))
        {
            MsgBox.Show("Insufficient Information", " Please fill Supplier Details");
        }else {
            try {

                Cursor cursor = dbGoodsInwardNote.getAllSupplierName_nonGST();
                //labelsSupplierName = dbGoodsInwardNote.getAllSupplierName_nonGST();
                labelsSupplierName = new ArrayList<String>();
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        labelsSupplierName.add(cursor.getString(cursor.getColumnIndex("SupplierName")));// adding
                    } while (cursor.moveToNext());
                }

                for (String supplier : labelsSupplierName) {
                    if (suppliername.equalsIgnoreCase(supplier)) {
                        MsgBox.setTitle("Warning")
                                .setMessage("Supplier already present in list")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }
                }

                long l = dbGoodsInwardNote.saveSupplierDetails("UnRegistered", "", suppliername, supplierphone, supplieradress);
                if (l > 0) {
                    Log.d("Inward_Item_Entry", " Supplier details saved at " + l);
                    Toast.makeText(myContext, "Supplier details saved at " + l, Toast.LENGTH_SHORT).show();
                }

                cursor = dbGoodsInwardNote.getAllSupplierName_nonGST();
                labelsSupplierName = new ArrayList<String>();
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        labelsSupplierName.add(cursor.getString(cursor.getColumnIndex("SupplierName")));// adding
                    } while (cursor.moveToNext());
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                        labelsSupplierName);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
                autocompletetv_suppliername.setAdapter(dataAdapter);

                et_supplier_code.setText(String.valueOf(l));
                loadAutoCompleteData_purchaseOrder((int)(l));
                loadAutoCompleteData_item((int)l);
                autocompletetv_purchase_order.setText("");
                autocompletetv_itemlist.setText("");

            } catch (Exception e) {
                MsgBox.setMessage(e.getMessage())
                        .setNeutralButton("Ok", null)
                        .show();
            }
        }
    }
    private void InsertItem(int suppliercode, String suppliername, String itemName, String strbarCode, float ratef,
                            float quantity, String mou, String ImageUri, float SalesTax, float ServiceTax,String supplytype) {


        Item objItem = new Item();
        objItem.setsuppliercode(suppliercode);
        objItem.setsupplierName(suppliername);
        objItem.setItemname(itemName);
        objItem.setItemBarcode(strbarCode);
        objItem.setRate(ratef);
        objItem.setQuantity(quantity);
        objItem.setMOU(mou);
        objItem.setImageId(ImageUri);
        objItem.setSalesTaxPercent(SalesTax);
        objItem.setServiceTaxPercent(ServiceTax);
        objItem.setSupplyType(supplytype);



        lRowId = dbGoodsInwardNote.addItem_Inw_nonGST(objItem);

        Log.d("Item", "Row Id:" + String.valueOf(lRowId));
    }

    /**
     * Return date in specified format.
     * @param milliSeconds Date in milliseconds
     * @param dateFormat Date format
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            AlertDialog.Builder AuthorizationDialog = new AlertDialog.Builder(myContext);
            LayoutInflater UserAuthorization = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View vwAuthorization = UserAuthorization.inflate(R.layout.user_authorization, null);
            final EditText txtUserId = (EditText) vwAuthorization.findViewById(R.id.etAuthorizationUserId);
            final EditText txtPassword = (EditText) vwAuthorization.findViewById(R.id.etAuthorizationUserPassword);
            final TextView tvAuthorizationUserId = (TextView) vwAuthorization.findViewById(R.id.tvAuthorizationUserId);
            final TextView tvAuthorizationUserPassword = (TextView) vwAuthorization.findViewById(R.id.tvAuthorizationUserPassword);
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
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        }
                    })
                    .show();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onHomePressed() {
        ActionBarUtils.navigateHome(this);
    }
}














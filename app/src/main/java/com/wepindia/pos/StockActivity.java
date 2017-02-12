/****************************************************************************
 * Project Name		:	VAJRA
 * <p>
 * File Name		:	StockActivity
 * <p>
 * Purpose			:	Represents Item Stock management activity, takes care of all
 * UI back end operations in this activity, such as event
 * handling data read from or display in views.
 * <p>
 * DateOfCreation	:	26-November-2012
 * <p>
 * Author			:	Balasubramanya Bharadwaj B S
 ****************************************************************************/
package com.wepindia.pos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.wep.common.app.Database.DatabaseHandler;
import com.wep.common.app.WepBaseActivity;
import com.wep.common.app.views.WepButton;
import com.wepindia.pos.GenericClasses.ImageAdapter;
import com.wepindia.pos.GenericClasses.MessageDialog;
import com.wepindia.pos.utils.ActionBarUtils;

import java.util.Date;

public class StockActivity extends WepBaseActivity {

    // Context object
    Context myContext;

    // DatabaseHandler object
    DatabaseHandler dbStock = new DatabaseHandler(StockActivity.this);
    // MessageDialog object
    MessageDialog MsgBox;

    // View handlers
    AutoCompleteTextView ItemLongName;
    TextView tvExistingStock;

    EditText txtNewStock, txtRate1, txtRate2, txtRate3;
    WepButton btnUpdate;
    ListView lstvwDepartment, lstvwCategory;
    GridView grdItems;

    // Variables

    String strMenuCode = "", strUserName = "";
    SimpleCursorAdapter deptdataAdapter, categdataAdapter;
    String[] Name;
    String[] ImageUri;
    int[] MenuCode;
    Cursor crsrSettings = null;
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.application_title_bar);
        TextView tvTitleText = (TextView) findViewById(R.id.tvTitleBarCaption);
        TextView tvTitleUserName = (TextView) findViewById(R.id.tvTitleBarUserName);
        TextView tvTitleDate = (TextView) findViewById(R.id.tvTitleBarDate);
        ActionBarUtils.goBack(this, findViewById(R.id.imgTitleBackIcon));
        ActionBarUtils.goHome(this, findViewById(R.id.imgTitleHomeIcon));
        ActionBarUtils.takeScreenshot(this, findViewById(R.id.imgTitleScreenshotIcon), findViewById(R.id.lnrPriceStock));
        tvTitleText.setText("Price & Stock");*/

        myContext = this;
        MsgBox = new MessageDialog(myContext);

        strUserName = getIntent().getStringExtra("USER_NAME");

        //tvTitleUserName.setText(strUserName.toUpperCase());
        Date d = new Date();
        CharSequence s = DateFormat.format("dd-MM-yyyy", d.getTime());
        //tvTitleDate.setText("Date : " + s);

        com.wep.common.app.ActionBarUtils.setupToolbar(this,toolbar,getSupportActionBar(),"Price and Stock",strUserName," Date:"+s.toString());

        try {
            InitializeViews();

            dbStock.CreateDatabase();
            dbStock.OpenDatabase();
            //DisplayItems();

            ResetStock();
            // Get departments

            // Get Category
            crsrSettings = dbStock.getBillSetting();
            TextView tvdeptline = (TextView) findViewById(R.id.tvStockdeptline);
            TextView tvcategline = (TextView) findViewById(R.id.tvStockcategline);
            WepButton btndepart = (WepButton) findViewById(R.id.btn_Stockdepart);
            WepButton btncateg = (WepButton) findViewById(R.id.btn_Stockcateg);
            WepButton btnitem = (WepButton) findViewById(R.id.btn_Stockitem);
            /*btndepart.setVisibility(View.INVISIBLE);
            btncateg.setVisibility(View.INVISIBLE);
            btnitem.setVisibility(View.INVISIBLE);*/
            if(crsrSettings.moveToFirst()) {
                String fast = crsrSettings.getString(crsrSettings.getColumnIndex("FastBillingMode"));
                if (crsrSettings.getString(crsrSettings.getColumnIndex("FastBillingMode")).equalsIgnoreCase("1")) {
                    btndepart.setVisibility(View.GONE);
                    btncateg.setVisibility(View.GONE);
                    btnitem.setVisibility(View.VISIBLE);
                } else if (crsrSettings.getString(crsrSettings.getColumnIndex("FastBillingMode")).equalsIgnoreCase("2")) {
                    btndepart.setVisibility(View.VISIBLE);
                    btncateg.setVisibility(View.GONE);
                    btnitem.setVisibility(View.VISIBLE);
                } else if (crsrSettings.getString(crsrSettings.getColumnIndex("FastBillingMode")).equalsIgnoreCase("3")) {
                    btndepart.setVisibility(View.VISIBLE);
                    btncateg.setVisibility(View.VISIBLE);
                    btnitem.setVisibility(View.VISIBLE);
                }
                final ListView lstDepname = (ListView) findViewById(R.id.lstStockDepartmentNames);
                final ListView lstCateg = (ListView) findViewById(R.id.lstStockCategoryNames);
                final GridView griditem = (GridView) findViewById(R.id.gridStockItems);



                // Load Items without Dept and Categ
                if (crsrSettings.getString(crsrSettings.getColumnIndex("FastBillingMode")).equalsIgnoreCase("1")) {
                    griditem.setNumColumns(6);
                    GetItemDetails();
                    tvdeptline.setVisibility(View.GONE);
                    tvcategline.setVisibility(View.GONE);
                    lstDepname.setVisibility(View.GONE);
                    lstCateg.setVisibility(View.GONE);
                } else if (crsrSettings.getString(crsrSettings.getColumnIndex("FastBillingMode")).equalsIgnoreCase("2")) {
                    griditem.setNumColumns(4);
                    //GetItemDetailswithoutDeptCateg();
                    GetItemDetails();
                    tvcategline.setVisibility(View.GONE);
                    lstCateg.setVisibility(View.GONE);
                } else {
                    //GetItemDetailswithoutDeptCateg();
                    GetItemDetails();
                }


                if (Name.length > 0) {
                    // Assign item grid to image adapter
                    grdItems.setAdapter(new ImageAdapter(myContext, Name, MenuCode, ImageUri, Byte.parseByte("1")));
                    // Make the item grid visible
                    grdItems.setVisibility(View.VISIBLE);
                } else {
                    // Make the item grid invisible
                    grdItems.setVisibility(View.INVISIBLE);
                }

                btndepart.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        lstDepname.setVisibility(View.VISIBLE);
                        Cursor Departments = null;
                        // Get departments
                        Departments = dbStock.getAllDepartments();
                        // Load departments to Department list
                        if (crsrSettings.getString(crsrSettings.getColumnIndex("FastBillingMode")).equalsIgnoreCase("3")) {
                            LoadDepartments(Departments);
                        } else if (crsrSettings.getString(crsrSettings.getColumnIndex("FastBillingMode")).equalsIgnoreCase("2")) {
                            LoadDepartmentsItems(Departments);
                        }
                        lstCateg.setAdapter(null);
                        griditem.setAdapter(null);
                    }
                });

                btncateg.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        lstCateg.setVisibility(View.VISIBLE);
                        Cursor Category = null;
                        // Get Category
                        Category = dbStock.getCategories();
//            // Load Category to Category List
                        LoadCategories(Category);
                        //lstDepname.setAdapter(null);
                        griditem.setAdapter(null);

                    }
                });

                btnitem.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        griditem.setVisibility(View.VISIBLE);
                        // Get Department items detail
                        if (crsrSettings.getString(crsrSettings.getColumnIndex("FastBillingMode")).equalsIgnoreCase("1")) {
                            griditem.setNumColumns(6);
                            GetItemDetails();
                        } else {
                            //GetItemDetailswithoutDeptCateg();
                            GetItemDetails();
                        }
                        // This condition is to avoid NullReferenceException in getCount()
                        // in ImageAdapter class.
                        if (Name.length > 0) {
                            // Assign item grid to image adapter
                            grdItems.setAdapter(new ImageAdapter(myContext, Name, MenuCode, ImageUri, Byte.parseByte("1")));
                            // Make the item grid visible
                            // grdItems.setVisibility(View.VISIBLE);
                        } else {
                            // Make the item grid invisible
                            grdItems.setVisibility(View.INVISIBLE);
                        }
                        lstDepname.setAdapter(null);
                        lstCateg.setAdapter(null);
                    }
                });
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            MsgBox.Show("", e.getMessage());
        }
    }

    private void InitializeViews() {
        ItemLongName = (AutoCompleteTextView) findViewById(R.id.autoTextItemLongNameValue);
        tvExistingStock = (TextView) findViewById(R.id.tvItemExistingStockValue);
        txtNewStock = (EditText) findViewById(R.id.etItemNewStock);
        txtRate1 = (EditText) findViewById(R.id.etItemRate1);
        txtRate2 = (EditText) findViewById(R.id.etItemRate2);
        txtRate3 = (EditText) findViewById(R.id.etItemRate3);
        btnUpdate = (WepButton) findViewById(R.id.btnUpdateStock);

        lstvwDepartment = (ListView) findViewById(R.id.lstStockDepartmentNames);
        lstvwCategory = (ListView) findViewById(R.id.lstStockCategoryNames);
        grdItems = (GridView) findViewById(R.id.gridStockItems);
        grdItems.setOnItemClickListener(GridItemImageClick);
    }

    private void UpdateItemStock(int MenuCode, float NewStock, float Rate1, float Rate2, float Rate3) {
        long lRowId = 0;

        lRowId = dbStock.updateItemStock(MenuCode, NewStock, Rate1, Rate2, Rate3);

        Toast.makeText(myContext, "Updated Successfully", Toast.LENGTH_LONG).show();
        Log.d("StockUpdate", "Row Id:" + String.valueOf(lRowId));
    }

    private void ResetStock() {
        ItemLongName.setText("");
        txtNewStock.setText("0");
        tvExistingStock.setText("0");
        txtRate1.setText("0");
        txtRate2.setText("0");
        txtRate3.setText("0");
        btnUpdate.setEnabled(false);
        Cursor crsrItems;
        crsrItems = dbStock.getAllItems();
    }

    public void UpdateStock(View v) {
        String strExistingStock = tvExistingStock.getText().toString();
        String strNewStock = txtNewStock.getText().toString();
        String strRate1 = txtRate1.getText().toString();
        String strRate2 = txtRate2.getText().toString();
        String strRate3 = txtRate3.getText().toString();

        if (strNewStock.equalsIgnoreCase("")) {
            MsgBox.Show("Warning", "Enter stock before updating");
            return;
        }

        UpdateItemStock(Integer.parseInt(strMenuCode), (Float.parseFloat(strExistingStock) + Float.parseFloat(strNewStock)),
                Float.parseFloat(strRate1), Float.parseFloat(strRate2), Float.parseFloat(strRate3));
        Toast.makeText(myContext, "Price & Stock Updated Successfully", Toast.LENGTH_LONG).show();
        //DisplayItems();
        ResetStock();

    }

    public void ClearStock(View v) {
        ResetStock();
    }

    public void CloseStock(View v) {
        // Close Database connection and finish the activity
        dbStock.CloseDatabase();
        this.finish();
    }

    private void LoadDepartments(Cursor crsrDept) {

        Cursor cursor = dbStock.getDepartments();
        String columns[] = new String[]{"_id", "DeptName"};
        int vals[] = new int[]{R.id.tvlstDeptCode, R.id.tvlstDeptName};
        deptdataAdapter = new SimpleCursorAdapter(this, R.layout.activity_deptnames, cursor, columns, vals);

        lstvwDepartment.setVisibility(View.VISIBLE);
        lstvwDepartment.setAdapter(deptdataAdapter);
        lstvwDepartment.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                @SuppressWarnings("unchecked")
                String deptcode = ((TextView) view.findViewById(R.id.tvlstDeptCode)).getText().toString();
                Cursor Category = dbStock.getCategoryItems(Integer.valueOf(deptcode));
                // Load Category to Category List
                if (Category.moveToFirst()) {
                    lstvwCategory.setVisibility(View.VISIBLE);
                    LoadCategories(Category);

                    /*GetItemDetailsByDept(Integer.valueOf(deptcode));
                    if (Name.length > 0) {
                        // Assign item grid to image adapter
                        grdItems.setAdapter(new ImageAdapter(myContext, Name, MenuCode, ImageUri, Byte.parseByte("1")));
                        // Make the item grid visible
                        grdItems.setVisibility(View.VISIBLE);
                    } else {
                        // Make the item grid invisible
                        grdItems.setVisibility(View.INVISIBLE);
                    }*/
                }
                else {

                   Toast.makeText(myContext, "No category found for this department", Toast.LENGTH_SHORT).show();
                    /*
                    lstvwCategory.setAdapter(null);
                    //MsgBox.Show("","Items");
                    GetItemDetailsByDept(Integer.valueOf(deptcode));//, Integer.valueOf(categdeptcode));
                    // This condition is to avoid NullReferenceException in getCount()
                    // in ImageAdapter class.
                    if (Name.length > 0) {
                        // Assign item grid to image adapter
                        grdItems.setAdapter(new ImageAdapter(myContext, Name, MenuCode, ImageUri, Byte.parseByte("1")));
                        // Make the item grid visible
                        grdItems.setVisibility(View.VISIBLE);
                    } else {
                        // Make the item grid invisible
                        grdItems.setVisibility(View.INVISIBLE);
                    }*/
                }
            }
        });
    }

    private void LoadDepartmentsItems(Cursor crsrDept) {

        Cursor cursor = dbStock.getDepartments();
        String columns[] = new String[]{"_id", "DeptName"};
        int vals[] = new int[]{R.id.tvlstDeptCode, R.id.tvlstDeptName};
        deptdataAdapter = new SimpleCursorAdapter(this, R.layout.activity_deptnames, cursor, columns, vals);

        lstvwDepartment.setAdapter(deptdataAdapter);
        lstvwDepartment.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                @SuppressWarnings("unchecked")
                //String val = o.get("id");
                        String deptcode = ((TextView) view.findViewById(R.id.tvlstDeptCode)).getText().toString();

                lstvwCategory.setAdapter(null);
                //MsgBox.Show("","Items");
                GetItemDetailsByDept(Integer.valueOf(deptcode));//, Integer.valueOf(categdeptcode));
                // This condition is to avoid NullReferenceException in getCount()
                // in ImageAdapter class.
                if (Name.length > 0) {
                    // Assign item grid to image adapter
                    grdItems.setAdapter(new ImageAdapter(myContext, Name, MenuCode, ImageUri, Byte.parseByte("1")));
                    // Make the item grid visible
                    grdItems.setVisibility(View.VISIBLE);
                } else {
                    // Make the item grid invisible
                    Toast.makeText(myContext, "No item found for this department ", Toast.LENGTH_SHORT).show();
                    grdItems.setVisibility(View.INVISIBLE);
                }

            }
        });
       // LoadItemsForAllDepartment();
    }

    void LoadItemsForAllDepartment()
    {
        Cursor Items = dbStock.getItemsForAllDepartments();
        int count = 0;
        while  (Items!=null && Items.moveToNext())
        {
            count++;
            Name = new String[Items.getCount()];
            ImageUri = new String[Items.getCount()];
            MenuCode = new int[Items.getCount()];

            do {
                MenuCode[Items.getPosition()] = Items.getInt(Items.getColumnIndex("MenuCode"));
                Name[Items.getPosition()] = Items.getString(Items.getColumnIndex("ItemName"));
                ImageUri[Items.getPosition()] = Items.getString(Items.getColumnIndex("ImageUri"));
            } while (Items.moveToNext());

        }
        if(count ==0){

            Log.d("LoadItemsToGrid", "No Items found");

            Name = new String[0];
            ImageUri = new String[0];
            MenuCode = new int[0];
        }
        if (Name.length > 0) {
            // Assign item grid to image adapter
            grdItems.setAdapter(new ImageAdapter(myContext, Name, MenuCode, ImageUri, Byte.parseByte("1")));
            // Make the item grid visible
            grdItems.setVisibility(View.VISIBLE);
        } else {
            // Make the item grid invisible
            grdItems.setVisibility(View.INVISIBLE);
        }
    }

    private void LoadCategories(Cursor crsrCateg) {

        //Cursor cursor = crsrCateg;
        String columns[] = new String[]{"_id", "CategName", "DeptCode"};
        int vals[] = new int[]{R.id.tvlstCategCode, R.id.tvlstCategName, R.id.tvlstCategDeptCode};
        categdataAdapter = new SimpleCursorAdapter(this, R.layout.activity_categnames, crsrCateg, columns, vals);

        lstvwCategory.setAdapter(categdataAdapter);
        grdItems.setAdapter(null);
        lstvwCategory.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                @SuppressWarnings("unchecked")

                String categcode = ((TextView) view.findViewById(R.id.tvlstCategCode)).getText().toString();
                String categdeptcode = ((TextView) view.findViewById(R.id.tvlstCategDeptCode)).getText().toString();

//                Toast.makeText(myContext, message, Toast.LENGTH_LONG).show();
                grdItems.setVisibility(View.VISIBLE);
                GetItemDetails(Integer.valueOf(categcode));//, Integer.valueOf(categdeptcode));
                // This condition is to avoid NullReferenceException in getCount()
                // in ImageAdapter class.
                if (Name.length > 0) {
                    // Assign item grid to image adapter
                    grdItems.setAdapter(new ImageAdapter(myContext, Name, MenuCode, ImageUri, Byte.parseByte("1")));
                    // Make the item grid visible
                    grdItems.setVisibility(View.VISIBLE);
                } else {
                    // Make the item grid invisible
                    Toast.makeText(myContext, "No item found for this category ", Toast.LENGTH_SHORT).show();
                    grdItems.setVisibility(View.INVISIBLE);
                }

            }
        });


    }

    // Get Items by CategCode
    private void GetItemDetails() {
        Cursor Items = null;
        Items = dbStock.getAllItems();
        //Items = dbBillScreen.getAllItemsWithoutDeptCateg();
        if (Items.moveToFirst()) {

            Name = new String[Items.getCount()];
            ImageUri = new String[Items.getCount()];
            MenuCode = new int[Items.getCount()];

            do {
                MenuCode[Items.getPosition()] = Items.getInt(Items.getColumnIndex("MenuCode"));
                Name[Items.getPosition()] = Items.getString(Items.getColumnIndex("ItemName"));
                ImageUri[Items.getPosition()] = Items.getString(Items.getColumnIndex("ImageUri"));
            } while (Items.moveToNext());

        } else {
            Name = new String[0];
            ImageUri = new String[0];
            MenuCode = new int[0];
        }
    }

    private void GetItemDetailswithoutDeptCateg() {
        Cursor Items = null;
        //Items = dbBillScreen.getAllItems();
        Items = dbStock.getAllItemsWithoutDeptCateg();
        if (Items.moveToFirst()) {

            Name = new String[Items.getCount()];
            ImageUri = new String[Items.getCount()];
            MenuCode = new int[Items.getCount()];

            do {
                MenuCode[Items.getPosition()] = Items.getInt(Items.getColumnIndex("MenuCode"));
                Name[Items.getPosition()] = Items.getString(Items.getColumnIndex("ItemName"));
                ImageUri[Items.getPosition()] = Items.getString(Items.getColumnIndex("ImageUri"));
            } while (Items.moveToNext());

        } else {
            Name = new String[0];
            ImageUri = new String[0];
            MenuCode = new int[0];
        }
    }

    // Get Items by CategCode
    private void GetItemDetails(int iCategCode) {
        Cursor Items = null;
        Items = dbStock.getCatbyItems(iCategCode);
        if (Items.moveToFirst()) {

            Name = new String[Items.getCount()];
            ImageUri = new String[Items.getCount()];
            MenuCode = new int[Items.getCount()];

            do {
                MenuCode[Items.getPosition()] = Items.getInt(Items.getColumnIndex("MenuCode"));
                Name[Items.getPosition()] = Items.getString(Items.getColumnIndex("ItemName"));
                ImageUri[Items.getPosition()] = Items.getString(Items.getColumnIndex("ImageUri"));
            } while (Items.moveToNext());

        } else {

            Log.d("LoadItemsToGrid", "No Items found for Category " + iCategCode);

            Name = new String[0];
            ImageUri = new String[0];
            MenuCode = new int[0];
        }
    }

    // Get Items by DeptCode
    private void GetItemDetailsByDept(int iDeptCode) {
        Cursor Items = null;
        Items = dbStock.getItems(iDeptCode);
        if (Items.moveToFirst()) {

            Name = new String[Items.getCount()];
            ImageUri = new String[Items.getCount()];
            MenuCode = new int[Items.getCount()];

            do {
                MenuCode[Items.getPosition()] = Items.getInt(Items.getColumnIndex("MenuCode"));
                Name[Items.getPosition()] = Items.getString(Items.getColumnIndex("ItemName"));
                ImageUri[Items.getPosition()] = Items.getString(Items.getColumnIndex("ImageUri"));
            } while (Items.moveToNext());

        } else {

            Log.d("LoadItemsToGrid", "No Items found");

            Name = new String[0];
            ImageUri = new String[0];
            MenuCode = new int[0];
        }
    }

    OnItemClickListener GridItemImageClick = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
            // TODO Auto-generated method stub
            //MsgBox.Show("", v.getTag().toString());
            Cursor Item = null;
            if (v.getTag() != null) {
                Item = dbStock.getItem(Integer.parseInt(v.getTag().toString()));
                if (Item.moveToNext()) {
                    strMenuCode = Item.getString(Item.getColumnIndex("MenuCode"));
                    ItemLongName.setText(Item.getString(Item.getColumnIndex("ItemName")));
                    tvExistingStock.setText(Item.getString(Item.getColumnIndex("Quantity")));
                    txtRate1.setText(Item.getString(Item.getColumnIndex("DineInPrice1")));
                    txtRate2.setText(Item.getString(Item.getColumnIndex("DineInPrice2")));
                    txtRate3.setText(Item.getString(Item.getColumnIndex("DineInPrice3")));
                    txtNewStock.setText("0");
                    btnUpdate.setEnabled(true);
                }
            }

        }
    };

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

    @Override
    public void onHomePressed() {
        ActionBarUtils.navigateHome(this);
    }
}

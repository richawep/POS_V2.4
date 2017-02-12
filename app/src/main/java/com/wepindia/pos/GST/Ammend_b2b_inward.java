/****************************************************************************
 * Project Name		:	VAJRA
 *
 * File Name		:	HeaderFooterActivity
 *
 * Purpose			:	Represents bill header and footer configuration activity,
 * 						takes care of all UI back end operations in this activity,
 * 						such as event handling data read from or display in views.
 *
 * DateOfCreation	:	07-November-2012
 *
 * Author			:	Balasubramanya Bharadwaj B S
 *
 ****************************************************************************/
package com.wepindia.pos.GST;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.wep.common.app.Database.DatabaseHandler;
import com.wepindia.pos.GenericClasses.MessageDialog;
import com.wepindia.pos.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Ammend_b2b_inward extends Activity{

    Context myContext;
    DatabaseHandler dbAmmend_b2cs;
    MessageDialog MsgBox;// = new MessageDialog(HeaderFooterActivity.this);
    EditText et_gstin_ori, et_invno_ori, et_invdate_ori, et_gstin_rev, et_invno_rev, et_invdate_rev;
    EditText et_value, et_pos,et_hsn,et_taxval,et_igstrate,et_sgstrate,et_cgstrate;
    Spinner spnr_g_s;

    Button btnAdd, btnSave, btnClear,btnClose;
    TableLayout tbl_data ;
    TableRow rowItems;
    private String TAG = Ammend_b2b_inward.class.getSimpleName();
    TextView tvTitleDate;// = (TextView) findViewById(R.id.tvTitleBarDate);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ammend_b2b_inward);
        myContext = this;
        tvTitleDate = (TextView) findViewById(R.id.tvTitleBarDate);

        dbAmmend_b2cs= new DatabaseHandler(Ammend_b2b_inward.this);
        MsgBox = new MessageDialog(myContext);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN );

        try{
            dbAmmend_b2cs.CreateDatabase();
            dbAmmend_b2cs.OpenDatabase();
            InitializeViews();
            Reset();
        }
        catch(Exception exp){
            exp.printStackTrace();
            MsgBox.Show("Exception", exp.getMessage());
        }
    }

    void InitializeViews()
    {
        et_gstin_ori = (EditText) findViewById(R.id.et_gstin_ori);
        et_invno_ori = (EditText)findViewById(R.id.et_invno_ori);
        et_invdate_ori = (EditText) findViewById(R.id.et_invdate_ori);
        et_gstin_rev = (EditText) findViewById(R.id.et_gstin_rev);
        et_invno_rev = (EditText) findViewById(R.id.et_invno_rev);
        et_invdate_rev = (EditText) findViewById(R.id.et_invdate_rev);
        et_value = (EditText)findViewById(R.id.et_value);
        et_pos = (EditText) findViewById(R.id.et_pos);
        et_taxval = (EditText) findViewById (R.id.et_taxval);
        et_igstrate = (EditText) findViewById (R.id.et_igstrate);
        et_cgstrate = (EditText) findViewById (R.id.et_cgstrate);
        et_sgstrate = (EditText) findViewById (R.id.et_sgstrate);
        et_hsn= (EditText)findViewById(R.id.et_hsn);

        spnr_g_s = (Spinner) findViewById(R.id.spnr_g_s);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnClose = (Button) findViewById(R.id.btnClose);

        tbl_data = (TableLayout) findViewById(R.id.tbl_data);

    }

    void Reset()
    {
        et_gstin_ori.setText("12ANTPA0870E1A1");
        et_invno_ori.setText("23");
        et_invdate_ori.setText("12-11-2016");
        et_gstin_rev.setText("12ANTPA0870E1A1");
        et_invno_rev.setText("50");
        et_invdate_rev.setText("22-11-2016");
        et_value.setText("500");
        et_pos.setText("14");
        et_taxval.setText("1000");
        et_hsn.setText("h5");
        et_igstrate.setText("18");
        et_sgstrate.setText("9");
        et_cgstrate.setText("9");

        spnr_g_s = (Spinner) findViewById(R.id.spnr_g_s);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnClose = (Button) findViewById(R.id.btnClose);

        btnSave.setEnabled(false);

    }


    public void Add(View v)
    {
        String gstin_ori = et_gstin_ori.getText().toString();
        String gstin_rev = et_gstin_rev.getText().toString();
        String invno_ori = et_invno_ori.getText().toString();
        String invno_rev = et_invno_rev.getText().toString();
        String invdate_ori = et_invdate_ori.getText().toString();
        String invdate_rev = et_invdate_rev.getText().toString();
        String hsn = et_hsn.getText().toString();
        String pos1 = et_pos.getText().toString();
        String value = et_value.getText().toString();
         String taxval = et_taxval.getText().toString();
        String supply = spnr_g_s.getSelectedItem().toString();



        String igstrate = et_igstrate.getText().toString();
        String cgstrate = et_cgstrate.getText().toString();
        String sgstrate = et_sgstrate.getText().toString();
        int count =1;



        if (gstin_ori.equals("") || (invdate_ori.equals("")) || (invno_ori.equals(""))||(gstin_rev.equals(""))||
                (invdate_rev.equals("")) || (invno_rev.equals(""))||(pos1.equals(""))||
                (value.equals("")) || (taxval.equals(""))||
                (igstrate.equals(""))|| (sgstrate.equals(""))||(cgstrate.equals("")))
        {
            MsgBox.setTitle(" Error ")
                    .setMessage(" Please fill all details ")
                    .show();
        }else
        {
            rowItems = new TableRow(myContext);
            rowItems.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            //rowItems.setBackgroundResource(R.drawable.border_item);


            TextView Sno  = new TextView(myContext);
            Sno.setText(String.valueOf(count));
            Sno.setWidth(50);
            Sno.setGravity(Gravity.CENTER);
            Sno.setBackgroundResource(R.drawable.border_item);
            count++;
            rowItems.addView(Sno);


            TextView tvmonth = new TextView(myContext);
            tvmonth.setText(gstin_ori);
            tvmonth.setWidth(180);
            tvmonth.setBackgroundResource(R.drawable.border_item);
            rowItems.addView(tvmonth);

            TextView tvsupply_ori = new TextView(myContext);
            tvsupply_ori.setText(invno_ori);
            tvsupply_ori.setWidth(70);
            tvsupply_ori.setGravity(Gravity.CENTER);
            tvsupply_ori.setBackgroundResource(R.drawable.border_item);
            rowItems.addView(tvsupply_ori);

            TextView tvhsn_ori = new TextView(myContext);
            tvhsn_ori.setText(invdate_ori);
            tvhsn_ori.setWidth(100);
            tvhsn_ori.setGravity(Gravity.CENTER);
            tvhsn_ori.setBackgroundResource(R.drawable.border_item);
            rowItems.addView(tvhsn_ori);

            TextView tvpos_ori = new TextView(myContext);
            tvpos_ori.setText(gstin_rev);
            tvpos_ori.setWidth(180);
            tvpos_ori.setGravity(Gravity.CENTER);
            tvpos_ori.setBackgroundResource(R.drawable.border_item);
            rowItems.addView(tvpos_ori);

            TextView tvsupply_rev = new TextView(myContext);
            tvsupply_rev.setText(invno_rev);
            tvsupply_rev.setBackgroundResource(R.drawable.border_item);
            tvsupply_rev.setWidth(70);
            tvsupply_rev.setGravity(Gravity.CENTER);
            rowItems.addView(tvsupply_rev);

            TextView tvhsn_rev = new TextView(myContext);
            tvhsn_rev.setBackgroundResource(R.drawable.border_item);
            tvhsn_rev.setText(invdate_rev);
            tvhsn_rev.setWidth(100);
            tvhsn_rev.setGravity(Gravity.CENTER);
            rowItems.addView(tvhsn_rev);

            TextView tvpos_rev = new TextView(myContext);
            tvpos_rev.setText(hsn);
            tvpos_rev.setBackgroundResource(R.drawable.border_item);
            tvpos_rev.setWidth(55);
            tvpos_rev.setGravity(Gravity.CENTER);
            rowItems.addView(tvpos_rev);

            TextView tvpos_rev1 = new TextView(myContext);
            String suuplytype = spnr_g_s.getSelectedItem().toString();
            tvpos_rev1.setText(suuplytype);
            tvpos_rev1.setBackgroundResource(R.drawable.border_item);
            tvpos_rev1.setWidth(50);
            tvpos_rev1.setGravity(Gravity.CENTER);
            rowItems.addView(tvpos_rev1);



            TextView tvpos_rev2 = new TextView(myContext);
            tvpos_rev2.setText(value);
            tvpos_rev2.setBackgroundResource(R.drawable.border_item);
            tvpos_rev2.setWidth(80);
            rowItems.addView(tvpos_rev2);

            TextView tvTaxVal = new TextView(myContext);
            float taxable = Float.parseFloat(taxval);
            tvTaxVal.setText(String.format("%.2f",taxable));
            tvTaxVal.setWidth(100);
            tvTaxVal.setBackgroundResource(R.drawable.border_item);
            rowItems.addView(tvTaxVal);

            TextView tvIgstRate = new TextView(myContext);
            float irate = Float.parseFloat(igstrate);
            tvIgstRate.setBackgroundResource(R.drawable.border_item);
            tvIgstRate.setText(String.format("%.2f",irate));
            tvIgstRate.setWidth(80);
            rowItems.addView(tvIgstRate);

            TextView tvIgstAmt = new TextView(myContext);
            float iamount =0;
            tvIgstAmt.setText("0");
            tvIgstAmt.setWidth(50);
            tvIgstAmt.setBackgroundResource(R.drawable.border_item);
            rowItems.addView(tvIgstAmt);

            TextView tvCgstRate = new TextView(myContext);
            float crate = Float.parseFloat(cgstrate);
            tvCgstRate.setText(String.format("%.2f",crate));
            tvCgstRate.setWidth(80);
            tvCgstRate.setBackgroundResource(R.drawable.border_item);
            rowItems.addView(tvCgstRate);

            TextView tvCgstAmt = new TextView(myContext);
            float camount =0;
            camount = taxable*(crate/100);
            tvCgstAmt.setText(String.format("%.2f",camount));
            tvCgstAmt.setWidth(70);
            tvCgstAmt.setBackgroundResource(R.drawable.border_item);
            rowItems.addView(tvCgstAmt);


            TextView tvSgstRate = new TextView(myContext);
            float srate = Float.parseFloat(sgstrate);
            tvSgstRate.setText(String.format("%.2f",srate));
            tvSgstRate.setWidth(80);
            tvSgstRate.setBackgroundResource(R.drawable.border_item);
            rowItems.addView(tvSgstRate);

            TextView tvSgstAmt = new TextView(myContext);
            float samount =0;
            samount = taxable*(srate/100);
            tvSgstAmt.setText(String.format("%.2f",samount));
            tvSgstAmt.setWidth(70);
            tvSgstAmt.setBackgroundResource(R.drawable.border_item);
            rowItems.addView(tvSgstAmt);


            TextView pos = new TextView(myContext);
            pos.setText(pos1);
            pos.setWidth(50);
            pos.setBackgroundResource(R.drawable.border_item);
            rowItems.addView(pos);

            int res = getResources().getIdentifier("delete", "drawable", this.getPackageName());
            ImageButton btnItemDelete = new ImageButton(myContext);
            btnItemDelete.setImageResource(res);
            btnItemDelete.setBackgroundResource(R.drawable.border_item);
            btnItemDelete.setLayoutParams(new TableRow.LayoutParams(40, 30));
            btnItemDelete.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(myContext)
                            .setTitle("Delete")
                            .setMessage("Are you sure you want to Delete this Item")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    View row = (View) v.getParent();
                                    ViewGroup container = ((ViewGroup) row.getParent());
                                    container.removeView(row);
                                    container.invalidate();

                                }
                            })
                            .setNegativeButton(R.string.cancel, null);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
            rowItems.addView(btnItemDelete);

            tbl_data.addView(rowItems);

            btnSave.setEnabled(true);
        }

    }

    public void Clear(View v) {
        for (int i = 0; i < tbl_data.getChildCount(); i++) {
            View Row = tbl_data.getChildAt(i);
            if (Row instanceof TableRow) {
                ((TableRow) Row).removeAllViews();
            }
        }
        Reset();
    }

    public void Save(View V)
    {
         int count = tbl_data.getChildCount();
        if (count<1)
        {
            MsgBox.setMessage("Please Add the Entry")
                    .setNeutralButton("OK",null)
                    .show();
            return;
        }

        for (int i=0; i<count; i++)
        {
            TableRow row = (TableRow) tbl_data.getChildAt(i);
            if (row.getChildAt(0)!= null)
            {
                TextView tvgstin_ori = (TextView) row.getChildAt(1);
                TextView tvinvno_ori = (TextView) row.getChildAt(2);
                TextView tvinvdate_ori = (TextView) row.getChildAt(3);
                TextView tvgstin_rev = (TextView) row.getChildAt(4);
                TextView tvinvno_rev = (TextView) row.getChildAt(5);
                TextView tvinvdate_rev = (TextView) row.getChildAt(6);
                TextView tvhsn = (TextView) row.getChildAt(7);
                TextView tvgs = (TextView) row.getChildAt(8);
                TextView tvvalue = (TextView) row.getChildAt(9);
                TextView tvtaxval= (TextView) row.getChildAt(10);
                TextView tvigstrate = (TextView) row.getChildAt(11);
                TextView tvigstamt = (TextView) row.getChildAt(12);
                TextView tvcgstrate = (TextView) row.getChildAt(13);
                TextView tvcgstamt = (TextView) row.getChildAt(14);
                TextView tvsgstrate = (TextView) row.getChildAt(15);
                TextView tvsgstamt = (TextView) row.getChildAt(16);
                TextView tvpos = (TextView) row.getChildAt(17);

                String gstin_ori = tvgstin_ori.getText().toString();
                String invno_ori = tvinvno_ori.getText().toString();
                String invdate_ori = tvinvdate_ori.getText().toString();
                String gstin_rev = tvgstin_rev.getText().toString();
                String invno_rev = tvinvno_rev.getText().toString();
                String invdate_rev = tvinvdate_rev.getText().toString();
                String hsn = tvhsn.getText().toString();
                String gs = tvgs.getText().toString();
                String value = tvvalue.getText().toString();
                String taxval = tvtaxval.getText().toString();
                String igstrate = tvigstrate.getText().toString();
                String cgstrate = tvcgstrate.getText().toString();
                String sgstrate = tvsgstrate.getText().toString();
                String igstamt = tvigstamt.getText().toString();
                String cgstamt = tvcgstamt.getText().toString();
                String sgstamt = tvsgstamt.getText().toString();
                String pos = tvpos.getText().toString();
                Calendar c =  Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                String date = sdf.format(c.getTime());

                long l = dbAmmend_b2cs.addB2BAmmend(gstin_ori, invno_ori , invdate_ori, gstin_rev, invno_rev ,invdate_rev,
                        gs,hsn,value,taxval,igstrate,cgstrate, sgstrate , igstamt, cgstamt ,sgstamt ,pos,"B2B",date);
            }

        }

    }
    public void Close(View v){
        dbAmmend_b2cs.CloseDatabase();
        this.finish();
    }
}

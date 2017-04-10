package com.wepindia.pos.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wep.common.app.Database.DatabaseHandler;
import com.wep.common.app.gst.GSTR1CDNCDN;
import com.wep.common.app.views.WepButton;
import com.wepindia.pos.GenericClasses.DateTime;
import com.wepindia.pos.GenericClasses.MessageDialog;
import com.wepindia.pos.R;
import com.wepindia.pos.adapters.CDNoteAdapter;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class FragmentCreditNote extends Fragment {


    EditText edt_IGSTRate,edt_IGSTAmount,edt_CGSTRate,edt_CGSTAmount,edt_SGSTRate,edt_SGSTAmount,edt_Value;
    EditText edt_InvoiceNo,edt_reason;
    TextView tv_InvoiceDate,tv_recipientName,tv_billamount,tv_reverseCharge,tv_totalIGSTVal,tv_totalCGSTVal,tv_totalSGSTVal,
            tv_note_no,tv_note_date;
    ImageButton imgButton_cal_Invoice;
    ListView listview_credit;
    WepButton btnAddCredit,btnEditCredit,btnClearCredit,btnPrintCredit,btnCloseCredit,btnCreditOk;
    LinearLayout linear_tax,linear_recipient;
    RelativeLayout rl_creditDisplay;

    private static final String TAG = FragmentDepartment.class.getSimpleName();
    Context myContext;
    DatabaseHandler dbCredit;
    MessageDialog MsgBox;
    ArrayList<GSTR1CDNCDN> noteList;
    CDNoteAdapter noteAdapter = null;
    Date date;
    public FragmentCreditNote() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            dbCredit = new DatabaseHandler(getActivity());
            date = new Date();

        }catch (Exception e){

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_credit_note, container, false);
        myContext = getActivity();
        MsgBox = new MessageDialog(myContext);
        try{
            dbCredit.CloseDatabase();
            dbCredit.CreateDatabase();
            dbCredit.OpenDatabase();
            InitializeViewVariables(view);
            ClearAll();

        }
        catch(Exception exp){
            Toast.makeText(myContext, "OnCreate: " + exp.getMessage(), Toast.LENGTH_LONG).show();
        }
        return view;
    }


    private void InitializeViewVariables(View v)
    {
        edt_IGSTRate = (EditText) v.findViewById(R.id.edt_IGSTRate);
        edt_IGSTAmount = (EditText) v.findViewById(R.id.edt_IGSTAmount);
        edt_CGSTRate = (EditText) v.findViewById(R.id.edt_CGSTRate);
        edt_CGSTAmount = (EditText) v.findViewById(R.id.edt_CGSTAmount);
        edt_SGSTRate = (EditText) v.findViewById(R.id.edt_SGSTRate);
        edt_SGSTAmount = (EditText) v.findViewById(R.id.edt_SGSTAmount);
        edt_Value = (EditText) v.findViewById(R.id.edt_Value);
        edt_InvoiceNo = (EditText) v.findViewById(R.id.edt_InvoiceNo);
        edt_reason = (EditText) v.findViewById(R.id.edt_reason);

        tv_InvoiceDate = (TextView) v.findViewById(R.id.tv_InvoiceDate);
        tv_recipientName = (TextView) v.findViewById(R.id.tv_recipientName);
        tv_billamount = (TextView) v.findViewById(R.id.tv_billamount);
        tv_reverseCharge = (TextView) v.findViewById(R.id.tv_reverseCharge);
        tv_totalIGSTVal = (TextView) v.findViewById(R.id.tv_totalIGSTVal);
        tv_totalCGSTVal = (TextView) v.findViewById(R.id.tv_totalCGSTVal);
        tv_totalSGSTVal = (TextView) v.findViewById(R.id.tv_totalSGSTVal);
        tv_note_no = (TextView) v.findViewById(R.id.tv_note_no);
        tv_note_date = (TextView) v.findViewById(R.id.tv_note_date);

        imgButton_cal_Invoice = (ImageButton) v.findViewById(R.id.imgButton_cal_Invoice) ;
        imgButton_cal_Invoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateSelection(v);
            }
        });
        listview_credit = (ListView) v.findViewById(R.id.listview_credit) ;
        listview_credit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listViewItemClickEvent(noteAdapter.getItems(position));
            }
        });
        btnAddCredit = (WepButton) v.findViewById(R.id.btnAddCredit) ;
        btnEditCredit = (WepButton) v.findViewById(R.id.btnEditCredit) ;
        btnClearCredit = (WepButton) v.findViewById(R.id.btnClearCredit) ;
        btnPrintCredit = (WepButton) v.findViewById(R.id.btnPrintCredit) ;
        btnCloseCredit = (WepButton) v.findViewById(R.id.btnCloseCredit) ;
        btnCreditOk = (WepButton) v.findViewById(R.id.btnCreditOk) ;

        btnAddCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddCredit();
                ClearNoteData();
                loadCredits(edt_InvoiceNo.getText().toString(), tv_InvoiceDate.getText().toString());

            }
        });
        btnEditCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditCredit();
                ClearNoteData();
                loadCredits(edt_InvoiceNo.getText().toString(), tv_InvoiceDate.getText().toString());
            }
        });

        /*btnPrintCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintCredit();
            }
        });
        */
        btnCloseCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseCredit();
            }
        });
        btnClearCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearAll();
            }
        });
        btnCreditOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String invoiceNo = edt_InvoiceNo.getText().toString().trim();
                String invoiceDate = tv_InvoiceDate.getText().toString().trim();
                if (!(invoiceNo != null && invoiceDate != null && !invoiceNo.equals("") && !invoiceDate.equals(""))) {
                    MsgBox.Show("Error", "Please enter invoice no and date for which credit note is to be issued");
                } else {
                    try {
                        Date date = new SimpleDateFormat("dd-MM-yyyy").parse(invoiceDate);
                        Cursor cursor = dbCredit.getBillDetail(Integer.parseInt(invoiceNo), String.valueOf(date.getTime()));
                        if (cursor != null && cursor.moveToFirst()) {
                            int result = fillData(cursor);
                            if(result ==1)
                            {
                                removeOpacity();
                                loadCredits(invoiceNo, invoiceDate);
                            }

                        } else {
                            MsgBox.Show("Error", "This invoice is not present");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(myContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        linear_tax = (LinearLayout) v.findViewById(R.id.linear_tax) ;
        linear_recipient = (LinearLayout) v.findViewById(R.id.linear_recipient) ;
        rl_creditDisplay = (RelativeLayout) v.findViewById(R.id.rl_creditDisplay) ;
    }

    private void listViewItemClickEvent(GSTR1CDNCDN note)
    {
        try {
            edt_IGSTRate.setText(String.valueOf(note.getIrt()));
            edt_IGSTAmount.setText(String.valueOf(note.getIamt()));
            edt_CGSTRate.setText(String.valueOf(note.getCrt()));
            edt_CGSTAmount.setText(String.valueOf(note.getCamt()));
            edt_SGSTRate.setText(String.valueOf(note.getSrt()));
            edt_SGSTAmount.setText(String.valueOf(note.getSamt()));
            edt_Value.setText(String.valueOf(note.getVal()));
            edt_reason.setText(note.getRsn());
            tv_note_no.setText(String.valueOf(note.getNt_num()));

            Date date_note = (new SimpleDateFormat("dd-MM-yyyy")).parse(note.getNt_dt());
            tv_note_date.setText(String.valueOf(date_note.getTime()));
            btnAddCredit.setEnabled(false);
            btnEditCredit.setEnabled(true);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private  int fillData(Cursor cursor)
    {
        String name = cursor.getString(cursor.getColumnIndex("GSTIN"));
        if(name== null || name.trim().equals(""))
        {
            name = cursor.getString(cursor.getColumnIndex("CustName"));
            if(name== null || name.trim().equals(""))
            {
                MsgBox.Show("Error","Since you have not saved Recipient's GSTIN/Name, you cannot make credit note for this invoice");
                return 0;
            }
        }
        tv_recipientName.setText(name);
        tv_billamount.setText(String.format("%.2f",cursor.getFloat(cursor.getColumnIndex("GrandTotal"))));
        String reverseCharge = cursor.getString(cursor.getColumnIndex("ReverseCharge"));
        if(reverseCharge== null || reverseCharge.equalsIgnoreCase("No")|| reverseCharge.equalsIgnoreCase("N")|| reverseCharge.equals(""))
        { tv_reverseCharge.setText("N");
        } else  {
            tv_reverseCharge.setText("Y");
        }
        return 1;

    }


    private  void loadCredits(String invoiceNo,String invoiceDate)
    {
        try {
            int count = 1;
            noteList = new ArrayList<GSTR1CDNCDN>();
            String date_str = String.valueOf((new SimpleDateFormat("dd-MM-yyyy").parse(invoiceDate)).getTime());

            Cursor cursor = dbCredit.getCreditDetails(invoiceNo, date_str, "C");
            while (cursor != null && cursor.moveToNext()) {
                GSTR1CDNCDN note = new GSTR1CDNCDN();
                long milli_note = cursor.getLong(cursor.getColumnIndex("NoteDate"));
                Date date=new Date(milli_note);
                String date_str1 = String.valueOf(new SimpleDateFormat("dd-MM-yyyy").format(date));
                note.setSno(count++);
                note.setNtty(cursor.getString(cursor.getColumnIndex("NoteType")));
                note.setNt_num(cursor.getDouble(cursor.getColumnIndex("NoteNo")));
                note.setNt_dt(date_str1);
                note.setInum(cursor.getString(cursor.getColumnIndex("InvoiceNo")));
                note.setIdt(cursor.getString(cursor.getColumnIndex("InvoiceDate")));
                note.setVal(cursor.getDouble(cursor.getColumnIndex("DifferentialValue")));
                note.setIrt(cursor.getDouble(cursor.getColumnIndex("IGSTRate")));
                note.setIamt(cursor.getDouble(cursor.getColumnIndex("IGSTAmount")));
                note.setCrt(cursor.getDouble(cursor.getColumnIndex("CGSTRate")));
                note.setCamt(cursor.getDouble(cursor.getColumnIndex("CGSTAmount")));
                note.setSrt(cursor.getDouble(cursor.getColumnIndex("SGSTRate")));
                note.setSamt(cursor.getDouble(cursor.getColumnIndex("SGSTAmount")));
                note.setRsn(cursor.getString(cursor.getColumnIndex("Reason")));
                noteList.add(note);
            }
            if (noteAdapter == null) {
                noteAdapter = new CDNoteAdapter(getActivity(), noteList, dbCredit,"C");
                listview_credit.setAdapter(noteAdapter);
            } else {
                noteAdapter.notifyNewDataAdded(noteList);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void AddCredit()
    {
        try {
            GSTR1CDNCDN note = new GSTR1CDNCDN();
            String date_temp = new SimpleDateFormat("dd-MM-yyyy").format(date);
            Date date_new = new SimpleDateFormat("dd-MM-yyyy").parse(date_temp);
            long milii = date_new.getTime();
            int notenum = dbCredit.getMaxCreditNoteNo();
            String invoiceDate = tv_InvoiceDate.getText().toString();
            Date date_inv = (new SimpleDateFormat("dd-MM-yyyy").parse(invoiceDate));
            String name  = tv_recipientName.getText().toString();
            String reason = edt_reason.getText().toString();
            String reverseCharge = tv_reverseCharge.getText().toString();

            note.setSno(listview_credit.getCount());
            note.setNtty("C");
            note.setNt_num(notenum);
            note.setNt_dt(String.valueOf(milii));
            note.setInum(edt_InvoiceNo.getText().toString());
            note.setIdt(String.valueOf(date_inv.getTime()));
            note.setVal(Float.parseFloat(String.format("%.2f", Float.parseFloat(edt_Value.getText().toString()))));
            note.setIrt(Float.parseFloat(String.format("%.2f", Float.parseFloat(edt_IGSTRate.getText().toString()))));
            note.setIamt(Float.parseFloat(String.format("%.2f", Float.parseFloat(edt_IGSTAmount.getText().toString()))));
            note.setCrt(Float.parseFloat(String.format("%.2f", Float.parseFloat(edt_CGSTRate.getText().toString()))));
            note.setCamt(Float.parseFloat(String.format("%.2f", Float.parseFloat(edt_CGSTAmount.getText().toString()))));
            note.setSrt(Float.parseFloat(String.format("%.2f", Float.parseFloat(edt_SGSTRate.getText().toString()))));
            note.setSamt(Float.parseFloat(String.format("%.2f", Float.parseFloat(edt_SGSTAmount.getText().toString()))));

            long lResult = dbCredit.addCredit(note, name,reason, reverseCharge);
            if(lResult>0) {
                Log.d(TAG, " Credit Note inserted Successfully @" + lResult);
                Toast.makeText(myContext, "Credit Note inserted Successfully", Toast.LENGTH_SHORT).show();
            }
            else
                Log.d(TAG, " Credit Note insertion failed !!");

        }catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(myContext, "An error occured."+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void EditCredit()
    {
        try {
            GSTR1CDNCDN note = new GSTR1CDNCDN();

            String invoiceDate = tv_InvoiceDate.getText().toString();
            Date date_inv = (new SimpleDateFormat("dd-MM-yyyy").parse(invoiceDate));
            String reason = edt_reason.getText().toString();

            note.setNt_num(Double.parseDouble(tv_note_no.getText().toString()));
            note.setNt_dt(tv_note_date.getText().toString());
            note.setInum(edt_InvoiceNo.getText().toString());
            note.setIdt(String.valueOf(date_inv.getTime()));
            note.setVal(Float.parseFloat(String.format("%.2f", Float.parseFloat(edt_Value.getText().toString()))));
            note.setIrt(Float.parseFloat(String.format("%.2f", Float.parseFloat(edt_IGSTRate.getText().toString()))));
            note.setIamt(Float.parseFloat(String.format("%.2f", Float.parseFloat(edt_IGSTAmount.getText().toString()))));
            note.setCrt(Float.parseFloat(String.format("%.2f", Float.parseFloat(edt_CGSTRate.getText().toString()))));
            note.setCamt(Float.parseFloat(String.format("%.2f", Float.parseFloat(edt_CGSTAmount.getText().toString()))));
            note.setSrt(Float.parseFloat(String.format("%.2f", Float.parseFloat(edt_SGSTRate.getText().toString()))));
            note.setSamt(Float.parseFloat(String.format("%.2f", Float.parseFloat(edt_SGSTAmount.getText().toString()))));

            long lResult = dbCredit.editCredit(note, reason);
            if(lResult>0) {
                Log.d(TAG, " Credit Note updated Successfully @" + lResult);
                Toast.makeText(myContext, "Credit Note updated Successfully", Toast.LENGTH_SHORT).show();
            }
            else
                Log.d(TAG, " Credit Note updation failed !!");

        }catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(myContext, "An error occured."+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void ClearNoteData()
    {
        edt_IGSTRate.setText("0.00");
        edt_IGSTAmount.setText("0.00");
        edt_CGSTRate.setText("0.00");
        edt_CGSTAmount.setText("0.00");
        edt_SGSTRate.setText("0.00");
        edt_SGSTAmount.setText("0.00");
        edt_Value.setText("0.00");
        edt_reason.setText("");
        tv_note_no.setText("");
        tv_note_date.setText("");


        tv_totalIGSTVal.setText("0.00");
        tv_totalCGSTVal.setText("0.00");
        tv_totalSGSTVal.setText("0.00");

        noteAdapter = null;
        listview_credit.setAdapter(noteAdapter);

        btnAddCredit.setEnabled(true);
        btnEditCredit.setEnabled(false);
        btnPrintCredit.setEnabled(false);

    }
    private void ClearAll()
    {
        edt_IGSTRate.setText("0.00");
        edt_IGSTAmount.setText("0.00");
        edt_CGSTRate.setText("0.00");
        edt_CGSTAmount.setText("0.00");
        edt_SGSTRate.setText("0.00");
        edt_SGSTAmount.setText("0.00");
        edt_Value.setText("0.00");
        edt_InvoiceNo.setText("");
        edt_reason.setText("");
        tv_note_no.setText("");
        tv_note_date.setText("");

        tv_InvoiceDate.setText("");
        tv_recipientName.setText("");
        tv_billamount.setText("0.00");
        tv_reverseCharge.setText("");
        tv_totalIGSTVal.setText("0.00");
        tv_totalCGSTVal.setText("0.00");
        tv_totalSGSTVal.setText("0.00");

        noteAdapter = null;
        listview_credit.setAdapter(noteAdapter);

        btnAddCredit.setEnabled(false);
        btnEditCredit.setEnabled(false);
        //btnClearCredit.setEnabled(false);
        btnPrintCredit.setEnabled(false);

        rl_creditDisplay.setAlpha(0.4f);
        linear_tax.setAlpha(0.4f);
        linear_tax.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        linear_recipient.setAlpha(0.4f);
        linear_recipient.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }
    void removeOpacity()
    {
        rl_creditDisplay.setAlpha(1);
        linear_tax.setAlpha(1);
        linear_tax.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        linear_recipient.setAlpha(1);

        btnAddCredit.setEnabled(true);
        //btnEditCredit.setEnabled(true);
        //btnPrintCredit.setEnabled(true);
    }
    public void dateSelection(View v){
    try {
        AlertDialog.Builder dlgReportDate = new AlertDialog.Builder(myContext);
        final DatePicker dateReportDate = new DatePicker(myContext);
        Date d = new Date();
        CharSequence currentdate = DateFormat.format("yyyy-MM-dd", d.getTime());
        DateTime objDate = new DateTime(currentdate.toString());
        dateReportDate.updateDate(objDate.getYear(), objDate.getMonth(), objDate.getDay());


        dlgReportDate
                .setIcon(R.drawable.ic_launcher)
                .setTitle("Date Selection")
                .setMessage("Please select Invoice date")
                .setView(dateReportDate)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        // richa date format change

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
                        tv_InvoiceDate.setText(strDate);
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
        e.printStackTrace();
    }
}
    private void CloseCredit()
    {
        dbCredit.CloseDatabase();
        getActivity().finish();
    }

}

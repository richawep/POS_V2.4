package com.wepindia.pos.GST.controlers;

import android.content.Context;
import android.database.Cursor;

import com.wep.common.app.Database.DatabaseHandler;
import com.wep.common.app.gst.B2Csmall;
import com.wep.common.app.gst.GSTR1B2CSAData;
import com.wep.common.app.gst.GSTR1CDN;
import com.wep.common.app.gst.GSTR1CDNCDN;
import com.wep.common.app.gst.GSTR2B2BAData;
import com.wep.common.app.gst.GSTR2B2BData;
import com.wep.common.app.gst.GSTR2B2BITCDetails;
import com.wep.common.app.gst.GSTR2B2BInvoiceItems;
import com.wep.common.app.gst.GSTR2B2BInvoices;
import com.wep.common.app.gst.GSTR2B2BItemDetails;
import com.wep.common.app.gst.GSTR2CDN;
import com.wepindia.pos.GenericClasses.MessageDialog;

import java.util.ArrayList;

/**
 * Created by PriyabratP on 24-11-2016.
 */

public class GSTDataController {

    private final MessageDialog MsgBox;
    private DatabaseHandler dbReport;
    private Context myContext;

    public GSTDataController(Context context, DatabaseHandler dbReport) {
        this.myContext = context;
        this.dbReport = dbReport;
        MsgBox  = new MessageDialog(myContext);
    }


    public ArrayList<com.wep.common.app.gst.B2Csmall> getGSTR1B2CSDataList(String StartDate, String EndDate) {
        String GSTEnable = "0", POSEnable = "0", HSNEnable = "0", ReverseChargeEnabe = "0";
        ArrayList<com.wep.common.app.gst.B2Csmall> datalist_s = new ArrayList<com.wep.common.app.gst.B2Csmall>();
        try {
            Cursor billsettingcursor = dbReport.getBillSetting();
            if (billsettingcursor != null && billsettingcursor.moveToFirst()) {
                GSTEnable = billsettingcursor.getString(billsettingcursor.getColumnIndex("GSTEnable"));
                if (GSTEnable == null) {
                    GSTEnable = "0";
                } else if (GSTEnable.equals("1")) {
                    HSNEnable = billsettingcursor.getString(billsettingcursor.getColumnIndex("HSNCode_Out"));
                    if (HSNEnable == null) {
                        HSNEnable = "0";
                    }
                    POSEnable = billsettingcursor.getString(billsettingcursor.getColumnIndex("POS_Out"));
                    if (POSEnable == null) {
                        POSEnable = "0";
                    }
                    ReverseChargeEnabe = billsettingcursor.getString(billsettingcursor.getColumnIndex("ReverseCharge_Out"));
                    if (ReverseChargeEnabe == null) {
                        ReverseChargeEnabe = "0";
                    }
                }
            }
            //String StartDate = txtReportDateStart.getText().toString();
            //String EndDate = txtReportDateEnd.getText().toString();
            Cursor cursor = dbReport.getOutwardB2Cs(StartDate, EndDate);
            if (cursor == null) {
                //MsgBox = new AlertDialog.Builder(myContext);
                MsgBox.setMessage("No data for entered period B2C-S")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                if (cursor.moveToFirst()) {

                    int count = 1;

                    do {// item_detail table
                        String POS_str = cursor.getString(cursor.getColumnIndex("POS"));
                        float TaxableValue_f = Float.parseFloat(cursor.getString(cursor.getColumnIndex("TaxableValue")));
                        if ((POS_str.equals("")) || (POS_str.equals("") == false && TaxableValue_f <= 250000)) {
                            // for interstate + interstate only + <=2.5l
                            String InvNo = cursor.getString(cursor.getColumnIndex("InvoiceNo"));
                            String InvDate = cursor.getString(cursor.getColumnIndex("InvoiceDate"));
                            String custname_str = cursor.getString(cursor.getColumnIndex("CustName"));
                            String statecode_str = cursor.getString(cursor.getColumnIndex("CustStateCode"));

                            Cursor rowcursor = dbReport.getitems_b2cl(InvNo, InvDate, custname_str, statecode_str);
                            if (rowcursor == null) {
                                //MsgBox = new AlertDialog.Builder(myContext);
                                MsgBox.setMessage("No items for Invoice No : " + InvNo + " & Invoice Date : " + InvDate)
                                        .setPositiveButton("OK", null)
                                        .show();
                            } else { // bill level

                                try {

                                    while (rowcursor.moveToNext()) {

                                        String SupplyType_str;
                                        float subtotal_f, taxablevalue_f, CGSTRate_f, CGSTAmount_f, SGSTRate_f;
                                        float SGSTAmount_f, IGSTRate_f, IGSTAmount_f;
                                        int found = 0;

                                        // supply type (g/s)
                                        SupplyType_str = rowcursor.getString(rowcursor.getColumnIndex("SupplyType"));

                                        String HSN = rowcursor.getString(rowcursor.getColumnIndex("HSNCode"));
                                        String desc = rowcursor.getString(rowcursor.getColumnIndex("ItemName"));
                                        HSN = HSN + "-" + desc;
                                        //HSNCode_str = HSN;


                                        //CustStateCode_str  = rowcursor.getString(rowcursor.getColumnIndex("CustStateCode"));
                                        //String POS_str1 = rowcursor.getString(rowcursor.getColumnIndex("POS"));


                                        taxablevalue_f = Float.parseFloat(rowcursor.getString(rowcursor.getColumnIndex("TaxableValue")));

                                        String igstrate_str = rowcursor.getString(rowcursor.getColumnIndex("IGSTRate"));
                                        if (igstrate_str == null) {
                                            IGSTRate_f = 0;
                                        } else {
                                            IGSTRate_f = Float.parseFloat(igstrate_str);
                                        }

                                        String igstamt_str = rowcursor.getString(rowcursor.getColumnIndex("IGSTAmount"));
                                        if (igstamt_str == null) {
                                            IGSTAmount_f = 0;
                                        } else {
                                            IGSTAmount_f = Float.parseFloat(igstamt_str);
                                        }

                                        String cgstrate_str = rowcursor.getString(rowcursor.getColumnIndex("CGSTRate"));
                                        if (cgstrate_str == null) {
                                            CGSTRate_f = 0;
                                        } else {
                                            CGSTRate_f = Float.parseFloat(cgstrate_str);
                                        }

                                        String cgstamt_str = rowcursor.getString(rowcursor.getColumnIndex("CGSTAmount"));
                                        if (cgstamt_str == null) {
                                            CGSTAmount_f = 0;
                                        } else {
                                            CGSTAmount_f = Float.parseFloat(cgstamt_str);
                                        }

                                        String sgstrate_str = rowcursor.getString(rowcursor.getColumnIndex("SGSTRate"));
                                        if (sgstrate_str == null) {
                                            SGSTRate_f = 0;
                                        } else {
                                            SGSTRate_f = Float.parseFloat(sgstrate_str);
                                        }

                                        String sgstamt_str = rowcursor.getString(rowcursor.getColumnIndex("SGSTAmount"));
                                        if (sgstamt_str == null) {
                                            SGSTAmount_f = 0;
                                        } else {
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
                                        } else {
                                            found = 0;
                                            for (B2Csmall data_s : datalist_s) {
                                                if (data_s.getHSNCode().equalsIgnoreCase(HSN) && data_s.getPlaceOfSupply().equalsIgnoreCase(POS_str)) {
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

                                                    found = 1;
                                                    break;

                                                }
                                            }  // end of for loop
                                            if (found == 0) // not in list
                                            {
                                                datalist_s.add(obj);
                                            }
                                        } // end of else


                                        // } while (cursor.moveToNext());

                                    }

                                }// end try
                                catch (Exception e) {
                                    //MsgBox = new AlertDialog.Builder(myContext);
                                    MsgBox.setTitle("Error while fetching items details")
                                            .setMessage(e.getMessage())
                                            .setPositiveButton("OK", null)
                                            .show();
                                }
                            } // end else bill level
                        }
                    } while (cursor.moveToNext());

                }
            }
        }// end try
        catch (Exception e) {
            datalist_s = null;
            //MsgBox = new AlertDialog.Builder(myContext);
            MsgBox.setMessage(e.getMessage())
                    .setPositiveButton("OK", null)
                    .show();
        }
        return datalist_s;
    }

    public ArrayList<GSTR2B2BData> getB2BItems(String StartDate, String EndDate)
    {
        ArrayList<GSTR2B2BData> b2bDataList = new ArrayList<GSTR2B2BData>();
        try {
            Cursor cursor = dbReport.getGSTR2GSTNs();
            if (cursor == null)
            {
                b2bDataList = null;
            }
            else
            {
                if (cursor.moveToFirst()) {
                    do {
                        String num = cursor.getString(cursor.getColumnIndex("GSTIN"));
                        ArrayList<GSTR2B2BInvoices> gstr2B2BInvoicesItemss = getGSTR2B2BGSTR2B2BInvoices(StartDate,EndDate,num);
                        GSTR2B2BData btoData = new GSTR2B2BData(num,gstr2B2BInvoicesItemss);
                        b2bDataList.add(btoData);
                    } while (cursor.moveToNext()) ;
                }
            }
        }
        catch (Exception e) {
            b2bDataList = null;
        }
        return b2bDataList;
    }

    public ArrayList<GSTR2B2BInvoices> getGSTR2B2BGSTR2B2BInvoices(String StartDate, String EndDate, String gstn)
    {
        ArrayList<GSTR2B2BInvoices> gstr2B2BInvoicesList = new ArrayList<GSTR2B2BInvoices>();
        try {

            Cursor cursor = dbReport.getGSTR2B2BDetails(StartDate, EndDate,gstn);
            if (cursor == null)
            {
                gstr2B2BInvoicesList = null;
            }
            else
            {
                if (cursor.moveToFirst()) {
                    do {
                        String num = cursor.getString(cursor.getColumnIndex("GSTIN"));
                        String dt = cursor.getString(cursor.getColumnIndex("InvoiceDate"));
                        double val = cursor.getDouble(cursor.getColumnIndex("SubTotal"));
                        String pos = cursor.getString(cursor.getColumnIndex("POS"));
                        String InvoiceNo = cursor.getString(cursor.getColumnIndex("InvoiceNo"));
                        String rchrg = cursor.getString(cursor.getColumnIndex("AttractsReverseCharge"));
                        ArrayList<GSTR2B2BInvoiceItems> itemsList = getGSTR2G2BInvoiceItemsList(InvoiceNo);
                        GSTR2B2BInvoices b2BInvoices = new GSTR2B2BInvoices(
                                "*flag*",
                                "*chksum*",
                                num,
                                dt,
                                val,
                                pos,
                                rchrg.toUpperCase(),
                                itemsList
                                );
                        gstr2B2BInvoicesList.add(b2BInvoices);

                    } while (cursor.moveToNext()) ;
                }
            }
        }
        catch (Exception e) {
            gstr2B2BInvoicesList = null;
        }
        return gstr2B2BInvoicesList;
    }

    private ArrayList<GSTR2B2BInvoiceItems> getGSTR2G2BInvoiceItemsList(String num) {
        ArrayList<GSTR2B2BInvoiceItems> gstr2B2BInvoiceItemsList = new ArrayList<GSTR2B2BInvoiceItems>();
        try {
            Cursor cursor = dbReport.getGSTR2B2BItems(num);
            if (cursor == null)
            {
                gstr2B2BInvoiceItemsList = null;
            }
            else
            {
                int i = 0;
                if (cursor.moveToFirst()) {
                    do {
                        i++;
                        String ty = cursor.getString(cursor.getColumnIndex("SupplyType"));
                        String hsn_sc = cursor.getString(cursor.getColumnIndex("HSNCode"));
                        double txval = cursor.getDouble(cursor.getColumnIndex("TaxableValue"));
                        double irt = cursor.getDouble(cursor.getColumnIndex("IGSTRate"));
                        double iamt = cursor.getDouble(cursor.getColumnIndex("IGSTAmount"));
                        double crt = cursor.getDouble(cursor.getColumnIndex("CGSTRate"));
                        double camt = cursor.getDouble(cursor.getColumnIndex("CGSTAmount"));
                        double srt = cursor.getDouble(cursor.getColumnIndex("SGSTRate"));
                        double samt = cursor.getDouble(cursor.getColumnIndex("SGSTAmount"));
                        String elg = cursor.getString(cursor.getColumnIndex("ITC_Eligible"));
                        GSTR2B2BItemDetails gstr2B2BItemDetails = new GSTR2B2BItemDetails(
                                ty,
                                hsn_sc,
                                txval,
                                irt,
                                iamt,
                                crt,
                                camt,
                                srt,
                                samt,
                                elg
                        );

                        GSTR2B2BITCDetails gstr2B2BITCDetails = new GSTR2B2BITCDetails(
                                cursor.getDouble(cursor.getColumnIndex("IGSTAmount")),
                                cursor.getDouble(cursor.getColumnIndex("CGSTAmount")),
                                cursor.getDouble(cursor.getColumnIndex("SGSTAmount")),
                                0,
                                0,
                                0
                        );
                        GSTR2B2BInvoiceItems b2BInvoices = new GSTR2B2BInvoiceItems(i,"A",gstr2B2BITCDetails,gstr2B2BItemDetails);
                        gstr2B2BInvoiceItemsList.add(b2BInvoices);
                    } while (cursor.moveToNext()) ;
                }
            }
        }
        catch (Exception e) {
            gstr2B2BInvoiceItemsList = null;
        }
        return gstr2B2BInvoiceItemsList;
    }

    public ArrayList<GSTR1B2CSAData> getGSTR1B2CSAList(String startDate, String endDate) {
        ArrayList<GSTR1B2CSAData> list = new ArrayList<GSTR1B2CSAData>();
        try {
            Cursor cursor = dbReport.getGSTR2B2BAItems(startDate,endDate);
            if (cursor == null)
            {
                list = null;
            }
            else
            {
                int i = 0;
                if (cursor.moveToFirst()) {
                    do {
                        i++;
                        String date = cursor.getString(cursor.getColumnIndex("OriginalInvoiceDate"));
                        String str[] = date.split("-");
                        GSTR1B2CSAData gstr1B2CSAData = new GSTR1B2CSAData(
                                "*flag*",
                                "*chksum*",
                                cursor.getString(cursor.getColumnIndex("POS")),
                                str[2]+str[0],
                                cursor.getString(cursor.getColumnIndex("SupplyType")),
                                cursor.getString(cursor.getColumnIndex("HSNCode")),
                                cursor.getString(cursor.getColumnIndex("RevisedPOS")),
                                cursor.getString(cursor.getColumnIndex("RevisedSupplyType")),
                                cursor.getString(cursor.getColumnIndex("HSNCode")),
                                cursor.getDouble(cursor.getColumnIndex("TaxableValue")),
                                cursor.getDouble(cursor.getColumnIndex("IGSTRate")),
                                cursor.getDouble(cursor.getColumnIndex("IGSTAmount")),
                                cursor.getDouble(cursor.getColumnIndex("CGSTRate")),
                                cursor.getDouble(cursor.getColumnIndex("CGSTAmount")),
                                cursor.getDouble(cursor.getColumnIndex("SGSTRate")),
                                cursor.getDouble(cursor.getColumnIndex("SGSTAmount")),
                                cursor.getString(cursor.getColumnIndex("SupplyType"))
                                );
                        list.add(gstr1B2CSAData);
                    } while (cursor.moveToNext()) ;
                }
            }
        }
        catch (Exception e) {
            list = null;
        }
        return list;
    }

    public ArrayList<GSTR1CDN> getGSTR1CDNData(String startDate, String endDate) {
        ArrayList<GSTR1CDN> list = new ArrayList<GSTR1CDN>();
        try {
            Cursor cursor = dbReport.getGSTR2CDNGSTNs();
            if (cursor == null)
            {
                list = null;
            }
            else
            {
                if (cursor.moveToFirst()) {
                    do {
                        String num = cursor.getString(cursor.getColumnIndex("GSTIN"));
                        ArrayList<GSTR1CDNCDN> cdnList = getGSTR1CDNsList(startDate,endDate,num);
                        GSTR1CDN cdn = new GSTR1CDN(
                                num,
                                "b2cs",
                                cursor.getString(cursor.getColumnIndex("CustName")),
                                cdnList
                                );
                        list.add(cdn);
                    } while (cursor.moveToNext()) ;
                }
            }
        }
        catch (Exception e) {
            list = null;
        }
        return list;
    }

    private ArrayList<GSTR1CDNCDN> getGSTR1CDNsList(String startDate, String endDate, String num) {
        ArrayList<GSTR1CDNCDN> list = new ArrayList<GSTR1CDNCDN>();
        try {

            Cursor cursor = dbReport.getGSTR1GSTR1CDNCDN(startDate, endDate,num);
            if (cursor == null)
            {
                list = null;
            }
            else
            {
                if (cursor.moveToFirst()) {
                    do {
                        //String num = cursor.getString(cursor.getColumnIndex("GSTIN"));
                        GSTR1CDNCDN cdncdn =  new GSTR1CDNCDN(
                                cursor.getString(cursor.getColumnIndex("NoteType")),
                                cursor.getDouble(cursor.getColumnIndex("NoteNo")),
                                cursor.getString(cursor.getColumnIndex("NoteDate")),
                                "*rsn*",
                                cursor.getString(cursor.getColumnIndex("OriginalInvoiceNo")),
                                cursor.getString(cursor.getColumnIndex("OriginalInvoiceDate")),
                                cursor.getDouble(cursor.getColumnIndex("DifferentialValue")),
                                cursor.getDouble(cursor.getColumnIndex("IGSTRate")),
                                cursor.getDouble(cursor.getColumnIndex("IGSTAmount")),
                                cursor.getDouble(cursor.getColumnIndex("CGSTRate")),
                                cursor.getDouble(cursor.getColumnIndex("CGSTAmount")),
                                cursor.getDouble(cursor.getColumnIndex("SGSTRate")),
                                cursor.getDouble(cursor.getColumnIndex("SGSTAmount"))
                        );
                        list.add(cdncdn);
                    } while (cursor.moveToNext()) ;
                }
            }
        }
        catch (Exception e) {
            list = null;
        }
        return list;
    }

    public ArrayList<GSTR2B2BAData> getGSTR2B2BSaveData() {
        return null;
    }

    public ArrayList<GSTR2CDN> getGSTR2CDNSaveData() {
        return null;
    }
}

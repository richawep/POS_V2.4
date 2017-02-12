package com.wep.common.app.gst;

import java.util.ArrayList;

/**
 * Created by PriyabratP on 18-11-2016.
 */

public class GSTR2B2BData {

    private String ctin;
    private ArrayList<GSTR2B2BInvoices> inv;

    public GSTR2B2BData(){}

    public GSTR2B2BData(String ctin, ArrayList<GSTR2B2BInvoices> inv) {
        this.ctin = ctin;
        this.inv = inv;
    }

    public String getCtin() {
        return ctin;
    }

    public void setCtin(String ctin) {
        this.ctin = ctin;
    }

    public ArrayList<GSTR2B2BInvoices> getGstr2Invoices() {
        return inv;
    }

    public void setGstr2Invoices(ArrayList<GSTR2B2BInvoices> gstr2Invoices) {
        this.inv = gstr2Invoices;
    }
}

package com.wep.common.app.gst;

/**
 * Created by PriyabratP on 18-11-2016.
 */

public class GSTR2B2BAInvoiceItems {

    private int lineno; //Serial no
    private String status;//Status of invoice
    private GSTR2B2BAITCDetails itc;//itc Details
    private GSTR2B2BAItemDetails itm_det;//Item Details

    public GSTR2B2BAInvoiceItems() {
    }

    public GSTR2B2BAInvoiceItems(int lineno, String status, GSTR2B2BAITCDetails itc, GSTR2B2BAItemDetails itm_det) {
        this.lineno = lineno;
        this.status = status;
        this.itc = itc;
        this.itm_det = itm_det;
    }

    public int getLineno() {
        return lineno;
    }

    public void setLineno(int lineno) {
        this.lineno = lineno;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public GSTR2B2BAITCDetails getItcDetails() {
        return itc;
    }

    public void setItcDetails(GSTR2B2BAITCDetails itc) {
        this.itc = itc;
    }

    public GSTR2B2BAItemDetails getItemDetails() {
        return itm_det;
    }

    public void setItemDetails(GSTR2B2BAItemDetails itemDetails) {
        this.itm_det = itemDetails;
    }
}

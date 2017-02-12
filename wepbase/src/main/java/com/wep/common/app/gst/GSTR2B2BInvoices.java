package com.wep.common.app.gst;

import java.util.ArrayList;

/**
 * Created by PriyabratP on 18-11-2016.
 */

public class GSTR2B2BInvoices {

    private String flag; //Invoice type
    private String chksum;//Invoice Check sum value
    private String num;
    private String  dt;
    private double val;
    private String pos;
    private String rchrg;
    private ArrayList<GSTR2B2BInvoiceItems> itms;

    public GSTR2B2BInvoices(){
    }

    public GSTR2B2BInvoices(String flag, String chksum, String num, String dt, double val, String pos, String rchrg, ArrayList<GSTR2B2BInvoiceItems> itms) {
        this.flag = flag;
        this.chksum = chksum;
        this.num = num;
        this.dt = dt;
        this.val = val;
        this.pos = pos;
        this.rchrg = rchrg;
        this.itms = itms;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getChksum() {
        return chksum;
    }

    public void setChksum(String chksum) {
        this.chksum = chksum;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getRchrg() {
        return rchrg;
    }

    public void setRchrg(String rchrg) {
        this.rchrg = rchrg;
    }

    public ArrayList<GSTR2B2BInvoiceItems> getInvoiceItems() {
        return itms;
    }

    public void setInvoiceItems(ArrayList<GSTR2B2BInvoiceItems> itms) {
        this.itms = itms;
    }
}

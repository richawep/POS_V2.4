package com.wep.common.app.gst;

import java.util.ArrayList;

/**
 * Created by PriyabratP on 18-11-2016.
 */

public class GSTR2B2BAInvoices {

    private String flag; //Invoice type
    private String chksum;//Invoice Check sum value
    private String num;
    private String dt;
    private double val;
    private String pos;
    private ArrayList<GSTR2B2BAInvoiceItems> itms;
    private String onum;
    private String odt;

    public GSTR2B2BAInvoices() {
    }

    public GSTR2B2BAInvoices(String flag, String chksum, String num, String dt, double val, String pos, ArrayList<GSTR2B2BAInvoiceItems> itms, String onum, String odt) {
        this.flag = flag;
        this.chksum = chksum;
        this.num = num;
        this.dt = dt;
        this.val = val;
        this.pos = pos;
        this.itms = itms;
        this.onum = onum;
        this.odt = odt;
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

    public ArrayList<GSTR2B2BAInvoiceItems> getInvoiceItems() {
        return itms;
    }

    public void setInvoiceItems(ArrayList<GSTR2B2BAInvoiceItems> itms) {
        this.itms = itms;
    }

    public ArrayList<GSTR2B2BAInvoiceItems> getItms() {
        return itms;
    }

    public void setItms(ArrayList<GSTR2B2BAInvoiceItems> itms) {
        this.itms = itms;
    }

    public String getOnum() {
        return onum;
    }

    public void setOnum(String onum) {
        this.onum = onum;
    }

    public String getOdt() {
        return odt;
    }

    public void setOdt(String odt) {
        this.odt = odt;
    }
}

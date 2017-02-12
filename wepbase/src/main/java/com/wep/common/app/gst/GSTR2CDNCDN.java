package com.wep.common.app.gst;

import java.util.ArrayList;

/**
 * Created by PriyabratP on 01-12-2016.
 */
public class GSTR2CDNCDN {

    private String flag;
    private String chksum;
    private String ty;
    private double nt_num;
    private String nt_dt;
    private String i_num;
    private String i_dt;
    private String rsn;
    private double val;
    private double irt;
    private double iamt;
    private double crt;
    private double camt;
    private double srt;
    private double samt;
    private String elg;
    private ArrayList<GSTR2B2BITCDetails> itc;

    public GSTR2CDNCDN(){}

    public GSTR2CDNCDN(String flag, String chksum, String ty, double nt_num, String nt_dt, String i_num, String i_dt, String rsn, double val, double irt, double iamt, double crt, double camt, double srt, double samt, String elg, ArrayList<GSTR2B2BITCDetails> itc) {
        this.flag = flag;
        this.chksum = chksum;
        this.ty = ty;
        this.nt_num = nt_num;
        this.nt_dt = nt_dt;
        this.i_num = i_num;
        this.i_dt = i_dt;
        this.rsn = rsn;
        this.val = val;
        this.irt = irt;
        this.iamt = iamt;
        this.crt = crt;
        this.camt = camt;
        this.srt = srt;
        this.samt = samt;
        this.elg = elg;
        this.itc = itc;
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

    public String getTy() {
        return ty;
    }

    public void setTy(String ty) {
        this.ty = ty;
    }

    public double getNt_num() {
        return nt_num;
    }

    public void setNt_num(double nt_num) {
        this.nt_num = nt_num;
    }

    public String getNt_dt() {
        return nt_dt;
    }

    public void setNt_dt(String nt_dt) {
        this.nt_dt = nt_dt;
    }

    public String getI_num() {
        return i_num;
    }

    public void setI_num(String i_num) {
        this.i_num = i_num;
    }

    public String getI_dt() {
        return i_dt;
    }

    public void setI_dt(String i_dt) {
        this.i_dt = i_dt;
    }

    public String getRsn() {
        return rsn;
    }

    public void setRsn(String rsn) {
        this.rsn = rsn;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }

    public double getIrt() {
        return irt;
    }

    public void setIrt(double irt) {
        this.irt = irt;
    }

    public double getIamt() {
        return iamt;
    }

    public void setIamt(double iamt) {
        this.iamt = iamt;
    }

    public double getCrt() {
        return crt;
    }

    public void setCrt(double crt) {
        this.crt = crt;
    }

    public double getCamt() {
        return camt;
    }

    public void setCamt(double camt) {
        this.camt = camt;
    }

    public double getSrt() {
        return srt;
    }

    public void setSrt(double srt) {
        this.srt = srt;
    }

    public double getSamt() {
        return samt;
    }

    public void setSamt(double samt) {
        this.samt = samt;
    }

    public String getElg() {
        return elg;
    }

    public void setElg(String elg) {
        this.elg = elg;
    }

    public ArrayList<GSTR2B2BITCDetails> getItc() {
        return itc;
    }

    public void setItc(ArrayList<GSTR2B2BITCDetails> itc) {
        this.itc = itc;
    }
}

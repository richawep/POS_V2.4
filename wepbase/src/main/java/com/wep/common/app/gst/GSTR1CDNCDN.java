package com.wep.common.app.gst;

/**
 * Created by PriyabratP on 01-12-2016.
 */
public class GSTR1CDNCDN {
    private int sno;
    private String ntty;
    private double nt_num;
    private String nt_dt;
    private String rsn;
    private String inum;// invoice no
    private String idt;
    private double val;
    private double irt;
    private double iamt;
    private double crt;
    private double camt;
    private double srt;
    private double samt;

    public GSTR1CDNCDN() {
        this.ntty = "";
        this.nt_num = 0.00;
        this.nt_dt = "";
        this.rsn = "";
        this.inum = "";
        this.idt = "";
        this.val = 0.00;
        this.irt = 0.00;
        this.iamt = 0.00;
        this.crt = 0.00;
        this.camt = 0.00;
        this.srt =0.00;
        this.samt = 0.00;
        this.sno = 0;
    }

    public GSTR1CDNCDN(String ntty, double nt_num, String nt_dt, String rsn, String inum, String idt, double val, double irt, double iamt, double crt, double camt, double srt, double samt) {
        this.ntty = ntty;
        this.nt_num = nt_num;
        this.nt_dt = nt_dt;
        this.rsn = rsn;
        this.inum = inum;
        this.idt = idt;
        this.val = val;
        this.irt = irt;
        this.iamt = iamt;
        this.crt = crt;
        this.camt = camt;
        this.srt = srt;
        this.samt = samt;
        this.sno = 0;
    }

    public int getSno() {
        return sno;
    }

    public void setSno(int sno) {
        this.sno = sno;
    }

    public String getNtty() {
        return ntty;
    }

    public void setNtty(String ntty) {
        this.ntty = ntty;
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

    public String getRsn() {
        return rsn;
    }

    public void setRsn(String rsn) {
        this.rsn = rsn;
    }

    public String getInum() {
        return inum;
    }

    public void setInum(String inum) {
        this.inum = inum;
    }

    public String getIdt() {
        return idt;
    }

    public void setIdt(String idt) {
        this.idt = idt;
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
}

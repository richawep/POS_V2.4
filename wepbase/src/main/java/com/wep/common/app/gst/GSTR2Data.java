package com.wep.common.app.gst;

import java.util.ArrayList;

/**
 * Created by PriyabratP on 18-11-2016.
 */

public class GSTR2Data {

    private String gstin; //GSTIN of the Tax Payer
    private String fp; //Financial period
    private double gt; //Gross Turn Over
    private double ttl; // Total Tax Liability
    private ArrayList<GSTR2_B2B_Data_registered> b2b; //B2B Invoice Data
    private ArrayList<GSTR2_B2B_Data_registered> b2ba;
    private ArrayList<GSTR2CDN> cdn;

    public GSTR2Data() {
    }

    public GSTR2Data(String gstin, String fp, double gt, double ttl, ArrayList<GSTR2_B2B_Data_registered> b2b, ArrayList<GSTR2_B2B_Data_registered> b2ba, ArrayList<GSTR2CDN> cdn) {
        this.gstin = gstin;
        this.fp = fp;
        this.gt = gt;
        this.ttl = ttl;
        this.b2b = b2b;
        this.b2ba = b2ba;
        this.cdn = cdn;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getFp() {
        return fp;
    }

    public void setFp(String fp) {
        this.fp = fp;
    }

    public double getGt() {
        return gt;
    }

    public void setGt(double gt) {
        this.gt = gt;
    }

    public double getTtl() {
        return ttl;
    }

    public void setTtl(double ttl) {
        this.ttl = ttl;
    }

    public ArrayList<GSTR2_B2B_Data_registered> getB2b() {
        return b2b;
    }

    public void setB2b(ArrayList<GSTR2_B2B_Data_registered> b2b) {
        this.b2b = b2b;
    }

    public ArrayList<GSTR2_B2B_Data_registered> getB2ba() {
        return b2ba;
    }

    public void setB2ba(ArrayList<GSTR2_B2B_Data_registered> b2ba) {
        this.b2ba = b2ba;
    }

    public ArrayList<GSTR2CDN> getCdn() {
        return cdn;
    }

    public void setCdn(ArrayList<GSTR2CDN> cdn) {
        this.cdn = cdn;
    }
}

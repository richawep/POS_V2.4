package com.wep.common.app.gst;

import java.util.ArrayList;

/**
 * Created by PriyabratP on 01-12-2016.
 */
public class GSTR1CDN {

    private String cgstin;
    private String typ;
    private String cname;
    private ArrayList<GSTR1CDNCDN> cdn;

    public GSTR1CDN() {
    }

    public GSTR1CDN(String cgstin, String typ, String cname, ArrayList<GSTR1CDNCDN> cdn) {
        this.cgstin = cgstin;
        this.typ = typ;
        this.cname = cname;
        this.cdn = cdn;
    }

    public String getCgstin() {
        return cgstin;
    }

    public void setCgstin(String cgstin) {
        this.cgstin = cgstin;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public ArrayList<GSTR1CDNCDN> getCdn() {
        return cdn;
    }

    public void setCdn(ArrayList<GSTR1CDNCDN> cdn) {
        this.cdn = cdn;
    }
}

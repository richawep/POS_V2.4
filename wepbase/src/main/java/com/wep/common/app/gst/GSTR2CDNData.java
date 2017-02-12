package com.wep.common.app.gst;

import java.util.ArrayList;

/**
 * Created by PriyabratP on 01-12-2016.
 */
public class GSTR2CDNData {

    private String cgstin;
    private String typ;
    private String cname;
    private ArrayList<GSTR2CDNCDN> cdn;

    public GSTR2CDNData() {
    }

    public GSTR2CDNData(String cgstin, String typ, String cname, ArrayList<GSTR2CDNCDN> cdn) {
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

    public ArrayList<GSTR2CDNCDN> getCdn() {
        return cdn;
    }

    public void setCdn(ArrayList<GSTR2CDNCDN> cdn) {
        this.cdn = cdn;
    }
}

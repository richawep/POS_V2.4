package com.wep.common.app.gst;

import java.util.ArrayList;

/**
 * Created by PriyabratP on 01-12-2016.
 */

public class GSTR2CDN {

    private ArrayList<GSTR2CDNData> cdn_data;

    public GSTR2CDN(){}

    public GSTR2CDN(ArrayList<GSTR2CDNData> cdn_data) {
        this.cdn_data = cdn_data;
    }

    public ArrayList<GSTR2CDNData> getCdn_data() {
        return cdn_data;
    }

    public void setCdn_data(ArrayList<GSTR2CDNData> cdn_data) {
        this.cdn_data = cdn_data;
    }
}

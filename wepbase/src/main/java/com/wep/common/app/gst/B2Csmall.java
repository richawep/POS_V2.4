package com.wep.common.app.gst;

import java.util.Comparator;

/**
 * Created by welcome on 21-11-2016.
 */

public class B2Csmall  {

    private String SupplyType;
    private String HSNCode;
    private String PlaceOfSupply, Description,stateCode;
    private float TaxableValue , SubTotal ;
    private float IGSTRate, IGSTAmt, CGSTRate, CGSTAmt, SGSTRate, SGSTAmt;
    private String ProAss;
    private float NilRatedValue, ExemptedValue, NonGSTValue, CompoundingValue,unregisteredValue;
    private String cessRate ="0";
    private String cessAmt  ="0";
    private String Orderno="0";
    private String OrderDate="0";
    private String etin="";
    private String etype="";


    public B2Csmall() {
        this.Description="";
        this.SupplyType ="";
        this.HSNCode="";
        this.PlaceOfSupply="";
        this.TaxableValue=0;
        this.IGSTRate=0;
        this.IGSTAmt=0;
        this.CGSTRate=0;
        this.CGSTAmt=0;
        this.SGSTRate=0;
        this.SGSTAmt=0;
        this.ProAss="";
        this.NilRatedValue=0;
        this.ExemptedValue=0;
        this.NonGSTValue=0;
        this.CompoundingValue=0;
        this.unregisteredValue=0;
        this.SubTotal=0;
        cessRate ="0";
        cessAmt  ="0";
        Orderno="0";
        OrderDate="0";
        etin="";
        etype="";
        stateCode="";
    }

    public B2Csmall(String supplyType, String HSNCode, String placeOfSupply, String description, float taxableValue, float subTotal, float IGSTRate, float IGSTAmt, float CGSTRate, float CGSTAmt, float SGSTRate, float SGSTAmt, String proAss, float nilRatedValue, float exemptedValue, float nonGSTValue, float compoundingValue, float unregisteredValue, String cessRate, String cessAmt, String orderno, String orderDate, String etin, String etype) {
        SupplyType = supplyType;
        this.HSNCode = HSNCode;
        stateCode = stateCode;
        Description = description;
        TaxableValue = taxableValue;
        SubTotal = subTotal;
        this.IGSTRate = IGSTRate;
        this.IGSTAmt = IGSTAmt;
        this.CGSTRate = CGSTRate;
        this.CGSTAmt = CGSTAmt;
        this.SGSTRate = SGSTRate;
        this.SGSTAmt = SGSTAmt;
        ProAss = proAss;
        NilRatedValue = nilRatedValue;
        ExemptedValue = exemptedValue;
        NonGSTValue = nonGSTValue;
        CompoundingValue = compoundingValue;
        this.unregisteredValue = unregisteredValue;
        this.cessRate = cessRate;
        this.cessAmt = cessAmt;
        Orderno = orderno;
        OrderDate = orderDate;
        this.etin = etin;
        this.etype = etype;
        //this.stateCode = stateCode;
    }

    /*public B2Csmall(String supplyType, String HSNCode, String placeOfSupply, float TaxableValue, float IGSTRate, float IGSTAmt, float CGSTRate, float CGSTAmt, float SGSTRate, float SGSTAmt, String proAss, float nilRatedValue, float exemptedValue, float nonGSTValue, float compoundingValue, float unregisteredValue) {
            SupplyType = supplyType;
            this.HSNCode = HSNCode;
            PlaceOfSupply = placeOfSupply;
            this.TaxableValue = TaxableValue;
            this.IGSTRate = IGSTRate;
            this.IGSTAmt = IGSTAmt;
            this.CGSTRate = CGSTRate;
            this.CGSTAmt = CGSTAmt;
            this.SGSTRate = SGSTRate;
            this.SGSTAmt = SGSTAmt;
            ProAss = proAss;
            NilRatedValue = nilRatedValue;
            ExemptedValue = exemptedValue;
            NonGSTValue = nonGSTValue;
            CompoundingValue = compoundingValue;
            this.unregisteredValue = unregisteredValue;
        }
    */

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getCessRate() {
        return cessRate;
    }

    public void setCessRate(String cessRate) {
        this.cessRate = cessRate;
    }

    public String getCessAmt() {
        return cessAmt;
    }

    public void setCessAmt(String cessAmt) {
        this.cessAmt = cessAmt;
    }

    public String getOrderno() {
        return Orderno;
    }

    public void setOrderno(String orderno) {
        Orderno = orderno;
    }

    public String getOrderDate() {
        return OrderDate;
    }

    public void setOrderDate(String orderDate) {
        OrderDate = orderDate;
    }

    public String getEtin() {
        return etin;
    }

    public void setEtin(String etin) {
        this.etin = etin;
    }

    public String getEtype() {
        return etype;
    }

    public void setEtype(String etype) {
        this.etype = etype;
    }

    public float getSubTotal() {
        return SubTotal;
    }

    public void setSubTotal(float subTotal) {
        SubTotal = subTotal;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getSupplyType() {
        return SupplyType;
    }

    public void setSupplyType(String supplyType) {
        SupplyType = supplyType;
    }

    public String getHSNCode() {
        return HSNCode;
    }

    public void setHSNCode(String HSNCode) {
        this.HSNCode = HSNCode;
    }

    public String getPlaceOfSupply() {
        return PlaceOfSupply;
    }

    public void setPlaceOfSupply(String placeOfSupply) {
        PlaceOfSupply = placeOfSupply;
    }

    public float getTaxableValue() {
        return TaxableValue;
    }

    public void setTaxableValue(float TaxableValue) {
        this.TaxableValue = TaxableValue;
    }

    public float getIGSTRate() {
        return IGSTRate;
    }

    public void setIGSTRate(float IGSTRate) {
        this.IGSTRate = IGSTRate;
    }

    public float getIGSTAmt() {
        return IGSTAmt;
    }

    public void setIGSTAmt(float IGSTAmt) {
        this.IGSTAmt = IGSTAmt;
    }

    public float getCGSTRate() {
        return CGSTRate;
    }

    public void setCGSTRate(float CGSTRate) {
        this.CGSTRate = CGSTRate;
    }

    public float getCGSTAmt() {
        return CGSTAmt;
    }

    public void setCGSTAmt(float CGSTAmt) {
        this.CGSTAmt = CGSTAmt;
    }

    public float getSGSTRate() {
        return SGSTRate;
    }

    public void setSGSTRate(float SGSTRate) {
        this.SGSTRate = SGSTRate;
    }

    public float getSGSTAmt() {
        return SGSTAmt;
    }

    public void setSGSTAmt(float SGSTAmt) {
        this.SGSTAmt = SGSTAmt;
    }

    public String getProAss() {
        return ProAss;
    }

    public void setProAss(String proAss) {
        ProAss = proAss;
    }

    public float getNilRatedValue() {
        return NilRatedValue;
    }

    public void setNilRatedValue(float nilRatedValue) {
        NilRatedValue = nilRatedValue;
    }

    public float getExemptedValue() {
        return ExemptedValue;
    }

    public void setExemptedValue(float exemptedValue) {
        ExemptedValue = exemptedValue;
    }

    public float getNonGSTValue() {
        return NonGSTValue;
    }

    public void setNonGSTValue(float nonGSTValue) {
        NonGSTValue = nonGSTValue;
    }

    public float getCompoundingValue() {
        return CompoundingValue;
    }

    public void setCompoundingValue(float compoundingValue) {
        CompoundingValue = compoundingValue;
    }

    public float getUnregisteredValue() {
        return unregisteredValue;
    }

    public void setUnregisteredValue(float unregisteredValue) {
        this.unregisteredValue = unregisteredValue;
    }

    public static Comparator<B2Csmall>  HSNComparator = new Comparator<B2Csmall>() {
        @Override
        public int compare(B2Csmall o1, B2Csmall o2) {

            String B2Csmall1 = o1.getHSNCode();
            String B2Csmall2 = o2.getHSNCode();

            //ascending order
            return B2Csmall1.compareTo(B2Csmall2);
        }
    };

}

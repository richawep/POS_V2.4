package com.wep.common.app.gst;

/**
 * Created by welcome on 21-11-2016.
 */

public class B2Csmall  {

    private String SupplyType;
    private String HSNCode;
    private String PlaceOfSupply, Description;
    private float TaxableValue , SubTotal ;
    private float IGSTRate, IGSTAmt, CGSTRate, CGSTAmt, SGSTRate, SGSTAmt;
    private String ProAss;
    private float NilRatedValue, ExemptedValue, NonGSTValue, CompoundingValue,unregisteredValue;

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
    }

    public B2Csmall(String supplyType, String HSNCode, String placeOfSupply, float TaxableValue, float IGSTRate, float IGSTAmt, float CGSTRate, float CGSTAmt, float SGSTRate, float SGSTAmt, String proAss, float nilRatedValue, float exemptedValue, float nonGSTValue, float compoundingValue, float unregisteredValue) {
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
}

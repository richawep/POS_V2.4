package com.wep.common.app.models;

/**
 * Created by RichaA on 2/13/2017.
 */

public class ItemOutward {


    private int menuCode;
    private String LongName;
    private float DineIn1;
    private float DineIn2;
    private float DineIn3;
    private float Stock;
    private int DeptCode;
    private int CategCode;
    private int KitchenCode;
    private String BarCode;
    private String ImageUri;
    private int ItemId;
    private float SalesTaxPercent;
    private float ServiceTaxPercent;
    private float IGSTRate;
    private String UOM;
    private String HSN;
    private String TaxationType;

    public ItemOutward() {

        this.menuCode = 0;
        LongName = "";
        DineIn1 = 0;
        DineIn2 = 0;
        DineIn3 = 0;
        Stock = 0;
        DeptCode = 0;
        CategCode = 0;
        KitchenCode = 0;
        BarCode = "";
        ImageUri = "";
        ItemId = 0;
        SalesTaxPercent = 0;
        ServiceTaxPercent = 0;
        IGSTRate = 0;
        this.UOM = "";
        this.HSN = "";
        this.TaxationType="GST";
    }

    public ItemOutward(int menuCode, String longName, float dineIn1, float dineIn2, float dineIn3, float stock,
                       int deptCode, int categCode, int kitchenCode, String barCode, String imageUri, int itemId, float salesTaxPercent,
                       float serviceTaxPercent, String UOM, String HSN, String taxationType, float IGSTRate) {
        this.menuCode = menuCode;
        LongName = longName;
        DineIn1 = dineIn1;
        DineIn2 = dineIn2;
        DineIn3 = dineIn3;
        Stock = stock;
        DeptCode = deptCode;
        CategCode = categCode;
        KitchenCode = kitchenCode;
        BarCode = barCode;
        ImageUri = imageUri;
        ItemId = itemId;
        SalesTaxPercent = salesTaxPercent;
        ServiceTaxPercent = serviceTaxPercent;
        this.IGSTRate = IGSTRate;
        this.UOM = UOM;
        this.HSN = HSN;
        TaxationType = taxationType;
    }

    public String getTaxationType() {
        return TaxationType;
    }

    public void setTaxationType(String taxationType) {
        TaxationType = taxationType;
    }

    public String getHSN() {
        return HSN;
    }

    public void setHSN(String HSN) {
        this.HSN = HSN;
    }

    public int getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(int menuCode) {
        this.menuCode = menuCode;
    }

    public String getLongName() {
        return LongName;
    }

    public void setLongName(String longName) {
        LongName = longName;
    }

    public float getDineIn1() {
        return DineIn1;
    }

    public void setDineIn1(float dineIn1) {
        DineIn1 = dineIn1;
    }

    public float getDineIn2() {
        return DineIn2;
    }

    public void setDineIn2(float dineIn2) {
        DineIn2 = dineIn2;
    }

    public float getDineIn3() {
        return DineIn3;
    }

    public void setDineIn3(float dineIn3) {
        DineIn3 = dineIn3;
    }

    public float getStock() {
        return Stock;
    }

    public void setStock(float stock) {
        Stock = stock;
    }

    public int getDeptCode() {
        return DeptCode;
    }

    public void setDeptCode(int deptCode) {
        DeptCode = deptCode;
    }

    public int getCategCode() {
        return CategCode;
    }

    public void setCategCode(int categCode) {
        CategCode = categCode;
    }

    public int getKitchenCode() {
        return KitchenCode;
    }

    public void setKitchenCode(int kitchenCode) {
        KitchenCode = kitchenCode;
    }

    public String getBarCode() {
        return BarCode;
    }

    public void setBarCode(String barCode) {
        BarCode = barCode;
    }

    public String getImageUri() {
        return ImageUri;
    }

    public void setImageUri(String imageUri) {
        ImageUri = imageUri;
    }

    public int getItemId() {
        return ItemId;
    }

    public void setItemId(int itemId) {
        ItemId = itemId;
    }

    public float getSalesTaxPercent() {
        return SalesTaxPercent;
    }

    public void setSalesTaxPercent(float salesTaxPercent) {
        SalesTaxPercent = salesTaxPercent;
    }

    public float getServiceTaxPercent() {
        return ServiceTaxPercent;
    }

    public void setServiceTaxPercent(float serviceTaxPercent) {
        ServiceTaxPercent = serviceTaxPercent;
    }

    public float getIGSTRate() {
        return IGSTRate;
    }

    public void setIGSTRate(float IGSTRate) {
        this.IGSTRate = IGSTRate;
    }

    public String getUOM() {
        return UOM;
    }

    public void setUOM(String UOM) {
        this.UOM = UOM;
    }

}












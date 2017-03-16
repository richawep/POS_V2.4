package com.wep.common.app.Database;

/**
 * Created by RichaA on 3/15/2017.
 */

public class Supplier_Model {
    // Private variables
    String SupplierName;
    int SupplierCode;
    int SupplierPhone;
    String SupplierAddress;

    public Supplier_Model() {
        SupplierName = "";
        SupplierAddress="";
        SupplierCode=-1;
        SupplierPhone = 0;
    }

    public String getSupplierName() {
        return SupplierName;
    }

    public void setSupplierName(String supplierName) {
        SupplierName = supplierName;
    }

    public int getSupplierCode() {
        return SupplierCode;
    }

    public void setSupplierCode(int supplierCode) {
        SupplierCode = supplierCode;
    }

    public int getSupplierPhone() {
        return SupplierPhone;
    }

    public void setSupplierPhone(int supplierPhone) {
        SupplierPhone = supplierPhone;
    }

    public String getSupplierAddress() {
        return SupplierAddress;
    }

    public void setSupplierAddress(String supplierAddress) {
        SupplierAddress = supplierAddress;
    }
}

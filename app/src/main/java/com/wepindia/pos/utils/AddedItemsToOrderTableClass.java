package com.wepindia.pos.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by RichaA on 5/18/2017.
 */

public class AddedItemsToOrderTableClass implements Parcelable {

    int menuCode;
    String itemName;
    double quantity;
    double rate;
    double igstRate;
    double igstAmt;
    double cgstRate;
    double cgstAmt;
    double sgstRate;
    double sgstAmt;
    double subtotal;
    double billamount;

    public AddedItemsToOrderTableClass(int menuCode, String itemName, double quantity, double rate, double igstRate, double igstAmt, double cgstRate, double cgstAmt, double sgstRate, double sgstAmt, double subtotal, double billamount) {
        this.menuCode = menuCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.rate = rate;
        this.igstRate = igstRate;
        this.igstAmt = igstAmt;
        this.cgstRate = cgstRate;
        this.cgstAmt = cgstAmt;
        this.sgstRate = sgstRate;
        this.sgstAmt = sgstAmt;
        this.subtotal = subtotal;
        this.billamount = billamount;
    }

    public AddedItemsToOrderTableClass() {
        this.menuCode = 0;
        this.itemName = "";
        this.quantity = 0.00;
        this.rate = 0.00;
        this.igstRate = 0.00;
        this.igstAmt = 0.00;
        this.cgstRate = 0.00;
        this.cgstAmt = 0.00;
        this.sgstRate = 0.00;
        this.sgstAmt = 0.00;
        this.subtotal = 0.00;
        this.billamount = 0.00;
    }
    /**
     * Storing the Student data to Parcel object
     **/
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(menuCode);
        dest.writeString(itemName);
        dest.writeDouble(quantity);
        dest.writeDouble(rate);
        dest.writeDouble(igstRate);
        dest.writeDouble(igstAmt);
        dest.writeDouble(cgstRate);
        dest.writeDouble(cgstAmt);
        dest.writeDouble(sgstRate);
        dest.writeDouble(sgstAmt);
        dest.writeDouble(subtotal);
        dest.writeDouble(billamount);
    }
    public AddedItemsToOrderTableClass(Parcel in) {
        menuCode = in.readInt();
        itemName = in.readString();
        quantity = in.readDouble();
        rate = in.readDouble();
        igstRate = in.readDouble();
        igstAmt = in.readDouble();
        cgstRate = in.readDouble();
        cgstAmt = in.readDouble();
        sgstRate = in.readDouble();
        sgstAmt = in.readDouble();
        subtotal = in.readDouble();
        billamount = in.readDouble();
    }


    public static final Parcelable.Creator<AddedItemsToOrderTableClass> CREATOR = new Parcelable.Creator<AddedItemsToOrderTableClass>()
    {
        @Override
        public AddedItemsToOrderTableClass createFromParcel(Parcel in) {
            return new AddedItemsToOrderTableClass(in);
        }

        @Override
        public AddedItemsToOrderTableClass[] newArray(int size) {
            return new AddedItemsToOrderTableClass[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public int getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(int menuCode) {
        this.menuCode = menuCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getIgstRate() {
        return igstRate;
    }

    public void setIgstRate(double igstRate) {
        this.igstRate = igstRate;
    }

    public double getIgstAmt() {
        return igstAmt;
    }

    public void setIgstAmt(double igstAmt) {
        this.igstAmt = igstAmt;
    }

    public double getCgstRate() {
        return cgstRate;
    }

    public void setCgstRate(double cgstRate) {
        this.cgstRate = cgstRate;
    }

    public double getCgstAmt() {
        return cgstAmt;
    }

    public void setCgstAmt(double cgstAmt) {
        this.cgstAmt = cgstAmt;
    }

    public double getSgstRate() {
        return sgstRate;
    }

    public void setSgstRate(double sgstRate) {
        this.sgstRate = sgstRate;
    }

    public double getSgstAmt() {
        return sgstAmt;
    }

    public void setSgstAmt(double sgstAmt) {
        this.sgstAmt = sgstAmt;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getBillamount() {
        return billamount;
    }

    public void setBillamount(double billamount) {
        this.billamount = billamount;
    }
}

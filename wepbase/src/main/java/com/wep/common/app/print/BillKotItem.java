package com.wep.common.app.print;

import java.io.Serializable;

/**
 * Created by PriyabratP on 06-09-2016.
 */
public class BillKotItem implements Serializable {
    private int itemId;
    private String itemName;
    private double qty;
    private double rate;
    private double amount;

    public BillKotItem() {
    }

    public BillKotItem(String itemName, double qty, double rate, double amount) {
        this.itemName = itemName;
        this.qty = qty;
        this.rate = rate;
        this.amount = amount;
    }

    public BillKotItem(int itemId, String itemName, double qty, double rate, double amount) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.qty = qty;
        this.rate = rate;
        this.amount = amount;

    }



    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}

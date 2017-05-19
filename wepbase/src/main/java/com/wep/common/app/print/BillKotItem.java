package com.wep.common.app.print;

import java.io.Serializable;

/**
 * Created by PriyabratP on 06-09-2016.
 */
public class BillKotItem implements Serializable {
    private int itemId;
    private String itemName;
    private int qty;
    private double qty1;
    private double rate;
    private double amount;

    public BillKotItem() {
    }

    public BillKotItem(String itemName, int qty, double rate, double amount) {
        this.itemName = itemName;
        this.qty = qty;
        this.rate = rate;
        this.amount = amount;
    }

    public BillKotItem(int itemId, String itemName, int qty, double rate, double amount) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.qty = qty;
        this.rate = rate;
        this.amount = amount;
        this.qty1 = 0.00;
    }

    public BillKotItem(int itemId, String itemName, double qty, double rate, double amount) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.qty1 = qty;
        this.rate = rate;
        this.amount = amount;
    }

    public double getQty1() {
        return qty1;
    }

    public void setQty1(double qty1) {
        this.qty1 = qty1;
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

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
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

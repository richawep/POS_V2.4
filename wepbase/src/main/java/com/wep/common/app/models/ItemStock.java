package com.wep.common.app.models;

/**
 * Created by RichaA on 3/9/2017.
 */

public class ItemStock {

    private  int MenuCode;
    private  String ItemName;
    private  double OpeningStock;
    private  double ClosingStock;
    private  double Rate;

    public ItemStock(int menuCode, String itemName, double openingStock, double closingStock, double rate) {
        MenuCode = menuCode;
        ItemName = itemName;
        OpeningStock = openingStock;
        ClosingStock = closingStock;
        Rate = rate;
    }

    public ItemStock() {
        MenuCode = 0;
        ItemName = "";
        OpeningStock = 0;
        ClosingStock = 0;
        Rate = 0;
    }

    public int getMenuCode() {
        return MenuCode;
    }

    public void setMenuCode(int menuCode) {
        MenuCode = menuCode;
    }

    public String getItemName() {
        return ItemName;
    }

    public void setItemName(String itemName) {
        ItemName = itemName;
    }

    public double getOpeningStock() {
        return OpeningStock;
    }

    public void setOpeningStock(double openingStock) {
        OpeningStock = openingStock;
    }

    public double getClosingStock() {
        return ClosingStock;
    }

    public void setClosingStock(double closingStock) {
        ClosingStock = closingStock;
    }

    public double getRate() {
        return Rate;
    }

    public void setRate(double rate) {
        Rate = rate;
    }


}

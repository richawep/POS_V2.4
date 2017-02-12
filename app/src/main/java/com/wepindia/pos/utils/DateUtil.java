package com.wepindia.pos.utils;

import java.text.SimpleDateFormat;

/**
 * Created by PriyabratP on 29-11-2016.
 */

public class DateUtil {

    public static String getDateForDatePicker(String input){
        String rData = "";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-mm-dd");
        java.util.Date date= null;
        try {
            date = sdf.parse(input);
            sdf=new SimpleDateFormat("dd-mm-yyyy");
            rData = sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rData;
    }
}

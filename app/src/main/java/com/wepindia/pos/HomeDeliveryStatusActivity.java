package com.wepindia.pos;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.wep.common.app.Database.DatabaseHandler;
import com.wepindia.pos.GenericClasses.MessageDialog;

public class HomeDeliveryStatusActivity extends Activity{
	
	// Context object
	Context myContext;
	
	// DatabaseHandler object
	DatabaseHandler dbHomeDeliveryStatus = new DatabaseHandler(HomeDeliveryStatusActivity.this);
	// MessageDialog object
	MessageDialog MsgBox;// = new MessageDialog(HeaderFooterActivity.this);
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homedeliverystatus);
        
        myContext = this;
        
        MsgBox = new MessageDialog(myContext);
	}
}

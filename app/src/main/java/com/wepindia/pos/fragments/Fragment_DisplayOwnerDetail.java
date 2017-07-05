package com.wepindia.pos.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.wep.common.app.Database.DatabaseHandler;
import com.wep.common.app.views.WepButton;
import com.wepindia.pos.R;

public class Fragment_DisplayOwnerDetail extends Fragment {

    private EditText Name,Gstin,Phone,Email,Address,RefernceNo;
    private DatabaseHandler dbHelper;
    Spinner spinner1, spinner2;
    Context myContext;
    Button btnClose;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_fragment__display_owner_detail, container, false);
        myContext = getActivity();
        initialseViewVariablesAndDisplay(view);
        return view;
    }

    private void initialseViewVariablesAndDisplay(View view)
    {

        dbHelper = new DatabaseHandler(myContext);
        btnClose=(Button)view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close(v);
            }
        });
        Name=(EditText)view.findViewById(R.id.ownerName);
        Gstin=(EditText)view.findViewById(R.id.ownerGstin);
        RefernceNo=(EditText)view.findViewById(R.id.ownerReferenceNo);
        Phone=(EditText)view.findViewById(R.id.ownerContact);
        Email=(EditText)view.findViewById(R.id.ownerEmail);
        spinner1=(Spinner)view.findViewById(R.id.ownerPos);
        spinner2=(Spinner)view.findViewById(R.id.ownerOffice);
        spinner2.setSelection(1);
        Address=(EditText)view.findViewById(R.id.ownerAddress);
        loadOwnerDetail();
    }
    private void loadOwnerDetail()
    {
        dbHelper.CreateDatabase();
        dbHelper.OpenDatabase();
        Cursor cursor = dbHelper.getOwnerDetail();
        if(cursor!=null && cursor.moveToFirst())
        {
            String name = cursor.getString(cursor.getColumnIndex("OwnerName"));
            String gstin = cursor.getString(cursor.getColumnIndex("GSTIN"));
            String refernceNo = cursor.getString(cursor.getColumnIndex("refNo"));
            String phone = cursor.getString(cursor.getColumnIndex("Phone"));
            String email = cursor.getString(cursor.getColumnIndex("Email"));
            String address = cursor.getString(cursor.getColumnIndex("Address"));
            String pos = cursor.getString(cursor.getColumnIndex("POS"));
            String mainOffice = cursor.getString(cursor.getColumnIndex("IsMainOffice"));
            Name.setText(name);
            Gstin.setText(gstin);
            RefernceNo.setText(refernceNo);
            Phone.setText(phone);
            Email.setText(email);
            Address.setText(address);
            spinner1.setSelection(getIndex_pos(pos));
            if(mainOffice.equalsIgnoreCase("yes"))
                spinner2.setSelection(1);
            else
                spinner2.setSelection(2);
        }
        // dbHelper.close();
    }
    private int getIndex_pos(String substring){

        int index = 0;
        for (int i = 0; index==0 && i < spinner1.getCount(); i++){

            if (spinner1.getItemAtPosition(i).toString().contains(substring)){
                index = i;
            }

        }

        return index;

    }
    private void close(View v)
    {
        dbHelper.CloseDatabase();
        getActivity().finish();
    }
}

package com.wepindia.pos.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wep.common.app.Database.Department;
import com.wep.common.app.Database.Supplier_Model;
import com.wepindia.pos.R;

import java.util.ArrayList;

/**
 * Created by RichaA on 3/15/2017.
 */

public class SupplierAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<Supplier_Model> SupplierList;

    public SupplierAdapter(Activity activity, ArrayList<Supplier_Model> supplierList) {
        this.activity = activity;
        SupplierList = supplierList;
    }

    @Override
    public int getCount() {
        return SupplierList.size();
    }

    @Override
    public Object getItem(int position) {
        return SupplierList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       SupplierAdapter.ViewHolder viewHolder;
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_cat, null);
            viewHolder = new SupplierAdapter.ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.textViewTitle);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (SupplierAdapter.ViewHolder) convertView.getTag();
        }
        Supplier_Model suplier = SupplierList.get(position);
        String title = suplier.getSupplierName();
        viewHolder.textView.setText(title+"");
        return convertView;
    }
    public void notifyDataSetChanged(ArrayList<Supplier_Model> list) {
        this.SupplierList = list;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView textView;
    }
}

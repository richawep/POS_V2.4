package com.wepindia.pos.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wep.common.app.models.ItemStock;
import com.wepindia.pos.R;

import java.util.ArrayList;

/**
 * Created by RichaA on 3/16/2017.
 */

public class ItemInwardAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<ItemStock> itemList;

    public ItemInwardAdapter( Activity activity, ArrayList<ItemStock> itemList) {
        this.itemList = itemList;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemInwardAdapter.ViewHolder viewHolder;
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_cat, null);
            viewHolder = new ItemInwardAdapter.ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.textViewTitle);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ItemInwardAdapter.ViewHolder) convertView.getTag();
        }
        ItemStock item = itemList.get(position);
        String title = item.getItemName();
        viewHolder.textView.setText(title+"");
        return convertView;
    }
    public void notifyDataSetChanged(ArrayList<ItemStock> list) {
        this.itemList = list;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView textView;
    }
}

package com.wepindia.pos.GST.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.wep.common.app.Database.DatabaseHandler;
import com.wep.common.app.models.GSTR2_B2B_Amend;
import com.wepindia.pos.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by RichaA on 4/11/2017.
 */

public class GSTR1_B2CL_AmendAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<GSTR2_B2B_Amend> amendArrayList;
    private DatabaseHandler handler;


    public GSTR1_B2CL_AmendAdapter(Activity activity, ArrayList<GSTR2_B2B_Amend> amendArrayList, DatabaseHandler handler ){
        this.activity = activity;
        this. amendArrayList = amendArrayList;
        this.handler = handler;


    }


    public int getCount() {
        return  amendArrayList.size();
    }

    public Object getItem(int i) {
        return  amendArrayList.get(i);
    }
    public GSTR2_B2B_Amend getItems(int i) {
        return  amendArrayList.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    public void notifyNewDataAdded(ArrayList<GSTR2_B2B_Amend> list) {
        this. amendArrayList = list;
        notifyDataSetChanged();
    }

    public ArrayList<GSTR2_B2B_Amend> getItems() {
        return  amendArrayList;
    }

    public void notifyDataSetChanged(ArrayList<GSTR2_B2B_Amend> allItem) {
        this. amendArrayList = allItem;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView SNo;

        TextView invdate_ori;
        TextView invno_ori;
        TextView invno_rev;
        TextView invdate_rev;

        TextView IGSTAmount;
        TextView hsnCode;
        TextView tvg_s;
        TextView taxableValue;
        TextView value;

        ImageView btndel;
    }

    public View getView(int i, View convertView, ViewGroup viewGroup) {
        GSTR1_B2CL_AmendAdapter.ViewHolder viewHolder = null;
        int count =1;
        if(true)
        {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.amend_gstr1_b2cl_view,null);
            viewHolder = new GSTR1_B2CL_AmendAdapter.ViewHolder();
            viewHolder.SNo = (TextView) convertView.findViewById(R.id.tvSNo);
            viewHolder.invno_ori = (TextView) convertView.findViewById(R.id.tvinvno_ori);
            viewHolder.invdate_ori = (TextView) convertView.findViewById(R.id.tvinvdate_ori);
            viewHolder.invno_rev = (TextView) convertView.findViewById(R.id.tvinvno_rev);
            viewHolder.invdate_rev = (TextView) convertView.findViewById(R.id.tvinvdate_rev);
            viewHolder.hsnCode = (TextView) convertView.findViewById(R.id.tvhsn);
            viewHolder.tvg_s = (TextView) convertView.findViewById(R.id.tvg_s);
            viewHolder.taxableValue = (TextView) convertView.findViewById(R.id.tvtaxableValue);
            //viewHolder.value = (TextView) convertView.findViewById(R.id.tvValue);

            //viewHolder.IGSTRate = (TextView) convertView.findViewById(R.id.tvIGSTRate);
            viewHolder.IGSTAmount = (TextView) convertView.findViewById(R.id.tvIGSTAmount);
            //viewHolder.CGSTRate = (TextView) convertView.findViewById(R.id.tvCGSTRate);
            //viewHolder.CGSTAmount = (TextView) convertView.findViewById(R.id.tvCGSTAmount);
            //viewHolder.SGSTRate = (TextView) convertView.findViewById(R.id.tvSGSTRate);
            //viewHolder.SGSTAmount = (TextView) convertView.findViewById(R.id.tvSGSTAmount);

            viewHolder.btndel = (ImageView) convertView.findViewById(R.id.btnItemDelete);
            viewHolder.btndel.setTag(i);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (GSTR1_B2CL_AmendAdapter.ViewHolder) convertView.getTag();
        }
        GSTR2_B2B_Amend itemOutward =  amendArrayList.get(i);
        viewHolder.SNo.setText(String.valueOf(itemOutward.getSno()));

        viewHolder.invno_ori.setText(itemOutward.getInvoiceNo_ori());
        viewHolder.invdate_ori.setText(itemOutward.getInvoiceDate_ori());

        viewHolder.invno_rev.setText(itemOutward.getInvoiceNo_rev());
        viewHolder.invdate_rev.setText(itemOutward.getInvoiceDate_rev());

        viewHolder.tvg_s.setText(itemOutward.getType().toString());
        viewHolder.hsnCode.setText(itemOutward.getHSn());
        viewHolder.taxableValue.setText(String.format("%.2f",itemOutward.getTaxableValue()));
        //viewHolder.pos.setText(itemOutward.getPOS());
        //viewHolder.IGSTRate.setText(String.format("%.2f",itemOutward.getIrt()));
        viewHolder.IGSTAmount.setText(String.format("%.2f",itemOutward.getIgstamt()));
        //viewHolder.CGSTRate.setText(String.format("%.2f",itemOutward.getCrt()));
        //viewHolder.CGSTAmount.setText(String.format("%.2f",itemOutward.getCgstamt()));
        //viewHolder.SGSTRate.setText(String.format("%.2f",itemOutward.getSrt()));
        //viewHolder.SGSTAmount.setText(String.format("%.2f",itemOutward.getSgstamt()));

        viewHolder.btndel.setLayoutParams(new TableRow.LayoutParams(40, 35));
        viewHolder.btndel.setBackground(activity.getResources().getDrawable(R.drawable.delete_icon_border));
        viewHolder.btndel.setOnClickListener(mListener);

        return convertView;
    }


    private View.OnClickListener mListener = new View.OnClickListener() {
        public void onClick(final View v) {


            final int i  = Integer.parseInt(v.getTag().toString());
            final GSTR2_B2B_Amend obj = getItems(i);
            //String ItemName = obj.getLongName();
            AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to Delete this entry")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try{
                                String inv_no_ori = (obj.getInvoiceNo_ori());
                                String inv_date_ori_str = (obj.getInvoiceDate_ori());
                                Date date = new SimpleDateFormat("dd-MM-yyyy").parse(inv_date_ori_str);
                                String inv_date_ori = String.valueOf(date.getTime());
                                String inv_no_rev = (obj.getInvoiceNo_rev());
                                String inv_date_rev_str = (obj.getInvoiceDate_rev());
                                date = new SimpleDateFormat("dd-MM-yyyy").parse(inv_date_rev_str);
                                String inv_date_rev = String.valueOf(date.getTime());
                                String hsn= (obj.getHSn());
                                long lResult = handler.DeleteAmmend_GSTR1_b2cl(inv_no_ori, inv_date_ori,inv_no_rev,
                                            inv_date_rev,hsn,obj.getTaxableValue());

                                if(lResult>0)
                                {
                                    amendArrayList.remove(i);
                                    /*ArrayList<GSTR2_B2B_Amend> newAmmendList = new ArrayList<GSTR2_B2B_Amend>();
                                    newAmmendList.addAll(amendArrayList);
                                    */
                                    notifyDataSetChanged();
                                    /*notifyDataSetChanged(amendArrayList);
                                    adapter.notifyDataSetChanged();
                                    ll.setAdapter(adapter);*/

                                }
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
            AlertDialog alert = builder.create();
            alert.show();

        }
    };
}



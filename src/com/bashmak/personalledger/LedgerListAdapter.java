package com.bashmak.personalledger;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LedgerListAdapter extends ArrayAdapter<Ledger>
{
    private Context context; 
    private int layoutResourceId;    
    private ArrayList<Ledger> data = null;
    
    public LedgerListAdapter(Context context, int layoutResourceId, ArrayList<Ledger> data)
    {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }
	
	public void setDataAndNotify(ArrayList<Ledger> data)
    {
		this.data = data;
        notifyDataSetChanged();
    }
	
    @Override public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
        }
        
        TextView title = (TextView) row.findViewById(android.R.id.text1);
        title.setText(data.get(position).Name);
        
        return row;
    }

    @Override public int getCount()
    {
    	if (data != null)
    	{
    		return data.size();
    	}
    	else
    	{
    		return 0;
    	}
    }
    
    @Override public Ledger getItem(int position)
    {
    	return data.get(position);
    }

    public ArrayList<Ledger> getData()
    {
    	return data;
    }
}
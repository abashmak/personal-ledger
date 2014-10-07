package com.bashmak.personalledger;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bashmak.beeutils.BeeLog;

public class LedgerListAdapter extends ArrayAdapter<JSONObject>
{
    private Context context; 
    private int layoutResourceId;    
    private ArrayList<JSONObject> data = null;
    
    public LedgerListAdapter(Context context, int layoutResourceId, ArrayList<JSONObject> data)
    {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }
	
	public void setDataAndNotify(ArrayList<JSONObject> data)
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
        try
        {
			title.setText(data.get(position).getString("title"));
		}
        catch (JSONException e)
        {
        	BeeLog.e1("Exception parsing ledger JSON object", e);
		}
        
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
    
    @Override public JSONObject getItem(int position)
    {
    	return data.get(position);
    }

    public ArrayList<JSONObject> getData()
    {
    	return data;
    }
}
package com.bashmak.personalledger;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bashmak.beeutils.BeeGraphics;

public class ThumbGridAdapter extends ArrayAdapter<NameValuePair>
{
    private Context context; 
    private int layoutResourceId;    
    private ArrayList<NameValuePair> data = null;
    
    public ThumbGridAdapter(Context context, int resourceId, ArrayList<NameValuePair> data)
    {
    	super(context, resourceId, data);
    	layoutResourceId = resourceId;
        this.context = context;
        this.data = data;
    }
	
	public void setDataAndNotify(ArrayList<NameValuePair> data)
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
        
		ImageView img = (ImageView) row.findViewById(R.id.img1);
		BasicNameValuePair nvp = (BasicNameValuePair) data.get(position);
		Bitmap bitmap = BeeGraphics.decodeFile(nvp.getValue());
		bitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, false);
		img.setImageBitmap(bitmap);
        
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
    
    @Override public NameValuePair getItem(int position)
    {
    	return data.get(position);
    }

    public ArrayList<NameValuePair> getData()
    {
    	return data;
    }
}
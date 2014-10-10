package com.bashmak.personalledger;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class ImageGridAdapter extends ArrayAdapter<Bitmap>
{
    private Context context; 
    private int layoutResourceId;    
    private ArrayList<Bitmap> data = null;
    
    public ImageGridAdapter(Context context, int resourceId, ArrayList<Bitmap> data)
    {
    	super(context, resourceId, data);
    	layoutResourceId = resourceId;
        this.context = context;
        this.data = data;
    }
	
	public void setDataAndNotify(ArrayList<Bitmap> data)
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
		img.setImageBitmap(data.get(position));
        
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
    
    @Override public Bitmap getItem(int position)
    {
    	return data.get(position);
    }

    public ArrayList<Bitmap> getData()
    {
    	return data;
    }
}
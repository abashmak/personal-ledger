package com.bashmak.personalledger.activity;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;

import com.bashmak.beeutils.BeeGraphics;
import com.bashmak.beeutils.BeeLog;
import com.bashmak.beeutils.BeeToast;
import com.bashmak.personalledger.ImageGridAdapter;
import com.bashmak.personalledger.R;
import com.bashmak.personalledger.network.ApiResult;
import com.bashmak.personalledger.network.UpdateLedgerAsync;
import com.bashmak.personalledger.utility.Common;

public class AddEntryActivity extends WrapperActivity
{
	private JSONObject mLedger;
	private Uri mImageUri;
	private char mImageIndex = 'a';
	private String mEntryNum;
	
	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mLedger = Common.Ledgers.get(getIntent().getExtras().getInt("ledger_position"));
		setContentView(R.layout.view_add_entry);
		mEntryNum = getNextNumber();
		Common.Images.clear();
	}
	
	@Override public void handleAsyncResult(ApiResult result)
	{
		if (!result.Error.isEmpty())
		{
			BeeToast.showCenteredToastShort(this, "Unable to create new entry: " + result);
			setContentView(R.layout.view_add_entry);
		}
		else
		{
			//BeeToast.showCenteredToastShort(this, result.Response);
			finish();
		}
	}
	
	public void onCancelClicked(View view)
	{
		finish();
	}
	
	public void onSubmitClicked(View view)
	{
		hideKeyboard(view);
		EditText et = (EditText) findViewById(R.id.editDescription);
		final String description = et.getText().toString().trim();
		if (description.isEmpty())
		{
			BeeToast.showCenteredToastShort(this, "Description is required!");
			et.requestFocus();
			return;
		}
		
		et = (EditText) findViewById(R.id.editAmount);
		final String amount = et.getText().toString().trim();
		
		et = (EditText) findViewById(R.id.editDocDate);
		final String docDate = et.getText().toString().trim();
		
		if (Common.Images.isEmpty())
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.txt_confirm_entry_title);
			builder.setMessage(R.string.txt_confirm_entry_msg);
			builder.setPositiveButton(R.string.btn_ok, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					setProgressView(getString(R.string.txt_wait_create_entry));
					Common.addEntry(mEntryNum, description, amount, docDate);
			        new UpdateLedgerAsync(AddEntryActivity.this, "/" + mLedger.optString("code") + "/").execute(mLedger);
				}
			});
			builder.setNegativeButton(R.string.btn_cancel, null);
			builder.create().show();
		}
		else
		{
			setProgressView(getString(R.string.txt_wait_create_entry));
			Common.addEntry(mEntryNum, description, amount, docDate);
	        new UpdateLedgerAsync(AddEntryActivity.this, "/" + mLedger.optString("code") + "/").execute(mLedger);
		}
	}

	public void onNewImageClicked(View view)
	{
		mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
    	startActivityForResult(intent, 0);
	}
    
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            String imgPath = getPathFromUri(mImageUri);
            String imgName = mEntryNum + mImageIndex++ + imgPath.substring(imgPath.lastIndexOf('.'));
            Common.Images.add(new BasicNameValuePair(imgName, imgPath));
            ArrayList<Bitmap> thumbs = new ArrayList<Bitmap>();
            for (BasicNameValuePair nvp : Common.Images)
            {
                Matrix matrix = new Matrix();
                ExifInterface ei;
    			try
    			{
    				ei = new ExifInterface(nvp.getValue());
    	            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    	            switch(orientation)
    	            {
    	            case ExifInterface.ORIENTATION_ROTATE_90:
    	                matrix.postRotate(90);
    	                break;
    	            case ExifInterface.ORIENTATION_ROTATE_180:
    	                matrix.postRotate(180);
    	                break;
    	            case ExifInterface.ORIENTATION_ROTATE_270:
    	                matrix.postRotate(270);
    	                break;
    	            }
    			}
    			catch (IOException e)
    			{
    				e.printStackTrace();
    			}
        		Bitmap bitmapFull = BeeGraphics.decodeFile(nvp.getValue());
        		int newHeight = (int) Math.round(bitmapFull.getHeight() * 200.0 / bitmapFull.getWidth());
        		Bitmap bitmapScale = Bitmap.createScaledBitmap(bitmapFull, 200, newHeight, false);
        		bitmapFull.recycle();
                Bitmap bitmapRotate = Bitmap.createBitmap(bitmapScale, 0, 0, bitmapScale.getWidth(), bitmapScale.getHeight(), matrix, true);
                bitmapScale.recycle();
        		thumbs.add(bitmapRotate);
            }
			ImageGridAdapter adapter = new ImageGridAdapter(this, R.layout.grid_item_1, thumbs);
			GridView gv = (GridView) findViewById(R.id.gridThumbs);
			gv.setAdapter(adapter);
        }
    }

    private String getPathFromUri(Uri uri)
    {
    	String path = "";
		String[] projection = new String[] {Images.Media.DATA};
		Cursor cursor = Images.Media.query(getContentResolver(), uri, projection);
		if (cursor != null)
		{
			int data_column = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
			if (data_column >= 0 && cursor.moveToFirst())
			{
				path = cursor.getString(data_column);
			}
			cursor.close();
		}
		return path;
    }

    private static String getNextNumber()
    {
    	int num = 1;
    	for (JSONObject entry : Common.Entries)
    	{
    		try
    		{
    			int number = Integer.parseInt(entry.optString("number"));
    			if (number >= num)
    			{
    				num = number + 1;
    			}
    		}
    		catch (Exception e)
    		{
    			BeeLog.e1("Exception parsing entry number", e);
    		}
    	}
    	if (num < 10)
    	{
    		return "00" + num;
    	}
    	else if (num < 100)
    	{
    		return "0" + num;
    	}
    	else
    	{
    		return "" + num;
    	}
    }
}

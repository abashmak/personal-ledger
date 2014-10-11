package com.bashmak.personalledger.activity;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.beeutils.BeeToast;
import com.bashmak.personalledger.R;
import com.bashmak.personalledger.ThumbGridAdapter;
import com.bashmak.personalledger.network.ApiResult;
import com.bashmak.personalledger.utility.Common;

public class AddEntryActivity extends WrapperActivity
{
	//private JSONObject mLedger;
	private Uri mImageUri;
	private char mImageIndex = 'a';
	private String mEntryNum;
	
	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//mLedger = Common.Ledgers.get(getIntent().getExtras().getInt("ledger_position"));
		setContentView(R.layout.view_add_entry);
		mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
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
			BeeToast.showCenteredToastShort(this, result.Response);
			finish();
		}
	}
	
	public void onCancelClicked(View view)
	{
		finish();
	}
	
	public void onSubmitClicked(View view)
	{
		EditText et = (EditText) findViewById(R.id.editDescription);
		String description = et.getText().toString().trim();
		
		et = (EditText) findViewById(R.id.editAmount);
		String amount = et.getText().toString().trim();
		
		et = (EditText) findViewById(R.id.editDocDate);
		String docDate = et.getText().toString().trim();
		
		Common.addEntry(mEntryNum, description, amount, docDate);
		hideKeyboard(view);
		setProgressView(getString(R.string.txt_wait_create));
        //new UpdateLegersAsync(this, "/").execute();
	}

	public void onNewImageClicked(View view)
	{
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
    	startActivityForResult(intent, 0);
	}
    
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            String imgName = mEntryNum + mImageIndex++;
            String imgPath = getPathFromUri(mImageUri);
            for (int i = 0; i < 20; i++)
            Common.Images.add(new BasicNameValuePair(imgName, imgPath));
			ThumbGridAdapter adapter = new ThumbGridAdapter(this, R.layout.grid_item_1, Common.Images);
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
    				num++;
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

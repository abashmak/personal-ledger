package com.bashmak.personalledger.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.View;
import android.widget.EditText;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.beeutils.BeeToast;
import com.bashmak.personalledger.R;
import com.bashmak.personalledger.network.ApiResult;
import com.bashmak.personalledger.network.UpdateLegersAsync;
import com.bashmak.personalledger.utility.Common;

public class AddEntryActivity extends WrapperActivity
{
	//private JSONObject mLedger;
	private Uri mImageUri;
	
	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		//mLedger = Common.Ledgers.get(getIntent().getExtras().getInt("ledger_position"));

		setContentView(R.layout.view_add_entry);
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
		
		Common.addEntry(description, amount, docDate);
		hideKeyboard(view);
		setProgressView(getString(R.string.txt_wait_create));
        new UpdateLegersAsync(this, "/").execute();
	}

	public void onNewImageClicked(View view)
	{
    	ContentValues values = new ContentValues();
    	values.put(MediaStore.Images.Media.DESCRIPTION,"Image capture");
    	mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
    	startActivityForResult(intent, 0);
	}
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
        {
            String imgPath = getPathFromUri(data.getData());
            BeeLog.i1("DEBUG", imgPath);
			//mPic.setImageBitmap(BeeGraphics.decodeFile(imgPath));
        }
    }

    private String getPathFromUri(Uri uri)
    {
    	String path = "";
		String[] projection = new String[] {Images.Media.TITLE, Images.Media.DATA};
		Cursor cursor = Images.Media.query(getContentResolver(), uri, projection);
		if (cursor != null)
		{
			int data_column = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
			int title_column = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
			if (data_column >= 0 && title_column >= 0 && cursor.moveToFirst())
			{
				path = cursor.getString(data_column);
				BeeLog.i1("DEBUG", "title: " + cursor.getString(title_column));
			}
		}
		return path;
    }
}

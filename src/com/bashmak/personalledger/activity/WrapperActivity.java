package com.bashmak.personalledger.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.bashmak.personalledger.R;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

public abstract class WrapperActivity extends Activity
{
	protected ActionBar mActionBar;
    protected DropboxAPI<AndroidAuthSession> mApi;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mActionBar = getActionBar();
		if (!getClass().equals(MainActivity.class))
		{
			mActionBar.setHomeButtonEnabled(true);
		}
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
	    switch (item.getItemId())
	    {
	        case android.R.id.home:
	        	finish();
    			overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_out_right);
            	return true;
            default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override public void onBackPressed()
	{
		finish();
		if (!getClass().equals(MainActivity.class))
		{
			overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_out_right);
		}
	}
	
	protected void hideKeyboard(View view)
	{
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	protected void setProgressView(String message)
	{
		setContentView(R.layout.view_progress);
		TextView msg = (TextView) findViewById(R.id.message);
		msg.setText(message);
	}

	public abstract void handleAsyncResult(String result);
}

package com.bashmak.personalledger.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.bashmak.personalledger.network.ApiResult;

public class LedgerActivity extends WrapperActivity
{
	private final String TAG = "PL-Ledger";
	private String mMessage;
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override protected void onResume()
	{
    }

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}

	@Override public void handleAsyncResult(ApiResult result)
	{
	}
}

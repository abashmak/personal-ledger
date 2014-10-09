package com.bashmak.personalledger.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.beeutils.BeeToast;
import com.bashmak.personalledger.R;
import com.bashmak.personalledger.network.ApiResult;

public class ViewEntryActivity extends WrapperActivity
{
	private final String TAG = "PL-Entry";
	private JSONObject mEntry;
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.view_entry);
		String entry = getIntent().getExtras().getString("entry");
		try
		{
			mEntry = new JSONObject(entry);
		}
		catch (JSONException e)
		{
			BeeLog.e1(TAG, "Exception parsing entry JSON: ", e);
			BeeToast.showCenteredToastLong(this, "Unable to retrieve ledger entry\nContact app developer");
			finish();
		}
	}

	@Override protected void onResume()
	{
		super.onResume();
		
		setViewsVisibility(false, true, false, false);
		//new GetLedgerAsync(this, "/" + ledger.optString("code") + "/catalog.json").execute();
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
		if (!result.Error.isEmpty())
		{
			setViewsVisibility(false, false, true, true);
		}
		else if (result.Response.isEmpty())
		{
			setViewsVisibility(false, false, true, true);
		}
		else
		{
			try
			{
			}
			catch (Exception e)
			{
				BeeLog.e1(TAG, "Exception parsing catalog.json", e);
				setViewsVisibility(false, false, true, true);
			}
		}
	}

	private void setViewsVisibility(boolean list, boolean progress, boolean text, boolean button)
	{
		findViewById(R.id.listEntries).setVisibility(list ? View.VISIBLE : View.GONE);
		findViewById(R.id.progressEntries).setVisibility(progress ? View.VISIBLE : View.GONE);
		findViewById(R.id.txtNoEntries).setVisibility(text ? View.VISIBLE : View.GONE);
		findViewById(R.id.btnNewEntry).setVisibility(button ? View.VISIBLE : View.GONE);
	}
}

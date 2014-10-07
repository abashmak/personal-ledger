package com.bashmak.personalledger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.bashmak.beeutils.BeeLog;

public class MainActivity extends WrapperActivity
{
	private LedgerListAdapter mAdapter;
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		BeeLog.setPrefs("PL_Main", 3);
		setProgressView(getString(R.string.txt_wait_ledgers));

		// Obtain user's first name
		Cursor c = getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
		if (c != null && c.moveToFirst())
		{
			String fullName = c.getString(c.getColumnIndex("display_name"));
			if (fullName != null && !fullName.isEmpty())
			{
				String firstLast[] = fullName.split(" ");
				if (!firstLast[0].isEmpty())
				{
					Common.CreatorName = firstLast[0];
				}
			}
			BeeLog.i1("DEBUG", "User's name: " + Common.CreatorName);
		}
		
		// Obtain user's email
		AccountManager am = AccountManager.get(this);
		Account accounts[] = am.getAccountsByType("com.google");
		if (accounts.length > 0)
		{
			Common.CreatorEmail = accounts[0].name;
			BeeLog.i1("DEBUG", "User's email: " + Common.CreatorEmail);
		}
		
		new Handler().postDelayed(new Runnable()
		{
			@Override public void run()
			{
				refreshUI();
			}
		}, 5000);
	}
	
	private void refreshUI()
	{
		setContentView(R.layout.view_main);
		if (Common.Ledgers.isEmpty())
		{
			findViewById(R.id.txtNoLedgers).setVisibility(View.VISIBLE);
			findViewById(R.id.linListHolder).setVisibility(View.GONE);
		}
		else
		{
			mAdapter = new LedgerListAdapter(this, android.R.layout.simple_list_item_1, Common.Ledgers);
			((ListView) findViewById(R.id.linListHolder)).setAdapter(mAdapter);
			findViewById(R.id.txtNoLedgers).setVisibility(View.GONE);
			findViewById(R.id.linListHolder).setVisibility(View.VISIBLE);
		}
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onMainClicked(View view)
	{
		
	}
}

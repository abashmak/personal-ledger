package com.bashmak.personalledger.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.personalledger.LedgerListAdapter;
import com.bashmak.personalledger.R;
import com.bashmak.personalledger.network.DownloadTextFileAsync;
import com.bashmak.personalledger.utility.Common;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

public class MainActivity extends WrapperActivity
{
	private LedgerListAdapter mAdapter;
	private String mMessage;
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		BeeLog.setPrefs("PL_Main", 3);
		Common.init(this);
        mApi = new DropboxAPI<AndroidAuthSession>(Common.buildSession(this));

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
			c.close();
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
	}

	@Override protected void onResume()
	{
        super.onResume();
     
        AndroidAuthSession session = mApi.getSession();

        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful())
        {
            try
            {
                // Mandatory call to complete the auth
                session.finishAuthentication();
                Common.storeAuth(this, session);
            }
            catch (IllegalStateException e)
            {
                BeeLog.e1("Error authenticating dropbox", e);
            }
        }

        invalidateOptionsMenu();
		if (Common.DropboxSecret.isEmpty())
		{
			mMessage = getString(R.string.txt_no_cloud);
			refreshUI();
		}
		else
		{
			setProgressView(getString(R.string.txt_wait_ledgers));
            new DownloadTextFileAsync(this, mApi, "/ledgers.json").execute();
		}
    }

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem dropbox = menu.findItem(R.id.action_dropbox);
		if (Common.DropboxSecret.isEmpty())
		{
			dropbox.setTitle(R.string.action_dropbox_connect);
		}
		else
		{
			dropbox.setTitle(R.string.action_dropbox_disconnect);
		}
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId())
		{
		case R.id.action_dropbox:
			if (Common.DropboxSecret.isEmpty())
			{
	            mApi.getSession().startOAuth2Authentication(MainActivity.this);
			}
			else
			{
				invalidateOptionsMenu();
				Common.clearKeys(this);
				mMessage = getString(R.string.txt_no_cloud);
				refreshUI();
			}
			return true;
		case R.id.action_new_ledger:
			startActivity(new Intent(this, AddLedgerActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override public void handleAsyncResult(String result)
	{
		if (result.isEmpty())
		{
			mMessage = getString(R.string.txt_no_ledgers);
			refreshUI();
		}
		else
		{
			
		}
	}
	
	private void refreshUI()
	{
		setContentView(R.layout.view_main);
		if (Common.Ledgers.isEmpty())
		{
			TextView message = (TextView) findViewById(R.id.txtMessage);
			message.setVisibility(View.VISIBLE);
			message.setText(mMessage);
			findViewById(R.id.linListHolder).setVisibility(View.GONE);
		}
		else
		{
			mAdapter = new LedgerListAdapter(this, android.R.layout.simple_list_item_1, Common.Ledgers);
			((ListView) findViewById(R.id.listLedgers)).setAdapter(mAdapter);
			findViewById(R.id.txtMessage).setVisibility(View.GONE);
			findViewById(R.id.linListHolder).setVisibility(View.VISIBLE);
		}
	}
}

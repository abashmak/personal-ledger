package com.bashmak.personalledger.activity;

import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.beeutils.BeeToast;
import com.bashmak.personalledger.LedgerListAdapter;
import com.bashmak.personalledger.R;
import com.bashmak.personalledger.network.ApiResult;
import com.bashmak.personalledger.network.DeleteLedgerAsync;
import com.bashmak.personalledger.network.GetLedgersAsync;
import com.bashmak.personalledger.utility.Common;
import com.dropbox.client2.android.AndroidAuthSession;

public class MainActivity extends WrapperActivity implements OnItemClickListener, OnItemLongClickListener
{
	private final static String TAG = "PL-Main";
	private LedgerListAdapter mAdapter;
	private String mMessage;
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		BeeLog.setPrefs("PL-", 3);
		Common.init(this);

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
			BeeLog.i1(TAG, "User's name: " + Common.CreatorName);
		}
		
		// Obtain user's email
		AccountManager am = AccountManager.get(this);
		Account accounts[] = am.getAccountsByType("com.google");
		if (accounts.length > 0)
		{
			Common.CreatorEmail = accounts[0].name;
			BeeLog.i1(TAG, "User's email: " + Common.CreatorEmail);
		}
	}

	@Override protected void onResume()
	{
        super.onResume();
     
        AndroidAuthSession session = Common.DropboxApi.getSession();

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
                BeeLog.e1(TAG, "Error authenticating dropbox", e);
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
            new GetLedgersAsync(this, "/ledgers.json").execute();
		}
    }

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem dropbox = menu.findItem(R.id.action_dropbox);
		MenuItem ledger = menu.findItem(R.id.action_new_ledger);
		if (Common.DropboxSecret.isEmpty())
		{
			dropbox.setTitle(R.string.action_dropbox_connect);
			ledger.setEnabled(false);
			//ledger.setVisible(false);
		}
		else
		{
			dropbox.setTitle(R.string.action_dropbox_disconnect);
			ledger.setEnabled(true);
			//ledger.setVisible(true);
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
	            Common.DropboxApi.getSession().startOAuth2Authentication(MainActivity.this);
			}
			else
			{
				invalidateOptionsMenu();
				Common.clearKeys(this);
				Common.Ledgers.clear();
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

	@Override public void handleAsyncResult(ApiResult result)
	{
		if (!result.Error.isEmpty())
		{
			Common.Ledgers.clear();
			mMessage = getString(R.string.txt_no_ledgers);
		}
		else if (result.Response.equals("delete success"))
		{
			BeeToast.showCenteredToastShort(this, "Successfully deleted ledger");
		}
		else if (result.Response.isEmpty())
		{
			Common.Ledgers.clear();
			mMessage = getString(R.string.txt_no_ledgers);
		}
		else
		{
			Common.Ledgers.clear();
			try
			{
				JSONArray jArr = new JSONArray(result.Response);
				int len = jArr.length();
				for (int i = 0; i < len; i++)
				{
					Common.Ledgers.add((JSONObject) jArr.get(i));
				}
			}
			catch (Exception e)
			{
				BeeLog.e1(TAG, "Exception parsing ledgers.json", e);
			}
		}
		refreshUI();
	}

	@Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		showContextMenu(position);
		return true;
	}

	@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
	}
	
	private void refreshUI()
	{
		setContentView(R.layout.view_main);
		if (Common.Ledgers.isEmpty())
		{
			TextView message = (TextView) findViewById(R.id.txtMessage);
			message.setVisibility(View.VISIBLE);
			message.setText(mMessage);
			findViewById(R.id.txtMyLedgers).setVisibility(View.GONE);
			findViewById(R.id.listLedgers).setVisibility(View.GONE);
		}
		else
		{
			mAdapter = new LedgerListAdapter(this, android.R.layout.simple_list_item_1, Common.Ledgers);
			ListView lv = (ListView) findViewById(R.id.listLedgers);
			lv.setAdapter(mAdapter);
			lv.setOnItemClickListener(this);
			lv.setOnItemLongClickListener(this);
			findViewById(R.id.txtMessage).setVisibility(View.GONE);
			findViewById(R.id.txtMyLedgers).setVisibility(View.VISIBLE);
			findViewById(R.id.listLedgers).setVisibility(View.VISIBLE);
		}
	}

	private void showContextMenu(final int position)
    {
		CharSequence[] options = {getString(R.string.txt_view_ledger), getString(R.string.txt_delete_ledger)};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.txt_ledger_actions);
		builder.setItems(options, new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int option)
			{
				switch (option)
				{
				case 0:
					BeeToast.showCenteredToastShort(MainActivity.this, "View ledger selected");
    			   break;
				case 1:
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle(R.string.txt_confirm_delete);
					builder.setPositiveButton(R.string.btn_submit, new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							JSONObject ledger = mAdapter.getItem(position);
							Common.Ledgers.remove(position);
							setProgressView(getString(R.string.txt_wait_delete));
							new DeleteLedgerAsync(MainActivity.this, "/").execute(ledger.optString("code"));
						}
					});
					builder.setNegativeButton(R.string.btn_cancel, null);
					builder.create().show();
    			   break;
				default:
    			   break;
    		   }
    	   }
       });
       builder.create().show();
    }
}

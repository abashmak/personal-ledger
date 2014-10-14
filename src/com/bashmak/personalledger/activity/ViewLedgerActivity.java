package com.bashmak.personalledger.activity;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.personalledger.EntryListAdapter;
import com.bashmak.personalledger.R;
import com.bashmak.personalledger.network.ApiResult;
import com.bashmak.personalledger.network.GetLedgerAsync;
import com.bashmak.personalledger.utility.Common;

public class ViewLedgerActivity extends WrapperActivity implements OnItemClickListener, OnItemLongClickListener
{
	private final String TAG = "PL-Ledger";
	private JSONObject mLedger;
	private int mLedgerPosition;
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.view_ledger);
		mLedgerPosition = getIntent().getExtras().getInt("position");
		mLedger = Common.Ledgers.get(mLedgerPosition);
		((TextView) findViewById(R.id.txtTitle)).setText(mLedger.optString("title"));
		((TextView) findViewById(R.id.txtDescription)).setText(mLedger.optString("description"));
		((TextView) findViewById(R.id.txtCreatedBy)).setText(mLedger.optString("creator"));
		((TextView) findViewById(R.id.txtCreatedOn)).setText(new Date(mLedger.optLong("create_date")).toString());
		((TextView) findViewById(R.id.txtModified)).setText(new Date(mLedger.optLong("modify_date")).toString());
	}

	@Override protected void onResume()
	{
		super.onResume();
		
		setViewsVisibility(false, true, false, false);
		new GetLedgerAsync(this, "/" + mLedger.optString("code") + "/catalog.json").execute();
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
				Common.Entries.clear();
				JSONArray jArr = new JSONArray(result.Response);
				int len = jArr.length();
				if (len > 0)
				{
					for (int i = 0; i < len; i++)
					{
						Common.Entries.add((JSONObject) jArr.get(i));
					}
					EntryListAdapter adapter = new EntryListAdapter(this, R.layout.list_item_1, Common.Entries);
					ListView lv = (ListView) findViewById(R.id.listEntries);
					lv.setAdapter(adapter);
					lv.setOnItemClickListener(this);
					lv.setOnItemLongClickListener(this);
					setViewsVisibility(true, false, false, true);
				}
			}
			catch (Exception e)
			{
				BeeLog.e1(TAG, "Exception parsing catalog.json", e);
				setViewsVisibility(false, false, true, true);
			}
		}
	}

	@Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		BeeLog.i1(TAG, "Long item click position " + position);
		return true;
	}

	@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		Intent i = new Intent(this, ViewEntryActivity.class);
		i.putExtra("ledger_position", mLedgerPosition);
		i.putExtra("entry_position", position);
		startActivity(i);
	}

	public void onDescriptionClicked(View view)
	{
		TextView tv = (TextView) view;
		if (tv.getLayout().getEllipsisCount(0) > 0)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.txt_ledger_description);
			builder.setMessage(mLedger.optString("description"));
			builder.setNeutralButton("OK", null);
			builder.create().show();
		}
	}

	public void onNewEntryClicked(View view)
	{
		Intent i = new Intent(this, AddEntryActivity.class);
		i.putExtra("ledger_position", mLedgerPosition);
		startActivity(i);
	}
	
	public void onCreatorClicked(View view)
	{
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{mLedger.optString("email")});
		email.setType("html/text");
		startActivity(Intent.createChooser(email, "Send via..."));
	}

	private void setViewsVisibility(boolean list, boolean progress, boolean text, boolean button)
	{
		findViewById(R.id.listEntries).setVisibility(list ? View.VISIBLE : View.GONE);
		findViewById(R.id.progressEntries).setVisibility(progress ? View.VISIBLE : View.GONE);
		findViewById(R.id.txtNoEntries).setVisibility(text ? View.VISIBLE : View.GONE);
		findViewById(R.id.btnNewEntry).setVisibility(button ? View.VISIBLE : View.GONE);
	}
}

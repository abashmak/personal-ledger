package com.bashmak.personalledger.activity;

import java.util.ArrayList;
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
	private JSONObject ledger;
	private EntryListAdapter mAdapter;
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.view_ledger);
		int position = getIntent().getExtras().getInt("position");
		ledger = Common.Ledgers.get(position);
		((TextView) findViewById(R.id.txtTitle)).setText(ledger.optString("title"));
		((TextView) findViewById(R.id.txtDescription)).setText(ledger.optString("description"));
		((TextView) findViewById(R.id.txtCreatedBy)).setText(ledger.optString("creator"));
		((TextView) findViewById(R.id.txtCreatedOn)).setText(new Date(ledger.optLong("create_date")).toString());
		((TextView) findViewById(R.id.txtModified)).setText(new Date(ledger.optLong("modify_date")).toString());
	}

	@Override protected void onResume()
	{
		super.onResume();
		
		setViewsVisibility(false, true, false, false);
		new GetLedgerAsync(this, "/" + ledger.optString("code") + "/catalog.json").execute();
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
				JSONArray jArr = new JSONArray(result.Response);
				int len = jArr.length();
				if (len > 0)
				{
					ArrayList<JSONObject> entries = new ArrayList<JSONObject>();
					for (int i = 0; i < len; i++)
					{
						entries.add((JSONObject) jArr.get(i));
					}
					mAdapter = new EntryListAdapter(this, R.layout.list_item_1, entries);
					ListView lv = (ListView) findViewById(R.id.listEntries);
					lv.setAdapter(mAdapter);
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
		BeeLog.i1(TAG, "Item click position " + position);
	}

	public void onDescriptionClicked(View view)
	{
		TextView tv = (TextView) view;
		if (tv.getLayout().getEllipsisCount(0) > 0)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.txt_ledger_description);
			CharSequence[] options = {ledger.optString("description")};
			builder.setItems(options, null);
			builder.setNeutralButton("OK", null);
			builder.create().show();
		}
	}
	
	public void onCreatorClicked(View view)
	{
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{ledger.optString("email")});
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

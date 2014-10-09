package com.bashmak.personalledger.network;

import java.io.ByteArrayInputStream;

import org.json.JSONObject;

import android.os.AsyncTask;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.personalledger.activity.WrapperActivity;
import com.bashmak.personalledger.utility.Common;

public class DeleteLedgerAsync extends AsyncTask<String, Long, Boolean>
{
	private final static String TAG = "PL-DeleteLedger";
	private WrapperActivity mActivity;
    private String mPath;
    private ApiResult mResult;

    public DeleteLedgerAsync(WrapperActivity activity, String dropboxPath)
    {
    	mActivity = activity;
        mPath = dropboxPath;
        mResult = new ApiResult("delete success");
    }

    @Override protected Boolean doInBackground(String... params)
    {
        try
        {
        	int numLedgers = Common.Ledgers.size();
        	if (numLedgers > 0)
        	{
	        	// Upload full ledgers.json
	        	StringBuilder sb = new StringBuilder("[ ");
	        	for (JSONObject ledger : Common.Ledgers)
	        	{
	        		sb.append(ledger.toString()).append(",");
	        	}
	        	sb.replace(sb.length()-1, sb.length(), "]");
	        	byte[] bytes = sb.toString().getBytes("UTF-8");
	        	ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
	        	Common.DropboxApi.putFileOverwrite(mPath + "ledgers.json", bais, bytes.length, null);
	        	bais.close();
        	
	        	// Recreate ledgers.html
	        	bytes = Common.genHtmlLedgers().getBytes("UTF-8");
	        	bais = new ByteArrayInputStream(bytes);
	        	Common.DropboxApi.putFileOverwrite(mPath + "ledgers.html", bais, bytes.length, null);
	        	bais.close();
        	}
        	else
        	{
        		// Delete ledgers.json and ledgers.html
            	Common.DropboxApi.delete(mPath + "ledgers.json");
            	Common.DropboxApi.delete(mPath + "ledgers.html");
        	}

        	// Delete ledger folder
        	Common.DropboxApi.delete(mPath + params[0]);
        	
            return true;
        }
        catch (Exception e)
        {
        	BeeLog.w1(TAG, "Dropbox api exception: " + e.toString());
        	mResult.Error = "unknown error";
        	if (Common.Ledgers.size() > 0)
        	{
        		Common.Ledgers.remove(Common.Ledgers.size()-1);
        	}
            return false;
        }
    }

    @Override protected void onPostExecute(Boolean result)
    {
    	if (mActivity!= null)
    	{
    		mActivity.handleAsyncResult(mResult);
    	}
    }
}

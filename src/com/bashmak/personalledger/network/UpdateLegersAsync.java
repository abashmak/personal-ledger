package com.bashmak.personalledger.network;

import java.io.ByteArrayInputStream;

import org.json.JSONObject;

import android.os.AsyncTask;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.personalledger.activity.WrapperActivity;
import com.bashmak.personalledger.utility.Common;

public class UpdateLegersAsync extends AsyncTask<Void, Long, Boolean>
{
	private final static String TAG = "PL-UpdateLedgers";
	private WrapperActivity mActivity;
    private String mPath;
    private ApiResult mResult;

    public UpdateLegersAsync(WrapperActivity activity, String dropboxPath)
    {
    	mActivity = activity;
        mPath = dropboxPath;
        mResult = new ApiResult("Success!");
    }

    @Override protected Boolean doInBackground(Void... params)
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
	        	
	        	// Create folder path for the newly added ledger
	        	JSONObject newLedger = Common.Ledgers.get(numLedgers-1);
	        	String code = newLedger.optString("code");
	        	Common.DropboxApi.createFolder(mPath + code);
	        	
	        	// Create ledger.html for the newly added ledger
	        	bytes = Common.genHtmlLedger(newLedger).getBytes("UTF-8");
	        	bais = new ByteArrayInputStream(bytes);
	        	Common.DropboxApi.putFileOverwrite(mPath + "/" + code + "/index.html", bais, bytes.length, null);
	        	bais.close();
        	}
        	
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

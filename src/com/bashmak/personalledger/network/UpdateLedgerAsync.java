package com.bashmak.personalledger.network;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.personalledger.activity.WrapperActivity;
import com.bashmak.personalledger.utility.Common;

public class UpdateLedgerAsync extends AsyncTask<JSONObject, Long, Boolean>
{
	private final static String TAG = "PL-UpdateLedger";
	private WrapperActivity mActivity;
    private String mPath;
    private ApiResult mResult;

    public UpdateLedgerAsync(WrapperActivity activity, String dropboxPath)
    {
    	mActivity = activity;
        mPath = dropboxPath;
        mResult = new ApiResult("Success!");
    }

    @Override protected Boolean doInBackground(JSONObject... params)
    {
        try
        {
        	int numEntries = Common.Entries.size();
        	if (numEntries > 0)
        	{
        		HashMap<String, String> entriesHtml = new HashMap<String, String>();
        		
	        	// Upload full catalog.json
	        	StringBuilder sb = new StringBuilder("[ ");
	        	for (JSONObject entry : Common.Entries)
	        	{
	        		entriesHtml.put(entry.optString("number"), Common.genHtmlEntry(entry));
	        		sb.append(entry.toString()).append(",");
	        	}
	        	sb.replace(sb.length()-1, sb.length(), "]");
	        	byte[] bytes = sb.toString().getBytes("UTF-8");
	        	ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
	        	Common.DropboxApi.putFileOverwrite(mPath + "catalog.json", bais, bytes.length, null);
	        	bais.close();
	        	
	        	// Upload entry html files
	        	for (String key : entriesHtml.keySet())
	        	{
		        	bytes = entriesHtml.get(key).getBytes("UTF-8");
		        	bais = new ByteArrayInputStream(bytes);
		        	Common.DropboxApi.putFileOverwrite(mPath + key + ".html", bais, bytes.length, null);
		        	bais.close();
	        	}

	        	// Recreate index.html
	        	bytes = Common.genHtmlLedger(params[0]).getBytes("UTF-8");
	        	bais = new ByteArrayInputStream(bytes);
	        	Common.DropboxApi.putFileOverwrite(mPath + "index.html", bais, bytes.length, null);
	        	bais.close();
	        	
	        	// Upload images
	        	for (BasicNameValuePair image : Common.Images)
	        	{
	        		File file = new File(image.getValue());
	                FileInputStream fis = new FileInputStream(file);
	                String path = mPath + image.getName();
	                Common.DropboxApi.putFileOverwrite(path, fis, file.length(), null);
	                fis.close();
	                file.delete();
	                if (mActivity != null)
	                {
			    		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			    		mediaScanIntent.setData(Uri.fromFile(file));
			    		mActivity.sendBroadcast(mediaScanIntent);
	                }
	        	}
        	}
        	
            return true;
        }
        catch (Exception e)
        {
        	BeeLog.w1(TAG, "Dropbox api exception: " + e.toString());
        	mResult.Error = "unknown error";
        	if (Common.Entries.size() > 0)
        	{
        		Common.Entries.remove(Common.Entries.size()-1);
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

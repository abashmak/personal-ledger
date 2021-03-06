package com.bashmak.personalledger.network;

import java.io.ByteArrayInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.personalledger.activity.WrapperActivity;
import com.bashmak.personalledger.utility.Common;
import com.dropbox.client2.exception.DropboxServerException;

public class DeleteEntryAsync extends AsyncTask<JSONObject, Long, Boolean>
{
	private final static String TAG = "PL-DeleteEntry";
	private WrapperActivity mActivity;
    private String mPath;
    private ApiResult mResult;

    public DeleteEntryAsync(WrapperActivity activity, String dropboxPath)
    {
    	mActivity = activity;
        mPath = dropboxPath;
        mResult = new ApiResult("delete success");
    }

    @Override protected Boolean doInBackground(JSONObject... params)
    {
        try
        {
        	// Upload full catalog.json
        	StringBuilder sb = new StringBuilder("[ ");
        	for (JSONObject entry : Common.Entries)
        	{
        		sb.append(entry.toString()).append(",");
        	}
        	sb.replace(sb.length()-1, sb.length(), "]");
        	byte[] bytes = sb.toString().getBytes("UTF-8");
        	ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
       		Common.DropboxApi.putFileOverwrite(mPath + "catalog.json", bais, bytes.length, null);
        	bais.close();
    	
        	// Recreate index.html
        	bytes = Common.genHtmlLedger(params[0]).getBytes("UTF-8");
        	bais = new ByteArrayInputStream(bytes);
       		Common.DropboxApi.putFileOverwrite(mPath + "index.html", bais, bytes.length, null);
        	bais.close();

        	// Delete <number>.html
        	try
        	{
        		Common.DropboxApi.delete(mPath + params[1].getString("number") + ".html");
    		}
    		catch (DropboxServerException e)
    		{
        		// Allow for the case where file may have been deleted outside of the api
            	if (e.error != DropboxServerException._404_NOT_FOUND)
            	{
            		throw new Exception(e);
            	}
    		}

        	// Delete images
        	JSONArray images = params[1].getJSONArray("images");
        	for (int i = 0; i < images.length(); i++)
        	{
        		try
        		{
        			Common.DropboxApi.delete(mPath + images.getString(i));
        		}
        		catch (DropboxServerException e)
        		{
            		// Allow for the case where file may have been deleted outside of the api
                	if (e.error != DropboxServerException._404_NOT_FOUND)
                	{
                		throw new Exception(e);
                	}
        		}
        	}
        	
            return true;
        }
        catch (Exception e)
        {
        	BeeLog.w1(TAG, "Dropbox api exception: " + e.toString());
        	mResult.Error = "unknown error";
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

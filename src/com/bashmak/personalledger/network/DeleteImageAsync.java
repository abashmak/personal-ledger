package com.bashmak.personalledger.network;

import java.io.ByteArrayInputStream;

import org.json.JSONObject;

import android.os.AsyncTask;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.personalledger.activity.WrapperActivity;
import com.bashmak.personalledger.utility.Common;
import com.dropbox.client2.exception.DropboxServerException;

public class DeleteImageAsync extends AsyncTask<JSONObject, Long, Boolean>
{
	private final static String TAG = "PL-DeleteImage";
	private WrapperActivity mActivity;
    private String mPath;
    private String mImage;
    private ApiResult mResult;

    public DeleteImageAsync(WrapperActivity activity, String dropboxPath, String img)
    {
    	mActivity = activity;
        mPath = dropboxPath;
        mImage = img;
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
    	
        	// Recreate entry html
        	bytes = Common.genHtmlEntry(params[0]).getBytes("UTF-8");
        	bais = new ByteArrayInputStream(bytes);
        	Common.DropboxApi.putFileOverwrite(mPath + params[0].optString("number") + ".html", bais, bytes.length, null);
        	bais.close();

        	// Delete image
        	try
        	{
        		Common.DropboxApi.delete(mPath + mImage);
    		}
    		catch (DropboxServerException e)
    		{
        		// Allow for the case where file may have been deleted outside of the api
            	if (e.error != DropboxServerException._404_NOT_FOUND)
            	{
            		throw new Exception(e);
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

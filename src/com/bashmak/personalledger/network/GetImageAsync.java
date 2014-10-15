package com.bashmak.personalledger.network;

import java.io.ByteArrayOutputStream;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.bashmak.beeutils.BeeLog;
import com.bashmak.personalledger.activity.WrapperActivity;
import com.bashmak.personalledger.utility.Common;
import com.dropbox.client2.DropboxAPI.DropboxInputStream;
import com.dropbox.client2.DropboxAPI.Entry;

public class GetImageAsync extends AsyncTask<String, Long, Boolean>
{
	private final static String TAG = "PL-GetImage";
	private WrapperActivity mActivity;
    private String mPath;
    private ApiResult mResult;

    public GetImageAsync(WrapperActivity activity, String dropboxPath)
    {
    	mActivity = activity;
        mPath = dropboxPath;
        mResult = new ApiResult();
    }

    @Override protected Boolean doInBackground(String... params)
    {
        try
        {
        	Entry entry = Common.DropboxApi.metadata(mPath, 1000, null, true, null);
        	for (Entry file : entry.contents)
        	{
        		if (file.mimeType.startsWith("image/") && file.fileName().startsWith(params[0]))
        		{
                    DropboxInputStream dis = Common.DropboxApi.getFileStream(file.path, null);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    dis.copyStreamToOutput(baos, null);
                    baos.close();
                    byte thumbBytes[] = baos.toByteArray();
                    mResult.Thumbnails.add(BitmapFactory.decodeByteArray(thumbBytes, 0, thumbBytes.length));
                    dis.close();
                    break;
        		}
        	}
            return true;
        }
        catch (Exception e)
        {
        	BeeLog.w1(TAG, "Dropbox api exception: " + e.toString());
        	mResult.Error = "Unable to obtain image";
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

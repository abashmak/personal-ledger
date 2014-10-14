package com.bashmak.personalledger.utility;

import java.util.ArrayList;
import java.util.Date;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.bashmak.beeutils.BeeLog;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

public class Common
{
    private final static String APP_KEY = "v9izdoem5z240yn";
    private final static String APP_SECRET = "pzj4274kxvlouml";
    private final static String DROPBOX_ACCOUNT_PREFS_NAME = "prefs";
    private final static String ACCESS_SECRET_NAME = "access_secret";
    
    public static String DropboxSecret; 
    public static DropboxAPI<AndroidAuthSession> DropboxApi;
    
    public static ArrayList<JSONObject> Ledgers = new ArrayList<JSONObject>();
	public static ArrayList<JSONObject> Entries = new ArrayList<JSONObject>();
	public static ArrayList<BasicNameValuePair> Images = new ArrayList<BasicNameValuePair>();
	
	public static String CreatorName = "Unknown";
	public static String CreatorEmail = "unknown";

	public static void init(Context context)
	{
        SharedPreferences prefs = context.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
        DropboxSecret = prefs.getString(ACCESS_SECRET_NAME, "");
        DropboxApi = new DropboxAPI<AndroidAuthSession>(buildSession(context));
        Ledgers.clear();
	}
	
    public static AndroidAuthSession buildSession(Context context)
    {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        if (!DropboxSecret.isEmpty())
        {
            session.setOAuth2AccessToken(DropboxSecret);
        }
        return session;
    }
    
	public static void clearKeys(Context context)
	{
        DropboxSecret = "";
        SharedPreferences prefs = context.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
        prefs.edit().remove(ACCESS_SECRET_NAME).commit();
	}

    public static void storeAuth(Context context, AndroidAuthSession session)
    {
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null)
        {
            SharedPreferences prefs = context.getSharedPreferences(DROPBOX_ACCOUNT_PREFS_NAME, 0);
            prefs.edit().putString(ACCESS_SECRET_NAME, oauth2AccessToken).commit();
        	DropboxSecret = oauth2AccessToken;
        }
    }

    public static String addLedger(String title, String description)
    {
		JSONObject jObj = new JSONObject();
		try
		{
			String code = genUniqueCode(title);
			jObj.putOpt("code", code);
			jObj.putOpt("title", title);
			jObj.putOpt("description", description);
			jObj.putOpt("creator", CreatorName);
			jObj.putOpt("email", CreatorEmail);
			jObj.putOpt("create_date", System.currentTimeMillis());
			jObj.putOpt("modify_date", System.currentTimeMillis());
			jObj.putOpt("template", 1);
			Ledgers.add(jObj);
			return code;
		}
		catch (JSONException e)
		{
			BeeLog.e1("Exception adding new ledger", e);
			return "";
		}
    }

    public static String addEntry(String number, String description, String amount, String docDate)
    {
		JSONObject jObj = new JSONObject();
		try
		{
			jObj.putOpt("number", number);
			jObj.putOpt("description", description);
			jObj.putOpt("amount", amount);
			jObj.putOpt("doc_date", docDate);
			jObj.putOpt("create_date", System.currentTimeMillis());
			jObj.putOpt("modify_date", System.currentTimeMillis());
			JSONArray images = new JSONArray();
			for (BasicNameValuePair image : Common.Images)
			{
				images.put(image.getName());
			}
			jObj.putOpt("images", images);
			Entries.add(jObj);
			return number;
		}
		catch (JSONException e)
		{
			BeeLog.e1("Exception adding new entry", e);
			return "";
		}
    }
    
    public static String genHtmlLedgers()
    {
    	StringBuilder html = new StringBuilder();
    	String style = "<style>table, th, td {border: 1px solid black; border-collapse: collapse;} th, td {padding: 5px; text-align: left;}</style>";
    	html.append("<!DOCTYPE html><html><head><title>My Ledgers</title>" + style + "</head><body><h1>My Ledgers:</h1><table style=\"width:100%\">");
    	html.append("<tr><th>Title</th><th>Description</th><th>Created By</th><th>Created On</th><th>Modified On</th></tr>");
    	for (JSONObject ledger : Ledgers)
    	{
    		html.append("<tr>");
    		html.append("<td><a href=\"" + ledger.optString("code") + "/index.html\">" + ledger.optString("title") + "</a></td>");
    		html.append("<td>" + ledger.optString("description") + "</td>");
    		html.append("<td>" + ledger.optString("creator") + "</td>");
    		html.append("<td>" + new Date(ledger.optLong("create_date")) + "</td>");
    		html.append("<td>" + new Date(ledger.optLong("modify_date")) + "</td>");
    		html.append("</tr>");
    	}
    	return html.append("</table></body></html>").toString();
    }
    
    public static String genHtmlLedger(JSONObject ledger)
    {
    	String style = "<style>table, th, td {border: 1px solid black; border-collapse: collapse;} th, td {padding: 5px; text-align: left;}</style>";
    	StringBuilder html = new StringBuilder("<!DOCTYPE html><html><head><title>Ledger</title>" + style + "</head><body>");
    	html.append("<h1>" + ledger.optString("title") + "</h1>");
    	html.append("<label><b>Description: </b><span>" + ledger.optString("description") + "</span></label><br>");
    	html.append("<label><b>Created by: </b><span>" + ledger.optString("creator") + "</span></label><br>");
    	html.append("<label><b>Email: </b><span>" + ledger.optString("email") + "</span></label><br>");
    	html.append("<label><b>Created on: </b><span>" + new Date(ledger.optLong("create_date")) + "</span></label><br>");
    	html.append("<label><b>Modified on: </b><span>" + new Date(ledger.optLong("modify_date")) + "</span></label><br><br>");
    	html.append("<h2>Entries:</h2>");
    	html.append("<table style=\"width:100%\"><tr><th>Number</th><th>Description</th><th>Amount</th><th>Document Date</th><th>Created On</th><th>Modified On</th></tr>");
    	for (JSONObject entry : Entries)
    	{
    		html.append("<tr>");
    		html.append("<td><a href=\"" + entry.optString("number") + ".html\">" + entry.optString("number") + "</a></td>");
    		html.append("<td>" + entry.optString("description") + "</td>");
    		html.append("<td>" + entry.optString("amount") + "</td>");
    		html.append("<td>" + entry.optString("doc_date") + "</td>");
    		html.append("<td>" + new Date(entry.optLong("create_date")) + "</td>");
    		html.append("<td>" + new Date(entry.optLong("modify_date")) + "</td>");
    		html.append("</tr>");
    	}
    	return html.append("</table></body></html>").toString();
    }
    
    private static String genUniqueCode(String title)
    {
    	title = title.replaceAll("\\s+","");
    	int num = 0;
    	for (JSONObject ledger : Ledgers)
    	{
    		if (ledger.optString("code").startsWith(title))
    		{
    			num++;
    		}
    	}
    	return title + "_" + num;
    }
}

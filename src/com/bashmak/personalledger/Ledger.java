package com.bashmak.personalledger;

import org.json.JSONException;
import org.json.JSONObject;

import com.bashmak.beeutils.BeeLog;

public class Ledger
{
	public enum Template {GeneralLedger};
	
	public String Code;
	public String Name;
	public String Description;
	public String Creator;
	public String Email;
	
	public long CreateDate;
	public long ModifyDate;

	private Template template;
	
	public Ledger(String code, String name, String desc, Template t, String creator, String email)
	{
		Code = code;
		Name = name;
		Description = desc;
		Creator = creator;
		Email = email;
		CreateDate = System.currentTimeMillis();
		ModifyDate = System.currentTimeMillis();
		template = t;
	}
	
	public Ledger(String json)
	{
		
	}
	
	public int getTemplate()
	{
		switch (template)
		{
		case GeneralLedger:
			return 1;
		default:
			return 0;
		}
	}

	public String toJson()
	{
		JSONObject jObj = new JSONObject();
		try
		{
			jObj.putOpt("code", Code);
			jObj.putOpt("Name", Name);
			jObj.putOpt("description", Description);
			jObj.putOpt("creator", Creator);
			jObj.putOpt("email", Email);
			jObj.putOpt("create_date", CreateDate);
			jObj.putOpt("modify_date", ModifyDate);
			jObj.putOpt("template", getTemplate());
		}
		catch (JSONException e)
		{
			BeeLog.e1("Exception converting Ledger object to JSON: " + e.getMessage());
		}
		
		return jObj.toString();
	}
}

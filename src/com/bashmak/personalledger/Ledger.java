package com.bashmak.personalledger;

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
}

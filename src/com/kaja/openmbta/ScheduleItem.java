package com.kaja.openmbta;

import java.util.Date;

public class ScheduleItem {

	private String name="";
	private String[] desc;
	private String type="";
	
	
	public ScheduleItem(String _name, String[] _desc, String  _type) {
		name = _name;
	    desc = _desc;
	    type = _type;
	  }
	
	public ScheduleItem()
	{
	}
	public String getName() {
		return(name);
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public String getDesc(int i) {
		return(desc[i]);
	}
	
	public void setDesc(int i) {
		this.desc[i]=desc[i];
	}
	
	public String getType() {
		return(type);
	}
	
	public void setType(String type) {
		this.type=type;
	}
	
	public String toString() {
		return(getName());
	}
}
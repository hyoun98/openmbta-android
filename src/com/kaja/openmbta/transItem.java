package com.kaja.openmbta;

import java.util.Date;

public class transItem {
    
	private String route="";
	private String name="";
	private String desc="";
	private String type="";
	
	
	public transItem(String _route, String _name, String _desc, String  _type) {
		route = _route;
		name = _name;
	    desc = _desc;
	    type = _type;
	  }
	
	public transItem()
	{
	}
	public String getRoute(){
		return(route);
	}
	public String getName() {
		return(name);
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public void setRoute(String route){
		this.route=route;
	}
	public String getDesc() {
		return(desc);
	}
	
	public void setDesc(String desc) {
		this.desc=desc;
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
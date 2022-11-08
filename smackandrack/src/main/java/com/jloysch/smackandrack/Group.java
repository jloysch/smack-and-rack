package com.jloysch.smackandrack;

public class Group {
	String friendlyName;
	String guuname;
	private int uuid;
	int accessLevel;
	
	@Override
	public String toString() {
		return (this.friendlyName == null ? "NULL" : this.friendlyName);
	}

	public int getUuid() {
		return uuid;
	}

	public void setUuid(int uuid) {
		this.uuid = uuid;
	}
	
	public String getgUuname() {
		return this.guuname;
	}

	public void setgUuname(String guuname) {
		this.guuname = guuname;
	}
}

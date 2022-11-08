package com.jloysch.smackandrack;

import java.util.HashMap;

public class User {
	
	private String friendlyName;
	private String uuname;
	private int uuid;
	int guuid;
	public HashMap <String, Boolean> directAccess; //List of permissions user has direct access to, regardless of group.
	
	public User() {
		this.directAccess = new HashMap <String, Boolean>();
		this.setUuid(-1);
		this.guuid = -1;
	}
	
	@Override
	public String toString() {
		return ("FRIENDLY NAME> " + (this.getFriendlyName() == null ? "NULL" : this.getFriendlyName()) + "\nUUNAME> " + this.uuname + "\nUUID> " + this.getUuid() + "\nGUUID> " + this.guuid);
	}

	public int getUuid() {
		return uuid;
	}

	public void setUuid(int uuid) {
		this.uuid = uuid;
	}
	
	public void setUuname(String uuname) {
		this.uuname = uuname;
	}
	
	public String getUuname() {
		return this.uuname;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}
}

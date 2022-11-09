package com.jloysch.smackandrack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

//Resource Access Control Kit
/*
class User {
	String friendlyName;
	int uuid, guuid;
	HashMap <String, Boolean> directAccess; //List of permissions user has direct access to, regardless of group.
	
	public User() {
		this.directAccess = new HashMap <String, Boolean>();
		this.uuid = -1;
		this.guuid = -1;
	}
	
	@Override
	public String toString() {
		return ("Username> " + (this.friendlyName == null ? "NULL" : this.friendlyName) + "\nUUID> " + this.uuid + "\nGUUID> " + this.guuid);
	}
}

class Group {
	String friendlyName;
	int uuid;
	int accessLevel;
	
	@Override
	public String toString() {
		return (this.friendlyName == null ? "NULL" : this.friendlyName);
	}
}
*/

class GroupHierarchyTree {
	
	LinkedList <LinkedList <Group>> groupHierarchyList; 
	private int internalIncrement;
	
	public GroupHierarchyTree() {
		this.groupHierarchyList = new LinkedList <LinkedList <Group>>();
		
		this.internalIncrement = 300;
	}
	
	public int getStep() {
		return this.internalIncrement;
	}
	
	public int step() {
		return this.internalIncrement++;
	}
	
	public String getGroupNameFor(int guuid) {
		for (int i = 0; i < this.groupHierarchyList.size(); i++) {
			for (int j = 0; j < this.groupHierarchyList.get(i).size(); j++) {
				//System.out.println(groupHierarchyList.get(i).get(j).uuid + " == " + guuid);
				if (this.groupHierarchyList.get(i).get(j).getUuid() == guuid) return this.groupHierarchyList.get(i).get(j).friendlyName;
			}
		}
		return null;
	}
	
	
	public int getAccessLevelFor (int guuid) {
		
		for (int i = 0; i < this.groupHierarchyList.size(); i++) {
			for (int j = 0; j < this.groupHierarchyList.get(i).size(); j++) {
				//System.out.println(groupHierarchyList.get(i).get(j).uuid + " == " + guuid);
				if (this.groupHierarchyList.get(i).get(j).getUuid() == guuid) return i;
			}
		}
		
		return -1;
	}
	
	public boolean isValidGUUID(int guuid) {
		
		for (int i = 0; i < this.groupHierarchyList.size(); i++) {
			for (int j = 0; j < this.groupHierarchyList.get(i).size(); j++) {
				if (this.groupHierarchyList.get(i).get(j).getUuid() == guuid) return true;
			}
		}
		
		return false;
	}
	
	public void trimInternal() {
		for (int i = 0; i < this.groupHierarchyList.size(); i++) {
			if (this.groupHierarchyList.get(i).size() == 0) {
				this.groupHierarchyList.remove(i);
			}
		}
	}
	
	public String guunameFromgUuid(int guuid) {
		return this.locateGroup(guuid).getgUuname();
	}
	
	
	public void add(int accessLevel, String groupName) {
		//shouldn't leave any gaps, so accesslevels should be within 1 , also have to deal with cleaning the tree after
		
		if (accessLevel > this.groupHierarchyList.size()) { 
			//The access level isn't implying we should create a new one, e.g. the 'highest' is 5 and they entered 7.
			System.out.println("Access level out of bounds for the tree, please check your input."
					+ " FAILED > ( " + accessLevel + " < " + this.groupHierarchyList.size() + "?)");
			return;
		} else if (accessLevel < 0) {
			System.out.println("Access level out of bounds for the tree, please check your input ( accessLevel < 0)");
			return;
		}
		
		Group newGroup = new Group();
		
		newGroup.friendlyName = groupName;
		newGroup.setUuid(internalIncrement++); //Heavily doubt they'll have even close to INT_MAX groups, lol.
		newGroup.accessLevel = accessLevel;
		
	
		//this.groupHierarchyList.add(accessLevel, new LinkedList <Group>());
		
		if (accessLevel+1 > this.groupHierarchyList.size()) {
			for (int i = 0; i < (accessLevel + 1 - this.groupHierarchyList.size()); i++) {
				this.groupHierarchyList.add(new LinkedList <Group> ());
			}
		}
		
		this.groupHierarchyList.get(accessLevel).add(newGroup);
	}
	
	public void add(int accessLevel, Group g) {
		
		if (accessLevel > this.groupHierarchyList.size()) { 
			//The access level isn't implying we should create a new one, e.g. the 'highest' is 5 and they entered 7.
			System.out.println("Access level out of bounds for the tree, please check your input."
					+ " FAILED > ( " + accessLevel + " < " + this.groupHierarchyList.size() + "?)");
			return;
		} else if (accessLevel < 0) {
			System.out.println("Access level out of bounds for the tree, please check your input ( accessLevel < 0)");
			return;
		}
		
		if (accessLevel+1 > this.groupHierarchyList.size()) {
			for (int i = 0; i < (accessLevel + 1 - this.groupHierarchyList.size()); i++) {
				this.groupHierarchyList.add(new LinkedList <Group> ());
			}
		}
		
		this.groupHierarchyList.get(accessLevel).add(g);
		
	}
	
	public void remove(int accessLevel, int groupUUID) {
		for (Group g : this.groupHierarchyList.get(accessLevel)) {
			if (g.getUuid() == groupUUID) this.groupHierarchyList.get(accessLevel).remove(g);
		}
		
		if (this.groupHierarchyList.get(accessLevel).size() == 0) {
			this.groupHierarchyList.remove(accessLevel);
		}
	}
	
	public void move(int groupUUID, int targetAccessLevel) {
		Group swapee;
		
		for (int i = 0; i < this.groupHierarchyList.size(); i++) {
			for (Group group : this.groupHierarchyList.get(i)) {
				if (group.getUuid() == groupUUID) {
					swapee = group;
					this.groupHierarchyList.get(i).remove(group);
					
					if (targetAccessLevel < 0) {
						System.out.println("Swap access level no good!");
					}
					
					if (targetAccessLevel + 1 > this.groupHierarchyList.size()) {
						//TODO Check if I should keep overriding the add method, this seems the most appropriate though atm
						for (int j = 0; j < (targetAccessLevel + 1 - this.groupHierarchyList.size()); j++) {
							this.groupHierarchyList.add(new LinkedList <Group> ());
						}
					}
					
					this.groupHierarchyList.get(targetAccessLevel).add(swapee);
					return;
				}
			}
		}
	}
	
	/*
	 
	public void promote(int groupUUID) {
		
	}
	
	public void demote(int groupUUID) {
		
	}
	
	public void remove(int groupUUID) {
		
	}
	
	*/
	
	public boolean isUniquegUuname(String guuname) {
		
		for (int i = 0; i < this.groupHierarchyList.size(); i++) {
			for (int j = 0; j < this.groupHierarchyList.get(i).size(); j++) {
				if (this.groupHierarchyList.get(i).get(j).getgUuname() == guuname) return false;
			}
		}
		
		return true;
	}
	
	
	public Group locateGroup(int guuid) {
		
		for (int i = 0; i < this.groupHierarchyList.size(); i++) {
			for (int j = 0; j < this.groupHierarchyList.get(i).size(); j++) {
				if (this.groupHierarchyList.get(i).get(j).getUuid() == guuid) {
					//System.out.println("\nlocateGroup > GUUID MATCH\n");
					return this.groupHierarchyList.get(i).get(j);
				}
			}
		}
		
		//System.out.println("\nlocateGroup > NO GUUID MATCH\n");
		return null;
		
	}
	
	public Group locateGroup(String guuname) {
		
		for (int i = 0; i < this.groupHierarchyList.size(); i++) {
			for (int j = 0; j < this.groupHierarchyList.get(i).size(); j++) {
				
				if (this.groupHierarchyList.get(i).get(j).getgUuname() == null) {
					return null;
				}
				
				if (this.groupHierarchyList.get(i).get(j).getgUuname().equals(guuname)) {
					//System.out.println("\nlocateGroup > GUUNAME MATCH\n");
					return this.groupHierarchyList.get(i).get(j);
				}
			}
		}
		
		//System.out.println("\nlocateGroup > NO GUUNAME MATCH\n");
		return null;
		
	}
	
	public void print() {
		
		if (!(this.groupHierarchyList.size() == 0)) {
			System.out.println("[Access Level]\t|\t[Group{UUID} {GUUNAME}]\n");
			for (int i = 0; i < this.groupHierarchyList.size(); i++) {
				System.out.print("[" + i + "] ->\t");
				
				for (int j = 0; j < this.groupHierarchyList.get(i).size(); j++) {
					System.out.print("| " + this.groupHierarchyList.get(i).get(j) 
							+ " {" 
							+ this.groupHierarchyList.get(i).get(j).getUuid() 
							+ "} {"
							+ this.groupHierarchyList.get(i).get(j).getgUuname() + "}"
							+ (j == this.groupHierarchyList.get(i).size() - 1 ? "|" : ""));
				}
				System.out.println();
			}
			
			System.out.println("\n---\t---\t---\t---\t---\t---");
			
		} else {
			System.out.println("[RACK > Group Hierarchy Tree is Empty!]");
		}
	}
	
	public static void selfTestA() {
	
		GroupHierarchyTree groupHierarchyTree = new GroupHierarchyTree();
		
		//groupHierarchyTree.add(0, "SU"); //Will be included by RACK, but not inside the hierarchy tree
		groupHierarchyTree.add(0, "Owner");
		groupHierarchyTree.add(0, "Co-owner");
		groupHierarchyTree.add(1, "Manager");
		groupHierarchyTree.add(1, "Co-Manager");
		groupHierarchyTree.add(1, "Head-of-House");
		groupHierarchyTree.add(2, "Host");
		groupHierarchyTree.add(2, "Hostess");
		
		groupHierarchyTree.print();
		
		System.out.println("\nMoving 'co-owner' to access level 2\n");
		
		groupHierarchyTree.move(301, 2);
		
		groupHierarchyTree.print();	
	}
}

public class RACK {
	
	public final String SU_OVERRIDE_PERMISSION = "rack.su_override";
	
	private final int HAS_ACCESS_VIA_SU_OVERRIDE_KEY = 199;
	private final int HAS_ACCESS_VIA_GROUP = 200;
	private final int HAS_ACCESS_VIA_DIRECT = 201;
	private final int HAS_ACCESS_VIA_ACCESS_LEVEL = 202;
	private final int HAS_ACCESS_VIA_AUTHORITY = 203;

	GroupHierarchyTree groupTree;
	
	public LinkedList <User> users;
	
	private HashMap <Integer, HashMap <String, Boolean>> groupLevelPermissions;
	private HashMap <Integer, HashMap <String, Boolean>> accessLevelPermissions;
	
	private HashMap <String, Boolean> masterPermissionsList;
	
	private int internalIncrement;
	
	public RACK() {
		this.groupTree = new GroupHierarchyTree();
		this.internalIncrement = 100;
		this.users = new LinkedList <User>();
		this.groupLevelPermissions = new HashMap <Integer, HashMap <String, Boolean>>();
		this.accessLevelPermissions = new HashMap <Integer, HashMap <String, Boolean>>();
		this.masterPermissionsList = new HashMap <String, Boolean>();	
	}
	
	public LinkedList <User> getUsers() {
		return this.users;
	}
	
	public boolean permissionExists(String permission) {
		return (this.masterPermissionsList.get(permission) != null);
	}
	
	public void grantAll(String permission) {
		
		for (User u : this.users) {
			this.grantToGroup(u.guuid, permission);
			this.grantToAccessLevel(this.groupTree.getAccessLevelFor(u.guuid), permission);
			this.grantToUser(u.getUuid(), permission);
		}
		
		this.masterPermissionsList.put(permission, true);
	}
	
	public void grantAllGroups(String permission) {
		for (User u : this.users) this.grantToGroup(u.guuid, permission);
	}
	
	public void grantAllUsers(String permission) {
		for (User u : this.users) this.grantToUser(u.getUuid(), permission);
	}
	
	public void grantAllAccessLevels(String permission) {
		for (int i = 0; i < this.groupTree.groupHierarchyList.size(); i++) this.grantToAccessLevel(i, permission);
	}
	
	public void revokeFromAll(String permission) {
		
		for (User u : this.users) {
			
			this.revokeFromAccessLevel(this.groupTree.getAccessLevelFor(u.guuid), permission);
			this.revokeFromUser(u.getUuid(), permission);
		}
		
		this.revokeFromAllGroups(permission);
		
		this.masterPermissionsList.put(permission, null);
	}
	
	public void revokeFromAllGroups(String permission) {
		
		for (User u : this.users) this.revokeFromGroup(u.guuid, permission);
		
		if (!(this.groupTree.groupHierarchyList.size() == 0)) {
			for (int i = 0; i < this.groupTree.groupHierarchyList.size(); i++) {
				
				for (int j = 0; j < this.groupTree.groupHierarchyList.get(i).size(); j++) {
					
					if (this.groupLevelPermissions.get(this.groupTree.groupHierarchyList.get(i).get(j).getUuid()) == null) {
						this.groupLevelPermissions.put(this.groupTree.groupHierarchyList.get(i).get(j).getUuid(), new HashMap <String, Boolean> ());
					}
					
					this.groupLevelPermissions.get(this.groupTree.groupHierarchyList.get(i).get(j).getUuid()).put(permission, null);							
				}		
			}	
		} else {	
		}	
	}
	
	public void revokeFromAllUsers(String permission) {
		for (User u : this.users) this.revokeFromUser(u.getUuid(), permission);
	}
	
	public void revokeFromAllAccessLevels(String permission) {
		for (int i = 0; i < this.groupTree.groupHierarchyList.size(); i++) this.revokeFromAccessLevel(i, permission);
	}
	
	public void grantToAccessLevel(int accessLevel, String permission) {
		
		if (this.accessLevelPermissions.get(accessLevel) == null) this.accessLevelPermissions.put(accessLevel, new HashMap <String, Boolean>());
		
		this.accessLevelPermissions.get(accessLevel).put(permission, true);
		this.masterPermissionsList.put(permission, true);
	}
	
	public void revokeFromAccessLevel(int accessLevel, String permission) {
		
		if (this.accessLevelPermissions.get(accessLevel) == null) return;
		
		this.accessLevelPermissions.get(accessLevel).put(permission, null);
		
		
	}
	
	public void grantToGroup(int guuid, String permission) {
		
		if (this.groupLevelPermissions.get(guuid) == null) this.groupLevelPermissions.put(guuid, new HashMap <String, Boolean>());
		
		this.groupLevelPermissions.get(guuid).put(permission, true);
		this.masterPermissionsList.put(permission, true);
		
	}
	
	public void revokeFromGroup(int guuid, String permission) {
		
		if (this.groupLevelPermissions.get(guuid) == null) return;
		
		this.groupLevelPermissions.get(guuid).put(permission, null);
	
	}
	
	public void grantToUser(int uuid, String permission) {
		
		if (this.locateUser(uuid) == null) System.out.println("USER COULD NOT BE FOUND!");
		
		this.locateUser(uuid).directAccess.put(permission, true);
		this.masterPermissionsList.put(permission, true);
	}
	
	public void grantToUser(String uuname, String permission) {
		this.locateUser(uuname).directAccess.put(permission, true);
		this.masterPermissionsList.put(permission, true);
	}
	
	public void revokeFromUser(int uuid, String permission) {
		this.locateUser(uuid).directAccess.put(permission, null);
	}
	
	public void revokeFromUser(String uuname, String permission) {
		this.locateUser(uuname).directAccess.put(permission, null);
	}
	
	public User locateUser(int uuid) {
		for (User u : this.users) if (u.getUuid() == uuid) return u;
		return null;
	}
	
	public User locateUser(String uuname) {
		for (User u : this.users) if (u.getUuname().equals(uuname)) return u;
		return null;
	}
	
	public boolean hasPermission(int uuid, String permission) {
		//check direct access, then access level, then group permissions! Can locate all of it based off of the uuid's linking each other.
		
		User u = locateUser(uuid);
		
		if (u == null) return false;
				
		/*
		 * Direct-access Check
		 */
		
	
		
		if (u.directAccess.get(SU_OVERRIDE_PERMISSION) != null) {
			return true;
		}
		
		if (u.directAccess.get(permission) != null) {
			//System.out.println("ADDED DIRECT RETURN CODE");
			return true;
		}
			
		
		
		/*
		 * Access-Level Check
		 */
		
		if (this.accessLevelPermissions.get(groupTree.getAccessLevelFor(u.guuid)) == null) {
			//System.out.println("NULL");
			this.accessLevelPermissions.put(groupTree.getAccessLevelFor(u.guuid), new HashMap <String, Boolean> ());
		}
		
		if (this.accessLevelPermissions.get(groupTree.getAccessLevelFor(u.guuid)).get(SU_OVERRIDE_PERMISSION) != null) {
			return true;
		}
		
		if (this.accessLevelPermissions.get(groupTree.getAccessLevelFor(u.guuid)).get(permission) != null) {
			return true;
		}
		
		/*
		 * Group-Level Check
		 */
		
		if (this.groupLevelPermissions.get(u.guuid) == null) {
			this.groupLevelPermissions.put(u.guuid, new HashMap <String, Boolean> ());
		}
		
		if (this.groupLevelPermissions.get(u.guuid).get(SU_OVERRIDE_PERMISSION) != null) {
			return true;
		}
		
		if (this.groupLevelPermissions.get(u.guuid).get(permission) != null) {
			return true;
		}
		
		for (int i = this.groupTree.getAccessLevelFor(u.guuid) + 1; i < this.groupTree.groupHierarchyList.size(); i++) {
			for (Group g : this.groupTree.groupHierarchyList.get(i)) {
				
				if ((this.accessLevelPermissions.get(i) == null)) {
					this.accessLevelPermissions.put(i, new HashMap <String, Boolean> ());
				}
				
				if (this.accessLevelPermissions.get(i).get(SU_OVERRIDE_PERMISSION) != null) {
					
					return true; //(HAS_ACCESS_VIA_SU_OVERRIDE_KEY);
				}
				
				if ((this.accessLevelPermissions.get(i).get(permission)) != null) {
					return true; //(HAS_ACCESS_VIA_AUTHORITY)
				}
				
				if (this.groupLevelPermissions.get(g.getUuid()) == null) {
					this.groupLevelPermissions.put(g.getUuid(), new HashMap <String, Boolean> ());
				}
				
				if (this.groupLevelPermissions.get(g.getUuid()).get(SU_OVERRIDE_PERMISSION) != null) {
					return true; //(HAS_ACCESS_VIA_SU_OVERRIDE_KEY);
				}
				
				if (this.groupLevelPermissions.get(g.getUuid()).get(permission) != null) { 
					return true; //(HAS_ACCESS_VIA_AUTHORITY);
				}
				
			}
		}
		
		return false;
	}
	
	public LinkedList <Integer> hasPermissionVia(int uuid, String permission) {
		//check direct access, then access level, then group permissions! Can locate all of it based off of the uuid's linking each other.
		
		User u = locateUser(uuid);
		
		//System.out.print(u);
		
		
		LinkedList <Integer> returnCodes = new LinkedList <Integer>();
		
		/*
		 *  SU Override key check
		 */
		
		/*
		 * Direct-access Check
		 */
		/*
		if (u == null) {
			System.out.println("USER NOT FOUND!");
		} else {
			
			if (u.directAccess.get(SU_OVERRIDE_PERMISSION) != null) {
				returnCodes.add(HAS_ACCESS_VIA_SU_OVERRIDE_KEY);
			}
			
			System.out.println("DIRECT ACCESS SIZE > " + u.directAccess.get(0));
			
			if (u.directAccess.get(permission) != null) {
				
				returnCodes.add(HAS_ACCESS_VIA_DIRECT);
				System.out.println("DIRECT ACCESS!\n\n\n\n\n\n\n");
			}
		}
		*/
		if (u == null) {
			//System.out.println("USER NOT FOUND!");
		} else {
			
			if (u.directAccess.get(SU_OVERRIDE_PERMISSION) != null) {
				returnCodes.add(HAS_ACCESS_VIA_SU_OVERRIDE_KEY);
			}
			
			if (u.directAccess.get(permission) != null) {
				//System.out.println("ADDED DIRECT RETURN CODE");
				returnCodes.add(HAS_ACCESS_VIA_DIRECT);
			}
			
		}
		
		/*
		 * Access-Level Check
		 */
		
		if (this.accessLevelPermissions.get(groupTree.getAccessLevelFor(u.guuid)) == null) {
			//System.out.println("NULL");
			this.accessLevelPermissions.put(groupTree.getAccessLevelFor(u.guuid), new HashMap <String, Boolean> ());
		}
		
		if (this.accessLevelPermissions.get(groupTree.getAccessLevelFor(u.guuid)).get(SU_OVERRIDE_PERMISSION) != null) {
			returnCodes.add(HAS_ACCESS_VIA_SU_OVERRIDE_KEY);
		}
		
		
		if (this.accessLevelPermissions.get(groupTree.getAccessLevelFor(u.guuid)).get(permission) != null) {
			returnCodes.add(HAS_ACCESS_VIA_ACCESS_LEVEL);
		}
		
		/*
		 * Group-Level Check
		 */
		
		if (this.groupLevelPermissions.get(u.guuid) == null) {
			this.groupLevelPermissions.put(u.guuid, new HashMap <String, Boolean> ());
		}
		
		if (this.groupLevelPermissions.get(u.guuid).get(SU_OVERRIDE_PERMISSION) != null) {
			returnCodes.add(HAS_ACCESS_VIA_SU_OVERRIDE_KEY);
		}
		
		if (this.groupLevelPermissions.get(u.guuid).get(permission) != null) {
			returnCodes.add(HAS_ACCESS_VIA_GROUP);
		}
		
		/*
		 *  By Authority Check
		 */
		
		for (int i = this.groupTree.getAccessLevelFor(u.guuid) + 1; i < this.groupTree.groupHierarchyList.size(); i++) {
		
			for (Group g : this.groupTree.groupHierarchyList.get(i)) {
				
					
				
				if ((this.accessLevelPermissions.get(i) == null)) {
					this.accessLevelPermissions.put(i, new HashMap <String, Boolean> ());
				}
				
				if (this.accessLevelPermissions.get(i).get(SU_OVERRIDE_PERMISSION) != null) {
					returnCodes.add(HAS_ACCESS_VIA_SU_OVERRIDE_KEY);
					i = this.groupTree.groupHierarchyList.size();
					break;
				}
				
				if ((this.accessLevelPermissions.get(i).get(permission)) != null) {
					returnCodes.add(HAS_ACCESS_VIA_AUTHORITY);
					i = this.groupTree.groupHierarchyList.size(); //TODO CHECK EARLY BREAKING
					break;
				}
				
				if (this.groupLevelPermissions.get(g.getUuid()) == null) {
					this.groupLevelPermissions.put(g.getUuid(), new HashMap <String, Boolean> ());
				}
				
				if (this.groupLevelPermissions.get(g.getUuid()).get(SU_OVERRIDE_PERMISSION) != null) {
					returnCodes.add(HAS_ACCESS_VIA_SU_OVERRIDE_KEY);
					i = this.groupTree.groupHierarchyList.size();
					break;
				}
				
				
				
				if (this.groupLevelPermissions.get(g.getUuid()).get(permission) != null) { /* children check */
					returnCodes.add(HAS_ACCESS_VIA_AUTHORITY);
					i = this.groupTree.groupHierarchyList.size();
					break;
				}
			
				
				
			}
		}
		
		return returnCodes;
	}
	
	public void testA() {
		
		
		groupTree.add(0, "Owner");
		groupTree.add(0, "Co-owner");
		groupTree.add(1, "Manager");
		groupTree.add(1, "Co-Manager");
		groupTree.add(1, "Head-of-House");
		groupTree.add(2, "Host");
		groupTree.add(2, "Hostess");
		
		groupTree.print();
		
		String testPerm = "ips.homeScreenEdit";
		
		User u = createUser("Admin");
		assignUserToGroup(u.getUuid(), 300);
		
		createUser("John");
		createUser("Adam");
		createUser("Isaac");
		createUser("Richard");
		createUser("Patrick");
		
		System.out.println(u);
		System.out.println("AXSLVL > " + this.groupTree.getAccessLevelFor(u.guuid) + "\nGroup> " + this.groupTree.getGroupNameFor(u.guuid));
		
		//this.groupTree.print();
		
		
		//grantToUser(u.uuid, testPerm);
		
		//grantToAccessLevel(groupTree.getAccessLevelFor(u.guuid), testPerm);
		
		grantToGroup(300, testPerm);
		
		
		
		//grantToUser(u.uuid, SU_OVERRIDE_PERMISSION);
		
		//System.out.println(groupTree.getAccessLevelFor(u.guuid));
		
		System.out.print("\nUser admin has access to '" + testPerm + "'? " + hasPermission(u.getUuid(), testPerm) + ", via [");
		
		
		for (int i : hasPermissionVia(u.getUuid(), testPerm)) {
			System.out.print(i + " (");
			
			if (i == HAS_ACCESS_VIA_DIRECT) {
				System.out.print("_DIRECT_");
			} else if (i == HAS_ACCESS_VIA_ACCESS_LEVEL) {
				System.out.print("_ACCESS_LEVEL_");
			} else if (i == HAS_ACCESS_VIA_GROUP) {
				System.out.print("_GROUP_");
			} else if (i == HAS_ACCESS_VIA_AUTHORITY){
				System.out.print("_AUTHORITY_");
			} else if (i == HAS_ACCESS_VIA_SU_OVERRIDE_KEY) {
				System.out.print("_SU_OVERRIDE_");
			} else {
				System.out.print("_NONE_");
			}
			System.out.print("), ");
		}
		
		System.out.print("]");
		
		
		System.out.println("\n\nRevoking test permission '" + testPerm + "'..");
		revokeFromGroup(300, testPerm);
		
		
		
		//grantToUser(u.uuid, SU_OVERRIDE_PERMISSION);
		
		//System.out.println(groupTree.getAccessLevelFor(u.guuid));
		
		System.out.print("\nUser admin has access to '" + testPerm + "?' " + hasPermission(u.getUuid(), testPerm) + ", via [");
		
		
		for (int i : hasPermissionVia(u.getUuid(), testPerm)) {
			System.out.print(i + " (");
			
			if (i == HAS_ACCESS_VIA_DIRECT) {
				System.out.print("_DIRECT_");
			} else if (i == HAS_ACCESS_VIA_ACCESS_LEVEL) {
				System.out.print("_ACCESS_LEVEL_");
			} else if (i == HAS_ACCESS_VIA_GROUP) {
				System.out.print("_GROUP_");
			} else if (i == HAS_ACCESS_VIA_AUTHORITY){
				System.out.print("_AUTHORITY_");
			} else if (i == HAS_ACCESS_VIA_SU_OVERRIDE_KEY) {
				System.out.print("_SU_OVERRIDE_");
			} else {
				System.out.print("_NONE_");
			}
			System.out.print("), ");
		}
		
		System.out.print("]");
		
		

		System.out.println("\n\nTrying grant-all\n\n");
		
		grantAllUsers(testPerm);
		
		//grantToGroup(304, testPerm);
		
		//grantToUser(101, testPerm);
		
		System.out.print("\nUser admin has access to '" + testPerm + "?' " + hasPermission(u.getUuid(), testPerm) + ", via [");
		
		
		for (int i : hasPermissionVia(u.getUuid(), testPerm)) {
			System.out.print(i + " (");
			
			if (i == HAS_ACCESS_VIA_DIRECT) {
				System.out.print("_DIRECT_");
			} else if (i == HAS_ACCESS_VIA_ACCESS_LEVEL) {
				System.out.print("_ACCESS_LEVEL_");
			} else if (i == HAS_ACCESS_VIA_GROUP) {
				System.out.print("_GROUP_");
			} else if (i == HAS_ACCESS_VIA_AUTHORITY){
				System.out.print("_AUTHORITY_");
			} else if (i == HAS_ACCESS_VIA_SU_OVERRIDE_KEY) {
				System.out.print("_SU_OVERRIDE_");
			} else {
				System.out.print("_NONE_");
			}
			System.out.print("), ");
		}
		
		System.out.print("]");
		
		
	}
	
	public int uuidFromUuname(String uuname) {
		for (User u : this.users) {
			if (u.getUuname().equals(uuname)) return u.getUuid();
		}
		
		return -1;
	}
	
	public boolean isUniqueUuname(String uuname) {
		for (User u : this.users) if (u.getUuname().equals(uuname)) return false;
		return true;
	}
	
	public String rerollUunameIfNotUnique(String uuname) {
		if (isUniqueUuname(uuname)) {
			return uuname;
		} else {
			for (int i = 0; i < Integer.MAX_VALUE; i++) {
				if (isUniqueUuname(uuname + i)) {
					return (uuname + i);
				} 
			}
		}
		
		return null;
	}
	
	public String rerollgUunameIfNotUnique(String guuname) { //TODO REROLL
		if (this.groupTree.isUniquegUuname(guuname)) {
			System.out.println("'" + guuname +"' is unique.");
			return guuname;
		} else {
			for (int i = 0; i < Integer.MAX_VALUE; i++) {
				if (isUniqueUuname(String.valueOf(guuname + i))) {
					return (guuname + i);
				} 
			}
		}
		
		return null;
	}
	
	public User createUser(String username) { //Create and return instance
		User newUser = new User();
		newUser.setFriendlyName(username);
		newUser.setUuid(this.internalIncrement++);
		newUser.guuid = -1; //Will automatically place it at the last step
		
		newUser.setUuname(rerollUunameIfNotUnique(username));
		
		this.users.add(newUser);
		return newUser;
	}
	
	public Group createGroup(int accessLevel, String groupName) {
		Group g = new Group();
		g.accessLevel = accessLevel;
		g.friendlyName = groupName;
		g.setUuid(this.groupTree.step());
		
		g.setgUuname(this.rerollgUunameIfNotUnique(groupName)); //TODO ROLL STRING
		
		//System.out.println("SET GUUNAME TO " + this.rerollgUunameIfNotUnique(groupName));
		
		groupTree.add(accessLevel, g);
		return g;
	}
	
	public LinkedList <String> getAllPermissionsForGroup(int guuid) {
		LinkedList <String> returnValues = new LinkedList <String> ();
		
		for (Entry <String, Boolean> permission : this.groupLevelPermissions.get(guuid).entrySet()) {
			String permissionKey = permission.getKey();
			Boolean accessValue = permission.getValue();
			
			if (accessValue != null) if (accessValue) returnValues.add(permissionKey);
			
			permissionKey = null;
			accessValue = null;
		}
		
		return returnValues;
	}
	
	public LinkedList <String> getAllPermissionsForUser(String uuname) {
		User u = locateUser(uuname);
		return this.getAllPermissionsForUser(u.getUuid());
	}
	
	public LinkedList <String> getAllPermissionsForUser(int uuid) {
		LinkedList <String> returnValues = new LinkedList <String> ();
		
		for (Entry <String, Boolean> entry : locateUser(uuid).directAccess.entrySet()) {
			String permissionKey = entry.getKey();
			Boolean accessValue = entry.getValue();
			
			if (accessValue != null) if (accessValue) returnValues.add("[VIA DIRECT] '" + permissionKey + "'");
			
			permissionKey = null;
			accessValue = null;
		}
		
		for (String s : this.getAllPermissionsForGroup(locateUser(uuid).guuid)) {
			if (s != null) returnValues.add("[VIA GROUP] '" + s + "'");
		}
		

		for (String s : this.getAllPermissionsForAccessLevel(locateGroup(locateUser(uuid).guuid).accessLevel)) {
			
			if (s != null) returnValues.add("[VIA AXS LVL] '" + s + "'");
		}
		
		
		return returnValues;
	}
	
	public LinkedList <String> getAllPermissionsForAccessLevel(int accessLevel) {
		LinkedList <String> returnValues = new LinkedList <String> ();
		
		for (Entry <Integer, HashMap <String, Boolean>> entry : this.accessLevelPermissions.entrySet()) {
			//Integer accessLevelKey = entry.getKey();
			HashMap <String, Boolean> permissionMapForLevel = entry.getValue();
			
			for (Entry <String, Boolean> permission : permissionMapForLevel.entrySet()) {
				String permissionKeyName = permission.getKey();
				Boolean accessValue = permission.getValue();
				
				if (accessValue != null) if (accessValue) returnValues.add(permissionKeyName);
				
				permissionKeyName = null;
				accessValue = null;
			}
			
			//accessLevelKey = null;
			permissionMapForLevel = null;
		}
		
		return returnValues;
	}
	
	public void createGroupExplicit(int accessLevel, String friendlyName, String guuname, int guuid) {
		Group g = new Group();
		
		g.accessLevel = accessLevel;
		g.friendlyName = friendlyName;
		g.setUuid(guuid);
		
		if (this.internalIncrement < guuid) this.internalIncrement = guuid;
		
		g.setgUuname(guuname); 
		
		groupTree.add(accessLevel, g);
	}
	
	public User createUserExplicit(String uuname, String friendlyName, int uuid, int guuid) {
		
		User u = new User();
		
		u.setFriendlyName(friendlyName);
		u.setUuname(uuname);
		u.setUuid(uuid);
		u.guuid = guuid;
		
		this.users.add(u);
		
		return u;
		
		//int guuid;
		//public HashMap <String, Boolean> directAccess; //List of permissions user has direct access to, regardless of group.
		
	}
	
	public void assignUserToGroup(int uuid, int guuid) { //TODO Informative prompt, possible bug for silly input (EDIT: WILL NOW DO REGARDLESS OF VALID OR NOT)
		//if (this.groupTree.isValidGUUID(guuid)) 
		this.locateUser(uuid).guuid = guuid;
	}
	
	public void assignUserToGroup(String uuname, int guuid) { //TODO Informative prompt, possible bug for silly input (EDIT: WILL NOW DO REGARDLESS OF VALID OR NOT)
		//if (this.groupTree.isValidGUUID(guuid)) 
		this.locateUser(uuname).guuid = guuid;
	}
	
	
	public void assignUserToGroup(String uuname, String guuname) {
		//TODO LOCATEGROUP
		//this.locateUser(uuname).guuid = this.locateGroup(guuname).getUuid();
		
		for (User u : this.users) {
			if (u.getUuname().equals(uuname)) u.guuid = this.groupTree.locateGroup(guuname).getUuid(); //TODO BUG
		}
		
	}
	
	public void printAccessRightsToPermission(String uuname, String permission) {
		System.out.print("\nAccess rights for {" + uuname + "(" + this.locateUser(uuname).getUuid() + "), Group={'" + this.locateGroup(this.locateUser(uuname).guuid) + "(" + this.locateGroup(this.locateUser(uuname).guuid).getUuid()  + ")}} to '" + permission + "' > \n[");
		for (int i : hasPermissionVia(this.locateUser(uuname).getUuid(), permission)) {
			System.out.print(i + " (");
			
			if (i == HAS_ACCESS_VIA_DIRECT) {
				System.out.print("_DIRECT_");
			} else if (i == HAS_ACCESS_VIA_ACCESS_LEVEL) {
				System.out.print("_ACCESS_LEVEL_");
			} else if (i == HAS_ACCESS_VIA_GROUP) {
				System.out.print("_GROUP_");
			} else if (i == HAS_ACCESS_VIA_AUTHORITY){
				System.out.print("_AUTHORITY_");
			} else if (i == HAS_ACCESS_VIA_SU_OVERRIDE_KEY) {
				System.out.print("_SU_OVERRIDE_");
			} else {
				System.out.print("_NONE_");
			}
			System.out.print("), ");
		}
		
		System.out.print("] (" + hasPermissionVia(this.locateUser(uuname).getUuid(), permission).size() + ")\n");
	}
	
	public void print() {
		System.out.println("\n--[Users]--\n");
		for (User u : this.users) { System.out.println(u); }
		System.out.println("-- -- --\n");
	}
	
	public Group locateGroup(int guuid) {
		return this.groupTree.locateGroup(guuid);
	}
	
	public Group locateGroup(String guuname) {
		return this.groupTree.locateGroup(guuname);
	}
	public void setFriendlyNameForUser(int uuid, String newFriendlyName) {
		this.locateUser(uuid).setFriendlyName(newFriendlyName);
	}
	
	public void setFriendlyNameForUser(String uuname, String newFriendlyName) {
		this.locateUser(uuname).setFriendlyName(newFriendlyName);
	}
	
	public String translateInternalCode(int ic) {
		
		if (ic == HAS_ACCESS_VIA_DIRECT) {
			return ("_DIRECT_");
		} else if (ic == HAS_ACCESS_VIA_ACCESS_LEVEL) {
			return ("_ACCESS_LEVEL_");
		} else if (ic == HAS_ACCESS_VIA_GROUP) {
			return ("_GROUP_");
		} else if (ic == HAS_ACCESS_VIA_AUTHORITY){
			return ("_AUTHORITY_");
		} else if (ic == HAS_ACCESS_VIA_SU_OVERRIDE_KEY) {
			return ("_SU_OVERRIDE_");
		} else {
			return ("_NONE_");
		}
		
	}
	
	public void getInformativeAccessInformationFor(int uuid, String permission) {
		String print = "[";
		for (int returnCode : this.hasPermissionVia(uuid, permission)) {
			print += returnCode + " (" + translateInternalCode(returnCode) + "), ";
		}
		print += "]\n";
		System.out.println("'" + uuid + "' has permission to access '" + permission + "' through access vector " + print);
	}
	
	public void getInformativeAccessInformationFor(String uuname, String permission) {
		String print = "[";
		
		if (locateUser(uuname) == null) {
			System.out.println("Unable to locate user '" + uuname + "'");
			return;
		}
		
		for (int returnCode : this.hasPermissionVia(locateUser(uuname).getUuid(), permission)) {
			print += returnCode + " (" + translateInternalCode(returnCode) + "), ";
		}
		print += "]\n";
		System.out.println("'" + uuname + "' has permission to access '" + permission + "' through access vector " + print);
	}
	
	public void printHierarchy() {
		this.groupTree.print();
	}
	
	public String getInstanceString() {
		String ret = "";
		
		/*
		 * | Users Info
		 */
		
		for (User u : this.users) {

		    ret += u.getUuname() + "~" + u.getFriendlyName() + "~" + u.getUuid() + "~" + u.guuid + " ";

		}
		
		ret += "\n";
		
		/*
		 * | Group Info 
		 */
		
		for (int i = 0; i < this.groupTree.groupHierarchyList.size(); i++) {
			for (int j = 0; j < this.groupTree.groupHierarchyList.get(i).size(); j++) {
				//System.out.println(groupHierarchyList.get(i).get(j).uuid + " == " + guuid);
				ret+= ("" 
				+ this.groupTree.groupHierarchyList.get(i).get(j).accessLevel 
				+ "~" 
				+ this.groupTree.groupHierarchyList.get(i).get(j).friendlyName
				+ "~"
				+ this.groupTree.groupHierarchyList.get(i).get(j).guuname
				+ "~"
				+ this.groupTree.groupHierarchyList.get(i).get(j).getUuid()
				+ " ") ;
			}
		}
		
		ret += "\n";
		
		/*
		 * | Group Permission Mapping
		 */
		
		//this.groupLevelPermissions.put
		
		for (Entry<Integer, HashMap<String, Boolean>> entry : this.groupLevelPermissions.entrySet()) {
			
		    Integer guuid = entry.getKey();
		    HashMap <String, Boolean> mapForGroup = entry.getValue();
		    
		    ret+= "" + guuid + "~";
		    
		    for (Entry<String, Boolean> permission : mapForGroup.entrySet()) {
		    	
		    	String permissionName = permission.getKey();
		    	Boolean permissionGranted = permission.getValue();
		    	
		    	ret += permissionName
		    			+ "~"
		    			+ permissionGranted
		    			+ "~";
		    	
		    	permissionName = null;
		    	permissionGranted = null;
		    }
		    
		    guuid = null;
		    mapForGroup = null;
		    
		    ret += " ";

		}
		
		ret += "\n";
		
		/*
	     * | Access-Level permission map
	     */
		
		for (Entry<Integer, HashMap<String, Boolean>> entry : this.accessLevelPermissions.entrySet()) {
			
		    Integer accessLevel = entry.getKey();
		    HashMap <String, Boolean> mapForLevel = entry.getValue();
		    
		    ret+= "" + accessLevel + "~";
		    
		    for (Entry<String, Boolean> permission : mapForLevel.entrySet()) {
		    	
		    	String permissionName = permission.getKey();
		    	Boolean permissionGranted = permission.getValue();
		    	
		    	ret += permissionName
		    			+ "~"
		    			+ permissionGranted
		    			+ "~";
		    	
		    	permissionName = null;
		    	permissionGranted = null;
		    }
		    
		    ret += " ";
		    
		    accessLevel = null;
		    mapForLevel = null;

		}
		
		ret += "\n";
		
		for (User u : this.users) {
			
			ret += "" + u.getUuid() + "~";
			
			for (Entry<String, Boolean> permission : u.directAccess.entrySet()) {
			    	
			    	String permissionName = permission.getKey();
			    	Boolean permissionGranted = permission.getValue();
			    	
			    	ret += permissionName
			    			+ "~"
						+ permissionGranted
						+ "~";
			    	
			    	permissionName = null;
			    	permissionGranted = null;
			}
			 
			 ret += " ";
		}
		
		
		return ret;
	}

}


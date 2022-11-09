package com.jloysch.smackandrack;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class SMACK {
	
	//DEPENDS RACK

	private final String PERMISSION_FORCE_LOGOUT = "smack.forcelogout";

	public final static Path smackFilePath = Paths.get(System.getProperty("user.home") + File.separator + "smack.mfst");
	
	private SecretKey SECRET_KEY;
	
	private RACK resourceAccessControlKit;
	private LinkedList <User> userSessions;
	private HashMap <String, String> passwordHashes;
	private LinkedList <User> userStore;
	
	public SMACK() {

		this.resourceAccessControlKit = new RACK();
		this.userSessions = new LinkedList <User>();
		this.passwordHashes = new HashMap <String, String>();
		

		/*
		 *  CONSTANT secret key generation based on hardware specs
		 */
		
		byte[] encodedKey  = Base64.getDecoder().decode(generateDirtyKey());
		
		byte[] padder = new byte[32];
		
		for (int i = 0; i < 32; i++) {
			if (i < encodedKey.length) {
				padder[i] = encodedKey[i];
			} else {
				padder[i] = 'X';
			}
			
			
		}
		
		SecretKey originalKey = new SecretKeySpec(padder, 0, padder.length, "AES");
		this.SECRET_KEY = originalKey;
		
		padder = null;
		encodedKey = null;
		originalKey = null;
		
		
		this.userStore = new LinkedList <User>();
		
	}

	public User createUser(String username) {
		return this.getRACKInstance().createUser(username);
	}
	
	public void grantForceLogoutToUser(User u) {
		this.resourceAccessControlKit.grantToUser(u.getUuid(), this.PERMISSION_FORCE_LOGOUT);
	}
	
	public void grantForceLogoutToGroup(Group g) {
		this.resourceAccessControlKit.grantToGroup(g.getUuid(), this.PERMISSION_FORCE_LOGOUT);
	}
	
	public void grantForceLogoutToAccessLevel(int accessLevel) {
		this.resourceAccessControlKit.grantToAccessLevel(accessLevel, this.PERMISSION_FORCE_LOGOUT);
	}
	
	public boolean isLoggedIn(String uuname) {
		for (User u : this.userSessions) if (u.getUuname().equals(uuname)) return true;
			
		return false;
	}
	
	public void setPassword(String uuname, String newPassword) {
		if (userExists(uuname)) this.passwordHashes.put(uuname, byteCryptToString(encrypt(newPassword)));
	}
	
	public User login(String uuname, String password) {
		
		if (isLoggedIn(uuname)) {
			System.out.println("User '" + uuname + "' already logged in.");
			return null;
		}
		
		for (User u : resourceAccessControlKit.getUsers()) { //If two users have the same name, we'll scroll through the credentials and try each combo?
			if (u.getUuname().equals(uuname)) {
				
				if (this.passwordHashes.get(u.getUuname()) == null) {
					System.out.println("No password set for user '" + uuname +"'");
					return null;
				}
				
				if (this.passwordHashes.get(u.getUuname()).equals(byteCryptToString(encrypt(password)))) {

					System.out.println("\nUser '" + u.getUuname() + " (" + u.getFriendlyName() + ")' logged in successfully.\n");

					this.userSessions.add(u);
					return u;

				} else {
					System.out.println("Incorrect combination for '" + uuname + "'");
					return null;
				}
			} 
		}
			
		System.out.println("No user found for '" + uuname + "'");
	
		
		return null;
	}
	
	public void logout(User u) {
		this.userSessions.remove(u);
	}
	
	public void logout(int uuid) {
		for (User u : this.userSessions) {
			if (u.getUuid() == uuid) this.userSessions.remove(u);
		}
	}
	
	public void logout (String uuname) {
		for (User u : this.userSessions) {
			if (u.getUuname().equals(uuname)) {
				this.userSessions.remove(u);
			}
		}
	}
	
	public void flogout(User target, User requester) {
		if (this.resourceAccessControlKit.hasPermission(requester.getUuid(), this.PERMISSION_FORCE_LOGOUT)) {
			this.userSessions.remove(target);
			System.out.println("User '" + target.getUuname() + "' ('" + target.getFriendlyName() + "') has been logged out by force by '" + requester.getUuname() + "' ('" + requester.getFriendlyName() + "')");
		} else {
			System.out.println("User '" + requester.getUuname() + "' ('" + target.getFriendlyName() + "') lacks force logout permission.");
		}
		
	}
	
	public void flogout(int targetuuid, int requesteruuid) {
		
	}
	
	public boolean userExists(String uuname) {
		for (User u : this.userStore) if (u.getUuname().equals(uuname)) return true; 
		return false;
		
		
	}
	
	public User registerUser(String username, String password) {
		
		
		if (userExists (username)) {
			System.out.println("User '" + username + "' already registered.");
			return null;
		}
		
		
		User u = this.resourceAccessControlKit.createUser(username);
		this.passwordHashes.put(u.getUuname(), byteCryptToString(encrypt(password)));
		this.userStore.add(u);
		return u;
	}
	
	private User reRegisterUser(String username, String encryptedPassword) { //Only to be used by reserializing
		User u = this.resourceAccessControlKit.createUser(username);
		this.passwordHashes.put(u.getUuname(), encryptedPassword);
		this.userStore.add(u);
		return u;
	}
	
	public void deleteUser(int uuid) {
		
	}
	
	public void deleteUser(User user) {
		
	}
	
	public RACK getRACKInstance() {
		return this.resourceAccessControlKit;
	}
	
	public void print() {
		System.out.println("[User sessions] >");
		
		for (User u : this.userSessions) System.out.println("\t[" 
		+ u.getUuid() 
		+ "\\" 
		+ u.getUuname() 
		+ "(" 
		+ u.getFriendlyName() 
		+ ")]");
		
		System.out.println("---");
	}
	
	public String generateDirtyKey() {
		String hwUUID = "";
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
			    NetworkInterface ni = networkInterfaces.nextElement();
			    byte[] hardwareAddress = ni.getHardwareAddress();
			    if (hardwareAddress != null) {
			        String[] hexadecimalFormat = new String[hardwareAddress.length];
			        for (int i = 0; i < hardwareAddress.length; i++) {
			            hexadecimalFormat[i] = String.format("%02X", hardwareAddress[i]);
			        }
			        //System.out.println(String.join("-", hexadecimalFormat));
			        
			        //System.out.println(hexadecimalFormat);
			        
			        hwUUID += String.join("", hexadecimalFormat);
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return hwUUID;
	}
	
	public String secretKeyAsString() {
		return Base64.getEncoder().encodeToString(this.SECRET_KEY.getEncoded());
	}
	
	public String decrypt(byte[] cipher) {
		
		try {
			return new String(AES.decrypt(cipher, this.SECRET_KEY),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public byte[] encrypt(String plainText) {
		
		try {
			return AES.encrypt(plainText.getBytes(), this.SECRET_KEY);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String bytesToString(byte[] bytes) { //TODO: Remove duplicate methods
		return Base64.getEncoder().encodeToString(bytes);
	}
	
	public String byteCryptToString(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}
	

	
	public String generateSmackAndRackInstanceString() {
		String ret = "";
		
		
		for (User u : this.userStore) {
			ret+= (u.getUuname() + "~" + this.passwordHashes.get(u.getUuname()) + "~");
		}
		
		ret += "\n" + this.resourceAccessControlKit.getInstanceString();
	
		return ret;
	}
	

	
public static void saveSmackAndRackInstanceToFile(SMACK smack) {


	// Custom string as an input

	// System.out.println("\nBEFORE SAVE >\n\n"  + "\n");

	// Try block to check for exceptions

	try {
		// Now calling Files.writeString() method
		// with path , content & standard charsets
		//Files.delete(smackFilePath);
		Files.createFile(smackFilePath);
		Files.writeString(smackFilePath, smack.generateSmackAndRackInstanceString(),
				StandardCharsets.UTF_8);
	}

	// Catch block to handle the exception
	catch (IOException ex) {
		// Print messqage exception occurred as
		// invalid. directory local path is passed
		/* 
		System.out.print("Unable to create manifest. Please check permissions.");
		
		*/

		
		try { 
			Files.delete(smackFilePath);
			Files.createFile(smackFilePath);
			Files.writeString(smackFilePath, smack.generateSmackAndRackInstanceString(),
					StandardCharsets.UTF_8);
		} catch (Exception e) {
			System.out.println("Unable to save manifest." + e);
		}

	}
}
	
	public static SMACK buildFromInstanceFile() {
		String dataResult = "";
		SMACK newSmack = new SMACK();
		
		//newSmack.resourceAccessControlKit = new RACK();
		
		 try {
		        // Now calling Files.writeString() method
		        // with path , content & standard charsets
		        dataResult = Files.readString(smackFilePath,
		                          StandardCharsets.UTF_8);
		    }

		    // Catch block to handle the exception
		    catch (IOException ex) {
		        // Print messqage exception occurred as
		        // invalid. directory local path is passed
		        System.out.println("SMACK > unable to read file from path\n");
				return null;
		    }
		 
		
		 
		 String[] byLine = (dataResult.split("\n"));
		 
		 if (byLine.length == 0) {
			System.out.println("\nManifest empty, returning new SMACK > '" + SMACK.smackFilePath  + "'");
			return new SMACK();
		 }

		 
		 /*
		  * | User login info
		  */
		 //TODO WTF
		 String[] userLoginInfo = byLine[0].split("~");
		
		 //System.out.println( "UDUDUDUD >> " + byLine[0] + " << UDUDUDUD << ");
		 
		 for (int i = 0; i < userLoginInfo.length-1; i+=2) {
			// newSmack.passwordHashes.put(userLoginInfo[i], userLoginInfo[i+1]);
			 
			 if (newSmack.passwordHashes == null) newSmack.passwordHashes = new HashMap <String, String> ();
			 
			 //newSmack.passwordHashes.put(userLoginInfo[i], userLoginInfo[i+1]);
			
			 newSmack.reRegisterUser(userLoginInfo[i], userLoginInfo[i+1]);
			 
			// System.out.println("Registered " + userLoginInfo[i] + ", with " + userLoginInfo[i+1]);
	
		 }
		 /*
		 for (Entry<String, String> entry : newSmack.passwordHashes.entrySet()) {
			 System.out.println(entry.getKey() + "|" + entry.getValue());
		 }
		*/

		 /*
		  * | User Info / Details (friendly name, etc)
		  */
		 
		 String[] userDetailsLine = byLine[1].split(" ");
		 String[] userDetails;
		 
		 newSmack.getRACKInstance().users = new LinkedList <User>(); //Solved weird irroneous values in the reread
		 
		 for (String s : userDetailsLine) {
			 userDetails = s.split("~");
			 //System.out.println("User details > " + s);
			// System.out.println("\nUDETAILLINE > " + s + "\n");

			if (s != null) {
				//System.out.println("[" + userDetails[0] + "," + userDetails[1] + "," + userDetails[2] + "," + userDetails[3] + "]");
				newSmack.resourceAccessControlKit.createUserExplicit(userDetails[0], userDetails[1], Integer.parseInt(userDetails[2]), Integer.parseInt(userDetails[3]));
				//System.out.println("Created " + userDetails[0]);
			}
		 }

		
		  /*
		   * | Group Info
		   */
		 
		  String[] accessLevelSpecOut = byLine[2].split(" ");
		  String[] thisUser;
		  
		  for (String s : accessLevelSpecOut) {
			  thisUser = s.split("~");
			  /*
			  for (String se : thisUser) {
				  System.out.println("\t" + se);
			  }
			  */
			  try {
				System.out.println("Group details > " + s);
				newSmack.resourceAccessControlKit.createGroupExplicit(Integer.parseInt(thisUser[0]), thisUser[1], thisUser[2], Integer.parseInt(thisUser[3]));
			  } catch (Exception e) {
				//System.out.println("Unable to parse line '" + s + "'");
			  }
			  //newSmack.resourceAccessControlKit.printHierarchy();
			 
		  
		  }
		  
		  /*
		   * | Group-Level Permissions Map
		   */
		  
		  String[] thisGroup;
		  
		  for (String s : byLine[3].split(" ")) {
			  
			  thisGroup = s.split("~");
			  
			  
			try {
			  for (int i = 1; i < thisGroup.length-1; i++) {
				  if (thisGroup[i+1].equals("true")) newSmack.resourceAccessControlKit.grantToGroup(Integer.parseInt(thisGroup[0]), thisGroup[i]);
			  }
			  
			  if (thisGroup.length==1) { //TODO Check if I want to keep doing this. Just putting null keys so we see that it's picked up by the read again.
				  newSmack.resourceAccessControlKit.grantToGroup(Integer.parseInt(thisGroup[0]), null);
			  }
			} catch (Exception e) {
				//System.out.println("Unable to parse '" + s + "'");
			}
			  
		  }
		  
		  /*
		   * | Access-Level Permissions Map
		   */
		  
		  String[] thisAccessLevel;
		  
		  for (String s : byLine[4].split(" ")) {
			  thisAccessLevel = s.split("~");
			  
			  try {
				for (int i = 1; i < thisAccessLevel.length-1; i++) {
					if (thisAccessLevel[i+1].equals("true")) newSmack.resourceAccessControlKit.grantToAccessLevel(Integer.parseInt(thisAccessLevel[0]), thisAccessLevel[i]);
				}
				
				if (thisAccessLevel.length==1) {
					newSmack.resourceAccessControlKit.grantToAccessLevel(Integer.parseInt(thisAccessLevel[0]), null);
				}
			} catch (Exception e) {
				//System.out.println("Unable to parse '" + s + "'");
			}
		  }
		  
		  /*
		   * | Direct Permissions Map
		   */
		  
		  String[] thisUsersDirectPermissions;
		  
		  for (String s : byLine[5].split(" ")) {
			  thisUsersDirectPermissions = s.split("~");

			 //int uuid = Integer.parseInt(thisUsersDirectPermissions[0]);
			  try { 
				for (int i = 1; i < thisUsersDirectPermissions.length-1; i++) {
					// System.out.println(thisUsersDirectPermissions[i]);
					
					if (thisUsersDirectPermissions[i+1].equals("true")) {
						
						
						newSmack.resourceAccessControlKit.grantToUser(Integer.parseInt(thisUsersDirectPermissions[0]), thisUsersDirectPermissions[i]);
						
						
					}
				}
			} catch (Exception e ) {
				System.out.println("Unable to parse '" + s + "'");
			}
			  
			  if (thisUsersDirectPermissions.length==1) {
				  //this.resourceAccessControlKit.grantToUser(Integer.valueOf(thisUsersDirectPermissions[0]), null);
				  
			  }
		  }
		 
		  thisUsersDirectPermissions = null;
		  thisAccessLevel = null;
		  thisGroup = null;
		  accessLevelSpecOut = null;
		  thisUser = null;
		  byLine = null;
		  userLoginInfo = null;
		  userDetailsLine = null;
		  userDetails = null;
		  
		  //this.resourceAccessControlKit.grantToUser(100, "x");
		 return newSmack;
	}
	
	public void printUsers() {
		for (User u : this.userStore) System.out.println(u);
	}

	public static void save(SMACK smack) {
		SMACK.saveSmackAndRackInstanceToFile(smack);
	}
	
}

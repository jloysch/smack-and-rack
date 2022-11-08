package com.jloysch.smackandrack;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String args[]) {
		
		SMACK smack = SMACK.buildFromInstanceFile();
		
		//SMACK smack = new SMACK();
		
		smack.setPassword("Admin", "tt");
		
		
		User u = smack.login("Admin", "tt");
		
		//u.setFriendlyName("x");
		
		SMACK.saveSmackAndRackInstanceToFile(smack);
		
		smack.print();
		
		smack.getRACKInstance().printHierarchy();
		
		System.out.println("\n");
		
		//smack.printUsers();

		
	}
}

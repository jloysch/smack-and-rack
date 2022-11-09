package com.jloysch.smackandrack;

import java.util.Scanner;

/**
 * Hello world!
 *
 */

public class App {
	private final static String[] recognized_tokens = {"create", "delete", "modify", "read", "save", "reload", "rack", "smack"};

	private static boolean isRecognizedToken(String token) {
		for (String t : recognized_tokens) if (token.equalsIgnoreCase(t)) return true;
		return false;
	}

	public static void shell() {
		Scanner sc = new Scanner(System.in);
		String input = "",  firstToken = "";
		System.out.print(">> ");
		while ((input = sc.nextLine()) != "quit") {
		
			firstToken = input.split(" ")[0];

			if (!isRecognizedToken(firstToken)) { //e.g. the first one is 'create' 'read' 'modify' etc
				System.out.println("Your input, '" + input + "'', was unrecognized.");
			} else {
				System.out.println("echo: " + input);

				//create [user/group] [uuname/guuname]

				//rack permit
			}

			System.out.print(">> ");
		}

		sc.close();
	}

    public static void main(String args[]) {

		System.out.println("\nPath for smack '" + SMACK.smackFilePath + "'\n");

		SMACK smack = SMACK.buildFromInstanceFile();
		
		if (smack == null) {
			System.out.println("Unable to load smack manifest '" + SMACK.smackFilePath + "'");
			System.out.println("Creating new instance in memory, please save to use this in the future.");
			smack = new SMACK(); /* file read error, new instance */

			smack.createUser("John");
		}

		RACK rack = smack.getRACKInstance();

		//smack.grantForceLogoutToUser(u);

		rack.print();

		SMACK.save(smack);

		shell();
		

	}
}

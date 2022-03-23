package com.abdullahteke.ysd.WlcChecker;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import expectj.ExpectJ;
import expectj.ExpectJException;
import expectj.Spawn;


/*
 * */
public class ApChecker {

	static Channel channel = null;
	static JSch jSch = null;
	static Session session = null;

	public ApChecker(String wlcIP) {
	}

	public static void main(String[] args) throws JSchException {
		String wlcIP = null;
		String apName = null;
		String ifSwName = null;

		if (args[0] != null && args[1] != null) {

			wlcIP = args[0];
			apName = args[1];
			connectToWLC(wlcIP);
			ifSwName = getConnectedInterfaceAndSwitch(apName);
			System.err.println(ifSwName);
			System.out.println("ok..");

		}

	}

	private static void connectToWLC(String wlcIP) {

		try {
			jSch = new JSch();
			session = jSch.getSession("admin", wlcIP, 22);
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setPassword("Admin123");
			session.connect();

			channel = session.openChannel("shell");
			channel.connect();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public static String getConnectedInterfaceAndSwitch(String apName) {

		String out;
		try {
			out = runCommand(apName);
			
			if (out.indexOf("Info: The AP does not exist.") < 0 && out != null && out != "") {

				String[] parts = out.split("\n");
				String[] parts2;

				String ifName = "";
				String sw = "";

				for (int i = 0; i < parts.length; i++) {
					parts2 = parts[i].split(":");
					if (parts2[0].trim().equalsIgnoreCase("Port ID")) {
						ifName = parts2[1].trim();
					} else if (parts2[0].trim().equalsIgnoreCase("System name")) {
						sw = parts2[1].trim();
					}
				}

				return sw + ":" + ifName;

			} else {

				return null;

			}
			
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block 
			e.printStackTrace();
		}
		return null;

	}

	public static String runCommand(String apName) throws TimeoutException {
		String retVal = null;

		try {
			ExpectJ e = new ExpectJ(5);
			Spawn shell = e.spawn(channel);
			shell.expect(">");
			shell.send("display ap lldp neighbor ap-name " + apName + "\r");
			shell.expect(">");
			retVal = shell.getCurrentStandardOutContents();
			shell.send("quit\r ");
			// shell.send("display ap lldp neighbor ap-name 3c78-4305-a780");

			shell.expectClose();
			channel.disconnect();
			session.disconnect();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExpectJException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (expectj.TimeoutException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return retVal;
	}

}

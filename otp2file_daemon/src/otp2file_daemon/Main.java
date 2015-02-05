package otp2file_daemon;

import java.util.concurrent.TimeUnit;

public class Main {

	public static void main(String[] args) {
		if (args.length < 8) {
			System.out.println("Not enough arguments. You need PC target, android target, URL and time! Syntax:");
			System.out
			        .println("java -jar <program.jar> <File name including full path> <Path to device location> <Key URL> <Refresh Period> <User name> <Pasword> <machine name> <Domain name>");
			System.exit(-1);
		}
		String pcTarget = args[0];
		String androidTarget = args[1];
		String URL = args[2];
		String recallTimeString = args[3];
		String ntUsername = args[4];
		String ntPassword = args[5];
		String localMachineName = args[6];
		String domainName = args[7];

		int recallTimeInt = (Integer.valueOf(recallTimeString));

		// new arguments for the constructor
		otp2file2sdcard otp = new otp2file2sdcard(pcTarget, androidTarget, URL, ntUsername, ntPassword, localMachineName, domainName);

		while (true) {
			otp.getKeyAndSendToSD();

			try {
				TimeUnit.SECONDS.sleep(recallTimeInt);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

}

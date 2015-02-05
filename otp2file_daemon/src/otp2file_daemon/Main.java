package otp2file_daemon;

import java.util.concurrent.TimeUnit;

public class Main {

	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println("Not enough arguments. You need PC target, android target, URL and time! Syntax:");
			System.out.println("java -jar <program.jar> <File name including full path> <Path to device location> <Key URL> <Refresh Period>");
			System.exit(-1);
		}
		String pcTarget = args[0];
		String androidTarget = args[1];
		String URL = args[2];
		String recallTimeString = args[3];

		int recallTimeInt = (Integer.valueOf(recallTimeString));

		// new arguments for the constructor
		otp2file2sdcard otp = new otp2file2sdcard(pcTarget, androidTarget, URL);

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

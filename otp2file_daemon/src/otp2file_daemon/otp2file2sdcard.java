package otp2file_daemon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.util.EntityUtils;

public class otp2file2sdcard {

	private String fileNamePath;
	private String fileNamePathOnDevice;
	private String siteURL;
	private String ntUsername;
	private String ntPassword;
	private String localMachineName;
	private String domainName;

	public otp2file2sdcard(String fileNamePath, String fileNamePathOnDevice, String siteURL, String ntUsername,
	        String ntPassword, String localMachineName, String domainName) {
		this.fileNamePath = fileNamePath;
		this.fileNamePathOnDevice = fileNamePathOnDevice;
		this.siteURL = siteURL;
		this.ntUsername = ntUsername;
		this.ntPassword = ntPassword;
		this.localMachineName = localMachineName;
		this.domainName = domainName;
	}

	/*
	 * this method stores the raw key from the URL, then it calls extractKey()
	 * to parse it it will print the key, then write it to the file in the PC
	 * path it will print info message that it was stored then it will call the
	 * method to move the key from the PC to the android device it will print
	 * another info message that the key was sent to android
	 */
	public void getKeyAndSendToSD() {
		String rawKey = getResponseFromSite(siteURL);
		String key = extractKey(rawKey);
		System.out.println("Key " + key + " extracted!");
		writeKeyToFile(fileNamePath, key);
		System.out.println("Key wrote to file " + fileNamePath);
		sendToDevice(fileNamePath, fileNamePathOnDevice);
		System.out.println("Key (maybe) sent do device path " + fileNamePathOnDevice);
	}

	/*
	 * retrieves the HTTP response from the specified URLif the HTTP response
	 * code is anything other than 2XX it will throw an exceptionif HTTP
	 * response is OK it will convert it to string, print it and return it
	 */
	public String getResponseFromSite(String siteURL) {
		String proxyHost = "192.168.5.125";
		int proxyPort = 8080;

		NTCredentials ntCreds = new NTCredentials(ntUsername, ntPassword, localMachineName, domainName);

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), ntCreds);
		HttpClientBuilder clientBuilder = HttpClientBuilder.create();

		clientBuilder.useSystemProperties();
		clientBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
		clientBuilder.setDefaultCredentialsProvider(credsProvider);
		clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());

		CloseableHttpClient httpclient = clientBuilder.build();

		String responseBody = "";

		try {
			HttpGet httpget = new HttpGet(siteURL);

			System.out.println("Executing request " + httpget.getRequestLine());
			// Create a custom response handlerF
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					String reason = response.getStatusLine().getReasonPhrase();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status + ". Reason: "
						        + reason);
					}
				}

			};
			try {
				responseBody = httpclient.execute(httpget, responseHandler);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			System.out.println("---------------------------------------->>");
			System.out.println(responseBody);
			System.out.println("<<----------------------------------------");

		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}

		return responseBody;
	}

	// the key retrieved from the site is returned by reading it from the 5th
	// position in string until its end
	public String extractKey(String rawkey) {
		return rawkey.substring(5);
	}

	// argument key string is written to specified file on disk
	public void writeKeyToFile(String path, String key) {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "utf-8"))) {
			writer.write(key);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	/*
	 * this method prepares the command to send the key from PC to android
	 * devicecommand syntax is
	 * "adb push <filename path from PC> <filename path to android>"it will
	 * print what the shell returns (both STDout and STDerr)if it throws an
	 * exception it will print a warning and exit the program
	 */
	private void sendToDevice(String fileNamePath, String fileNamePathOnDevice) {
		String[] command = { "adb", "push", fileNamePath, fileNamePathOnDevice };
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectErrorStream(true);// merge error stream to input stream
		Process p;
		try {
			p = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuffer output = new StringBuffer();
			String line = "";

			// get STDout and STDerr and print it
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
			System.out.println(output.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

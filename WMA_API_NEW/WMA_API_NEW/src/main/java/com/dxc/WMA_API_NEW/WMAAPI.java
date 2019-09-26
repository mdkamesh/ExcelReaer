package com.dxc.WMA_API_NEW;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

public class WMAAPI {

	public static void insertingWMAPosSubStatusData(int q) {

		Connection con = null;
		PreparedStatement preparedStmt = null;

		try {
			System.out.println("Started");
			FileInputStream fis = new FileInputStream("config.properties");
			Properties prop = new Properties();
			prop.load(fis);
			String driver = (String) prop.get("driver");
			String url = (String) prop.get("url");
			String username = (String) prop.get("username");
			String db_password = (String) prop.get("db_password");
			String wma_username = (String) prop.get("wma_username");
			String wma_Password = (String) prop.get("wma_Password");
			String wma_url = (String) prop.getProperty("wma_url");
			Class.forName(driver);
			con = DriverManager.getConnection(url, username, db_password);
			System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

			URL obj = new URL(wma_url + q);
			HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
			postConnection.setRequestMethod("POST");

			String authString = wma_username + ":" + wma_Password;
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);

			postConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			postConnection.setRequestProperty("Content-Type", "application/json");

			postConnection.setDoOutput(true);
			OutputStream os = postConnection.getOutputStream();

			int responseCode = postConnection.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				BufferedReader in = new BufferedReader(new InputStreamReader(postConnection.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}

				in.close();

				System.out.println("DB insertion Started");

				JSONObject myresponse = new JSONObject(response.toString());

				JSONArray values = myresponse.getJSONArray("Values");
				String query = "insert into WMA_API (ID, Status, Positions, Modified) values (?, ?, ?, ?)";

				preparedStmt = con.prepareStatement(query);
				for (int i = 0; i < values.length(); i++) {
					JSONArray value0 = values.getJSONArray(i);

					if (value0.get(2).toString() != "null") {

						preparedStmt.setString(1, value0.get(0).toString());
						preparedStmt.setString(2, value0.get(1).toString());
						preparedStmt.setString(3, value0.get(2).toString());
						preparedStmt.setString(4, value0.get(3).toString());
						preparedStmt.execute();
						// System.out.println("Data Stored :"+i);
					}
					con.commit();
				}
				System.out.println("Completed: Data Stored in DB");

			} else {
				System.out.println("POST NOT WORKED");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (con != null)
					con.close();
				if (preparedStmt != null)
					preparedStmt.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		try {
			if (args.length > 0) {
				int q = Integer.parseInt(args[0].toString());
				WMAAPI.insertingWMAPosSubStatusData(q);
			} else {
				System.out.println("Please Provide the arguments to get the Data from WMA");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

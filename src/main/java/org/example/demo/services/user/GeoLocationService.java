package org.example.demo.services.user;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeoLocationService {
    private static final String IP_API_URL = "http://ip-api.com/json/";

    public static String[] getLatitudeLongitude() {
        try {
            String ip = getPublicIP();
            if (ip == null) {
                return null;
            }
            JsonObject geoData = getGeoData(ip);

            if (geoData == null || !geoData.get("status").getAsString().equals("success")) {
                return null;
            }
            String latitude = geoData.get("lat").getAsString();
            String longitude = geoData.get("lon").getAsString();
            String city = geoData.get("city").getAsString();
            String country = geoData.get("country").getAsString();

            return new String[]{latitude, longitude, city, country};

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getPublicIP() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api64.ipify.org?format=json").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();
            return jsonResponse.get("ip").getAsString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JsonObject getGeoData(String ip) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(IP_API_URL + ip).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            return JsonParser.parseReader(reader).getAsJsonObject();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

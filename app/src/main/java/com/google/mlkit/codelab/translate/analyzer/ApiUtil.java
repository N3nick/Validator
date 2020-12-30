package com.google.mlkit.codelab.translate.analyzer;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;

import android.provider.Settings.Secure;
import android.util.Log;

public class ApiUtil {
    private ApiUtil() {}

    public static final String BASE_API_URL =
            "https://transport.palmkash.com/ticket-boarding";

    public static URL buildUrl (String ticket_number) {
        String device_id = UUID.randomUUID().toString();
        String fullUrl = BASE_API_URL+ "?device_id=" + device_id + "&ticket_number=" + ticket_number;
        URL url = null;

        try {
            url = new URL(fullUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getJson(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {

            InputStream stream = connection.getInputStream();
            Scanner scanner = new Scanner(stream);
            scanner.useDelimiter("//A");

            boolean hasData = scanner.hasNext();

            if (hasData) {
                return scanner.next();
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.d("Error", e.toString());
            return null;
        }

        finally {
            connection.disconnect();
        }
    }
}



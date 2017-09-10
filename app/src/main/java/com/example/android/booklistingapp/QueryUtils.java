package com.example.android.booklistingapp;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getName();

    private QueryUtils() {}

    public static List<Book> fetchBookQuery(String booksBaseUrl, String searchQuery, String key) {

        try {
            searchQuery = URLEncoder.encode(searchQuery, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "Problem encoding search query", e);
        }

        URL url = createUrl(booksBaseUrl + searchQuery + key);
        Log.v(LOG_TAG, "-> fetchBookQuery -> " + url.toString());

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        List<Book> books = extractFeatureFromJson(jsonResponse);

        return books;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static List<Book> extractFeatureFromJson(String jsonResponse) {

        if(TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        List<Book> books = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);
            JSONArray itemsArray = baseJsonResponse.has("items") ? baseJsonResponse.optJSONArray("items") : new JSONArray();

            for( int i=0 ; i<itemsArray.length() ; i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                JSONObject volumeInfo = item.has("volumeInfo") ? item.getJSONObject("volumeInfo") : new JSONObject();
                String title = volumeInfo.optString("title");

                JSONArray authorsArray = volumeInfo.has("authors") ? volumeInfo.getJSONArray("authors") : new JSONArray();
                String authors = new String();
                for( int j=0 ; j< authorsArray.length() ; j++)
                    authors = authors + authorsArray.getString(j) + ", ";
                int lastIndexOf = authors.lastIndexOf(',');
                lastIndexOf = lastIndexOf == -1 ? 0 : lastIndexOf;
                authors = authors.substring(0, lastIndexOf);

                String url = volumeInfo.optString("previewLink");

                books.add(new Book(title, authors, url));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem extracting feature from JSON", e);
        }

        return books;
    }
}

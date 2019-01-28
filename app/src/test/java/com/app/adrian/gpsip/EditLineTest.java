package com.app.adrian.gpsip;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class EditLineTest {
    String GET_STOPS_POINT_URL = "http://ux.up.krakow.pl/~adrian.ozog/json/test/stopPoints";
    String GET_STOPS_URL = "http://ux.up.krakow.pl/~adrian.ozog/json/test/stops";
    String GET_LINES_URL = "http://ux.up.krakow.pl/~adrian.ozog/json/test/lines";
    String GET_STOPS_MODIFY_URL = "http://ux.up.krakow.pl/~adrian.ozog/json/test/stopsModify";
    String GET_STOPS_POINT_MODIFY_URL = "http://ux.up.krakow.pl/~adrian.ozog/json/test/stopPointsModify";
    MainActivity mainActivity;
    EditLine editLine;
    ArrayList<String> stopsNames = new ArrayList<>();
    @Before
    public void setUp() throws Exception {
        mainActivity = new MainActivity();
        mainActivity.inTest = true;
        editLine = new EditLine();
        dowloadJSON();
        mainActivity.generateLinesList();
        stopsNames.add("Kraków Zabłocie");
        stopsNames.add("Kraków Podgórze");
    }
    private void dowloadJSON(){
        JSONObject tempJson = null;
        URL url = null;
        URLConnection connection = null;
        InputStream is = null;
        try {
            url = new URL(GET_LINES_URL);
            connection = url.openConnection();
            is = connection.getInputStream();
            tempJson = new JSONObject(mainActivity.streamToString(is));
            mainActivity.jsonLines = tempJson.getJSONArray("routes");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            url = new URL(GET_STOPS_URL);
            connection = url.openConnection();
            is = connection.getInputStream();
            tempJson = new JSONObject(mainActivity.streamToString(is));
            mainActivity.jsonStops = tempJson.getJSONArray("stops");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            url = new URL(GET_STOPS_POINT_URL);
            connection = url.openConnection();
            is = connection.getInputStream();
            tempJson = new JSONObject(mainActivity.streamToString(is));
            mainActivity.jsonStopsPoint = tempJson.getJSONArray("stopPoints");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            url = new URL(GET_STOPS_MODIFY_URL);
            connection = url.openConnection();
            is = connection.getInputStream();
            tempJson = new JSONObject(mainActivity.streamToString(is));
            mainActivity.jsonStopsModify = tempJson.getJSONArray("stops");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            url = new URL(GET_STOPS_POINT_MODIFY_URL);
            connection = url.openConnection();
            is = connection.getInputStream();
            tempJson = new JSONObject(mainActivity.streamToString(is));
            mainActivity.jsonStopsPointModify = tempJson.getJSONArray("stopPoints");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test1_generateStops() {
        editLine.originalLine = mainActivity.lines.get(10);


        editLine.firstNumberStop = 1;
        editLine.lastNumberStop = 2;
        ArrayList<Stop> stops = editLine.generateStops();
        assertEquals(stops.size(), stopsNames.size());
        for(int i=0; i<stops.size(); i++){
            assertEquals(stops.get(i).getName(), stopsNames.get(i));
        }

    }
}
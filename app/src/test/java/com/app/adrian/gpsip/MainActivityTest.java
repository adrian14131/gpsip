package com.app.adrian.gpsip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static org.junit.Assert.*;
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MainActivityTest {
    MainActivity mainActivity;
    String urlAddress = "http://ux.up.krakow.pl/~adrian.ozog/json/test/test";
    String text = "test1\n" +
            "test2\n" +
            "test3\n";
    String GET_STOPS_POINT_URL = "http://ux.up.krakow.pl/~adrian.ozog/json/test/stopPoints";
    String GET_STOPS_URL = "http://ux.up.krakow.pl/~adrian.ozog/json/test/stops";
    String GET_LINES_URL = "http://ux.up.krakow.pl/~adrian.ozog/json/test/lines";
    String GET_STOPS_MODIFY_URL = "http://ux.up.krakow.pl/~adrian.ozog/json/test/stopsModify";
    String GET_STOPS_POINT_MODIFY_URL = "http://ux.up.krakow.pl/~adrian.ozog/json/test/stopPointsModify";
    ArrayList<String> sortedNumericLines = new ArrayList<>();
    ArrayList<String> sortedTextLines = new ArrayList<>();
    ArrayList<Track> tracks = new ArrayList<>();
    String jsonContent = "{\n" +
            "   \"stops\":[\n" +
            "\t\t{\n" +
            "\t\t\t\"shortName\": \"88\",\n" +
            "\t\t\t\"name\": \"UP\",\n" +
            "\t\t\t\"say\": \"Uniwersytet Pedagogiczny\"\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"shortName\": \"MKA2\",\n" +
            "\t\t\t\"name\": \"Kraków Podgórze\"\n" +
            "\t\t}\n" +
            "   ]\n" +
            "}";
    @Before
    public void setUp() throws Exception {
        mainActivity = new MainActivity();
        mainActivity.jsonLines = new JSONArray();
        mainActivity.jsonStops = new JSONArray();
        mainActivity.jsonStopsModify = new JSONArray();
        mainActivity.jsonStopsPoint = new JSONArray();
        mainActivity.jsonStopsPointModify = new JSONArray();
        sortedNumericLines.add("1");
        sortedNumericLines.add("6");
        sortedNumericLines.add("41");
        sortedTextLines.add("A1");
        sortedTextLines.add("A3");
        sortedTextLines.add("SKA1");
        mainActivity.inTest = true;
        tracks.add(new Track(1, "Uniwersytet Pedagogiczny", "Uniwersytet Pedagogiczny"));
        tracks.add(new Track(1, "Biprostal", "Biprostal"));
        tracks.add(new Track(6, "Uniwersytet Pedagogiczny", "Uniwersytet Pedagogiczny"));
        tracks.add(new Track(6, "Biprostal", "Biprostal"));
        tracks.add(new Track(41, "UP", "Uniwersytet Pedagogiczny"));
        tracks.add(new Track(41, "Biprostal", "Biprostal"));
        tracks.add(new Track("A1", "Kraków Podgórze", "Kraków Podgórze"));
        tracks.add(new Track("A1", "Kraków Główny", "Kraków Główny"));
        tracks.add(new Track("A3", "Kraków Podgórze", "Kraków Podgórze"));
        tracks.add(new Track("A3", "Kraków Główny", "Kraków Główny"));
        tracks.add(new Track("SKA1", "Kraków Podgórze", "Kraków Podgórze"));
        tracks.add(new Track("SKA1", "Kraków Główny", "Kraków Główny"));
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
    public void test1_isNumeric() {
        assertFalse(mainActivity.isNumeric("test123"));
        assertFalse(mainActivity.isNumeric("123test"));
        assertTrue(mainActivity.isNumeric("123"));
    }
    @Test
    public void test2a_streamToString() {
        try {
            URL url = new URL(urlAddress);
            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();
            assertEquals(mainActivity.streamToString(is),text);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test2b_equalJsonString(){
        JSONObject tempJson = null;
        URL url = null;
        URLConnection connection = null;
        InputStream is = null;
        try {
            url = new URL(GET_STOPS_MODIFY_URL);
            connection = url.openConnection();
            is = connection.getInputStream();
            tempJson = new JSONObject(mainActivity.streamToString(is));
            assertTrue(mainActivity.equalJsonString(tempJson, jsonContent));
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
    public void test3_downloadedCorrectly(){
        assertTrue(mainActivity.downloadedCorrectly());
        mainActivity.jsonLines = null;
        assertFalse(mainActivity.downloadedCorrectly());
        dowloadJSON();
        assertTrue(mainActivity.downloadedCorrectly());
    }

    @Test
    public void test4a_getStopSayName(){
        dowloadJSON();
        assertEquals(mainActivity.getStopSayName(mainActivity.jsonStopsModify, "88"), "Uniwersytet Pedagogiczny");
        assertEquals(mainActivity.getStopSayName(mainActivity.jsonStopsModify, "MKA2"), null);
        assertNotEquals(mainActivity.getStopSayName(mainActivity.jsonStopsModify, "88"), "UP");
        assertNotEquals(mainActivity.getStopSayName(mainActivity.jsonStopsModify, "MKA2"), "Kraków Podgórze");
        assertNotEquals(mainActivity.getStopSayName(mainActivity.jsonStopsModify, "MKA2"), "Uniwersytet Pedagogiczny");
    }
    //getStopPoint(jsonStopsPoint, routeStops.getString(j));
    @Test
    public void test4b_getStopPoint(){
        dowloadJSON();
        try {
            JSONObject jsonObject = mainActivity.getStopPoint(mainActivity.jsonStopsPoint, "8819");
            assertEquals(jsonObject.getString("stopPoint"), "8819");
            assertEquals(jsonObject.getString("shortName"), "88");
            assertEquals(jsonObject.getString("name"), "Uniwersytet Pedagogiczny (8819)");
            jsonObject = mainActivity.getStopPoint(mainActivity.jsonStopsPoint, "MKA211");
            assertEquals(jsonObject.getString("stopPoint"), "MKA211");
            assertEquals(jsonObject.getString("shortName"), "MKA2");
            assertEquals(jsonObject.getString("name"), "Kraków Krzemionki (MKA211)");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test4c_getStopName(){
        dowloadJSON();
        //getStopName(jsonStops, stopPoint.getString("shortName")
        assertEquals(mainActivity.getStopName(mainActivity.jsonStops, "88"), "Uniwersytet Pedagogiczny");
        assertEquals(mainActivity.getStopName(mainActivity.jsonStops, "MKA1"), "Kraków Główny");

    }
    @Test
    public void test5_searchStopModify(){
        dowloadJSON();
        //searchStopModify(JSONArray stopsModify, String shortName)
        try{
            JSONObject jsonObject = mainActivity.searchStopModify(mainActivity.jsonStopsModify, "88");
            assertEquals(jsonObject.getString("shortName"), "88");
            assertEquals(jsonObject.optString("name"), "UP");
            assertEquals(jsonObject.optString("say"), "Uniwersytet Pedagogiczny");
            jsonObject = mainActivity.searchStopModify(mainActivity.jsonStopsModify, "MKA2");
            assertEquals(jsonObject.getString("shortName"), "MKA2");
            assertEquals(jsonObject.optString("name"), "Kraków Podgórze");
            assertEquals(jsonObject.optString("say"), "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test6_getLinesFromJson(){
        dowloadJSON();
        ArrayList<JSONObject> lines = mainActivity.getLinesFromJson(mainActivity.jsonLines, true);
        for (JSONObject jsonObject: lines) {

            try {
                assertEquals(mainActivity.isNumeric(jsonObject.getString("name")), true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        lines = mainActivity.getLinesFromJson(mainActivity.jsonLines, false);
        for (JSONObject jsonObject: lines) {

            try {
                assertEquals(mainActivity.isNumeric(jsonObject.getString("name")), false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @Test
    public void test7a_sortNumericLines(){
        dowloadJSON();
        ArrayList<JSONObject> lines = mainActivity.getLinesFromJson(mainActivity.jsonLines, true);
        ArrayList<JSONObject> sortedLines = mainActivity.sortNumericLines(lines);
        for(int i=0; i<sortedLines.size(); i++){
            JSONObject object = sortedLines.get(i);
            try {
                assertEquals(object.getString("name"), sortedNumericLines.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @Test
    public void test7b_sortTextLines(){
        dowloadJSON();
        ArrayList<JSONObject> lines = mainActivity.getLinesFromJson(mainActivity.jsonLines, false);
        ArrayList<JSONObject> sortedLines = mainActivity.sortTextLines(lines);
        for(int i=0; i<sortedLines.size(); i++){
            JSONObject object = sortedLines.get(i);
            try {
                assertEquals(object.getString("name"), sortedTextLines.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @Test
    public void test8_generateLinesList(){
        dowloadJSON();
        mainActivity.generateLinesList();
        assertEquals(mainActivity.getLines().size(), tracks.size());
        for(int i=0; i<mainActivity.getLines().size(); i++){
            Track trackFromClass = mainActivity.getLines().get(i);
            Track trackFromTestClass = tracks.get(i);
            //System.out.println(trackFromClass.getLine());
            assertEquals(String.valueOf(trackFromClass.getLine()), String.valueOf(trackFromTestClass.getLine()));
            assertEquals(trackFromClass.getDirection(), trackFromTestClass.getDirection());
            assertEquals(trackFromClass.getSayDirection(), trackFromTestClass.getSayDirection());
        }
    }

}
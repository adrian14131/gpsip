package com.app.adrian.gpsip;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    JSONArray jsonStops, jsonStopsPoint, jsonLines, jsonStopsModify, jsonStopsPointModify;
    private int index = 0;
    private TextView infoLabel, stopNameView, speedView, distanceView, gpsLatitudeView, gpsLongitudeView, gpsAccuracyView, stopLatitudeView, stopLongitudeView;
    private Button prevButton, nextButton, startButton, exitButton, lineButton, setButton;
    private EditText distanceEditText;
    private File internalStorageDir;
    File stopsPointJsonFile, stopsJsonFile, lineJsonFile, stopsModifyJsonFile, stopsPointModifyJsonFile;
    private String stopsJsonContent, stopsPointJsonContent, linesJsonContent, stopsModifyJsonContent, stopsPointModifyJsonContent;
    private boolean isRequireUpdate = false, emptyData = false, lineIsChoose = false, isStarted = false;
    ArrayList<Track> lines = new ArrayList<>();
    private Track currentLine;
    private TrackThread trackThread;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean wasStart = false;
    private android.support.v7.app.ActionBar actionBar;
    private double defaultDistance = 50.0;
    public static final int GET_VALUE_CODE = 123;
    public static final String GET_STOPS_POINT_URL = "http://www.ttss.krakow.pl/internetservice/geoserviceDispatcher/services/stopinfo/stopPoints?left=-648000000&bottom=-324000000&right=648000000&top=324000000";
    public static final String GET_STOPS_URL = "http://www.ttss.krakow.pl/internetservice/geoserviceDispatcher/services/stopinfo/stops?left=-648000000&bottom=-324000000&right=648000000&top=324000000";
    public static final String GET_LINES_URL = "http://ux.up.krakow.pl/~adrian.ozog/json/lines";
    public static final String GET_STOPS_MODIFY_URL = "http://ux.up.krakow.pl/~adrian.ozog/json/stopsModify";
    public static final String GET_STOPS_POINT_MODIFY_URL = "http://ux.up.krakow.pl/~adrian.ozog/json/stopPointsModify";
    private int lastNumberStop = -1;
    public int percent = 0;
    boolean inInstrumentedTest = false;
    boolean inTest = false;
    boolean networkInTest = true;
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        Log.e("OnCreateView", "CREATE "+name);
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("MainActivity", "StartAdd");

        setContentView(R.layout.activity_main);
        Intent testIntent = getIntent();
        if(testIntent!=null && testIntent.getStringExtra("mode")!=null && testIntent.getStringExtra("mode").equals("Test")){
            inInstrumentedTest = testIntent.getBooleanExtra("inInstrumentedTest", false);
            networkInTest = testIntent.getBooleanExtra("networkInTest", true);
        }
        infoLabel = findViewById(R.id.infoLabel);
        infoLabel.setText("Ładowanie");
        stopNameView = findViewById(R.id.stopNameView);
        speedView = findViewById(R.id.speedView);
        distanceView = findViewById(R.id.distanceView);
        gpsLatitudeView = findViewById(R.id.gpsLatitudeView);
        gpsLongitudeView = findViewById(R.id.gpsLongitudeView);
        gpsAccuracyView = findViewById(R.id.gpsAccuracyView);
        stopLatitudeView = findViewById(R.id.stopLatitudeView);
        stopLongitudeView = findViewById(R.id.stopLongitudeView);
        prevButton = findViewById(R.id.previousButton);
        prevButton.setEnabled(false);
        nextButton = findViewById(R.id.nextButton);
        nextButton.setEnabled(false);
        startButton = findViewById(R.id.startButton);
        startButton.setEnabled(false);
        exitButton = findViewById(R.id.stopButton);
        lineButton = findViewById(R.id.chooseLineButton);
        lineButton.setEnabled(false);
        setButton = findViewById(R.id.setButton);
        setButton.setEnabled(false);
        distanceEditText = findViewById(R.id.distanceEditText);
        distanceEditText.setHint(String.valueOf(defaultDistance));
        actionBar = getSupportActionBar();
        changeActionBar();
        internalStorageDir = this.getFilesDir();

        stopsJsonFile = new File(internalStorageDir, "stops.JSON");
        stopsPointJsonFile = new File(internalStorageDir, "stopsPoint.JSON");
        lineJsonFile = new File(internalStorageDir, "lines.JSON");
        stopsModifyJsonFile = new File(internalStorageDir, "stopsModify.JSON");
        stopsPointModifyJsonFile = new File(internalStorageDir, "stopPointsModify");
        isRequireUpdate = false;
        Log.e("MainActivity","OnCreate");
        if (networkIsActive()) {
            DownloadTask downloadJson = new DownloadTask();
            try {
                stopsJsonContent = downloadJson.execute(GET_STOPS_URL).get();
                percent = 20;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            downloadJson = new DownloadTask();
            try {
                stopsPointJsonContent = downloadJson.execute(GET_STOPS_POINT_URL).get();
                percent = 40;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            downloadJson = new DownloadTask();
            try {
                linesJsonContent = downloadJson.execute(GET_LINES_URL).get();
                percent = 60;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            downloadJson = new DownloadTask();
            try {
                stopsModifyJsonContent = downloadJson.execute(GET_STOPS_MODIFY_URL).get();
                percent = 80;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            downloadJson = new DownloadTask();
            try {
                stopsPointModifyJsonContent = downloadJson.execute(GET_STOPS_POINT_MODIFY_URL).get();
                percent = 100;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if (stopsPointJsonContent != null && stopsJsonContent != null && linesJsonContent != null && stopsModifyJsonContent!=null && stopsPointModifyJsonContent!=null) {
                JSONObject tempJson = null;
                try {
                    tempJson = new JSONObject(stopsJsonContent);
                    jsonStops = tempJson.getJSONArray("stops");
                    tempJson = new JSONObject(stopsPointJsonContent);
                    jsonStopsPoint = tempJson.getJSONArray("stopPoints");
                    tempJson = new JSONObject(linesJsonContent);
                    jsonLines = tempJson.getJSONArray("routes");
                    tempJson = new JSONObject(stopsModifyJsonContent);
                    jsonStopsModify = tempJson.getJSONArray("stops");
                    tempJson = new JSONObject(stopsPointModifyJsonContent);
                    jsonStopsPointModify = tempJson.getJSONArray("stopPoints");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        } else {
            isRequireUpdate = false;
            Toast.makeText(this, "Brak połączenia z siecią", Toast.LENGTH_SHORT).show();
        }

        JSONObject tempStopsJson = null;
        JSONObject tempStopsPointJson = null;
        JSONObject tempLinesJson = null;
        JSONObject tempStopsModifyJson = null;
        JSONObject tempStopsPointModifyJson = null;
        if (areRequireFiles()) {

            try {
                tempStopsJson = new JSONObject(streamToString(new FileInputStream(stopsJsonFile)));
                tempStopsPointJson = new JSONObject(streamToString(new FileInputStream(stopsPointJsonFile)));
                tempLinesJson = new JSONObject(streamToString(new FileInputStream(lineJsonFile)));
                tempStopsModifyJson = new JSONObject(streamToString(new FileInputStream(stopsModifyJsonFile)));
                tempStopsPointModifyJson = new JSONObject(streamToString(new FileInputStream(stopsPointModifyJsonFile)));

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            if (!stopsJsonFile.exists()) {
                try {
                    stopsJsonFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!stopsPointJsonFile.exists()) {
                try {
                    stopsPointJsonFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!lineJsonFile.exists()) {
                try {
                    lineJsonFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(!stopsModifyJsonFile.exists()){
                try {
                    stopsModifyJsonFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(!stopsPointModifyJsonFile.exists()){
                try {
                    stopsPointModifyJsonFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        if (areRequireFiles()) {
            if (tempStopsJson != null && tempStopsPointJson != null && tempLinesJson != null && tempStopsModifyJson != null && tempStopsPointModifyJson != null) {
                if (downloadedCorrectly()) {
                    if (!equalJsonString(tempStopsJson, stopsJsonContent) || !equalJsonString(tempStopsPointJson, stopsPointJsonContent) || !equalJsonString(tempLinesJson, linesJsonContent) || !equalJsonString(tempStopsModifyJson, stopsModifyJsonContent) || !equalJsonString(tempStopsPointModifyJson, stopsPointModifyJsonContent)) {
                        infoLabel.setText("Czy zaktualizować dane?");
                        isRequireUpdate = true;

                    }
                } else {
                    try {
                        jsonStops = tempStopsJson.getJSONArray("stops");
                        jsonStopsPoint = tempStopsPointJson.getJSONArray("stopPoints");
                        jsonLines = tempLinesJson.getJSONArray("routes");
                        jsonStopsModify = tempStopsModifyJson.getJSONArray("stops");
                        jsonStopsPointModify = tempStopsPointModifyJson.getJSONArray("stopPoints");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            } else if(downloadedCorrectly()){
                infoLabel.setText("Brak danych. Czy pobrać dane?");
                isRequireUpdate = true;
                emptyData = true;
            }
        }
        if (isUpdate()&&downloadedCorrectly()) {
            prevButton.setEnabled(true);
            prevButton.setText("Tak");
            nextButton.setEnabled(true);
            nextButton.setText("Nie");
        } else {
            if (downloadedCorrectly()) {
                infoLabel.setText("Załadowano dane");
            } else {
                infoLabel.setText("Brak danych");
            }
        }
        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        lineButton.setOnClickListener(this);
        startButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);
        setButton.setOnClickListener(this);
        if (!isUpdate() && downloadedCorrectly()) {
            generateLinesList();
        }
        trackThread = new TrackThread(currentLine, this);
        trackThread.setMinDistance(defaultDistance);
        trackThread.start();
        setButton.setEnabled(true);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                trackThread.setCordinateSet(true);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                double speed = location.getSpeed();
                speed = speed * 3.6;
                speed = Math.floor(speed * 100)/100;
                trackThread.setCurrentLatitude(latitude);
                trackThread.setCurrentLongitude(longitude);
                //Log.e("GPS","Log: "+longitude+" lat: "+latitude);
                speedView.setText(speed+" km/h");
                if(trackThread.getCurrentStop()!=null){
                    trackThread.setDistance(trackThread.calculateDistance(trackThread.getCurrentStop().getLatitude(), trackThread.getCurrentStop().getLongitude(), latitude, longitude));
                    //Log.e("GPS","CalculateMain");
                    updateDistance();
                }

                //updateDistance();
                gpsLatitudeView.setText("Szerokość: "+latitude);
                gpsLongitudeView.setText("Długość: "+longitude);
                gpsAccuracyView.setText("Dokładność: "+location.getAccuracy()+" m");

                //location.getSpeed();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(gpsIntent);
            }
        };
        configureGPS();
    }
    public boolean isEmptyData(){
        return emptyData;
    }
    public boolean isUpdate(){
        return isRequireUpdate;
    }
    public void buttonEnabler(){
        if(wasStart&&trackThread.isEnabledButton()&&trackThread.getLastNumberStop()!=trackThread.getNumberOfStop()){
            if(trackThread.getNumberOfStop()==0){
                prevButton.setEnabled(false);
                nextButton.setEnabled(true);
            }
            else if(trackThread.getNumberOfStop()>0&&trackThread.getNumberOfStop()<trackThread.getCurrentLine().getStops().size()-1){
                prevButton.setEnabled(true);
                nextButton.setEnabled(true);
            }
            else if(trackThread.getNumberOfStop()==trackThread.getCurrentLine().getStops().size()-1){
                prevButton.setEnabled(true);
                nextButton.setEnabled(false);
            }
            lastNumberStop = trackThread.getNumberOfStop();
        }

    }
    public void toInfoLabel(String str){
        infoLabel.setText(str);
    }
    public void updateStopInfo(boolean startSay){
        if(startSay){
            nextButton.setEnabled(false);
        }
        else{
            stopNameView.setText((trackThread.getNumberOfStop()+1)+" z "+trackThread.getCurrentLine().getStops().size()+" "+trackThread.getCurrentStop().getName());
            stopLatitudeView.setText("Szerokość: "+trackThread.getCurrentStop().getLatitude());
            stopLongitudeView.setText("Długość: "+trackThread.getCurrentStop().getLongitude());
            buttonEnabler();
        }

    }
    public void updateDistance(){
        distanceView.setText(trackThread.getDistance()+" m");
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case 10:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    configureGPS();

                }
                break;
            default: break;
        }
    }

    @SuppressLint("MissingPermission")
    private void configureGPS() {
        Log.e("GPS", "Żądanie GPS");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
            }
            return;
        }
        locationManager.requestLocationUpdates("gps", 1000, 0, locationListener);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle extra;
        if(requestCode==GET_VALUE_CODE){
            if(resultCode == Activity.RESULT_OK&&data!=null){
                extra = data.getBundleExtra("linesBundle");
                wasStart = false;
                currentLine = (Track) extra.getSerializable("lines");
                infoLabel.setText(currentLine.getLine()+" -> "+currentLine.getDirection());
                startButton.setEnabled(true);
                prevButton.setEnabled(false);
                nextButton.setEnabled(false);
                trackThread.setCurrentLine(currentLine);
            }
        }
    }



    @VisibleForTesting
    boolean networkIsActive(){
        boolean active=false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni!=null){
            active=ni.isConnected() && ni.isAvailable();
        }
        if(inInstrumentedTest){
            if(networkInTest==false){
                active = networkInTest;
            }

        }
       System.out.println("networkIsActive: "+active);
        return active;
    }
    @VisibleForTesting
    boolean downloadedCorrectly(){
        if(jsonStops!=null&&jsonStopsPoint!=null&&jsonLines!=null&&jsonStopsModify!=null&&jsonStopsPointModify!=null){
            return true;
        }
        else{
            return false;
        }
    }
    @VisibleForTesting
    boolean areRequireFiles(){
        if(stopsPointJsonFile.exists()&&stopsPointJsonFile.exists()&&lineJsonFile.exists()&&stopsModifyJsonFile.exists()&&stopsPointModifyJsonFile.exists()){
            return true;
        }
        else{
            return false;
        }
    }
    @VisibleForTesting
    boolean equalJsonString(JSONObject jsonObject, String jsonString){
        JSONObject tempJson = null;

        try {
            tempJson = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(jsonObject!=null&&tempJson!=null){
            String jsonFirst = jsonObject.toString();
            String jsonSecond = tempJson.toString();
            if(jsonFirst.equals(jsonSecond)){
                return true;
            }
        }
        return false;
    }

    @VisibleForTesting
    void generateLinesList(){
        ArrayList<JSONObject> tempNumbersLines = getLinesFromJson(jsonLines, true);
        ArrayList<JSONObject> tempNamesLines = getLinesFromJson(jsonLines, false);
        /*for(int i=0;i<jsonLines.length();i++){
            JSONObject element = jsonLines.optJSONObject(i);
            if(element!=null){
                try {
                    if(isNumeric(element.getString("name"))){
                        tempNumbersLines.add(element);
                    }
                    else{
                        tempNamesLines.add(element);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }*/
        ArrayList<JSONObject> sortedNumbersLines = sortNumericLines(tempNumbersLines);
        ArrayList<JSONObject> sortedNamesLines = sortTextLines(tempNamesLines);
        Iterator<JSONObject> iterator = sortedNumbersLines.iterator();
        while(iterator.hasNext()){
            JSONObject element = iterator.next();
			
            try {
                int line = Integer.parseInt(element.getString("name"));
                JSONArray routes = element.optJSONArray("directions");
                if(routes.length()>0){

                    for(int i=0;i<routes.length();i++){
                        JSONObject route = routes.optJSONObject(i);
                        String direction = route.getString("direction");
                        String sayDirection = direction;
                        if(route.has("sayDirection")){
                            sayDirection=route.getString("sayDirection");
                            String temp = getStopSayName(jsonStopsModify, sayDirection);
                            if(temp!=null){
                                sayDirection = temp;
                            }

                        }
                        Track<Integer> track = new Track<Integer>(line, direction, sayDirection);

                        JSONArray routeStops = route.optJSONArray("routeStops");
                        for(int j=0;j<routeStops.length();j++){
                            JSONObject stopPoint = getStopPoint(jsonStopsPoint, routeStops.getString(j));

                            if(stopPoint!=null){
                                String name = getStopName(jsonStops, stopPoint.getString("shortName"));
                                String sayName = name;
                                JSONObject stopModifier = searchStopModify(jsonStopsModify, stopPoint.getString("shortName"));
                                JSONObject stopPointModifier = getStopPoint(jsonStopsPointModify, routeStops.getString(j));
                                double latitude = (double)stopPoint.getInt("latitude")/3600000.0;
                                double longitude = (double)stopPoint.getInt("longitude")/3600000.0;
                                if(stopModifier!=null){
                                    if(stopModifier.has("name")){
                                        name = stopModifier.getString("name");
                                    }
                                    if(stopModifier.has("say")){
                                        sayName = stopModifier.getString("say");
                                    }
                                }
                                if(stopPointModifier!=null){
                                    if(stopPointModifier.has("latitude")&&stopPointModifier.has("longitude")){
                                        latitude = stopPointModifier.getInt("latitude")/3600000.0;
                                        longitude = stopPointModifier.getInt("longitude")/3600000.0;
                                    }
                                }
                                /*double latitude = (double)stopPoint.getInt("latitude")/3600000.0;
                                double longitude = (double)stopPoint.getInt("longitude")/3600000.0;*/

                                track.addStop(new Stop(name, latitude, longitude, sayName));
                            }

                        }
                        lines.add(track);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        iterator = sortedNamesLines.iterator();
        while(iterator.hasNext()){
            JSONObject element = iterator.next();
            try {
                String line = element.getString("name");
                JSONArray routes = element.optJSONArray("directions");
                if(routes.length()>0){
                    for(int i=0;i<routes.length();i++){
                        JSONObject route = routes.optJSONObject(i);
                        String direction = route.getString("direction");
                        String sayDirection = direction;
                        if(route.has("sayDirection")){
                            sayDirection=route.getString("sayDirection");
                            String temp = getStopSayName(jsonStopsModify, sayDirection);
                            if(temp!=null){
                                sayDirection = temp;
                            }
                        }
                        Track<String> track = new Track<String>(line, direction, sayDirection);

                        JSONArray routeStops = route.optJSONArray("routeStops");
                        for(int j=0;j<routeStops.length();j++){
                            JSONObject stopPoint = getStopPoint(jsonStopsPoint, routeStops.getString(j));
                            if(stopPoint!=null) {
                                String name = getStopName(jsonStops, stopPoint.getString("shortName"));
                                String sayName = name;
                                JSONObject stopModifier = searchStopModify(jsonStopsModify, stopPoint.getString("shortName"));
                                JSONObject stopPointModifier = getStopPoint(jsonStopsPointModify, routeStops.getString(j));
                                double latitude = (double) stopPoint.getInt("latitude") / 3600000.0;
                                double longitude = (double) stopPoint.getInt("longitude") / 3600000.0;
                                if(stopModifier!=null){
                                    if(stopModifier.has("name")){
                                        name = stopModifier.getString("name");
                                    }
                                    if(stopModifier.has("say")){
                                        sayName = stopModifier.getString("say");
                                    }
                                }
                                if(stopPointModifier!=null){
                                    if(stopPointModifier.has("latitude")&&stopPointModifier.has("longitude")){
                                        latitude = stopPointModifier.getInt("latitude")/3600000.0;
                                        longitude = stopPointModifier.getInt("longitude")/3600000.0;
                                    }
                                }
                                track.addStop(new Stop(name, latitude, longitude, sayName));
                            }
                        }
                        lines.add(track);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(!inTest){
            if(lines.size()>0){
                lineButton.setEnabled(true);
                infoLabel.setText("Wybierz linię");
            }
            else{
                infoLabel.setText("Brak linii");
            }
        }
    }
    @VisibleForTesting
    ArrayList<JSONObject> getLinesFromJson(JSONArray jsonLinesList, boolean numeric){
        ArrayList<JSONObject> tempLines = new ArrayList<>();
        for(int i=0;i<jsonLines.length();i++){
            JSONObject element = jsonLines.optJSONObject(i);
            if(element!=null){
                try {
                    if(isNumeric(element.getString("name"))==numeric) {
                        tempLines.add(element);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return tempLines;
    }
    @VisibleForTesting
    String getStopSayName(JSONArray stopList, String shortName){
        for(int i=0;i<stopList.length();i++){
            JSONObject element = null;
            try {
                element = stopList.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String elementId;
            if(element != null){
                try {

                    elementId = element.getString("shortName");
                    if(elementId.equals(shortName)&&element.has("say")){
                        return element.getString("say");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    @VisibleForTesting
    ArrayList<JSONObject> sortNumericLines(ArrayList<JSONObject> numericLines){
        Collections.sort(numericLines, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject obj1, JSONObject obj2) {
                int a=0,b=0;
                try {
                    a = Integer.parseInt(obj1.getString("name"));
                    b = Integer.parseInt(obj2.getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                };

                return Integer.valueOf(a).compareTo(b);
            }
        });
        return numericLines;
    }
    @VisibleForTesting
    ArrayList<JSONObject> sortTextLines(ArrayList<JSONObject> textLines){
        Collections.sort(textLines, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject obj1, JSONObject obj2) {
                String a="",b="";
                try {
                    a = obj1.getString("name");
                    b = obj2.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return String.valueOf(a).compareTo(b);
            }
        });
        return textLines;
    }
    @VisibleForTesting
    String getStopName(JSONArray stopList, String shortName){
        for(int i=0;i<stopList.length();i++){
            JSONObject element = null;
            try {
                element = stopList.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String elementId;
            if(element != null){
                try {
                    elementId = element.getString("shortName");
                    if(elementId.equals(shortName)){
                        return element.getString("name");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    @VisibleForTesting
    JSONObject searchStopModify(JSONArray stopsModify, String shortName){

        for(int i=0; i<stopsModify.length();i++){
            JSONObject element = null;
            try {
                element = stopsModify.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String elementId;
            if(element!=null){

                try {
                    elementId = element.getString("shortName");
                    if(elementId.equals(shortName)){
                        return  element;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }
    @VisibleForTesting
    JSONObject getStopPoint(JSONArray stopsPointList, String stopPoint){
        for(int i=0; i<stopsPointList.length();i++){
            JSONObject element = null;
            try {
                element = stopsPointList.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String elementId;
            if(element!=null){
                try {
                    elementId = element.getString("stopPoint");
                    if(elementId.equals(stopPoint)){
                        return element;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static boolean isNumeric(String str){
        try{
            int number = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe){
            return false;
        }
        return true;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeActionBar();

    }
    private void changeActionBar(){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){

            actionBar.hide();
        }
        else{
            actionBar.show();
        }
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previousButton:
                if(isUpdate()){
                    JSONObject tempJson = null;
                    FileWriter file = null;
                    try{
                        file = new FileWriter(stopsJsonFile);
                        tempJson = new JSONObject(stopsJsonContent);
                        file.write(tempJson.toString());
                        file.flush();

                        file = new FileWriter(stopsPointJsonFile);
                        tempJson = new JSONObject(stopsPointJsonContent);
                        file.write(tempJson.toString());
                        file.flush();

                        file = new FileWriter(lineJsonFile);
                        tempJson = new JSONObject(linesJsonContent);
                        file.write(tempJson.toString());
                        file.flush();

                        file = new FileWriter(stopsModifyJsonFile);
                        tempJson = new JSONObject(stopsModifyJsonContent);
                        file.write(tempJson.toString());
                        file.flush();

                        file = new FileWriter(stopsPointModifyJsonFile);
                        tempJson = new JSONObject(stopsPointModifyJsonContent);
                        file.write(tempJson.toString());
                        file.flush();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(emptyData){
                        infoLabel.setText("Pobrano dane");
                        emptyData = false;
                    }
                    else{
                        infoLabel.setText("Zaktualizowano dane");
                    }

                    isRequireUpdate = false;
                    prevButton.setEnabled(false);
                    prevButton.setText("Poprzedni przystanek");
                    nextButton.setEnabled(false);
                    nextButton.setText("Następny przystanek");
                    generateLinesList();
                }
                else if(trackThread.isEnabledButton()){
                    trackThread.prevStop();
                }
                break;
            case R.id.nextButton:
                if(isUpdate()){
                    if(emptyData){
                        infoLabel.setText("Brak danych");

                    }
                    else{
                        JSONObject tempJson = null;
                        try {
                            tempJson = new JSONObject(streamToString(new FileInputStream(stopsJsonFile)));
                            jsonStops = tempJson.getJSONArray("stops");
                            tempJson = new JSONObject(streamToString(new FileInputStream(stopsPointJsonFile)));
                            jsonStopsPoint = tempJson.getJSONArray("stopPoints");
                            tempJson = new JSONObject(streamToString(new FileInputStream(lineJsonFile)));
                            jsonLines = tempJson.getJSONArray("routes");
                            tempJson = new JSONObject(streamToString(new FileInputStream(stopsModifyJsonFile)));
                            jsonStopsModify = tempJson.getJSONArray("stops");
                            tempJson = new JSONObject(streamToString(new FileInputStream(stopsPointModifyJsonFile)));
                            jsonStopsPointModify = tempJson.getJSONArray("stopPoints");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        infoLabel.setText("Załadowano dane");
                        generateLinesList();
                    }
                    isRequireUpdate = false;
                }
                else if(trackThread.isEnabledButton()){
                    trackThread.nextStop();
                }
                break;
            case R.id.chooseLineButton:

                Bundle extra = new Bundle();
                extra.putSerializable("lines", lines);

                Intent intent = new Intent(v.getContext(),ChooseLine.class);
                intent.putExtra("linesBundle", extra);
                startActivityForResult(intent, GET_VALUE_CODE);
                break;
            case R.id.stopButton:
                if(!trackThread.isStarted()){
                    finish();
                    System.exit(0);
                }
                else{
                    exitButton.setText("Wyjdź");
                    trackThread.setStarted(false);
                }
                break;
            case R.id.startButton:

                exitButton.setText("Stop");

                if(wasStart){
                    if(!trackThread.isStarted()&&trackThread.getNumberOfStop()==0){
                        trackThread.setStartNow(false);
                        trackThread.setEndSayStart(true);
                        trackThread.setPartInfo(0);
                        trackThread.setSay(true);
                    }else if(trackThread.isEndSayStart()){

                        trackThread.setStartNow(false);
                        trackThread.setEndSayStart(false);
                        trackThread.setPartInfo(0);
                        trackThread.setSay(true);
                    }
                }
                wasStart = true;
                trackThread.setStarted(true);

                break;
            case R.id.setButton:
                if(isNumeric(distanceEditText.getText().toString())){
                    defaultDistance = Double.parseDouble(distanceEditText.getText().toString());
                    trackThread.setMinDistance(defaultDistance);
                    distanceEditText.setText("");
                    distanceEditText.setHint(String.valueOf(defaultDistance));
                }
                else{
                    Toast.makeText(this, "Błędne dane. Wpisz liczbę.", Toast.LENGTH_SHORT).show();
                }
        }
    }
    public ArrayList<Track> getLines(){
        return lines;
    }
    @VisibleForTesting
    String streamToString(InputStream is) throws UnsupportedEncodingException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try{
                Log.e("Async","Start");
                URL url = new URL(urls[0]);
                URLConnection connection = url.openConnection();
                InputStream is = connection.getInputStream();
                Log.e("Async","End");
                return streamToString(is);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
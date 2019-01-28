package com.app.adrian.gpsip;

import android.location.LocationListener;
import android.location.LocationManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Adrian on 03.01.2018.
 */

public class TrackThread extends Thread {
    private boolean started = false;
    private boolean isShowNextStop = false;
    private Track currentLine;
    private MainActivity activity;
    private TextToSpeech tts;
    private double distance = -1.0;
    private double minDistance = 50;
    private Stop currentStop;
    private boolean cordinateSet = false;
    private double currentLatitude;
    private double currentLongitude;
    private int numberOfStop = 0;
    private boolean isSay = false;
    private boolean isCurrentStop = false;
    private boolean longSay = false;
    private boolean sayStart = true;
    private boolean buttonWasClicked = false;
    private boolean startNow = true;
    private int partInfo = 0;
    private int loopPartInfo = 0;
    private int endLoopPartInfo = 0;
    private boolean endSayStart = false;
    private boolean enabledButton = false;
    private long startLoopInfoTime = 0;
    private int lastNumberStop = -1;
    private int timeWait = 0;
    private long endSayTime = 0;

    public TrackThread(Track currentLine, MainActivity activity){
        this.currentLine = currentLine;
        this.activity = activity;

    }
    public void run(){
        tts = new TextToSpeech(activity.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

            }
        });
        tts.setLanguage(new Locale("pl"));
        Log.d("TTS", "Polski");



        while(true){
            if(started){

                if(currentLine.getStops().size()>0){
                    //Log.e("DISTANCE", "Distance: "+distance);
                    setCurrentStop();
                    if(!startNow){
                        startNow = true;
                    }
                    if(lastNumberStop!=numberOfStop&&enabledButton){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activity.buttonEnabler();
                            }
                        });
                    }

                    if(cordinateSet){

                        calculateDistance(currentStop.getLatitude(), currentStop.getLongitude(), currentLatitude, currentLongitude);

                        //updateDistance();
                        //Log.e("DISTANCE", "Distance: "+distance+" gLat: "+ currentLatitude+" dLong: "+currentLongitude+" sLat: "+currentStop.getLatitude()+" sLong: "+currentStop.getLongitude());
                    }
                    if(isShowNextStop){
                        nextStopInfo();
                        //Log.e("STOP", "Next stop:"+distance);
                    }
                    else{
                        if(numberOfStop==0&&sayStart&&!endSayStart){


                            startInfo();
                            enabledButton = true;
                            isSay = true;
                            continue;
                        }

                        //Log.e("STOP", "stop:"+distance);
                        stopInfo();


                    }
                }
            }
            else{
                startLoopInfoTime = System.currentTimeMillis();
            }
            delayUntilCurrentStop(10);

        }
    }
    private void toInfoLabel(final String str){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.toInfoLabel(str);
            }
        });
    }
    private void updateStopInfo(final boolean blockOnStart){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.updateStopInfo(blockOnStart);
            }
        });
    }
    public double calculateDistance(double latitudeStop, double longitudeStop, double gpsLatitude, double gpsLongitude){
        int radius = 6371;
        double dLat = (gpsLatitude-latitudeStop)*Math.PI/180.0;
        double dLong = (gpsLongitude-longitudeStop)*Math.PI/180.0;
        //double dLat = Math.toRadians(gpsLatitude) - Math.toRadians(latitudeStop);
        //double dLong = Math.toRadians(gpsLongitude) - Math.toRadians(longitudeStop);
        double a = Math.pow(Math.sin(dLat/2),2)+(Math.cos(latitudeStop)*Math.cos(gpsLatitude)*Math.pow(Math.sin(dLong/2),2));
        //double c = 2 * Math.asin(Math.sqrt(a));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = radius * c;
        d = d*1000;
        d = Math.floor(d*100)/100;
        this.distance = d;

        return this.distance;
    }

    public void startInfo(){
        updateStopInfo(true);
        String info = "Linia: "+currentLine.getLine();
        //infoLabel.setText(info);
        toInfoLabel(info);
        lastNumberStop = -1;
        if(currentLine.getLine() instanceof Integer){
            tts.speak("Linia numer "+currentLine.getLine(), TextToSpeech.QUEUE_FLUSH, null);
            delayUntilCurrentStop(2000);
            //Thread.sleep(2000);
            while(tts.isSpeaking()&&isCurrentStop){
                longSay = true;
            };
            if(longSay) {
                delayUntilCurrentStop(1000);
                //Thread.sleep(1000);
                longSay = false;
            }
        }
        else if(currentLine.getLine() instanceof String){
            if(currentLine.getLine()!=""){
                tts.speak("Linia: "+currentLine.getLine(), TextToSpeech.QUEUE_FLUSH, null);
                delayUntilCurrentStop(2000);
                //Thread.sleep(2000);
                while(tts.isSpeaking()&&isCurrentStop){
                    longSay = true;
                };
                if(longSay){
                    delayUntilCurrentStop(1000);
                    //Thread.sleep(1000);
                    longSay = false;
                }

            }
        }
        if(currentLine.getDirection()!=""){
            info = "Kierunek: "+currentLine.getSayDirection();
            tts.speak(info, TextToSpeech.QUEUE_FLUSH, null);
            toInfoLabel("Kierunek: ");
            //infoLabel.setText("Kierunek: ");
            delayUntilCurrentStop(1000);
            //Thread.sleep(1000);
            //infoLabel.setText(currentLine.getDirection());
            toInfoLabel(currentLine.getDirection());
            delayUntilCurrentStop(4000);
            //Thread.sleep(4000);
            while(tts.isSpeaking()&&isCurrentStop){
                longSay = true;
            };
            if(longSay){
                delayUntilCurrentStop(1000);
                //Thread.sleep(1000);
                longSay = false;
            };

        }
        endSayStart = true;

    }
    private void stopInfo(){

        if(isSay){
            switch(partInfo){
                case 0:
                    partInfo++;
                    break;
                case 1:

                    toInfoLabel("Przystanek");
                    if(!sayStart){
                        tts.speak(currentStop.getSayName(), TextToSpeech.QUEUE_FLUSH, null);
                    }
                    delayUntilCurrentStop(1000);
                    partInfo++;
                    break;
                case 2:
                    toInfoLabel(currentStop.getName());
                    if(numberOfStop!=currentLine.getStops().size()-1){
                        timeWait = 8000;
                        endSayTime = System.currentTimeMillis();
                        //delayUntilCurrentStop(8000);
                        //Thread.sleep(8000);
                    }
                    else{
                        delayUntilCurrentStop(3000);
                        //Thread.sleep(3000);
                    }
                    while(tts.isSpeaking()&&isCurrentStop){
                        longSay = true;
                    };
                    tts.stop();
                    if(endSayTime!=0&&longSay&&System.currentTimeMillis()>endSayTime+timeWait){
                        delayUntilCurrentStop(1000);
                        //Thread.sleep(1000);
                        longSay = false;
                    };
                    longSay = false;
                    partInfo++;
                    break;
                case 3:
                    if(numberOfStop==currentLine.getStops().size()-1){
                        toInfoLabel("Koniec trasy: ");
                        //infoLabel.setText("Koniec trasy:");
                        tts.speak("Koniec trasy. Proszę opuścić pojazd.", TextToSpeech.QUEUE_FLUSH, null);
                        delayUntilCurrentStop(5000);
                        //Thread.sleep(5000);
                        while(tts.isSpeaking()&&isCurrentStop){
                            longSay = true;
                        };
                        if(longSay){
                            delayUntilCurrentStop(1000);
                            //Thread.sleep(1000);
                            longSay = false;
                        };
                        endSayTime -= timeWait;
                        partInfo=0;
                        loopPartInfo = 0;
                        endLoopPartInfo = 0;
                        isSay = false;

                    }
                    if(endSayTime!=0&&System.currentTimeMillis()>endSayTime+timeWait&&numberOfStop<currentLine.getStops().size()-1){
                        partInfo=0;
                        loopPartInfo = 0;
                        endLoopPartInfo = 0;
                        isSay = false;
                    }
                    break;



            }
        }
        if(endSayTime!=0&&System.currentTimeMillis()>endSayTime+timeWait){

            switch (loopPartInfo) {
                case 0:

                    loopPartInfo++;
                    break;
                case 1:

                    if (loopPartInfo != endLoopPartInfo) {

                        startLoopInfoTime = System.currentTimeMillis();
                        toInfoLabel(currentLine.getLine() + " -> " + currentLine.getDirection());
                        endLoopPartInfo = loopPartInfo;
                    }
                    if (System.currentTimeMillis() - startLoopInfoTime >= 10000) {
                        loopPartInfo++;
                    }

                    break;
                case 2:

                    if (loopPartInfo != endLoopPartInfo) {

                        toInfoLabel("Przystanek:");
                        endLoopPartInfo = loopPartInfo;
                    }
                    if (System.currentTimeMillis() - startLoopInfoTime >= 12000) {
                        loopPartInfo++;
                    }
                    break;
                case 3:

                    if (loopPartInfo != endLoopPartInfo) {

                        toInfoLabel(currentStop.getName());
                    }
                    if (System.currentTimeMillis() - startLoopInfoTime >= 20000) {
                        if (numberOfStop == currentLine.getStops().size() - 1) {
                            loopPartInfo++;
                        } else {
                            loopPartInfo = 0;
                        }
                    }

                    break;
                case 4:
                    if (loopPartInfo != endLoopPartInfo) {
                        toInfoLabel("Koniec trasy:");
                    }
                    if (System.currentTimeMillis() - startLoopInfoTime >= 25000) {
                        loopPartInfo = 0;
                    }
                    endLoopPartInfo = 0;
                    break;

            }

        }
        if(endSayTime!=0&&System.currentTimeMillis()>endSayTime+2000&&!longSay){
            if (sayStart) {
                if (distance >= 0 && distance <= minDistance) {
                    sayStart = false;
                }
            }
            else if (distance >= 0 && distance > minDistance && numberOfStop < currentLine.getStops().size() - 1) {
                isShowNextStop = true;
                isCurrentStop = false;
                lastNumberStop = numberOfStop;
                numberOfStop++;
                sayStart = false;
                isSay = true;
                endSayTime = 0;
            }
        }

    }
    private void nextStopInfo(){
        //Log.e("Track","IsSay: "+isSay+" partInfo: "+partInfo+" isNextStop: "+isShowNextStop+" isCurrentStop: "+isCurrentStop+" last: "+lastNumberStop+" now:"+numberOfStop+" start:"+started);
        if(isSay){
            switch(partInfo){
                case 0:
                    partInfo++;
                    break;
                case 1:
                    toInfoLabel("Następny przystanek:");
                    tts.speak("Następny przystanek: "+currentStop.getSayName(), TextToSpeech.QUEUE_FLUSH, null);
                    delayUntilCurrentStop(1000);
                    partInfo++;
                    break;
                case 2:
                    toInfoLabel(currentStop.getName());
                    timeWait = 8000;
                    endSayTime = System.currentTimeMillis();
                    //delayUntilCurrentStop(8000);
                    while(tts.isSpeaking()&&isCurrentStop){
                        longSay = true;
                    };
                    tts.stop();
                    if(endSayTime!=0&&longSay&&System.currentTimeMillis()>endSayTime+timeWait){
                        delayUntilCurrentStop(1000);
                        //Thread.sleep(1000);
                        longSay = false;
                    };
                    longSay = false;
                    isSay = false;
                    partInfo = 0;
                    loopPartInfo = 0;
                    endLoopPartInfo = 0;
                    break;
                case 3:
                    if(endSayTime!=0&&System.currentTimeMillis()>endSayTime+timeWait)
                    {

                    }
                    break;

            }

        }
        if(endSayTime!=0&&System.currentTimeMillis()>endSayTime+timeWait){
            switch(loopPartInfo) {
                case 0:

                    loopPartInfo++;
                    break;
                case 1:

                    if (loopPartInfo != endLoopPartInfo) {

                        startLoopInfoTime = System.currentTimeMillis();
                        toInfoLabel(currentLine.getLine() + " -> " + currentLine.getDirection());
                        endLoopPartInfo = loopPartInfo;
                    }
                    if (System.currentTimeMillis() - startLoopInfoTime >= 10000) {
                        loopPartInfo++;
                    }

                    break;
                case 2:

                    if (loopPartInfo != endLoopPartInfo) {

                        toInfoLabel("Następny przystanek:");
                        endLoopPartInfo = loopPartInfo;
                    }
                    if (System.currentTimeMillis() - startLoopInfoTime >= 12000) {
                        loopPartInfo++;
                    }

                    break;
                case 3:

                    if (loopPartInfo != endLoopPartInfo) {

                        toInfoLabel(currentStop.getName());
                    }
                    if (System.currentTimeMillis() - startLoopInfoTime >= 20000) {
                        loopPartInfo = 0;
                    }
                    endLoopPartInfo = 0;

                    break;
            }

        }
        if(endSayTime!=0&&System.currentTimeMillis()>endSayTime+2000&&!longSay){
            if(distance >=0 && distance<=minDistance&&!buttonWasClicked)
            {
                isShowNextStop = false;
                isSay = true;
                endSayTime=0;
            }
            if(buttonWasClicked){
                buttonWasClicked = false;
            }

        }


    }
    public void prevStop(){
        if(numberOfStop>0){

            buttonWasClicked = true;
            isShowNextStop = true;
            isCurrentStop = false;
            lastNumberStop = numberOfStop;
            partInfo = 0;
            numberOfStop--;
            setCurrentStop();
            isSay = true;
            endSayTime = 0;
            if(numberOfStop==0){
                endSayStart = true;
                sayStart = true;
                isShowNextStop = false;
            }
            else{
                sayStart = false;

            }
            if(!started){
                toInfoLabel(currentStop.getName());
                //infoLabel.setText(currentStop.getName());
            }
        }
    }
    public void nextStop(){
        if(numberOfStop<currentLine.getStops().size()-1){

            buttonWasClicked = true;
            isShowNextStop = true;
            isCurrentStop = false;
            lastNumberStop = numberOfStop;
            partInfo = 0;
            numberOfStop++;
            setCurrentStop();
            endSayStart = true;
            endSayTime = 0;
            sayStart = false;
            isSay = true;
            if(!started){
                toInfoLabel(currentStop.getName());
            }

        }
    }

    private void setCurrentStop(){
        if(numberOfStop<currentLine.getStops().size()&&!isCurrentStop){

            ArrayList<Stop> stopsOfLine = currentLine.getStops();
            currentStop = stopsOfLine.get(numberOfStop);

            if(currentStop!=null&&cordinateSet){
                calculateDistance(currentStop.getLatitude(), currentStop.getLongitude(), currentLatitude, currentLongitude);
            }
            partInfo = 0;
            updateStopInfo(false);

            if(currentStop!=null){
                isCurrentStop=true;
            }
        }

    }
    private void delayUntilCurrentStop(int miliSecond){
        long until = System.currentTimeMillis() + miliSecond;
        while(isCurrentStop&&startNow&&System.currentTimeMillis()<until){

        }

    }
    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(double minDistance) {
        this.minDistance = minDistance;
    }

    public boolean isCordinateSet() {
        return cordinateSet;
    }

    public void setCordinateSet(boolean cordinateSet) {
        this.cordinateSet = cordinateSet;
    }

    public double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public Track getCurrentLine() {
        return currentLine;
    }

    public void setCurrentLine(Track currentLine) {

        this.started = false;
        this.isShowNextStop = false;
        this.currentLine = currentLine;
        this.distance = -1.0;
        this.numberOfStop = 0;
        this.isSay = false;
        this.isCurrentStop = false;
        this.partInfo = 0;
        setCurrentStop();
        this.sayStart = true;
        this.endSayTime=0;
        this.endSayStart = false;
        this.enabledButton = false;


    }

    public boolean isEnabledButton() {
        return enabledButton;
    }

    public int getNumberOfStop() {
        return numberOfStop;
    }

    public Stop getCurrentStop() {
        return currentStop;
    }

    public boolean isEndSayStart() {
        return endSayStart;
    }

    public void setEndSayStart(boolean endSayStart) {
        this.endSayStart = endSayStart;
    }

    public void setPartInfo(int partInfo) {
        this.partInfo = partInfo;
    }

    public void setSay(boolean say) {
        isSay = say;
    }

    public void setStartNow(boolean startNow) {
        this.startNow = startNow;
    }

    public int getLastNumberStop() {
        return lastNumberStop;
    }
}
package com.app.adrian.gpsip;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Adrian Ożóg on 02.01.2018.
 */

public class Track<T> implements Serializable{
    private T line;
    private String direction;
    private String sayDirection;
    private ArrayList<Stop> stops;

    public Track(T line, String direction, String sayDirection) {
        this.line = line;
        this.direction = direction;
        this.sayDirection = sayDirection;
        this.stops = new ArrayList<>();
    }
    public Track(T line, String direction, ArrayList<Stop> stops, String sayDirection){
        this.line = line;
        this.direction = direction;
        this.stops = stops;
    }
    public T getLine (){
        return line;
    }
    public String getDirection (){
        return direction;
    }
    public void setDirection(String direction){
        this.direction = direction;
    }
    public ArrayList<Stop> getStops(){
        return stops;
    }
    public void addStop(Stop stop){
        this.stops.add(stop);
    }

    public void setStops(ArrayList<Stop> stops) {
        this.stops = stops;
    }

    public String getSayDirection() {
        return sayDirection;
    }

    public void setSayDirection(String sayDirection) {
        this.sayDirection = sayDirection;
    }
}
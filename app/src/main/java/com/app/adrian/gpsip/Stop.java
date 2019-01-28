package com.app.adrian.gpsip;

import java.io.Serializable;

/**
 * Created by Adrian Ożóg on 03.01.2018.
 */

public class Stop implements Serializable{
    private String name;
    private String sayName;
    private double latitude;
    private double longitude;

    public Stop(String name, double latitude, double longitude, String sayName){
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sayName = sayName;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getSayName() {
        return sayName;
    }
}
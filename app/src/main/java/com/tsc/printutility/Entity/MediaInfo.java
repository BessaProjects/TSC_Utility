package com.tsc.printutility.Entity;

public class MediaInfo {

    public final static int UNIT_IN = 0;
    public final static int UNIT_MM = 1;

    public final static String SENSOR_TYPE_GAP = "gap";
    public final static String SENSOR_TYPE_BLACK = "black";
    public final static String SENSOR_TYPE_CONTINUE = "continue";

    long id;
    String name;
    double width;
    double height;
    int unit;
    String sensorType;
    String updateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }
}

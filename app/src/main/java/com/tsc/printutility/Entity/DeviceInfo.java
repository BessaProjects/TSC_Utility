package com.tsc.printutility.Entity;

public class DeviceInfo {

    String name;
    String battery;
    String speed;
    String density;
    String width;
    String height;
    String sensor;
    String dpi;

    public String getDpi() {
        return dpi;
    }

    public void setDpi(String dpi) {
        this.dpi = dpi;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getDensity() {
        return density;
    }

    public void setDensity(String density) {
        this.density = density;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getSensor() {
        if(sensor != null){
            if(sensor.toLowerCase().startsWith("gap"))
                return "GAP";
            else if(sensor.toLowerCase().startsWith("bline"))
                return "Mark";
            else
                return "Cont.";
        }
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }
}

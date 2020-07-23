package com.sysu.sdcs.weatherstation;

public class SimWea {
    private String id;
    private String city;
    private String temp;
    private String text;

    SimWea() {
    }

    SimWea(String id, String city, String temp, String text) {
        this.id = id;
        this.city = city;
        this.temp = temp;
        this.text = text;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String name) {
        this.city = name;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String tem) {
        this.temp = tem;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}

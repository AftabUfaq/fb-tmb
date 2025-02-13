package com.orion.testmybloodft.models;

import java.io.Serializable;

/**
 * Created by Wekancode on 30-Mar-17.
 */

/**
 * Model class for scheduled today order
 */

public class TodayMod implements Serializable {
    private String tmb_order_id;
    private String pickup_date;
    private String pickup_time;
    private String patient_name;
    private String address;
    private String contact_number;
    private String field_tech_name;
    private int status;
    private int id;
    private int user_id;
    public int field_tech_id;
    private double latitude, longitude;
    private boolean tripStatus = false;

    public TodayMod(int field_tech_id) {
        this.field_tech_id = field_tech_id;
    }

    public boolean isTripStatus() {
        return tripStatus;
    }

    public void setTripStatus(boolean tripStatus) {
        this.tripStatus = tripStatus;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getField_tech_name() {
        return field_tech_name;
    }

    public void setField_tech_name(String field_tech_name) {
        this.field_tech_name = field_tech_name;
    }

    public int getField_tech_id() {
        return field_tech_id;
    }

    public void setField_tech_id(int field_tech_id) {
        this.field_tech_id = field_tech_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTmb_order_id() {
        return tmb_order_id;
    }

    public void setTmb_order_id(String tmb_order_id) {
        this.tmb_order_id = tmb_order_id;
    }

    public String getPickup_date() {
        return pickup_date;
    }

    public void setPickup_date(String pickup_date) {
        this.pickup_date = pickup_date;
    }

    public String getPickup_time() {
        return pickup_time;
    }

    public void setPickup_time(String pickup_time) {
        this.pickup_time = pickup_time;
    }

    public String getPatient_name() {
        return patient_name;
    }

    public void setPatient_name(String patient_name) {
        this.patient_name = patient_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

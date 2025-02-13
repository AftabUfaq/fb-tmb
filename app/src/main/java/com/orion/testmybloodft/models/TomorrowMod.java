package com.orion.testmybloodft.models;

import java.io.Serializable;

/**
 * Created by Wekancode on 30-Mar-17.
 */

/**
 * Model class for scheduled tomorrow order
 */

public class TomorrowMod implements Serializable {
    private String tmb_order_id, pickup_date, pickup_time, patient_name, address;

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

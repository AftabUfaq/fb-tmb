package com.orion.testmybloodft.models;

/**
 * Created by Arun on 30-Mar-17.
 */

/**
 * Model class for areas covered
 */

public class CityMod {

    private String cityID,cityName,status,stateName;


    public CityMod(){

    }

    public CityMod(String cityID,String cityName,String status,String stateName){

        this.cityID = cityID;
        this.cityName = cityName;
        this.status = status;
        this.stateName = stateName;
    }

    public String getCityID() {
        return cityID;
    }

    public void setCityID(String cityID) {
        this.cityID = cityID;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }
}

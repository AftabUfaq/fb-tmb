package com.orion.testmybloodft.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arun on 7/19/2017.
 */

/**
 * globally access today, tomorrow and
 * picked up model
 */

public class DataHolder {

    private List<TodayMod> todayModList = new ArrayList<TodayMod>();
    private List<TomorrowMod> tomorrowModList = new ArrayList<TomorrowMod>();
    private List<PickedUpMod> pickedUpModList = new ArrayList<PickedUpMod>();

    public List<TestListMod> getTestList() {
        return testList;
    }

    public void setTestList(List<TestListMod> testList) {
        this.testList = testList;
    }

    private List<TestListMod> testList = new ArrayList<TestListMod>();

    public DataHolder() {
    }

    public List<TodayMod> getTodayModList() {
        return todayModList;
    }

    public void setTodayModList(List<TodayMod> todayModList) {
        this.todayModList = todayModList;
    }

    public List<TomorrowMod> getTomorrowModList() {
        return tomorrowModList;
    }

    public void setTomorrowModList(List<TomorrowMod> tomorrowModList) {
        this.tomorrowModList = tomorrowModList;
    }

    public List<PickedUpMod> getPickedUpModList() {
        return pickedUpModList;
    }

    public void setPickedUpModList(List<PickedUpMod> pickedUpModList) {
        this.pickedUpModList = pickedUpModList;
    }
}

package com.orion.testmybloodft.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arun on 7/22/2017.
 */

/**
 * Global point of access to the object
 */

public class Singleton {

    private static List<TestListMod> testList;

    private static Singleton instance;

    private Singleton(){
        testList = new ArrayList<TestListMod>();
    }

    public static Singleton getInstance(){
        if (instance == null){
            instance = new Singleton();
        }
        return instance;
    }

    public List<TestListMod> getTestList() {
        return testList;
    }

    public void setTestList(List<TestListMod> testList) {
        Singleton.testList = testList;
    }
}


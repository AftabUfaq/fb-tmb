package com.orion.testmybloodft.models;

import java.io.Serializable;

/**
 * Created by Wekancode on 30-Mar-17.
 */

public class TestListMod implements Serializable {
    private String test, cost;

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    @Override
    public String toString() {
        return "PropertyMod{" +
                "test='" + test + '\'' +
                ", cost='" + cost + '\'' +
                '}';
    }
}

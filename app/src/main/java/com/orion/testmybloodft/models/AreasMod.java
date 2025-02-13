package com.orion.testmybloodft.models;

import java.io.Serializable;

/**
 * Created by Wekancode on 30-Mar-17.
 */

/**
 * Model class for FT areas covered
 */

public class AreasMod implements Serializable {
    private String area, pincode;

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Override
    public String toString() {
        return "PropertyMod{" +
                "area='" + area + '\'' +
                ", pincode='" + pincode + '\'' +
                '}';
    }
}

package com.orion.testmybloodft.api;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.android.volley.Request;


import java.util.Map;

/**
 * Created by Arun on 1/7/2017.
 */

public class ApiModel {

    public String FROM_TAG = "ApiModel";
    public String TOKEN_HEADER = "authorization";
    public Context context;
    public Activity activity;
    public int DATA_OR_FILE_Type = 0;
    public Drawable drawable;
    public String filename;
    public Map<String, String> params;
    public int serviceType;
    public String url = "https://testmyblood.in/";
    public int method = 0;
    public boolean volleyDataCache = false;
    public Request.Priority priority = Request.Priority.NORMAL;

}

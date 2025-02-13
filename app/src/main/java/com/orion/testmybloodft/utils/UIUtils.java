package com.orion.testmybloodft.utils;


/**
 * Created by Arun on 1/6/2017.
 */

/**
 * Retrieve the app start time,set when the application was created. This is used to calculate
 * the current time, in debug mode only.
 */

public final class UIUtils {
    private static final String TAG =UIUtils.class.getSimpleName();

    public static final String MOCK_DATA_PREFERENCES = "mock_data";
    public static final String PREFS_MOCK_CURRENT_TIME = "mock_current_time";
    public static final String PREFS_MOCK_APP_START_TIME = "mock_app_start_time";

    public static final int HANDLER_TIME_FAST = 100;
}

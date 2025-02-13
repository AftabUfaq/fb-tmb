package com.orion.testmybloodft.api;

import java.util.HashMap;

/**
 * Created by Arun on 1/30/2017.
 */

public final class ServerParams {

    // TMB APP
    public final static String PASSWORD = "password";
    public final static String OLD_PASSWORD = "old_password";
    public final static String USER_ID = "user_id";
    public final static String NEW_PASSWORD = "new_password";
    public final static String CONFIRM_PASSWORD = "confirm_password";
    public final static String SESSION_TOKEN = "session_token";
    public final static String DEVICE_NAME = "device"; // android, ios, web
    public final static String DEVICE_TOKEN = "device_token"; // token of mobile device\


    private static final String USER_TYPE = "user_type";
    public final static String EMAIL = "email";
    public final static String OTP = "otp";
    public final static String EMAIL_FOR_OTP = "emailForOtp";
    public final static String EMAIL_TECHNICIAN = "6";
    public final static String PICK_UP_DATE = "date";
    public final static String ORDER_STATUS = "status";
    public final static String ORDER_START = "start";
    public final static String ORDER_LIST_SIZE = "size";
    public final static String ORDER_ID = "order_id";
    public final static String END_USER_LATITUDE = "latitude";
    public final static String END_USER_LONGIITUDE = "longitude";
    public final static String TEST_TUBE_COUNT = "testtube_count";
    public final static String SCANNED_BARCODE = "barcode_collection";
    public final static String USER_VERIFIED = "is_userid_verified";
    public final static String PAYMENT_TYPE = "payment_type";
    public final static String TRANS_ID = "transaction_id";
    public final static String NOTIFICATION_IDS = "notification_ids";

    public static HashMap<String, String> checkEmail(String email, String user_type) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(EMAIL, email);
        hashMapObj.put(USER_TYPE, user_type);
        // return user
        return hashMapObj;
    }

    public static HashMap<String, String> getResendPwd(String email) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(EMAIL, email);
        // return user
        return hashMapObj;
    }


    public static HashMap<String, String> postLoginDetails(String email, String password, String user_type, String session_token, String device) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(EMAIL, email);
        hashMapObj.put(PASSWORD, password);
        hashMapObj.put(USER_TYPE, user_type);
        hashMapObj.put(SESSION_TOKEN, session_token);
        hashMapObj.put(DEVICE_NAME, device);
        hashMapObj.put(DEVICE_TOKEN, session_token);
        //hashMapObj.put(EMAIL_FOR_OTP,email);
        //return user
        return hashMapObj;
    }

    public static HashMap<String, String> changePass(String old_password, String new_password, String confirm_password, String session_token) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(OLD_PASSWORD, old_password);
        hashMapObj.put(NEW_PASSWORD, new_password);
        hashMapObj.put(CONFIRM_PASSWORD, confirm_password);
        hashMapObj.put(SESSION_TOKEN, session_token);
        // return user
        return hashMapObj;
    }

    public static HashMap<String, String> verifyForgot(String email, String otp, String session_token, String device, String device_token) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(EMAIL, email);
        hashMapObj.put(OTP, otp);
        hashMapObj.put(SESSION_TOKEN, session_token);
        hashMapObj.put(DEVICE_NAME, device);
        hashMapObj.put(DEVICE_TOKEN, device_token);
        // return user
        return hashMapObj;
    }

    public static HashMap<String, String> resetPassword(String user_id, String new_password, String confirm_password, String device, String device_token) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(USER_ID, user_id);
        hashMapObj.put(NEW_PASSWORD, new_password);
        hashMapObj.put(CONFIRM_PASSWORD, confirm_password);
        hashMapObj.put(DEVICE_NAME, device);
        hashMapObj.put(DEVICE_TOKEN, device_token);
        // return user
        return hashMapObj;
    }

    public static HashMap<String, String> getOrders(String session_token, String pickup_date, String status, String start, String size) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(SESSION_TOKEN, session_token);
        hashMapObj.put(PICK_UP_DATE, pickup_date);
        hashMapObj.put(ORDER_STATUS, status);
        hashMapObj.put(ORDER_START, start);
        hashMapObj.put(ORDER_LIST_SIZE, size);
        // return user
        return hashMapObj;
    }

    public static HashMap<String, String> logout(String session_token, String device, String device_token) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(SESSION_TOKEN, session_token);
        hashMapObj.put(DEVICE_NAME, device);
        hashMapObj.put(DEVICE_TOKEN, device_token);
        // return user
        return hashMapObj;
    }

    public static HashMap<String, String> imageUpload(String session_token) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(SESSION_TOKEN, session_token);
        // return user
        return hashMapObj;
    }

    public static HashMap<String, String> getOrderDetails(String order_id) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(ORDER_ID, order_id);
        // return user
        return hashMapObj;
    }

    public static HashMap<String, String> startTrip(String session_token, String order_id, String latitude, String longitude) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(SESSION_TOKEN, session_token);
        hashMapObj.put(ORDER_ID, order_id);
        hashMapObj.put(END_USER_LATITUDE, latitude);
        hashMapObj.put(END_USER_LONGIITUDE, longitude);
        // return user
        return hashMapObj;
    }

    public static HashMap<String, String> endTrip(String session_token, String order_id, String latitude, String longitude) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(SESSION_TOKEN, session_token);
        hashMapObj.put(ORDER_ID, order_id);
        hashMapObj.put(END_USER_LATITUDE, latitude);
        hashMapObj.put(END_USER_LONGIITUDE, longitude);
        // return user
        return hashMapObj;
    }

    public static HashMap<String, String> collectSample(String session_token, String order_id, String testtube_count, String barcode_collection, String is_userid_verified) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(SESSION_TOKEN, session_token);
        hashMapObj.put(ORDER_ID, order_id);
        hashMapObj.put(TEST_TUBE_COUNT, testtube_count);
        hashMapObj.put(SCANNED_BARCODE, barcode_collection);
        hashMapObj.put(USER_VERIFIED, is_userid_verified);
        // return user
        return hashMapObj;
    }

    public static HashMap<String, String> collectPayment(String session_token, String order_id, String payment_type, String transaction_id) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(SESSION_TOKEN, session_token);
        hashMapObj.put(ORDER_ID, order_id);
        hashMapObj.put(PAYMENT_TYPE, payment_type);
        hashMapObj.put(TRANS_ID, transaction_id);
        // return user
        return hashMapObj;
    }

    public static HashMap<String, String> getNotification(String session_token, String start, String size) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(SESSION_TOKEN, session_token);
        hashMapObj.put(ORDER_START, start);
        hashMapObj.put(ORDER_LIST_SIZE, size);
        return hashMapObj;
    }

    public static HashMap<String, String> clearNotification(String session_token, String notification_ids) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(SESSION_TOKEN, session_token);
        hashMapObj.put(NOTIFICATION_IDS, notification_ids);
        return hashMapObj;
    }

    public static HashMap<String, String> notifyCount(String session_token) {
        HashMap<String, String> hashMapObj = new HashMap<String, String>();
        // user email id
        hashMapObj.put(SESSION_TOKEN, session_token);
        return hashMapObj;
    }

}

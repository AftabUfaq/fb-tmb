package com.orion.testmybloodft.api;

/**
 * Created by Arun on 1/30/2017.
 */

public final class ServerApi {

    public final static int CODE_CHK_USER = 0;
    public final static String URL_CHK_USER = "user/checkUser";

    public final static int CODE_RESEND_PWD = 1;
    public final static String URL_RESEND_PWD = "user/resendFTLoginOTP";

    public final static int CODE_LOGIN = 2;
    public final static String URL_LOGIN = "user/login";

    public final static int CODE_CHANGE_PASS = 3;
    public final static String URL_CHANGE_PASS = "user/changepass";

    public final static int CODE_USER_FORGOT = 4;
    public final static String URL_USER_FORGOT = "user/forgot";

    public final static int CODE_VERIFY_FORGOT = 5;
    public final static String URL_VERIFY_FORGOT = "user/verifyForgotOTP";

    public final static int CODE_RESET_PWD = 6;
    public final static String URL_RESET_PWD = "user/reset";

    public final static int CODE_FT_LOGOUT = 7;
    public final static String URL_FT_LOGOUT = "user/logout";

    public final static int CODE_IMAGE_UPLOAD = 8;
    public final static String URL_IMAGE_UPLOAD = "userImageUpload";

    public final static int CODE_USER_DETAIS = 9;
    public final static String URL_USER_DETAILS = "getUserDetails";

    public final static int CODE_MY_ORDERS = 10;
    public final static String URL_MY_ORDERS = "user/myorders";

    public final static int CODE_ORDER_DETAILS = 11;
    public final static String URL_ORDER_DETAILS = "getOrderDetails/";

    public final static int CODE_START_TRIP = 12;
    public final static String URL_START_TRIP = "startTrip";

    public final static int CODE_END_TRIP = 13;
    public final static String URL_END_TRIP = "endTrip";

    public final static int CODE_COLLECT_SAMPLE = 14;
    public final static String URL_COLLECT_SAMPLE = "collectSample";

    public final static int CODE_COLLECT_PAYMENT = 15;
    public final static String URL_COLLECT_PAYMENT = "collectPayment";

    public final static int CODE_NOTIFICATION = 16;
    public final static String URL_NOTIFICATION = "getNotifications";

    public final static int CODE_CLEAR_NOTIFICATION = 17;
    public final static String URL_CLEAR_NOTIFICATION = "clearNotifications";

    public final static int CODE_COUNT_NOTIFICATION = 18;
    public final static String URL_COUNT_NOTIFICATION = "getNotificationsCount";

    public static String getApiType(int serviceType) {
        if (serviceType == CODE_CHK_USER) {
            return URL_CHK_USER;
        } else if (serviceType == CODE_RESEND_PWD) {
            return URL_RESEND_PWD;
        } else if (serviceType == CODE_LOGIN) {
            return URL_LOGIN;
        } else if (serviceType == CODE_CHANGE_PASS) {
            return URL_CHANGE_PASS;
        } else if (serviceType == CODE_USER_FORGOT) {
            return URL_USER_FORGOT;
        } else if (serviceType == CODE_VERIFY_FORGOT) {
            return URL_VERIFY_FORGOT;
        } else if (serviceType == CODE_RESET_PWD) {
            return URL_RESET_PWD;
        } else if (serviceType == CODE_FT_LOGOUT) {
            return URL_FT_LOGOUT;
        } else if (serviceType == CODE_IMAGE_UPLOAD) {
            return URL_IMAGE_UPLOAD;
        } else if (serviceType == CODE_USER_DETAIS) {
            return URL_USER_DETAILS;
        } else if (serviceType == CODE_MY_ORDERS) {
            return URL_MY_ORDERS;
        } else if (serviceType == CODE_ORDER_DETAILS) {
            return URL_ORDER_DETAILS;
        } else if (serviceType == CODE_START_TRIP) {
            return URL_START_TRIP;
        } else if (serviceType == CODE_END_TRIP) {
            return URL_END_TRIP;
        } else if (serviceType == CODE_COLLECT_SAMPLE) {
            return URL_COLLECT_SAMPLE;
        } else if (serviceType == CODE_COLLECT_PAYMENT) {
            return URL_COLLECT_PAYMENT;
        } else if (serviceType == CODE_NOTIFICATION) {
            return URL_NOTIFICATION;
        } else if (serviceType == CODE_CLEAR_NOTIFICATION) {
            return URL_CLEAR_NOTIFICATION;
        } else if (serviceType == CODE_COUNT_NOTIFICATION) {
            return URL_COUNT_NOTIFICATION;
        }
        return null;
    }
}
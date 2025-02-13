package com.orion.testmybloodft.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.orion.testmybloodft.config.App;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Arun on 30-Mar-17.
 */

/**
 * Globally access member variable and method
 */

public class Constants {
    public static String QA_SOCKET_URL = "https://testmyblood.in/";
     public static String DEV_URL = "https://testmyblood.in/";
    public static String QA_URL = "https://testmyblood.in/";
    public static final String PREFERENCES_TMB = "TMBPreferences";

    public static final boolean DEBUG = Boolean.parseBoolean("true");
    public static final String FROM_TAG = "FROM_TAG";
    public static final String DEVICE_NAME = "android";
    public static final String DATA_REQUIRED = "35";
    public static final int LOAD_DATA_REQUIRED = 10;
    public static final String LIST_START_SIZE = "0";
    public static final String CLEAR_NOTIFICATIONS = "-1";
    public static final String TMB_NOTIFICATION = "TMB Notification";
    public static final String SOCKET_ENDTRIP = "socket_send_endtrip";

    public static final String PACKAGE_NAME = "com.sample.sishin.maplocation";

    public static final String NO_INTERNET_CONNECTIVITY = "No Internet Connection..!";

    public static final String ACCESS_SHOW_MY_ROUTE = "In order to access Order Status, First start the Trip..!";

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) App.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isValidEmail(String email) {
        boolean email_valid = false;
        email_valid = email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        return email_valid;
    }

    public static void hideKeyboard(View view) {
        try {
            InputMethodManager mImm = (InputMethodManager) App.getInstance().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            mImm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("wkcleg_Constants", "hideKeyboard Exception", e);
        }
    }

    public static String getHashPassword(String pass) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        assert md != null;
        md.update(pass.getBytes());

        byte byteData[] = md.digest();

        //convert the byte to hex format method 2
        StringBuilder hexString = new StringBuilder();
        for (byte aByteData : byteData) {
            String hex = Integer.toHexString(0xff & aByteData);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String getDeviceId(Activity parentActivity) {
        String devId = "";
        devId = Settings.Secure.getString(parentActivity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.i("DEVICE_ID", devId);
        return devId;

    }

    public static String getBase64String(String pass) {
        // Sending side
        byte[] data = pass.getBytes(StandardCharsets.UTF_8);
        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
        return base64;
    }

    public static String nameOfTheMonth(String monthNumber) {

        switch (monthNumber) {
            case "1":
                monthNumber = "Jan";
                break;
            case "01":
                monthNumber = "Jan";
                break;
            case "2":
                monthNumber = "Feb";
                break;
            case "02":
                monthNumber = "Feb";
                break;
            case "3":
                monthNumber = "Mar";
                break;
            case "03":
                monthNumber = "Mar";
                break;
            case "4":
                monthNumber = "Apr";
                break;
            case "04":
                monthNumber = "Apr";
                break;
            case "5":
                monthNumber = "May";
                break;
            case "05":
                monthNumber = "May";
                break;
            case "6":
                monthNumber = "Jun";
                break;
            case "06":
                monthNumber = "Jun";
                break;
            case "7":
                monthNumber = "Jul";
                break;
            case "07":
                monthNumber = "Jul";
                break;
            case "8":
                monthNumber = "Aug";
                break;
            case "08":
                monthNumber = "Aug";
                break;
            case "9":
                monthNumber = "Sep";
                break;
            case "09":
                monthNumber = "Sep";
                break;
            case "10":
                monthNumber = "Oct";
                break;
            case "11":
                monthNumber = "Nov";
                break;
            case "12":
                monthNumber = "Dec";
                break;
        }

        return monthNumber;
    }

    public static boolean isGPSEnabled(Context mContext) {
        LocationManager lm = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void showToast(Context context, String messageText) {

        Toast toast = Toast.makeText(context, messageText, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

    }


    public static long daysBetween(String date1, String date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date Date1 = null, Date2 = null;
        try {
            Date1 = sdf.parse(date1);
            Date2 = sdf.parse(date2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int days = (int) (Date2.getTime() - Date1.getTime()) / (24 * 60 * 60 * 1000);
        return (Date2.getTime() - Date1.getTime()) / (24 * 60 * 60 * 1000);
    }

    public static boolean getLoginStatus(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("login", false);
    }

    public static void setLoginStatus(Activity parentActivity, boolean upgrade) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("login", upgrade);
        editor.commit();
    }

    public static boolean getChangePwd(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("change_pwd", false);
    }

    public static void setChangePwd(Activity parentActivity, boolean change_pwd) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("change_pwd", change_pwd);
        editor.commit();
    }

    public static boolean getOtp(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("otp_status", false);
    }

    public static void setOtp(Activity parentActivity, boolean checkuser) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("otp_status", checkuser);
        editor.commit();
    }

    public static String getSession(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getString("accessToken", "");
    }

    public static void setSession(Context context, String value) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("accessToken", value);
        editor.commit();

    }

    public static int getCount(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("count", 0);
    }

    public static void setCount(Activity parentActivity, int value) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("count", value);
        editor.commit();
    }

    public static String getDeviceToken(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getString("deviceToken", "");
    }

    public static void setDeviceToken(Activity parentActivity, String dev_toc) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("deviceToken", dev_toc);
        editor.commit();

    }

    public static int getUserID(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("user_id", 0);
    }

    public static void setUserID(Activity parentActivity, int user_id) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("user_id", user_id);
        editor.commit();
    }

    public static String getProfileDetails(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getString("profileDetails", "");
    }

    public static void setProfileDetails(Activity parentActivity, String profileDetails) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("profileDetails", profileDetails);
        editor.commit();
    }

    public static String getBillingDetails(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getString("BillingDetails", "");
    }

    public static void setBillingDetails(Activity parentActivity, String BillingDetails) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("BillingDetails", BillingDetails);
        editor.commit();
    }

    public static String getOriginLatitude(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getString("latitude", "");
    }

    public static void setOriginLatitude(Context parentActivity, String latitude) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("latitude", latitude);
        editor.commit();
    }

    public static String getOriginLongitude(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getString("longitude", "");
    }

    public static void setOriginLongitude(Context parentActivity, String longitude) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("longitude", longitude);
        editor.commit();
    }

    public static int getOrderId(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("order_id", 0);
    }

    public static void setOrderId(Activity parentActivity, int order_id) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("order_id", order_id);
        editor.commit();
    }

    public static int getFieldTechId(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("field_tech_id", 0);
    }

    public static void setFieldTechId(Activity parentActivity, int field_tech_id) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("field_tech_id", field_tech_id);
        editor.commit();
    }

    public static String getFieldTechName(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getString("field_tech_name", "");
    }

    public static void setFieldTechName(Activity parentActivity, String field_tech_name) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("field_tech_name", field_tech_name);
        editor.commit();
    }

    public static void clearPreferences(Activity parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
    }

    public static boolean getFirstTimeLaunch(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("firstTimeLaunch", false);
    }

    public static void setFirstTimeLaunch(Context context, boolean launch) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("firstTimeLaunch", launch);
        editor.commit();
    }

    public static boolean getNewNotification(Context parentActivity) {
        SharedPreferences sharedpreferences = parentActivity.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("new_push", false);
    }

    public static void setNewNotification(Context context, boolean new_push) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("new_push", new_push);
        editor.commit();
    }

    public static String getFcmId(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getString("registration_id", "");
    }

    public static void setFcmId(Context context, String token) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("registration_id", token);
        editor.commit();
    }

    public static int getTabPos(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("tab_pos", 0);
    }

    public static void setTabPos(Context context, int tab_pos) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("tab_pos", tab_pos);
        editor.commit();
    }

    public static int getType(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("type", 0);
    }

    public static void setType(Context context, int type) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("type", type);
        editor.commit();
    }

    public static String getDate(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getString("date", "");
    }

    public static void setDate(Context context, String date) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("date", date);
        editor.commit();
    }

    public static boolean getInAppFirstTime(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("in_app", false);
    }

    public static void setInAppFirstTime(Context context, boolean in_app) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("in_app", in_app);
        editor.commit();
    }

    public static int getInAppTabPos(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("tab_pos", 0);
    }

    public static void setInAppTabPos(Context context, int tab_pos) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("tab_pos", tab_pos);
        editor.commit();
    }

    public static int getInAppType(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("type", 0);
    }

    public static void setInAppType(Context context, int type) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("type", type);
        editor.commit();
    }

    public static String getInAppDate(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        return sharedpreferences.getString("date", "");
    }

    public static void setInAppDate(Context context, String date) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFERENCES_TMB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("date", date);
        editor.commit();
    }


    public static Bitmap bitmap = null;
    public static String shareMessage = "QR-Barcode Scanner & Generator\n Free Download Now\nhttps://play.google.com/store/apps/details?id=com.qrbarcodescanner.qrcodemaker";
    public static String shareSubject = "QR-Barcode Scanner & Generator";
    public static String text;


    public static Calendar setAlarmTime() {
        Calendar instance = Calendar.getInstance();
        instance.set(10, 9);
        instance.set(12, 0);
        instance.set(13, 0);
        instance.set(14, 0);
        instance.set(9, 0);
        if (System.currentTimeMillis() > instance.getTimeInMillis()) {
            instance.setTimeInMillis(instance.getTimeInMillis() + 86400000);
        }
        return instance;
    }


}

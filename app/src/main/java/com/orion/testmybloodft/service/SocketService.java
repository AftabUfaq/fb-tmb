package com.orion.testmybloodft.service;

/**
 * Created by Arun on 7/6/2017.
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.orion.testmybloodft.config.App;
import com.orion.testmybloodft.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


/**
 * Created by Arun on 12/9/2017.
 */

/**
 * Service emitting FT location every 10s. Location will be stored in
 * socket server, FT location sync with end user app. FT reached user destination
 * after call end trip api client side end trip emit will triggered then socket has disconnected
 */

public class SocketService extends Service implements LocationListener {
    private final String TAG = SocketService.class.getSimpleName();
    private IBinder mBinder = new SocketBinder();

    private Socket socketEmitObj;
    private boolean isConnected = false;
    private boolean isTripStarted = false;
    private boolean connectErrorBeginBroadC = false;
    private boolean isClose = false;
    /*Gps Location */
    private Timer timer;
    private TimerTask timerTask;
    //private final int interval = 60000; // 1 Min
    //private final int interval = 25000; // Testing 25s
    private final int interval = 10000; // Testing 10s
    //private final int interval = 5000; // Testing 5s
    private final Context mContext = getBaseContext();
    private int user_id, type, fieldTechId, orderId;
    private String fieldTechName = "";
    private boolean logoutStatus = false;

    // flag for GPS status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
    // flag for GPS status
    boolean canGetLocation = false;
    // Declaring a Location Manager
    protected LocationManager locationManager;

    private Location location; // location
    private double latitude; // latitude
    public double longitude; // longitude
    private Runnable runnable;
    private LatLng latlng1, latlng2;
    private boolean isstartSource = false;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000; // 25 Sec
    private JSONObject emitData;
    private int user_type = 6;
    private boolean alreadyCalled = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        // Register Broadcast
        if (updateMode != null)
            registerReceiver(updateMode, new IntentFilter(Constants.SOCKET_ENDTRIP), Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "in onBind");
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    @Override
    public void onRebind(Intent intent) {
        Log.v(TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "in onUnbind");
        return true;
    }

    public class SocketBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    private void onTimerTaskInit() {
        timerTask = new TimerTask() {
            String values = "" + latitude;

            @Override
            public void run() {
                handlerGettingLocation.post(gettingLatLng);
                if (values != null || !values.equals(null) || !values.equals("")) {
                    Log.d("Runnung", latitude + "  " + "  " + longitude);

                    // Source and upcoming location compare
                    if (!isstartSource) {
                        isstartSource = true;
                        latlng1 = new LatLng(latitude, longitude);
                        // latlng1 = new LatLng(getLocation().getLatitude(), 20.2142402055955);
                    } else {
                        latlng2 = new LatLng(latitude, longitude);
                        // One Time SMS Sent away from 100meters
                        if (!isTripStarted) {
                            handler.post(sepearteProcess);
                        }
                    }

                    try {
                        Constants.setOriginLatitude(getApplicationContext(), String.valueOf(latitude));
                        Constants.setOriginLongitude(getApplicationContext(), String.valueOf(longitude));
                        String field_tech_name = Constants.getFieldTechName(getApplicationContext());
                        socketLocationEmit(orderId, fieldTechName, latitude, longitude);
                    } catch (Exception e) {
                        Log.e(TAG, "timerTask Run Exception : " + e);
                    }
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        if (intent != null && intent.hasExtra("user_id")) {
            user_id = intent.getIntExtra("user_id", 0);
            Log.d(TAG, "user_id: " + user_id);
        }
        if (intent != null && intent.hasExtra("type")) {
            type = intent.getIntExtra("type", 0);
            Log.d(TAG, "type: " + type);
        }
        if (intent != null && intent.hasExtra("orderId")) {
            orderId = intent.getIntExtra("orderId", 0);
            Log.d(TAG, "orderId: " + orderId);
        }
        if (intent != null && intent.hasExtra("fieldTechId")) {
            fieldTechId = intent.getIntExtra("fieldTechId", 0);
            Log.d(TAG, "fieldTechId: " + fieldTechId);
        }
        if (intent != null && intent.hasExtra("fieldTechName")) {
            fieldTechName = intent.getStringExtra("fieldTechName");
            Log.d(TAG, "fieldTechName: " + fieldTechName);
        }
        if (intent != null && intent.hasExtra("logoutStatus")) {
            logoutStatus = intent.getBooleanExtra("logoutStatus", false);
            Log.d(TAG, "logoutStatus: " + logoutStatus);
        }

        //getLocation();
        if (type == 1) {
            // Start Location Getting
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            getLocation();
            socketEmitObj = App.getInstance().getSocket();
            socketEmitObj.on(Socket.EVENT_CONNECT, onConnectDeliveryPersonLocation);
            socketEmitObj.on(Socket.EVENT_DISCONNECT, onDisconnectDeliveryPersonLocation);
            socketEmitObj.on(Socket.EVENT_CONNECT_ERROR, onConnetErrorDeliveryPersonLocation);
            socketEmitObj.on(Socket.EVENT_CONNECT_TIMEOUT, onConnetErrorDeliveryPersonLocation);
            socketEmitObj.on("join_room", onJoinRoomResponse);
            socketEmitObj.connect();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        socketJoinRoomEmit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, 1000);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        socketEndTripEmit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, 1000);
        }
        //return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    public void socketJoinRoomEmit() throws JSONException {
        alreadyCalled = true;
        Log.i(TAG, "socketJoinRoomEmit");
        emitData = new JSONObject();
        emitData.put("order_id", orderId);
        emitData.put("user_id", user_id);
        emitData.put("field_tech_id", fieldTechId);
        emitData.put("user_type", user_type);
        Log.d(TAG, "EndTrip socket isConnected: " + isConnected);
        if (isConnected) {
            logoutStatus = false;
            socketEmitObj.emit("join_room", emitData);
            Log.d(TAG, "joinRoomSendData: " + emitData.toString());
        } else if (logoutStatus) {
            socketEmitObj.emit("join_room", emitData);
            Log.d(TAG, "joinRoomSendDataLogoutTriggered: " + emitData.toString());
        } else {
            logoutStatus = false;
            Log.i(TAG, "socketJoinRoomEmit Disconnected..");
        }
    }

    public void socketEndTripEmit() throws JSONException {
        Log.i(TAG, "socketEndTripEmit");
        user_type = 6;
        JSONObject emitData = new JSONObject();
        emitData.put("order_id", orderId);
        emitData.put("user_id", user_id);
        emitData.put("field_tech_id", fieldTechId);
        emitData.put("user_type", user_type);
        Log.d(TAG, "socket isConnected: " + isConnected);
        if (isConnected) {
            socketEmitObj.emit("end_trip", emitData);
            Log.d(TAG, "end_trip: " + emitData.toString());
            this.stopSelf();

            // Socket disconnect
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    socketEmitObj = App.getInstance().getSocket();
                    socketEmitObj.off();
                    socketEmitObj.disconnect();
                }
            }, 500);

        } else {
            Log.i(TAG, "EndTrip Socket URL 3000 Port Disconnected..");
        }
    }

    public void socketLocationEmit(int order_id, String field_tech_name, double lat, double longitude) throws JSONException {
        Log.i(TAG, "socketLocationEmit");
        JSONObject emitData = new JSONObject();
        emitData.put("order_id", order_id);
        emitData.put("field_tech_name", field_tech_name);
        emitData.put("latitude", lat);
        emitData.put("longitude", longitude);
        Log.d(TAG, "lat: " + lat);
        Log.d(TAG, "longitude: " + longitude);
        Log.d(TAG, "location socket isConnected: " + isConnected);
        if (isConnected) {
            socketEmitObj.emit("location", emitData);
            Log.d(TAG, "DeliveryPersonLocation: " + emitData.toString());
        } else {
            Log.i(TAG, "Location Socket URL 3000 Port Disconnected..");
        }
    }


    private Emitter.Listener onConnectDeliveryPersonLocation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "event has connected: ");
            if (!isConnected) {
                connectErrorBeginBroadC = false;
                isConnected = true;
                if (alreadyCalled) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                socketJoinRoomEmit();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 1000);
                }
            }
        }
    };

    private Emitter.Listener onDisconnectDeliveryPersonLocation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            isConnected = false;
        }
    };

    private Emitter.Listener onConnetErrorDeliveryPersonLocation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (!connectErrorBeginBroadC) {
                connectErrorBeginBroadC = true;
            }
        }
    };

    private Emitter.Listener onJoinRoomResponse = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(TAG, "join_room call: ");
            Log.d(TAG, "joinRoomResponse: " + args[0]);
            if (args[0] != null) {
                Log.d(TAG, "onJoinRoomResponse: " + args[0]);
                timer = new Timer();
                onTimerTaskInit();
                timer.scheduleAtFixedRate(timerTask, 2000, interval);
            }
        }

    };

    @SuppressLint("MissingPermission")
    public Location getLocation() {
        try {
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                //   Toast.makeText(getApplicationContext(), "Gps is not enabled", Toast.LENGTH_SHORT).show();
                location = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        // return TODO;
                    }

                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("GPS Enabled", "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            //  Toast.makeText(getApplicationContext(), "lat = " + latitude + " long = " + longitude, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (null != timer) {
            Log.i(TAG, "onDestroy, Timer");
            timerTask.cancel();
            timer.cancel();
            timerTask = null;
            timer = null;
            unregisterReceiver(updateMode);
            Log.i(TAG, "onDestroy, Final Line Timer");
        }
        isClose = true;
        if (locationManager != null) {
            //noinspection MissingPermission
            locationManager.removeUpdates(this);
            //  Toast.makeText(getApplicationContext(), "onDestroy", Toast.LENGTH_SHORT).show();
        }
    }

    Handler handler = new Handler();
    Handler handlerGettingLocation = new Handler();

    Runnable sepearteProcess = new Runnable() {
        @Override
        public void run() {
            // Block of code to execute
            Log.d("onStatusChanged", "latlng1 Value : " + latlng1 + ", latlng2 Value : " + latlng2);
            if (!latlng1.equals("")) {
                Double aDouble = SphericalUtil.computeDistanceBetween(latlng1, latlng2);
                if (aDouble > 1.00) {
                    // Changing to true for after above meter to trigger confirm sms
                    isTripStarted = true;
                    //startTrip();
                }

                Log.d("onStatusChanged", "Distance Value : " + aDouble);
            }
        }
    };

    Runnable gettingLatLng = new Runnable() {
        @Override
        public void run() {

            try {

                latitude = getLocation().getLatitude();
                longitude = getLocation().getLongitude();
            } catch (Exception e) {

            }

        }
    };


    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("onStatusChanged", provider + " # " + status + " # " + extras);
    }

    @Override
    public void onProviderEnabled(String provider) {
        //    Toast.makeText(getApplicationContext(), "  GPS Connected", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        //  Toast.makeText(getApplicationContext(), " Check GPS Connection ", Toast.LENGTH_SHORT).show();
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }


    public BroadcastReceiver updateMode = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra("status")) {
                int status = intent.getIntExtra("status", 0);
                Log.i(TAG, "status: "+status);
                Log.d(TAG, "Inside Socket: " + Constants.SOCKET_ENDTRIP);
                try {
                    socketEndTripEmit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

}

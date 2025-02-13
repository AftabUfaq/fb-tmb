package com.orion.testmybloodft.views.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.orion.testmybloodft.R;
import com.orion.testmybloodft.api.ApiModel;
import com.orion.testmybloodft.api.ApiResponseView;
import com.orion.testmybloodft.api.ApiViewPresenter;
import com.orion.testmybloodft.api.ApiViewPresenterImpl;
import com.orion.testmybloodft.api.ServerApi;
import com.orion.testmybloodft.api.ServerJsonResponseKey;
import com.orion.testmybloodft.api.ServerParams;
import com.orion.testmybloodft.config.MyVolley;
import com.orion.testmybloodft.databinding.ActivityDirectionBinding;
import com.orion.testmybloodft.googleDirection.DirectionCallback;
import com.orion.testmybloodft.googleDirection.GoogleDirection;
import com.orion.testmybloodft.googleDirection.constant.TransportMode;
import com.orion.testmybloodft.googleDirection.model.Direction;
import com.orion.testmybloodft.googleDirection.util.DirectionConverter;
import com.orion.testmybloodft.service.SocketService;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;

import org.json.JSONObject;

import java.util.ArrayList;

public class DirectionActivity extends AppCompatActivity implements OnMapReadyCallback, DirectionCallback, ApiResponseView, Handler.Callback, LocationListener {
    private GoogleMap mGoogleMap;
    private String serverKey = "AIzaSyDVNcjpeWPe2f0pYmbvlw2zel0yORgog38";
    private LatLng mCenterLatLong;
    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;
    private LatLng origin;
    private LatLng destination;
    private Context mContext;
    private Activity mActivity;
    private static final String TAG = DirectionActivity.class.getSimpleName();
    private static final int REQUEST_CHECK_SETTINGS = 0;
    private final int PICK_LOCATION_REQUEST = 1;
    private final int CALL_PHONE_REQUEST = 2;
    private boolean tripStatus;
    private SocketService mBoundService;
    private boolean mServiceBound = false;

    private String tmb_order_id, message, address, contact_number, from, fieldTechName = "";
    private double destLatitude, destLongitude;
    private int order_status, order_id, user_id, field_tech_id;

//    @BindView(R.id.tvAddress)
//    TextView tvAddress;
//    @BindView(R.id.orderIdTv)
//    TextView orderIdTv;
//    @BindView(R.id.ivCall)
//    ImageView ivCall;

    // flag for GPS status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
    // flag for GPS status
    boolean canGetLocation = false;
    // Declaring a Location Manager
    private Location location;  // location
    private LocationManager lm;
    private GoogleApiClient googleApiClient;
    private Intent intent;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 2000; // 0 minute
    private Activity act = DirectionActivity.this; // 0 minute
    private double latitude; // latitude
    public double longitude; // longitude

    private int removeItemPos = -1;
    private int polylineColor = 0xFF28AEF5;
    private SupportMapFragment supportMapFragment;

    ActivityDirectionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        binding = ActivityDirectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mContext = this;
        mActivity = this;
        handleClick();
        googleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API).build();
        intent = getIntent();
        if (getIntent() != null && getIntent().hasExtra("position")) {
            removeItemPos = getIntent().getIntExtra("position", -1);
        }
        if (getIntent() != null && getIntent().hasExtra("address")) {
            address = getIntent().getStringExtra("address");
            binding.tvAddress.setText(address);
        }
        if (getIntent() != null && getIntent().hasExtra("fieldTechName")) {
            fieldTechName = getIntent().getStringExtra("fieldTechName");
        }
        if (getIntent() != null && getIntent().hasExtra("contact_number")) {
            contact_number = getIntent().getStringExtra("contact_number");
        }
        if (getIntent() != null && getIntent().hasExtra("from")) {
            from = getIntent().getStringExtra("from");
            Log.d(TAG, "fromTag: " + from);
        }
        if (getIntent() != null && getIntent().hasExtra("tmb_order_id")) {
            tmb_order_id = getIntent().getStringExtra("tmb_order_id");
        }
        if (getIntent() != null && getIntent().hasExtra("field_tech_id")) {
            field_tech_id = getIntent().getIntExtra("field_tech_id", 0);
        }
        if (getIntent() != null && getIntent().hasExtra("order_id")) {
            order_id = getIntent().getIntExtra("order_id", 0);
            binding.orderIdTv.setText("#" + order_id);
        }
        if (getIntent() != null && getIntent().hasExtra("user_id")) {
            user_id = getIntent().getIntExtra("user_id", 0);
        }
        if (getIntent() != null && getIntent().hasExtra("order_status")) {
            order_status = getIntent().getIntExtra("order_status", 0);
        }

        if (getIntent() != null && getIntent().hasExtra("latitude")) {
            destLatitude = getIntent().getDoubleExtra("latitude", 0.00);
            Log.d(TAG, "destLatitude: " + destLatitude);
        }
        if (getIntent() != null && getIntent().hasExtra("longitude")) {
            destLongitude = getIntent().getDoubleExtra("longitude", 0.00);
            Log.d(TAG, "destLongitude: " + destLongitude);
        }
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
        getCurrentLocation();
        init();

        if (from.equals("OrderStatus") && from != null) {
            if (Constants.isNetworkAvailable()) {
                //getCurrentLocation();
            } else {

                Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
            }
        } else {
            if (Constants.isNetworkAvailable()) {
                if (order_status == 1) {
                    apiDirectionCall();
                }
            } else {

                Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void getCurrentLocation() {
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // getting GPS status
        isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !canGetLocation) {
            displayLocationSettingsRequest(mContext);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        PICK_LOCATION_REQUEST);
                // to handle the case where the user grants the permission. See the documentation
                return;
            }
            this.canGetLocation = true;
            // First get location from Network Provider
            if (isNetworkEnabled) {
                lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if (lm != null) {
                    location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (lm != null) {
                    location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
            Log.d(TAG, "locationLatitude: " + latitude);
            Log.d(TAG, "locationLongitude: " + longitude);
            requestDirection();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_LOCATION_REQUEST) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permissions Granted... ");
                // permission was granted, yay! Do the
                // location-related task you need to do.
            } else {
                Log.d(TAG, "permissions Denied... ");
                // permission denied, boo! Disable the
                // functionality that depends on this permission.

                Toast.makeText(mContext, "Permission denied to access device Location", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CALL_PHONE_REQUEST) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permissions Granted... ");
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
            } else {
                Log.d(TAG, "permissions Denied... ");
                // permission denied, boo! Disable the
                // functionality that depends on this permission.

                Toast.makeText(mContext, "Permission Denied to call Telephone", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void handleClick() {
        binding.ivCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact_number));
                if (ActivityCompat.checkSelfPermission(DirectionActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                                CALL_PHONE_REQUEST);
                    }
                    // to handle the case where the user grants the permission. See the documentation
                    return;
                }
                startActivity(intent);
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void displayLocationSettingsRequest(Context context) {
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                Log.d(TAG, "onResult: " + status);
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                        finish();
                        startActivity(intent);
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(DirectionActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        // All required changes were successfully made
                        // Toast.makeText(DirectionActivity.this, "Location enabled by user!", Toast.LENGTH_LONG).show();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                finish();
                                Log.i(TAG, "All location settings are satisfied.");
                                // todayAdapter.returnAllow(positionNew);
                                // interacterPermis.returnResponse(positionNew);
                                // startActivity(intent);
                            }
                        }, 4000);
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to

                        Toast.makeText(DirectionActivity.this, "Location not enabled, user cancelled.", Toast.LENGTH_LONG).show();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        mGoogleMap = googleMap;
        mCenterLatLong = new LatLng(latitude, longitude);
        // Zoom in, animating the camera.
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);

        // Move the camera instantly to location with a zoom of 15.
        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCenterLatLong, 10));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCenterLatLong, 15));
    }

    public void requestDirection() {
        origin = new LatLng(latitude, longitude);
        destination = new LatLng(destLatitude, destLongitude);
        Log.e("RequestingDirection", "From " + latitude + "," + longitude);
        Log.e("RequestingDirection", "To " + destLatitude + "," + destLongitude);
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(this);
    }


    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        Log.d(TAG, "onDirectionSuccessBody: " + rawBody);
        Log.d(TAG, "onDirectionSuccessStatus: " + direction.getStatus());


        if (direction.isOK()) {
            Toast.makeText(mContext, "Success with status : " + direction.getStatus(), Toast.LENGTH_SHORT).show();

            mGoogleMap.addMarker(new MarkerOptions().position(origin).icon(BitmapDescriptorFactory
                    .fromResource(R.mipmap.homeicon)));
            mGoogleMap.addMarker(new MarkerOptions().position(destination).icon(BitmapDescriptorFactory
                    .fromResource(R.mipmap.technician)));

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            mGoogleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 3, polylineColor));
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {

        Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void init() {
        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void apiDirectionCall() {
        Log.d(TAG, "apiDirectionCall latitude: " + latitude);
        Log.d(TAG, "apiDirectionCall longitude: " + longitude);
        if (!apiCallAlreadyInvoked) {
            String session = Constants.getSession(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_START_TRIP;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.startTrip(session, String.valueOf(order_id), String.valueOf(latitude), String.valueOf(longitude));
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    @Override
    public void onNoInternet(int serviceType) {

    }

    @Override
    public void showProgress(int serviceType) {
        showProgressDialog();
    }

    @Override
    public void hideProgress(int serviceType) {
        apiCallAlreadyInvoked = false;
        hideProgressDialog();
    }

    @Override
    public void onSuccessData(String response, int serviceType) {
        onCallbackReleaseToUpdateUI(serviceType, response, UIUtils.HANDLER_TIME_FAST);
    }

    @Override
    public void onSuccessException(String response, Throwable throwable, int serviceType) {

    }

    @Override
    public void onErrorData(Throwable throwable, int serviceType) {

    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {

            case ServerApi.CODE_START_TRIP: {
                parseResponseData(msg.getData().getString(ServerJsonResponseKey.RESPONSE, null), msg.what);
                break;
            }
        }
        return false;
    }


    private void onCallbackReleaseToUpdateUI(int serviceType, String response, int timeDelay) {
        Message msgObj = apiCallHandler.obtainMessage();
        msgObj.what = serviceType;
        Bundle b = new Bundle();
        b.putString(ServerJsonResponseKey.RESPONSE, response);
        msgObj.setData(b);
        apiCallHandler.sendMessageDelayed(msgObj, timeDelay);
    }

    private void parseResponseData(String response, int serviceType) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            Log.d(TAG, "jsonObject: " + jsonObject.toString());
            message = jsonObject.optString("message");
            int status = jsonObject.optInt("data");
            order_status = status;
            Intent intent = new Intent("updateMode");
            intent.putExtra("status", order_status);
            sendBroadcast(intent);
            Log.d(TAG, "after getting res statusCode : " + order_status);
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                Log.d(TAG, "success: " + success);
                if (success == 1) {

                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

                    Intent startTrip = new Intent(mContext, SocketService.class);
                    startTrip.putExtra("user_id", user_id);
                    startTrip.putExtra("fieldTechId", field_tech_id);
                    startTrip.putExtra("orderId", order_id);
                    startTrip.putExtra("fieldTechName", fieldTechName);
                    startTrip.putExtra("type", 1);
                    startTrip.setPackage("com.wekancode.testmybloodft.service");
                    mContext.startService(startTrip);
                    // start service
                    bindService(startTrip, mServiceConnection, Context.BIND_AUTO_CREATE);
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);
        }
    }

    private void showProgressDialog() {
       /* if (!m_Dialog.isShowing()) {
            m_ProgressBar.setVisibility(View.VISIBLE);
            m_Dialog.show();
        }*/
    }

    private void hideProgressDialog() {
        /*if (m_Dialog.isShowing()) {
            m_ProgressBar.setVisibility(View.GONE);
            m_Dialog.dismiss();
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != apiCallHandler) {
            apiCallHandler.removeCallbacksAndMessages(null);
            apiCallHandler = null;
        }
        apiViewPresenter.onDestroy();

        if (apiCallInvoked) {
            MyVolley.getInstance(getApplicationContext()).getRequestQueue().cancelAll(TAG);
        }
    }

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

    }

    @Override
    public void onProviderDisabled(String provider) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        //  Toast.makeText(getApplicationContext(), " Check GPS Connection ", Toast.LENGTH_SHORT).show();
        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.SocketBinder myBinder = (SocketService.SocketBinder) service;
            mBoundService = myBinder.getService();
            mServiceBound = true;
        }
    };


}


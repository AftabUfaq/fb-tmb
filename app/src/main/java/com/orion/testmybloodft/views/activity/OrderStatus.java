package com.orion.testmybloodft.views.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.orion.testmybloodft.R;
import com.orion.testmybloodft.api.ApiModel;
import com.orion.testmybloodft.api.ApiResponseView;
import com.orion.testmybloodft.api.ApiViewPresenter;
import com.orion.testmybloodft.api.ApiViewPresenterImpl;
import com.orion.testmybloodft.api.ServerApi;
import com.orion.testmybloodft.api.ServerJsonResponseKey;
import com.orion.testmybloodft.api.ServerParams;
import com.orion.testmybloodft.config.App;
import com.orion.testmybloodft.config.MyVolley;
import com.orion.testmybloodft.databinding.ActivityOrderStatusBinding;
import com.orion.testmybloodft.models.DataHolder;
import com.orion.testmybloodft.models.TestListMod;
import com.orion.testmybloodft.service.SocketService;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.socket.client.Socket;

/**
 * Created by Wekancode on 30-Mar-17.
 */

/**
 * This activity displays a order status of particular order
 */

public class OrderStatus extends AppCompatActivity implements ApiResponseView, Handler.Callback, LocationListener {
    private static final String TAG = OrderStatus.class.getSimpleName();

    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;
    private Activity mActivity;
    private Context mContext;

    private Dialog m_Dialog;
    private ProgressBar m_ProgressBar;
    private String from_tag, tmb_order_id, message, address, contact_number, patient_name, pickup_date, pickup_time = "";
    private int current_status, order_status, user_id, order_id, field_tech_id;
    private double destLatitude, destLongitude;
    private final int CALL_PHONE_REQUEST = 2;
    private final int CLOSE_PICKUP_REQUEST_CODE = 1001;
    private int removeItemPos = -1;
    private List<TestListMod> testListMods = new ArrayList<TestListMod>();
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 2000; // 0 minute
    private double latitude; // latitude
    public double longitude; // longitude
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
    private static final int REQUEST_CHECK_SETTINGS = 0;
    private final int PICK_LOCATION_REQUEST = 1;
    private SocketService mBoundService;
    private boolean mServiceBound = false;
    private Socket socketEmitObj;

//    @BindView(R.id.myRouteBtn)
//    RelativeLayout myRouteBtn;
//    @BindView(R.id.reachDestination)
//    RelativeLayout reachDestination;
//    @BindView(R.id.scanVacutainers)
//    RelativeLayout scanVacutainers;
//    @BindView(R.id.collectPayment)
//    RelativeLayout collectPayment;
//    @BindView(R.id.ivOrderStatus)
//    ImageView ivOrderStatus;
//    @BindView(R.id.ivReachDest)
//    ImageView ivReachDest;
//    @BindView(R.id.ivVacutainers)
//    ImageView ivVacutainers;
//    @BindView(R.id.ivPayment)
//    ImageView ivPayment;
//    @BindView(R.id.myRouteArrow)
//    ImageView myRouteArrow;
//    @BindView(R.id.reachDestArrow)
//    ImageView reachDestArrow;
//    @BindView(R.id.scanArrow)
//    ImageView scanArrow;
//    @BindView(R.id.closePickupArrow)
//    ImageView closePickupArrow;
//    @BindView(R.id.orderID)
//    TextView orderID;
//    @BindView(R.id.tvPatientName)
//    TextView tvPatientName;
//    @BindView(R.id.pickupDate)
//    TextView pickupDate;
//    @BindView(R.id.pickupTime)
//    TextView pickupTime;



    BroadcastReceiver updateMode = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra("status")) {
                order_status = intent.getIntExtra("status", 0);
                Log.d(TAG, "onReceive order_status: " + order_status);

                if (order_status == 2) {
                    // Scan
                    binding.scanVacutainers.setBackground(ContextCompat.getDrawable(mContext, R.drawable.schedule_bg));
                    binding.ivVacutainers.setBackgroundResource(R.mipmap.showmyroute);
                    binding.scanArrow.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    /*'order_status'=>[
            '1' => 'Scheduled Appointment',
            '2' => 'Sample Collected',
            '3' => 'Payment Collected',
            '4' => 'Sample Received',
            '5' => 'Sample Sent to Lab',
            '6' => 'Report Ready',
            '7' => 'Cancelled',
            '8' => 'Draft',
            '9' => 'Report(s) in Progress',
            '10' => 'FT On Route',
            '11' => 'FT Reached Destination'
            ]*/


    ActivityOrderStatusBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityOrderStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mActivity = this;
        mContext = this;
        handleclicks();
        if (updateMode != null)
            mContext.registerReceiver(updateMode, new IntentFilter("updateMode"), Context.RECEIVER_NOT_EXPORTED);


        googleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API).build();
        getCurrentLocation();

        init();
        if (getIntent() != null && getIntent().hasExtra("order_status")) {
            order_status = getIntent().getIntExtra("order_status", 0);
            current_status = order_status;
        }
        if (getIntent() != null && getIntent().hasExtra("from_tag")) {
            from_tag = getIntent().getStringExtra("from_tag");
        }
        if (getIntent() != null && getIntent().hasExtra("position")) {
            removeItemPos = getIntent().getIntExtra("position", -1);
        }
        Log.d(TAG, "from_tag: " + from_tag);
        Log.d(TAG, "order_status: " + order_status);
        Log.d(TAG, "current_status: " + current_status);
        if (order_status == 11) {
            binding.reachDestination.setBackground(ContextCompat.getDrawable(mContext, R.drawable.schedule_bg));
            binding.ivReachDest.setBackgroundResource(R.mipmap.showmyroute);
            binding.reachDestArrow.setVisibility(View.VISIBLE);
        } else if (order_status == 2) {
            // Reached destination
            binding.reachDestination.setBackground(ContextCompat.getDrawable(mContext, R.drawable.schedule_bg));
            binding.ivReachDest.setBackgroundResource(R.mipmap.showmyroute);
            binding.reachDestArrow.setVisibility(View.VISIBLE);

            binding.scanVacutainers.setBackground(ContextCompat.getDrawable(mContext, R.drawable.schedule_bg));
            binding.ivVacutainers.setBackgroundResource(R.mipmap.showmyroute);
            binding.scanArrow.setVisibility(View.VISIBLE);
        }

        if (getIntent() != null && getIntent().hasExtra("patient_name")) {
            patient_name = getIntent().getStringExtra("patient_name");
            binding.tvPatientName.setText(patient_name);
        }
        if (getIntent() != null && getIntent().hasExtra("pickup_date")) {
            pickup_date = getIntent().getStringExtra("pickup_date");
            String stdate[] = pickup_date.split("-");
            String monthSt = Constants.nameOfTheMonth(stdate[1]);
            String pickupDt = stdate[2] + " " + monthSt + " " + stdate[0];
            binding.pickupDate.setText(pickupDt);
        }
        if (getIntent() != null && getIntent().hasExtra("pickup_time")) {
            pickup_time = getIntent().getStringExtra("pickup_time");
            try {
                String pickUpTime = pickup_time;
                String[] time_split = pickUpTime.split(":");
                String _24HourTime = time_split[0] + ":" + time_split[1];
                SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
                SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
                Date _24HourDt = _24HourSDF.parse(_24HourTime);
                Log.d(TAG, "_12HourSDF: " + _12HourSDF.format(_24HourDt));
                binding.pickupTime.setText(_12HourSDF.format(_24HourDt));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (getIntent() != null && getIntent().hasExtra("address")) {
            address = getIntent().getStringExtra("address");
        }
        if (getIntent() != null && getIntent().hasExtra("contact_number")) {
            contact_number = getIntent().getStringExtra("contact_number");
        }
        if (getIntent() != null && getIntent().hasExtra("order_id")) {
            order_id = getIntent().getIntExtra("order_id", 0);
            Log.d(TAG, "order_id: " + order_id);
        }
        if (getIntent() != null && getIntent().hasExtra("field_tech_id")) {
            field_tech_id = getIntent().getIntExtra("field_tech_id", 0);
        }
        if (getIntent() != null && getIntent().hasExtra("user_id")) {
            user_id = getIntent().getIntExtra("user_id", 0);
        }
        if (getIntent() != null && getIntent().hasExtra("tmb_order_id")) {
            tmb_order_id = getIntent().getStringExtra("tmb_order_id");
            binding.orderID.setText("#" + tmb_order_id);
            if (Constants.isNetworkAvailable()) {
                apiOrderDetailsCall(tmb_order_id);
            } else {
                Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
            }
        }
        if (getIntent() != null && getIntent().hasExtra("latitude")) {
            destLatitude = getIntent().getDoubleExtra("latitude", 0.00);
            Log.d(TAG, "destLatitude: " + destLatitude);
        }
        if (getIntent() != null && getIntent().hasExtra("longitude")) {
            destLongitude = getIntent().getDoubleExtra("longitude", 0.00);
            Log.d(TAG, "destLongitude: " + destLongitude);
        }

        /*m_Dialog = new Dialog(SignIn.this, R.style.Theme_Transparent);
        m_Dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_Dialog.setContentView(R.layout.progress_loader);
        m_ProgressBar = (ProgressBar) m_Dialog.findViewById(R.id.progressBar);
        m_ProgressBar.setIndeterminate(true);
        //noinspection deprecation,deprecation
        m_ProgressBar.setIndeterminateDrawable(SignIn.this.getResources().getDrawable(R.drawable.loader_anim));
        m_Dialog.setCancelable(false);
        m_Dialog.setCanceledOnTouchOutside(false); */

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
        }
    }

    private void displayLocationSettingsRequest(Context context) {
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

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

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(OrderStatus.this, REQUEST_CHECK_SETTINGS);
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

    void handleclicks(){

        binding.backBtn.setOnClickListener(v -> onBackPressed());

        binding.detailBtn.setOnClickListener(v -> startActivity(new Intent(mContext, Summary.class).putExtra("tmb_order_id", tmb_order_id)));

        binding.myRouteBtn.setOnClickListener(v -> {
            Intent verifyIntent = new Intent(mContext, DirectionActivity.class);
            verifyIntent.putExtra("address", address);
            verifyIntent.putExtra("contact_number", contact_number);
            verifyIntent.putExtra("order_id", order_id);
            verifyIntent.putExtra("tmb_order_id", tmb_order_id);
            verifyIntent.putExtra("latitude", destLatitude);
            verifyIntent.putExtra("longitude", destLongitude);
            verifyIntent.putExtra("user_id", user_id);
            verifyIntent.putExtra("order_status", order_status);
            verifyIntent.putExtra("from", TAG);
            startActivity(verifyIntent);
        });

        binding.reachDestination.setOnClickListener(v -> {
            if (Constants.isNetworkAvailable()) {
                if (order_status == 10) {
                    apiEndTripCall();
                }
            } else {
                Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
            }
        });

        binding.collectPayment.setOnClickListener(v -> {
            if (order_status == 2) {
                Intent verifyIntent = new Intent(mContext, ClosePickup.class);
                verifyIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                verifyIntent.putExtra("tmb_order_id", tmb_order_id);
                verifyIntent.putExtra("position", removeItemPos);
                startActivityForResult(verifyIntent, CLOSE_PICKUP_REQUEST_CODE);
            } else {
                Log.d(TAG, "getStatus: " + order_status);
            }
        });

        binding.scanVacutainers.setOnClickListener(v -> {
            if (order_status == 11) {
                Intent verifyIntent = new Intent(mContext, ScanVacutainer.class);
                verifyIntent.putExtra("patient_name", patient_name);
                verifyIntent.putExtra("pickup_date", pickup_date);
                verifyIntent.putExtra("pickup_time", pickup_time);
                verifyIntent.putExtra("address", address);
                verifyIntent.putExtra("contact_number", contact_number);
                verifyIntent.putExtra("order_id", order_id);
                verifyIntent.putExtra("tmb_order_id", tmb_order_id);
                verifyIntent.putExtra("latitude", destLatitude);
                verifyIntent.putExtra("longitude", destLongitude);
                verifyIntent.putExtra("user_id", user_id);
                verifyIntent.putExtra("order_status", order_status);
                startActivity(verifyIntent);
            } else {
                Log.d(TAG, "getOrderStatus: " + order_status);
            }
        });

        binding.callBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact_number));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                            CALL_PHONE_REQUEST);
                }
                // to handle the case where the user grants the permission. See the documentation
                return;
            }
            startActivity(intent);
        });
    }
//    @OnClick({R.id.backBtn, R.id.detailBtn, R.id.myRouteBtn, R.id.reachDestination, R.id.scanVacutainers, R.id.collectPayment, R.id.callBtn})
//    void clickListenterOrderStatus(View view) {
//        // TODO ...
//
//        switch (view.getId()) {
//            case R.id.backBtn: {
//                onBackPressed();
//                break;
//            }
//            case R.id.detailBtn: {
//                startActivity(new Intent(mContext, Summary.class).putExtra("tmb_order_id", tmb_order_id));
//                break;
//            }
//            case R.id.myRouteBtn: {
//                Intent verifyIntent = new Intent(mContext, DirectionActivity.class);
//                verifyIntent.putExtra("address", address);
//                verifyIntent.putExtra("contact_number", contact_number);
//                verifyIntent.putExtra("order_id", order_id);
//                verifyIntent.putExtra("tmb_order_id", tmb_order_id);
//                verifyIntent.putExtra("latitude", destLatitude);
//                verifyIntent.putExtra("longitude", destLongitude);
//                verifyIntent.putExtra("user_id", user_id);
//                verifyIntent.putExtra("order_status", order_status);
//                verifyIntent.putExtra("from", TAG);
//                startActivity(verifyIntent);
//                break;
//            }
//            case R.id.reachDestination: {
//                if (Constants.isNetworkAvailable()) {
//                    if (order_status == 10) {
//                        apiEndTripCall();
//                    }
//                } else {
//                    Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
//                }
//                break;
//            }
//            case R.id.collectPayment: {
//                if (order_status == 2) {
//                    Intent verifyIntent = new Intent(mContext, ClosePickup.class);
//                    verifyIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                    verifyIntent.putExtra("tmb_order_id", tmb_order_id);
//                    verifyIntent.putExtra("position", removeItemPos);
//                    startActivityForResult(verifyIntent, CLOSE_PICKUP_REQUEST_CODE);
//                } else {
//                    Log.d(TAG, "getStatus: " + order_status);
//                }
//                break;
//            }
//            case R.id.scanVacutainers: {
//                if (order_status == 11) {
//                    Intent verifyIntent = new Intent(mContext, ScanVacutainer.class);
//                    verifyIntent.putExtra("patient_name", patient_name);
//                    verifyIntent.putExtra("pickup_date", pickup_date);
//                    verifyIntent.putExtra("pickup_time", pickup_time);
//                    verifyIntent.putExtra("address", address);
//                    verifyIntent.putExtra("contact_number", contact_number);
//                    verifyIntent.putExtra("order_id", order_id);
//                    verifyIntent.putExtra("tmb_order_id", tmb_order_id);
//                    verifyIntent.putExtra("latitude", destLatitude);
//                    verifyIntent.putExtra("longitude", destLongitude);
//                    verifyIntent.putExtra("user_id", user_id);
//                    verifyIntent.putExtra("order_status", order_status);
//                    startActivity(verifyIntent);
//                } else {
//                    Log.d(TAG, "getOrderStatus: " + order_status);
//                }
//                break;
//            }
//            case R.id.callBtn: {
//                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact_number));
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
//                                CALL_PHONE_REQUEST);
//                    }
//                    // to handle the case where the user grants the permission. See the documentation
//                    return;
//                }
//                startActivity(intent);
//                break;
//            }
//
//        }
//    }

    private void init() {

        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void apiEndTripCall() {
        if (!apiCallAlreadyInvoked) {
            String session = Constants.getSession(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_END_TRIP;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.endTrip(session, String.valueOf(order_id), String.valueOf(latitude), String.valueOf(longitude));
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    private void apiOrderDetailsCall(String order_id) {
        if (!apiCallAlreadyInvoked) {
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_ORDER_DETAILS;
            apiModel.context = getApplicationContext();
            apiModel.method = 0;
            apiModel.params = ServerParams.getOrderDetails(order_id);
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
            case ServerApi.CODE_ORDER_DETAILS:
            case ServerApi.CODE_END_TRIP: {
                parseResponseData(msg.getData().getString(ServerJsonResponseKey.RESPONSE, null), msg.what);
                break;
            }
        }
        return false;
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
            Log.i(TAG, "responseStr: " + response);
            JSONObject jsonObject = new JSONObject(response);
            Log.i(TAG, "jsonObject: " + jsonObject.toString());
            message = jsonObject.optString("message");
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                if (success == 1) {
                    if (ServerApi.CODE_END_TRIP == serviceType) {
                        successEndTripJsonResponse(jsonObject, success);
                    } else if (ServerApi.CODE_ORDER_DETAILS == serviceType) {
                        successOrderDetailsJsonResponse(jsonObject);
                    }
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);
        }
    }

    private void successEndTripJsonResponse(JSONObject jsonObject, int success) {
        try {
            Log.d(TAG, "successEndTripJsonResponse: " + jsonObject.toString());
            Log.d(TAG, "statusCode : " + success);
            int status = jsonObject.optInt("data");
            order_status = status;
            Log.d(TAG, "after getting res statusCode: " + status);
            /*Intent intent = new Intent("updateMode");
            intent.putExtra("status", order_status);
            sendBroadcast(intent);*/
            if (success == 1) {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                binding.reachDestination.setBackground(ContextCompat.getDrawable(mContext, R.drawable.schedule_bg));
                binding.ivReachDest.setBackgroundResource(R.mipmap.showmyroute);
                binding.reachDestArrow.setVisibility(View.VISIBLE);

                // Passing for Broadcast Receiver
                Intent inttt = new Intent(Constants.SOCKET_ENDTRIP);
                inttt.putExtra("status", 0);
                sendBroadcast(inttt);

                /*if (mServiceBound) {
                    // stop service
                    mContext.unbindService(mServiceConnection);
                    mServiceBound = false;
                }*/
                // NNN
                //mContext.stopService(new Intent(mContext, SocketService.class));
               /* socketEmitObj = App.getInstance().getSocket();
                socketEmitObj.off();
                socketEmitObj.disconnect();
                isMyServiceRunning(SocketService.class);*/

            } else {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        boolean isRunning = false;
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                isRunning = true;
                return isRunning;
            }
        }
        Log.d(TAG, "isMyServiceRunning: " + isRunning);
        return isRunning;

    }


    private void successOrderDetailsJsonResponse(JSONObject jsonObject) {
        try {
            JSONObject jsonArrayObj = jsonObject.optJSONObject(ServerJsonResponseKey.DATA);
            if (null != jsonArrayObj) {
                // Patient
                String patient_name = jsonArrayObj.optString("patient_name");
                String gender = jsonArrayObj.optString("gender");
                int age = jsonArrayObj.optInt("age");
                String pickup_time_date = jsonArrayObj.optString("pickup_time_date");
                String pickup_time_ampm = jsonArrayObj.optString("pickup_time_ampm");
                String patient_address = jsonArrayObj.optString("patient_address").trim();
                String contact_number = jsonArrayObj.optString("contact_number").trim();
                String stdate[] = pickup_time_date.split("-");
                String monthSt = Constants.nameOfTheMonth(stdate[1]);
                String pickupDate = stdate[0] + " " + monthSt + " " + stdate[2];
                String profile = patient_name + "|" + gender + "|" + age + "|" + tmb_order_id + "|" + pickupDate + " - " + pickup_time_ampm + "|" + patient_address
                        + "|" + contact_number;
                Constants.setProfileDetails(mActivity, profile);

                // Billing
                JSONArray tsetArrayObj = jsonObject.optJSONArray(ServerJsonResponseKey.TEST_LIST);
                Log.i(TAG, " tsetArrayObj ::: " + tsetArrayObj.toString());
                if (!tsetArrayObj.equals("[]")) {
                    if (null != tsetArrayObj) {
                        if (tsetArrayObj.length() > 0) {
                            for (int i = 0; i < tsetArrayObj.length(); i++) {
                                JSONObject areaObj = tsetArrayObj.optJSONObject(i);
                                Gson gson = new Gson();
                                TestListMod testListMod = gson.fromJson(areaObj.toString(), TestListMod.class);
                                testListMods.add(testListMod);
                            }
                            DataHolder dataHolder = new DataHolder();
                            dataHolder.setTestList(testListMods);
                        }
                    }
                }
                String labName = jsonArrayObj.optString("labName");
                String pay_mode = jsonArrayObj.optString("pay_mode");
                String voucher_discount = jsonArrayObj.optString("voucher_discount");
                String total_amount = jsonArrayObj.optString("total_amount");
                String bucketUrl = jsonArrayObj.optString("bucketUrl");
                String labImage = jsonArrayObj.optString("labImage");
                String labLogo = bucketUrl + labImage;
                String collection_charge = jsonArrayObj.optString("collection_charge");
                String billing = labName + "|" + pay_mode + "|" + voucher_discount + "|" + total_amount
                        + "|" + contact_number + "|" + labLogo + "|" + collection_charge;
                Constants.setBillingDetails(mActivity, billing);
            }
            // Set Test list
            ((App) this.getApplication()).setTestList(testListMods);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProgressDialog() {
        /*if (!m_Dialog.isShowing()) {
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
    protected void onResume() {
        super.onResume();
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

        try {
            if (updateMode != null)
                unregisterReceiver(updateMode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i(TAG, "Socket service stopped...");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("11112", "requestCode : " + requestCode + ", resultCode(-1) : " + resultCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == CLOSE_PICKUP_REQUEST_CODE) {
                if (data != null) {
                    boolean maintainActivityView = data.getBooleanExtra("maintainActivityView", false);
                    if (maintainActivityView) {
                        Intent verifyIntent = new Intent();
                        verifyIntent.putExtra("refreshItem", data.getIntExtra("refreshItem", 0));
                        verifyIntent.putExtra("position", data.getIntExtra("position", -1));
                        verifyIntent.putExtra("maintainActivityView", maintainActivityView);
                        setResult(RESULT_OK, verifyIntent);
                        finish();
                    }
                }
            }
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


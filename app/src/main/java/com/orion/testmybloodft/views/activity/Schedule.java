package com.orion.testmybloodft.views.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.material.tabs.TabLayout;
import com.orion.testmybloodft.R;
import com.orion.testmybloodft.api.ApiModel;
import com.orion.testmybloodft.api.ApiResponseView;
import com.orion.testmybloodft.api.ApiViewPresenter;
import com.orion.testmybloodft.api.ApiViewPresenterImpl;
import com.orion.testmybloodft.api.ServerApi;
import com.orion.testmybloodft.api.ServerJsonResponseKey;
import com.orion.testmybloodft.api.ServerParams;
import com.orion.testmybloodft.config.MyVolley;
import com.orion.testmybloodft.databinding.ActivityScheduleBinding;
import com.orion.testmybloodft.fragment.FragmentPickedUp;
import com.orion.testmybloodft.fragment.FragmentToday;
import com.orion.testmybloodft.fragment.FragmentTomorrow;
import com.orion.testmybloodft.fragment.FullScreenDialogFragment;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;
import com.orion.testmybloodft.views.adapter.TabsPagerAdapter;
import com.orion.testmybloodft.views.adapter.TodayAdapter;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Created by Wekancode on 30-Mar-17.
 */

/**
 * This activity displays a scheduled orders along with tabs (Today, Tomorrow & Picked up)
 */

public class Schedule extends AppCompatActivity implements ApiResponseView, Handler.Callback, TodayAdapter.PermissionCheck {
    private static final String TAG = Schedule.class.getSimpleName();

    private Activity mActivity;
    private Context mContext;
    private TabsPagerAdapter adapter;
    private FullScreenDialogFragment dialogFragment;
    private String message = "";
    private final int PICK_LOCATION_REQUEST = 1;
    private int order_status;
    private final int ORDER_SUMMARY_REQUEST_CODE = 1002;
    private int scheduleTabPos;
    private int type;
    private int new_push;
    private int count;
    private String date = "";
    private GoogleApiClient googleApiClient;
    private Activity act = Schedule.this;
    private TodayAdapter todayAdapter;
    private static final int REQUEST_CHECK_SETTINGS = 0;

    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;
    private int positionNew;
    private Intent intent;

//    @BindView(R.id.pager)
//    ViewPager viewPager;
//    @BindView(R.id.tablayout)
//    TabLayout tabLayout;
//    @BindView(R.id.ivGreenDot)
//    ImageView ivGreenDot;

    BroadcastReceiver tabUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra("scheduleTabPos")) {
                scheduleTabPos = intent.getIntExtra("scheduleTabPos", -1);
                Log.d(TAG, "onReceive scheduleTabPos: " + scheduleTabPos);
            }
            if (intent != null && intent.hasExtra("type")) {
                type = intent.getIntExtra("type", 0);
                Log.d(TAG, "onReceive type: " + type);
            }
            if (intent != null && intent.hasExtra("date")) {
                date = intent.getStringExtra("date");
                Log.d(TAG, "onReceive date: " + date);
            }
            if (intent != null && intent.hasExtra("new_push")) {
                new_push = intent.getIntExtra("new_push", 0);
                Log.d(TAG, "onReceive new_push: " + new_push);
            }

            //boolean background = isAppIsInBackground(mContext);

            if (new_push == 0) {
                binding.ivGreenDot.setVisibility(View.GONE);
            } else {
                binding.ivGreenDot.setVisibility(View.VISIBLE);
            }

            if (type == 13) {
                Log.d(TAG, "Cancelled && Today && Tomorrow");
                // Tomorrow
                Calendar calendar1 = Calendar.getInstance();
                calendar1.add(Calendar.DAY_OF_YEAR, 1);
                Date tomorrow = calendar1.getTime();
                DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                String tomorrowAsString = dateFormat1.format(tomorrow);

                if (!date.equals(tomorrowAsString)) {
                    Log.d(TAG, "date: " + date);
                    binding.pager.setCurrentItem(0, true);
                    Fragment fragment = adapter.getItem(0);
                    if (fragment instanceof FragmentToday) {
                        FragmentToday today = (FragmentToday) fragment;
                        today.refreshTodayList();
                    }
                } else {
                    Log.d(TAG, "tomorrowAsString: " + tomorrowAsString);
                    binding.pager.setCurrentItem(1, true);
                    Fragment fragment = adapter.getItem(1);
                    if (fragment instanceof FragmentTomorrow) {
                        FragmentTomorrow fragmentTomorrow = (FragmentTomorrow) fragment;
                        fragmentTomorrow.refreshTomorrowList();
                    }
                }
            } else if (type == 14 && scheduleTabPos == 1) {
                Log.d(TAG, "Tomorrow");
                Fragment fragment = adapter.getItem(1);
                if (fragment instanceof FragmentTomorrow) {
                    binding.pager.setCurrentItem(1, true);
                    FragmentTomorrow fragmentTomorrow = (FragmentTomorrow) fragment;
                    fragmentTomorrow.refreshTomorrowList();
                }
            } else if (type == 15) {
                Log.d(TAG, "Today && Tomorrow");
                Calendar calendar = Calendar.getInstance();
                Date today = calendar.getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String todayAsString = dateFormat.format(today);
                if (!date.equals(todayAsString)) {
                    Log.d(TAG, "date: " + date);
                    binding.pager.setCurrentItem(1, true);
                    Fragment fragment = adapter.getItem(1);
                    if (fragment instanceof FragmentTomorrow) {
                        FragmentTomorrow fragmentTomorrow = (FragmentTomorrow) fragment;
                        fragmentTomorrow.refreshTomorrowList();
                    }
                } else {
                    Log.d(TAG, "todayAsString: " + todayAsString);
                    binding.pager.setCurrentItem(0, true);
                    Fragment fragment = adapter.getItem(0);
                    if (fragment instanceof FragmentToday) {
                        FragmentToday today1 = (FragmentToday) fragment;
                        today1.refreshTodayList();
                    }
                }
            }
        }
    };

    BroadcastReceiver updateMode = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra("status")) {
                order_status = intent.getIntExtra("status", 0);
                Log.d(TAG, "onReceive order_status: " + order_status);
            }
        }
    };

    ActivityScheduleBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mActivity = this;
        mContext = this;


//        binding.launchScannerBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(Schedule.this,QrCodeActivity.class));
//            }
//        });
        handleClick();
        double d  = 23.132679999999997;
        DecimalFormat dFormat = new DecimalFormat("#.######");
        d = Double.valueOf(dFormat .format(d));
        Log.d(TAG, "roundOff: "+d);


        init();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).build();
        if (tabUpdate != null)
            registerReceiver(tabUpdate, new IntentFilter("tabUpdate"), Context.RECEIVER_NOT_EXPORTED);

        adapter = new TabsPagerAdapter(getSupportFragmentManager(), mContext);

        // Set the binding.pagerAdapter into binding.pager
        binding.pager.setAdapter(adapter);
        binding.pager.setOffscreenPageLimit(3);
        binding.tablayout.setupWithViewPager(binding.pager);
        binding.pager.setCurrentItem(0);
        Log.d(TAG, "currentTab: " + binding.pager.getCurrentItem());

        binding.pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tablayout));
        binding.tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabPos = tab.getPosition();
                Log.d("11112", "onTabSelected getPosition : " + tabPos);
                updateFragment(tabPos, TAG);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Bg-push
        Handler handler = new Handler();
        // in-app
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("in-app", "before: " + Constants.getInAppFirstTime(mContext));
                onPushForeground();
            }
        }, 2000);
    }

    private void onPushBackground() {
        Log.d("bgPush", "before: " + Constants.getFirstTimeLaunch(mContext));
        if (Constants.getFirstTimeLaunch(mContext)) {
            Log.d("bgPush", "after: " + Constants.getFirstTimeLaunch(mContext));
            Constants.setFirstTimeLaunch(mContext, false);

            int mType = Constants.getType(mContext);
            int mScheduleTabPos = Constants.getTabPos(mContext);
            String mDate = Constants.getDate(mContext);
            Log.d("bgPush", "getDate: " + mDate);
            if (mType == 13) {
                Log.d("bgPush", "Cancelled && Today && Tomorrow");
                // Tomorrow
                Calendar calendar1 = Calendar.getInstance();
                calendar1.add(Calendar.DAY_OF_YEAR, 1);
                Date tomorrow = calendar1.getTime();
                DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                String tomorrowAsString = dateFormat1.format(tomorrow);

                if (!mDate.equals(tomorrowAsString)) {
                    Log.d("bgPush", "Cancelled Today: " + mDate);
                    binding.pager.setCurrentItem(0, true);
                    Fragment fragment = adapter.getItem(0);
                    if (fragment instanceof FragmentToday) {
                        FragmentToday today = (FragmentToday) fragment;
                        today.refreshTodayList();
                    }
                } else {
                    Log.d("bgPush", "Cancelled Tomorrow: " + tomorrowAsString);
                    binding.pager.setCurrentItem(1, true);
                    Fragment fragment = adapter.getItem(1);
                    if (fragment instanceof FragmentTomorrow) {
                        FragmentTomorrow fragmentTomorrow = (FragmentTomorrow) fragment;
                        fragmentTomorrow.refreshTomorrowList();
                    }
                }
            } else if (mType == 14 && mScheduleTabPos == 1) {
                Log.d("bgPush", "Summary Tomorrow");
                binding.pager.setCurrentItem(1, true);
                Fragment fragment = adapter.getItem(1);
                if (fragment instanceof FragmentTomorrow) {
                    FragmentTomorrow tomorrow = (FragmentTomorrow) fragment;
                    tomorrow.refreshTomorrowList();
                }
            } else if (mType == 15) {
                Log.d("bgPush", "Today && Tomorrow");
                Calendar calendar = Calendar.getInstance();
                Date today = calendar.getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String todayAsString = dateFormat.format(today);
                Log.d("bgPush", "date: " + mDate);
                Log.d("bgPush", "todayAsString: " + todayAsString);
                if (!mDate.equals(todayAsString)) {
                    Log.d("bgPush", "New Bookings Tomorrow: ");
                    binding.pager.setCurrentItem(1, true);
                    Fragment fragment = adapter.getItem(1);
                    if (fragment instanceof FragmentTomorrow) {
                        FragmentTomorrow tomorrow = (FragmentTomorrow) fragment;
                        tomorrow.refreshTomorrowList();
                    }
                } else {
                    Log.d("bgPush", "New Bookings Today: ");
                    binding.pager.setCurrentItem(0, true);
                    Fragment fragment = adapter.getItem(0);
                    if (fragment instanceof FragmentToday) {
                        FragmentToday today1 = (FragmentToday) fragment;
                        today1.refreshTodayList();
                    }
                }
            }
        }
    }

    private void onPushForeground() {
        if (Constants.getInAppFirstTime(mContext)) {
            Log.d("in-app", "after: " + Constants.getInAppFirstTime(mContext));
            Constants.setInAppFirstTime(mContext, false);

            int mType = Constants.getInAppType(mContext);
            int mScheduleTabPos = Constants.getInAppTabPos(mContext);
            String mDate = Constants.getInAppDate(mContext);
            Log.d("in-app", "getDate: " + mDate);
            if (mType == 13) {
                Log.d("in-app", "Cancelled && Today && Tomorrow");
                // Tomorrow
                Calendar calendar1 = Calendar.getInstance();
                calendar1.add(Calendar.DAY_OF_YEAR, 1);
                Date tomorrow = calendar1.getTime();
                DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                String tomorrowAsString = dateFormat1.format(tomorrow);

                if (!mDate.equals(tomorrowAsString)) {
                    Log.d("in-app", "Cancelled Today: " + mDate);
                    binding.pager.setCurrentItem(0, true);
                    Fragment fragment = adapter.getItem(0);
                    if (fragment instanceof FragmentToday) {
                        FragmentToday today = (FragmentToday) fragment;
                        today.refreshTodayList();
                    }
                } else {
                    Log.d("in-app", "Cancelled Tomorrow: " + tomorrowAsString);
                    binding.pager.setCurrentItem(1, true);
                    Fragment fragment = adapter.getItem(1);
                    if (fragment instanceof FragmentTomorrow) {
                        FragmentTomorrow fragmentTomorrow = (FragmentTomorrow) fragment;
                        fragmentTomorrow.refreshTomorrowList();
                    }
                }
            } else if (mType == 14 && mScheduleTabPos == 1) {
                Log.d("in-app", "Summary Tomorrow");
                binding.pager.setCurrentItem(1, true);
                Fragment fragment = adapter.getItem(1);
                if (fragment instanceof FragmentTomorrow) {
                    FragmentTomorrow tomorrow = (FragmentTomorrow) fragment;
                    tomorrow.refreshTomorrowList();
                }
            } else if (mType == 15) {
                Log.d("in-app", "Today && Tomorrow");
                Calendar calendar = Calendar.getInstance();
                Date today = calendar.getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String todayAsString = dateFormat.format(today);
                Log.d("in-app", "date: " + mDate);
                Log.d("in-app", "todayAsString: " + todayAsString);
                if (!mDate.equals(todayAsString)) {
                    Log.d("in-app", "New Bookings Tomorrow: ");
                    binding.pager.setCurrentItem(1, true);
                    Fragment fragment = adapter.getItem(1);
                    if (fragment instanceof FragmentTomorrow) {
                        FragmentTomorrow tomorrow = (FragmentTomorrow) fragment;
                        tomorrow.refreshTomorrowList();
                    }
                } else {
                    Log.d("in-app", "New Bookings Today: ");
                    binding.pager.setCurrentItem(0, true);
                    Fragment fragment = adapter.getItem(0);
                    if (fragment instanceof FragmentToday) {
                        FragmentToday today1 = (FragmentToday) fragment;
                        today1.refreshTodayList();
                    }
                }
            }
        }
        Log.d("bgPush", "before: " + Constants.getInAppFirstTime(mContext));
        onPushBackground();
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        Log.d(TAG, "isAppIsInBackground: " + isInBackground);
        return isInBackground;
    }

    private void init() {
        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void apiPushCountCall() {
        if (!apiCallAlreadyInvoked) {
            String session = Constants.getSession(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_COUNT_NOTIFICATION;
            apiModel.context = mContext;
            apiModel.method = 1;
            apiModel.params = ServerParams.notifyCount(session);
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
            case ServerApi.CODE_COUNT_NOTIFICATION: {
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
            Log.d(TAG, "parseResponseData: " + response);
            JSONObject jsonObject = new JSONObject(response);
            message = jsonObject.optString("message");
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                if (success == 1) {
                    if (success == 1) {
                        if (ServerApi.CODE_COUNT_NOTIFICATION == serviceType) {
                            successPushCountJsonResponse(jsonObject);
                        }
                    } else {
                        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);
        }
    }

    private void successPushCountJsonResponse(JSONObject jsonObject) {
        try {
            if (null != jsonObject) {
                count = jsonObject.optInt("data");
                Log.d(TAG, "successPushCountJsonResponse count: " + count);
                if (count == 0) {
                   binding. ivGreenDot.setVisibility(View.GONE);
                } else {
                    binding.ivGreenDot.setVisibility(View.VISIBLE);
                }
            }
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


    private void openPopup() {
        dialogFragment = FullScreenDialogFragment.newInstance("FullScreenDialogFragment", mActivity);
        // SETS the target fragment for use later when sending results
        // customDialogFragment.setTargetFragment(this, 22);
        // customDialogFragment.sho(getActivity().getSupportFragmentManager(), "GifLoaderFragment");
        if (mActivity != null && getSupportFragmentManager() != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(dialogFragment, "FullScreenDialogFragment");
            ft.commitAllowingStateLoss();
        }
    }

    void handleClick(){
        binding.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPopup();
            }
        });

        binding.notifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent verifyIntent = new Intent(mContext, Notification.class);
                startActivityForResult(verifyIntent, ORDER_SUMMARY_REQUEST_CODE);
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (dialogFragment != null && dialogFragment.getDialog() != null
                && dialogFragment.getDialog().isShowing()) {
            //dialog is showing so do something
            Log.d(TAG, "dialog is showing: ");
            dialogFragment.dismiss();
        } else {
            //dialog is not showing
            Log.d(TAG, "dialog is not showing: ");
        }

        if (updateMode != null) {
            registerReceiver(updateMode, new IntentFilter("updateMode"), Context.RECEIVER_NOT_EXPORTED);
        }

        if (Constants.isNetworkAvailable()) {
            apiPushCountCall();
        } else {
            Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
        }
        // New push
        if (count == 0) {
            binding.ivGreenDot.setVisibility(View.GONE);
        } else {
            binding.ivGreenDot.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != apiCallHandler) {
            apiCallHandler.removeCallbacksAndMessages(null);
            apiCallHandler = null;
        }
        apiViewPresenter.onDestroy();
        googleApiClient.disconnect();

        if (apiCallInvoked) {
            MyVolley.getInstance(getApplicationContext()).getRequestQueue().cancelAll(TAG);
        }

        try {
            /*if(updateMode!=null)
                unregisterReceiver(updateMode);*/
            if (tabUpdate != null)
                unregisterReceiver(tabUpdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            if (updateMode != null)
                unregisterReceiver(updateMode);
            /*if(tabUpdate!=null)
                unregisterReceiver(tabUpdate);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (data != null) {
            boolean notificationAct = data.getBooleanExtra("notification_main_act", false);
            String created_at = data.getStringExtra("created_at");
            Log.d("11112", "onActivityResult created_at: " + created_at);

            switch (requestCode) {
                case REQUEST_CHECK_SETTINGS:
                    switch (resultCode) {
                        case Activity.RESULT_OK: {
                            // All required changes were successfully made
                            //Toast.makeText(act, "Location enabled by user!", Toast.LENGTH_LONG).show();
                            // todayAdapter.returnAllow(positionNew);
                            Toast.makeText(mActivity, "Please wait map loading", Toast.LENGTH_LONG).show();
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    Log.i(TAG, "All location settings are satisfied.");
                                    // todayAdapter.returnAllow(positionNew);
                                    // interacterPermis.returnResponse(positionNew);
                                    Log.i("Intent Datas", "" + intent.toString());
                                    Log.i("Intent  Latitude", "" + intent.hasExtra("latitude"));
                                    startActivity(intent);
                                }
                            }, 4000);

                            break;
                        }
                        case Activity.RESULT_CANCELED: {
                            // The user was asked to change settings, but chose not to
                            Toast.makeText(act, "Location not enabled, user cancelled.", Toast.LENGTH_LONG).show();
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                    break;
            }

            if (notificationAct) {
                int type = data.getIntExtra("type", -1);
                Log.d("11112", "onActivityResult type: " + type);
                if (type != -1) {
                    Fragment fragmentNoti = getFragment(type);
                    if (fragmentNoti != null) {
                        if (fragmentNoti instanceof FragmentToday) {
                            Log.d("11112", "onActivityResult Today && Tomorrow: ");
                            Calendar calendar = Calendar.getInstance();
                            Date today = calendar.getTime();
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String todayAsString = dateFormat.format(today);
                            Log.d("11112", "created_at: " + created_at);
                            Log.d("11112", "todayAsString: " + todayAsString);
                            if (created_at != null) {
                                if (!created_at.equals(todayAsString)) {
                                    Log.d("11112", "Tomorrow: ");
                                    binding.pager.setCurrentItem(1, true);
                                } else {
                                    Log.d("11112", "Today: ");
                                    binding.pager.setCurrentItem(0, true);
                                }
                            }
                        } else if (fragmentNoti instanceof FragmentTomorrow) {
                            Log.d("11112", "onActivityResult tomorrow: ");
                            binding.pager.setCurrentItem(1, true);
                        }
                    }
                }
            } else if (requestCode == Activity.RESULT_OK) {
                // All required changes were successfully made
                Toast.makeText(act, "Location enabled by user!", Toast.LENGTH_LONG).show();

            } else {
                try {
                    Fragment fragment = adapter.getItem(binding.pager.getCurrentItem());
                    if (fragment != null) {
                        if (fragment instanceof FragmentToday) {
                            FragmentToday fragmentToday = (FragmentToday) fragment;
                            Log.d("11112", "onActivityResult fragmentToday: ");
                            fragmentToday.onActivityResult(requestCode, resultCode, data);
                        } else if (fragment instanceof FragmentTomorrow) {
                            FragmentTomorrow tomorrow = (FragmentTomorrow) fragment;
                            Log.d("11112", "onActivityResult tomorrow: ");
                            tomorrow.onActivityResult(requestCode, resultCode, data);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (Constants.isNetworkAvailable()) {
                apiPushCountCall();
            } else {
                Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void updateFragment(int position, String from) {
        try {
            Log.d("11112", "updateFragment position: " + position);
            Fragment fragment = adapter.getItem(position);
            if (fragment != null) {
                if (fragment instanceof FragmentToday) {
                    binding.pager.setCurrentItem(position, true);
                } else if (fragment instanceof FragmentTomorrow) {
                    binding.pager.setCurrentItem(position, true);
                } else if (fragment instanceof FragmentPickedUp) {
                    FragmentPickedUp fragmentPickedUp = (FragmentPickedUp) fragment;
                    fragmentPickedUp.updateFromParent(from);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Fragment getFragment(int position) {
        try {
            Log.d(TAG, "getFragment position: " + position);
            Fragment fragment = adapter.getItem(position);
            if (fragment != null) {
                return fragment;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void check(int position) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        PICK_LOCATION_REQUEST);
            }
            // to handle the case where the user grants the permission. See the documentation
            return;
        } else {
            locationRequests(position);

        }
    }


    public void dataPass(Intent intt) {

        intent = intt;

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

        }

    }

    public void locationRequests(int position) {
        positionNew = position;
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

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "All location settings are satisfied.");
                                // todayAdapter.returnAllow(positionNew);
                                // interacterPermis.returnResponse(positionNew);
                                Log.i("Intent Datas", "" + intent.toString());
                                Log.i("Intent  Latitude", "" + intent.hasExtra("latitude"));
                                startActivity(intent);
                            }
                        }, 1000);

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                        //finish();
                        /// startActivity(intent);
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(Schedule.this, REQUEST_CHECK_SETTINGS);
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

}


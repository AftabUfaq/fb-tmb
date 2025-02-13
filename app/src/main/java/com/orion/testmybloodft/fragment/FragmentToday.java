package com.orion.testmybloodft.fragment;

/**
 * Created by Arun on 7/7/2017.
 */

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orion.testmybloodft.R;
import com.orion.testmybloodft.api.ApiModel;
import com.orion.testmybloodft.api.ApiResponseView;
import com.orion.testmybloodft.api.ApiViewPresenter;
import com.orion.testmybloodft.api.ApiViewPresenterImpl;
import com.orion.testmybloodft.api.ServerApi;
import com.orion.testmybloodft.api.ServerJsonResponseKey;
import com.orion.testmybloodft.api.ServerParams;
import com.orion.testmybloodft.config.MyVolley;
import com.orion.testmybloodft.databinding.FragmentBilllingBinding;
import com.orion.testmybloodft.databinding.FragmentTodayNewBinding;
import com.orion.testmybloodft.models.TodayMod;
import com.orion.testmybloodft.service.SocketService;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;
import com.orion.testmybloodft.views.activity.Schedule;
import com.orion.testmybloodft.views.adapter.TodayAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import static android.app.Activity.RESULT_OK;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Here we loading today orders and returns a list of orders
 */

@SuppressLint("ValidFragment")
public class FragmentToday extends Fragment implements ApiResponseView, Handler.Callback {
    private static final String TAG = FragmentToday.class.getSimpleName();
    private Context mContext;
    private String message = "";
    private List<TodayMod> todayMods = new ArrayList<TodayMod>();
    public TodayAdapter scheduleAdapter;
    private String todayAsString, tomorrowAsString, yesterdayAsString = "";
    private final int GPS_REQUEST = 1;
    private final int CALL_PHONE_REQUEST = 2;
    private final int LOCATION_REQUEST = 3;
    private final int UPDATE_LIST_REQUEST_CODE = 1000;
    private int order_status;
    private int order_id, field_tech_id, user_id, status;
    private SocketService mBoundService;
    private String fieldTechName = "";
    private boolean mServiceBound = false;

    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;

//    @BindView(R.id.todayRecyclerView)
//    RecyclerView todayRecyclerView;
//    @BindView(R.id.errorMessage)
//    TextView errorMessage;
//    @BindView(R.id.todayTv)
//    TextView todayTv;
////    @BindView(R.id.parentLl)
////    LinearLayout parentLl;
//    @BindView(R.id.pickupsLeftTv)
//    TextView pickupsLeftTv;
//    @BindView(R.id.swifeRefresh)
//    SwipeRefreshLayout mSwipeRefreshLayout;

    private int update = 0;
    private int position = -1;

    BroadcastReceiver updateMode = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra("status")) {
                order_status = intent.getIntExtra("status", 0);
                Log.d(TAG, "onReceive order_status: " + order_status);
            }
        }
    };



    public FragmentToday(Context mContext) {
        this.mContext = mContext;
        if (updateMode != null)
            mContext.registerReceiver(updateMode, new IntentFilter("updateMode"), Context.RECEIVER_NOT_EXPORTED);
    }

    public FragmentToday() {
        super();
    }

    FragmentTodayNewBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        ButterKnife.bind(this, view);
        binding = FragmentTodayNewBinding.inflate(inflater, container, false);
        getDate();
        init();

        binding.swifeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Constants.isNetworkAvailable()) {
                    todayMods.clear();
                    if (scheduleAdapter != null)
                        scheduleAdapter.notifyDataSetChanged();
                    apiCall();
                } else {
                    Toast.makeText(getContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (Constants.isNetworkAvailable()) {
            apiCall();
        } else {
            Toast.makeText(getContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
        }
        return binding.getRoot();
    }

    private void init() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);

    }

    private void updateUI() {
        if (todayMods.size() > 0) {
            binding.pickupsLeftTv.setText("pickups left: " + todayMods.size());

            scheduleAdapter = new TodayAdapter(this, mContext, getActivity(), todayMods);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
            binding.todayRecyclerView.setLayoutManager(layoutManager);
            binding.todayRecyclerView.setItemAnimator(new DefaultItemAnimator());
            binding.todayRecyclerView.smoothScrollToPosition(0);
            binding.todayRecyclerView.setHasFixedSize(true);
            binding.todayRecyclerView.setAdapter(scheduleAdapter);

            callSocketService();
        }
    }

    public void onUiRefresh() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "updateTodaySchedule: "+todayMods.size());
                scheduleAdapter.removeAt(position);
                binding.pickupsLeftTv.setText("pickups left: " + todayMods.size());
                ((Schedule) mContext).updateFragment(2, TAG);
                if (todayMods.size() > 0) {
                    Log.d(TAG, "ListSizeData: "+todayMods.size());
                } else {
                    //todayRecyclerView.setVisibility(View.GONE);
                    binding.errorMessage.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "TodayScheduleListSize: "+todayMods.size());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PHONE_REQUEST) {
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

        } else if (requestCode == LOCATION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted :)
                Log.d(TAG, "Location permission was granted: ");
            } else {
                // permission was not granted
                Log.d(TAG, "Location permission was not granted: ");
                Toast.makeText(mContext, "Permission Denied to access device Location", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == GPS_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted :)
                Log.d(TAG, "Gps permission was granted: ");
            } else {
                // permission was not granted
                Log.d(TAG, "Gps permission was not granted: ");
                Toast.makeText(mContext, "Permission Denied to access device Location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getDate() {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = calendar.getTime();

        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = calendar.getTime();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        todayAsString = dateFormat.format(today);
        Log.d(TAG, "todayAsString: " + todayAsString);
        String stdate[] = todayAsString.split("-");
        String monthSt = Constants.nameOfTheMonth(stdate[1]);
        String todayDt = monthSt + " " + stdate[2];
        Log.d(TAG, "todayDt: " + todayDt);
        binding.todayTv.setText(todayDt);
        tomorrowAsString = dateFormat.format(tomorrow);
        yesterdayAsString = dateFormat.format(yesterday);
    }

    private void apiCall() {
        // if (!apiCallAlreadyInvoked) {
        if(mContext==null){
            mContext=getActivity();
        }
        String session = Constants.getSession(mContext);
        Log.d(TAG, "session: " + session);
        ApiModel apiModel = new ApiModel();
        apiModel.FROM_TAG = TAG;
        apiModel.serviceType = ServerApi.CODE_MY_ORDERS;
        apiModel.context = mContext;
        apiModel.method = 1;
        apiModel.params = ServerParams.getOrders(session, todayAsString, "1,2,10,11", Constants.LIST_START_SIZE, Constants.DATA_REQUIRED);
        apiCallAlreadyInvoked = true;
        apiCallInvoked = true;
        if (apiViewPresenter == null) {
            init();
        }
        apiViewPresenter.validateToHitRequest(apiModel);
        // }
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

            case ServerApi.CODE_MY_ORDERS: {
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
            message = jsonObject.optString("message");
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                if (success == 1) {
                    successJsonResponse(jsonObject);
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void successJsonResponse(JSONObject jsonObject) {

        try {
            int count = jsonObject.optInt("count");
            Log.d(TAG, "successJsonResponse count: " + count);
            if (count > 0)
                binding.pickupsLeftTv.setText("pickups left: " + count);
            JSONArray jsonArrayObj = jsonObject.optJSONArray(ServerJsonResponseKey.DATA);
            if (null != jsonArrayObj) {
                if (jsonArrayObj.length() > 0) {
                    Log.d(TAG, "successJsonResponse: " + jsonArrayObj.toString());
                    for (int i = 0; i < jsonArrayObj.length(); i++) {
                        JSONObject areaObj = jsonArrayObj.optJSONObject(i);
                        Gson gson = new Gson();
                        TodayMod todayMod = gson.fromJson(areaObj.toString(), TodayMod.class);
                        todayMods.add(todayMod);
                    }
                    updateUI();
                    binding.swifeRefresh.setRefreshing(false);
                    binding.errorMessage.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "no orders found: ");
                    binding.swifeRefresh.setRefreshing(false);
                    binding.errorMessage.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (scheduleAdapter != null && order_status != 0) {
            Log.d("11112", "onResume Update : ");
            if (order_status == 3) {
                Log.d("11112", "onResume List Update : ");
                if (update == 1) {
                    onUiRefresh();
                }
            } else {
                Log.d("11112", "onResume no Update : ");
                scheduleAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != apiCallHandler) {
            apiCallHandler.removeCallbacksAndMessages(null);
            apiCallHandler = null;
        }
        apiViewPresenter.onDestroy();

        if (apiCallInvoked) {
            MyVolley.getInstance(mContext).getRequestQueue().cancelAll(TAG);
        }

        try {
            if (updateMode != null)
                mContext.unregisterReceiver(updateMode);
            if (scheduleAdapter != null && scheduleAdapter.updateMode != null)
                mContext.unregisterReceiver(scheduleAdapter.updateMode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //mContext.unbindService(mServiceConnection);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("11112", "requestCodeFragToday: " + requestCode + ", resultCode(-1) : " + resultCode);
        if (requestCode == UPDATE_LIST_REQUEST_CODE && resultCode == RESULT_OK) {
            // deal with the item yourself
            if (data != null) {
                update = data.getIntExtra("refreshItem", 0);
                position = data.getIntExtra("position", -1);
            }
        }
    }

    public void refreshTodayList() {
        Log.d("11112", "refreshTodayList triggered");
        todayMods.clear();
        scheduleAdapter.notifyDataSetChanged();
        getDate();
        if (Constants.isNetworkAvailable()) {
            apiCall();
        } else {
            Toast.makeText(getContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
        }
    }

    public void fromr() {
        Log.i("Getting Getting", "Data Getting");
    }

    private void callSocketService() {
        Log.d(TAG, "myOrderSize: " + todayMods.size());
        if(todayMods.size() > 0) {
            user_id = todayMods.get(0).getUser_id();
            status = todayMods.get(0).getStatus();
            order_id = todayMods.get(0).getId();
            field_tech_id = todayMods.get(0).getField_tech_id();
            fieldTechName = todayMods.get(0).getField_tech_name();
        }

        if (status == 10) {
            Intent socketService = new Intent(mContext, SocketService.class);
            socketService.putExtra("user_id", user_id);
            socketService.putExtra("fieldTechId", field_tech_id);
            socketService.putExtra("orderId", order_id);
            socketService.putExtra("fieldTechName", fieldTechName);
            socketService.putExtra("logoutStatus", true);
            socketService.putExtra("type", 1);
            socketService.setPackage("com.wekancode.testmybloodft.service");
            mContext.startService(socketService);
            mContext.bindService(socketService, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
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


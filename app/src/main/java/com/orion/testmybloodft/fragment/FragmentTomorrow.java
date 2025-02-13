package com.orion.testmybloodft.fragment;

/**
 * Created by Arun on 7/7/2017.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.orion.testmybloodft.databinding.FragmentTomorrowBinding;
import com.orion.testmybloodft.databinding.FragmentTomorrowNewBinding;
import com.orion.testmybloodft.models.TodayMod;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;
import com.orion.testmybloodft.views.activity.Schedule;
import com.orion.testmybloodft.views.adapter.TomorrowAdapter;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Here we loading tomorrow orders and returns a list of orders
 */

@SuppressLint("ValidFragment")
public class FragmentTomorrow extends Fragment implements ApiResponseView, Handler.Callback{
    private static final String TAG = FragmentTomorrow.class.getSimpleName();
    private Context mContext;
    private String message = "";
    private List<TodayMod> todayMods = new ArrayList<TodayMod>();
    private TomorrowAdapter tomorrowAdapter;
    private String todayAsString, tomorrowAsString, yesterdayAsString = "";

    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;
    private final int ORDER_SUMMARY_REQUEST_CODE = 1002;

//    @BindView(R.id.tomorrowRecyclerView)
//    RecyclerView tomorrowRecyclerView;
//
//    @BindView(R.id.errorMessage)
//    TextView errorMessage;
//    @BindView(R.id.todayTv)
//    TextView todayTv;
////    @BindView(R.id.parentLl)
////    LinearLayout parentLl;
//    @BindView(R.id.swifeRefresh)
//    SwipeRefreshLayout mSwipeRefreshLayout;

    public FragmentTomorrow(Context mContext) {
        this.mContext = mContext;
    }

    public FragmentTomorrow() {
        super();
    }

    FragmentTomorrowNewBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTomorrowNewBinding.inflate(inflater, container, false);

        getDate();
        init();

        binding.swifeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Constants.isNetworkAvailable()) {
                    todayMods.clear();
                    if (tomorrowAdapter != null)
                        tomorrowAdapter.notifyDataSetChanged();
                    apiCall();
                }
                else
                {
                    Toast.makeText(getContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (Constants.isNetworkAvailable()) {
            apiCall();
        }
        else
        {
            Toast.makeText(getContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
        }
        return binding.getRoot();
    }

    private void init() {
        if (Looper.myLooper() == null)
        {
            Looper.prepare();
        }
        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void updateUI() {
        if (todayMods.size() > 0) {
            tomorrowAdapter = new TomorrowAdapter(mContext, getActivity(), todayMods);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
            binding.tomorrowRecyclerView.setLayoutManager(layoutManager);
            binding.tomorrowRecyclerView.setItemAnimator(new DefaultItemAnimator());
            binding.tomorrowRecyclerView.smoothScrollToPosition(0);
            binding.tomorrowRecyclerView.setHasFixedSize(true);
            binding.tomorrowRecyclerView.setAdapter(tomorrowAdapter);
            //tomorrowAdapter.notifyDataSetChanged();
        }
    }

    private void getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        tomorrowAsString = dateFormat.format(tomorrow);
        String stdate[] = tomorrowAsString.split("-");
        String monthSt = Constants.nameOfTheMonth(stdate[1]);
        String tomorrowDt = monthSt + " " + stdate[2];
        binding.todayTv.setText(tomorrowDt);
    }

    private void apiCall() {
       // if (!apiCallAlreadyInvoked) {
            String session = Constants.getSession(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_MY_ORDERS;
            apiModel.context = mContext;
            apiModel.method = 1;
            apiModel.params = ServerParams.getOrders(session, tomorrowAsString, "1", Constants.LIST_START_SIZE, Constants.DATA_REQUIRED);
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            if(apiViewPresenter==null) {
                init();
            }
            apiViewPresenter.validateToHitRequest(apiModel);
        //}
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
            JSONArray jsonArrayObj = jsonObject.optJSONArray(ServerJsonResponseKey.DATA);
            if (null != jsonArrayObj) {
                if (jsonArrayObj.length() > 0) {
                    for(int i=0;i < jsonArrayObj.length();i++) {
                        JSONObject areaObj = jsonArrayObj.optJSONObject(i);
                        Gson gson = new Gson();
                        TodayMod todayMod = gson.fromJson(areaObj.toString(),TodayMod.class);
                        todayMods.add(todayMod);
                    }
                    updateUI();
                    binding.swifeRefresh.setRefreshing(false);
                    binding.errorMessage.setVisibility(View.GONE);
                } else{
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
    public void onDestroy() {
        super.onDestroy();

        if(null != apiCallHandler) {
            apiCallHandler.removeCallbacksAndMessages(null);
            apiCallHandler = null;
        }
        apiViewPresenter.onDestroy();

        if(apiCallInvoked){
            MyVolley.getInstance(mContext).getRequestQueue().cancelAll(TAG);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("11112", "requestCodeFragTomorrow: "+requestCode+", resultCode(-1) : "+resultCode);
        if (requestCode == ORDER_SUMMARY_REQUEST_CODE && resultCode == RESULT_OK) {
            // deal with the item yourself
            if(data != null){
                boolean updateTabActivityView = data.getBooleanExtra("updateTabActivityView", false);
                Log.d("11112", "onActivityResult updateTabActivityView: "+updateTabActivityView);
                int type = data.getIntExtra("type", -1);
                Log.d("11112", "onActivityResult type: "+type);
                if(updateTabActivityView){
                    Log.d("11112", " get data intent value ");
                    updateTabFrom(TAG, type);
                }
            } else{
                Log.d("11112", " No data intent value ");
            }
        }
    }

    public void updateTabFrom(String from, int type){
        Log.d("11112", "updateTabFrom , from: "+from);
        Log.d("11112", "updateTabFrom , type: "+type);
        ((Schedule) getContext()).updateFragment(1, TAG);

    }

    public void refreshTomorrowList(){
        Log.d("11112", "refreshTomorrowList triggered");
        todayMods.clear();
        tomorrowAdapter.notifyDataSetChanged();
        getDate();
        if (Constants.isNetworkAvailable()) {
            apiCall();
        }
        else
        {
            Toast.makeText(getContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
        }
    }
}


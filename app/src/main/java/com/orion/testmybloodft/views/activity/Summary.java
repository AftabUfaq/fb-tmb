package com.orion.testmybloodft.views.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
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
import com.orion.testmybloodft.databinding.ActivitySummaryBinding;
import com.orion.testmybloodft.models.TestListMod;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;
import com.orion.testmybloodft.views.adapter.ScheduleTabsAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Arun on 7/10/2017.
 */

/**
 * This activity displays a order summary. This page contains test/billing and patient details
 */

public class Summary extends AppCompatActivity implements ApiResponseView, Handler.Callback {
    private static final String TAG = Summary.class.getSimpleName();

    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;
    private Activity mActivity;
    private Context mContext;
    private String tmb_order_id, message = "";

//    @BindView(R.id.summary_viewpager)
//    ViewPager summary_viewpager;
//    @BindView(R.id.tvBookingID)
//    TextView tvBookingID;
//
//    @BindView(R.id.tablayout)
//    TabLayout tabLayout;

    private ScheduleTabsAdapter adapter;
    private List<TestListMod> testListMods = new ArrayList<TestListMod>();

    ActivitySummaryBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mContext = this;
        mActivity = this;

        handleClick();

        //setupViewPager(summary_viewpager);
        adapter = new ScheduleTabsAdapter(getSupportFragmentManager(), mContext);

        init();
        if(getIntent()!=null && getIntent().hasExtra("tmb_order_id")) {
            tmb_order_id = getIntent().getStringExtra("tmb_order_id");
            binding.tvBookingID.setText("#"+ tmb_order_id);
            if (Constants.isNetworkAvailable()) {
                apiOrderDetailsCall(tmb_order_id);
            }
            else
            {
                Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
            }
        }

    }

    void handleClick(){
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    private void init() {
        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
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
            case ServerApi.CODE_ORDER_DETAILS: {
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
                    if (success == 1) {
                        if (ServerApi.CODE_ORDER_DETAILS == serviceType) {
                            successJsonResponse(jsonObject);
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

    private void successJsonResponse(JSONObject jsonObject) {
        try {
            Log.d(TAG, "successJsonResponse: "+jsonObject.toString());
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
                Log.d(TAG, "contact_number: "+contact_number);
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
                            for(int i=0;i < tsetArrayObj.length();i++) {
                                JSONObject areaObj = tsetArrayObj.optJSONObject(i);
                                Gson gson = new Gson();
                                TestListMod testListMod = gson.fromJson(areaObj.toString(),TestListMod.class);
                                testListMods.add(testListMod);
                            }
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
            setViewPager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setViewPager() {
        // Set the ViewPagerAdapter into ViewPager
        binding.summaryViewpager.setAdapter(adapter);
        binding.summaryViewpager.setOffscreenPageLimit(2);

        binding.tablayout.setupWithViewPager(binding.summaryViewpager);
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
    protected void onDestroy() {
        super.onDestroy();
        if(null != apiCallHandler) {
            apiCallHandler.removeCallbacksAndMessages(null);
            apiCallHandler = null;
        }
        apiViewPresenter.onDestroy();

        if(apiCallInvoked){
            MyVolley.getInstance(getApplicationContext()).getRequestQueue().cancelAll(TAG);
        }

    }

}

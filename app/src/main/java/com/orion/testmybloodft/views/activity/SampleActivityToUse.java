package com.orion.testmybloodft.views.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.orion.testmybloodft.R;
import com.orion.testmybloodft.api.ApiModel;
import com.orion.testmybloodft.api.ApiResponseView;
import com.orion.testmybloodft.api.ApiViewPresenter;
import com.orion.testmybloodft.api.ApiViewPresenterImpl;
import com.orion.testmybloodft.api.ServerApi;
import com.orion.testmybloodft.api.ServerJsonResponseKey;
import com.orion.testmybloodft.config.MyVolley;
import com.orion.testmybloodft.databinding.ActivityMainBinding;
import com.orion.testmybloodft.models.CityMod;
import com.orion.testmybloodft.utils.UIUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Wekancode on 30-Mar-17.
 */

public class SampleActivityToUse extends AppCompatActivity implements ApiResponseView,Handler.Callback{

    private static final String TAG = SampleActivityToUse.class.getSimpleName();


    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;
    private ArrayList<CityMod> cityArrayList;

    private Dialog m_Dialog;
    private ProgressBar m_ProgressBar;

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        m_Dialog = new Dialog(SampleActivityToUse.this, R.style.Theme_Transparent);
        m_Dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_Dialog.setContentView(R.layout.progress_loader);
        m_ProgressBar = m_Dialog.findViewById(R.id.progressBar);
        m_ProgressBar.setIndeterminate(true);
        //noinspection deprecation,deprecation
        m_ProgressBar.setIndeterminateDrawable(SampleActivityToUse.this.getResources().getDrawable(R.drawable.loader_anim));
        m_Dialog.setCancelable(false);
        m_Dialog.setCanceledOnTouchOutside(false);


        init();

        apiCall();

    }

    private void init() {

        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void apiCall() {

        if (!apiCallAlreadyInvoked) {
            // HashMap<String, String> user = session.getUserDetails();
            //String userId = user.get(ServerJsonResponseKey.USER_ID);
            // String token = user.get(ServerJsonResponseKey.TOKEN);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            // apiModel.TOKEN_HEADER = token;
            apiModel.serviceType = ServerApi.CODE_USER_FORGOT;
            apiModel.context = getApplicationContext();
            apiModel.method = 0;
            //apiModel.params = ServerParams.getSearchCity();
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    /*@OnClick({R.id.txtDates})
    void clickListenterSignIn(View view) {
        // TODO ...

        switch (view.getId()) {
            case R.id.txtDates: {

                break;
            }
        }
    }*/

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

            case ServerApi.CODE_USER_FORGOT: {
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
        String msgApiDefault = "Get Search City";
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                if (success == 1) {

                   /* String msg = jsonObject.optString(ServerJsonResponseKey.MSG, null);
                    if (msg != null) {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                    Log.i(TAG, msgApiDefault + " Success");*/

                    successJsonResponse(jsonObject);
                } else {
                    //jsonSccessNotOne(jsonObject, msgApiDefault);
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);
            Toast.makeText(getApplicationContext(), msgApiDefault + " having trouble!", Toast.LENGTH_SHORT).show();
        }
    }

    private void successJsonResponse(JSONObject jsonObject) {

        try {
            JSONArray jsonArrayObj = jsonObject.optJSONArray(ServerJsonResponseKey.DATA);
            if (null != jsonArrayObj) {

                if (jsonArrayObj.length() > 0) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CityMod getJsonSearchCityData(JSONObject cityJsonData) {

        CityMod cityMod = new CityMod();
        cityMod.setCityID(cityJsonData.optString(ServerJsonResponseKey._ID));
        cityMod.setCityName(cityJsonData.optString(ServerJsonResponseKey.NAME));
        cityMod.setStatus(cityJsonData.optString(ServerJsonResponseKey.STATUS));
        cityMod.setStateName(cityJsonData.optString(ServerJsonResponseKey.STATE_NAME));

        return cityMod;

    }

    private void showProgressDialog() {
        if (!m_Dialog.isShowing()) {
            m_ProgressBar.setVisibility(View.VISIBLE);
            m_Dialog.show();
        }
    }

    private void hideProgressDialog() {
        if (m_Dialog.isShowing()) {
            m_ProgressBar.setVisibility(View.GONE);
            m_Dialog.dismiss();
        }
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


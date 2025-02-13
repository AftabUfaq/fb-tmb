package com.orion.testmybloodft.views.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.orion.testmybloodft.databinding.ActivityNotificationBinding;
import com.orion.testmybloodft.models.NotificationMod;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;
import com.orion.testmybloodft.views.adapter.NotificationAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Wekancode on 30-Mar-17.
 */

/**
 * This activity displays a list of local notifications
 */

public class Notification extends AppCompatActivity implements ApiResponseView,Handler.Callback{
    private static final String TAG = Notification.class.getSimpleName();

    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;


    private Activity mActivity;
    private Context mContext;
    private NotificationAdapter adapter;
    private List<NotificationMod> notificationMods = new ArrayList<NotificationMod>();
    private String message = "", markStatusCount = "";
    private boolean loadMore = false;
    private boolean isClearClicked = false;
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean swipeRefresh = false;
    private boolean alreadyListenerAdded = false;

    private Dialog m_Dialog;
    private ProgressBar m_ProgressBar;

//    @BindView(R.id.notificationRecycler)
//    RecyclerView notificationRecycler;
//    @BindView(R.id.errorMessage)
//    TextView errorMessage;
//    @BindView(R.id.tvClearAll)
//    TextView tvClearAll;
//    @BindView(R.id.swifeRefresh)
//    SwipeRefreshLayout mSwipeRefreshLayout;
    ActivityNotificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mActivity = this;
        mContext = this;

        handleclick();
//        ButterKnife.bind(this);
        /*m_Dialog = new Dialog(Notification.this, R.style.Theme_Transparent);
        m_Dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_Dialog.setContentView(R.layout.progress_loader);
        m_ProgressBar = (ProgressBar) m_Dialog.findViewById(R.id.progressBar);
        m_ProgressBar.setIndeterminate(true);
        //noinspection deprecation,deprecation
        m_ProgressBar.setIndeterminateDrawable(Notification.this.getResources().getDrawable(R.drawable.loader_anim));
        m_Dialog.setCancelable(false);
        m_Dialog.setCanceledOnTouchOutside(false);*/

        init();

        adapter = new NotificationAdapter(mActivity, mContext, notificationMods);
        initPageListener(0);

        binding.notificationRecycler.setHasFixedSize(true);
        binding.notificationRecycler.setLayoutManager(mLayoutManager);
        binding.notificationRecycler.setAdapter(adapter);

        binding.swifeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Constants.isNetworkAvailable()) {
                    swipeRefresh = true;
                    notificationMods.clear();
                    adapter.notifyDataSetChanged();
                    notifyApiCall(Constants.LOAD_DATA_REQUIRED);
                }
                else
                {
                    Toast.makeText(mContext, Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (Constants.isNetworkAvailable()) {
            notifyApiCall(Constants.LOAD_DATA_REQUIRED);
        }
        else
        {
            Toast.makeText(mContext, Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
        }
    }

    private void initPageListener(final int from){
        Log.d(TAG, "initPageListener from: "+from);
        if(from == 1){
            if(!alreadyListenerAdded){
                alreadyListenerAdded = true;
            }else{
                return;
            }
        }

        adapter.setLoadMoreListener(new NotificationAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                binding.notificationRecycler.post(new Runnable() {
                    @Override
                    public void run() {
                        int list_size = notificationMods.size();
                        if (list_size >= 10) {
                            int index = list_size;
                            if (Constants.isNetworkAvailable()) {
                                notifyApiCall(index, Constants.LOAD_DATA_REQUIRED);
                            }
                            else
                            {
                                Toast.makeText(mContext, Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                //Calling loadMore function in Runnable to fix the
                // java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling error
            }
        });
    }

    private void init() {
        mLayoutManager = new LinearLayoutManager(mContext);
        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void notifyApiCall(int size) {
        if (!apiCallAlreadyInvoked) {
            loadMore = false;
            String session = Constants.getSession(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_NOTIFICATION;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.getNotification(session, Constants.LIST_START_SIZE, String.valueOf(size));
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    private void notifyApiCall(int index, int size) {
        //add loading progress view
        notificationMods.add(new NotificationMod(0));
        adapter.notifyItemInserted(notificationMods.size()-1);
        if (!apiCallAlreadyInvoked) {
            loadMore = true;
            String session = Constants.getSession(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_NOTIFICATION;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.getNotification(session, String.valueOf(index), String.valueOf(size));
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    private void clearNotifyApiCall() {
        if (!apiCallAlreadyInvoked) {
            String session = Constants.getSession(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_CLEAR_NOTIFICATION;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.clearNotification(session, Constants.CLEAR_NOTIFICATIONS);
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    void handleclick(){
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.tvClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constants.isNetworkAvailable()) {
                    clearNotifyApiCall();
                }
                else
                {
                    Toast.makeText(mContext, Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                }
            }
        });
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
            case ServerApi.CODE_CLEAR_NOTIFICATION:
            case ServerApi.CODE_NOTIFICATION: {
                parseResponseData(msg.getData().getString(ServerJsonResponseKey.RESPONSE, null), msg.what, loadMore);
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

    private void parseResponseData(String response, int serviceType, boolean loadMore) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            Log.d(TAG, "jsonObject: "+jsonObject.toString());
            message = jsonObject.optString("message");
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                if (success == 1) {
                    if (ServerApi.CODE_NOTIFICATION == serviceType) {
                        if (loadMore)
                            loadMoreJsonResponse(jsonObject);
                        else
                            notificationJsonResponse(jsonObject);
                    } else if (ServerApi.CODE_CLEAR_NOTIFICATION == serviceType) {
                        clearNotificationJsonResponse(jsonObject);
                    }
                } else {
                    Toast.makeText(Notification.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);
        }
    }

    private void notificationJsonResponse(JSONObject jsonObject) {
        try {
            JSONArray jsonArrayObj = jsonObject.optJSONArray(ServerJsonResponseKey.DATA);
            if (null != jsonArrayObj) {
                Log.d(TAG, "notificationJsonResponse: "+jsonArrayObj.toString());
                if (jsonArrayObj.length() > 0) {
                    binding.tvClearAll.setVisibility(View.VISIBLE);
                    for(int i=0;i < jsonArrayObj.length();i++) {
                        JSONObject areaObj = jsonArrayObj.optJSONObject(i);
                        Gson gson = new Gson();
                        NotificationMod notificationMod = gson.fromJson(areaObj.toString(),NotificationMod.class);
                        notificationMods.add(notificationMod);
                    }
                    markStatusCount = jsonObject.optString(ServerJsonResponseKey.COUNT);
                    if(markStatusCount.equalsIgnoreCase("0")){
                        binding.tvClearAll.setVisibility(View.GONE);
                    }else{
                        binding.tvClearAll.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                    //adapter.notifyDataChanged();
                    binding.swifeRefresh.setRefreshing(false);
                    Log.d(TAG, "notificationJsonResponse swipeRefresh: "+swipeRefresh);
                    if (swipeRefresh) {
                        alreadyListenerAdded = false;
                        adapter.setMoreDataAvailable(true);
                        initPageListener(1);
                    }
                } else{
                    binding.swifeRefresh.setVisibility(View.GONE);
                    //notificationRecycler.setVisibility(View.GONE);
                    binding.tvClearAll.setVisibility(View.GONE);
                    binding.errorMessage.setVisibility(View.VISIBLE);
                }
            } else{
                Log.e(TAG," Response Error "+message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMoreJsonResponse(JSONObject jsonObject) {
        try {
            JSONArray jsonArrayObj = jsonObject.optJSONArray(ServerJsonResponseKey.DATA);
            if (null != jsonArrayObj) {
                //remove loading view
                notificationMods.remove(notificationMods.size()-1);

                if (jsonArrayObj.length() > 0) {
                    for(int i=0;i < jsonArrayObj.length();i++) {
                        JSONObject areaObj = jsonArrayObj.optJSONObject(i);
                        Gson gson = new Gson();
                        NotificationMod notificationMod = gson.fromJson(areaObj.toString(),NotificationMod.class);
                        notificationMods.add(notificationMod);
                    }
                } else {
                    //result size 0 means there is no more data available at server
                    adapter.setMoreDataAvailable(false);
                    //telling adapter to stop calling load more as no more server data available
                    Toast.makeText(mContext,"No More Data Available",Toast.LENGTH_LONG).show();
                }
                adapter.notifyDataChanged();
                //should call the custom method adapter.notifyDataChanged here to get the correct loading status
            } else{
                Log.e(TAG," Load More Response Error "+message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearNotificationJsonResponse(JSONObject jsonObject) {
        Log.d(TAG, "clearNotificationJsonResponse: "+jsonObject.toString());
        try {
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                String message = jsonObject.optString("message");
                if (success == 1) {
                    isClearClicked = true;
                    Toast.makeText(Notification.this, message, Toast.LENGTH_SHORT).show();
                    if(adapter != null && adapter.getItemCount() > 0){
                        for(int i=0; i < adapter.getItemCount(); i++){

                            adapter.updateReadStatus(i);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    onUiRefresh();
                } else {
                    Toast.makeText(Notification.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);
        }
    }

    private void onUiRefresh(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Constants.isNetworkAvailable()) {
                    //notifyApiCall(Constants.LOAD_DATA_REQUIRED);
                    //updateList();
                    binding.notificationRecycler.smoothScrollToPosition(0);
                    binding.tvClearAll.setVisibility(View.GONE);
                }
                else
                {
                    Toast.makeText(mContext, Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                }
            }
        });
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


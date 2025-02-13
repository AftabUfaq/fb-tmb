package com.orion.testmybloodft.views.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
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
import com.orion.testmybloodft.databinding.FragmentTomorrowBinding;
import com.orion.testmybloodft.fragment.FragmentTomorrow;
import com.orion.testmybloodft.models.TodayMod;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;
import com.orion.testmybloodft.views.adapter.LoadMoreAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Wekancode on 30-Mar-17.
 */

public class LoadMoreRecycler extends AppCompatActivity implements ApiResponseView,Handler.Callback{
    private static final String TAG = LoadMoreRecycler.class.getSimpleName();
    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;

    private Dialog m_Dialog;
    private ProgressBar m_ProgressBar;

    private Activity mActivity;
    private Context mContext;
    private boolean isPageLoaded = false;
    private String message = "";
    private List<TodayMod> todayMods = new ArrayList<TodayMod>();
    private LoadMoreAdapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean loadMore = false;



    FragmentTomorrowBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=FragmentTomorrowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mActivity = this;
        mContext = this;


        /*m_Dialog = new Dialog(LoadMoreRecycler.this, R.style.Theme_Transparent);
        m_Dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_Dialog.setContentView(R.layout.progress_loader);
        m_ProgressBar = (ProgressBar) m_Dialog.findViewById(R.id.progressBar);
        m_ProgressBar.setIndeterminate(true);
        //noinspection deprecation,deprecation
        m_ProgressBar.setIndeterminateDrawable(LoadMoreRecycler.this.getResources().getDrawable(R.drawable.loader_anim));
        m_Dialog.setCancelable(false);
        m_Dialog.setCanceledOnTouchOutside(false);*/

        init();

        adapter = new LoadMoreAdapter(mContext, todayMods);
        adapter.setLoadMoreListener(new LoadMoreAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                binding.tomorrowRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        //int index = todayMods.size() - 1;
                        int index = todayMods.size();
                        apiCall(index, 2);
                    }
                });
                //Calling loadMore function in Runnable to fix the
                // java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling error
            }
        });

        binding.tomorrowRecyclerView.setHasFixedSize(true);
        binding.tomorrowRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
      //tomorrowRecyclerView.addItemDecoration(new VerticalLineDecorator(2));
        binding.tomorrowRecyclerView.setAdapter(adapter);

        apiCall(5);
    }

    private void init() {
        mLayoutManager = new LinearLayoutManager(mContext);
        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void apiCall(int size) {
        if (!apiCallAlreadyInvoked) {
            loadMore = false;
            String session = Constants.getSession(this);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_MY_ORDERS;
            apiModel.context = mContext;
            apiModel.method = 1;
            apiModel.params = ServerParams.getOrders(session, "2017-08-07", "1,2,10,11", Constants.LIST_START_SIZE, String.valueOf(size));
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    private void apiCall(int index, int size) {

        //add loading progress view
        todayMods.add(new TodayMod(0));
        adapter.notifyItemInserted(todayMods.size()-1);
        if (!apiCallAlreadyInvoked) {
            loadMore = true;
            String session = Constants.getSession(this);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_MY_ORDERS;
            apiModel.context = mContext;
            apiModel.method = 1;
            apiModel.params = ServerParams.getOrders(session, "2017-08-07", "1,2,10,11", String.valueOf(index), String.valueOf(size));
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
            message = jsonObject.optString("message");
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                if (success == 1) {
                    if (loadMore)
                        loadMoreJsonResponse(jsonObject);
                    else
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
                }
                adapter.notifyDataChanged();
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
                todayMods.remove(todayMods.size()-1);

                if (jsonArrayObj.length() > 0) {
                    for(int i=0;i < jsonArrayObj.length();i++) {
                        JSONObject areaObj = jsonArrayObj.optJSONObject(i);
                        Gson gson = new Gson();
                        TodayMod todayMod = gson.fromJson(areaObj.toString(),TodayMod.class);
                        todayMods.add(todayMod);
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

}


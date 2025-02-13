package com.orion.testmybloodft.fragment;

/**
 * Created by Arun on 7/7/2017.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.orion.testmybloodft.databinding.FragmentTomorrowNewBinding;
import com.orion.testmybloodft.models.TodayMod;
import com.orion.testmybloodft.multidatepicker.date.DatePickerDialog;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;
import com.orion.testmybloodft.views.adapter.TomorrowAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Here we loading past closed orders and returns a list of closed orders
 */

@SuppressLint("ValidFragment")
public class FragmentPickedUp extends Fragment implements DatePickerDialog.OnDateSetListener, ApiResponseView, Handler.Callback{
    private static final String TAG = FragmentPickedUp.class.getSimpleName();
    private boolean pickup = false;
    private Context mContext;
    private boolean isClickedDateSelected = false;
    private String startDate,endDate,selectedCityID,selectedStalls,selectedStartDate,selectedEndDate,selectedPropertyTypeID,selectedTypeName = "";
    private int startDatePic,startMonth,endYear,endMonth,endDatePic,startYear;
    private String message = "";
    private List<TodayMod> todayMods = new ArrayList<TodayMod>();
    private TomorrowAdapter tomorrowAdapter;
    private String currentDay, tomorrowAsString, todayAsString = "";
    private boolean loading = true;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private RecyclerView.LayoutManager mLayoutManager;
    private int colorAccent = 0xFF28AEF5;

    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;

//    @BindView(R.id.tomorrowRecyclerView)
//    RecyclerView tomorrowRecyclerView;
//    @BindView(R.id.pickupBtn)
//    ImageView mPickupBtn;
//    @BindView(R.id.errorMessage)
//    TextView errorMessage;
//    @BindView(R.id.todayTv)
//    TextView todayTv;
//    @BindView(R.id.swifeRefresh)
//    SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean wantUpdate = false;

    public FragmentPickedUp(Context mContext) {
        this.mContext = mContext;
    }

    public FragmentPickedUp() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private FragmentTomorrowNewBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTomorrowNewBinding.inflate(inflater, container, false);


        //here is your arguments
        Bundle bundle=getArguments();
        pickup = bundle.getBoolean("Pickup");
        if (pickup)
            binding.pickupBtn.setVisibility(View.VISIBLE);
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
        binding.pickupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker();
            }
        });

        return binding.getRoot();
    }



    private void datePicker() {

        Calendar now = Calendar.getInstance();

        DatePickerDialog dpd;

        if (!isClickedDateSelected) {
            dpd = DatePickerDialog.newInstance(
                    FragmentPickedUp.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
        } else {
            dpd = DatePickerDialog.newInstance(
                    FragmentPickedUp.this,
                    startYear,
                    startMonth,
                    startDatePic
            );
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        dpd.setMaxDate(calendar);

        dpd.setAccentColor(colorAccent);

        dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        isClickedDateSelected = true;

        String date = "" + Constants.nameOfTheMonth("" + (++monthOfYear)) + " " + dayOfMonth + ", " + year;

        selectedStartDate = year +"-"+(+monthOfYear)+"-"+dayOfMonth;

        Log.i(TAG,"selectedStartDate ::: "+selectedStartDate);

        startYear = year;
        startDatePic = dayOfMonth;
        startMonth = monthOfYear - 1;

        String monthCalender = String.valueOf(monthOfYear);

    /*For event start Date*/
        if (monthCalender.length() == 2) {
            if (String.valueOf(dayOfMonth).length() == 2)
                startDate = year + "-" + monthCalender + "-" + dayOfMonth;
            else
                startDate = year + "-" + monthCalender + "-" + "0" + dayOfMonth;
        } else {
            monthCalender = "0" + monthCalender;
            startDate = year + "-" + monthCalender + "-" + dayOfMonth;
            if (String.valueOf(dayOfMonth).length() == 2)
                startDate = year + "-" + monthCalender + "-" + dayOfMonth;
            else
                startDate = year + "-" + monthCalender + "-" + "0" + dayOfMonth;
        }
        String stdate[] = startDate.split("-");
        String monthSt = Constants.nameOfTheMonth(stdate[1]);
        String pastDayDt = monthSt + " " + stdate[2];
        binding.todayTv.setText(pastDayDt);
        binding.todayTv.setVisibility(View.VISIBLE);
        todayMods.clear();

        if (Constants.isNetworkAvailable()) {
            apiCall();
        }
        else
        {
            Toast.makeText(getContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
        }
        Log.i("Pickedup Date", "" + startDate);

    }

    private void init() {
        mLayoutManager = new LinearLayoutManager(mContext);
        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void updateUI() {
        if (todayMods.size() > 0) {
            tomorrowAdapter = new TomorrowAdapter(mContext, getActivity(), todayMods);
            binding.tomorrowRecyclerView.setLayoutManager(mLayoutManager);
            binding.tomorrowRecyclerView.setItemAnimator(new DefaultItemAnimator());
            binding.tomorrowRecyclerView.smoothScrollToPosition(0);
            binding.tomorrowRecyclerView.setHasFixedSize(true);
            binding.tomorrowRecyclerView.setAdapter(tomorrowAdapter);
            tomorrowAdapter.notifyDataSetChanged();
        }
    }

    private void getDate() {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        todayAsString = dateFormat.format(today);
        String stdate[] = todayAsString.split("-");
        String monthSt = Constants.nameOfTheMonth(stdate[1]);
        String todayDt = monthSt + " " + stdate[2];
        Log.d(TAG, "todayDt: "+todayDt);
        binding.todayTv.setText(todayDt);
    }

    private void apiCall() {
        Log.d(TAG, "startDate: "+startDate);
        //if (!apiCallAlreadyInvoked) {
            String session = Constants.getSession(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_MY_ORDERS;
            apiModel.context = mContext;
            apiModel.method = 1;
            if (startDate!=null) {
                Log.d(TAG, "Past Order Previous: ");
                apiModel.params = ServerParams.getOrders(session, startDate, "3,4,5,6,9", Constants.LIST_START_SIZE, Constants.DATA_REQUIRED);
            } else {
                Log.d(TAG, "Current Day order: ");
                apiModel.params = ServerParams.getOrders(session, todayAsString, "3,4,5,6,9", Constants.LIST_START_SIZE, Constants.DATA_REQUIRED);
            }
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
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
            JSONArray jsonArrayObj = jsonObject.optJSONArray(ServerJsonResponseKey.DATA);
            Log.d(TAG, "pickupJsonResponse AryLength: "+jsonArrayObj.length());
            if (null != jsonArrayObj) {
                if (jsonArrayObj.length() > 0) {
                    binding.swifeRefresh.setVisibility(View.VISIBLE);
                    binding.todayTv.setVisibility(View.VISIBLE);
                    binding.errorMessage.setVisibility(View.GONE);
                    for(int i=0;i < jsonArrayObj.length();i++) {
                        JSONObject areaObj = jsonArrayObj.optJSONObject(i);
                        Gson gson = new Gson();
                        TodayMod todayMod = gson.fromJson(areaObj.toString(),TodayMod.class);
                        todayMods.add(todayMod);
                    }
                    updateUI();
                    binding.swifeRefresh.setRefreshing(false);
                    binding.errorMessage.setVisibility(View.GONE);
                    binding.todayTv.setVisibility(View.VISIBLE);
                } else{
                    //mSwipeRefreshLayout.setVisibility(View.GONE);
                    binding.todayTv.setVisibility(View.GONE);
                    binding.swifeRefresh.setRefreshing(false);
                    binding.errorMessage.setVisibility(View.VISIBLE);
                }
            } else {
                Log.d(TAG, "pickupJsonResponse elseBlock Length: "+jsonArrayObj.length());
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

    public void updateFromParent(String from){
        Log.d("11112", "updateFromParent , from: "+from);
        if(from.equalsIgnoreCase("Schedule")){
           if(wantUpdate){
               wantUpdate = false;
               if (Constants.isNetworkAvailable()) {
                   todayMods.clear();
                   apiCall();
               }
               else
               {
                   Toast.makeText(getContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
               }
           }
        } else if(from.equalsIgnoreCase("FragmentToday")){
            wantUpdate = true;
        }
    }

}


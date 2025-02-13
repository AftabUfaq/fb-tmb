package com.orion.testmybloodft.views.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.orion.testmybloodft.databinding.ActivityPaymentBinding;
import com.orion.testmybloodft.models.TestListMod;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;
import com.orion.testmybloodft.views.adapter.BillingAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.List;


public class ClosePickup extends AppCompatActivity implements ApiResponseView, Handler.Callback {
    private static final String TAG = ClosePickup.class.getSimpleName();
    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;

    private Dialog m_Dialog;
    private ProgressBar m_ProgressBar;
    private Activity mActivity;
    private Context mContext;
    private String message, collection_charge, payment_mode, payment_type, tmb_order_id = "";
    private BillingAdapter billingAdapter;
    private List<TestListMod> testList;
    private int removeItemPos = -1;
    private final int UPDATE_LIST_REQUEST_CODE = 1000;

//    @BindView(R.id.tvVoucher)
//    TextView tvVoucher;
//    @BindView(R.id.tvCharge)
//    TextView tvCharge;
//    @BindView(R.id.tvTotalAmt)
//    TextView tvTotalAmt;
//    @BindView(R.id.edtTransactionId)
//    EditText edtTransactionId;
//    @BindView(R.id.llDiscount)
//    LinearLayout llDiscount;
//    @BindView(R.id.vDisVoucher)
//    View vDisVoucher;
//    @BindView(R.id.paymentRecyclerView)
//    RecyclerView paymentRecyclerView;
//    @BindView(R.id.tvBookingID)
//    TextView tvBookingID;
//    @BindView(R.id.tvCard)
//    TextView tvCard;
//    @BindView(R.id.tvCash)
//    TextView tvCash;

    ActivityPaymentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mActivity = this;
        mContext = this;
        handleClicks();
        if (getIntent() != null && getIntent().hasExtra("tmb_order_id")) {
            tmb_order_id = getIntent().getStringExtra("tmb_order_id");
            binding.tvBookingID.setText("#" + tmb_order_id);
        }
        if (getIntent() != null && getIntent().hasExtra("position")) {
            removeItemPos = getIntent().getIntExtra("position", -1);
        }

        /*m_Dialog = new Dialog(ScanVacutainer.this, R.style.Theme_Transparent);
        m_Dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_Dialog.setContentView(R.layout.progress_loader);
        m_ProgressBar = (ProgressBar) m_Dialog.findViewById(R.id.progressBar);
        m_ProgressBar.setIndeterminate(true);
        //noinspection deprecation,deprecation
        m_ProgressBar.setIndeterminateDrawable(ScanVacutainer.this.getResources().getDrawable(R.drawable.loader_anim));
        m_Dialog.setCancelable(false);
        m_Dialog.setCanceledOnTouchOutside(false);*/

        updateUI();
        init();
    }

    private void init() {
        binding.paymentRecyclerView.setNestedScrollingEnabled(false);

        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void updateUI() {
        // Get Test list
        testList = ((App) getApplication()).getTestList();
        billingAdapter = new BillingAdapter(mContext, testList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        binding.paymentRecyclerView.setLayoutManager(layoutManager);
        binding.paymentRecyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.paymentRecyclerView.smoothScrollToPosition(0);
        binding.paymentRecyclerView.setHasFixedSize(true);
        binding.paymentRecyclerView.setAdapter(billingAdapter);

        String billingStr = Constants.getBillingDetails(mContext);
        String[] value_split = billingStr.split("\\|");
        payment_mode = value_split[1];
        Log.d(TAG, "payment_mode: " + payment_mode);
        if (payment_mode.equals("Cash")) {
            payment_type = "1";
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String cts = String.valueOf(timestamp.getTime());
            binding.edtTransactionId.setText(cts);
            binding.edtTransactionId.setSelection(binding.edtTransactionId.getText().length());
        } else if (payment_mode.equals("Card")) {
            payment_type = "2";
        }

        String discount = value_split[2];
        if (!discount.equals("0.00")) {
            binding.tvVoucher.setText(discount);
        } else {
            binding.llDiscount.setVisibility(View.GONE);
            binding.vDisVoucher.setVisibility(View.GONE);
        }
        binding.tvTotalAmt.setText(value_split[3]);
        collection_charge = value_split[6];
        binding.tvCharge.setText(collection_charge);
    }

    private void apiPaymentCollectCall() {
        if (!apiCallAlreadyInvoked) {
            String transaction_id = binding.edtTransactionId.getText().toString().trim();
            String session = Constants.getSession(mContext);
            int order_id = Constants.getOrderId(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_COLLECT_PAYMENT;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.collectPayment(session, String.valueOf(order_id), payment_type, transaction_id);
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    void handleClicks() {
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.tvCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.tvCash.setBackgroundResource(R.drawable.btn_bg);
                binding.tvCash.setTextColor(Color.parseColor("#ffffff"));
                binding.tvCard.setBackgroundResource(0);
                binding.tvCard.setTextColor(Color.parseColor("#666666"));
                binding.edtTransactionId.setText("");
                payment_type = "1";
                //method 1
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String cts = String.valueOf(timestamp.getTime());
                binding.edtTransactionId.setText(cts);
                binding.edtTransactionId.setSelection(binding.edtTransactionId.getText().length());
            }
        });
        binding.tvCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.tvCash.setBackgroundResource(0);
                binding.tvCash.setTextColor(Color.parseColor("#666666"));
                binding.tvCard.setBackgroundResource(R.drawable.btn_bg);
                binding.tvCard.setTextColor(Color.parseColor("#ffffff"));
                binding.edtTransactionId.setSelection(binding.edtTransactionId.getText().length());
                binding.edtTransactionId.setText("");
                payment_type = "2";
            }
        });
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        binding.closePickupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String trans_id = binding.edtTransactionId.getText().toString().trim();
                if (trans_id.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter Transaction Id..!", Toast.LENGTH_SHORT).show();
                } else {
                    if (Constants.isNetworkAvailable()) {
                        apiPaymentCollectCall();
                    } else {
                        Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                    }
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
            case ServerApi.CODE_COLLECT_PAYMENT: {
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
            JSONObject jsonObject = null;
            jsonObject = new JSONObject(response);
            Log.d(TAG, "jsonObject: " + jsonObject.toString());
            message = jsonObject.optString("message");
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                Log.d(TAG, "success: " + success);
                if (success == 1) {
                    if (ServerApi.CODE_COLLECT_PAYMENT == serviceType) {
                        paymentCollectJsonResponse(jsonObject, success);
                    }
                }
            } else {
                Toast.makeText(ClosePickup.this, message, Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    private void paymentCollectJsonResponse(JSONObject jsonObject, int success) {
        try {
            Log.d(TAG, "paymentCollectJsonResponse: " + jsonObject.toString());
            Log.d(TAG, "statusCode : " + success);
            int status = jsonObject.optInt("data");
            Log.d(TAG, "after getting res statusCode: " + status);
            Intent intent = new Intent("updateMode");
            intent.putExtra("status", status);
            sendBroadcast(intent);
            if (success == 1) {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                callSchedule();
            } else {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callSchedule() {
        Intent verifyIntent = new Intent();
        verifyIntent.putExtra("refreshItem", 1);
        verifyIntent.putExtra("position", removeItemPos);
        verifyIntent.putExtra("maintainActivityView", true);
        setResult(RESULT_OK, verifyIntent);
        finish();
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

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
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
    public void onBackPressed() {
        hideSoftKeyboard(mActivity);
        super.onBackPressed();
    }

}


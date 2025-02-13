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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.orion.testmybloodft.R;
import com.orion.testmybloodft.api.ApiModel;
import com.orion.testmybloodft.api.ApiResponseView;
import com.orion.testmybloodft.api.ApiViewPresenter;
import com.orion.testmybloodft.api.ApiViewPresenterImpl;
import com.orion.testmybloodft.api.ServerApi;
import com.orion.testmybloodft.api.ServerJsonResponseKey;
import com.orion.testmybloodft.api.ServerParams;
import com.orion.testmybloodft.config.MyVolley;
import com.orion.testmybloodft.databinding.ActivityVacutainerBinding;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Wekancode on 30-Mar-17.
 */

/**
 * This activity displays a scan the vacutainer using Zxing
 */

public class ScanVacutainer extends AppCompatActivity implements ApiResponseView,Handler.Callback{
    private static final String TAG = ScanVacutainer.class.getSimpleName();
    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;

    private Dialog m_Dialog;
    private ProgressBar m_ProgressBar;
    private Activity mActivity;
    private Context mContext;
    private String tmb_order_id, message, address, contact_number, patient_name, pickup_date, pickup_time, barcodeCollection = "";
    private String user_verified = "0";
    private int current_status, order_status, user_id, order_id;
    private double destLatitude, destLongitude;
    private boolean lauchedScanner = false;

//    @BindView(R.id.tvBookingID)
//    TextView tvBookingID;
//    @BindView(R.id.edtBarcode)
//    EditText edtBarcode;
//    @BindView(R.id.edtVacutainers)
//    EditText edtVacutainers;
//    @BindView(R.id.tvYes)
//    TextView tvYes;
//    @BindView(R.id.tvNo)
//    TextView tvNo;

    ActivityVacutainerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityVacutainerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        mActivity = this;
        mContext = this;

        handleClicks();
//        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().hasExtra("order_status")) {
            order_status = getIntent().getIntExtra("order_status", 0);
        }

        if(getIntent()!=null && getIntent().hasExtra("patient_name")) {
            patient_name = getIntent().getStringExtra("patient_name");
        }
        if(getIntent()!=null && getIntent().hasExtra("pickup_date")) {
            pickup_date = getIntent().getStringExtra("pickup_date");
        }
        if(getIntent()!=null && getIntent().hasExtra("pickup_time")) {
            pickup_time = getIntent().getStringExtra("pickup_time");
        }
        if(getIntent()!=null && getIntent().hasExtra("address")) {
            address = getIntent().getStringExtra("address");
        }
        if (getIntent() != null && getIntent().hasExtra("contact_number")) {
            contact_number = getIntent().getStringExtra("contact_number");
        }
        if (getIntent() != null && getIntent().hasExtra("order_id")) {
            order_id = getIntent().getIntExtra("order_id", 0);
        }
        if (getIntent() != null && getIntent().hasExtra("user_id")) {
            user_id = getIntent().getIntExtra("user_id", 0);
        }
        if (getIntent() != null && getIntent().hasExtra("tmb_order_id")) {
            tmb_order_id = getIntent().getStringExtra("tmb_order_id");
            binding.tvBookingID.setText("#"+ tmb_order_id);
        }
        if (getIntent() != null && getIntent().hasExtra("latitude")) {
            destLatitude = getIntent().getDoubleExtra("latitude", 0.00);
        }
        if (getIntent() != null && getIntent().hasExtra("longitude")) {
            destLongitude = getIntent().getDoubleExtra("longitude", 0.00);
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

        init();
    }

    private void init() {
        binding.edtBarcode.setFocusable(false);
        binding.edtVacutainers.setFocusable(false);
        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void apiSampleCollectCall() {
        if (!apiCallAlreadyInvoked) {
            String testtube_count = binding.edtVacutainers.getText().toString().trim();
            //String barcode_collection = edtBarcode.getText().toString().trim();
            String session = Constants.getSession(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_COLLECT_SAMPLE;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.collectSample(session, String.valueOf(order_id), testtube_count, barcodeCollection, user_verified);
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    void handleClicks(){

       binding. backBtn.setOnClickListener(v -> onBackPressed());

        binding.launchScannerBtn.setOnClickListener(v -> {IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
            integrator.setPrompt("Scan the Barcode");
            integrator.setOrientationLocked(false);
            integrator.setBeepEnabled(false);
            integrator.initiateScan();
        });

        binding.tvYes.setOnClickListener(v -> {
            binding.tvYes.setBackgroundResource(R.drawable.btn_bg);
            binding.tvYes.setTextColor(Color.parseColor("#ffffff"));
            binding.tvNo.setBackgroundResource(0);
            binding.tvNo.setTextColor(Color.parseColor("#666666"));
            user_verified = "1";
        });

        binding.tvNo.setOnClickListener(v -> {
            binding.tvYes.setBackgroundResource(0);
            binding.tvYes.setTextColor(Color.parseColor("#666666"));
            binding.tvNo.setBackgroundResource(R.drawable.btn_bg);
            binding.tvNo.setTextColor(Color.parseColor("#ffffff"));
            user_verified = "0";
        });

        binding.confirmBtn.setOnClickListener(v -> {
            String barcode_collection = binding.edtBarcode.getText().toString().trim();
            if (barcode_collection.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please launch Scanner to get Barcode..!", Toast.LENGTH_SHORT).show();
            } else if (!lauchedScanner) {
                Toast.makeText(getApplicationContext(), "Please launch Scanner to get Barcode..!", Toast.LENGTH_SHORT).show();
            } else {
                if (Constants.isNetworkAvailable()) {
                    apiSampleCollectCall();
                } else {
                    Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.ivRefresh.setOnClickListener(v -> {
            String barcode = binding.edtBarcode.getText().toString().trim();
            if (barcode != null)
                binding.edtBarcode.setText("");
        });

        binding.tvMinus.setOnClickListener(v -> {
            int minusQnt;
            String qty = binding.edtVacutainers.getText().toString().trim();
            int minus = Integer.parseInt(qty);
            minusQnt = minus - 1;
            if (minusQnt > 0)
                binding.edtVacutainers.setText(""+minusQnt);
        });

        binding.tvPlus.setOnClickListener(v -> {
            int plusQnt;
            String qty = binding.edtVacutainers.getText().toString().trim();
            int plus = Integer.parseInt(qty);
            plusQnt = plus + 1;
            if (plusQnt > 1 && plusQnt < 100)
                binding.edtVacutainers.setText(""+plusQnt);
        });
    }


    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    //Getting the Scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d(TAG, "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                lauchedScanner = false;
            } else {
                Log.d(TAG, "Scanned");
                barcodeCollection = result.getContents();
                Log.d(TAG, "barcodeCollection: "+barcodeCollection);
                Toast.makeText(this, "Scanned: " + barcodeCollection, Toast.LENGTH_LONG).show();
                String barcodeVal = result.getContents();
                Log.d(TAG, "barcodeVal: "+barcodeVal.length());

                if (barcodeVal.length() > 20) {
                    String threeDots = barcodeVal.substring(0, 20)+"...";
                    binding.edtBarcode.setText(threeDots);
                } else {
                    binding.edtBarcode.setText(barcodeVal);
                }

                //edtBarcode.setText(result.getContents());
                lauchedScanner = true;
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
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
            case ServerApi.CODE_COLLECT_SAMPLE: {
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
                    if (ServerApi.CODE_COLLECT_SAMPLE == serviceType) {
                        sampleCollectJsonResponse(jsonObject, success);
                    }
                } else {
                    JSONObject jsonObj = jsonObject.optJSONObject(ServerJsonResponseKey.SCAN_MESSAGE);
                    JSONArray jsonArrayObj = jsonObj.optJSONArray(ServerJsonResponseKey.BARCODE_COLLECTION);
                    try {
                        if (null != jsonArrayObj) {
                            Toast.makeText(mContext, "This Barcode has already been taken..!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(ScanVacutainer.this, message, Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    private void sampleCollectJsonResponse(JSONObject jsonObject, int success) {
        try {
            Log.d(TAG, "sampleCollectJsonResponse: "+jsonObject.toString());
            Log.d(TAG, "statusCode : "+success);
            int status = jsonObject.optInt("data");
            order_status = status;
            Log.d(TAG, "after getting res statusCode: "+status);
            Intent intent = new Intent("updateMode");
            intent.putExtra("status", order_status);
            sendBroadcast(intent);
            if (success == 1) {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                callOrderStatus();
            } else {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callOrderStatus() {
        Intent verifyIntent = new Intent(mContext, OrderStatus.class);
        verifyIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        verifyIntent.putExtra("patient_name",patient_name);
        verifyIntent.putExtra("pickup_date",pickup_date);
        verifyIntent.putExtra("pickup_time",pickup_time);
        verifyIntent.putExtra("address",address);
        verifyIntent.putExtra("contact_number",contact_number);
        verifyIntent.putExtra("order_id",order_id);
        verifyIntent.putExtra("tmb_order_id",tmb_order_id);
        verifyIntent.putExtra("latitude",destLatitude);
        verifyIntent.putExtra("longitude",destLongitude);
        verifyIntent.putExtra("user_id",user_id);
        verifyIntent.putExtra("order_status",order_status);
        verifyIntent.putExtra("from_tag",TAG);
        startActivity(verifyIntent);
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

    @Override
    public void onBackPressed() {
        //hideSoftKeyboard(mActivity);
        super.onBackPressed();
    }
}


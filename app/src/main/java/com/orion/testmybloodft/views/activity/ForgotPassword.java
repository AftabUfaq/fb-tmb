package com.orion.testmybloodft.views.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.orion.testmybloodft.api.ServerParams;
import com.orion.testmybloodft.config.MyVolley;
import com.orion.testmybloodft.databinding.ActivityOtpBinding;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



/**
 * Created by Wekancode on 30-Mar-17.
 */

/**
 * This activity displays a forgot password form and ft gives request for forgot password
 */

public class ForgotPassword extends AppCompatActivity implements ApiResponseView, Handler.Callback {
    private static final String TAG = ForgotPassword.class.getSimpleName();

    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;
    private Activity mActivity;
    private Context mContext;

    private Dialog m_Dialog;
    private ProgressBar m_ProgressBar;
    private String currentpwd = "", newpwd = "";
    private String user_id, change_pwd_status, message, email, android_id, checkUser, verifyCodeStr = "";
    private String userSession, emailId = "";
    private boolean first_time_user, mStatus, mForgotPwd = false;
    private boolean loginUserStatus  = false;
    private int userId;



    ActivityOtpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mActivity = this;
        mContext = this;


        handleClick();

        /* m_Dialog = new Dialog(SignIn.this, R.style.Theme_Transparent);
        m_Dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_Dialog.setContentView(R.layout.progress_loader);
        m_ProgressBar = (ProgressBar) m_Dialog.findViewById(R.id.progressBar);
        m_ProgressBar.setIndeterminate(true);
        //noinspection deprecation,deprecation
        m_ProgressBar.setIndeterminateDrawable(SignIn.this.getResources().getDrawable(R.drawable.loader_anim));
        m_Dialog.setCancelable(false);
        m_Dialog.setCanceledOnTouchOutside(false); */

        init();
        //formValidation();
        if(getIntent()!=null && getIntent().hasExtra("ft_email")) {
            email = getIntent().getStringExtra("ft_email");
            Log.d(TAG, "email: "+email);
        }
        if(getIntent()!=null && getIntent().hasExtra("first_time_user")) {
            first_time_user = getIntent().getBooleanExtra("first_time_user", false);
        }
        if(getIntent()!=null && getIntent().hasExtra("login_user_status")) {
            loginUserStatus = getIntent().getBooleanExtra("login_user_status", false);
        }
        if(getIntent()!=null && getIntent().hasExtra("user_id")) {
            userId = getIntent().getIntExtra("user_id", -1);
        }
        Log.d(TAG, "firstTimeUser: "+first_time_user);
        if (!first_time_user) {
            int maxLength = 6;
            InputFilter[] fArray = new InputFilter[1];
            fArray[0] = new InputFilter.LengthFilter(maxLength);
            binding.edtOtp.setFilters(fArray);
            apiForgotCall(email);
        } else {
            binding.edtOtp.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.edtOtp.setSelection(binding.edtOtp.getText().length());
        }
    }

    private void formValidation() {
        // Change password
        binding.edtOtp.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!first_time_user)
                    if (!binding.edtOtp.getText().toString().trim().isEmpty() && binding.edtOtp.getText().toString().trim().length() < 6) {
                        binding.edtOtp.setError("One Time Password must be a 6 digit");
                    } else {
                        binding.edtOtp.setError(null);
                    }
                else {
                    if (!binding.edtOtp.getText().toString().trim().isEmpty() && binding.edtOtp.getText().toString().trim().length() < 4) {
                        binding.edtOtp.setError("Enter minimum 8 characters");
                    } else {
                        binding.edtOtp.setError(null);
                    }
                }

            }
        });
    }

    private boolean validatePassword() {
        if (!first_time_user) {
            if (binding.edtOtp.getText().toString().trim().isEmpty() || binding.edtOtp.getText().toString().trim().length() < 6) {
                binding.edtOtp.setError("One Time Password must be a 6 digit");
            } else {
                binding.edtOtp.setError(null);
            }
            //return false;
        } else {
            if (binding.edtOtp.getText().toString().trim().isEmpty() || binding.edtOtp.getText().toString().trim().length() < 4) {
                binding. edtOtp.setError("Enter minimum 8 characters");
            } else {
                binding.edtOtp.setError(null);
            }
            //return false;
        }

        return true;
    }

    void handleClick(){
        binding.verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validatePassword()) {
                }else{
                    if (Constants.isNetworkAvailable()) {
                        if (first_time_user)
                            apiLoginCall();
                        else
                            apiVerifyForgotCall(email);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        binding.resendPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constants.isNetworkAvailable()) {
                    Log.d(TAG, "first_time_user: "+first_time_user);
                    if (first_time_user)
                        apiResendPwdCall(email);
                    else
                        apiForgotCall(email);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void apiResendPwdCall(String email) {
        if (!apiCallAlreadyInvoked) {
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_RESEND_PWD;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.getResendPwd(email);
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    private void apiVerifyForgotCall(String email) {
        if (!apiCallAlreadyInvoked) {
            String otp = binding.edtOtp.getText().toString().trim();
            String device_token = Constants.getDeviceToken(mContext);
            String session_token = Constants.getFcmId(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_VERIFY_FORGOT;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.verifyForgot(email, otp, session_token, Constants.DEVICE_NAME, device_token);
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }


    public static String getHashPassword(String pass) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        assert md != null;
        md.update(pass.getBytes());

        byte byteData[] = md.digest();

        //convert the byte to hex format method 2
        StringBuilder hexString = new StringBuilder();
        for (byte aByteData : byteData) {
            String hex = Integer.toHexString(0xff & aByteData);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        Log.d("Decrypt", "getHashPassword: " + hexString.toString());
        return hexString.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void init() {

        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void apiForgotCall(String email) {
        if (!apiCallAlreadyInvoked) {
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_USER_FORGOT;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.getResendPwd(email);
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    private void apiLoginCall() {
        if (!apiCallAlreadyInvoked) {
            String pwd = binding.edtOtp.getText().toString().trim();
            String device_token = Constants.getDeviceToken(mContext);
            String session_token = Constants.getFcmId(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_LOGIN;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.postLoginDetails(email, getHashPassword(pwd), ServerParams.EMAIL_TECHNICIAN, session_token, Constants.DEVICE_NAME);
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
            case ServerApi.CODE_LOGIN:
            case ServerApi.CODE_RESEND_PWD:
            case ServerApi.CODE_VERIFY_FORGOT:
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
        try {
            JSONObject jsonObject = new JSONObject(response);
            Log.d(TAG, "jsonObject: "+jsonObject.toString());
            message = jsonObject.optString("message");
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                Log.d(TAG, "success: "+success);
                if (success == 1) {
                    if (ServerApi.CODE_USER_FORGOT == serviceType) {
                        Toast.makeText(ForgotPassword.this, message, Toast.LENGTH_SHORT).show();
                        successForgotJsonResponse(jsonObject);
                    } else if (ServerApi.CODE_VERIFY_FORGOT == serviceType) {
                        if (first_time_user) {
                            Toast.makeText(ForgotPassword.this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForgotPassword.this, "OTP verified successfully..!", Toast.LENGTH_SHORT).show();
                        }
                        successVerifyForgotJsonResponse(jsonObject);
                    } else if (ServerApi.CODE_RESEND_PWD == serviceType) {
                        Toast.makeText(ForgotPassword.this, message, Toast.LENGTH_SHORT).show();
                        successResendJsonResponse(jsonObject);
                    } else if (ServerApi.CODE_LOGIN == serviceType) {
                        Toast.makeText(ForgotPassword.this, message, Toast.LENGTH_SHORT).show();
                        successLoginJsonResponse(jsonObject);
                    }
                } else {
                    Toast.makeText(ForgotPassword.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);
        }
    }

    private void successLoginJsonResponse(JSONObject jsonObject) {
        Log.d(TAG, "successLoginJsonResponse: "+jsonObject.toString());
        userSession = jsonObject.optString("userSession");
        Constants.setSession(mContext, userSession);
        JSONObject jsonArrayObj = jsonObject.optJSONObject(ServerJsonResponseKey.USER_DATA);
        try {
            if (null != jsonArrayObj) {
                Constants.setLoginStatus(mActivity, true);
                android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                Constants.setDeviceToken(mActivity, android_id);
                int user_id = jsonArrayObj.optInt("id");
                Constants.setUserID(mActivity, user_id);
                callReset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void successForgotJsonResponse(JSONObject jsonObject) {
        try {
            if (null != jsonObject) {
                Log.d(TAG, "forgot: "+jsonObject.toString());
                //callReset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void successResendJsonResponse(JSONObject jsonObject) {
        try {
            if (null != jsonObject) {
                Log.d(TAG, "resend: "+jsonObject.toString());
                //callReset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void successVerifyForgotJsonResponse(JSONObject jsonObject) {
        Log.d(TAG, "successVerifyForgotJsonResponse: "+jsonObject.toString());
        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Constants.setDeviceToken(mActivity, android_id);
        userSession = jsonObject.optString("userSession");
        Constants.setSession(mContext, userSession);
        try {
            JSONObject jsonArrayObj = jsonObject.optJSONObject(ServerJsonResponseKey.USER_DATA);
            if (null != jsonArrayObj) {
                int user_id = jsonArrayObj.optInt("id");
                Constants.setUserID(mActivity, user_id);
                callReset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callReset() {
        setResult(RESULT_OK);
        finish();
    }


    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
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


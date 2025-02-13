package com.orion.testmybloodft.views.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.orion.testmybloodft.databinding.ActivitySigninBinding;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;

import org.json.JSONObject;

/**
 * Created by Wekancode on 30-Mar-17.
 */

/**
 * This activity displays a sign-in form. After putting email page
 * redirect to login page
 */

public class SignIn extends AppCompatActivity implements ApiResponseView, Handler.Callback {
    private static final String TAG = SignIn.class.getSimpleName();

    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;
    private Activity mActivity;
    private Context mContext;

    private Dialog m_Dialog;
    private ProgressBar m_ProgressBar;
    private boolean old_pwd_valid = false, password_valid = false, confirm_password_valid = false;
    private String currentpwd = "", newpwd = "";
    private String user_id, change_pwd_status, message, android_id, checkUser, verifyCodeStr = "";
    private String userSession, emailId = "";
    private boolean mStatus, mForgotPwd = false;

//    @BindView(R.id.nextBtn)
//    Button nextBtn;
//    @BindView(R.id.edtEmail)
//    EditText edtEmail;

    ActivitySigninBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mActivity = this;
        mContext = this;

        handleCLicks();
//        ButterKnife.bind(this);

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
    }

    private void formValidation() {
        binding.edtEmail.addTextChangedListener(new TextWatcher() {

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
                if (!validateEmail()) {
                }
            }
        });


        binding.edtEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!validateEmail()) {
                    }
                }
            }
        });


        binding.edtEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                switch (result) {
                    case EditorInfo.IME_ACTION_DONE:
                        if (!validateEmail()) {
                        }
                        break;
                }
                return false;
            }
        });

    }

    void handleCLicks(){
        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUser = binding.edtEmail.getText().toString().trim();
                
                if (!validateEmail()) {
                    Toast.makeText(mActivity, "validate", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    if (validateEmail()) {
                        String email = binding.edtEmail.getText().toString().trim();
                        if (Constants.isNetworkAvailable()) {
                            apiCall(email);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }


    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /*private boolean validateEmail() {
        emailId = edtEmail.getText().toString().trim();
        if (!isValidEmail(emailId)) {
            edtEmail.setError("Enter valid Email ID");
            return false;
        }

        return true;
    }*/

    private boolean validateEmail() {
        String emailId = binding.edtEmail.getText().toString().trim();

        if (emailId.isEmpty() || !Constants.isValidEmail(emailId)) {
            binding.edtEmail.setError("Enter valid Email ID");
            return false;
        }

        return true;
    }



    @Override
    protected void onResume() {
        super.onResume();
        binding.edtEmail.setText("");
    }

    private void init() {

        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void apiCall(String email) {
        if (!apiCallAlreadyInvoked) {
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_CHK_USER;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.checkEmail(email, ServerParams.EMAIL_TECHNICIAN);
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

            case ServerApi.CODE_CHK_USER: {
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
                if(success==0)
                {
                    Toast.makeText(this,message,Toast.LENGTH_LONG).show();
                }else{
                    successJsonResponse(jsonObject);
                }

                /*if (success == 1) {
                    Log.d(TAG, "parseResponseData 1: "+success);
                    if (ServerApi.CODE_CHK_USER == serviceType) {
                        successJsonResponse(jsonObject);
                    }
                } else if (success == 3 || success == 4) {
                    Log.d(TAG, "parseResponseData 3 && 4: "+success);
                    if (success == 4) {
                        Toast.makeText(SignIn.this, "Reverify your Account using new OTP..!", Toast.LENGTH_SHORT).show();
                    }
                    successLoginResponse(jsonObject);
                    startActivityForResult(new Intent(mContext, ForgotPassword.class).putExtra("ft_email",edtEmail.getText().toString().trim()), 2);
                }  if (success == 2) {
                    // handle users who are not verified

                }else {
//                    startActivityForResult(new Intent(mContext, ForgotPassword.class).putExtra("ft_email",edtEmail.getText().toString().trim()), 2);
                    Toast.makeText(SignIn.this, message, Toast.LENGTH_SHORT).show();
                }*/
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 2: {
                switch (resultCode) {
                    case RESULT_OK: {
                        startActivityForResult(new Intent(mContext, ResetPassword.class),3);
                        break;
                    }
                    case RESULT_CANCELED: {

                        break;
                    }
                }
                break;
            }
            case 3: {
                switch (resultCode) {
                    case RESULT_OK: {
                        Intent verifyIntent = new Intent(this, Schedule.class);
                        startActivity(verifyIntent);
                        finish();
                        break;
                    }
                    case RESULT_CANCELED: {

                        break;
                    }
                }
                break;
            }
        }

    }

    private void successJsonResponse(JSONObject jsonObject) {
        try {
            JSONObject jsonArrayObj = jsonObject.optJSONObject(ServerJsonResponseKey.DATA);
            Log.d(TAG, "successJsonResponse: "+jsonArrayObj);
            if (null != jsonArrayObj) {
                int user_id = jsonArrayObj.optInt("id");
                Constants.setUserID(mActivity, user_id);
                Log.d(TAG, "successJsonResponse user_id: "+Constants.getUserID(mContext));
                change_pwd_status = jsonArrayObj.optString("change_pwd_status");
                Log.d(TAG, "change_pwd_status: "+change_pwd_status);
                android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                Constants.setDeviceToken(mActivity, android_id);

                // First Time User
                /*if (change_pwd_status.equals(null) || change_pwd_status.equals("null") || change_pwd_status.equals("0")) {
                    Log.d(TAG, "First Time User: ");
                    startActivityForResult(new Intent(mContext, ForgotPassword.class).putExtra("ft_email",edtEmail.getText().toString().trim()).putExtra("first_time_user", true), 2);
                }

                //  Regular User
                else if (change_pwd_status.equals("1"))*/ {
                    Log.d(TAG, "Regular User: ");
                    Intent verifyIntent = new Intent(this, LoginActivity.class);
                    verifyIntent.putExtra("ft_email",binding.edtEmail.getText().toString().trim());
                    verifyIntent.putExtra("change_pwd_status",change_pwd_status);
                    startActivity(verifyIntent);
                    finish();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void successLoginResponse(JSONObject jsonObject) {
        try {
            JSONObject jsonArrayObj = jsonObject.optJSONObject(ServerJsonResponseKey.DATA);
            Log.d(TAG, "successLoginResponse: "+jsonArrayObj);
            if (null != jsonArrayObj) {
                int user_id = jsonArrayObj.optInt("id");
                Log.d(TAG, "successLoginResponse user_id: "+user_id);
                Constants.setUserID(mActivity, user_id);
                android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                Constants.setDeviceToken(mActivity, android_id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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


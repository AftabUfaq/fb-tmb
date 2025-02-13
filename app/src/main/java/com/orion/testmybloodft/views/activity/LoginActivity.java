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
import android.view.inputmethod.InputMethodManager;
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
import com.orion.testmybloodft.databinding.ActivityLoginBinding;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Created by Wekancode on 30-Mar-17.
 */

/**
 * This activity displays a login form and ft gives credentials and access app
 */

public class LoginActivity extends AppCompatActivity implements ApiResponseView, Handler.Callback {
    private static final String TAG = LoginActivity.class.getSimpleName();

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
    private String user_id, change_pwd_status, message, android_id, email = "";
    private String userSession, emailId = "";
    private boolean mStatus, mForgotPwd = false;
    private int success;

    private int loginSuccessUserId=-1;
//    @BindView(R.id.edtEmail)
//    EditText edtEmail;
//    @BindView(R.id.edtPassword)
//    EditText edtPassword;
//    @BindView(R.id.forgotPwd)
//    TextView forgotPwd;
//    @BindView(R.id.resendPwd)
//    TextView resendPwd;

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityLoginBinding.inflate(getLayoutInflater());
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

        if(getIntent()!=null && getIntent().hasExtra("ft_email")) {
            email = getIntent().getStringExtra("ft_email");
            binding.edtEmail.setText(email);
        }
        /*if (getIntent() != null && getIntent().hasExtra("change_pwd_status")) {
            change_pwd_status = getIntent().getStringExtra("change_pwd_status");
            Log.d(TAG, "Login change_pwd_status: " + change_pwd_status);
            if (change_pwd_status.equals("1")) {
                forgotPwd.setVisibility(View.VISIBLE);
            } else {
                resendPwd.setVisibility(View.VISIBLE);
            }
            forgotPwd.setVisibility(View.VISIBLE);

        }*/
        binding.forgotPwd.setVisibility(View.VISIBLE);

        init();
        formValidation();
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

        /*edtPassword.addTextChangedListener(new TextWatcher() {

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
                if (!validatePassword()) {
                }
            }
        });*/

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

    void handleClick(){
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "login btn triggered: ");
                if (Constants.isNetworkAvailable()) {
                    if (!validatePassword()) {
                    }else{
                        apiLoginCall();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.resendPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "resendPwd triggered: ");
                String email = binding.edtEmail.getText().toString().trim();
                apiResendPwdCall(email);
                if (Constants.isNetworkAvailable()) {
                    apiResendPwdCall(email);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.forgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(mContext, ForgotPassword.class).putExtra("ft_email",binding.edtEmail.getText().toString().trim()), 2);
            }
        });


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
            }case 4: {
                switch (resultCode) {
                    case RESULT_OK: {
                        Constants.setLoginStatus(mActivity, true);
                        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                        Constants.setDeviceToken(mActivity, android_id);
                        Constants.setUserID(mActivity, loginSuccessUserId);

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

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean validateEmail() {
        emailId = binding.edtEmail.getText().toString().trim();
        if (!emailId.isEmpty() && !isValidEmail(emailId)) {
            binding.edtEmail.setError("Enter valid Email ID");
            return false;
        }

        return true;
    }

    private boolean validatePassword() {
        String pass = binding.edtPassword.getText().toString().trim();
        if (pass.isEmpty() || binding.edtPassword.getText().toString().trim().length() < 8) {
            binding.edtPassword.setError("Enter min 8 characters");
            return false;
        }

        return true;
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

    private void apiLoginCall() {
        if (!apiCallAlreadyInvoked) {
            String email = binding.edtEmail.getText().toString().trim();
            String pwd = binding.edtPassword.getText().toString().trim();
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
            case ServerApi.CODE_RESEND_PWD:
            case ServerApi.CODE_USER_FORGOT:
            case ServerApi.CODE_LOGIN: {
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
                success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                Log.d("jsonObjectAB ", "success 222: "+success);
                Log.d("jsonObjectAB", "parseResponseData: "+serviceType + "dsdsds" + ServerApi.CODE_LOGIN);

                if (success == 2) {
                    if (ServerApi.CODE_LOGIN == serviceType || true) {
                        successLoginJsonResponse(jsonObject);
                    } else if (ServerApi.CODE_USER_FORGOT == serviceType) {
                        successForgotJsonResponse(jsonObject);
                    } else if (ServerApi.CODE_RESEND_PWD == serviceType) {
                        successResendJsonResponse(jsonObject);
                    }
                } else {
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
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
                //uncomment the bellow code to work
                Constants.setLoginStatus(mActivity, true);
                android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                Constants.setDeviceToken(mActivity, android_id);
                int user_id = jsonArrayObj.optInt("id");
                Constants.setUserID(mActivity, user_id);
                callSchedule(user_id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void successForgotJsonResponse(JSONObject jsonObject) {
        try {
            if (null != jsonObject) {
                Log.d(TAG, "forgot: "+jsonObject.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void successResendJsonResponse(JSONObject jsonObject) {
        try {
            if (null != jsonObject) {
                Log.d(TAG, "resend: "+jsonObject.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callSchedule(int userID) {
        hideSoftKeyboard(mActivity);
        Intent intent = new Intent(this,ForgotPassword.class);
        intent.putExtra("ft_email",binding.edtEmail.getText().toString().trim());
        intent.putExtra("user_id",userID);
        intent.putExtra("login_user_status",true);
loginSuccessUserId=userID;
        startActivityForResult(intent,4);


//        Intent verifyIntent = new Intent(this, Schedule.class);
//        verifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(verifyIntent);
//        finish();
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
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


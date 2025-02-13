package com.orion.testmybloodft.views.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import com.orion.testmybloodft.databinding.ActivityResetBinding;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Wekancode on 30-Mar-17.
 */

/**
 * This activity displays a reset password and gives request for reset their password
 */

public class ResetPassword extends AppCompatActivity implements ApiResponseView, Handler.Callback {
    private static final String TAG = ResetPassword.class.getSimpleName();

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

//    @BindView(R.id.edtNewPass)
//    EditText edtNewPass;
//    @BindView(R.id.edtConfirmPass)
//    EditText edtConfirmPass;
//    @BindView(R.id.newPassView)
//    View mNewPassView;
//    @BindView(R.id.confPassView)
//    View mConfPassView;

    ActivityResetBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityResetBinding.inflate(getLayoutInflater());
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
        formValidation();
    }

    private void formValidation() {
        binding.edtConfirmPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                switch (result) {
                    case EditorInfo.IME_ACTION_DONE:
                        Log.d(TAG, "validateNewPasswordStatus: "+validateNewPassword());
                        if (!validateNewPassword()) {
                        } else if (!validateConfirmPassword()) {
                        } else {
                            newpwd = binding.edtNewPass.getText().toString().trim();
                            if (Constants.isNetworkAvailable()) {
                                apiResetPwdCall();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
                            }

                        }
                        break;
                }
                return false;
            }
        });


        binding.edtNewPass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.newPassView.setBackgroundColor(0xFF28AEF5);
                    binding.confPassView.setBackgroundColor(0xFF333333);
                }
            }
        });

        binding.edtConfirmPass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                   binding.newPassView.setBackgroundColor(0xFF333333);
                   binding.confPassView.setBackgroundColor(0xFF28AEF5);
                }
            }
        });
    }
    void handleClick(){

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "validateNewPasswordStatus: "+validateNewPassword());
                if (!validateNewPassword()) {
                } else if (!validateConfirmPassword()) {
                } else {
                    newpwd = binding.edtNewPass.getText().toString().trim();
                    if (Constants.isNetworkAvailable()) {
                        apiResetPwdCall();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        binding.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(mActivity);
                onBackPressed();
            }
        });
    }



    private boolean validateNewPassword() {
        String pass = binding.edtNewPass.getText().toString().trim();
        if (pass.isEmpty() || binding.edtNewPass.getText().toString().trim().length() < 6) {
            binding.edtNewPass.setError("Enter min 6 characters");
            return false;
        }

        return true;
    }

    private boolean validateConfirmPassword() {
        String pass = binding.edtNewPass.getText().toString().trim();
        String confirm = binding.edtConfirmPass.getText().toString().trim();
        if (!pass.equals(confirm)) {
            binding.edtConfirmPass.setError("Password Not matching");
            return false;
        }

        return true;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    private boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{4,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

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

    private void apiResetPwdCall() {
        if (!apiCallAlreadyInvoked) {
            Log.d(TAG, "apiResetPwdCall userId: "+Constants.getUserID(mContext));
            String device_token = Constants.getDeviceToken(mContext);
            String new_pass = binding.edtNewPass.getText().toString().trim();
            String confirm_pass = binding.edtConfirmPass.getText().toString().trim();
            int user_id = Constants.getUserID(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_RESET_PWD;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.resetPassword(String.valueOf(user_id), getHashPassword(new_pass), getHashPassword(confirm_pass), Constants.DEVICE_NAME, device_token);
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

            case ServerApi.CODE_RESET_PWD: {
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
            Log.d(TAG, "parseResponseData: "+response);
            Log.d(TAG, "parseResponseData serviceType: "+serviceType);
            JSONObject jsonObject = new JSONObject(response);
            Log.d(TAG, "jsonObject: "+jsonObject.toString());
            message = jsonObject.optString("message");
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                Log.d(TAG, "success: "+success);
                if (success == 1) {
                    if (ServerApi.CODE_RESET_PWD == serviceType) {
                        successResetPwdJsonResponse(jsonObject);
                    }
                    Toast.makeText(ResetPassword.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ResetPassword.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);
        }
    }

    private void successResetPwdJsonResponse(JSONObject jsonObject) {
        try {
            Log.d(TAG, "successResetPwdJsonResponse: "+jsonObject.toString());
            if (null != jsonObject) {
                userSession = jsonObject.optString("userSession");
                Log.d(TAG, "successResetPwdJsonResponse userSession: "+userSession);
                Constants.setSession(mContext, userSession);
                Constants.setLoginStatus(mActivity, true);
                callSchedule();
            }
            JSONObject jsonArrayObj = jsonObject.optJSONObject(ServerJsonResponseKey.USER_DATA);
            if (null != jsonArrayObj) {
                Log.d(TAG, "jsonArrayObj: "+jsonArrayObj.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callSchedule() {
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


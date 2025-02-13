package com.orion.testmybloodft.views.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.orion.testmybloodft.R;
import com.orion.testmybloodft.api.ApiModel;
import com.orion.testmybloodft.api.ApiResponseView;
import com.orion.testmybloodft.api.ApiViewPresenter;
import com.orion.testmybloodft.api.ServerApi;
import com.orion.testmybloodft.api.ServerJsonResponseKey;
import com.orion.testmybloodft.api.ServerParams;
import com.orion.testmybloodft.databinding.ActivityVerifyLoginOtpBinding;
import com.orion.testmybloodft.utils.Constants;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class VerifyLoginWithOTP extends AppCompatActivity implements ApiResponseView, Handler.Callback  {

//    @BindView(R.id.edtOtp) EditText edtOtp;
//    @BindView(R.id.verifyBtn) Button btnVerify;
//    @BindView(R.id.resendPwd) TextView resendPwd;
//

    ApiViewPresenter apiViewPresenter;
    private boolean apiCallAlreadyInvoked = false;
    private boolean apiCallInvoked = false;
    String TAG ="VerifyLoginWithOTP";

    ActivityVerifyLoginOtpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityVerifyLoginOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        ButterKnife.bind(this);
        initialization();
        buttonClicks();


    }

    private void buttonClicks() {
        binding.verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyUserOtp();
            }
        });
        binding.resendPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiResendOTP();
            }
        });
    }

    String userEmail,message,userSession="",android_id="";
    private void initialization() {
        if(getIntent()!=null && getIntent().hasExtra("ft_email")) {
            userEmail = getIntent().getStringExtra("ft_email");
        }
    }


    private void apiResendOTP() {
        if (!apiCallAlreadyInvoked) {
            String otp = binding.edtOtp.getText().toString().trim();
            String device_token = Constants.getDeviceToken(this);
            String session_token = Constants.getFcmId(this);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_VERIFY_FORGOT;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.verifyForgot(userEmail, getHashPassword(otp), session_token, Constants.DEVICE_NAME, device_token);
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    private void verifyUserOtp() {
        if (!binding.edtOtp.getText().toString().trim().isEmpty() && binding.edtOtp.getText().toString().trim().length() < 6) {
            binding.edtOtp.setError("One Time Password must be a 6 digit");
        } else {
            binding.edtOtp.setError(null);
            callApiToValidateOTP(binding.edtOtp.getText().toString().trim());
        }
    }

    private void callApiToValidateOTP(String otp) {
        if (!apiCallAlreadyInvoked) {
            String device_token = Constants.getDeviceToken(this);
            String session_token = Constants.getFcmId(this);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = "VerifyLoginWithOTP";
            apiModel.serviceType = ServerApi.CODE_VERIFY_FORGOT;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.verifyForgot(userEmail, getHashPassword(otp), session_token, Constants.DEVICE_NAME, device_token);
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
    public void onNoInternet(int serviceType) {

    }

    @Override
    public void showProgress(int serviceType) {

    }

    @Override
    public void hideProgress(int serviceType) {

    }

    @Override
    public void onSuccessData(String response, int serviceType) {

    }

    @Override
    public void onSuccessException(String response, Throwable throwable, int serviceType) {

    }

    @Override
    public void onErrorData(Throwable throwable, int serviceType) {

    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case ServerApi.CODE_LOGIN:
            case ServerApi.CODE_RESEND_PWD:
            case ServerApi.CODE_VERIFY_FORGOT:
            case ServerApi.CODE_USER_FORGOT: {
                parseResponseData(message.getData().getString(ServerJsonResponseKey.RESPONSE, null), message.what);
                break;
            }
        }
        return false;
    }

    private void parseResponseData(String response, int serviceType) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            message = jsonObject.optString("message");
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);

                if (success == 1) {
                  if (ServerApi.CODE_RESEND_PWD == serviceType) {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    } else if (ServerApi.CODE_LOGIN == serviceType) {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        successLoginJsonResponse(jsonObject);
                    }
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);

        }
    }

    private void successLoginJsonResponse(JSONObject jsonObject) {
        Log.d(TAG, "successLoginJsonResponse: "+jsonObject.toString());
        userSession = jsonObject.optString("userSession");
        Constants.setSession(this, userSession);
        JSONObject jsonArrayObj = jsonObject.optJSONObject(ServerJsonResponseKey.USER_DATA);
        try {
            if (null != jsonArrayObj) {
                Constants.setLoginStatus(this, true);
                android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                Constants.setDeviceToken(this, android_id);
                int user_id = jsonArrayObj.optInt("id");
                Constants.setUserID(this, user_id);
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
}

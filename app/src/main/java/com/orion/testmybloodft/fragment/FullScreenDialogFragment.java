package com.orion.testmybloodft.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

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
import com.orion.testmybloodft.database.DBHelper;
import com.orion.testmybloodft.databinding.FullscreenDialogBinding;
import com.orion.testmybloodft.service.DeleteTokenService;
import com.orion.testmybloodft.service.SocketService;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;
import com.orion.testmybloodft.views.activity.ChangePassword;
import com.orion.testmybloodft.views.activity.Profile;
import com.orion.testmybloodft.views.activity.SignIn;
import org.json.JSONObject;
import io.socket.client.Socket;


/**
 * Created by ${Arun} on 07/07/17.
 */

/**
 * This fragment displays a ft profile details and change ft password
 */

@SuppressWarnings({"ALL", "unused"})
public class FullScreenDialogFragment extends DialogFragment implements ApiResponseView, Handler.Callback {
    private static final String TAG = FullScreenDialogFragment.class.getSimpleName();

    private Context mContext;
    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;

    private Dialog m_Dialog;
    private ProgressBar m_ProgressBar;

    private String message = "";
    private FullScreenDialogFragment dialogFragment;
    private SocketService mBoundService;
    private boolean mServiceBound = false;
    private Socket socketEmitObj;

    public static FullScreenDialogFragment newInstance(String tag, Activity activity) {
        FullScreenDialogFragment fragment = new FullScreenDialogFragment();
        Bundle args = new Bundle();
        args.putString("FRAGMENT_TAG", tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }
    private FullscreenDialogBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_Transparent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FullscreenDialogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        init();
        handleClick();
        return root;
    }

    private void init() {
        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void apiLogoutCall() {
        if (!apiCallAlreadyInvoked) {
            //String session = Constants.getSession(getContext());
            String session_token = Constants.getFcmId(mContext);
            String device_token = Constants.getDeviceToken(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_FT_LOGOUT;
            apiModel.context = mContext;
            apiModel.method = 1;
            apiModel.params = ServerParams.logout(session_token, Constants.DEVICE_NAME, device_token);
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }


   void handleClick(){

        binding.closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePopup();
            }
        });
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Profile.class));

            }
        });
        binding.changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ChangePassword.class));
            }
        });
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constants.isNetworkAvailable()) {
                    apiLogoutCall();
                }
                else
                {
                    Toast.makeText(mContext, Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    public void closePopup() {
        notifyToTarget(AppCompatActivity.RESULT_CANCELED);
        FullScreenDialogFragment.this.dismiss();
    }

    private void notifyToTarget(int code) {
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            targetFragment.onActivityResult(getTargetRequestCode(), code, null);
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

            case ServerApi.CODE_FT_LOGOUT: {
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
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                message = jsonObject.optString("message");
                Log.d(TAG, "message: "+message);
                if (success == 1) {
                    callLogin();
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);
        }
    }

    private void callLogin() {
        Log.i(TAG, "logout btn triggered... ");
        try {
            Log.d(TAG, "FCM deleteInstanceId deleting..");
            //FirebaseInstanceId.getInstance().deleteInstanceId();
            //FirebaseMessaging.getInstance().unsubscribeFromTopic(userID);
            mContext.startService(new Intent(mContext, DeleteTokenService.class));
            //Log.d(TAG, "FCM deleteInstanceId deleted");
        } catch (Exception e){
            Log.e(TAG, "FCM deleteInstanceId token ", e);
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mServiceBound) {
                    mContext.unbindService(mServiceConnection);
                    mServiceBound = false;
                }
                // NNN
                mContext.stopService(new Intent(mContext, SocketService.class));
                socketEmitObj = App.getInstance().getSocket();
                socketEmitObj.off();
                socketEmitObj.disconnect();
            }
        }, 2000);
        Log.d(TAG, "instanceStatus: "+mServiceBound);

        Constants.clearPreferences(getActivity());
        DBHelper dbHelper = new DBHelper();
        dbHelper.deleteTodayTable();

        Intent intent = new Intent(mContext, SignIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.SocketBinder myBinder = (SocketService.SocketBinder) service;
            mBoundService = myBinder.getService();
            mServiceBound = true;
        }
    };

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

package com.orion.testmybloodft.views.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
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
import com.orion.testmybloodft.utils.BadgeUtils;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;

import org.json.JSONObject;

/**
 * Created by Wekancode on 30-Mar-17.
 */

/**
 * This activity displays a splash screen
 */

public class Splash extends AppCompatActivity implements ApiResponseView, Handler.Callback {
    private static final String TAG = Splash.class.getSimpleName();

    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;
    private Dialog m_Dialog;
    private ProgressBar m_ProgressBar;
    private Thread mSplashThread;
    private boolean loginStatus = false;
    private Context mContext;
    private Activity mActivity;
    private String message, type ,count, order_id, notification_id, date = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        Log.i(TAG, "onCreate");
        mContext = this;
        mActivity = this;

        /*m_Dialog = new Dialog(Splash.this, R.style.Theme_Transparent);
        m_Dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_Dialog.setContentView(R.layout.progress_loader);
        m_ProgressBar = (ProgressBar) m_Dialog.findViewById(R.id.progressBar);
        m_ProgressBar.setIndeterminate(true);
        //noinspection deprecation,deprecation
        m_ProgressBar.setIndeterminateDrawable(Splash.this.getResources().getDrawable(R.drawable.loader_anim));
        m_Dialog.setCancelable(false);
        m_Dialog.setCanceledOnTouchOutside(false); */

        launchScrreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    private void bgPush(){
        try {
            Intent intent = getIntent();
            if(intent != null){
                Bundle bundle= intent.getExtras();
                if(bundle!=null) {
                    type = bundle.getString("type");
                    count = bundle.getString("count");
                    order_id = bundle.getString("order_id");
                    notification_id = bundle.getString("notification_id");
                    date = bundle.getString("date");
                    Log.d(TAG, "bgPush type: "+type);
                    Log.d(TAG, "count: "+count);
                    Log.d(TAG, "order_id: "+order_id);
                    Log.d(TAG, "notification_id: "+ notification_id);
                    Log.d(TAG, "date: "+ date);

                    if (count !=null) {
                        BadgeUtils.setBadge(getApplicationContext(), Integer.parseInt(count));
                    }

                    // Set Notification as read
                    /*if (notification_id !=null) {
                        readNotifyApiCall(notification_id);
                    }*/

                    int scheduleTabPos = -1;
                    if (type !=null) {
                        if (type.equals("13")) {
                            scheduleTabPos = 0;

                            Intent reload = new Intent("tabUpdate");
                            reload.putExtra("scheduleTabPos", scheduleTabPos);
                            reload.putExtra("type", Integer.parseInt(type));
                            reload.putExtra("date", date);
                            reload.putExtra("new_push", Integer.parseInt(count));
                            sendBroadcast(reload);
                        } else if (type.equals("14")) {
                            scheduleTabPos = 1;

                            Intent reload = new Intent("tabUpdate");
                            reload.putExtra("scheduleTabPos", scheduleTabPos);
                            reload.putExtra("type", Integer.parseInt(type));
                            reload.putExtra("date", date);
                            reload.putExtra("new_push", Integer.parseInt(count));
                            sendBroadcast(reload);
                        } else if (type.equals("15")) {
                            scheduleTabPos = 0;

                            Intent reload = new Intent("tabUpdate");
                            reload.putExtra("scheduleTabPos", scheduleTabPos);
                            reload.putExtra("type", Integer.parseInt(type));
                            reload.putExtra("date", date);
                            reload.putExtra("new_push", Integer.parseInt(count));
                            sendBroadcast(reload);
                        }

                        Constants.setType(mContext, Integer.parseInt(type));
                        Constants.setTabPos(mContext, scheduleTabPos);
                        Constants.setDate(mContext, date);
                        Log.d(TAG, "Date: "+Constants.getDate(mContext));
                        Constants.setFirstTimeLaunch(mContext, true);
                    }
                }
            }
        } catch (Exception e){
            Log.d(TAG, "getIntent ", e);
        }

    }

    private void launchScrreen() {
        loginStatus = Constants.getLoginStatus(mContext);
        Log.d(TAG, "launchScrreen: " + loginStatus);
        final Splash sPlashScreen = this;
        mSplashThread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(3000);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                // Landing page
                if (loginStatus) {
                    init();
                    bgPush();
                    Intent landingIntent = new Intent(sPlashScreen, Schedule.class);
                    // Closing all the Activities from stack
                    landingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    // Add new Flag to start new Activity
                    landingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //landingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(landingIntent);
                    finish();
                } else {
                    startActivity(new Intent(sPlashScreen, SignIn.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                }

            }
        };
        mSplashThread.start();
    }

    private void init() {
        Looper.prepare();
        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void readNotifyApiCall(String id) {
        if (!apiCallAlreadyInvoked) {
            String session = Constants.getSession(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_CLEAR_NOTIFICATION;
            apiModel.context = mContext;
            apiModel.method = 1;
            apiModel.params = ServerParams.clearNotification(session, id);
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

            case ServerApi.CODE_CLEAR_NOTIFICATION: {
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
                if (success == 1) {
                    if (ServerApi.CODE_CLEAR_NOTIFICATION == serviceType) {
                        readNotificationJsonResponse(jsonObject);
                    }
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);
        }
    }

    private void readNotificationJsonResponse(JSONObject jsonObject) {
        Log.d(TAG, "readNotificationJsonResponse: "+jsonObject.toString());
        try {
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                String message = jsonObject.optString("message");
                if (success == 1) {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);
        }
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
        if(apiViewPresenter != null) {
            apiViewPresenter.onDestroy();
        }

        if(apiCallInvoked){
            MyVolley.getInstance(getApplicationContext()).getRequestQueue().cancelAll(TAG);
        }
    }
}


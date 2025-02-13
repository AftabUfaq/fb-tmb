/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orion.testmybloodft.push;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.orion.testmybloodft.R;
import com.orion.testmybloodft.api.ApiModel;
import com.orion.testmybloodft.api.ApiResponseView;
import com.orion.testmybloodft.api.ApiViewPresenter;
import com.orion.testmybloodft.api.ApiViewPresenterImpl;
import com.orion.testmybloodft.api.ServerApi;
import com.orion.testmybloodft.api.ServerJsonResponseKey;
import com.orion.testmybloodft.api.ServerParams;
import com.orion.testmybloodft.utils.BadgeUtils;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;
import com.orion.testmybloodft.views.activity.Schedule;

import org.json.JSONObject;

/**
 * We getting notification data. The notification key in the
 * data bundle contains the notification
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService implements ApiResponseView,Handler.Callback {
    private static final String TAG = "MyFirebaseMsgService";
    //private final int FCM_REQUEST_CODE = 1003;
    private final int FCM_REQUEST_CODE = 0;

    private String message = "";

    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Data: " + remoteMessage.getData());
        init();

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.i(TAG, "FCM Message data payload: " + remoteMessage.getData());
            JSONObject jsonObject = new JSONObject(remoteMessage.getData());
            String bodyMsg = remoteMessage.getNotification().getBody();
            try {
                sendNotificationJsonData(jsonObject, bodyMsg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (remoteMessage.getNotification() != null) { // Check if message contains a notification payload.
            String bodyMsg = remoteMessage.getNotification().getBody();
            Log.i(TAG, "FCM Message Notification Body: " + bodyMsg);
            sendNotification(bodyMsg);
        }

        //Calling method to generate notification
        sendNotification(remoteMessage.getNotification().getBody());
    }
    //http://52.33.94.167/v2/user

    private void clearNotifyApiCall(String id) {
        if (!apiCallAlreadyInvoked) {
            String session = Constants.getSession(getApplicationContext());
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_CLEAR_NOTIFICATION;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.clearNotification(session, id);
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    private void init() {
        Looper.prepare();
        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
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
                        clearNotificationJsonResponse(jsonObject);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);
        }
    }

    private void clearNotificationJsonResponse(JSONObject jsonObject) {
        Log.d(TAG, "clearNotificationJsonResponse: "+jsonObject.toString());
        try {
            if (null != jsonObject) {
                int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
                String message = jsonObject.optString("message");
                if (success == 1) {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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

    private void sendNotificationJsonData(JSONObject jsonObject, String messageBody) {
        Log.d(TAG, "sendNotificationJsonData: "+jsonObject.toString());
        String type ="";
        String count ="";
        String order_id ="";
        String notification_id ="";
        String date ="";

        try {
            type = jsonObject.optString("type");
            count = jsonObject.optString("count");
            order_id = jsonObject.optString("order_id");
            notification_id = jsonObject.optString("notification_id");
            date = jsonObject.optString("date");

            // Set Notification as read
            /*if (notification_id !=null) {
                clearNotifyApiCall(notification_id);
            }*/

            Log.d(TAG, "type: "+type);
            Log.d(TAG, "count: "+count);
            Log.d(TAG, "order_id: "+order_id);
            Log.d(TAG, "notification_id: "+notification_id);
            Log.d(TAG, "date: "+date);

            if (count !=null) {
                BadgeUtils.setBadge(getApplicationContext(), Integer.parseInt(count));
            }

            int scheduleTabPos = -1;
            if(type.equals("13")) {
                scheduleTabPos = 0;

                Intent reload = new Intent("tabUpdate");
                reload.putExtra("scheduleTabPos", scheduleTabPos);
                reload.putExtra("type", Integer.parseInt(type));
                reload.putExtra("date", date);
                reload.putExtra("new_push", Integer.parseInt(count));
                sendBroadcast(reload);
            } else if(type.equals("14")) {
                scheduleTabPos = 1;

                Intent reload = new Intent("tabUpdate");
                reload.putExtra("scheduleTabPos", scheduleTabPos);
                reload.putExtra("type", Integer.parseInt(type));
                reload.putExtra("date", date);
                reload.putExtra("new_push", Integer.parseInt(count));
                sendBroadcast(reload);
            } else if(type.equals("15")){
                scheduleTabPos = 0;

                Intent reload = new Intent("tabUpdate");
                reload.putExtra("scheduleTabPos", scheduleTabPos);
                reload.putExtra("type", Integer.parseInt(type));
                reload.putExtra("date", date);
                reload.putExtra("new_push", Integer.parseInt(count));
                sendBroadcast(reload);
            }
            Constants.setInAppType(getApplicationContext(), Integer.parseInt(type));
            Constants.setInAppTabPos(getApplicationContext(), scheduleTabPos);
            Constants.setInAppDate(getApplicationContext(), date);
            Constants.setInAppFirstTime(getApplicationContext(), true);

            Intent landingIntent = new Intent(this, Schedule.class);
            //landingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            landingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, landingIntent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.notify_icon)
                    .setContentTitle(Constants.TMB_NOTIFICATION)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, notificationBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, Schedule.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.notify_icon)
                .setContentTitle(Constants.TMB_NOTIFICATION)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}

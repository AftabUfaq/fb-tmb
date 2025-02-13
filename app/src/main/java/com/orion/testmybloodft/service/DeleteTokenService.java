package com.orion.testmybloodft.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.orion.testmybloodft.utils.Constants;

import java.io.IOException;


/**
 * Created by Arun on 12/27/2017.
 */

/**
 * Here deleting device token while tapping on logout from profile screen
 */

public class DeleteTokenService extends IntentService {
    public static final String TAG = DeleteTokenService.class.getSimpleName();

    public DeleteTokenService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // Check for current token
            Log.d(TAG, "FCM Token before deletion: " + Constants.getFcmId(getApplicationContext()));

            // Resets Instance ID and revokes all tokens.
            FirebaseInstanceId.getInstance().deleteInstanceId();

            // Clear current saved token
            Constants.setFcmId(getApplicationContext(), "");

            // Check for success of empty token
            Log.d(TAG, "FCM Token deleted. Proof: " + Constants.getFcmId(getApplicationContext()));

            // Now manually call onTokenRefresh()
            Log.d(TAG, "FCM Getting new token");
            FirebaseInstanceId.getInstance().getToken();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
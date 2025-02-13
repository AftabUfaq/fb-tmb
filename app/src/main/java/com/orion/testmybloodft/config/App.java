package com.orion.testmybloodft.config;

import android.content.Context;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.orion.testmybloodft.database.DBHelper;
import com.orion.testmybloodft.database.DatabaseManager;
import com.orion.testmybloodft.models.TestListMod;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.TimeUtils;

import java.net.URISyntaxException;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;


/**
 * Created by Arun on 30-Mar-17.
 */

/**
 * Application class for TMB - FT
 */

public class App extends MultiDexApplication {

    private static final String TAG = App.class.getSimpleName();

    private static App mInstance;

    private static DBHelper dbHelper;

    private List<TestListMod> testList;

    public List<TestListMod> getTestList() {
        return testList;
    }

    public void setTestList(List<TestListMod> testList) {
        this.testList = testList;
    }

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket(Constants.QA_SOCKET_URL);
        } catch (URISyntaxException e) {
            //throw new RuntimeException(e);
            Log.e(TAG, ("URISyntaxException : " + Constants.QA_SOCKET_URL), e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        TimeUtils.setAppStartTime(getApplicationContext(), System.currentTimeMillis());

        dbHelper = new DBHelper();
        DatabaseManager.initializeInstance(dbHelper);

        MyVolley.getInstance(getBaseContext()).initOkHttp3Stack(this);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized App getInstance() {
        return mInstance;
    }
}

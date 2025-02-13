package com.orion.testmybloodft.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orion.testmybloodft.api.ApiModel;
import com.orion.testmybloodft.api.ApiResponseView;
import com.orion.testmybloodft.api.ApiViewPresenter;
import com.orion.testmybloodft.api.ApiViewPresenterImpl;
import com.orion.testmybloodft.api.ServerApi;
import com.orion.testmybloodft.api.ServerJsonResponseKey;
import com.orion.testmybloodft.api.ServerParams;
import com.orion.testmybloodft.models.TodayMod;
import com.orion.testmybloodft.service.SocketService;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Socket running in the background phone got
 * switched off due to battery drain, Once phone got switched
 * the broadcast receiver will restart socket service again
 */

public class ScheduleTaskReciever extends BroadcastReceiver implements ApiResponseView, Handler.Callback {
	private static final String TAG = ScheduleTaskReciever.class.getSimpleName();

	private List<TodayMod> todayMods = new ArrayList<TodayMod>();
	private String todayAsString, message = "";

	/*Api Call Interface */
	ApiViewPresenter apiViewPresenter;
	private boolean apiCallInvoked = false;
	private boolean apiCallAlreadyInvoked = false;
	private Handler apiCallHandler;

	private boolean isBootCompleted = false;
	private Context mContext;
	private int user_id, status, field_tech_id, order_id;
	private String fieldTechName = "";
	private SocketService mBoundService;
	private boolean mServiceBound = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		Log.d(TAG, "onReceiveScheduleTask called: ");
		mContext = context;

		getDate();
		init();

		if (Constants.isNetworkAvailable()) {
			apiCall();
		} else {
			Toast.makeText(mContext, Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
		}
	}

	private void init() {
		apiViewPresenter = new ApiViewPresenterImpl(this);
		apiCallHandler = new Handler(this);
	}

	private void getDate() {
		Calendar calendar = Calendar.getInstance();
		Date today = calendar.getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		todayAsString = dateFormat.format(today);
		Log.d(TAG, "todayAsString: " + todayAsString);
	}

	private void apiCall() {
		if (!apiCallAlreadyInvoked) {
			String session = Constants.getSession(mContext);
			Log.d(TAG, "session: " + session);
			ApiModel apiModel = new ApiModel();
			apiModel.FROM_TAG = TAG;
			apiModel.serviceType = ServerApi.CODE_MY_ORDERS;
			apiModel.context = mContext;
			apiModel.method = 1;
			apiModel.params = ServerParams.getOrders(session, todayAsString, "1,2,10,11", Constants.LIST_START_SIZE, Constants.DATA_REQUIRED);
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

			case ServerApi.CODE_MY_ORDERS: {
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
				int success = jsonObject.optInt(ServerJsonResponseKey.SUCCESS);
				if (success == 1) {
					successJsonResponse(jsonObject);
				} else {
					Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
				}
			}
		} catch (Exception e) {
			Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
		}
	}

	private void successJsonResponse(JSONObject jsonObject) {
		try {
			int count = jsonObject.optInt("count");
			Log.d(TAG, "successJsonResponse count: " + count);
			JSONArray jsonArrayObj = jsonObject.optJSONArray(ServerJsonResponseKey.DATA);
			if (null != jsonArrayObj) {
				if (jsonArrayObj.length() > 0) {
					Log.d(TAG, "successJsonResponse: " + jsonArrayObj.toString());
					for (int i = 0; i < jsonArrayObj.length(); i++) {
						JSONObject areaObj = jsonArrayObj.optJSONObject(i);
						Gson gson = new Gson();
						TodayMod todayMod = gson.fromJson(areaObj.toString(), TodayMod.class);
						todayMods.add(todayMod);
					}
					callSocketService();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void callSocketService() {
		Log.d(TAG, "myOrderSize: " + todayMods.size());
		if(todayMods.size() > 0) {
			user_id = todayMods.get(0).getUser_id();
			status = todayMods.get(0).getStatus();
			field_tech_id = todayMods.get(0).getField_tech_id();
			order_id = todayMods.get(0).getId();
			fieldTechName = todayMods.get(0).getField_tech_name();
		}

		if (status == 10) {
			Intent socketService = new Intent(mContext, SocketService.class);
			socketService.putExtra("user_id", user_id);
			socketService.putExtra("fieldTechId", field_tech_id);
			socketService.putExtra("orderId", order_id);
			socketService.putExtra("fieldTechName", fieldTechName);
			socketService.putExtra("type", 1);
			if (socketService != null && socketService.getAction() != null && socketService.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
				isBootCompleted = true;
			}
			Log.d(TAG, "isBootCompleted: " + isBootCompleted);
			socketService.setPackage("com.wekancode.testmybloodft.service");
			mContext.startService(socketService);
			mContext.bindService(socketService, mServiceConnection, Context.BIND_AUTO_CREATE);
		}
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
}


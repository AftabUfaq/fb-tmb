package com.orion.testmybloodft.views.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orion.testmybloodft.R;
import com.orion.testmybloodft.api.ApiModel;
import com.orion.testmybloodft.api.ApiResponseView;
import com.orion.testmybloodft.api.ApiViewPresenter;
import com.orion.testmybloodft.api.ApiViewPresenterImpl;
import com.orion.testmybloodft.api.ServerApi;
import com.orion.testmybloodft.api.ServerJsonResponseKey;
import com.orion.testmybloodft.api.ServerParams;
import com.orion.testmybloodft.models.NotificationMod;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.UIUtils;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Adapter which displays list of notifications
 */

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ApiResponseView,Handler.Callback {
    private static final String TAG = NotificationAdapter.class.getSimpleName();

    public final int TYPE_NOTIFY = 0;
    public final int TYPE_LOAD = 1;

    static Context context;
    static Activity activity;
    private List<NotificationMod> notificationMods;
    private OnLoadMoreListener loadMoreListener;
    private boolean isLoading = false, isMoreDataAvailable = true;
    //private int type;
    private String message = "";
    private RecyclerView.LayoutManager mLayoutManager;
    //private LinearLayout llParent;

    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;

    /*
    * isLoading - to set the remote loading and complete status to fix back to back load more call
    * isMoreDataAvailable - to set whether more data from server available or not.
    * It will prevent useless load more request even after all the server data loaded
    * */


    public NotificationAdapter(Activity activity, Context context, List<NotificationMod> notificationMods) {
        NotificationAdapter.context = context;
        NotificationAdapter.activity = activity;
        this.notificationMods = notificationMods;
        init();
    }

    private void init() {
        mLayoutManager = new LinearLayoutManager(context);
        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    public void  updateReadStatus(int pos){
        notificationMods.get(pos).setStatus(1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if(viewType== TYPE_NOTIFY){
            return new NotifyHolder(inflater.inflate(R.layout.notification_items,parent,false));
        }else{
            return new LoadHolder(inflater.inflate(R.layout.row_load,parent,false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position>=getItemCount()-1 && isMoreDataAvailable && !isLoading && loadMoreListener!=null){
            isLoading = true;
            loadMoreListener.onLoadMore();
        }

        if(getItemViewType(position)== TYPE_NOTIFY){
            ((NotifyHolder)holder).bindData(notificationMods.get(position));
        }
        //No else part needed as load holder doesn't bind any data
    }

    @Override
    public int getItemViewType(int position) {
        if(notificationMods.get(position).from_user_id == 0){
            return TYPE_LOAD;
        } else{
            return TYPE_NOTIFY;
        }
    }

    @Override
    public int getItemCount() {
        return notificationMods.size();
    }

    /* VIEW HOLDERS */

    class NotifyHolder extends RecyclerView.ViewHolder{
        TextView tvTimeAgo, tvContent, tvAddress;
        LinearLayout llParent;

        public NotifyHolder(View itemView) {
            super(itemView);
            llParent = itemView.findViewById(R.id.llParent);
            tvTimeAgo = itemView.findViewById(R.id.tvTimeAgo);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvAddress = itemView.findViewById(R.id.tvAddress);
        }

        void bindData(final NotificationMod notificationMod){
            int status = notificationMod.getStatus();
            if (status == 0) {
                llParent.setBackground(ContextCompat.getDrawable(context, R.drawable.unread_border_shadow));
            } else {
                llParent.setBackground(ContextCompat.getDrawable(context, R.drawable.read_border_shadow));

            }
            getTimesNow(notificationMod.getCreated_at(), tvTimeAgo);
            tvContent.setText(notificationMod.getContent());
            tvAddress.setText(""+notificationMod.getType());

            llParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                    'type' => [
                    '13' => 'Cancel',
                            '14' => 'Summary',
                            '15' => 'New Bookings' ]
                    */

                    int type = notificationMod.getType();
                    int notification_id = notificationMod.getId();
                    String created_dt = notificationMod.getCreated_at();
                    String stdate[] = created_dt.split(" ");
                    String date = stdate[0];

                    Log.d(TAG, "date: "+date);
                    Log.d(TAG, "type: "+type);
                    Log.d(TAG, "notification_id: "+notification_id);
                    clearNotifyApiCall(String.valueOf(notification_id));

                    int scheduleTabPos = -1;
                    if (type == 13) {
                        scheduleTabPos = 0;

                        Intent verifyIntent = new Intent();
                        verifyIntent.putExtra("updateTabActivityView",true);
                        verifyIntent.putExtra("notification_main_act", true);
                        verifyIntent.putExtra("type",scheduleTabPos);
                        verifyIntent.putExtra("created_at",date);
                        activity.setResult(RESULT_OK, verifyIntent);
                        activity.finish();
                    } else if (type == 14) {
                        scheduleTabPos = 1;

                        Intent verifyIntent = new Intent();
                        verifyIntent.putExtra("updateTabActivityView",true);
                        verifyIntent.putExtra("notification_main_act", true);
                        verifyIntent.putExtra("type",scheduleTabPos);
                        verifyIntent.putExtra("created_at",date);
                        activity.setResult(RESULT_OK, verifyIntent);
                        activity.finish();
                    } else if (type == 15) {
                        scheduleTabPos = 0;

                        Intent verifyIntent = new Intent();
                        verifyIntent.putExtra("updateTabActivityView",true);
                        verifyIntent.putExtra("notification_main_act", true);
                        verifyIntent.putExtra("type",scheduleTabPos);
                        verifyIntent.putExtra("created_at",date);
                        activity.setResult(RESULT_OK, verifyIntent);
                        activity.finish();
                    }
                    llParent.setBackground(ContextCompat.getDrawable(context, R.drawable.read_border_shadow));
                }
            });
        }
    }

    private void clearNotifyApiCall(String id) {
        if (!apiCallAlreadyInvoked) {
            String session = Constants.getSession(context);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_CLEAR_NOTIFICATION;
            apiModel.context = context;
            apiModel.method = 1;
            apiModel.params = ServerParams.clearNotification(session, id);
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    static class LoadHolder extends RecyclerView.ViewHolder{
        public LoadHolder(View itemView) {
            super(itemView);
        }
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        isMoreDataAvailable = moreDataAvailable;
    }

    /* notifyDataSetChanged is final method so we can't override it
         call adapter.notifyDataChanged(); after update the list
         */
    public void notifyDataChanged(){
        notifyDataSetChanged();
        isLoading = false;
    }

    public static void getTimesNow(String srcDate, TextView tvTimeAgo) {
        SimpleDateFormat desiredFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date past = desiredFormat.parse(srcDate);
            Date now = new Date();

            long seconds=TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
            long minutes= TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
            long hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
            long days=TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

            if(seconds<60)
            {
                tvTimeAgo.setText(seconds+" seconds ago");
            }
            else if(minutes<60)
            {
                tvTimeAgo.setText(minutes+" minutes ago");
            }
            else if(hours<24)
            {
                tvTimeAgo.setText(hours+" hours ago");
            }
            else
            {
                tvTimeAgo.setText(days+" days ago");
            }
        } catch (ParseException e) {
            Log.d("Exception parsing dt. ", e.getMessage());
            e.printStackTrace();
        }
    }

    public interface OnLoadMoreListener{
        void onLoadMore();
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
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
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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
}



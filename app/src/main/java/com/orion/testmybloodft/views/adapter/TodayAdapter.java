package com.orion.testmybloodft.views.adapter;

import static androidx.core.app.ActivityCompat.requestPermissions;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orion.testmybloodft.R;
import com.orion.testmybloodft.models.TodayMod;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.views.activity.DirectionActivity;
import com.orion.testmybloodft.views.activity.OrderStatus;
import com.orion.testmybloodft.views.activity.Schedule;
import com.orion.testmybloodft.views.activity.Summary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Arun on 03-Apr-17.
 */

/**
 * Adapter which displays today orders
 */

public class TodayAdapter extends RecyclerView.Adapter<TodayAdapter.MyViewHolder>{
    private static final String TAG = TodayAdapter.class.getSimpleName();
    private Activity activity;
    private Context context;
    private Fragment fragment;
    private List<TodayMod> todayMods = new ArrayList<TodayMod>();
    private int order_status;
    private final int CALL_PHONE_REQUEST = 2;
    private final int UPDATE_LIST_REQUEST_CODE = 1000;
    private LocationManager lm;
    // flag for GPS status
    private static final int REQUEST_CHECK_SETTINGS = 0;
    boolean isGPSEnabled = false;
    private PermissionCheck permissionCheck;
    private int status;
    private String field_tech_name = "";


    public interface PermissionCheck {


        void check(int position);

    }

    public BroadcastReceiver updateMode = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra("status")) {
                order_status = intent.getIntExtra("status", 0);
                Log.d(TAG, "onReceive order_status: " + order_status);
            }
        }
    };

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView startTripBtn;
        private TextView orderIdTv, pickUPTimeTv, patientNameTv, addressTv;
        private View pickupDivider;
        private LinearLayout startTripLl, callBtn, pickupLay;
        private RelativeLayout parentLl;

        public MyViewHolder(View view) {
            super(view);
            orderIdTv = view.findViewById(R.id.tvBookingID);
            pickUPTimeTv = view.findViewById(R.id.tvScheduleTime);
            patientNameTv = view.findViewById(R.id.tvPatientName);
            addressTv = view.findViewById(R.id.tvaddress);
            pickupDivider = view.findViewById(R.id.pickupDivider);
            parentLl = view.findViewById(R.id.parentLl);
            startTripLl = view.findViewById(R.id.startTripLl);
            callBtn = view.findViewById(R.id.callBtn);
            pickupLay = view.findViewById(R.id.pickupLay);
            startTripBtn = view.findViewById(R.id.startTripBtn);
        }
    }

    public TodayAdapter(Fragment fragment, Context context, Activity activity, List<TodayMod> todayMods) {
        this.fragment = fragment;
        this.context = context;
        this.activity = activity;
        this.todayMods = todayMods;
        permissionCheck = (PermissionCheck) activity;

        if (updateMode != null)
            context.registerReceiver(updateMode, new IntentFilter("updateMode"), Context.RECEIVER_NOT_EXPORTED);

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.schedule_items, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        try {
            if (position == 0) {
                Log.d(TAG, "onBindViewHolder position: " + position);
                holder.pickupDivider.setVisibility(View.VISIBLE);
                holder.startTripLl.setVisibility(View.VISIBLE);
                status = todayMods.get(position).getStatus();
                Log.d(TAG, "getOrderStatus: "+status);
                field_tech_name = todayMods.get(position).getField_tech_name();
                Constants.setFieldTechName(activity, field_tech_name);

                if (status == 10)
                    holder.startTripBtn.setText("on going trip");
                else if (status == 11)
                    holder.startTripBtn.setText("reached destination");
                else if (status == 2)
                    holder.startTripBtn.setText("sample collected");
                else if (status == 3)
                    holder.startTripBtn.setText("payment collected");
                else
                    holder.startTripBtn.setText("start trip");

                if (order_status != 0) {
                    updateTrip(holder, order_status);
                }

                holder.callBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + todayMods.get(position).getContact_number()));
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE},
                                        CALL_PHONE_REQUEST);
                            }
                            // to handle the case where the user grants the permission. See the documentation
                            return;
                        }
                        activity.startActivity(intent);
                    }
                });

                holder.startTripBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // it will handle all condition in schedule activity
                        int field_tech_id = todayMods.get(position).getField_tech_id();
                        Constants.setFieldTechId(activity, field_tech_id);
                        int order_id = todayMods.get(position).getId();
                        Constants.setOrderId(activity, order_id);

                        Intent verifyIntent = new Intent(context, DirectionActivity.class);
                        verifyIntent.putExtra("address", todayMods.get(position).getAddress());
                        verifyIntent.putExtra("contact_number", todayMods.get(position).getContact_number());
                        verifyIntent.putExtra("order_id", todayMods.get(position).getId());
                        verifyIntent.putExtra("tmb_order_id", todayMods.get(position).getTmb_order_id());
                        verifyIntent.putExtra("latitude", todayMods.get(position).getLatitude());
                        verifyIntent.putExtra("longitude", todayMods.get(position).getLongitude());
                        verifyIntent.putExtra("user_id", todayMods.get(position).getUser_id());
                        verifyIntent.putExtra("field_tech_id", todayMods.get(position).getField_tech_id());
                        verifyIntent.putExtra("fieldTechName", todayMods.get(position).getField_tech_name());
                        verifyIntent.putExtra("order_status", status);
                        verifyIntent.putExtra("from", "");

                        Log.e("TodayAdapter","From "+todayMods.get(position).getLatitude()+","+todayMods.get(position).getLongitude());
                        permissionCheck.check(position);
                        ((Schedule) activity).dataPass(verifyIntent);

                    }
                });

                holder.pickupLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (order_status != 0) {
                            if (order_status == 2 || order_status == 3 || order_status == 10 || order_status == 11) {
                                int field_tech_id = todayMods.get(position).getField_tech_id();
                                Constants.setFieldTechId(activity, field_tech_id);
                                int order_id = todayMods.get(position).getId();
                                Constants.setOrderId(activity, order_id);

                                Intent verifyIntent = new Intent(context, OrderStatus.class);
                                verifyIntent.putExtra("patient_name", todayMods.get(position).getPatient_name());
                                verifyIntent.putExtra("pickup_date", todayMods.get(position).getPickup_date());
                                verifyIntent.putExtra("pickup_time", todayMods.get(position).getPickup_time());
                                verifyIntent.putExtra("address", todayMods.get(position).getAddress());
                                verifyIntent.putExtra("contact_number", todayMods.get(position).getContact_number());
                                verifyIntent.putExtra("order_id", todayMods.get(position).getId());
                                verifyIntent.putExtra("tmb_order_id", todayMods.get(position).getTmb_order_id());
                                verifyIntent.putExtra("latitude", todayMods.get(position).getLatitude());
                                verifyIntent.putExtra("longitude", todayMods.get(position).getLongitude());
                                verifyIntent.putExtra("user_id", todayMods.get(position).getUser_id());
                                verifyIntent.putExtra("field_tech_id", todayMods.get(position).getField_tech_id());
                                verifyIntent.putExtra("order_status", order_status);
                                verifyIntent.putExtra("from_tag", TAG);
                                verifyIntent.putExtra("position", position);
                                activity.startActivityForResult(verifyIntent, UPDATE_LIST_REQUEST_CODE);
                            } else {
                                Toast.makeText(context, Constants.ACCESS_SHOW_MY_ROUTE, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (status == 2 || status == 3 || status == 10 || status == 11) {
                                int field_tech_id = todayMods.get(position).getField_tech_id();
                                Constants.setFieldTechId(activity, field_tech_id);
                                int order_id = todayMods.get(position).getId();
                                Constants.setOrderId(activity, order_id);

                                Intent verifyIntent = new Intent(context, OrderStatus.class);
                                verifyIntent.putExtra("patient_name", todayMods.get(position).getPatient_name());
                                verifyIntent.putExtra("pickup_date", todayMods.get(position).getPickup_date());
                                verifyIntent.putExtra("pickup_time", todayMods.get(position).getPickup_time());
                                verifyIntent.putExtra("address", todayMods.get(position).getAddress());
                                verifyIntent.putExtra("contact_number", todayMods.get(position).getContact_number());
                                verifyIntent.putExtra("order_id", todayMods.get(position).getId());
                                verifyIntent.putExtra("tmb_order_id", todayMods.get(position).getTmb_order_id());
                                verifyIntent.putExtra("latitude", todayMods.get(position).getLatitude());
                                verifyIntent.putExtra("longitude", todayMods.get(position).getLongitude());
                                verifyIntent.putExtra("user_id", todayMods.get(position).getUser_id());
                                verifyIntent.putExtra("field_tech_id", todayMods.get(position).getField_tech_id());
                                verifyIntent.putExtra("order_status", status);
                                verifyIntent.putExtra("from_tag", TAG);
                                verifyIntent.putExtra("position", position);
                                activity.startActivityForResult(verifyIntent, UPDATE_LIST_REQUEST_CODE);
                            } else {
                                Toast.makeText(context, Constants.ACCESS_SHOW_MY_ROUTE, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }

            try {
                String pickUpTime = todayMods.get(position).getPickup_time();
                String[] time_split = pickUpTime.split(":");
                String _24HourTime = time_split[0] + ":" + time_split[1];
                SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
                SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
                Date _24HourDt = _24HourSDF.parse(_24HourTime);
                Log.d(TAG, "_12HourSDF: " + _12HourSDF.format(_24HourDt));
                holder.pickUPTimeTv.setText(_12HourSDF.format(_24HourDt));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (todayMods != null) {
                holder.orderIdTv.setText("#" + todayMods.get(position).getTmb_order_id());
                holder.patientNameTv.setText(todayMods.get(position).getPatient_name());
                holder.addressTv.setText(todayMods.get(position).getAddress());
            }

            holder.parentLl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position > 0) {
                        activity.startActivity(new Intent(context, Summary.class).putExtra("tmb_order_id", todayMods.get(position).getTmb_order_id()));
                    }
                }
            });
        } catch (
                Exception e)

        {
            e.printStackTrace();
        }

    }

    public void updateTrip(MyViewHolder holder, int order_status) {
        if (order_status == 10)
            holder.startTripBtn.setText("on going trip");
        else if (order_status == 11)
            holder.startTripBtn.setText("reached destination");
        else if (order_status == 2)
            holder.startTripBtn.setText("sample collected");
        else
            holder.startTripBtn.setText("start trip");

    }

    public void removeAt(int index) {
        todayMods.remove(index);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, todayMods.size());

        Intent intent = new Intent("updateMode");
        intent.putExtra("status", 0);
        activity.sendBroadcast(intent);
    }

    @Override
    public int getItemCount() {
        return todayMods.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onViewRecycled(MyViewHolder holder) {
        super.onViewRecycled(holder);
    }

}


package com.orion.testmybloodft.views.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.orion.testmybloodft.R;
import com.orion.testmybloodft.models.TodayMod;
import com.orion.testmybloodft.views.activity.Summary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Arun on 03-Apr-17.
 */

/**
 * Adapter which displays tomorrow orders
 */

public class TomorrowAdapter extends RecyclerView.Adapter<TomorrowAdapter.MyViewHolder> {
    private static final String TAG = TomorrowAdapter.class.getSimpleName();
    private Activity activity;
    private Context context;
    private List<TodayMod> todayMods = new ArrayList<TodayMod>();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView orderIdTv, pickUPTimeTv, patientNameTv, addressTv;
        private RelativeLayout parentLl;

        public MyViewHolder(View view) {
            super(view);
            orderIdTv = view.findViewById(R.id.tvBookingID);
            pickUPTimeTv = view.findViewById(R.id.tvScheduleTime);
            patientNameTv = view.findViewById(R.id.tvPatientName);
            addressTv = view.findViewById(R.id.tvaddress);
            parentLl = view.findViewById(R.id.parentLl);
        }
    }

    public TomorrowAdapter(Context context, Activity activity, List<TodayMod> todayMods) {
        this.context = context;
        this.activity = activity;
        this.todayMods = todayMods;
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
            String pickUpTime = todayMods.get(position).getPickup_time();
            String[] time_split = pickUpTime.split(":");
            String _24HourTime = time_split[0] + ":" + time_split[1];
            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
            SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
            Date _24HourDt = _24HourSDF.parse(_24HourTime);
            Log.d(TAG, "_12HourSDF: "+_12HourSDF.format(_24HourDt));
            holder.pickUPTimeTv.setText(_12HourSDF.format(_24HourDt));

            holder.orderIdTv.setText("#"+todayMods.get(position).getTmb_order_id());
            holder.patientNameTv.setText(todayMods.get(position).getPatient_name());
            holder.addressTv.setText(todayMods.get(position).getAddress());

            holder.parentLl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.startActivity(new Intent(context, Summary.class).putExtra("tmb_order_id",todayMods.get(position).getTmb_order_id()));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    @Override
    public int getItemCount() {
        return todayMods.size();
    }

    @Override public int getItemViewType(int position) { return position; }

}


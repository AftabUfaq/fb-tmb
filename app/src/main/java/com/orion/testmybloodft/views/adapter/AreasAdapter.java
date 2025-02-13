package com.orion.testmybloodft.views.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.orion.testmybloodft.R;
import com.orion.testmybloodft.models.AreasMod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arun on 03-Apr-17.
 */

/**
 * Adapter which displays ft covered areas
 */

public class AreasAdapter extends RecyclerView.Adapter<AreasAdapter.MyViewHolder> {
    private static final String TAG = AreasAdapter.class.getSimpleName();
    private Activity activity;
    private Context context;
    private List<AreasMod> areasMods = new ArrayList<AreasMod>();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView areaTv;

        public MyViewHolder(View view) {
            super(view);
            areaTv = view.findViewById(R.id.tvArea);
        }
    }


    public AreasAdapter(Context context, List<AreasMod> areasMods) {
        this.context = context;
        this.areasMods = areasMods;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.areas_covered_items, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.areaTv.setText(areasMods.get(position).getArea()+" - "+areasMods.get(position).getPincode());
    }

    @Override
    public int getItemCount() {
        return areasMods.size();
    }

    @Override public int getItemViewType(int position) { return position; }


}


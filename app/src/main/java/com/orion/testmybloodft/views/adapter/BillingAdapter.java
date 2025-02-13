package com.orion.testmybloodft.views.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.orion.testmybloodft.R;
import com.orion.testmybloodft.models.TestListMod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arun on 03-Apr-17.
 */

/**
 * Adapter which displays patient blood test name and cost of test
 */

public class BillingAdapter extends RecyclerView.Adapter<BillingAdapter.MyViewHolder> {
    private static final String TAG = BillingAdapter.class.getSimpleName();
    private Activity activity;
    private Context context;
    private List<TestListMod> testListMods = new ArrayList<TestListMod>();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTestName, tvPrice;

        public MyViewHolder(View view) {
            super(view);
            tvTestName = view.findViewById(R.id.tvTestName);
            tvPrice = view.findViewById(R.id.tvPrice);
        }
    }

    public BillingAdapter(Context context, List<TestListMod> areasMods) {
        this.context = context;
        this.testListMods = areasMods;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.billing_items, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.tvTestName.setText(testListMods.get(position).getTest());
        holder.tvPrice.setText(testListMods.get(position).getCost());
    }

    @Override
    public int getItemCount() {
        return testListMods.size();
    }

    @Override public int getItemViewType(int position) { return position; }


}


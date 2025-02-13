package com.orion.testmybloodft.views.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.orion.testmybloodft.R;
import com.orion.testmybloodft.models.TodayMod;

import java.util.List;

/**
 * Created by Arun on 03-Apr-17.
 */

/**
 * isLoading - to set the remote loading and complete status to fix back to back load more call
 * isMoreDataAvailable - to set whether more data from server available or not.
 * It will prevent useless load more request even after all the server data loaded
 */

public class LoadMoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final int TYPE_SCHEDULE = 0;
    public final int TYPE_LOAD = 1;

    static Context context;
    List<TodayMod> todayMods;
    OnLoadMoreListener loadMoreListener;
    boolean isLoading = false, isMoreDataAvailable = true;

    /*
    * isLoading - to set the remote loading and complete status to fix back to back load more call
    * isMoreDataAvailable - to set whether more data from server available or not.
    * It will prevent useless load more request even after all the server data loaded
    * */


    public LoadMoreAdapter(Context context, List<TodayMod> todayMods) {
        LoadMoreAdapter.context = context;
        this.todayMods = todayMods;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if(viewType== TYPE_SCHEDULE){
            return new MovieHolder(inflater.inflate(R.layout.schedule_items,parent,false));
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

        if(getItemViewType(position)== TYPE_SCHEDULE){
            ((MovieHolder)holder).bindData(todayMods.get(position));
        }
        //No else part needed as load holder doesn't bind any data
    }

    @Override
    public int getItemViewType(int position) {
        if(todayMods.get(position).field_tech_id == 0){
            return TYPE_LOAD;
        } else{
            return TYPE_SCHEDULE;
        }
    }

    @Override
    public int getItemCount() {
        return todayMods.size();
    }

    /* VIEW HOLDERS */

    static class MovieHolder extends RecyclerView.ViewHolder{
        TextView orderIdTv, patientNameTv, addressTv;
        public MovieHolder(View itemView) {
            super(itemView);
            orderIdTv= itemView.findViewById(R.id.tvBookingID);
            patientNameTv = itemView.findViewById(R.id.tvPatientName);
            addressTv = itemView.findViewById(R.id.tvaddress);
        }

        void bindData(TodayMod movieModel){
            orderIdTv.setText("#"+movieModel.getTmb_order_id());
            patientNameTv.setText(movieModel.getPatient_name());
            addressTv.setText(movieModel.getAddress());
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


    public interface OnLoadMoreListener{
        void onLoadMore();
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }
}



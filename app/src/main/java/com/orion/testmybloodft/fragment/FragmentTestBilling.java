package com.orion.testmybloodft.fragment;

/**
 * Created by Arun on 7/7/2017.
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.orion.testmybloodft.R;
import com.orion.testmybloodft.config.App;
import com.orion.testmybloodft.databinding.FragmentBilllingBinding;
import com.orion.testmybloodft.models.TestListMod;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.views.adapter.BillingAdapter;

import java.util.List;


/**
 * Showing user test & billing info
 */

@SuppressLint("ValidFragment")
public class FragmentTestBilling extends Fragment {
    private static final String TAG = FragmentTestBilling.class.getSimpleName();
    private final int CALL_PHONE_REQUEST = 2;
    private String contact_number, collection_charge = "";
    private BillingAdapter billingAdapter;
    private List<TestListMod> testList;
    private Context mContext;
    public FragmentTestBilling(Context mContext) {
        this.mContext = mContext;
    }
    public FragmentTestBilling() {
        super();
    }
    FragmentBilllingBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentBilllingBinding.inflate(inflater, container, false);


        updateUI();

        return binding.getRoot();
    }

    private void updateUI() {
        // Get Test list
        testList = ((App) getActivity().getApplication()).getTestList();
        billingAdapter = new BillingAdapter(getContext(), testList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.billingRecyclerView.setLayoutManager(layoutManager);
        binding.billingRecyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.billingRecyclerView.smoothScrollToPosition(0);
        binding.billingRecyclerView.setHasFixedSize(true);
        binding.billingRecyclerView.setAdapter(billingAdapter);

        String billingStr = Constants.getBillingDetails(getContext());
        String[] value_split = billingStr.split("\\|");
        binding.tvLabName.setText(value_split[0]);
        binding.tvPaymentMode.setText(value_split[1]);
        String discount = value_split[2];
        if (!discount.equals("0.00")) {
            binding.tvVoucher.setText(discount);
        } else {
            binding.llDiscount.setVisibility(View.GONE);
            binding.vDisVoucher.setVisibility(View.GONE);
        }
        binding.tvTotalAmt.setText(value_split[3]);
        contact_number = value_split[4];
        collection_charge = value_split[6];
        binding.tvCharge.setText(collection_charge);
        Log.d(TAG, "labLogo: "+value_split[5]);
        boolean mLogoCheck = value_split[5].contains("null");
        Log.d(TAG, "logoCheck: "+mLogoCheck);
        if (mLogoCheck) {
            binding.ivLabLogo.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(App.getInstance().getBaseContext())
                    .asBitmap()
                    .load(value_split[5])

                    .thumbnail(Glide.with(App.getInstance().getBaseContext())
                            .asBitmap()
                            .load(value_split[5])

                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .thumbnail(0.8f)
                    )
                    .into(binding.ivLabLogo);
        }

        binding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact_number));
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                                CALL_PHONE_REQUEST);
                    }
                    return;
                }
                startActivity(intent);

            }
        });

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PHONE_REQUEST) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permissions Granted... ");
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
            } else {
                Log.d(TAG, "permissions Denied... ");
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(getContext(), "Permission Denied to call Telephone", Toast.LENGTH_SHORT).show();
            }

        }
    }

}


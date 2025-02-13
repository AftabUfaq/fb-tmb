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
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.orion.testmybloodft.R;
import com.orion.testmybloodft.databinding.FragmentProfileBinding;
import com.orion.testmybloodft.utils.Constants;


/**
 * Showing user profile info
 */

@SuppressLint("ValidFragment")
public class FragmentProfile extends Fragment {
    private static final String TAG = FragmentProfile.class.getSimpleName();
    private final int CALL_PHONE_REQUEST = 2;



//    @BindView(R.id.tvCustomerName)
//    TextView tvCustomerName;
//    @BindView(R.id.tvGender)
//    TextView tvGender;
//    @BindView(R.id.tvAge)
//    TextView tvAge;
//    @BindView(R.id.tvOrderId)
//    TextView tvOrderId;
//    @BindView(R.id.tvPickupDtTime)
//    TextView tvPickupDtTime;
//    @BindView(R.id.tvPickAddr)
//    TextView tvPickAddr;
//    @BindView(R.id.tvPhone)
//    TextView tvPhone;
    private Context mContext;

    public FragmentProfile(Context mContext) {
        this.mContext = mContext;
    }

    public FragmentProfile() {
        super();
    }

    FragmentProfileBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        updateUI();

        return binding.getRoot();
    }
String phoneNo="";
    private void updateUI() {
        String profileStr = Constants.getProfileDetails(getContext());
        String[] value_split = profileStr.split("\\|");
        binding.tvCustomerName.setText(value_split[0]);
        if (value_split[1].equals("m")) {
            binding.tvGender.setText("Male");
        } else if (value_split[1].equals("f")) {
            binding.tvGender.setText("Female");
        }
        binding.tvAge.setText(value_split[2]);
        binding.tvOrderId.setText(value_split[3]);
        binding.tvPickupDtTime.setText(value_split[4]);
        binding.tvPickAddr.setText(value_split[5]);
        binding.tvPhone.setText(value_split[6]);
        phoneNo=value_split[6];

        binding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNo));
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                                CALL_PHONE_REQUEST);
                    }
                    // to handle the case where the user grants the permission. See the documentation
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


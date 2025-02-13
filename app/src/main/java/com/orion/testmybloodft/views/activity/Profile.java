package com.orion.testmybloodft.views.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.orion.testmybloodft.R;
import com.orion.testmybloodft.api.ApiModel;
import com.orion.testmybloodft.api.ApiResponseView;
import com.orion.testmybloodft.api.ApiViewPresenter;
import com.orion.testmybloodft.api.ApiViewPresenterImpl;
import com.orion.testmybloodft.api.ServerApi;
import com.orion.testmybloodft.api.ServerJsonResponseKey;
import com.orion.testmybloodft.api.ServerParams;
import com.orion.testmybloodft.config.App;
import com.orion.testmybloodft.config.MyVolley;
import com.orion.testmybloodft.databinding.ActivityProfileBinding;
import com.orion.testmybloodft.models.AreasMod;
import com.orion.testmybloodft.utils.Constants;
import com.orion.testmybloodft.utils.MarshMallowPermission;
import com.orion.testmybloodft.utils.UIUtils;
import com.orion.testmybloodft.views.adapter.AreasAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;





public class Profile extends AppCompatActivity implements ApiResponseView, Handler.Callback {
    private static final String TAG = Profile.class.getSimpleName();
    private static final String IMAGE_DIRECTORY_NAME = "TMB_Android";

    /*Api Call Interface */
    ApiViewPresenter apiViewPresenter;
    private boolean apiCallInvoked = false;
    private boolean apiCallAlreadyInvoked = false;
    private Handler apiCallHandler;
    private Activity mActivity;
    private Context mContext;

    private Dialog m_Dialog;
    private ProgressBar m_ProgressBar;
    private Uri fileUri, pickedUri;
    private String firstName, lastNsme, email, contNumber, bucketUrl, profile_pic, message, mCurrentPhotoPath = "";
    private int ftStatus;

//    @BindView(R.id.ivProfile)
//    ImageView profilePhoto;
//    @BindView(R.id.backBtn)
//    ImageView backBtn;
//    @BindView(R.id.ftName)
//    TextView ftName;
//    @BindView(R.id.ftEmail)
//    TextView ftEmail;
//    @BindView(R.id.tvMobile)
//    TextView tvMobile;
//    @BindView(R.id.tvStatus)
//    TextView tvStatus;
//    @BindView(R.id.areasRecycler)
//    RecyclerView areasRecycler;

    private AreasAdapter areasAdapter;
    private List<AreasMod> areasMods = new ArrayList<AreasMod>();

    ActivityProfileBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mActivity = this;
        mContext = this;

//        ButterKnife.bind(this);
        /*ButterKnife.bind(this);

        m_Dialog = new Dialog(SignIn.this, R.style.Theme_Transparent);
        m_Dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_Dialog.setContentView(R.layout.progress_loader);
        m_ProgressBar = (ProgressBar) m_Dialog.findViewById(R.id.progressBar);
        m_ProgressBar.setIndeterminate(true);
        //noinspection deprecation,deprecation
        m_ProgressBar.setIndeterminateDrawable(SignIn.this.getResources().getDrawable(R.drawable.loader_anim));
        m_Dialog.setCancelable(false);
        m_Dialog.setCanceledOnTouchOutside(false);*/

        handleClicks();
        init();

        if (Constants.isNetworkAvailable()) {
            apiUserDetailsCall();
        }
        else
        {
            Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
        }
    }


    void handleClicks(){
        binding.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void selectImage() {

        if (Build.VERSION.SDK_INT >= 23) {

            final MarshMallowPermission marshMallowPermission = new MarshMallowPermission(this);

            if (!marshMallowPermission.checkPermissionForCamera()) {

                marshMallowPermission.requestPermissionForCamera();

            } else {

                if (!marshMallowPermission.checkPermissionForExternalStorage()) {

                    marshMallowPermission.requestPermissionForExternalStorage();

                } else {

                    showDialogue();
                }
            }

        } else {
            showDialogue();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        /*File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName, *//* prefix *//*
                ".jpg", *//* suffix *//*
                storageDir *//* directory *//*
        );*/

        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = new File(path, imageFileName);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void showDialogue() {
        final Dialog dialog = new Dialog(Profile.this);
        dialog.setContentView(R.layout.custom_dialog_photo_upload);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        TextView cameraTV = dialog.findViewById(R.id.cameraTV);
        TextView galleryTV = dialog.findViewById(R.id.galleryTV);
        TextView cancelTV = dialog.findViewById(R.id.cancelTV);

        cameraTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = null;
                try {
                    fileUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", createImageFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, 1);
                dialog.dismiss();
            }
        });

        galleryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2);

                dialog.dismiss();
            }
        });

        cancelTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            if (requestCode == 1) {
                try {
                    binding.ivProfile.setImageURI(Uri.parse(compressImage(mCurrentPhotoPath)));
                    if (Constants.isNetworkAvailable()) {
                        apiImageUploadCall();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                    }
                    if (new File(mCurrentPhotoPath).exists()) {
                        new File(mCurrentPhotoPath).delete();
                    }


                } catch (NullPointerException e) {
                    e.printStackTrace();
                }


            } else if (requestCode == 2) {
                pickedUri = data.getData();

                Bitmap pic;
                String imgPath;

                //retrieve the string using media data
                String[] medData = {MediaStore.Images.Media.DATA};
                //query the data
                Cursor picCursor = managedQuery(pickedUri, medData, null, null, null);
                if (picCursor != null) {
                    //get the path string
                    int index = picCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    picCursor.moveToFirst();
                    imgPath = picCursor.getString(index);
                } else
                    imgPath = pickedUri.getPath();

                //if we have a new URI attempt to decode the image bitmap
                if (pickedUri != null) {

                    //set the width and height we want to use as maximum display
                    int targetWidth = 600;
                    int targetHeight = 400;
                    //create bitmap options to calculate and use sample size
                    BitmapFactory.Options bmpOptions = new BitmapFactory.Options();

                    //first decode image dimensions only - not the image bitmap itself
                    bmpOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(imgPath, bmpOptions);

                    //image width and height before sampling
                    int currHeight = bmpOptions.outHeight;
                    int currWidth = bmpOptions.outWidth;

                    //variable to store new sample size
                    int sampleSize = 1;

                    //calculate the sample size if the existing size is larger than target size
                    if (currHeight > targetHeight || currWidth > targetWidth) {
                        //use either width or height
                        if (currWidth > currHeight)
                            sampleSize = Math.round((float) currHeight / (float) targetHeight);
                        else
                            sampleSize = Math.round((float) currWidth / (float) targetWidth);
                    }

                    //use the new sample size
                    bmpOptions.inSampleSize = sampleSize;

                    //now decode the bitmap using sample options
                    bmpOptions.inJustDecodeBounds = false;

                    //get the file as a bitmap
                    pic = BitmapFactory.decodeFile(imgPath, bmpOptions);
                    binding.ivProfile.setImageBitmap(pic);
                    if (Constants.isNetworkAvailable()) {
                        apiImageUploadCall();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), Constants.NO_INTERNET_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        //  by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        //  you try the use the bitmap here, you will get null.

        //String filePath = imageUri.getPath();

        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        //  max Height and width values of the compressed image is taken as 816x612

        if (actualWidth == 0) actualWidth = 1;
        if (actualHeight == 0) actualHeight = 1;

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        //  width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        // setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

        //  inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

        //  this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            //   load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        assert scaledBitmap != null;
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        //  check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

            //   write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    private String getFilename() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath(), IMAGE_DIRECTORY_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");

    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private void init() {

        apiViewPresenter = new ApiViewPresenterImpl(this);
        apiCallHandler = new Handler(this);
    }

    private void apiUserDetailsCall() {
        if (!apiCallAlreadyInvoked) {
            String session = Constants.getSession(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            apiModel.serviceType = ServerApi.CODE_USER_DETAIS;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            apiModel.params = ServerParams.imageUpload(session);
            apiCallAlreadyInvoked = true;
            apiCallInvoked = true;
            apiViewPresenter.validateToHitRequest(apiModel);
        }
    }

    private void apiImageUploadCall() {
        if (!apiCallAlreadyInvoked) {
            String session = Constants.getSession(mContext);
            ApiModel apiModel = new ApiModel();
            apiModel.FROM_TAG = TAG;
            // apiModel.TOKEN_HEADER = token;
            apiModel.serviceType = ServerApi.CODE_IMAGE_UPLOAD;
            apiModel.context = getApplicationContext();
            apiModel.method = 1;
            if (binding.ivProfile.getDrawable() !=null) {
                apiModel.DATA_OR_FILE_Type = 1;
                apiModel.drawable = binding.ivProfile.getDrawable();
            } else {
                Log.d(TAG, "apiCallUserUpdate: "+"No Profile Photo");
            }
            apiModel.params = ServerParams.imageUpload(session);

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
            case ServerApi.CODE_USER_DETAIS:
            case ServerApi.CODE_IMAGE_UPLOAD: {
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
                    if (success == 1) {
                        if (ServerApi.CODE_IMAGE_UPLOAD == serviceType) {
                            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                            successJsonResponse(jsonObject);
                        } else if (ServerApi.CODE_USER_DETAIS == serviceType) {
                            successUserDetailsJsonResponse(jsonObject);
                        }
                    } else {
                        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseResponseData Exception ", e);
        }
    }

    private void successJsonResponse(JSONObject jsonObject) {
        try {
            Log.d(TAG, "successJsonResponse: "+jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void successUserDetailsJsonResponse(JSONObject jsonObject) {
        try {
            JSONObject jsonObj = jsonObject.optJSONObject(ServerJsonResponseKey.DATA);
            Log.d(TAG, "userDetails: "+jsonObj.toString());
            firstName = jsonObj.optString("first_name");
            lastNsme = jsonObj.optString("last_name");
            email = jsonObj.optString("email");
            contNumber = jsonObj.optString("contact_number");
            bucketUrl = jsonObj.optString("bucketUrl");
            profile_pic = jsonObj.optString("profile_pic");
            Log.d(TAG, "profile_pic: "+profile_pic);
            ftStatus = jsonObj.optInt("status");
            JSONArray jsonArrayObj = jsonObj.optJSONArray(ServerJsonResponseKey.AREA_COVERED);
            Log.i(TAG, " jsonArrayObj ::: " + jsonArrayObj.toString());
            if (!jsonArrayObj.equals("[]")) {
                if (null != jsonArrayObj) {
                    if (jsonArrayObj.length() > 0) {
                        for(int i=0;i < jsonArrayObj.length();i++) {
                            JSONObject areaObj = jsonArrayObj.optJSONObject(i);
                            Gson gson = new Gson();
                            AreasMod areasMod = gson.fromJson(areaObj.toString(),AreasMod.class);
                            areasMods.add(areasMod);
                        }
                    }
                }
            }
            updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        String profilePic = bucketUrl + profile_pic;
        Log.i(TAG, "profilePic: "+profilePic);
        Log.i(TAG, "profile_pic: "+profile_pic);
        binding.ftName.setText(firstName+" "+lastNsme);
       binding.ftEmail.setText(email);
       binding.tvMobile.setText(contNumber);
        if (ftStatus == 1) {
            binding.tvStatus.setText("Active");
        } else {
            binding.tvStatus.setText("InActive");
        }
        if (!profile_pic.equals("null")) {
            Log.i(TAG, "profile hav pic: ");
            Glide.with(App.getInstance().getBaseContext())
                    .asBitmap()
                    .load(profilePic)
                    .thumbnail(Glide.with(App.getInstance().getBaseContext())
                            .asBitmap()
                            .load(profilePic)

                            .placeholder(R.mipmap.profile)
                            .error(R.mipmap.profile)
                            .thumbnail(0.8f)
                    )
                    .into(binding.ivProfile);
        } else {
            Log.i(TAG, "profile dont have pic: ");
            binding.ivProfile.setImageResource(R.mipmap.profile);
        }

        // Areas covered
        areasAdapter = new AreasAdapter(mContext, areasMods);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.areasRecycler.setLayoutManager(layoutManager);
        binding.areasRecycler.setItemAnimator(new DefaultItemAnimator());
        binding.areasRecycler.smoothScrollToPosition(0);
        binding.areasRecycler.setHasFixedSize(true);
        binding.areasRecycler.setAdapter(areasAdapter);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != apiCallHandler) {
            apiCallHandler.removeCallbacksAndMessages(null);
            apiCallHandler = null;
        }
        apiViewPresenter.onDestroy();

        if(apiCallInvoked){
            MyVolley.getInstance(getApplicationContext()).getRequestQueue().cancelAll(TAG);
        }

    }

    /*@Override
    public void onBackPressed() {
        startActivity(new Intent(mContext, Schedule.class));
        finish();
    }*/
}


package com.orion.testmybloodft.views.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.orion.testmybloodft.R;
import com.orion.testmybloodft.databinding.ActivityQrCodeBinding;
import com.orion.testmybloodft.utils.Constants;

import java.util.Map;

public class QrCodeActivity extends AppCompatActivity {


    ActivityQrCodeBinding binding;

    private static final int SELECTED_PIC = 1;
    String barcodetype;
    BinaryBitmap binaryBitmap;
    FrameLayout frameLayout;
    Bitmap mBitmap;
    public CodeScanner mCodeScanner;
    MultiFormatReader mMultiFormatReader;
    private CodeScannerView scannerView;
    private ImageView scanrimage;
    String text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        this.scannerView = (CodeScannerView) findViewById(R.id.scanner_view);
        this.scanrimage = (ImageView) findViewById(R.id.media);
        this.mCodeScanner = new CodeScanner(this, this.scannerView);
        this.scanrimage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
              startActivityForResult(new Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 1);
            }
        });
        new Camera.PreviewCallback() {
            public void onPreviewFrame(byte[] bArr, Camera camera) {
                Result result = null;
                try {
                    result = mMultiFormatReader.decode(new BinaryBitmap(new HybridBinarizer(new PlanarYUVLuminanceSource(bArr, ItemTouchHelper.Callback.DEFAULT_SWIPE_ANIMATION_DURATION, ItemTouchHelper.Callback.DEFAULT_SWIPE_ANIMATION_DURATION, ItemTouchHelper.Callback.DEFAULT_SWIPE_ANIMATION_DURATION, ItemTouchHelper.Callback.DEFAULT_SWIPE_ANIMATION_DURATION, ItemTouchHelper.Callback.DEFAULT_SWIPE_ANIMATION_DURATION, ItemTouchHelper.Callback.DEFAULT_SWIPE_ANIMATION_DURATION, false))), (Map<DecodeHintType, ?>) null);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
                if (result != null) {
                    result.getText();
                }
            }
        };
        this.mCodeScanner.setDecodeCallback(new DecodeCallback() {
            public void onDecoded(final Result result) {
               runOnUiThread(new Runnable() {
                    @SuppressLint("WrongConstant")
                    public void run() {
                        Toast.makeText(QrCodeActivity.this, result.getText(), 0).show();
                        Intent intent = new Intent(QrCodeActivity.this, ShowDataActivity.class);
                        intent.putExtra("qrData", result.getText());
                        startActivity(intent);
                        mCodeScanner.releaseResources();
                        mCodeScanner.startPreview();
                        RingtoneManager.getRingtone(QrCodeActivity.this, RingtoneManager.getDefaultUri(2)).play();
                        if (result.getBarcodeFormat() == BarcodeFormat.QR_CODE) {
                            barcodetype = "QR_CODE";
                        } else {
                            barcodetype = "BarCode";
                        }
                        generateQRCode(result.getText(), result.getBarcodeFormat());
                    }
                });
            }
        });
        if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.CAMERA"}, 50);
        }

    }

    public void generateQRCode(String str, BarcodeFormat barcodeFormat) {
        try {
            BitMatrix encode = new MultiFormatWriter().encode(str, barcodeFormat, 350, 350);
            this.mBitmap = Bitmap.createBitmap(350, 350, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < 350; i++) {
                for (int i2 = 0; i2 < 350; i2++) {
                    this.mBitmap.setPixel(i, i2, encode.get(i, i2) ? ViewCompat.MEASURED_STATE_MASK : -1);
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = this.mBitmap;
        if (bitmap != null) {
            Constants.bitmap = bitmap;
//            Intent intent = new Intent(this, ImageScane.class);
//            intent.putExtra("loadsPosition1", str);
//            intent.putExtra("BarcodeType", new Gson().toJson((Object) barcodeFormat));
//            intent.putExtra("ScaneType", this.barcodetype);
//            startActivity(intent);
        }
    }

    private void selectImage() {
        final CharSequence[] charSequenceArr = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle((CharSequence) "Choose your profile picture");
        builder.setItems(charSequenceArr, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (charSequenceArr[i].equals("Take Photo")) {
                    startActivityForResult(new Intent("android.media.action.IMAGE_CAPTURE"), 0);
                } else if (charSequenceArr[i].equals("Choose from Gallery")) {
                    startActivityForResult(new Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 1);
                } else if (charSequenceArr[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });

    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 50 && iArr.length > 0 && iArr[0] == 0) {
            this.mCodeScanner.startPreview();
        }
    }

    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == 0) {
            this.mCodeScanner.startPreview();
        }
    }

    public void onPause() {
        this.mCodeScanner.releaseResources();
        super.onPause();
    }

    @SuppressLint("WrongConstant")
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 1 && i2 == -1) {
            String[] strArr = {"_data"};
            Cursor query = getContentResolver().query(intent.getData(), strArr, (String) null, (String[]) null, (String) null);
            query.moveToFirst();
            @SuppressLint("Range") String string = query.getString(query.getColumnIndex(strArr[0]));
            query.close();
            Bitmap decodeFile = BitmapFactory.decodeFile(string);
            if (decodeFile != null) {
                int width = decodeFile.getWidth();
                int height = decodeFile.getHeight();
                int[] iArr = new int[(width * height)];
                decodeFile.getPixels(iArr, 0, width, 0, 0, width, height);
                this.binaryBitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width, height, iArr)));
                Result result = null;
                try {
                    result = new MultiFormatReader().decode(this.binaryBitmap);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
                if (result != null) {
                    this.text = result.getText();
                    Constants.bitmap = decodeFile;
//                    Intent intent2 = new Intent(this, ImageScane.class);
//                    intent2.putExtra("loadsPosition1", this.text);
//                    startActivity(intent2);
                    return;
                }
                Toast.makeText(this, "Your Image is not clear...", 0).show();
                return;
            }
            Toast.makeText(this, "Your Image is not clear...", 0).show();
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }
}
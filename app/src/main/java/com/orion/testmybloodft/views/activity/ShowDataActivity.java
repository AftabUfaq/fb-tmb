package com.orion.testmybloodft.views.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.orion.testmybloodft.R;
import com.orion.testmybloodft.databinding.ActivityShowDataBinding;

public class ShowDataActivity extends AppCompatActivity {

    ActivityShowDataBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityShowDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String qrData = getIntent().getStringExtra("qrData");
        String[] qrDataParts = qrData.split(",");

        if (qrDataParts.length == 6) {
            binding.textViewDate.setText(qrDataParts[0]);
            binding.textViewName.setText(qrDataParts[1]);
            binding.textViewAge.setText(qrDataParts[2]);
            binding.textViewGender.setText(qrDataParts[3]);
            binding.textViewTests.setText(qrDataParts[4]);
            binding.textViewLabName.setText(qrDataParts[5]);
        } else {
            Toast.makeText(this, "Invalid QR code data format", Toast.LENGTH_SHORT).show();
        }

        Bitmap bitmap = generateQRCode(qrData);
        if (bitmap != null) {
            binding.imageViewQRCode.setImageBitmap(bitmap);
        }



    }


    private Bitmap generateQRCode(String qrData) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    qrData,
                    BarcodeFormat.QR_CODE,
                    500, // Width and height of QR code
                    500
            );
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
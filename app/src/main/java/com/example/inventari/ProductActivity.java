package com.example.inventari;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ProductActivity extends AppCompatActivity {

    private static final String TAG = "desenvolupament";
    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        dispatchTakePictureIntent();

        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Check if mCurrentPhotoPath exists and delete if exists
                dispatchTakePictureIntent();
            }
        });

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO delete mCurrentPhotoPath file and save code an quantity in a file
                Intent intent = new Intent(ProductActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            barcodeRecognitionFromImage();
        }
    }

    String mCurrentPhotoPath;
    private File createImageFile() throws IOException {
        Log.v(TAG, "Entrm a createImageFile");
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Log.v(TAG, "Entrm a dispatchTakePictureIntent");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void barcodeRecognitionFromImage() {
        Log.v(TAG, "Entrm a barcodeRecognitionFromImage");
        Log.v(TAG, "El mCurrentPhotoPath es: " + mCurrentPhotoPath);
        File photoFile = new File(mCurrentPhotoPath);
        Uri photoURI = FileProvider.getUriForFile(this,
                "com.example.android.fileprovider",
                photoFile);
        try {
            FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(ProductActivity.this, photoURI);
            barcodeRecognition(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void barcodeRecognition(FirebaseVisionImage image) {
        Log.v(TAG, "Entrm a barcodeRecognition");
        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_CODE_128,
                                FirebaseVisionBarcode.FORMAT_CODE_39,
                                FirebaseVisionBarcode.FORMAT_CODABAR,
                                FirebaseVisionBarcode.FORMAT_EAN_13,
                                FirebaseVisionBarcode.FORMAT_EAN_8,
                                FirebaseVisionBarcode.FORMAT_UPC_A,
                                FirebaseVisionBarcode.FORMAT_UPC_E)
                        .build();
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);

        // Extract barcode from image
        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        Log.v(TAG, "L'extracció de barcodes ha funcionat i s'han detectat " + barcodes.size() + " codis de barres");
                        if (barcodes.size() == 0) {
                            Snackbar.make(getWindow().getDecorView(),"No s'ha detectat cap codi de barres", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        } else {
                            // Toast with success
                            Snackbar.make(getWindow().getDecorView(),"S'han detectat " + barcodes.size() + " codis de barres", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            // Add barcode in code text input
                            for (FirebaseVisionBarcode barcode: barcodes) {
                                String rawValue = barcode.getRawValue();
                                EditText codeEditText = (EditText) findViewById(R.id.codeEditText);
                                codeEditText.setText(rawValue);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Toast with error
                        Snackbar.make(getWindow().getDecorView(),"No s'ha pogut reconeixer el codi de barres", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });

        EditText quantityEditText = (EditText) findViewById(R.id.quantityEditText);
        quantityEditText.requestFocus();
    }

}


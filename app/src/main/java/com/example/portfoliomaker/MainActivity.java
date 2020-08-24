package com.example.portfoliomaker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;


import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    EditText name, contact, email, dob, education, experience, skills;
    Button insert, update, delete, view, pdf;
    DBHelper DB;
    Bitmap bmp, scaledbmp, dp, scaleddp;
    int pageWidth = 1200;
    private ImageView pImageView;

    private  static final int CAMERA_REQUEST_CODE = 100;
    private  static final int STORAGE_REQUEST_CODE = 101;
    private  static final int IMAGE_PICK_CAMERA_CODE = 102;
    private  static final int IMAGE_PICK_GALLERY_CODE = 103;

    private String[] cameraPermissions;
    private String[] storagePermissions;
    private Uri imageUri;
    private int which;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        name = findViewById(R.id.name);
        contact = findViewById(R.id.contact);
        email = findViewById(R.id.email);
        dob = findViewById(R.id.dob);
        education = findViewById(R.id.education);
        experience = findViewById(R.id.experience);
        skills = findViewById(R.id.skills);

        insert = findViewById(R.id.btnInsert);
        update = findViewById(R.id.btnUpdate);
        delete = findViewById(R.id.btnDelete);
        view = findViewById(R.id.btnView);
        pdf = findViewById(R.id.btnPdf);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.header1);
        //bmp = BitmapFactory.decodeResource(getResources(),R.drawable.pastelblue);
        dp = BitmapFactory.decodeResource(getResources(), R.drawable.imgicon);
        scaledbmp = Bitmap.createScaledBitmap(bmp, 1200, 200, false);
        scaleddp = Bitmap.createScaledBitmap(dp,250, 250, false);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        DB = new DBHelper(this);

        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameTxt = name.getText().toString();
                String contactTxt = contact.getText().toString();
                String emailTxt = email.getText().toString();
                String dobTxt = dob.getText().toString();
                String educationTxt = education.getText().toString();
                String experienceTxt = experience.getText().toString();
                String skillsTxt = skills.getText().toString();

                Boolean checkinsertdata = DB.insertuserdata(nameTxt, contactTxt, emailTxt, dobTxt, educationTxt, experienceTxt, skillsTxt);
                if (checkinsertdata == true) {
                    Toast.makeText(MainActivity.this, "New Entry Inserted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "New Entry Not Inserted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameTxt = name.getText().toString();
                String contactTxt = contact.getText().toString();
                String emailTxt = email.getText().toString();
                String dobTxt = dob.getText().toString();
                String educationTxt = education.getText().toString();
                String experienceTxt = experience.getText().toString();
                String skillsTxt = skills.getText().toString();

                Boolean checkupdatedata = DB.updateuserdata(nameTxt, contactTxt, emailTxt, dobTxt, educationTxt, experienceTxt, skillsTxt);
                if (checkupdatedata == true) {
                    Toast.makeText(MainActivity.this, "Entry Updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Entry Not Updated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameTxt = name.getText().toString();
                Boolean checkdeletedata = DB.deletedata(nameTxt);
                if (checkdeletedata == true) {
                    Toast.makeText(MainActivity.this, "Entry Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Entry Not Deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor res = DB.getData();
                if (res.getCount() == 0) {
                    Toast.makeText(MainActivity.this, "No Entry Exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuffer buffer = new StringBuffer();
                while (res.moveToNext()) {
                    buffer.append("Name : " + res.getString(0) + "\n");
                    buffer.append("Contact : " + res.getString(1) + "\n");
                    buffer.append("email : " + res.getString(1) + "\n");
                    buffer.append("Date Of Birth : " + res.getString(2) + "\n");
                    buffer.append("Education : " + res.getString(3) + "\n");
                    buffer.append("Experience : " + res.getString(4) + "\n");
                    buffer.append("Skills : " + res.getString(5) + "\n\n");
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setTitle("Portfolio Data List");
                builder.setMessage(buffer.toString());
                builder.show();
            }
        });
//        pImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                imagePickDialog();
//            }
//        });


        createPDF();
    }

    private void createPDF () {
        pdf.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {

                if (name.getText().toString().length() == 0 ||
                        contact.getText().toString().length() == 0 ||
                        email.getText().toString().length() == 0 ||
                        dob.getText().toString().length() == 0 ||
                        education.getText().toString().length() == 0 ||
                        experience.getText().toString().length() == 0 ||
                        skills.getText().toString().length() == 0) {
                    Toast.makeText(MainActivity.this, "Some fields are empty", Toast.LENGTH_SHORT).show();
                } else {
                    PdfDocument myPdfDocument = new PdfDocument();
                    Paint myPaint = new Paint();

                    PdfDocument.PageInfo myPageInfo1 = new PdfDocument.PageInfo.Builder(1200, 2010, 1).create();
                    PdfDocument.Page myPage1 = myPdfDocument.startPage(myPageInfo1);
                    Canvas canvas = myPage1.getCanvas();

                    canvas.drawBitmap(scaledbmp, 0, 0, myPaint);
                    canvas.drawBitmap(scaleddp, 50, 350, myPaint);


                    myPaint.setTextAlign(Paint.Align.LEFT);
                    myPaint.setTextSize(25f);
                    myPaint.setColor(Color.GRAY);
                    canvas.drawText("Personal Information", 40, 300, myPaint);
                    canvas.drawText("Education", 40, 950, myPaint);
                    canvas.drawText("Professional Experiences & Skills", 40, 1100, myPaint);

                    myPaint.setTextAlign(Paint.Align.LEFT);
                    myPaint.setTextSize(30f);
                    myPaint.setColor(Color.DKGRAY);
                    canvas.drawText("Name:  " + name.getText(), 50, 700, myPaint);
                    canvas.drawText("Contact No:  " + contact.getText(), 50, 750, myPaint);
                    canvas.drawText("Email:  " + email.getText(), 50, 800, myPaint);
                    canvas.drawText("Date of Birth:  " + dob.getText(), 50, 850, myPaint);
                    canvas.drawText("Education:  " + education.getText(), 50, 1000, myPaint);
                    canvas.drawText("Job Experience:  " + experience.getText(), 50, 1150, myPaint);
                    canvas.drawText("Skills:  " + skills.getText(), 50, 1200, myPaint);

                    myPdfDocument.finishPage(myPage1);
                    File file = new File(Environment.getExternalStorageDirectory(), "/FirstPDF.pdf");

                    try {
                        myPdfDocument.writeTo(new FileOutputStream(file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    myPdfDocument.close();
                }

            }
        });


    }

    //THE CODE BELOW IS FOR SELECTING IMAGE FROM GALLERY:
//    private void imagePickDialog() {
//        String[] options = {"Camera", "Gallery"};
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        builder.setTitle("Select your image");
//        builder.setItems(options, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                if (which == 0) {
//                    //if 0 then open camera and check permission.
//                    if (!checkCameraPermission()) {
//                        requestCameraPermission();
//                    }
//                    else {
//                        pickFromCamera();
//                    }
//                }
//                else if (which == 1) {
//                    if (!checkStoragePermission()) {
//                        requestStoragePermission();
//                    } else {
//                        pickFromStorage();
//                    }
//                }
//
//            }
//        });
//        builder.create().show();
//
//    }

//    //function to get image from gallery
//    private void pickFromStorage() {
//        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
//        galleryIntent.setType("image/*");
//        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
//    }
//
//    //Function to get image from camera
//    private void pickFromCamera() {
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.TITLE, "Image title");
//        values.put(MediaStore.Images.Media.DESCRIPTION, "Image description");
//
//        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
//    }
//
//    private boolean checkStoragePermission(){
//        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                == (PackageManager.PERMISSION_GRANTED);
//        return result;
//    }
//    private void requestStoragePermission() {
//        ActivityCompat.requestPermissions(this,storagePermissions, STORAGE_REQUEST_CODE);
//    }
//    private boolean checkCameraPermission() {
//        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
//                == (PackageManager.PERMISSION_GRANTED);
//
//        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                == (PackageManager.PERMISSION_GRANTED);
//
//        return result && result1;
//    }
//    private void requestCameraPermission() {
//        ActivityCompat.requestPermissions(this, cameraPermissions,CAMERA_REQUEST_CODE);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        switch (requestCode) {
//            case CAMERA_REQUEST_CODE: {
//                if (grantResults.length>0) {
//                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//
//                    if (cameraAccepted && storageAccepted) {
//                        pickFromCamera();
//                    }
//                    else {
//                        Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//            break;
//            case STORAGE_REQUEST_CODE: {
//                if (grantResults.length> 0) {
//                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//
//                    if (storageAccepted) {
//                        pickFromStorage();
//                    }
//                    else {
//                        Toast.makeText(this, "Storage permission required",Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//
//        if (requestCode == RESULT_OK) {
//
//            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
//                CropImage.activity(data.getData())
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setAspectRatio(1,1)
//                        .start(this);
//            }
//            else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
//                CropImage.activity(imageUri)
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setAspectRatio(1,1)
//                        .start(this);
//            }
//            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//                CropImage.ActivityResult result = CropImage.getActivityResult(data);
//                if (requestCode == RESULT_OK) {
//                    Uri resultUri = result.getUri();
//                    imageUri = resultUri;
//                    pImageView.setImageURI(resultUri);
//                }
//                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                    Exception error = result.getError();
//                    Toast.makeText(this,""+error, Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return super.onSupportNavigateUp();
//    }


}


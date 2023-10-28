package com.example.shop2order.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shop2order.Constants;
import com.example.shop2order.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddProductctivity extends AppCompatActivity {

    //ui views
    private  ImageButton backBtn;
    private ImageView productIconIv;
    private EditText titleEt,descriptionEt,quantityEt,discountedpriceEt,discountedNoteEt,priceEt;
    private TextView categoryTv;
    private SwitchCompat discountSwitch;
    private Button addProductBtn;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //PERMISSION ARRAYS
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //image picked uri
    private Uri image_uri;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_productctivity);

        backBtn=findViewById(R.id.backBtn);
        productIconIv=findViewById(R.id.productIconIv);
        titleEt=findViewById(R.id.titleEt);
        descriptionEt=findViewById(R.id.descriptionEt);
        categoryTv=findViewById(R.id.categoryTv);
        quantityEt=findViewById(R.id.quantityEt);
        priceEt=findViewById(R.id.priceEt);
        discountSwitch=findViewById(R.id.discountSwitch);
        discountedpriceEt=findViewById(R.id.discountedpriceEt);
        discountedNoteEt=findViewById(R.id.discountedNoteEt);
        addProductBtn=findViewById(R.id.addProductBtn);

        //on start is unchecked,so hide discountPriceEt.discountNoteEt
        discountedpriceEt.setVisibility(View.GONE);
        discountedNoteEt.setVisibility(View.GONE);

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();


        //init permission array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //if discountSwitch is checked:show discountPriceEt,discountNoteEt | if discountSwitch is not checked: hide discountPriceEt,discountNoteEt
        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //checked,show discountPriceEt,discountNoteEt
                    discountedpriceEt.setVisibility(View.VISIBLE);
                    discountedNoteEt.setVisibility(View.VISIBLE);
                }
                else{
                    //unchecked,hide discountPriceEt,discountNoteEt
                    discountedpriceEt.setVisibility(View.GONE);
                    discountedNoteEt.setVisibility(View.GONE);
                    discountedpriceEt.setText("");
                    discountedNoteEt.setText("");
                    addProductBtn.setEnabled(true);

                }
            }
        });

        productIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog to pick image
                showImagePickDialog();
            }
        });

        categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick category
                categoryDialog();
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Flow:
                //1)Input data
                //2)Validate data
                //3)Add data to db
                inputData();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //automatic discount note calculator
        TextWatcher textWatcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!priceEt.getText().toString().equals("")&&!discountedpriceEt.getText().toString().equals("")){
                    float temp1=Float.parseFloat(priceEt.getText().toString());
                    float temp2=Float.parseFloat(discountedpriceEt.getText().toString());
                    float temp3=temp1-temp2;
                    float dNote= 100*temp3/temp1;
                    int dNote2=(int)dNote;
                    if(dNote2<0){
                       Toast.makeText(AddProductctivity.this,"Discount Price should be less than Price",Toast.LENGTH_LONG).show();
                        discountedNoteEt.setText((String.valueOf(dNote2)) + "% OFF");
                        addProductBtn.setEnabled(false);
                    }
                    else{

                        discountedNoteEt.setText((String.valueOf(dNote2)) + "% OFF");
                         addProductBtn.setEnabled(true);
                   }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        priceEt.addTextChangedListener(textWatcher);
        discountedpriceEt.addTextChangedListener(textWatcher);



    }

    private String productTitle,productDescription,productCategory,productQuantity,originalPrice,discountPrice,discountNote;
    private boolean discountAvailable=false;
    private void inputData() {
        //1)Input data
        productTitle=titleEt.getText().toString().trim();
        productDescription=descriptionEt.getText().toString().trim();
        productCategory=categoryTv.getText().toString().trim();
        productQuantity=quantityEt.getText().toString().trim();
        originalPrice=priceEt.getText().toString().trim();
        discountAvailable=discountSwitch.isChecked(); //true/false

        //2)Validate data
        if(TextUtils.isEmpty(productTitle)){
            Toast.makeText(this,"Title is required...",Toast.LENGTH_SHORT).show();
            return; //donot proceed further
        }
        if(TextUtils.isEmpty(productCategory)){
            Toast.makeText(this,"Category is required...",Toast.LENGTH_SHORT).show();
            return; //donot proceed further
        }


        if(TextUtils.isEmpty(originalPrice)){
            Toast.makeText(this,"Price is required...",Toast.LENGTH_SHORT).show();
            return; //donot proceed further
        }
        if(discountAvailable) {
            //product is with discount

            discountPrice = discountedpriceEt.getText().toString().trim();
            discountNote = discountedNoteEt.getText().toString().trim();

            if (TextUtils.isEmpty(discountPrice)) {
                Toast.makeText(this, "Discount Price is required...", Toast.LENGTH_SHORT).show();
                return; //donot proceed further
            }

        }
            else{
                //product is without discount
                discountPrice="0";
                discountNote="";


            }


        addProduct();

    }



    private void addProduct() {
        //3)Add data to db

        progressDialog.setMessage("Adding Product...");
        progressDialog.show();

        String timestamp=""+System.currentTimeMillis();


        if(image_uri==null){
            //upload without image

            //setup data to upload
            HashMap<String,Object> hashMap=new HashMap<>();
            hashMap.put("productId",""+timestamp);
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productDescription",""+productDescription);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("productQuantity",""+productQuantity);
            hashMap.put("productIcon","");      //no image,set empty
            hashMap.put("originalPrice",""+originalPrice);
            hashMap.put("discountPrice",""+discountPrice);
            hashMap.put("discountNote",""+discountNote);
            hashMap.put("discountAvailable",""+discountAvailable);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("blocked","false");
            hashMap.put("uid",""+firebaseAuth.getUid());
            hashMap.put("noStock","false");
            //add to db
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //added to db

                            progressDialog.dismiss();
                            Toast.makeText(AddProductctivity.this,"Product added...",Toast.LENGTH_SHORT).show();
                            clearData();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed to adding to db
                            progressDialog.dismiss();
                            Toast.makeText(AddProductctivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });
        }
        else{
            //upload with image

            //first upload image to storage

            //name and path of image to be uploaded
            String filePathAndName="product_images/"+""+timestamp;

            StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Image uploaded
                            //get url of uploaded image
                            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri=uriTask.getResult();

                            if(uriTask.isSuccessful()){
                                //url of image recieved,upload to db
                                //setup data to upload
                                HashMap<String,Object> hashMap=new HashMap<>();
                                hashMap.put("productId",""+timestamp);
                                hashMap.put("productTitle",""+productTitle);
                                hashMap.put("productDescription",""+productDescription);
                                hashMap.put("productCategory",""+productCategory);
                                hashMap.put("productQuantity",""+productQuantity);
                                hashMap.put("productIcon",""+downloadImageUri);
                                hashMap.put("originalPrice",""+originalPrice);
                                hashMap.put("discountPrice",""+discountPrice);
                                hashMap.put("discountNote",""+discountNote);
                                hashMap.put("discountAvailable",""+discountAvailable);
                                hashMap.put("timestamp",""+timestamp);
                                hashMap.put("uid",""+firebaseAuth.getUid());
                                hashMap.put("noStock","false");
                                hashMap.put("blocked","false");
                                //add to db
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //added to db
                                                progressDialog.dismiss();
                                                Toast.makeText(AddProductctivity.this,"Product added...",Toast.LENGTH_SHORT).show();
                                                clearData();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to adding to db
                                                progressDialog.dismiss();
                                                Toast.makeText(AddProductctivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                                            }
                                        });
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Failed uploading image
                            progressDialog.dismiss();
                            Toast.makeText(AddProductctivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    }
    private void clearData(){
        //clear data after uploading product
        titleEt.setText("");
        descriptionEt.setText("");
        categoryTv.setText("");
        quantityEt.setText("");
        priceEt.setText("");
        discountedpriceEt.setText("");
        discountedNoteEt.setText("");
        productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        image_uri=null;
    }

    private void categoryDialog() {
        //dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get picked category
                        String category=Constants.productCategories[which];

                        //set picked category
                        categoryTv.setText(category);
                    }
                })
                .show();
    }

    private void showImagePickDialog() {
        //options to display in dialog
        String[] options = {"Camera","Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle clicks
                        if (which == 0) {
                            //camera clicked
                            if (checkCameraePermission()) {
                                //camera permission allowed
                                pickFromCamera();
                                ;

                            } else {
                                //not allowed,request
                                requestCameraPermission();

                            }
                        } else {
                            //gallery clicked
                            if (checkStoragePermission()) {
                                //storage permission allowed
                                pickFromGallery();
                            } else {
                                //not allowed,request
                                requestStoragePermission();
                            }
                        }

                    }
                })
                .show();

    }

    private void  pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);

    }

    private void pickFromCamera() {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }
    private boolean checkStoragePermission() {
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraePermission() {
        boolean result=ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result&&result1;
    }

    private void  requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }

    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        //permission allowed
                        pickFromCamera();

                    }
                    else{
                        //permission denied
                        Toast.makeText(this, "Camera permissions are necessary...", Toast.LENGTH_SHORT).show();

                    }
                }
            }
            break;


            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        //permission allowed
                        pickFromGallery();

                    }
                    else{
                        //permission denied
                        Toast.makeText(this, "Storage permission is necessary...", Toast.LENGTH_SHORT).show();

                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //handle image pick result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                //get picked image
                image_uri=data.getData();
                //set to image view
                productIconIv.setImageURI(image_uri);
            }
            else if(requestCode==IMAGE_PICK_CAMERA_CODE){

                //set to image view
                productIconIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
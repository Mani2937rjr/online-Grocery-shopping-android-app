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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.shop2order.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProfileEditSellerActivity extends AppCompatActivity implements LocationListener {
    private ImageButton backBtn,gpsBtn;
    private ImageView profileIv;
    private EditText nameEt,shopNameET,phoneET,deliveryFeeET,countryET,stateET,cityET,addressET;
    private Button updateBtn;
    private SwitchCompat shopOpenSwitch;

    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //PERMISSION ARRAYS
    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //image picked uri
    private Uri image_uri;


    private double latitude = 0.0, longitude = 0.0;


    private LocationManager locationManager;

    //progress dialog
    private ProgressDialog progressDialog;

    //firebase auth
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_seller_activity);


        backBtn=findViewById(R.id.backBtn);
        gpsBtn=findViewById(R.id.gpsBtn);
        profileIv=findViewById(R.id.profileIv);
        nameEt=findViewById(R.id.nameEt);
        shopNameET=findViewById(R.id.shopNameET);
        phoneET=findViewById(R.id.phoneET);
        deliveryFeeET=findViewById(R.id.deliveryFeeET);
        countryET=findViewById(R.id.countryET);
        stateET=findViewById(R.id.stateET);
        cityET=findViewById(R.id.cityET);
        addressET=findViewById(R.id.addressET);
        shopOpenSwitch=findViewById(R.id.shopOpenSwitch);
        updateBtn=findViewById(R.id.updateBtn);

        //init permission array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        checkUser();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect  location
                if(checkLocationPermission()){
                    //already allowed
                    detectLocation();

                }
                else{
                    //not allowed,request
                    requestLocationPermission();

                }

            }
        });


        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick image
                showImagePickDialog();

            }
        });


        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //begin update profile
                inputData();

            }
        });
    }
    private String name,shopName,phone,deliveryFee,country,state,city,address;
    private boolean shopOpen;

    private void inputData() {
        //input data
        name=nameEt.getText().toString().trim();
        shopName=shopNameET.getText().toString().trim();
        phone=phoneET.getText().toString().trim();
        deliveryFee=deliveryFeeET.getText().toString().trim();
        country=countryET.getText().toString().trim();
        state=stateET.getText().toString().trim();
        city=cityET.getText().toString().trim();
        address=addressET.getText().toString().trim();
        shopOpen=shopOpenSwitch.isChecked();//true or false

        if(TextUtils.isEmpty(name)){
            nameEt.setError("Enter Name...");
            return;
        }

        if (!name.matches("^[a-zA-Z ]*$")){
            nameEt.setError("Enter Valid Name ...");
            return;
        }

        if(TextUtils.isEmpty(shopName)){
            shopNameET.setError("Enter Shop Name...");
            return;
        }

        if(TextUtils.isEmpty(deliveryFee)){
            shopNameET.setError("Enter delivery fee...");
            return;
        }


        if(TextUtils.isEmpty(phone)||!Patterns.PHONE.matcher(phone).matches()){
            phoneET.setError("Enter Phone Number...");
            return;
        }
        //mobile no less than 10
        if(!phone.matches("[0-9]{10}")) {
            phoneET.requestFocus();
            phoneET.setError("Enter phone number correctly");
            return;
        }

        if(latitude==0.0||longitude==0.0){
            Toast.makeText(this,"Please click GPS button to detect location...",Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(state)){
            Toast.makeText(this,"Enter State...",Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(country)){
            Toast.makeText(this,"Enter country...",Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(city)){
            Toast.makeText(this,"Enter city...",Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(address)){
            Toast.makeText(this,"Enter address...",Toast.LENGTH_LONG).show();
            return;
        }

        updateProfile();

    }

    private void updateProfile() {
        progressDialog.setMessage("Updating Profile...");
        progressDialog.show();

        if(image_uri==null){
            //update without image

            //setup data to update
            HashMap<String,Object>hashMap=new HashMap<>();
            hashMap.put("name",""+name);
            hashMap.put("shopName",""+shopName);
            hashMap.put("phone",""+phone);
            hashMap.put("deliveryFee",""+deliveryFee);
            hashMap.put("country",""+country);
            hashMap.put("state",""+state);
            hashMap.put("city",""+city);
            hashMap.put("address",""+address);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("shopOpen",""+shopOpen);

            //update to db
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //updated
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this,"Profile updated successfully",Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //fail to update
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });


        }
        else
        {
            //update withimage

            /*----------Upload image firs----------*/
            String filePathAndName="profile_images/*"+""+firebaseAuth.getUid();
            //get storage reference
            StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded,get url of upload image
                            Task<Uri>uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri=uriTask.getResult();

                            if(uriTask.isSuccessful()){
                                //image  url recieved,now update db

                                //setup data to update
                                HashMap<String,Object>hashMap=new HashMap<>();
                                hashMap.put("name",""+name);
                                hashMap.put("shopName",""+shopName);
                                hashMap.put("phone",""+phone);
                                hashMap.put("deliveryFee",""+deliveryFee);
                                hashMap.put("country",""+country);
                                hashMap.put("state",""+state);
                                hashMap.put("city",""+city);
                                hashMap.put("address",""+address);
                                hashMap.put("latitude",""+latitude);
                                hashMap.put("longitude",""+longitude);
                                hashMap.put("shopOpen",""+shopOpen);
                                hashMap.put("profileImage",""+downloadImageUri);

                                //update to db
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //updated
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileEditSellerActivity.this,"Profile updated successfully",Toast.LENGTH_SHORT).show();
                                                onBackPressed();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //fail to update
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileEditSellerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });

        }
    }

    private void checkUser() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user==null){
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
        }
        else{
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        //load user info and set to views
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            String accountType=""+ds.child("accountType").getValue();
                            String address=""+ds.child("address").getValue();
                            String city=""+ds.child("city").getValue();
                            String state=""+ds.child("state").getValue();
                            String country=""+ds.child("country").getValue();
                            String deliveryFee=""+ds.child("deliveryFee").getValue();
                            String email=""+ds.child("email").getValue();

                            latitude= Double.parseDouble(""+ds.child("latitude").getValue());
                            longitude= Double.parseDouble(""+ds.child("longitude").getValue());

                            String name=""+ds.child("name").getValue();
                            String online=""+ds.child("online").getValue();
                            String phone=""+ds.child("phone").getValue();
                            String profileImage=""+ds.child("profileImage").getValue();
                            String timestamp=""+ds.child("timestamp").getValue();
                            String shopName=""+ds.child("shopName").getValue();
                            String shopOpen=""+ds.child("shopOpen").getValue();
                            String uid=""+ds.child("uid").getValue();

                            nameEt.setText(name);
                            phoneET.setText(phone);
                            countryET.setText(country);
                            stateET.setText(state);
                            cityET.setText(city);
                            addressET.setText(address);
                            shopNameET.setText(shopName);
                            deliveryFeeET.setText(deliveryFee);

                            if(shopOpen.equals("true")){
                                shopOpenSwitch.setChecked(true);
                            }
                            else{
                                shopOpenSwitch.setChecked(false);
                            }
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIv);

                            }catch (Exception e){
                                profileIv.setImageResource(R.drawable.ic_person_gray);

                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

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
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }

    private void  requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,locationPermissions,LOCATION_REQUEST_CODE);

    }

    private boolean checkStoragePermission() {
        boolean result=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private boolean checkCameraePermission() {
        boolean result=ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result&&result1;
    }

    private boolean checkLocationPermission() {
        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)==(PackageManager.PERMISSION_GRANTED);
        return result;

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


    private void detectLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }



    private void findAddress() {
        //find country ,state ,city
        Geocoder geocoder;

        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses=geocoder.getFromLocation(latitude,longitude,1);

            String address=addresses.get(0).getAddressLine(0);//Complete address
            String city=addresses.get(0).getLocality();
            String state=addresses.get(0).getAdminArea();
            String country=addresses.get(0).getCountryName();

            //set address
            countryET.setText(country);
            stateET.setText(state);
            cityET.setText(city);
            addressET.setText(address);


        } catch (Exception e) {
            Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {

        //location detected
        latitude=location.getLatitude();
        longitude=location.getLongitude();

        findAddress();

    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        //gps/location disabled
        Toast.makeText(this, "Please turn on location", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean locationAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(locationAccepted){
                        //permission allowed
                        detectLocation();

                    }
                    else{
                        //permission denied
                        Toast.makeText(this, "Location permission neccessary...", Toast.LENGTH_SHORT).show();

                    }
                }
            }
            break;

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
                        Toast.makeText(this, "Camera permissions are neccessary...", Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(this, "Storage permission is neccessary...", Toast.LENGTH_SHORT).show();

                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                //get picked image
                image_uri=data.getData();
                //set to image view
                profileIv.setImageURI(image_uri);
            }
            else if(requestCode==IMAGE_PICK_CAMERA_CODE){

                //set to image view
                profileIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
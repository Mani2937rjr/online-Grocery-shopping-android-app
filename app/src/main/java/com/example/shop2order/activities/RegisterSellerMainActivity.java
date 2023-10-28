package com.example.shop2order.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.Toast;

import com.example.shop2order.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterSellerMainActivity extends AppCompatActivity implements LocationListener {
    private ImageButton backBtn, gpsBtn;
    private CircleImageView profileIv;
    private EditText nameET, shopNameET, phoneET, deliveryFeeET, countryET, stateET, cityET, addressET, emailET, passwordET, cpasswordET;
    private Button registerBtn;

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

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    //Password strength pattern creation
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,}" +               //at least 6 characters
                    "$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_seller_main);
        backBtn = findViewById(R.id.backBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        profileIv = findViewById(R.id.profileIv);
        nameET = findViewById(R.id.nameET);
        shopNameET = findViewById(R.id.shopNameET);
        phoneET = findViewById(R.id.phoneET);
        deliveryFeeET = findViewById(R.id.deliveryFeeET);
        countryET = findViewById(R.id.countryET);
        stateET = findViewById(R.id.stateET);
        cityET = findViewById(R.id.cityET);
        addressET = findViewById(R.id.addressET);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        cpasswordET = findViewById(R.id.cpasswordET);
        registerBtn = findViewById(R.id.registerBtn);

        //init permission array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};



        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect current location
                if (checkLocationPermission()) {

                    //already allowed
                    detectLocation();
                } else {
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

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //register user
                inputData();

            }
        });


    }



    private String fullName,shopName,phoneNumber,deliveryFee,country,state,city,address,email,password,confirmPassword;
    private void inputData() {
        //input data
        fullName=nameET.getText().toString().trim();
        shopName=shopNameET.getText().toString().trim();
        phoneNumber=phoneET.getText().toString().trim();
        deliveryFee=deliveryFeeET.getText().toString().trim();
        country=countryET.getText().toString().trim();
        state=stateET.getText().toString().trim();
        city=cityET.getText().toString().trim();
        address=addressET.getText().toString().trim();
        email=emailET.getText().toString().trim();
        password=passwordET.getText().toString().trim();
        confirmPassword=cpasswordET.getText().toString().trim();
        //validate dada
        if (TextUtils.isEmpty(fullName)&&TextUtils.isEmpty(shopName)&&TextUtils.isEmpty(deliveryFee)&&TextUtils.isEmpty(phoneNumber) || !Patterns.PHONE.matcher(phoneNumber).matches()&&
                TextUtils.isEmpty(country)&&TextUtils.isEmpty(state)&&TextUtils.isEmpty(city)&&TextUtils.isEmpty(address)&&TextUtils.isEmpty(email)&&
                TextUtils.isEmpty(password)&&TextUtils.isEmpty(confirmPassword)){
            nameET.setError("Enter Name...");
            shopNameET.setError("Enter Shop Name...");
            phoneET.setError("Enter Phone Number...");
            deliveryFeeET.setError("Enter Delivery Fee...");
            countryET.setError("Enter country name...");
            stateET.setError("Enter state name...");
            cityET.setError("Enter country name...");
            addressET.setError("Enter address name...");
            emailET.setError("Enter email...");
            passwordET.setError("Enter password...");
            cpasswordET.setError("Enter confirm password...");
            Toast.makeText(this,"Please enter all the fields",Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(fullName)){
            nameET.setError("Enter Name...");
            return;
        }
        if (!fullName.matches("^[a-zA-Z ]*$")){
            nameET.setError("Enter Valid Name ...");
            return;
        }
        if(TextUtils.isEmpty(shopName)){
            shopNameET.setError("Enter Shop Name...");
            return;
        }
        if(TextUtils.isEmpty(phoneNumber)){
            phoneET.setError("Enter Phone Number...");
            return;
        }
//mobile no less than 10
        if(!phoneNumber.matches("[0-9]{10}")) {
            phoneET.requestFocus();
            phoneET.setError("Enter phone number correctly");
            return;
        }
            if(TextUtils.isEmpty(deliveryFee)){
                deliveryFeeET.setError("Enter Delivery Fee...");

            return;
        }


        if(latitude==0.0||longitude==0.0){
            Toast.makeText(this,"Please click GPS button to detect location...",Toast.LENGTH_LONG).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailET.setError("Invalid email pattern...");
            return;
        }
        if(password.length()<6){
            passwordET.setError("Password must be atleast 6 character long...");
            return;
        }
        if(!PASSWORD_PATTERN.matcher(password).matches()){
            passwordET.setError("...Password is weak..."+"\n"+"1)at least 1 digit[0-9] \n 2)at least 1 lower case letter[a-z] \n " +
                    "3)at least 1 upper case letter[A-Z] \n 4)at least 1 special character[@#$%^&+=] \n 5)no white spaces");
            return;
        }

        if(!password.equals(confirmPassword)){
            cpasswordET.setError("Password doesn't match...");
            return;
        }

        createAccount();
    }



    private void createAccount() {
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        //create account
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account created
                        saverFirebaseData();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed creating account
                        progressDialog.dismiss();
                        Toast.makeText(RegisterSellerMainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void saverFirebaseData() {
        progressDialog.setMessage("Saving Account Info...");

        String timestamp=""+System.currentTimeMillis();

        if(image_uri==null){
            //save info without image

            //setup data to save
            HashMap<String,Object> hashMap=new HashMap<>();
            hashMap.put("uid",""+firebaseAuth.getUid());
            hashMap.put("email",""+email);
            hashMap.put("name",""+fullName);
            hashMap.put("shopName",""+shopName);
            hashMap.put("phone",""+phoneNumber);
            hashMap.put("deliveryFee",""+deliveryFee);
            hashMap.put("country",""+country);
            hashMap.put("state",""+state);
            hashMap.put("city",""+city);
            hashMap.put("address",""+address);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("accountType","Seller");
            hashMap.put("online","true");
            hashMap.put("shopOpen","true");
            hashMap.put("profileImage","");
            hashMap.put("verified", "false");
            hashMap.put("deleted", "false");


            //save to db
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            //db updated
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterSellerMainActivity.this,MainSellerActivity.class));
                            finish();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed updating db
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterSellerMainActivity.this,MainSellerActivity.class));
                            finish();

                        }
                    });

        }
        else{
            //save info with image

            //name and path of image
            String filePathAndName="profile_images/"+""+firebaseAuth.getUid();
            //upload image
            StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get url of uploaded image
                            Task<Uri>uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());
                            Uri downloadImageUri=uriTask.getResult();

                            if(uriTask.isSuccessful()){

                                //setup data to save
                                HashMap<String,Object> hashMap=new HashMap<>();
                                hashMap.put("uid",""+firebaseAuth.getUid());
                                hashMap.put("email",""+email);
                                hashMap.put("name",""+fullName);
                                hashMap.put("shopName",""+shopName);
                                hashMap.put("phone",""+phoneNumber);
                                hashMap.put("deliveryFee",""+deliveryFee);
                                hashMap.put("country",""+country);
                                hashMap.put("state",""+state);
                                hashMap.put("city",""+city);
                                hashMap.put("address",""+address);
                                hashMap.put("latitude",""+latitude);
                                hashMap.put("longitude",""+longitude);
                                hashMap.put("timestamp",""+timestamp);
                                hashMap.put("accountType","Seller");
                                hashMap.put("online","true");
                                hashMap.put("shopOpen","true");
                                hashMap.put("verified", "false");
                                hashMap.put("deleted", "false");
                                hashMap.put("profileImage",""+downloadImageUri);//url of uploaded image

                                //save to db
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //db updated
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterSellerMainActivity.this,MainSellerActivity.class));
                                                finish();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed updating db
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterSellerMainActivity.this,MainSellerActivity.class));
                                                finish();

                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            Toast.makeText(RegisterSellerMainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }
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

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);

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

    private boolean checkLocationPermission(){
        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,locationPermissions,LOCATION_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraePermission(){
        boolean result=ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result&&result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
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
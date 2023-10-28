package com.example.shop2order;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shop2order.activities.MainSellerActivity;
import com.example.shop2order.databinding.ActivityUploadDocumentBinding;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class UploadDocumentActivity extends AppCompatActivity {
    String shopUid;

    private ActivityUploadDocumentBinding binding;

    private FirebaseAuth firebaseAuth;

    private Uri pdfUri=null;

    private ProgressDialog progressDialog;

    private static  final String TAG="ADD_PDF_TAG";

    private  static final int PDF_PICK_CODE=1000;

    String date;

    private ArrayList<ModelAPdf> pdfArrayList;
    private AdapterSellerPdf adapterPdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUploadDocumentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        shopUid=getIntent().getStringExtra("shopUid");

        firebaseAuth=FirebaseAuth.getInstance();

        loadPdfList();

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(UploadDocumentActivity.this);
                builder.setTitle("REQUIRED DOCUMENTS")
                        .setCancelable(false)
                        .setMessage("Please attach below mentioned documents as pdf file\n" +
                                "1.  Shop and Establishment License\n2. Adhar Card\n3. Pan Card\n4. Photo" +
                                "\n5.Educational or any degree certificates \n6.Any other documents")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pdfPickIntent();
                            }
                        })
                        .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();

            }
        });

        binding.infoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view= LayoutInflater.from(UploadDocumentActivity.this).inflate(R.layout.dialog_documetinfo,null);
                TextView displayTv=view.findViewById(R.id.displayTv);

                displayTv.setText("1.  Shop and Establishment License\n2. Adhar Card\n3. Pan Card\n4. Photo" +
                                "\n5. Educational or any degree certificates \n6. Any other documents");

                //dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(UploadDocumentActivity.this);
                //set views to dialog
                builder.setView(view);
                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);

                AlertDialog dialog=builder.create();
                dialog.show();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validateData();
            }
        });
    }

    private void loadPdfList() {
        pdfArrayList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Documents");
        ref.orderByChild("id")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            ModelAPdf modelAPdf=ds.getValue(ModelAPdf.class);
                            pdfArrayList.add(modelAPdf);
                        }
                        adapterPdf=new AdapterSellerPdf(UploadDocumentActivity.this,pdfArrayList);
                        binding.bookRv.setAdapter(adapterPdf);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void validateData() {
        if (pdfUri==null) {
            Toast.makeText(this, "Please pick pdf file...", Toast.LENGTH_SHORT).show();
        }
        else {
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        Log.d(TAG,"uploadPdfToStorage:Uploading to storage...");

        progressDialog.setMessage("Uploading Pdf...");
        progressDialog.show();

        long timestamp=System.currentTimeMillis();
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(String.valueOf(timestamp)));
        date= DateFormat.format("dd/MM/yyyy",calendar).toString(); //e.g 20/06/2022

        String timestamp1=Long.toString(timestamp);

        String filePathAndName="pdfFiles/"+timestamp1;

        StorageReference storageReference=FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG,"onSuccess:PDF uploaded...");
                        Log.d(TAG,"onSuccess:getting pdf url...");

                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl=""+uriTask.getResult();

                        //upload to db
                        uploadPdfInfoToDb(uploadedPdfUrl,timestamp1);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onFailure:PDF upload failed due to"+e.getMessage());
                        Toast.makeText(UploadDocumentActivity.this,"PDF upload failed due to"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, String timestamp1) {
        Log.d(TAG,"uploadPdfInfoToDb:Uploading pdf info to firebase db...");

        progressDialog.setMessage("Uploading pdf info...");

        String uid=firebaseAuth.getUid();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp1);
        hashMap.put("date",""+date);
        hashMap.put("pdfUrl",""+uploadedPdfUrl);
        hashMap.put("timestamp",""+timestamp1);

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Documents");
        reference.child(""+timestamp1).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onSuccess:PDF uploaded successfully...");
                        Toast.makeText(UploadDocumentActivity.this,"PDF uploaded successfully...",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onFailure:Failed to upload to db due to "+e.getMessage());
                        Toast.makeText(UploadDocumentActivity.this,"Failed to upload to db due to"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void pdfPickIntent() {

        Log.d(TAG,"pdfPickIntent:starting pdf pick intent");

        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Pdf"),PDF_PICK_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK){
            if (requestCode==PDF_PICK_CODE){
                Log.d(TAG,"onActivityResult: PDF PICKED");

                pdfUri=data.getData();
                Log.d(TAG,"onActivityResult: URI:"+pdfUri);
            }
        }
        else {
            Log.d(TAG,"onActivityResult: cancelled picking pdf");
            Toast.makeText(this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
    }
}
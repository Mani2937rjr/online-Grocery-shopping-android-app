package com.example.shop2order;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shop2order.databinding.ActivityViewUploadedDocumentsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewUploadedDocumentsActivity extends AppCompatActivity {

    private ActivityViewUploadedDocumentsBinding binding;

    FirebaseAuth firebaseAuth;

    ProgressDialog progressDialog;

    String shopUid,shopName,shopEmail;
    private ArrayList<ModelAPdf> pdfArrayList;
    private AdapterPdf adapterPdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityViewUploadedDocumentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //get uid of the shop from intent
        shopUid=getIntent().getStringExtra("shopUid");
        shopName= getIntent().getStringExtra("shopName");
        shopEmail=getIntent().getStringExtra("shopEmail");
        firebaseAuth=FirebaseAuth.getInstance();

        loadPdfList();

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(ViewUploadedDocumentsActivity.this);
                builder.setCancelable(false);
                builder.setMessage("Please check "+shopName+"'s documents before verfying him")
                        .setPositiveButton("Already Checked", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.setMessage("Verifying "+shopName+" ...");
                                progressDialog.show();


                                //set data
                                HashMap<String,Object> hashMap=new HashMap<>();
                                hashMap.put("verified","true");

                                //update to db
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(shopUid).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                progressDialog.dismiss();

                                                AlertDialog.Builder alertBuilder=new AlertDialog.Builder(ViewUploadedDocumentsActivity.this);
                                                alertBuilder.setTitle("Important")
                                                        .setMessage(shopName+"'s account has been verified, Please confirm through email")
                                                        .setPositiveButton("Email", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                String msg="Hello,\n" +
                                                                        "Congratulations! "+shopName+ "\n"+" You are now our verified shopkeeper"+
                                                                        "\nYour Shop2Order application account linked with "+ shopEmail+" has been verified. Now you can start your " +
                                                                        "product selling. All the best for your business.\n"+
                                                                        "Thanks,\n" +
                                                                        "\n" +
                                                                        "Your Shop2Order team";
                                                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                                                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{shopEmail});
                                                                intent.putExtra(Intent.EXTRA_SUBJECT, "Congratulations! Account Verified");
                                                                intent.putExtra(Intent.EXTRA_TEXT,msg);
                                                                startActivity(intent);
                                                            }
                                                        })
                                                        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.cancel();

                                                            }
                                                        })
                                                        .show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(ViewUploadedDocumentsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();


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
                        adapterPdf=new AdapterPdf(ViewUploadedDocumentsActivity.this,pdfArrayList);
                        binding.bookRv.setAdapter(adapterPdf);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
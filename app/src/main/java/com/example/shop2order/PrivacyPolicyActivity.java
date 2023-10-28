package com.example.shop2order;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.shop2order.activities.MainSellerActivity;
import com.example.shop2order.activities.RegisterSellerMainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private EditText privacyPolicyEt;
    private Button saveBtn,clearBtn,previousBtn;
    private ImageButton backBtn;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        privacyPolicyEt=findViewById(R.id.privacyPolicyEt);
        saveBtn=findViewById(R.id.saveBtn);
        clearBtn=findViewById(R.id.clearBtn);
        backBtn=findViewById(R.id.backBtn);
        previousBtn=findViewById(R.id.previousBtn);

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

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                privacyPolicyEt.setText("");
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //register user
                inputData();
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Privacy");
                ref.orderByChild("privacypolicy").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds:snapshot.getChildren()){
                                    String text1=""+ds.child("pp").getValue();

                                    privacyPolicyEt.setText(text1);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

    }

    private String text;
    private void inputData() {
        text=privacyPolicyEt.getText().toString().trim();

        //validate dada
        if(TextUtils.isEmpty(text)){
            Toast.makeText(this,"Enter privacy policy...",Toast.LENGTH_LONG).show();
            return;
        }



        progressDialog.setMessage("Saving Privacy Policy...");

       /* String timestamp=""+System.currentTimeMillis();

        //setup data to save
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("privacypolicy",""+text);*/

        //save to db
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Privacy");
        ref.child("privacypolicy").child("pp").setValue(text)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        //db updated
                        progressDialog.dismiss();
                        Toast.makeText(PrivacyPolicyActivity.this,"Privacy Policy added successfully...",Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating db
                        progressDialog.dismiss();
                        Toast.makeText(PrivacyPolicyActivity.this,"Privacy Policy not added...",Toast.LENGTH_SHORT).show();


                    }
                });


    }


}
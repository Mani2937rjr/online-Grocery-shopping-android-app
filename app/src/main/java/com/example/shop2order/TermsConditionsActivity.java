package com.example.shop2order;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TermsConditionsActivity extends AppCompatActivity {

    private EditText displayEt;
    private Button saveBtn,clearBtn,previousBtn;
    private ImageButton backBtn;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_conditions);

        displayEt=findViewById(R.id.displayEt);
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
                displayEt.setText("");
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
                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Terms");
                ref.orderByChild("terms").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            String text1=""+ds.child("tc").getValue();

                            displayEt.setText(text1);
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
        text=displayEt.getText().toString().trim();

        //validate dada
        if(TextUtils.isEmpty(text)){
            Toast.makeText(this,"Enter Terms and Conditions...",Toast.LENGTH_LONG).show();
            return;
        }



        progressDialog.setMessage("Saving Terms and Conditions...");


        //save to db
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Terms");
        ref.child("terms").child("tc").setValue(text)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        //db updated
                        progressDialog.dismiss();
                        Toast.makeText(TermsConditionsActivity.this,"Terms and Conditions added successfully...",Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating db
                        progressDialog.dismiss();
                        Toast.makeText(TermsConditionsActivity.this,"Terms and Conditions not added...",Toast.LENGTH_SHORT).show();


                    }
                });


    }

}
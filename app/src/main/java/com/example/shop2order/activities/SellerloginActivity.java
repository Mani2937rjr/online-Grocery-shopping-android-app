package com.example.shop2order.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shop2order.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.regex.Pattern;

public class SellerloginActivity extends AppCompatActivity {
    private EditText emailET,passwordEt;
    private TextView forgotTv,noAccountTv;
    private Button loginBtn;
    private ImageButton backBtn;

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
        setContentView(R.layout.activity_sellerlogin);


        //init UI
        emailET=findViewById(R.id.emailET);
        passwordEt=findViewById(R.id.passwordET);
        forgotTv=findViewById(R.id.forgotTv);
        noAccountTv=findViewById(R.id.noAccountTv);
        loginBtn=findViewById(R.id.loginBtn);
        backBtn=findViewById(R.id.backBtn);


        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SellerloginActivity.this, RegisterSellerMainActivity.class));

            }
        });
        forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SellerloginActivity.this, ForgotPasswordActivity.class));
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });


    }
    private String email,password;

    private void loginUser() {
        email=emailET.getText().toString().trim();
        password=passwordEt.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            emailET.setError("Invalid email pattern...");
            return;
        }
        if(TextUtils.isEmpty(password))
        {
            passwordEt.setError("Enter password...");
            return;
        }
        if(password.length()<6){
            passwordEt.setError("Password must be atleast 6 character long...");
            return;
        }
        if(!PASSWORD_PATTERN.matcher(password).matches()){
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setMessage("Password is weak!")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onBackPressed();
                        }
                    })
                    .setNeutralButton("More Details", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder builder=new AlertDialog.Builder(SellerloginActivity.this);
                            builder.setMessage("Your new password doesn't satisfying this application's password strength rules." +
                                    "You can reset by through email.Do you want to reset password?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startActivity(new Intent(SellerloginActivity.this,ForgotPasswordActivity.class));
                                        }
                                    })
                                    .setNeutralButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog a=builder.create();
                            a.show();
                            return;
                        }
                    });
            AlertDialog alrt=builder.create();
            alrt.show();
            //Toast.makeText(this,"...Password is weak! Set a Strong Password as suggest in Reset Password Email...",Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //logged in successfully
                        makeMeOnline();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed logging in
                        progressDialog.dismiss();
                        AlertDialog.Builder builder=new AlertDialog.Builder(SellerloginActivity.this);
                        builder.setMessage("Invalid Password or Account doesn't exist!")
                                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        onBackPressed();
                                    }
                                });
                        AlertDialog alrt1=builder.create();
                        alrt1.show();
                        //Toast.makeText(SellerloginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }

    private void makeMeOnline() {
        //after logging in, make user online
        progressDialog.setMessage("Checking User...");

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("online","true");

        //update value to db
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //update successfully
                        /*progressDialog.dismiss();
                        startActivity(new Intent(SellerloginActivity.this,MainSellerActivity.class));
                        finish();*/
                        checkUserType();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating
                        progressDialog.dismiss();
                        Toast.makeText(SellerloginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });


    }
    private void checkUserType() {
        //if user is selle,display toast message and return back to home activity
        //if user is buyer,start user main screen

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String accountType = "" + ds.child("accountType").getValue();
                            if (accountType.equals("User")) {

                                //user is seller
                                Toast.makeText(SellerloginActivity.this, "No Shopkeeper account found ", Toast.LENGTH_LONG).show();
                                finish();

                            } else {
                                progressDialog.dismiss();

                                //user is buyer
                                startActivity(new Intent(SellerloginActivity.this, MainSellerActivity.class));
                                finish();
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
package com.example.shop2order.activities.adminactivities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.shop2order.R;

public class AdminloginActivity extends AppCompatActivity {

    private EditText emailET,passwordEt;
    private Button loginBtn;
    private ImageButton backBtn;

    private String Admin_email="shoptwoorder@gmail.com";
    private String Admin_password="admin123";

    String email,password;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminlogin);

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        emailET=findViewById(R.id.emailET);
        passwordEt=findViewById(R.id.passwordET);
        loginBtn=findViewById(R.id.loginBtn);
        backBtn=findViewById(R.id.backBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        String email,password;

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               validate();
            }
        });

    }

    private void validate() {
        email=emailET.getText().toString();
        password=passwordEt.getText().toString();

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
        else{
            loginAdmin();
        }
        progressDialog.setMessage("Logging in...");
        progressDialog.show();
    }

    private void loginAdmin() {
        if(email.equals(Admin_email) && password.equals(Admin_password)){
            progressDialog.dismiss();
            startActivity(new Intent(AdminloginActivity.this, MainAdminActivity.class));
            finish();


        }
        else{
            Toast.makeText(AdminloginActivity.this,"Invalid Admin details,Please check",Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
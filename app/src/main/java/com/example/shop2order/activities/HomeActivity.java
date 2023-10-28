package com.example.shop2order.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.shop2order.HomeAboutUsActivity;
import com.example.shop2order.HomeContactUsActivity;
import com.example.shop2order.HomePrivacyPolicyActivity;
import com.example.shop2order.HomeTermsActivity;
import com.example.shop2order.R;
import com.example.shop2order.activities.adminactivities.AdminloginActivity;

public class HomeActivity extends AppCompatActivity {
    private Button adminbtn,userbtn,shopkeeperbtn;
    private TextView aboutUs,contactUs,privacyPolicy,Terms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        adminbtn=findViewById(R.id.abtn);
        userbtn=findViewById(R.id.ubtn);
        shopkeeperbtn=findViewById(R.id.sbtn);
        aboutUs=findViewById(R.id.aboutUs);
        contactUs=findViewById(R.id.contactUs);
        privacyPolicy=findViewById(R.id.privacyPolicy);
        Terms=findViewById(R.id.Terms);

        userbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            }
        });

        shopkeeperbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, SellerloginActivity.class));
            }
        });

        adminbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, AdminloginActivity.class));
            }
        });

        aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, HomeAboutUsActivity.class));
            }
        });

        contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, HomeContactUsActivity.class));
            }
        });

        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, HomePrivacyPolicyActivity.class));
            }
        });

        Terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, HomeTermsActivity.class));
            }
        });
    }
}
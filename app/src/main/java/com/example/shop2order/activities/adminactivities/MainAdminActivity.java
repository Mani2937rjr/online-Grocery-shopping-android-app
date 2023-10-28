package com.example.shop2order.activities.adminactivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shop2order.AboutUsActivity;
import com.example.shop2order.ContactUsActivity;
import com.example.shop2order.PrivacyPolicyActivity;
import com.example.shop2order.R;
import com.example.shop2order.TermsConditionsActivity;
import com.example.shop2order.activities.HomeActivity;
import com.example.shop2order.adapters.AdapterAdminShop;
import com.example.shop2order.adapters.AdapterAdminUser;
import com.example.shop2order.models.ModelAdminShop;
import com.example.shop2order.models.ModelAdminUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainAdminActivity extends AppCompatActivity {

    private ImageButton logoutBtn,moreBtn;
    private TextView tabShopsTv,tabUsersTv;
    private RelativeLayout shopsRl,UsersRl;
    private RecyclerView shopsRv,usersRv;
    private EditText searchUserEt,searchShopEt;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    //for shop list
    private ArrayList<ModelAdminShop> AdminShopList ;
    private AdapterAdminShop adapterAdminShop;

    private ArrayList<ModelAdminUser> AdminUserList;
    private AdapterAdminUser adapterAdminUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        logoutBtn=findViewById(R.id.logoutBtn);
        tabShopsTv=findViewById(R.id.tabShopsTv);
        tabUsersTv=findViewById(R.id.tabUsersTv);
        shopsRl=findViewById(R.id.shopsRl);
        UsersRl=findViewById(R.id.UsersRl);
        shopsRv=findViewById(R.id.shopsRv);
        usersRv=findViewById(R.id.usersRv);
        searchUserEt=findViewById(R.id.searchUserEt);
        searchShopEt=findViewById(R.id.searchShopEt);
        moreBtn=findViewById(R.id.moreBtn);

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth=FirebaseAuth.getInstance();

        //at first show ui
        showShopsUI();


        //search user
        searchUserEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterAdminUser.getFilter().filter(s);

                }
                catch (Exception e){
                    e.printStackTrace();

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchShopEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterAdminShop.getFilter().filter(s);

                }
                catch (Exception e){
                    e.printStackTrace();

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Logging out...");
                progressDialog.show();

                startActivity(new Intent(MainAdminActivity.this, HomeActivity.class));

            }
        });

        //click shopkeeper option
        tabShopsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show shops
                showShopsUI();

            }
        });

        //click Users option
        tabUsersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show users
                showUsersUI();
            }
        });

        //popup menu
        PopupMenu popupMenu=new PopupMenu(MainAdminActivity.this,moreBtn);
        //add menu items to our menu
        popupMenu.getMenu().add("Add About Us");
        popupMenu.getMenu().add("Add Contact Us");
        popupMenu.getMenu().add("Add Privacy Policy");
        popupMenu.getMenu().add("Add Terms & Conditions");
        //handle menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle()=="Add About Us"){
                    startActivity(new Intent(MainAdminActivity.this, AboutUsActivity.class));

                }
                else if (item.getTitle()=="Add Contact Us"){
                    startActivity(new Intent(MainAdminActivity.this, ContactUsActivity.class));

                }
               else if (item.getTitle()=="Add Privacy Policy"){
                    startActivity(new Intent(MainAdminActivity.this, PrivacyPolicyActivity.class));

                }
                else if (item.getTitle()=="Add Terms & Conditions"){
                    startActivity(new Intent(MainAdminActivity.this, TermsConditionsActivity.class));

                }
                return true;
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show popup menu
                popupMenu.show();

            }
        });


    }

    private void showShopsUI() {

        //show shops ui,hide users ui
        shopsRl.setVisibility(View.VISIBLE);
        UsersRl.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabUsersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabUsersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        //load shop lists
        loadShops();
    }

    private void showUsersUI() {
        //show users ui,hide shops ui
        shopsRl.setVisibility(View.GONE);
        UsersRl.setVisibility(View.VISIBLE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabUsersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabUsersTv.setBackgroundResource(R.drawable.shape_rect04);

        loadUsers();
    }

    private void loadUsers() {
        AdminUserList=new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("User")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding
                        AdminUserList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelAdminUser modelAdminUser=ds.getValue(ModelAdminUser.class);

                            //display all user
                            AdminUserList.add(modelAdminUser);
                        }

                        //setup adapter
                        adapterAdminUser=new AdapterAdminUser(MainAdminActivity.this,AdminUserList);
                        //set adapter to recyleview
                        usersRv.setAdapter(adapterAdminUser);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShops() {
        //init list
        AdminShopList=new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding
                        AdminShopList.clear();
                        for(DataSnapshot ds:snapshot.getChildren()){
                           ModelAdminShop modelAdminShop=ds.getValue(ModelAdminShop.class);

                            //if you want to display all shops,skip the if statement and add this
                            AdminShopList.add(modelAdminShop);
                        }
                        //setup adapter
                        adapterAdminShop=new AdapterAdminShop(MainAdminActivity.this,AdminShopList);
                        //set adapter to recyleview
                        shopsRv.setAdapter(adapterAdminShop);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }



}
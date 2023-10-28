package com.example.shop2order.activities.adminactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.shop2order.R;
import com.example.shop2order.activities.MainSellerActivity;
import com.example.shop2order.activities.MainUserActivity;
import com.example.shop2order.adapters.AdapterAdminOrderUser;
import com.example.shop2order.adapters.AdapterOrderShop;
import com.example.shop2order.adapters.AdapterOrderUser;
import com.example.shop2order.models.ModelAdminOrderUser;
import com.example.shop2order.models.ModelOrderShop;
import com.example.shop2order.models.ModelOrderUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdminUserOrderListActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private RecyclerView orderRv;
    private ImageButton filterOrderBtn;
    private TextView filteredOrdersTv;


    private String userUid;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelAdminOrderUser>  AdminOrderUserArrayList;
    private AdapterAdminOrderUser adapterAdminOrderUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_order_list);

        backBtn=findViewById(R.id.backBtn);
        orderRv=findViewById(R.id.orderRv);
        filterOrderBtn=findViewById(R.id.filterOrderBtn);
        filteredOrdersTv=findViewById(R.id.filteredOrdersTv);

        userUid=getIntent().getStringExtra("userUid");

        firebaseAuth=FirebaseAuth.getInstance();

        loadMyInfo();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //option display in dialog
                String[] options={"All","In Progress","Completed","Cancelled","Order Cancelled"};
                //dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(AdminUserOrderListActivity.this);
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //handle item clicks
                                if (which==0){
                                    //All clicked
                                    filteredOrdersTv.setText("Showing All Orders");
                                    adapterAdminOrderUser.getFilter().filter("");//show all orders
                                }
                                else{
                                    String optionClicked=options[which];
                                    filteredOrdersTv.setText("Showing "+optionClicked+" Orders");
                                    adapterAdminOrderUser.getFilter().filter(optionClicked);
                                }

                            }
                        })
                        .show();

            }
        });
    }

    private void loadMyInfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(userUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            //get user data
                            String name=""+ds.child("name").getValue();
                            String email=""+ds.child("email").getValue();
                            String phone=""+ds.child("phone").getValue();
                            String profileImage=""+ds.child("profileImage").getValue();
                            String accountType=""+ds.child("accountType").getValue();
                            String city=""+ds.child("city").getValue();



                            loadOrders();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadOrders() {
        //init order list
        AdminOrderUserArrayList=new ArrayList<>();
        //get orders
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AdminOrderUserArrayList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    String uid=""+ds.getRef().getKey();

                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(userUid)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        for (DataSnapshot ds:snapshot.getChildren()){
                                            ModelAdminOrderUser modelAdminOrderUser=ds.getValue(ModelAdminOrderUser.class);

                                            //add to list
                                            AdminOrderUserArrayList.add(modelAdminOrderUser);
                                        }
                                        //setup adapter
                                        adapterAdminOrderUser=new AdapterAdminOrderUser(AdminUserOrderListActivity.this,AdminOrderUserArrayList);
                                        ////set to recycleview
                                        orderRv.setAdapter(adapterAdminOrderUser);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
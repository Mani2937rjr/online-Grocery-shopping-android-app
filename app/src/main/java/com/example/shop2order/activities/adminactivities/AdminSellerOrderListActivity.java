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
import com.example.shop2order.adapters.AdapterAdminOrderSeller;
import com.example.shop2order.adapters.AdapterAdminOrderUser;
import com.example.shop2order.adapters.AdapterOrderShop;
import com.example.shop2order.models.ModelAdminOrderSeller;
import com.example.shop2order.models.ModelAdminOrderUser;
import com.example.shop2order.models.ModelOrderShop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminSellerOrderListActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private RecyclerView orderRv;
    private ImageButton filterOrderBtn;
    private TextView filteredOrdersTv;

    private String shopUid;

    private ArrayList<ModelAdminOrderSeller> AdminOrderSellerArrayList ;
    private AdapterAdminOrderSeller adapterAdminOrderSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_seller_order_list);

        backBtn=findViewById(R.id.backBtn);
        orderRv=findViewById(R.id.orderRv);
        filterOrderBtn=findViewById(R.id.filterOrderBtn);
        filteredOrdersTv=findViewById(R.id.filteredOrdersTv);

        //getting uid from adminshopdeatils activity
        shopUid=getIntent().getStringExtra("shopUid");

       loadAllOrders();

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
                AlertDialog.Builder builder=new AlertDialog.Builder(AdminSellerOrderListActivity.this);
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //handle item clicks
                                if (which==0){
                                    //All clicked
                                    filteredOrdersTv.setText("Showing All Orders");
                                    adapterAdminOrderSeller.getFilter().filter("");//show all orders
                                }
                                else{
                                    String optionClicked=options[which];
                                    filteredOrdersTv.setText("Showing "+optionClicked+" Orders");
                                    adapterAdminOrderSeller.getFilter().filter(optionClicked);
                                }

                            }
                        })
                        .show();
            }
        });
    }

    private void loadAllOrders() {
        //init array list
        AdminOrderSellerArrayList=new ArrayList<>();

        //load orders of shop
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding new data in it
                        AdminOrderSellerArrayList.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            ModelAdminOrderSeller modelAdminOrderSeller=ds.getValue(ModelAdminOrderSeller.class);
                            //add to list
                            AdminOrderSellerArrayList.add(modelAdminOrderSeller);
                        }
                        //setup adapter
                        adapterAdminOrderSeller=new AdapterAdminOrderSeller(AdminSellerOrderListActivity.this,AdminOrderSellerArrayList);
                        //set adapter to recycler view
                        orderRv.setAdapter(adapterAdminOrderSeller);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}
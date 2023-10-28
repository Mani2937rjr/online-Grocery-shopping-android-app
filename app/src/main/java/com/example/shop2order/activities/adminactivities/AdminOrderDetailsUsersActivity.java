package com.example.shop2order.activities.adminactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.shop2order.R;
import com.example.shop2order.activities.OrderDetailsUsersActivity;
import com.example.shop2order.activities.WriteReviewActivity;
import com.example.shop2order.adapters.AdapterOrderedItem;
import com.example.shop2order.models.ModelOrderedItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminOrderDetailsUsersActivity extends AppCompatActivity {

    private String orderTo,orderId;

    //ui views
    private ImageButton backBtn;
    private TextView orderIdTv,dateTv,orderStatusTv,shopNameTv,totalItemsTv,amountTv,addressTv,deliveryDateTv
            ,shopEmailTv,shopPhoneTv,shopAddressTv;
    private RecyclerView itemsRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_details_users);

        //init views
        backBtn=findViewById(R.id.backBtn);
        orderIdTv=findViewById(R.id.orderIdTv);
        dateTv=findViewById(R.id.dateTv);
        orderStatusTv=findViewById(R.id.orderStatusTv);
        shopNameTv=findViewById(R.id.shopNameTv);
        totalItemsTv=findViewById(R.id.totalItemsTv);
        amountTv=findViewById(R.id.amountTv);
        addressTv=findViewById(R.id.addressTv);
        itemsRv=findViewById(R.id.itemsRv);
        deliveryDateTv=findViewById(R.id.deliveryDateTv);
        shopEmailTv=findViewById(R.id.shopEmailTv);
        shopPhoneTv=findViewById(R.id.shopPhoneTv);
        shopAddressTv=findViewById(R.id.shopAddressTv);

        Intent intent=getIntent();
        orderTo=intent.getStringExtra("orderTo"); //orderTo contain uid of the  shop where we placed order
        orderId=intent.getStringExtra("orderId");

        firebaseAuth=FirebaseAuth.getInstance();
        loadShopInfo();
        loadOrderDetails();
        loadOrderedItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

    }

    private void loadShopInfo() {
        //get shop info

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName=""+snapshot.child("shopName").getValue();
                        String shopEmail=""+snapshot.child("email").getValue();
                        String shopPhone=""+snapshot.child("phone").getValue();
                        String shopAddress=""+snapshot.child("address").getValue();

                        shopNameTv.setText(shopName);
                        shopEmailTv.setText(shopEmail);
                        shopPhoneTv.setText(shopPhone);
                        shopAddressTv.setText(shopAddress);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    private void loadOrderDetails() {
        //load order details
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String orderBy=""+snapshot.child("orderBy").getValue();
                        String orderCost=""+snapshot.child("orderCost").getValue();
                        String orderId=""+snapshot.child("orderId").getValue();
                        String orderStatus=""+snapshot.child("orderStatus").getValue();
                        String orderTime=""+snapshot.child("orderTime").getValue();
                        String orderTo=""+snapshot.child("orderTo").getValue();
                        String deliveryFee=""+snapshot.child("deliveryFee").getValue();
                        String latitude=""+snapshot.child("latitude").getValue();
                        String longitude=""+snapshot.child("longitude").getValue();
                        String deliveryDate=""+snapshot.child("deliveryDate").getValue();

                        //convert tomestamp to proper format
                        Calendar calendar=Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatedDate= DateFormat.format("dd/MM/yyyy hh:mm a",calendar).toString(); //e,g. 21/06/2022 10:16 AM


                        if (orderStatus.equals("In Progress")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (orderStatus.equals("Completed")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));

                        }
                        else if (orderStatus.equals("Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                        }

                        else if (orderStatus.equals("Order Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.darkPink));

                        }


                        //set data
                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);
                        amountTv.setText("₹"+orderCost+"[Including delivery fee ₹"+deliveryFee+"]");
                        dateTv.setText(formatedDate);
                        deliveryDateTv.setText(deliveryDate);
                        findAddress(latitude,longitude);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void findAddress(String latitude, String longitude) {
        double lat=Double.parseDouble(latitude);
        double lon=Double.parseDouble(longitude);

        //find address,state,city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder=new Geocoder(this, Locale.getDefault());

        try {
            addresses=geocoder.getFromLocation(lat,lon,1);

            String address=addresses.get(0).getAddressLine(0); //Completed address
            addressTv.setText(address);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadOrderedItems() {
        //init list
        orderedItemArrayList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderedItemArrayList.clear(); //before loading items clear list
                        for (DataSnapshot ds:snapshot.getChildren()){
                            ModelOrderedItem modelOrderedItem=ds.getValue(ModelOrderedItem.class);
                            //add to list
                            orderedItemArrayList.add(modelOrderedItem);
                        }

                        //allitems added to list
                        //setup adapter
                        adapterOrderedItem=new AdapterOrderedItem(AdminOrderDetailsUsersActivity.this,orderedItemArrayList);
                        //set adapter
                        itemsRv.setAdapter(adapterOrderedItem);

                        //set items count
                        totalItemsTv.setText(""+snapshot.getChildrenCount());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
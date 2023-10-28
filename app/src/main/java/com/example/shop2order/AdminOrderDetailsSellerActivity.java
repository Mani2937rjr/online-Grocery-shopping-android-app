package com.example.shop2order;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class AdminOrderDetailsSellerActivity extends AppCompatActivity {

    //ui views
    private ImageButton backBtn,mapBtn;
    private TextView orderIdTv,dateTv,orderStatusTv,emailTv,nameTv,phoneTv,totalItemsTv,deliveryDateTv,
            amountTv,addressTv;
    private RecyclerView itemsRv;

    String orderId,orderBy,orderTo;

    //to open destination in map
    String destinationLatitude,destinationLongitude;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_details_seller2);

        //init ui views
        backBtn=findViewById(R.id.backBtn);
        mapBtn=findViewById(R.id.mapBtn);
        orderIdTv=findViewById(R.id.orderIdTv);
        dateTv=findViewById(R.id.dateTv);
        orderStatusTv=findViewById(R.id.orderStatusTv);
        emailTv=findViewById(R.id.emailTv);
        nameTv=findViewById(R.id.nameTv);
        phoneTv=findViewById(R.id.phoneTv);
        totalItemsTv=findViewById(R.id.totalItemsTv);
        amountTv=findViewById(R.id.amountTv);
        addressTv=findViewById(R.id.addressTv);
        itemsRv=findViewById(R.id.itemsRv);
        deliveryDateTv=findViewById(R.id.deliveryDateTv);

        //get data from intent
        Intent intent=getIntent();
        orderId=intent.getStringExtra("orderId");
        orderBy=intent.getStringExtra("orderBy");
        orderTo=intent.getStringExtra("orderTo");

        firebaseAuth=FirebaseAuth.getInstance();

        loadBuyerInfo();
        loadOrderDetails();
        loadOrderedItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(AdminOrderDetailsSellerActivity.this);
                builder.setMessage("Do you want to see buyer's location?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                openMap();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();

            }
        });

    }

    private void loadOrderedItems() {
        //load the products/items of  order

        //init list
        orderedItemArrayList=new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderedItemArrayList.clear(); //before adding data clear list
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelOrderedItem modelOrderedItem=ds.getValue(ModelOrderedItem.class);

                            //add to list
                            orderedItemArrayList.add(modelOrderedItem);
                        }
                        //setup adapter
                        adapterOrderedItem=new AdapterOrderedItem(AdminOrderDetailsSellerActivity.this,orderedItemArrayList);

                        //set adapter to our  recyclerview
                        itemsRv.setAdapter(adapterOrderedItem);

                        //set total number of items/products in order
                        totalItemsTv.setText(""+snapshot.getChildrenCount());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBuyerInfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get buyer info
                        destinationLatitude =""+snapshot.child("latitude").getValue();
                        destinationLongitude =""+snapshot.child("longitude").getValue();
                        String email=""+snapshot.child("email").getValue();
                        String phone=""+snapshot.child("phone").getValue();
                        String name=""+snapshot.child("name").getValue();

                        //set info
                        if(email.equals("null")||phone.equals("null")||name.equals("null")){
                            emailTv.setText("NA");
                            phoneTv.setText("NA");
                            nameTv.setText("NA");
                            mapBtn.setEnabled(false);
                            mapBtn.setColorFilter(R.color.colorGray00);
                        }
                        else{
                            emailTv.setText(email);
                            phoneTv.setText(phone);
                            nameTv.setText(name);
                            mapBtn.setEnabled(true);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        //load detailed info of this order,based on order id
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get order info
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

                        //convert timestamp
                        Calendar calendar= Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String dateFormated= DateFormat.format("dd/MM/yyyy",calendar).toString();

                        //order status
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
                        amountTv.setText("₹"+orderCost+ " [Including delivery fee ₹"+deliveryFee+"]");
                        dateTv.setText(dateFormated);
                        deliveryDateTv.setText(deliveryDate);

                        // findAddress(latitude,longitude); //to find delivery address
                        findAddress(latitude,longitude);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {
        double lat= Double.parseDouble(latitude);
        double lon= Double.parseDouble(longitude);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder=new Geocoder(this, Locale.getDefault());

        try {
            addresses=geocoder.getFromLocation(lat,lon,1);

            //complete address
            String address=addresses.get(0).getAddressLine(0);
            addressTv.setText(address);
        } catch (Exception e) {
            Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }


    private void openMap() {
        //daddr means destination address
        String address="https://maps.google.com/maps?&daddr=" +destinationLatitude+","+destinationLongitude;
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);

    }
}
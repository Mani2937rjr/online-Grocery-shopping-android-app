package com.example.shop2order.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.shop2order.Constants;
import com.example.shop2order.R;
import com.example.shop2order.adapters.AdapterOrderedItem;
import com.example.shop2order.models.ModelOrderedItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetailsUsersActivity extends AppCompatActivity {
    private String orderTo,orderId ;

    //ui views
    private ImageButton backBtn,writeReviewBtn,cancelOrderBtn;
    private TextView orderIdTv,dateTv,orderStatusTv,shopNameTv,totalItemsTv,amountTv,addressTv,deliveryDateTv;
    private RecyclerView itemsRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_users);

        //init views
        backBtn=findViewById(R.id.backBtn);
        writeReviewBtn=findViewById(R.id.writeReviewBtn);
        orderIdTv=findViewById(R.id.orderIdTv);
        dateTv=findViewById(R.id.dateTv);
        orderStatusTv=findViewById(R.id.orderStatusTv);
        shopNameTv=findViewById(R.id.shopNameTv);
        totalItemsTv=findViewById(R.id.totalItemsTv);
        amountTv=findViewById(R.id.amountTv);
        addressTv=findViewById(R.id.addressTv);
        itemsRv=findViewById(R.id.itemsRv);
        cancelOrderBtn=findViewById(R.id.cancelOrderBtn);
        deliveryDateTv=findViewById(R.id.deliveryDateTv);


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

        //handle writeReviewBtn click,start write activity
        writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1= new Intent(OrderDetailsUsersActivity.this,WriteReviewActivity.class);
                intent1.putExtra("shopUid",orderTo);    //to write review to a shop we must have uid of shop
                startActivity(intent1);
            }
        });

            cancelOrderBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //edit order status:In Progress,Completed,Cancelled
                    OrderStatusDialog();
                }
            });

    }

    String selectedOption="";
    private void OrderStatusDialog() {
        //open to display in dialog
        //String[] options={"Cancel"};
        //dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("Do you want to cancel the order?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedOption="Order Cancelled";
                        OrderStatus(selectedOption);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
            .setNeutralButton("More Details", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(OrderDetailsUsersActivity.this,"If you cancel this order it will be permanently cancelled.",Toast.LENGTH_LONG).show();
                }
            });
        AlertDialog alert=builder.create();
        alert.show();

    }

    private void OrderStatus(String selectedOption) {
        //setup data to put in db
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("orderStatus",""+selectedOption);

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        String message ="Your "+selectedOption+" by the customer now";
                        //status updated
                        Toast.makeText(OrderDetailsUsersActivity.this,message,Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating status,show reason
                        Toast.makeText(OrderDetailsUsersActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

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
                        shopNameTv.setText(shopName);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        //load prder details
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
                            cancelOrderBtn.setEnabled(false);
                            cancelOrderBtn.setColorFilter(getColor(R.color.lightBlack));
                        }
                        else if (orderStatus.equals("Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                            cancelOrderBtn.setEnabled(false);
                            cancelOrderBtn.setColorFilter(getColor(R.color.lightBlack));
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
                        adapterOrderedItem=new AdapterOrderedItem(OrderDetailsUsersActivity.this,orderedItemArrayList);
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
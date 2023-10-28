package com.example.shop2order.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.shop2order.Constants;
import com.example.shop2order.R;
import com.example.shop2order.adapters.AdapterCartItem;
import com.example.shop2order.adapters.AdapterProductUser;
import com.example.shop2order.adapters.AdapterReview;
import com.example.shop2order.models.ModelCartItem;
import com.example.shop2order.models.ModelProduct;
import com.example.shop2order.models.ModelReview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    //declare ui views
    private ImageView shopIv;
    private TextView shopNameTv,phoneTv,emailTv,openCloseTv,deliveryFeeTv,addressTv,filteredProductsTv,cartCountTv;
    private ImageButton callBtn,mapBtn,cartBtn,backBtn,filterProductBtn,reviewsBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    private RatingBar ratingBar;

     String shopUid;
    private String myLatitude,myLongitude, myPhone;
    private String shopName,shopEmail,shopPhone,shopAddress,shopLatitude,shopLongitude;
    public String deliveryFee;

    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;

    //cart
    private ArrayList<ModelCartItem> cartItemList ;
    private AdapterCartItem adapterCartItem;

    private EasyDB easyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        //init ui views
        shopIv=findViewById(R.id.shopIv);
        shopNameTv=findViewById(R.id.shopNameTv);
        phoneTv=findViewById(R.id.phoneTv);
        emailTv=findViewById(R.id.emailTv);
        filteredProductsTv=findViewById(R.id.filteredProductsTv);
        openCloseTv=findViewById(R.id.openCloseTv);
        deliveryFeeTv=findViewById(R.id.deliveryFeeTv);
        addressTv=findViewById(R.id.addressTv);
        cartCountTv=findViewById(R.id.cartCountTv);
        callBtn=findViewById(R.id.callBtn);
        mapBtn=findViewById(R.id.mapBtn);
        cartBtn=findViewById(R.id.cartBtn);
        backBtn=findViewById(R.id.backBtn);
        filterProductBtn=findViewById(R.id.filterProductBtn);
        searchProductEt=findViewById(R.id.searchProductEt);
        productsRv=findViewById(R.id.productsRv);
        reviewsBtn=findViewById(R.id.reviewsBtn);
        ratingBar=findViewById(R.id.ratingBar);

        //init progress dialog
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //get uid of the shop from intent
        shopUid=getIntent().getStringExtra("shopUid");

        firebaseAuth=FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews();  //avg review set on ratingbar

        //declare it to class level and init in onCreate
        easyDB = EasyDB.init(this, "ITEM_DB")
                .setTableName("ITEM_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        //Each shop have its own products and orders so if user add items to cart and go back and open cart in different shop
        //then cart should be different
        //so delete cart data whenever user open this activity
        deleteCartData();
        cartCount();

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    adapterProductUser.getFilter().filter(s);

                }
                catch (Exception e){
                    e.printStackTrace();

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go previous activity
                onBackPressed();
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show cart dialoag

                showCartDialog();


            }
        });
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose category:")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected item
                                String selected=Constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("ALL")){
                                    //load all
                                    loadShopProducts();
                                }
                                else{
                                    //load filtered
                                    adapterProductUser.getFilter().filter(selected);
                                }
                            }
                        })
                        .show();
            }
        });

        //handle reviewsBtn click, open reviews activity
        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass shop uid to show it's reviews
                Intent intent=new Intent(ShopDetailsActivity.this,ShopReviewsActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });



    }

    private float ratingSum=0;
    private void loadReviews() {

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding
                        ratingSum=0;
                        for (DataSnapshot ds:snapshot.getChildren()){
                            float rating=Float.parseFloat(""+ds.child("ratings").getValue()); //e.g 4.3

                            ratingSum=ratingSum+rating; //for av rating, add (addition of) all ratings,later willdivide it by number of reviews


                        }

                        long numberOfReviews=snapshot.getChildrenCount();
                        float avgRating=ratingSum/numberOfReviews;

                        ratingBar.setRating(avgRating);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void deleteCartData() {
        easyDB.deleteAllDataFromTable();    //delete all records from cart

    }
    public void cartCount(){
        //keep it public so we can access in adapter
        //get cart count
        int count=easyDB.getAllData().getCount();
        if (count<=0){
            //no item in cart,hide cart count textview
            cartCountTv.setVisibility(View.GONE);

        }
        else{
            //have items in cart,show cart count textview and set count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText(""+count);//concatenate with string,because we can't set integer in textview
        }
    }

    public double allTotalPrice=0.00;
    //need to access these views in adapter so making public
    public TextView sTotalTv,dFeeTv,allTotalPriceTv;
    private void showCartDialog() {
        //init list
        cartItemList=new ArrayList<>();
        //inflate cart layout
        View view= LayoutInflater.from(this).inflate(R.layout.dialog_cart,null);
        //init views
        TextView shopNameTv=view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemsRv=view.findViewById(R.id.cartItemsRv);
        sTotalTv=view.findViewById(R.id.sTotalTv);
        dFeeTv=view.findViewById(R.id.dFeeTv);
        allTotalPriceTv=view.findViewById(R.id.totalTv);
        Button checkoutBtn=view.findViewById(R.id.checkoutBtn);

        //dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        //set views to dialog
        builder.setView(view);

        shopNameTv.setText(shopName);

        EasyDB easyDB = EasyDB.init(this, "ITEM_DB")
                .setTableName("ITEM_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        //get all records from db
        Cursor res=easyDB.getAllData();
        while (res.moveToNext()){
            String id=res.getString(1);
            String pId=res.getString(2);
            String name=res.getString(3);
            String price=res.getString(4);
            String cost=res.getString(5);
            String quantity=res.getString(6);

            allTotalPrice=allTotalPrice+Double.parseDouble(cost);

            ModelCartItem modelCartItem=new ModelCartItem(""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity);

            cartItemList.add(modelCartItem);

        }

        //setup adapter
        adapterCartItem=new AdapterCartItem(this,cartItemList);
        //set to recyleview
        cartItemsRv.setAdapter(adapterCartItem);

        dFeeTv.setText("₹"+deliveryFee);
        sTotalTv.setText("₹"+String.format("%.2f",allTotalPrice));
        allTotalPriceTv.setText("₹"+String.format("%.2f",(allTotalPrice+Double.parseDouble(deliveryFee.replace("₹","")))));

        //show dialog
        AlertDialog dialog=builder.create();
        dialog.show();

        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice=0.00;
            }
        });

        //place order
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first validate delivery address
                if(myLatitude.equals("")||myLatitude.equals("null")||myLongitude.equals("")||myLongitude.equals("null")){
                    //user didn't enter address in profile
                    Toast.makeText(ShopDetailsActivity.this,"Please enter your address in your profile before placing order...",Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                if(myPhone.equals("")||myPhone.equals("null")){
                    //user didn't enter phone number in profile
                    Toast.makeText(ShopDetailsActivity.this,"Please enter your phone number in your profile before placing order...",Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                if (cartItemList.size()==0){
                    //cart list is empty
                    Toast.makeText(ShopDetailsActivity.this,"No item in cart...",Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                submitOrder();
            }
        });
    }

    private void submitOrder() {
        //show progress dialog
        progressDialog.setMessage("Placing order...");
        progressDialog.show();

        //for order id and order time
        String timestamp=""+System.currentTimeMillis();

        String cost=allTotalPriceTv.getText().toString().trim().replace("₹",""); //remove ₹ if contains

        //add latitude,longitude of user to each other|delete previous orders from firebase or add manually to them


        //setup order data
        HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put("orderId",""+timestamp);
        hashMap.put("orderTime",""+timestamp);
        hashMap.put("orderStatus","In Progress"); //In Progress/completed/cancelled
        hashMap.put("orderCost",""+cost);
        hashMap.put("deliveryFee",""+deliveryFee);
        hashMap.put("orderBy",""+firebaseAuth.getUid());
        hashMap.put("orderTo",""+shopUid);
        hashMap.put("latitude",""+myLatitude);
        hashMap.put("longitude",""+myLongitude);
        hashMap.put("deliveryDate","");////


        //add to db
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //order info added now add order items
                        for (int i=0;i<cartItemList.size();i++){
                            String pId=cartItemList.get(i).getpId();
                            String id=cartItemList.get(i).getId();
                            String name=cartItemList.get(i).getName();
                            String cost=cartItemList.get(i).getCost();
                            String price=cartItemList.get(i).getPrice();
                            String quantity=cartItemList.get(i).getQuantity();

                            HashMap<String,String> hashMap1=new HashMap<>();
                            hashMap1.put("pId",pId);
                            hashMap1.put("name",name);
                            hashMap1.put("cost",cost);
                            hashMap1.put("price",price);
                            hashMap1.put("quantity",quantity);


                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);

                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this,"Order Placed Successfully...",Toast.LENGTH_SHORT).show();
                        easyDB.deleteAllDataFromTable();
                        cartCountTv.setVisibility(View.GONE);
                        cartCountTv.setText("");
                        prepareNotificationMessage(timestamp);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed placing order
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });


    }

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String address="https://maps.google.com/maps?saddr="+myLatitude+","+myLongitude+"&daddr=" +shopLatitude+","+shopLongitude;
        Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this,""+shopPhone,Toast.LENGTH_SHORT).show();
    }

    private void loadMyInfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            //get user data
                            String name=""+ds.child("name").getValue();
                            String email=""+ds.child("email").getValue();
                            myPhone=""+ds.child("phone").getValue();
                            String profileImage=""+ds.child("profileImage").getValue();
                            String accountType=""+ds.child("accountType").getValue();
                            String city=""+ds.child("city").getValue();
                             myLatitude=""+ds.child("latitude").getValue();
                             myLongitude=""+ds.child("longitude").getValue();


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadShopDetails() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get shop data
                String name=""+snapshot.child("name").getValue();
                shopName=""+snapshot.child("shopName").getValue();
                shopEmail=""+snapshot.child("email").getValue();
                shopPhone=""+snapshot.child("phone").getValue();
                shopAddress=""+snapshot.child("address").getValue();
                shopLatitude=""+snapshot.child("latitude").getValue();
                shopLongitude=""+snapshot.child("longitude").getValue();
                deliveryFee=""+snapshot.child("deliveryFee").getValue();
                String profileImage=""+snapshot.child("profileImage").getValue();
                String shopOpen=""+snapshot.child("shopOpen").getValue();

                //set data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("Delivery Fee: ₹"+deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);

                if(shopOpen.equals("true")){
                    openCloseTv.setText("Open");
                }
                else{
                    openCloseTv.setText("Closed");
                }
                try {
                    Picasso.get().load(profileImage).into(shopIv);

                } catch (Exception e) {
                    e.printStackTrace();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadShopProducts() {
        //init list
        productsList=new ArrayList<>();

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding items
                        productsList.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            String blocked=""+ds.child("blocked").getValue();
                            ModelProduct modelProduct=ds.getValue(ModelProduct.class);

                            if (blocked.equals("false")) {
                                productsList.add(modelProduct);
                            }

                        }
                        //setup adapater
                        adapterProductUser=new AdapterProductUser(ShopDetailsActivity.this,productsList);
                        //set adapter
                        productsRv.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void prepareNotificationMessage(String orderId){
        //When user places order, send notification to seller

        //prepare data for notification
        String NOTIFICATION_TOPIC= "/topics/"+ Constants.FCM_TOPIC; //must be same as subscribed by user
        String NOTIFICATION_TITLE= "New Order " + orderId;
        String NOTIFICATION_MESSAGE = "Congratulations...! You have new order.";
        String NOTIFICATION_TYPE = "NewOrder";

        //prepare json(what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            //Since we are logged in as buyer to place order so current user uid is buyer uid
            notificationBodyJo.put("buyerUid", firebaseAuth.getUid());
            notificationBodyJo.put("sellerUid",shopUid);
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);
            //where to send
            notificationJo.put("to",NOTIFICATION_TOPIC); //to all who subscribed to this topic
            notificationJo.put("data",notificationBodyJo);

        } catch (Exception e) {
            Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

        }

        sendFcmNotification(notificationJo,orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, String orderId) {
        //send volley request
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after sending fcm start order details activity
                Intent intent=new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent); //now get these values through intent on OrderDetailsUsersActivity


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if failed sending fcm, still start order details activity
                Intent intent=new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent);

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                //put request headers
                Map<String,String> headers=new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization","key="+Constants.FCM_KEY);
                return headers;
            }
        };
        //enque the volley  request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }


}
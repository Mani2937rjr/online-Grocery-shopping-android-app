package com.example.shop2order.activities.adminactivities;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import com.example.shop2order.ViewUploadedDocumentsActivity;
import com.example.shop2order.activities.MainSellerActivity;
import com.example.shop2order.activities.OrderDetailsUsersActivity;
import com.example.shop2order.activities.SettingsActivity;
import com.example.shop2order.activities.ShopDetailsActivity;
import com.example.shop2order.activities.ShopReviewsActivity;
import com.example.shop2order.adapters.AdapterAdminProductSeller;
import com.example.shop2order.adapters.AdapterCartItem;
import com.example.shop2order.adapters.AdapterProductUser;
import com.example.shop2order.models.ModelCartItem;
import com.example.shop2order.models.ModelProduct;
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

public class AdminShopDetailsActivity extends AppCompatActivity {

    //declare ui views
    private ImageView shopIv;
    private TextView shopNameTv,phoneTv,emailTv,openCloseTv,deliveryFeeTv,addressTv,filteredProductsTv;
    private ImageButton callBtn,mapBtn,moreBtn,backBtn,filterProductBtn,verifyBtn,removeVerificationBtn,emailBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    private RatingBar ratingBar;

    String shopUid;
    private String shopName,shopEmail,shopPhone,shopAddress,shopLatitude,shopLongitude,shopDeleted,verified;
    public String deliveryFee;

    public String deleted;

    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productsList;
    private AdapterAdminProductSeller adapterAdminProductSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_shop_details);


        //init ui views
        shopIv=findViewById(R.id.shopIv);
        shopNameTv=findViewById(R.id.shopNameTv);
        phoneTv=findViewById(R.id.phoneTv);
        emailTv=findViewById(R.id.emailTv);
        filteredProductsTv=findViewById(R.id.filteredProductsTv);
        openCloseTv=findViewById(R.id.openCloseTv);
        deliveryFeeTv=findViewById(R.id.deliveryFeeTv);
        addressTv=findViewById(R.id.addressTv);
        callBtn=findViewById(R.id.callBtn);
        mapBtn=findViewById(R.id.mapBtn);
        moreBtn=findViewById(R.id.moreBtn);
        backBtn=findViewById(R.id.backBtn);
        filterProductBtn=findViewById(R.id.filterProductBtn);
        searchProductEt=findViewById(R.id.searchProductEt);
        productsRv=findViewById(R.id.productsRv);
        verifyBtn=findViewById(R.id.verifyBtn);
        removeVerificationBtn=findViewById(R.id.removeVerificationBtn);
        ratingBar=findViewById(R.id.ratingBar);
        emailBtn=findViewById(R.id.emailBtn);

        //init progress dialog
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //get uid of the shop from intent
        shopUid=getIntent().getStringExtra("shopUid");

        firebaseAuth=FirebaseAuth.getInstance();
        loadShopDetails();
        loadShopProducts();
        loadReviews();  //avg review set on ratingbar

        removeVerificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder=new AlertDialog.Builder(AdminShopDetailsActivity.this);
                builder.setTitle("REMOVE VERIFICATION")
                        .setMessage("Do you want to unverify "+shopName+"?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.setMessage("Unverifying "+shopName+" ...");
                                progressDialog.show();


                                //set data
                                HashMap<String,Object> hashMap=new HashMap<>();
                                hashMap.put("verified","false");

                                //update to db
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(shopUid).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                progressDialog.dismiss();

                                                AlertDialog.Builder alertBuilder=new AlertDialog.Builder(AdminShopDetailsActivity.this);
                                                alertBuilder.setTitle("Important")
                                                        .setMessage(shopName+"'s account has been unverified, Please confirm through email")
                                                        .setPositiveButton("Email", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {


                                                                String msg="Hello," +shopName+"\n"+
                                                                        "\nYour Shop2Order application account linked with "+ shopEmail+" has been unverified.\n Now your no longer be the " +
                                                                        "verified shopkeeper of Shop2Order application. You can not do any business through this application " +
                                                                        "until your account has been verified. If any queries please contact us\n"+
                                                                        "Thanks,\n" +
                                                                        "\n" +
                                                                        "Your Shop2Order team";
                                                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                                                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{shopEmail});
                                                                intent.putExtra(Intent.EXTRA_SUBJECT, "Sorry! Account Unverified");
                                                                intent.putExtra(Intent.EXTRA_TEXT,msg);
                                                                startActivity(intent);
                                                            }
                                                        })
                                                        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.cancel();

                                                            }
                                                        })
                                                        .show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(AdminShopDetailsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });


                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setNeutralButton("DETAILS", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder builder1=new AlertDialog.Builder(AdminShopDetailsActivity.this);
                                builder1.setMessage("\tOnly verified  SHOPKEEPERS are allowed to sell their product.")
                                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        })
                                        .show();
                            }
                        })
                        .show();

            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(AdminShopDetailsActivity.this);
                builder.setTitle("VERIFY")
                        .setMessage("Do you want to verify "+shopName+"?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent=new Intent(AdminShopDetailsActivity.this, ViewUploadedDocumentsActivity.class);
                                intent.putExtra("shopUid",shopUid);
                                intent.putExtra("shopName",shopName);
                                intent.putExtra("shopEmail",shopEmail);
                                startActivity(intent);


                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setNeutralButton("DETAILS", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder builder1=new AlertDialog.Builder(AdminShopDetailsActivity.this);
                                builder1.setMessage("\tOnly verified  SHOPKEEPERS are allowed to sell their product.")
                                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        })
                                        .show();
                            }
                        })
                        .show();
            }
        });


        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    adapterAdminProductSeller.getFilter().filter(s);

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

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(AdminShopDetailsActivity.this);
                builder.setMessage("Do you want to contact "+shopName+" through phone call")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialPhone();
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

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(AdminShopDetailsActivity.this);
                builder.setMessage("Do you want to view the shop "+shopName+"'s Location")
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

       filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(AdminShopDetailsActivity.this);
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
                                    adapterAdminProductSeller.getFilter().filter(selected);
                                }
                            }
                        })
                        .show();
            }
        });

        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(AdminShopDetailsActivity.this);
                builder.setMessage("Do you want to contact "+shopName+" through email")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{shopEmail});
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Subject here");
                                intent.putExtra(Intent.EXTRA_TEXT,"Body Here");
                                startActivity(intent);

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

        //popup menu
        PopupMenu popupMenu=new PopupMenu(AdminShopDetailsActivity.this,moreBtn);

        //add menu items to our menu
        popupMenu.getMenu().add("Shop Reviews");
        popupMenu.getMenu().add("Delete Shop");
        popupMenu.getMenu().add("Order Details");
        //  popupMenu.getMenu().add("Promotion Codes");
        //handle menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle()=="Shop Reviews"){
                    AlertDialog.Builder builder=new AlertDialog.Builder(AdminShopDetailsActivity.this);
                    builder.setMessage("Do you want to see "+shopName+"'s reviews?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //pass shop uid to show it's reviews
                                    Intent intent=new Intent(AdminShopDetailsActivity.this, AdminShopReviewsActivity.class);
                                    intent.putExtra("shopUid",shopUid);
                                    startActivity(intent);
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
                else if (item.getTitle()=="Delete Shop"){
                    //delete shop
                    //show delete confirm dialog
                    AlertDialog.Builder builder=new AlertDialog.Builder(AdminShopDetailsActivity.this);
                    builder.setTitle("Delete ")
                            .setMessage("Are you sure want to delete "+shopName+"?")
                            .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    progressDialog.setMessage("Deleting "+shopName+" ...");
                                    progressDialog.show();


                                    //set data
                                    HashMap<String,Object> hashMap=new HashMap<>();
                                    hashMap.put("deleted","true");


                                    //update to db
                                    DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                                    ref.child(shopUid).updateChildren(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                    progressDialog.dismiss();

                                                    AlertDialog.Builder alertBuilder=new AlertDialog.Builder(AdminShopDetailsActivity.this);
                                                    alertBuilder.setTitle("Important")
                                                            .setMessage("Shopkeeper account has been deleted, Please confirm through email")
                                                            .setPositiveButton("Email", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {


                                                                    String msg="Hello,\n" +
                                                                            "Your Shop2Order application account linked with "+ shopEmail+" has been removed by the admin.\n"+
                                                                            "Thanks,\n" +
                                                                            "\n" +
                                                                            "Your Shop2Order team";
                                                                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                                                                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                                                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{shopEmail});
                                                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Account Deleted");
                                                                    intent.putExtra(Intent.EXTRA_TEXT,msg);
                                                                    startActivity(intent);
                                                                }
                                                            })
                                                            .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.cancel();

                                                                }
                                                            })
                                                            .show();
                                                    deleteShop(shopUid); //id is the product id
                                                    onBackPressed();

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(AdminShopDetailsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                            });


                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //cancel,dismiss dialog
                                    dialog.cancel();
                                }
                            })
                            .show();


                }
                else if (item.getTitle()=="Order Details"){
                    AlertDialog.Builder builder=new AlertDialog.Builder(AdminShopDetailsActivity.this);
                    builder.setMessage("Do you want to see "+shopName+"'s order details?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //open same reviews activity as used in user main
                                    Intent intent=new Intent(AdminShopDetailsActivity.this, AdminSellerOrderListActivity.class);
                                    intent.putExtra("shopUid",shopUid);
                                    startActivity(intent);
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

                return true;
            }
        });
        //more button contains reviews,delete,shop order details
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show popup menu
                popupMenu.show();

            }
        });
    }

    private void deleteShop(String shopUid) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //User deleted
                        Toast.makeText(AdminShopDetailsActivity.this,"Shopkeeper deleted successfully...",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed deleting product
                        Toast.makeText(AdminShopDetailsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

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

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String address="https://maps.google.com/maps?&daddr=" +shopLatitude+","+shopLongitude;
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this,""+shopPhone,Toast.LENGTH_SHORT).show();
    }



    private void loadShopDetails() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get shop data
                shopName=""+snapshot.child("shopName").getValue();
                shopEmail=""+snapshot.child("email").getValue();
                shopPhone=""+snapshot.child("phone").getValue();
                shopAddress=""+snapshot.child("address").getValue();
                shopLatitude=""+snapshot.child("latitude").getValue();
                shopLongitude=""+snapshot.child("longitude").getValue();
                deliveryFee=""+snapshot.child("deliveryFee").getValue();
                shopDeleted=""+snapshot.child("deleted").getValue();
                verified=""+snapshot.child("verified").getValue();
                String profileImage=""+snapshot.child("profileImage").getValue();
                String shopOpen=""+snapshot.child("shopOpen").getValue();

                //set data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("Delivery Fee: ₹"+deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);

                if (verified.equals("false")){
                    verifyBtn.setVisibility(View.VISIBLE);
                    removeVerificationBtn.setVisibility(View.GONE);
                    removeVerificationBtn.setEnabled(false);
                }
                else{
                    removeVerificationBtn.setVisibility(View.VISIBLE);
                    verifyBtn.setVisibility(View.GONE);
                    verifyBtn.setEnabled(false);
                }

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
                            ModelProduct modelProduct=ds.getValue(ModelProduct.class);
                            productsList.add(modelProduct);
                        }
                        //setup adapater
                        adapterAdminProductSeller=new AdapterAdminProductSeller(AdminShopDetailsActivity.this,productsList);
                        //set adapter
                        productsRv.setAdapter(adapterAdminProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



}
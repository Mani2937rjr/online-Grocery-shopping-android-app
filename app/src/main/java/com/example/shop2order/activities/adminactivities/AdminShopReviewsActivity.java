package com.example.shop2order.activities.adminactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.shop2order.R;
import com.example.shop2order.activities.ShopReviewsActivity;
import com.example.shop2order.adapters.AdapterReview;
import com.example.shop2order.models.ModelReview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdminShopReviewsActivity extends AppCompatActivity {

    //ui views
    private ImageButton backBtn;
    private ImageView profileIv;
    private TextView shopNameTv,ratingsTv;
    private RatingBar ratingBar;
    private RecyclerView reviewsRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelReview> reviewArrayList; //will contan list of all reviews
    private AdapterReview adapterReview;

    private String shopUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_shop_reviews);


        //init ui views
        backBtn=findViewById(R.id.backBtn);
        profileIv=findViewById(R.id.profileIv);
        ratingsTv=findViewById(R.id.ratingsTv);
        shopNameTv=findViewById(R.id.shopNameTv);
        ratingBar=findViewById(R.id.ratingBar);
        reviewsRv=findViewById(R.id.reviewsRv);

        //get shopUid uid from intent
        shopUid = getIntent().getStringExtra("shopUid");

        firebaseAuth=FirebaseAuth.getInstance();
        loadShopDetails(); //for shop name,image
        loadReviews(); //for reviews list,avg ratings


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private float ratingSum=0;
    private void loadReviews() {
        //init list
        reviewArrayList=new ArrayList<>();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding
                        reviewArrayList.clear();
                        ratingSum=0;
                        for (DataSnapshot ds : snapshot.getChildren()){
                            float rating=Float.parseFloat(""+ds.child("ratings").getValue()); //e.g 4.3

                            ratingSum=ratingSum+rating; //for av rating, add (addition of) all ratings,later will divide it by number of reviews

                            ModelReview modelReview=ds.getValue(ModelReview.class);
                            reviewArrayList.add(modelReview);

                        }
                        //setup adapter
                        adapterReview=new AdapterReview(AdminShopReviewsActivity.this, reviewArrayList);
                        //set to recyclerview
                        reviewsRv.setAdapter(adapterReview);

                        long numberOfReviews=snapshot.getChildrenCount();
                        float avgRating=ratingSum/numberOfReviews;

                        ratingsTv.setText(String.format("%.2f",avgRating)+" ["+numberOfReviews+"]"); //e.g 4.7 [10]
                        ratingBar.setRating(avgRating);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadShopDetails() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName=""+snapshot.child("shopName").getValue();
                        String profileImage=""+snapshot.child("profileImage").getValue();

                        shopNameTv.setText(shopName);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIv);

                        } catch (Exception e) {
                            profileIv.setImageResource(R.drawable.ic_store_gray);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
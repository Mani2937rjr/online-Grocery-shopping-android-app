package com.example.shop2order.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shop2order.FilterAdminShop;
import com.example.shop2order.FilterAdminUsers;
import com.example.shop2order.R;
import com.example.shop2order.activities.ShopDetailsActivity;
import com.example.shop2order.activities.adminactivities.AdminShopDetailsActivity;
import com.example.shop2order.models.ModelAdminShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterAdminShop extends RecyclerView.Adapter<AdapterAdminShop.HolderAdminShop> implements Filterable {
    private Context context;
    public ArrayList<ModelAdminShop> AdminShopList,filterList;
    private FilterAdminShop filter;


    public AdapterAdminShop(Context context, ArrayList<ModelAdminShop> AdminShopList) {
        this.context = context;
        this.AdminShopList = AdminShopList;
        this.filterList = AdminShopList;
    }

    @NonNull
    @Override
    public HolderAdminShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_shop.xml
        View view= LayoutInflater.from(context).inflate(R.layout.row_admin_shop,parent,false);
        return new HolderAdminShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAdminShop holder, int position) {
        //get data
        ModelAdminShop modelAdminShop=AdminShopList.get(position);
        String accountType= modelAdminShop.getAccountType();
        String address= modelAdminShop.getAddress();
        String city= modelAdminShop.getCity();
        String country= modelAdminShop.getCountry();
        String email= modelAdminShop.getEmail();
        String latitude= modelAdminShop.getLatitude();
        String langitude= modelAdminShop.getLongitude();
        String online= modelAdminShop.getOnline();
        String name= modelAdminShop.getName();
        String state= modelAdminShop.getState();
        String phone= modelAdminShop.getPhone();
        final String uid= modelAdminShop.getUid();
        String timestamp= modelAdminShop.getTimestamp();
        String shopOpen= modelAdminShop.getShopOpen();
        String profileImage= modelAdminShop.getProfileImage();
        String shopName= modelAdminShop.getShopName();
        String verified=modelAdminShop.getVerified();

        loadReviews( modelAdminShop,holder); //load avg rating,set to ratingbar

        //set data
        holder.shopNameTv.setText(shopName);
        holder.phoneTv.setText(phone);
        holder.addressTv.setText(address);
        holder.emailTv.setText(email);
        //check if online
        if(online.equals("true")){
            //shop owner is online
            holder.onlineIv.setVisibility(View.VISIBLE);
        }
        else {
            //shop owner is offline
            holder.onlineIv.setVisibility(View.GONE);
        }

        //check if shop open
        if (shopOpen.equals("true")){
            //shop open
            holder.shopClosedTv.setVisibility(View.GONE);
        }
        else{
            //shop close
            holder.shopClosedTv.setVisibility(View.VISIBLE);
        }
        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(holder.shopIv);
        } catch (Exception e) {
          holder.shopIv.setImageResource(R.drawable.ic_store_gray);
        }

        if (verified!=null && verified.equals("false")){
            holder.verifyLabel.setVisibility(View.VISIBLE);
        }
        else{
            holder.verifyLabel.setVisibility(View.GONE);
        }

        //handle click listener,show shop details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent intent=new Intent(context, AdminShopDetailsActivity.class);
                intent.putExtra("shopUid",uid);
               context.startActivity(intent);


            }
        });

    }

    private float ratingSum=0;
    private void loadReviews(ModelAdminShop modelAdminShop, HolderAdminShop holder) {

        String shopUid=modelAdminShop.getUid();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding
                        ratingSum=0;
                        for (DataSnapshot ds:snapshot.getChildren()){
                            float rating=Float.parseFloat(""+ds.child("ratings").getValue()); //e.g 4.3

                            ratingSum=ratingSum+rating; //for av rating, add (addition of) all ratings,later will divide it by number of reviews


                        }

                        long numberOfReviews=snapshot.getChildrenCount();
                        float avgRating=ratingSum/numberOfReviews;

                        holder.ratingBar.setRating(avgRating);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    @Override
    public int getItemCount() {
        return AdminShopList.size(); //return number of records
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            filter=new FilterAdminShop(this,filterList);
        }
        return filter;
    }

    //view holder
    class HolderAdminShop extends RecyclerView.ViewHolder{
        //ui views of row_admin_shop.xml
        private ImageView shopIv,onlineIv,nextIv;
        private TextView  shopClosedTv,shopNameTv,phoneTv,addressTv,emailTv,verifyLabel;
        private RatingBar ratingBar;

        public HolderAdminShop(@NonNull View itemView) {
            super(itemView);
            shopIv=itemView.findViewById(R.id.shopIv);
            onlineIv=itemView.findViewById(R.id.onlineIv);
            nextIv=itemView.findViewById(R.id.nextIv);
            shopClosedTv=itemView.findViewById(R.id.shopClosedTv);
            shopNameTv=itemView.findViewById(R.id.shopNameTv);
            phoneTv=itemView.findViewById(R.id.phoneTv);
            addressTv=itemView.findViewById(R.id.addressTv);
            ratingBar=itemView.findViewById(R.id.ratingBar);
            emailTv=itemView.findViewById(R.id.emailTv);
            verifyLabel=itemView.findViewById(R.id.verifyLabel);

        }
    }
}

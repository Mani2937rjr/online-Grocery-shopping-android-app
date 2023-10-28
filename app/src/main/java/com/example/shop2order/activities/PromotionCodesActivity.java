package com.example.shop2order.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.shop2order.R;
import com.example.shop2order.adapters.AdapterPromotionShop;
import com.example.shop2order.models.ModelPromotion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PromotionCodesActivity extends AppCompatActivity {

    private ImageButton backBtn,addPromoBtn,filterBtn;
    private TextView filteredTv;
    private RecyclerView promoRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelPromotion> promotionArrayList;
    private AdapterPromotionShop adapterPromotionShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_codes);
        //ini views
        backBtn=findViewById(R.id.backBtn);
        addPromoBtn=findViewById(R.id.addPromoBtn);
        filteredTv=findViewById(R.id.filteredTv);
        filterBtn=findViewById(R.id.filterBtn);
        promoRv=findViewById(R.id.promoRv);

        firebaseAuth=FirebaseAuth.getInstance();
        loadAllPromoCodes();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click ,open add promo code activity
        addPromoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PromotionCodesActivity.this,AddPromotionCodeActivity.class));

            }
        });

        //handle filter button click,show filter dialog
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog();
            }
        });
    }

    private void filterDialog() {
        //options to display in dialog
        String[] options={"All","Expired","Not Expired"};
        //dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Filter Promotion Codes")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       //handle item clicks
                       if (which==0){
                           //All clicked
                           filteredTv.setText("All Promotion Codes");
                           loadAllPromoCodes();
                       }
                       else if (which==1){
                           //Expired clicked
                           filteredTv.setText("Expired Promotion Codes");
                           loadExpiredPromoCodes();

                       }
                       else if (which==2){
                           //Not Expired clicked
                           filteredTv.setText("Not Expired Codes");
                           loadNotExpiredPromoCodes();

                       }
                    }
                })
                .show();
    }

    private void loadAllPromoCodes(){
        //init list
        promotionArrayList=new ArrayList<>();

        //db reference Users>Current user>Promotions>codes data
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion=ds.getValue(ModelPromotion.class);

                            //add to list
                            promotionArrayList.add(modelPromotion);

                        }
                        //setup adapter, add list to adapter
                        adapterPromotionShop=new AdapterPromotionShop(PromotionCodesActivity.this,promotionArrayList);
                        //set adapter to recyclerview
                        promoRv.setAdapter(adapterPromotionShop);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    private void loadExpiredPromoCodes(){
        //get Current date
        DecimalFormat mFormat=new DecimalFormat("00");
        Calendar calendar=Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH)+1;
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        String todayDate=day+"/"+month+"/"+year; //e.g 29/06/2022

        //init list
        promotionArrayList=new ArrayList<>();

        //db reference Users>Current user>Promotions>codes data
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion=ds.getValue(ModelPromotion.class);

                            String expDate=modelPromotion.getExpireDate();

                            //check for expired
                            try {
                                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate=simpleDateFormat.parse(todayDate);
                                Date expireDate=simpleDateFormat.parse(expDate);

                                if (expireDate.compareTo(currentDate)>0){
                                    //date 1 occurs after date 2

                                }
                                else if (expireDate.compareTo(currentDate)<0){
                                    //date 1 occurs before date 2 (i.e. Expired)
                                    //add to list
                                    promotionArrayList.add(modelPromotion);

                                }
                                else if (expireDate.compareTo(currentDate)==0){
                                    //both date equals

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }



                        }
                        //setup adapter, add list to adapter
                        adapterPromotionShop=new AdapterPromotionShop(PromotionCodesActivity.this,promotionArrayList);
                        //set adapter to recyclerview
                        promoRv.setAdapter(adapterPromotionShop);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    private void loadNotExpiredPromoCodes(){
        //get Current date
        DecimalFormat mFormat=new DecimalFormat("00");
        Calendar calendar=Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH)+1;
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        String todayDate=day+"/"+month+"/"+year; //e.g 29/06/2022

        //init list
        promotionArrayList=new ArrayList<>();

        //db reference Users>Current user>Promotions>codes data
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion=ds.getValue(ModelPromotion.class);

                            String expDate=modelPromotion.getExpireDate();

                            //check for expired
                            try {
                                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate=simpleDateFormat.parse(todayDate);
                                Date expireDate=simpleDateFormat.parse(expDate);

                                if (expireDate.compareTo(currentDate)>0){
                                    //date 1 occurs after date 2
                                    //add to list
                                    promotionArrayList.add(modelPromotion);

                                }
                                else if (expireDate.compareTo(currentDate)<0){
                                    //date 1 occurs before date 2 (i.e. Expired)


                                }
                                else if (expireDate.compareTo(currentDate)==0){
                                    //both date equals
                                    //add to list
                                    promotionArrayList.add(modelPromotion);

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }



                        }
                        //setup adapter, add list to adapter
                        adapterPromotionShop=new AdapterPromotionShop(PromotionCodesActivity.this,promotionArrayList);
                        //set adapter to recyclerview
                        promoRv.setAdapter(adapterPromotionShop);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}
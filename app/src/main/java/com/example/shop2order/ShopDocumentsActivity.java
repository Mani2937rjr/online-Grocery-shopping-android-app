package com.example.shop2order;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.shop2order.databinding.ActivityShopDocumentsBinding;
import com.example.shop2order.databinding.ActivityUploadDocumentBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShopDocumentsActivity extends AppCompatActivity {
    private String shopUid;
    private ActivityShopDocumentsBinding binding;

    private ArrayList<ModelAPdf> pdfArrayList;
    private AdapterShopPdf adapterPdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityShopDocumentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        shopUid=getIntent().getStringExtra("shopUid");

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loadPdfList();
    }

    private void loadPdfList() {
        pdfArrayList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Documents");
        ref.orderByChild("id")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            ModelAPdf modelAPdf=ds.getValue(ModelAPdf.class);
                            pdfArrayList.add(modelAPdf);
                        }
                        adapterPdf=new AdapterShopPdf(ShopDocumentsActivity.this,pdfArrayList);
                        binding.bookRv.setAdapter(adapterPdf);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
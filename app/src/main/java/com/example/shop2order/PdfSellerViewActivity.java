package com.example.shop2order;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.shop2order.databinding.ActivityPdfSellerViewBinding;
import com.example.shop2order.databinding.ActivityPdfViewBinding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PdfSellerViewActivity extends AppCompatActivity {

    private ActivityPdfSellerViewBinding binding;

    private String id,shopUid;

    private static final String TAG="PDF_VIEW_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfSellerViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        id=getIntent().getStringExtra("id");
        shopUid=getIntent().getStringExtra("shopUid");

        Log.d(TAG,"onCreate:id: "+id);

        loadPdfDetails();
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadPdfDetails() {
        Log.d(TAG,"loadPdfFromUrl: GetPdf Url....");

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Documents");
        ref.child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String pdfUrl=""+snapshot.child("pdfUrl").getValue();

                        Log.d(TAG,"onDataChange: PDF URL: "+pdfUrl);

                        loadPdfFromUrl(pdfUrl);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadPdfFromUrl(String pdfUrl) {
        Log.d(TAG,"loadPdfFromUrl: Get pdf from storage");

        StorageReference reference= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constanspdf.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        binding.pdfView.fromBytes(bytes)
                                .swipeVertical(true)
                                .onPageChange(new OnPageChangeListener() {
                                    @Override
                                    public void onPageChanged(int page, int pageCount) {
                                        int currentPage=(page);
                                        binding.toolbarSubtitleTv.setText(currentPage+"/"+pageCount);
                                    }
                                })
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Toast.makeText(PdfSellerViewActivity.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .load();
                        binding.progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
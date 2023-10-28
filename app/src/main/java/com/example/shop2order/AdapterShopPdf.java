package com.example.shop2order;

import static com.example.shop2order.Constanspdf.MAX_BYTES_PDF;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shop2order.databinding.RowPdfAdminBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterShopPdf extends RecyclerView.Adapter<AdapterShopPdf.HolderPdfAdmin> {

    private Context context;
    private ArrayList<ModelAPdf> pdfArrayList;

    private RowPdfAdminBinding binding;

    private static final String TAG="PDF_ADAPTER_TAG";

    public AdapterShopPdf(Context context, ArrayList<ModelAPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {


        ModelAPdf modelAPdf=pdfArrayList.get(position);
        String date=modelAPdf.getDate();
        String id=modelAPdf.getId();
        String timestamp1=modelAPdf.getTimestamp1();
        String uid=modelAPdf.getUid();
        String pdfUrl=modelAPdf.getPdfUrl();

        holder.dateTv.setText(date);
        holder.idTv.setText(id);

        loadPdfFromUrl(modelAPdf,holder);
        loadPdfSize(modelAPdf,holder);

        holder.deleteBtn.setVisibility(View.INVISIBLE);
        holder.deleteBtn.setEnabled(false);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context,PdfSellerViewActivity.class);
                intent.putExtra("id",id);
                intent.putExtra("shopUid",uid);
                context.startActivity(intent);
            }
        });
    }

    private void loadPdfFromUrl(ModelAPdf modelAPdf, HolderPdfAdmin holder) {
        String pdfUrl=modelAPdf.getPdfUrl();
        StorageReference ref= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG,"onSuccess: "+modelAPdf.getId()+" successfully got the file");
                        holder.pdfView.fromBytes(bytes)
                                .pages(0)
                                .swipeVertical(true)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);

                                        Log.d(TAG,"onError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG,"loadComplete: pdf loaded");
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG,"onFailure:Failed getting file from url due to "+e.getMessage());
                    }
                });
    }

    private void loadPdfSize(ModelAPdf modelAPdf, HolderPdfAdmin holder) {
        String pdfUrl=modelAPdf.getPdfUrl();

        StorageReference ref= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                       double bytes=storageMetadata.getSizeBytes();
                        Log.d(TAG,"onSuccess: "+modelAPdf.getId()+""+bytes);

                       double kb=bytes/1024;
                       double mb=kb/1024;

                       if (mb>=1){
                           holder.sizeTv.setText(String.format("%.2f",mb)+" MB");
                       }
                       else if (kb>=1){
                           holder.sizeTv.setText(String.format("%.2f",kb)+" KB");
                       }
                       else {
                           holder.sizeTv.setText(String.format("%.2f",bytes)+" bytes");
                       }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"onFailure: "+e.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{

        PDFView pdfView;
        ProgressBar progressBar;
        TextView dateTv,idTv,sizeTv;
        ImageButton deleteBtn;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            pdfView=binding.pdfView;
            progressBar=binding.progressBar;
            dateTv=binding.dateTv;
            idTv=binding.idTv;
            sizeTv=binding.sizeTv;
            deleteBtn=binding.deleteBtn;
        }
    }
}

package com.example.shop2order.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shop2order.FilterProduct;
import com.example.shop2order.R;
import com.example.shop2order.activities.EditProductActivity;
import com.example.shop2order.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {
    private Context context;
    public ArrayList<ModelProduct> productList,filterList;
    private FilterProduct filter;

    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList=productList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_product_seller,parent,false);
        return new HolderProductSeller(view);

    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
        //get data
        final ModelProduct modelProduct=productList.get(position);
        String id=modelProduct.getProductId();
        String discountAvailable=modelProduct.getDiscountAvailable();
        String uid=modelProduct.getUid();
        String discountNote=modelProduct.getDiscountNote();
        String discountPrice=modelProduct.getDiscountPrice();
        String productCategory=modelProduct.getProductCategory();
        String productDescription=modelProduct.getProductDescription();
        String icon=modelProduct.getProductIcon();
        String quantity=modelProduct.getProductQuantity();
        String title=modelProduct.getProductTitle();
        String timeStamp=modelProduct.getTimestamp();
        String originalPrice=modelProduct.getOriginalPrice();
        String noStock= modelProduct.getNoStock();
        String blocked=modelProduct.getBlocked();

        //set data
        holder.titleTv.setText(title);
        holder.quantityTv.setText(quantity);
        holder.discountedNotetv.setText(discountNote);
        holder.discountedPriceTv.setText("₹"+discountPrice);
        holder.originalPriceTv.setText("₹"+originalPrice);

        if(discountAvailable.equals("true")){
            //product is on discount
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNotetv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // add strike through on original price


        }
        else{
            //product is not on discount
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountedNotetv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);
        }

        if (noStock.equals("true")){
            holder.productNoStockTv.setVisibility(View.VISIBLE);

        }
        else {
            holder.productNoStockTv.setVisibility(View.GONE);
        }

        if (blocked.equals("true")){
            holder.productblockTv.setVisibility(View.VISIBLE);
            holder.productblockTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setMessage("This produt has been blocked by the admin due to some problem.\n" +
                            "You can contact admin to unblock this product.")
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                }
            });
        }
        else {
            holder.productblockTv.setVisibility(View.GONE);
        }




        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary).into(holder.productIconIv);
        }
        catch (Exception e){
            holder.productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Handle item clicks, show item details(in botton sheet)
                detailsBottomSheet(modelProduct); //here model product contains details of clicked products



            }
        });
    }

    private void detailsBottomSheet(ModelProduct modelProduct) {
        //bottom sheet
        BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(context);
        //Inflateview for bottom sheet
        View view=LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller,null);
        //set view to bottom sheet
        bottomSheetDialog.setContentView(view);

        //init view of bottom sheet
        ImageButton backBtn=view.findViewById(R.id.backBtn);
        ImageButton deleteBtn=view.findViewById(R.id.deleteBtn);
        ImageButton editBtn=view.findViewById(R.id.editBtn);
        ImageView productIconIv=view.findViewById(R.id.productIconIv);
        TextView discountNoteTv=view.findViewById(R.id.discountNoteTv);
        TextView titleTv=view.findViewById(R.id.titleTv);
        TextView descriptionTv=view.findViewById(R.id.descriptionTv);
        TextView categoryTv=view.findViewById(R.id.categoryTv);
        TextView quantityTv=view.findViewById(R.id.quantityTv);
        TextView discountedPriceTv=view.findViewById(R.id.discountedPriceTv);
        TextView originalPriceTv=view.findViewById(R.id.originalPriceTv);
        TextView productNoStockTv=view.findViewById(R.id.productNoStockTv);
        TextView productBlockTv=view.findViewById(R.id.productBlockTv);

        //get data
        String id=modelProduct.getProductId();
        String discountAvailable=modelProduct.getDiscountAvailable();
        String uid=modelProduct.getUid();
        String discountNote=modelProduct.getDiscountNote();
        String discountPrice=modelProduct.getDiscountPrice();
        String productCategory=modelProduct.getProductCategory();
        String productDescription=modelProduct.getProductDescription();
        String icon=modelProduct.getProductIcon();
        String quantity=modelProduct.getProductQuantity();
        String title=modelProduct.getProductTitle();
        String timeStamp=modelProduct.getTimestamp();
        String originalPrice=modelProduct.getOriginalPrice();
        String noStock=modelProduct.getNoStock();
        String blocked=modelProduct.getBlocked();

        //set data
        titleTv.setText(title);
        descriptionTv.setText(productDescription);
        categoryTv.setText(productCategory);
        quantityTv.setText(quantity);
        discountNoteTv.setText(discountNote);
        discountedPriceTv.setText("₹"+discountPrice);
        originalPriceTv.setText("₹"+originalPrice);

        if (blocked.equals("true"))
        {
            productBlockTv.setVisibility(View.VISIBLE);
        }
        else{
            productBlockTv.setVisibility(View.GONE);
        }

        if(discountAvailable.equals("true")){
            //product is on discount
            discountedPriceTv.setVisibility(View.VISIBLE);
            discountNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // add strike through on original price


        }
        else{
            //product is not on discount
            discountedPriceTv.setVisibility(View.GONE);
            discountNoteTv.setVisibility(View.GONE);

        }

        //bottom sheet view no stock
        if (noStock.equals("true")){
            productNoStockTv.setVisibility(View.VISIBLE);
        }
        else{
            productNoStockTv.setVisibility(View.GONE);
        }

        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary).into(productIconIv);
        }
        catch (Exception e){
           productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        }
        //show dialog
        bottomSheetDialog.show();
        //edit click
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                //open edit product activity,pass id of product
                Intent intent=new Intent(context, EditProductActivity.class);
                intent.putExtra("productId",id);
                context.startActivity(intent);

            }
        });
        //delete click
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete confirm dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete ")
                        .setMessage("Are you sure want to delete product "+title+"?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete
                                deleteProduct(id); //id is the product id
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
        });
        //back click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });

    }

    private void deleteProduct(String id) {
        //delete product using its id

        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //product deleted
                        Toast.makeText(context,"product deleted succesfully...",Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed deleting product
                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }


    @Override
    public int getItemCount() {
       return productList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter=new FilterProduct(this,filterList);

        }
            return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder {
        //Holds views of recyclerview

        private ImageView productIconIv;
        private TextView discountedNotetv,titleTv,quantityTv,discountedPriceTv,originalPriceTv,productNoStockTv,productblockTv;

        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);

            productIconIv=itemView.findViewById(R.id.productIconIv);
            discountedNotetv=itemView.findViewById(R.id.discountedNotetv);
            titleTv=itemView.findViewById(R.id.titleTv);
            quantityTv=itemView.findViewById(R.id.quantityTv);
            discountedPriceTv=itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv=itemView.findViewById(R.id.originalPriceTv);
            productNoStockTv=itemView.findViewById(R.id.productNoStockTv);
            productblockTv=itemView.findViewById(R.id.productblockTv);
        }
    }
}

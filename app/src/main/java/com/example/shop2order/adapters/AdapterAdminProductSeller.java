package com.example.shop2order.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shop2order.FilterAdminProduct;
import com.example.shop2order.FilterProduct;
import com.example.shop2order.R;
import com.example.shop2order.activities.EditProductActivity;
import com.example.shop2order.activities.adminactivities.AdminShopDetailsActivity;
import com.example.shop2order.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AdapterAdminProductSeller extends RecyclerView.Adapter<AdapterAdminProductSeller.HolderAdminProductSeller> implements Filterable {
    private Context context;
    public ArrayList<ModelProduct> productList,filterList;
    private FilterAdminProduct filter;


    public AdapterAdminProductSeller(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList=productList;
    }

    @NonNull
    @Override
    public HolderAdminProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_admin_product_seller,parent,false);
        return new HolderAdminProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAdminProductSeller holder, int position) {

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

        //block tv display
        if (blocked.equals("true")){
            holder.productblockTv.setVisibility(View.VISIBLE);
        }
        else{
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
        View view=LayoutInflater.from(context).inflate(R.layout.bs_admin_product_details_seller,null);
        //set view to bottom sheet
        bottomSheetDialog.setContentView(view);

        ProgressDialog  progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //init view of bottom sheet
        ImageButton backBtn=view.findViewById(R.id.backBtn);
        ImageButton deleteBtn=view.findViewById(R.id.deleteBtn);
        ImageButton blockBtn=view.findViewById(R.id.blockBtn);
        ImageButton unblockBtn=view.findViewById(R.id.unblockBtn);
        ImageView productIconIv=view.findViewById(R.id.productIconIv);
        TextView discountNoteTv=view.findViewById(R.id.discountNoteTv);
        TextView idTv=view.findViewById(R.id.idTv);
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

        if (blocked.equals("true")){
            productBlockTv.setVisibility(View.VISIBLE);
        }
        else{
            productBlockTv.setVisibility(View.GONE);
        }

        final String[] email = new String[1];
        final String[] sname = new String[1];
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               email[0] =""+snapshot.child("email").getValue();
               sname[0] =""+snapshot.child("shopName").getValue();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //set data
        idTv.setText(id);
        titleTv.setText(title);
        descriptionTv.setText(productDescription);
        categoryTv.setText(productCategory);
        quantityTv.setText(quantity);
        discountNoteTv.setText(discountNote);
        discountedPriceTv.setText("₹"+discountPrice);
        originalPriceTv.setText("₹"+originalPrice);
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
            deleteBtn.setColorFilter(R.color.colorGray02);
            blockBtn.setColorFilter(R.color.colorGray02);
        }
        else{
            productNoStockTv.setVisibility(View.GONE);
        }

        //to display block and unblock button
        if (blocked.equals("false")){
            blockBtn.setVisibility(View.VISIBLE);
            unblockBtn.setVisibility(View.GONE);
            unblockBtn.setEnabled(false);
        }
        else
        {
            unblockBtn.setVisibility(View.VISIBLE);
            blockBtn.setVisibility(View.GONE);
            blockBtn.setEnabled(false);
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
        blockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();

            }
        });

        //unblock button
        unblockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Unblock")
                        .setMessage("Do you want to unblock product " + title + "?")
                        .setPositiveButton("Unblock", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                progressDialog.setMessage("Unblocking product...");
                                progressDialog.show();

                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                                alertBuilder.setTitle("Important")
                                        .setMessage(title + " has been unblocked, Please confirm through email")
                                        .setPositiveButton("Email", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {


                                                String msg = "Hello, " + Arrays.toString(sname) + "\n" +
                                                        "Your product " + title + " with product id " + id + " has been unblocked by the admin.\n" +
                                                        "Now you can sell this product?\n" +
                                                        "Thanks,\n" +
                                                        "\n" +
                                                        "Your Shop2Order team";
                                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email[0]});
                                                intent.putExtra(Intent.EXTRA_SUBJECT, "Product " + id + " unblocked");
                                                intent.putExtra(Intent.EXTRA_TEXT, msg);
                                                context.startActivity(intent);
                                            }
                                        })
                                        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();

                                            }
                                        })
                                        .show();
                                //delete
                                unblockProduct(id, uid); //id is the product id
                                progressDialog.dismiss();
                            }
                        })

                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //cancel,dismiss dialog
                                progressDialog.dismiss();
                                dialog.cancel();
                            }
                        })

                        .show();
                }

        });


        //block button clicked
        blockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noStock.equals("true")){
                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setMessage("You can't block no stock product")
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                }

                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Block")
                            .setMessage("Do you want to block product " + title + "?")
                            .setPositiveButton("Block", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    progressDialog.setMessage("Blocking product...");
                                    progressDialog.show();

                                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                                    alertBuilder.setTitle("Important")
                                            .setMessage(title + " has been Blocked, Please confirm through email")
                                            .setPositiveButton("Email", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {


                                                    String msg = "Hello, " + Arrays.toString(sname) + "\n" +
                                                            "Your product " + title + " with product id " + id + " has been blocked by the admin.\n" +
                                                            "You can't sell this product\n" +
                                                            "Thanks,\n" +
                                                            "\n" +
                                                            "Your Shop2Order team";
                                                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                                                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email[0]});
                                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Product " + id + " blocked");
                                                    intent.putExtra(Intent.EXTRA_TEXT, msg);
                                                    context.startActivity(intent);
                                                }
                                            })
                                            .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();

                                                }
                                            })
                                            .show();
                                    //delete
                                    blockProduct(id, uid); //id is the product id
                                    progressDialog.dismiss();
                                }
                            })

                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //cancel,dismiss dialog
                                    progressDialog.dismiss();
                                    dialog.cancel();
                                }
                            })
                            .setNeutralButton("More Details", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AlertDialog.Builder builder1=new AlertDialog.Builder(context);
                                    builder1.setMessage("You can temporarily blocked this product if you find/n" +
                                            "any problem in this product. You can later unblock it.")
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

            }
        });

        //delete click
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete confirm dialog
                if (noStock.equals("true")){
                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setMessage("You can't deleted no stock product")
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete ")
                            .setMessage("Do you want to delete product " + title + "permanently?")
                            .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    progressDialog.setMessage("Deleting product...");
                                    progressDialog.show();

                                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                                    alertBuilder.setTitle("Important")
                                            .setMessage(title + " has been deleted, Please confirm through email")
                                            .setPositiveButton("Email", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {


                                                    String msg = "Hello, " + Arrays.toString(sname) + "\n" +
                                                            "Your product " + title + " with product id " + id + " has been removed by the admin.\n" +
                                                            "Thanks,\n" +
                                                            "\n" +
                                                            "Your Shop2Order team";
                                                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                                                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email[0]});
                                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Product " + id + " deleted");
                                                    intent.putExtra(Intent.EXTRA_TEXT, msg);
                                                    context.startActivity(intent);
                                                }
                                            })
                                            .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();

                                                }
                                            })
                                            .show();
                                    //delete
                                    deleteProduct(id, uid); //id is the product id
                                    bottomSheetDialog.dismiss();
                                    progressDialog.dismiss();
                                }
                            })

                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //cancel,dismiss dialog
                                    progressDialog.dismiss();
                                    dialog.cancel();
                                }
                            })
                            .show();
                }
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

    private void unblockProduct(String id, String uid) {
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("blocked","false");

        //update to db
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("Products").child(id)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //update success
                        Toast.makeText(context, "product unblocked succesfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void blockProduct(String id, String uid) {

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("blocked","true");

        //update to db
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("Products").child(id)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //update success
                        Toast.makeText(context, "product blocked succesfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void deleteProduct(String id,String uid) {
                //delete product using its id

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(uid).child("Products").child(id).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //product deleted
                                Toast.makeText(context, "product deleted succesfully...", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //failed deleting product
                                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
            }

            @Override
            public int getItemCount() {
                return productList.size();
            }

            @Override
            public Filter getFilter() {
                if (filter == null) {
                    filter = new FilterAdminProduct(this, filterList);

                }
                return filter;
            }

            class HolderAdminProductSeller extends RecyclerView.ViewHolder {
                private ImageView productIconIv;
                private TextView discountedNotetv, titleTv, quantityTv, discountedPriceTv, originalPriceTv, productNoStockTv,productblockTv;

                public HolderAdminProductSeller(@NonNull View itemView) {
                    super(itemView);

                    productIconIv = itemView.findViewById(R.id.productIconIv);
                    discountedNotetv = itemView.findViewById(R.id.discountedNotetv);
                    titleTv = itemView.findViewById(R.id.titleTv);
                    quantityTv = itemView.findViewById(R.id.quantityTv);
                    discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
                    originalPriceTv = itemView.findViewById(R.id.originalPriceTv);
                    productNoStockTv = itemView.findViewById(R.id.productNoStockTv);
                    productblockTv=itemView.findViewById(R.id.productblockTv);
                }
            }
        }
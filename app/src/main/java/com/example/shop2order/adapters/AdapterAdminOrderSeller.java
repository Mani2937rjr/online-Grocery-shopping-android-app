package com.example.shop2order.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shop2order.AdminOrderDetailsSellerActivity;
import com.example.shop2order.FilterOrderSeller;
import com.example.shop2order.R;
import com.example.shop2order.models.ModelAdminOrderSeller;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterAdminOrderSeller extends RecyclerView.Adapter<AdapterAdminOrderSeller.HolderAdminOrderSeller> implements Filterable {
    private Context context;
    public ArrayList<ModelAdminOrderSeller> orderSellerArrayList,filterList;
    private FilterOrderSeller filter;

    public AdapterAdminOrderSeller(Context context, ArrayList<ModelAdminOrderSeller> orderSellerArrayList) {
        this.context = context;
        this.orderSellerArrayList = orderSellerArrayList;
        this.filterList=orderSellerArrayList;
    }

    @NonNull
    @Override
    public HolderAdminOrderSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_order_seller,parent,false);
        return new HolderAdminOrderSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAdminOrderSeller holder, int position) {
        //get data at position
        ModelAdminOrderSeller modelAdminOrderSeller=orderSellerArrayList.get(position);
        String orderId=modelAdminOrderSeller.getOrderId();
        String orderBy=modelAdminOrderSeller.getOrderBy();
        String orderTo=modelAdminOrderSeller.getOrderTo();
        String orderCost=modelAdminOrderSeller.getOrderCost();
        String orderStatus=modelAdminOrderSeller.getOrderStatus();
        String orderTime=modelAdminOrderSeller.getOrderTime();

        //load user/buyer info
        loadUserInfo(modelAdminOrderSeller,holder);

        //set data
        holder.amountTv.setText("Amount: ₹" + orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("Order ID: " + orderId);
        //change order status text color
        if (orderStatus.equals("In Progress")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
        else if (orderStatus.equals("Completed")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }
        else if (orderStatus.equals("Cancelled")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorRed));
        }
        else if (orderStatus.equals("Order Cancelled")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.darkPink));
        }


        //convert timestamp tp proper format
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate= DateFormat.format("dd/MM/yyyy",calendar).toString(); //e.g 20/06/2022

        holder.orderDateTv.setText(formatedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details,we need to keys there, orderId,orderBy
                Intent intent = new Intent(context, AdminOrderDetailsSellerActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("orderBy", orderBy);
                intent.putExtra("orderTo", orderTo);
                context.startActivity(intent); //now get these values through intent on AdminOrderDetailsSellerActivity
            }
        });

    }

    private void loadUserInfo(ModelAdminOrderSeller modelAdminOrderSeller, HolderAdminOrderSeller holder) {
        //to load email of the user/buyer: modelOrderShop.getOrderBy() contains uid of that user/buyer
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelAdminOrderSeller.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email=""+snapshot.child("email").getValue();
                        String enabled=""+snapshot.child("enabled").getValue();

                        //to indicate deleted user's order
                        if (email.equals("null")){
                            holder.emailTv.setText("NA");
                            holder.userdeleteTv.setVisibility(View.VISIBLE);
                            holder.userBlockTv.setVisibility(View.GONE);
                            holder.userdeleteTv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                                    builder.setTitle("User Deleted")
                                            .setMessage("This user has been deleted from the application" +
                                                    "So the order will be automatically cancelled if order is in" +
                                                    "In progress status....!")
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

                            holder.emailTv.setText(email);
                            holder.userdeleteTv.setVisibility(View.GONE);}

                        if (enabled.equals("false")){
                            holder.userBlockTv.setVisibility(View.VISIBLE);
                           /* holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                                    builder.setTitle("User Blocked")
                                            .setMessage("This user has been temporarily blocked from the application." +
                                                    " So you can't view order details until admin unblocks user...!")
                                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            })
                                            .show();
                                }
                            });*/
                        }
                        else{
                            holder.userBlockTv.setVisibility(View.GONE);
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderSellerArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            //init filter
            filter=new FilterOrderSeller(this,filterList);
        }
        return filter;
    }

    class HolderAdminOrderSeller extends RecyclerView.ViewHolder{

        //views of layout row_order_seller
        private TextView orderIdTv,orderDateTv,emailTv,amountTv,
                userdeleteTv,userBlockTv,statusTv;


        public HolderAdminOrderSeller(@NonNull View itemView) {
            super(itemView);

            //init ui views
            orderIdTv=itemView.findViewById(R.id.orderIdTv);
            orderDateTv=itemView.findViewById(R.id.orderDateTv);
            emailTv=itemView.findViewById(R.id.emailTv);
            amountTv=itemView.findViewById(R.id.amountTv);
            statusTv=itemView.findViewById(R.id.statusTv);
            userdeleteTv=itemView.findViewById(R.id.userdeleteTv);
            userBlockTv=itemView.findViewById(R.id.userBlockTv);
        }
    }
}

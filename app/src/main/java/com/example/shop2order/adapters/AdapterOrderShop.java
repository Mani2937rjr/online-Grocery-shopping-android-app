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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shop2order.FilterOrderShop;
import com.example.shop2order.R;
import com.example.shop2order.activities.OrderDetailsSellerActivity;
import com.example.shop2order.activities.OrderDetailsUsersActivity;
import com.example.shop2order.models.ModelOrderShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.HolderOrderShop> implements Filterable {

    private Context context;
    public ArrayList<ModelOrderShop> orderShopArrayList,filterList;
    private FilterOrderShop filter;

    public AdapterOrderShop(Context context, ArrayList<ModelOrderShop> orderShopArrayList) {
        this.context = context;
        this.orderShopArrayList = orderShopArrayList;
        this.filterList=orderShopArrayList;
    }

    @NonNull
    @Override
    public HolderOrderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_order_seller,parent,false);
        return new HolderOrderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderShop holder, int position) {
        //get data at position
        ModelOrderShop modelOrderShop=orderShopArrayList.get(position);
        String orderId=modelOrderShop.getOrderId();
        String orderBy=modelOrderShop.getOrderBy();
        String orderTo=modelOrderShop.getOrderTo();
        String orderCost=modelOrderShop.getOrderCost();
        String orderStatus=modelOrderShop.getOrderStatus();
        String orderTime=modelOrderShop.getOrderTime();

        //load user/buyer info
        loadUserInfo(modelOrderShop,holder);

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
                    Intent intent = new Intent(context, OrderDetailsSellerActivity.class);
                    intent.putExtra("orderId", orderId);
                    intent.putExtra("orderBy", orderBy);
                    context.startActivity(intent); //now get these values through intent on OrderDetailsUsersActivity
                }
            });


    }

    private void loadUserInfo(ModelOrderShop modelOrderShop, HolderOrderShop holder) {
        //to load email of the user/buyer: modelOrderShop.getOrderBy() contains uid of that user/buyer
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrderShop.getOrderBy())
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
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
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
                            });
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
        return orderShopArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            //init filter
            filter=new FilterOrderShop(this,filterList);
        }
        return filter;
    }


    //view holder class for row_order_seller.xml
    class HolderOrderShop extends RecyclerView.ViewHolder{

        //ui views of row_order_seller.xml
        private TextView orderIdTv,orderDateTv,emailTv,amountTv,statusTv,
                userdeleteTv,userBlockTv;

        public HolderOrderShop(@NonNull View itemView) {
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

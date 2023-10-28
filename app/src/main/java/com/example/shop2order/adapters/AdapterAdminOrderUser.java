package com.example.shop2order.adapters;

import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shop2order.FilterOrderShop;
import com.example.shop2order.FilterOrderUser;
import com.example.shop2order.activities.adminactivities.AdminOrderDetailsUsersActivity;
import com.example.shop2order.R;
import com.example.shop2order.models.ModelAdminOrderUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Calendar;

public class AdapterAdminOrderUser extends RecyclerView.Adapter<AdapterAdminOrderUser.HolderAdminOrderUser> implements Filterable {
    private Context context;
    public ArrayList<ModelAdminOrderUser> AdminOrderUserArrayList,filterList;
    private FilterOrderUser filter;

    public AdapterAdminOrderUser(Context context, ArrayList<ModelAdminOrderUser> adminOrderUserArrayList) {
        this.context = context;
        this.AdminOrderUserArrayList = adminOrderUserArrayList;
        this.filterList=adminOrderUserArrayList;
    }

    @NonNull
    @Override
    public HolderAdminOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_order_user,parent,false);
        return new HolderAdminOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAdminOrderUser holder, int position) {

        //get data
        ModelAdminOrderUser modelAdminOrderUser=AdminOrderUserArrayList.get(position);
        String orderId=modelAdminOrderUser.getOrderId();
        String orderBy=modelAdminOrderUser.getOrderBy();
        String orderCost=modelAdminOrderUser.getOrderCost();
        String orderStatus=modelAdminOrderUser.getOrderStatus();
        String orderTime=modelAdminOrderUser.getOrderTime();
        String orderTo=modelAdminOrderUser.getOrderTo();

        //get shop info
        loadShopInfo(modelAdminOrderUser,holder);

        //set data
        holder.amountTv.setText("Amount: ₹"+orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("OrderID:"+orderId);

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

        holder.dateTv.setText(formatedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details,we need to keys there, orderId,orderTo
                Intent intent=new Intent(context, AdminOrderDetailsUsersActivity.class);
                intent.putExtra("orderTo",orderTo);
                intent.putExtra("orderId",orderId);
                context.startActivity(intent); //now get these values through intent on OrderDetailsUsersActivity
            }
        });

    }

    private void loadShopInfo(ModelAdminOrderUser modelAdminOrderUser, HolderAdminOrderUser holder) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelAdminOrderUser.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName=""+snapshot.child("shopName").getValue();
                        holder.shopNameTv.setText(shopName);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public int getItemCount() {
        return AdminOrderUserArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            //init filter
            filter=new FilterOrderUser(this,filterList);
        }
        return filter;
    }


    class HolderAdminOrderUser extends RecyclerView.ViewHolder{

        //views of layout row_order_user
        private TextView orderIdTv,dateTv,shopNameTv,amountTv,statusTv;

        public HolderAdminOrderUser(@NonNull View itemView) {
            super(itemView);
            //init views of layout row_order_user
            orderIdTv=itemView.findViewById(R.id.orderIdTv);
            dateTv=itemView.findViewById(R.id.dateTv);
            shopNameTv=itemView.findViewById(R.id.shopNameTv);
            amountTv=itemView.findViewById(R.id.amountTv);
            statusTv=itemView.findViewById(R.id.statusTv);
            dateTv=itemView.findViewById(R.id.dateTv);
        }
    }
}

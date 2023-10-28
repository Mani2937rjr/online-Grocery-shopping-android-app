package com.example.shop2order.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shop2order.R;
import com.example.shop2order.models.ModelCartItem;
import com.example.shop2order.models.ModelOrderedItem;

import java.util.ArrayList;

public class AdapterOrderedItem extends RecyclerView.Adapter<AdapterOrderedItem.HolderOrderedItem> {

    private Context context;
    private ArrayList<ModelOrderedItem> orderedItemsList;

    public AdapterOrderedItem(Context context, ArrayList<ModelOrderedItem> orderedItemsList) {
        this.context = context;
        this.orderedItemsList = orderedItemsList;
    }

    @NonNull
    @Override
    public HolderOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_ordereditem,parent,false);
        return new HolderOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderedItem holder, int position) {

        //get data
        ModelOrderedItem modelOrderedItem=orderedItemsList.get(position);
        String getpId=modelOrderedItem.getpId();
        String name=modelOrderedItem.getName();
        String cost=modelOrderedItem.getCost();
        String price=modelOrderedItem.getPrice();
        String quantity=modelOrderedItem.getQuantity();

        //set data
        holder.itemTitleTv.setText(name);
        holder.itemPriceTv.setText("₹"+cost);
        holder.itemQuantityTv.setText("["+quantity+"]"); //e.g [3]
        holder.itemPriceEachTv.setText("₹"+price);


    }

    @Override
    public int getItemCount() {
        return orderedItemsList.size(); //return size of the list
    }

    //view holder class
    class HolderOrderedItem extends RecyclerView.ViewHolder{

        //views of  row_orderitem.xml
        private TextView itemTitleTv,itemPriceTv,itemPriceEachTv,itemQuantityTv;

        public HolderOrderedItem(@NonNull View itemView) {
            super(itemView);
            itemTitleTv=itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv=itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv=itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv=itemView.findViewById(R.id.itemQuantityTv);
        }
    }
}

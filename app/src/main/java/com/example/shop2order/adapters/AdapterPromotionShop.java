package com.example.shop2order.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shop2order.R;
import com.example.shop2order.models.ModelPromotion;

import java.util.ArrayList;

public class AdapterPromotionShop extends RecyclerView.Adapter<AdapterPromotionShop.HolderPromotionShop> {

    private Context context;
    private ArrayList<ModelPromotion> promotionArrayList;

    public AdapterPromotionShop(Context context, ArrayList<ModelPromotion> promotionArrayList) {
        this.context = context;
        this.promotionArrayList = promotionArrayList;
    }

    @NonNull
    @Override
    public HolderPromotionShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layoutrow_promotion_shop.xml
        View view= LayoutInflater.from(context).inflate(R.layout.row_promotion_shop,parent,false);
        return new HolderPromotionShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPromotionShop holder, int position) {
        //get data
        ModelPromotion modelPromotion=promotionArrayList.get(position);
        String id=modelPromotion.getId();
        String timestamp=modelPromotion.getTimestamp();
        String description=modelPromotion.getDescription();
        String promoCode=modelPromotion.getPromoCode();
        String promoPrice=modelPromotion.getPromoPrice();
        String expireDate=modelPromotion.getExpireDate();
        String minimumOrderPrice=modelPromotion.getMinimumOrderPrice();

        //set data
        holder.descriptionTv.setText(description);
        holder.promoPriceTv.setText(promoPrice);
        holder.minimumOrderPriceTv.setText(minimumOrderPrice);
        holder.promoCodeTv.setText("Code: "+promoCode);
        holder.expireDateTv.setText("Expire Date: "+expireDate);

    }

    @Override
    public int getItemCount() {
        return promotionArrayList.size();
    }

    //view holder class
    class HolderPromotionShop extends RecyclerView.ViewHolder{

        //views of row_promotion_shop.xml
        private ImageView iconIv;
        private TextView promoCodeTv,promoPriceTv,minimumOrderPriceTv,expireDateTv,
                         descriptionTv;

        public HolderPromotionShop(@NonNull View itemView) {
            super(itemView);

            //init ui views
            iconIv=itemView.findViewById(R.id.iconIv);
            promoCodeTv=itemView.findViewById(R.id.promoCodeTv);
            promoPriceTv=itemView.findViewById(R.id.promoPriceTv);
            minimumOrderPriceTv=itemView.findViewById(R.id.minimumOrderPriceTv);
            expireDateTv=itemView.findViewById(R.id.expireDateTv);
            descriptionTv=itemView.findViewById(R.id.descriptionTv);

        }
    }
}

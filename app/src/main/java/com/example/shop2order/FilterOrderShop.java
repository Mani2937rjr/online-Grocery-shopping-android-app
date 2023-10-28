package com.example.shop2order;

import android.widget.Filter;

import com.example.shop2order.adapters.AdapterOrderShop;
import com.example.shop2order.adapters.AdapterProductSeller;
import com.example.shop2order.models.ModelOrderShop;
import com.example.shop2order.models.ModelProduct;

import java.util.ArrayList;

public class FilterOrderShop extends Filter {
    private AdapterOrderShop adapter;
    private ArrayList<ModelOrderShop> filterList;

    public FilterOrderShop(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results=new FilterResults();
        //search field not empty,searching something,perform search
        //Validate data for search query
        if(constraint != null && constraint.length() > 0){
            //Change to upper case,to make case insensitive
            constraint=constraint.toString().toUpperCase();

            //Store our filtered list
            ArrayList<ModelOrderShop> filteredModels=new ArrayList<>();
            for (int i=0; i<filterList.size();i++){
                //check, search by title and category
                if (filterList.get(i).getOrderStatus().toUpperCase().contains(constraint)){
                    //add filtered data to list
                    filteredModels.add(filterList.get(i));

                }
            }
            results.count=filteredModels.size();
            results.values=filteredModels;

        }
        else{
            //search field not empty,not searching,return original/all/complete list
            results.count=filterList.size();
            results.values=filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.orderShopArrayList=(ArrayList<ModelOrderShop>) results.values;
        //Refresh Adapter
        adapter.notifyDataSetChanged();
    }
}

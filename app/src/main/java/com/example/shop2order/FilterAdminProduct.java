package com.example.shop2order;

import android.widget.Filter;

import com.example.shop2order.adapters.AdapterAdminProductSeller;
import com.example.shop2order.models.ModelProduct;

import java.util.ArrayList;

public class FilterAdminProduct extends Filter {
    private AdapterAdminProductSeller adapterAdminProductSeller;
    private ArrayList<ModelProduct> filterList;

    public FilterAdminProduct(AdapterAdminProductSeller adapterAdminProductSeller, ArrayList<ModelProduct> filterList) {
        this.adapterAdminProductSeller = adapterAdminProductSeller;
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
            ArrayList<ModelProduct> filteredModels=new ArrayList<>();
            for (int i=0; i<filterList.size();i++){
                //check, search by title and category
                if (filterList.get(i).getProductTitle().toUpperCase().contains(constraint) || filterList.get(i).getProductCategory().toUpperCase().contains(constraint) ){
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
        adapterAdminProductSeller.productList=(ArrayList<ModelProduct>) results.values;
        //Refresh Adapter
       adapterAdminProductSeller.notifyDataSetChanged();
    }
}

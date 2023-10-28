package com.example.shop2order;

import android.widget.Filter;

import com.example.shop2order.adapters.AdapterAdminShop;
import com.example.shop2order.models.ModelAdminShop;
import com.example.shop2order.models.ModelAdminUser;

import java.util.ArrayList;

public class FilterAdminShop extends Filter {
    private AdapterAdminShop adapterAdminShop;
    private ArrayList<ModelAdminShop> filterList;

    public FilterAdminShop(AdapterAdminShop adapterAdminShop, ArrayList<ModelAdminShop> filterList) {
        this.adapterAdminShop = adapterAdminShop;
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
            ArrayList<ModelAdminShop> filteredModels=new ArrayList<>();
            for (int i=0; i<filterList.size();i++){
                //check, search by name and city
                if (filterList.get(i).getName().toUpperCase().contains(constraint)
                        || filterList.get(i).getCity().toUpperCase().contains(constraint)
                        || filterList.get(i).getShopName().toUpperCase().contains(constraint)
                        || filterList.get(i).getAddress().toUpperCase().contains(constraint)){
                    //add filtered data to list
                    filteredModels.add(filterList.get(i));

                }
            }
            results.count=filteredModels.size();
            results.values=filteredModels;

        }
        else
        {
            //search field not empty,not searching,return original/all/complete list
            results.count=filterList.size();
            results.values=filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterAdminShop.AdminShopList=(ArrayList<ModelAdminShop>) results.values;
        //Refresh Adapter
        adapterAdminShop.notifyDataSetChanged();

    }
}

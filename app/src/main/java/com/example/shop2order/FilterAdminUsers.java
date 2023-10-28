package com.example.shop2order;

import android.widget.Filter;

import com.example.shop2order.adapters.AdapterAdminUser;
import com.example.shop2order.models.ModelAdminUser;
import com.example.shop2order.models.ModelProduct;

import java.util.ArrayList;

public class FilterAdminUsers extends Filter {
    private AdapterAdminUser adapterAdminUser;
    private ArrayList<ModelAdminUser> filterList;

    public FilterAdminUsers(AdapterAdminUser adapterAdminUser, ArrayList<ModelAdminUser> filterList) {
        this.adapterAdminUser = adapterAdminUser;
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
            ArrayList<ModelAdminUser> filteredModels=new ArrayList<>();
            for (int i=0; i<filterList.size();i++){
                //check, search by name and city
                if (filterList.get(i).getName().toUpperCase().contains(constraint)
                        || filterList.get(i).getCity().toUpperCase().contains(constraint)
                        || filterList.get(i).getCountry().toUpperCase().contains(constraint)
                        || filterList.get(i).getAddress().toUpperCase().contains(constraint)){
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
        adapterAdminUser.AdminUserList=(ArrayList<ModelAdminUser>) results.values;
        //Refresh Adapter
        adapterAdminUser.notifyDataSetChanged();

    }
}

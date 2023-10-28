package com.example.shop2order.adapters;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shop2order.FilterAdminUsers;
import com.example.shop2order.R;
import com.example.shop2order.activities.ProfileEditUserActivity;
import com.example.shop2order.activities.adminactivities.AdminUserOrderListActivity;
import com.example.shop2order.models.ModelAdminUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterAdminUser extends RecyclerView.Adapter<AdapterAdminUser.HolderAdminUser> implements Filterable {
    private Context context;
    public ArrayList<ModelAdminUser> AdminUserList,filterList;
    private FilterAdminUsers filter;

    public AdapterAdminUser(Context context, ArrayList<ModelAdminUser> AdminUserList) {
        this.context = context;
        this.AdminUserList = AdminUserList;
        this.filterList = AdminUserList;
    }

    @NonNull
    @Override
    public HolderAdminUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_shop.xml
        View view= LayoutInflater.from(context).inflate(R.layout.row_admin_user,parent,false);
        return new HolderAdminUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAdminUser holder, int position) {
        //get data
        ModelAdminUser modelAdminUser=AdminUserList.get(position);
        String accountType= modelAdminUser.getAccountType();
        String address= modelAdminUser.getAddress();
        String city= modelAdminUser.getCity();
        String country= modelAdminUser.getCountry();
        String email= modelAdminUser.getEmail();
        String latitude= modelAdminUser.getLatitude();
        String langitude= modelAdminUser.getLongitude();
        String online= modelAdminUser.getOnline();
        String name= modelAdminUser.getName();
        String state= modelAdminUser.getState();
        String phone= modelAdminUser.getPhone();
        final String uid= modelAdminUser.getUid();
        String timestamp= modelAdminUser.getTimestamp();
        String profileImage= modelAdminUser.getProfileImage();
        String enabled=modelAdminUser.getEnabled();

        //set data
        holder.nameTv.setText(name);
        holder.addressTv.setText(address);
        holder.phoneTv.setText(phone);
        holder.emailTv.setText(email);
        //check if online
        if(online.equals("true")){
            //shop owner is online
            holder.onlineIv.setVisibility(View.VISIBLE);
        }
        else {
            //shop owner is offline
            holder.onlineIv.setVisibility(View.GONE);
        }

        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(holder.profileIv);
        } catch (Exception e) {
            holder.profileIv.setImageResource(R.drawable.ic_person_gray);
        }

        //blocked user label
        if (enabled.equals("true")){
            holder.userBlockTv.setVisibility(View.GONE);
        }
        else{
            holder.userBlockTv.setVisibility(View.VISIBLE);
        }

        //handle click listener,show user details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //to display user details bottom sheet of bs_users_details_admin.xml
                detailsBottomSheet(modelAdminUser);


            }
        });
    }

    private void detailsBottomSheet(ModelAdminUser modelAdminUser) {
        //bottom sheet
        BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(context);
        //Inflateview for bottom sheet
        View view=LayoutInflater.from(context).inflate(R.layout.bs_users_details_admin,null);
        //set view to bottom sheet
        bottomSheetDialog.setContentView(view);

        ProgressDialog progressDialog=new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //init view of bottom sheet
        ImageButton backBtn=view.findViewById(R.id.backBtn);
        ImageButton orderDetailsBtn=view.findViewById(R.id.orderDetailsBtn);
        ImageButton deleteBtn=view.findViewById(R.id.deleteBtn);
        ImageButton disableBtn=view.findViewById(R.id.disableBtn);
        ImageButton enableBtn=view.findViewById(R.id.enableBtn);
        ImageView profileIv=view.findViewById(R.id.profileIv);
        ImageView onlineIv=view.findViewById(R.id.onlineIv);
        ImageView offlineIv=view.findViewById(R.id.offlineIv);
        TextView userIdTv=view.findViewById(R.id.userIdTv);
        TextView nameTv=view.findViewById(R.id.nameTv);
        TextView countryTv=view.findViewById(R.id.countryTv);
        TextView stateTv=view.findViewById(R.id.stateTv);
        TextView addressTv=view.findViewById(R.id.addressTv);
        ImageButton callBtn=view.findViewById(R.id.callBtn);
        ImageButton mailBtn=view.findViewById(R.id.mailBtn);

        //get data
        String accountType= modelAdminUser.getAccountType();
        String address= modelAdminUser.getAddress();
        String city= modelAdminUser.getCity();
        String country= modelAdminUser.getCountry();
        String email= modelAdminUser.getEmail();
        String latitude= modelAdminUser.getLatitude();
        String langitude= modelAdminUser.getLongitude();
        String online= modelAdminUser.getOnline();
        String name= modelAdminUser.getName();
        String state= modelAdminUser.getState();
        String phone= modelAdminUser.getPhone();
        String uid= modelAdminUser.getUid();
        String timestamp= modelAdminUser.getTimestamp();
        String enabled=modelAdminUser.getEnabled();
        String deleted=modelAdminUser.getDeleted();
        String profileImage= modelAdminUser.getProfileImage();

        //initially enable button is invisible and disabled
        if (enabled.equals("false")){
            enableBtn.setVisibility(View.VISIBLE);
            enableBtn.setEnabled(true);

            disableBtn.setVisibility(View.GONE);
            disableBtn.setEnabled(false);

        }
        else {
            enableBtn.setVisibility(View.GONE);
            enableBtn.setEnabled(false);

            disableBtn.setVisibility(View.VISIBLE);
            disableBtn.setEnabled(true);
        }


        //set data
        userIdTv.setText(uid);
        nameTv.setText(name);
        countryTv.setText(country);
        stateTv.setText(state);
        addressTv.setText(address);

        //online/offline
        if(online.equals("true")){
            //shop owner is online
            onlineIv.setVisibility(View.VISIBLE);
            offlineIv.setVisibility(View.GONE);
        }
        else {
            //shop owner is offline
            onlineIv.setVisibility(View.GONE);
            offlineIv.setVisibility(View.VISIBLE);
        }

        //profile pic
        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_add_person_white).into(profileIv);
        }
        catch (Exception e){
            profileIv.setImageResource(R.drawable.ic_add_person_white);
        }
        //show dialog
        bottomSheetDialog.show();

        //back click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });

        //delete click
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete confirm dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete ")
                        .setMessage("Are you sure want to delete "+name+"?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                progressDialog.setMessage("Deleting "+name+" ...");
                                progressDialog.show();

                                //set data
                                HashMap<String,Object> hashMap=new HashMap<>();
                                hashMap.put("deleted","true");


                                //update to db
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(uid).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                progressDialog.dismiss();

                                                AlertDialog.Builder alertBuilder=new AlertDialog.Builder(context);
                                                alertBuilder.setTitle("Important")
                                                        .setMessage("User account has been deleted, Please confirm through email or sms")
                                                        .setPositiveButton("Email", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {


                                                                String msg="Hello,\n" +
                                                                        "Your Shop2Order application account linked with "+ email+" has been removed by the admin.\n"+
                                                                        "Thanks,\n" +
                                                                        "\n" +
                                                                        "Your Shop2Order team";
                                                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                                                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                                                                intent.putExtra(Intent.EXTRA_SUBJECT, "Account Deleted");
                                                                intent.putExtra(Intent.EXTRA_TEXT,msg);
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
                                                deleteUser(uid); //id is the product id
                                                bottomSheetDialog.dismiss();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });


                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //cancel,dismiss dialog
                                dialog.cancel();
                            }
                        })
                        .show();

            }
        });

        //user order details click
        orderDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setMessage("Do you want to see "+name+"'s order details?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                bottomSheetDialog.dismiss();
                                Intent intent=new Intent(context, AdminUserOrderListActivity.class);
                                intent.putExtra("userUid",uid);
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();

                            }
                        })
                        .show();

            }
        });

        //disable user button click
        disableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Block User")
                        .setMessage("Do you want to block "+name+"'s account....!")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.setMessage("Blocking "+name+" ...");
                                progressDialog.show();

                                //set data
                                HashMap<String,Object> hashMap=new HashMap<>();
                                hashMap.put("enabled","false");

                                //update to db
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(uid).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(context,"User account blocked successfully",Toast.LENGTH_SHORT).show();


                                                //send email to user
                                                AlertDialog.Builder builder1=new AlertDialog.Builder(context);
                                                builder1.setTitle("Confirm Blocking")
                                                        .setMessage("Please confirm "+name+"'s account blocking through email....")
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                String msg="Hello,\n" +
                                                                        "Your Shop2Order application account linked with "+ email+" has been blocked by the admin.\n"+
                                                                        "Thanks,\n" +
                                                                        "\n" +
                                                                        "Your Shop2Order team";
                                                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                                                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                                                                intent.putExtra(Intent.EXTRA_SUBJECT, "Account Deleted");
                                                                intent.putExtra(Intent.EXTRA_TEXT,msg);
                                                                context.startActivity(intent);
                                                            }
                                                        })
                                                        .show();

                                                bottomSheetDialog.dismiss();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });


                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setNeutralButton("More Details", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder builder1=new AlertDialog.Builder(context);
                                builder1.setMessage("This action will block the user. You can later unblock him")
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
        });

        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Unblock User")
                        .setMessage("Do you want to unblock "+name+"'s account....!")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.setMessage("Unlocking "+name+" ...");
                                progressDialog.show();

                                //set data
                                HashMap<String,Object> hashMap=new HashMap<>();
                                hashMap.put("enabled","true");

                                //update to db
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(uid).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(context,"User account unblocked successfully",Toast.LENGTH_SHORT).show();


                                                //send email to user
                                                AlertDialog.Builder builder1=new AlertDialog.Builder(context);
                                                builder1.setTitle("Confirm Blocking")
                                                        .setMessage("Please confirm "+name+"'s account blocking through email....")
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                String msg="Hello,\n" +
                                                                        "Your Shop2Order application account linked with "+ email+" has been unblocked by the admin.\n"+
                                                                        "Thanks,\n" +
                                                                        "\n" +
                                                                        "Your Shop2Order team";
                                                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                                                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                                                                intent.putExtra(Intent.EXTRA_SUBJECT, "Account Deleted");
                                                                intent.putExtra(Intent.EXTRA_TEXT,msg);
                                                                context.startActivity(intent);
                                                            }
                                                        })
                                                        .show();

                                                bottomSheetDialog.dismiss();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });


                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setNeutralButton("More Details", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder builder1=new AlertDialog.Builder(context);
                                builder1.setMessage("This action will unblock the user. You can later block him")
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
        });

        //call button clicked
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setMessage("Do you want to contact "+name+" through phone call?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(phone))));
                                Toast.makeText(context,""+phone,Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               dialog.cancel();
                            }
                        })
                        .show();

            }
        });

        //mail button clicked
        mailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setMessage("Do you want to contact "+name+" through email")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Subject here");
                                intent.putExtra(Intent.EXTRA_TEXT,"Body Here");
                                context.startActivity(intent);

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();

            }
        });
    }




    private void deleteUser(String uid) {
        //delete user using his/her uid

        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //User deleted
                        Toast.makeText(context,"User deleted successfully...",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed deleting product
                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }




    @Override
    public int getItemCount() {
        return AdminUserList.size(); //return number of records
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            filter=new FilterAdminUsers(this,filterList);
        }
        return filter;
    }

    //view holder
    class HolderAdminUser extends RecyclerView.ViewHolder{

        //ui views of row_admin_user.xml
        private ImageView profileIv,onlineIv;
        private TextView nameTv,addressTv,phoneTv,emailTv,userBlockTv;

        public HolderAdminUser(@NonNull View itemView) {
            super(itemView);
            profileIv=itemView.findViewById(R.id.profileIv);
            onlineIv=itemView.findViewById(R.id.onlineIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            addressTv=itemView.findViewById(R.id.addressTv);
            phoneTv=itemView.findViewById(R.id.phoneTv);
            emailTv=itemView.findViewById(R.id.emailTv);
            userBlockTv=itemView.findViewById(R.id.userBlockTv);
        }
    }
}

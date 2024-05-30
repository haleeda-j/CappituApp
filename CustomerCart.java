package com.example.loginpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.loginpage.Adapter.CustomerCartAdapter;
import com.example.loginpage.Adapter.CustomerCartItemList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class CustomerCart extends AppCompatActivity {
    ListView itemListView;
    Handler handler;
    CustomerCartAdapter cartAdapter;
    ArrayList<CustomerCartItemList> items = new ArrayList<>();
    TextView tvCafeName,tvPricetotal, tvDeliveryfess, tvFinalTotal, tvWalletBalance;
    EditText etAddress;
    Button btnCheckOut, btnBack, btnTopUp;
    private DatabaseReference mDatabase, orderReference, cafeReference, customerReference;
    FirebaseAuth mUser;
    String cName, orderItemName,orderItemprice, orderItemQty, wallet;
    Double total = 0.0, sum = 0.0, price =0.0, finalTotal = 0.0, amountLeft = 0.0;
    int qty = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_cart);
        Intent intent = getIntent();
        cName = intent.getStringExtra("cafeName");

        itemListView = findViewById(R.id.customer_cart_listview);
        tvPricetotal = findViewById(R.id.tv_customer_cart_subtotal_price);
        tvCafeName = findViewById(R.id.tv_customer_cart_cafename);
        btnCheckOut = findViewById(R.id.btn_customer_cart_check_out);
        btnBack = findViewById(R.id.btn_customer_cart_back);
        etAddress = findViewById(R.id.et_customer_cart_address);

        tvDeliveryfess = findViewById(R.id.tv_customer_cart_delivery_fees);
        tvFinalTotal = findViewById(R.id.tv_customer_cart_total_price);
        tvWalletBalance = findViewById(R.id.tv_customer_cart_wallet_balance);
        btnTopUp = findViewById(R.id.btn_customer_cart_topUp);

        mUser = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("AddToCart");
        orderReference = FirebaseDatabase.getInstance().getReference("Order");
        cafeReference = FirebaseDatabase.getInstance().getReference("Cafe");
        customerReference = FirebaseDatabase.getInstance().getReference("Customer");
        tvPricetotal.setText("0");
        tvDeliveryfess.setText("0");
        tvFinalTotal.setText("0");


        tvCafeName.setText(cName);

        customerReference.child(mUser.getCurrentUser().getUid()).child("walletAmount").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    wallet = task.getResult().getValue().toString();
                    tvWalletBalance.setText(wallet);
                }
            }
        });

        btnTopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CustomerCart.this, Payment.class);
                i.putExtra("balance", wallet);
                startActivity(i);
            }
        });


        btnCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String addr =  etAddress.getText().toString();
                if(addr.isEmpty()){
                    etAddress.setError("Please fill up the address");
                }
                else if(tvPricetotal.getText().toString().equals("0")){
                    Toast.makeText(CustomerCart.this, "The cart is empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    float walletBalance = Float.parseFloat(wallet);
                    if(walletBalance <finalTotal){
                        AlertDialog.Builder alert = new AlertDialog.Builder(CustomerCart.this);
                        alert.setTitle("Insufficient wallet amount");
                        alert.setMessage("The wallet amount is insufficient. Do you want to top up the wallet?");
                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent2 = new Intent(CustomerCart.this, Payment.class);
                                intent2.putExtra("balance", wallet);
                                startActivity(intent2);
                            }
                        });
                        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                        alert.show();
                    }
                    else {
                        amountLeft = walletBalance - finalTotal;
                        String id = orderReference.push().getKey();
                        String userID = mUser.getCurrentUser().getUid();
                        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("CustomerAddress", addr);
                        hashMap.put("CafeName", cName);
                        hashMap.put("OrderId", id);
                        hashMap.put("totalAmount", "" + finalTotal);
                        hashMap.put("Sub Total", "" + total);
                        hashMap.put("Ordered Date", currentDate);
                        hashMap.put("Ordered Time", currentTime);
                        hashMap.put("Order Status", "pending");
                        hashMap.put("Customer ID", userID);

                        mDatabase.child(userID).child(cName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if(task.isSuccessful()){
                                    DataSnapshot result = task.getResult();
                                    if(result != null){
                                        for (DataSnapshot ch : result.getChildren()){
                                            orderReference.child(id).child("ItemList")
                                                    .child(ch.getKey().toString()).child("ItemName").setValue(ch.getKey().toString());
                                            orderReference.child(id).child("ItemList")
                                                    .child(ch.getKey().toString()).child("quantity").setValue(ch.child("quantity").getValue().toString());
                                            orderReference.child(id).child("ItemList")
                                                    .child(ch.getKey().toString()).child("price").setValue(ch.child("Price").getValue().toString());
                                        }
                                    }
                                }
                            }
                        });

                        orderReference.child(id).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(CustomerCart.this, "Order Placed Successfully", Toast.LENGTH_SHORT).show();
                                mDatabase.child(mUser.getCurrentUser().getUid()).child(cName).removeValue();
                            }
                        });
                        customerReference.child(mUser.getCurrentUser().getUid()).child("walletAmount").setValue(String.valueOf(amountLeft));

                        Intent i = new Intent(CustomerCart.this, CustomerProfile.class);
                       // i.putExtra("cafeName", cName);
                       // i.putExtra("Order ID", id);
                        i.putExtra("signal", "order");
                        startActivity(i);
                    }
                }

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cafeReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            DataSnapshot result = task.getResult();
                            if (result != null) {
                                for (DataSnapshot child : result.getChildren()) {
                                    String cafeID = child.getKey().toString();
                                    //Toast.makeText(CustomerMenuCafe.this, cafeID, Toast.LENGTH_SHORT).show();
                                    String cafeName = child.child("CafeName").getValue(String.class);
                                    //String cafeSpot = child.child("AreaName").getValue(String.class);
                                    String cafeImageUri = child.child("CafeImageURL").getValue().toString();
                                    if(cafeName.equals(cName)){
                                        Intent i = new Intent(CustomerCart.this, CustomerMenuItem.class);
                                        i.putExtra("cafeID", cafeID);
                                        i.putExtra("cafeName", cName);
                                        i.putExtra("cafeImage", cafeImageUri);
                                        startActivity(i);
                                    }
                                }
                            }
                        }
                    }
                });

            }
        });

        handler = new Handler(Looper.myLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 1:
                        cartAdapter = new CustomerCartAdapter(CustomerCart.this,R.layout.design_customer_cart_recycler_row, items);
                        itemListView.setAdapter(cartAdapter);
                        Log.d("TA1G", "handleMessage: ");
                        break;
                }
            }
        };
        loadItems(handler,cName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        customerReference.child(mUser.getCurrentUser().getUid()).child("walletAmount").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    wallet = task.getResult().getValue().toString();
                    tvWalletBalance.setText(String.format("%.2f", Float.parseFloat(wallet)));
                }
            }
        });
    }


    private void loadItems(Handler handler, String cName){
        //Toast.makeText(CustomerMenuItem.this, cafeID, Toast.LENGTH_SHORT).show();

        mDatabase.child(mUser.getCurrentUser().getUid()).child(cName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    DataSnapshot result = task.getResult();
                    if (result.hasChildren()) {
                        for (DataSnapshot child : result.getChildren()) {
                            orderItemName = child.child("productName").getValue().toString();
                            orderItemprice = child.child("Price").getValue().toString();
                            //Log.d("firebase", ItemName);
                            orderItemQty = child.child("quantity").getValue().toString();
                            String orderItemUri = child.child("productImage").getValue().toString();
                            String orderItemDesc = child.child("productDesc").getValue().toString();
                            price = Double.parseDouble(orderItemprice);
                            qty = Integer.parseInt(orderItemQty);
                            total = total + price * qty;
                            finalTotal = total +2;

                            CustomerCartItemList item = new CustomerCartItemList(orderItemName, orderItemprice, orderItemQty, cName, orderItemDesc, orderItemUri);
                            items.add(item);
                        }
                        tvPricetotal.setText("RM " + String.format("%.2f", total));
                        tvDeliveryfess.setText("RM 2.00");
                        tvFinalTotal.setText("RM "+ String.format("%.2f", finalTotal));
                        handler.sendEmptyMessage(1);
                        Log.d("TG", "onComplete: ok");
                    }
                    else {
                        Toast.makeText(CustomerCart.this, "The cart is empty", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });
    }
}
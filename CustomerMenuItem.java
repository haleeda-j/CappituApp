package com.example.loginpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.loginpage.Adapter.CustomerMenuItemAdapter;
import com.example.loginpage.Adapter.CustomerMenuItemList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomerMenuItem extends AppCompatActivity {
    ListView itemListView;
    Handler handler;
    CustomerMenuItemAdapter itemAdapter;
    ArrayList<CustomerMenuItemList> items = new ArrayList<>();
    TextView cafeNameTV;
    private DatabaseReference mDatabase;
    private String cafeName, cafeID, cafeImage;
    Button cart;
    ImageView ivItemPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_menu_item);
        Intent intent = getIntent();
        cafeName = intent.getStringExtra("cafeName");
        cafeID = intent.getStringExtra("cafeID");
        cafeImage = intent.getStringExtra("cafeImage");

        mDatabase = FirebaseDatabase.getInstance().getReference("Cafe");

        cafeNameTV = findViewById(R.id.cafe_detail_cafe_name);
        cafeNameTV.setText(cafeName);

        ImageView cafeImg = findViewById(R.id.cafe_detail_cafe_photo);
        Picasso.with(this).load(Uri.parse(cafeImage)).into(cafeImg);

        itemListView = findViewById(R.id.cafe_detail_items_listview);

        cart = findViewById(R.id.cafe_detail_cafe_cart_btn);
        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomerMenuItem.this, CustomerCart.class);
                intent.putExtra("cafeName", cafeName);
                startActivity(intent);
            }
        });


        handler = new Handler(Looper.myLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 1:
                        itemAdapter = new CustomerMenuItemAdapter(CustomerMenuItem.this,R.layout.design_customer_menu_item_recycler_row,items);
                        itemListView.setAdapter(itemAdapter);
                        Log.d("TA1G", "handleMessage: ");
                        break;
                }
            }
        };
        loadItems(handler,cafeID,cafeName);
    }
    private void loadItems(Handler handler,String cafeID, String cName){
        //Toast.makeText(CustomerMenuItem.this, cafeID, Toast.LENGTH_SHORT).show();

        mDatabase.child(cafeID).child("Item").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    DataSnapshot result = task.getResult();
                    for (DataSnapshot child : result.getChildren()) {
                                  String itemName = child.child("ItemName").getValue().toString();

                                Log.d("firebase", itemName);
                                String price = child.child("price").getValue().toString();
                                String imageId = child.child("itemImageURL").getValue().toString();
                                String description = child.child("Description").getValue().toString();
                                CustomerMenuItemList item = new CustomerMenuItemList(itemName,description,price,imageId, cName);
                                items.add(item);

                    }
                    handler.sendEmptyMessage(1);
                    Log.d("TG", "onComplete: ok");
                }
            }

        });
    }
}
package com.example.loginpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.loginpage.Adapter.CustomerMenuCafeAdapter;
import com.example.loginpage.Adapter.CustomerMenuCafeList;
import com.example.loginpage.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CustomerMenuCafe extends AppCompatActivity {
    ListView cafeListView;
    Handler handler;
    CustomerMenuCafeAdapter cafeListAdapter;
    ArrayList<CustomerMenuCafeList> cafes = new ArrayList<>();
    private DatabaseReference mDatabase;
    TextView spotTV;

    private ImageView spotImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_menu_cafe);
        Intent intent = getIntent();
        String spotName = intent.getStringExtra("SPOT_NAME");
        spotTV = findViewById(R.id.cafe_SpotName);
        spotImage = findViewById(R.id.cafe_spotImg);
        spotTV.setText(spotName);
        loadSpotImg(spotName);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        cafeListView = findViewById(R.id.cafe_listview);

        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 1:
                        cafeListAdapter = new CustomerMenuCafeAdapter(CustomerMenuCafe.this, R.layout.design_customer_menu_cafe_recycler_row, cafes);
                        cafeListView.setAdapter(cafeListAdapter);
                        cafeListView.setVisibility(View.VISIBLE);
//                        Toast.makeText(CafeActivity.this, "Data loaded successfully", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", String.valueOf(cafes.size()));
                        break;
                    case 0:
                        Toast.makeText(CustomerMenuCafe.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        load_cafes(handler,spotName);
    }
    private void load_cafes(Handler handler,String spotName) {
        //根据AreaName寻找对应的Cafe
        mDatabase.child("Cafe").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot result = task.getResult();
                    if (result != null) {
                        for (DataSnapshot child : result.getChildren()) {
                            String cafeID = child.getKey().toString();
                            //Toast.makeText(CustomerMenuCafe.this, cafeID, Toast.LENGTH_SHORT).show();
                            String cafeName = child.child("CafeName").getValue(String.class);
                            String cafeSpot = child.child("AreaName").getValue(String.class);
                            String cafeImageUri = child.child("CafeImageURL").getValue().toString();
                            if(cafeSpot.equals(spotName)){

                                    CustomerMenuCafeList cafe = new CustomerMenuCafeList(cafeName,cafeID, cafeImageUri);
                                    cafes.add(cafe);

                            }
                        }
                        handler.sendEmptyMessage(1);
                    } else {
                        handler.sendEmptyMessage(0);
                    }
                } else {
                    Log.e("firebase", "Error getting data", task.getException());
                    handler.sendEmptyMessage(0);
                }
            }
        });
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private void loadSpotImg(String spotName){
        switch (spotName){
            case "KPZ":
                spotImage.setBackground(getDrawable(R.drawable.kpz));
                break;
            case "PUSANIKA":
                spotImage.setBackground(getDrawable(R.drawable.pusanika));
                break;

            case "FSSK":
                spotImage.setBackground(getDrawable(R.drawable.fssk));
                break;
        }
    }
}
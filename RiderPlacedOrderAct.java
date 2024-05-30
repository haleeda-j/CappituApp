package com.example.loginpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RiderPlacedOrderAct extends AppCompatActivity {
    Button updateBtn, btnBack;
    TextView tvOrderid, tvCafeName, tvOrderDate, tvOrderTime, tvDeliveryAddress, tvSubtotal, tvDeliveryFees, tvTotal;
    ListView lvItemList;
    List itemList;
    ArrayAdapter<String> adapter;
    DatabaseReference orderReference,customerReferece;
    DatabaseReference riderRef;
    String cOrderID;
    String cStatus;
    FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_order_details);
        Intent i = getIntent();
        cOrderID = i.getStringExtra("order_id");
        cStatus = i.getStringExtra("status");
        tvOrderid = findViewById(R.id.tv_rider_order_details_orderid);
        tvCafeName = findViewById(R.id.tv_rider_order_details_cafename);
        tvOrderDate = findViewById(R.id.tv_rider_order_details_orderdate);
        tvOrderTime = findViewById(R.id.tv_rider_order_details_ordetime);
        tvDeliveryAddress = findViewById(R.id.tv_rider_order_details_deliveryadress);
        tvSubtotal = findViewById(R.id.tv_rider_order_details_subtotal_price);
        tvDeliveryFees = findViewById(R.id.tv_rider_order_details_delivery_fees);
        tvTotal = findViewById(R.id.tv_rider_order_details_total_price);
        updateBtn = findViewById(R.id.btn_rider_order_details_status);
        btnBack = findViewById(R.id.btn_rider_order_back);
//        btnStatus = findViewById(R.id.btn_rider_order_details_status);

        lvItemList = findViewById(R.id.lv_rider_order_details_item);

        orderReference = FirebaseDatabase.getInstance().getReference("Order");
        riderRef = FirebaseDatabase.getInstance().getReference("Rider");
        customerReferece = FirebaseDatabase.getInstance().getReference("Customer");

        mAuth = FirebaseAuth.getInstance();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        adapter.clear();

        listViewController(lvItemList);
        if(cStatus.equals("completed")){
            updateBtn.setVisibility(View.INVISIBLE);
            updateBtn.setClickable(false);
        }
        else{
            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateStatus();
                }
            });
        }
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RiderPlacedOrderAct.this, RiderProfile.class);
                i.putExtra("signal","order");
                startActivity(i);
            }
        });

        orderReference.child(cOrderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot != null) {
                    String ocafeName = snapshot.child("CafeName").getValue().toString();
                    String oDate = snapshot.child("Ordered Date").getValue().toString();
                    String oTime = snapshot.child("Ordered Time").getValue().toString();
                    String cID = snapshot.child("Customer ID").getValue().toString();
                    String oAddress = snapshot.child("CustomerAddress").getValue().toString();
                    String osubtotal = snapshot.child("Sub Total").getValue().toString();
                    String oTotal = snapshot.child("totalAmount").getValue().toString();
                    String oStatus = snapshot.child("Order Status").getValue().toString();
                    customerReferece.child(cID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot sn) {
                            String cName = sn.child("CustomerName").getValue().toString();
                            String cContact = sn.child("TelNo").getValue().toString();
                            tvOrderid.setText(cOrderID);
                            tvCafeName.setText(ocafeName);
                            tvOrderDate.setText(cName);
                            tvOrderTime.setText(cContact);
                            tvDeliveryAddress.setText(oAddress);
                            tvSubtotal.setText(String.format("RM %.2f", Float.parseFloat(osubtotal)));
                            tvTotal.setText(String.format("RM %.2f", Float.parseFloat(oTotal)));
                            tvDeliveryFees.setText("RM 2.00");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

//                    btnStatus.setText(oStatus);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
       /* orderReference.child(cOrderID).child("ItemList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ch : snapshot.getChildren()){
                    String iName = ch.child("ItemName").getValue().toString();
                    String iPrice = ch.child("price").getValue().toString();
                    String iQty = ch.child("quantity").getValue().toString();
                    //OrderedItems iOrder = new OrderedItems(iName, iPrice, iQty);
                    //itemList.add(iOrder);
                    adapter.add("Item: " +iName + "\n" + "Price: RM " + iPrice + " x" + iQty);
                    lvItemList.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
        orderReference.child(cOrderID).child("ItemList").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if(dataSnapshot != null) {
                        for (DataSnapshot ch : dataSnapshot.getChildren()) {
                            String iName = ch.child("ItemName").getValue().toString();
                            String iPrice = ch.child("price").getValue().toString();
                            String iQty = ch.child("quantity").getValue().toString();
                            //OrderedItems iOrder = new OrderedItems(iName, iPrice, iQty);
                            //itemList.add(iOrder);
                            adapter.add("Item: " + iName + "\n" + "Price: RM " + iPrice + " x" + iQty);
                            lvItemList.setAdapter(adapter);
                        }
                    }

                }
            }
        });
    }

    private void updateStatus(){
        switch (cStatus){
            case "in the kitchen":
                cStatus = "on delivery";
                orderReference.child(cOrderID).child("Order Status").setValue(cStatus);
                Intent i = new Intent(RiderPlacedOrderAct.this, RiderProfile.class);
                i.putExtra("signal","order");
                startActivity(i);
                break;
            case "on delivery":
                cStatus = "completed";
                orderReference.child(cOrderID).child("Order Status").setValue(cStatus);
                //
                String time = getTime();
                orderReference.child(cOrderID).child("Finish Time").setValue(time);

                String riderId = mAuth.getCurrentUser().getUid();
                riderRef.child(riderId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        }
                        else {
                            long earn = (long) task.getResult().child("TotalEarning").getValue() + 2;
                            long or = (long) task.getResult().child("TotalOrder").getValue() + 1;
                            riderRef.child(riderId).child("TotalEarning").setValue(earn);
                            riderRef.child(riderId).child("RiderStatus").setValue("online");
                            riderRef.child(riderId).child("TotalOrder").setValue(or);
                            Intent i = new Intent(RiderPlacedOrderAct.this, RiderProfile.class);
                            i.putExtra("signal","order");
                            startActivity(i);

                        }
                    }
                });;


                break;
            default:
                break;
        }
    }

    private String getTime(){
        String formattedDateTime = null;
        LocalDateTime now = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            now = LocalDateTime.now();
        }

        // 指定日期时间格式
        DateTimeFormatter formatter = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mma");
        }

        // 格式化日期时间
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formattedDateTime = now.format(formatter);
        }
        return formattedDateTime;
    }


    private void listViewController(ListView listView){
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                listView.requestDisallowInterceptTouchEvent(true);
                int action = motionEvent.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_UP:
                        listView.requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });

    }
}

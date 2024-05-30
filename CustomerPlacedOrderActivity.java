package com.example.loginpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.loginpage.Adapter.CustomerMenuCafeList;
import com.example.loginpage.Adapter.OrderedItems;
import com.example.loginpage.Profile.CustomerOrderFragment;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CustomerPlacedOrderActivity extends AppCompatActivity {

    Button btnStatus, back;
    TextView tvOrderid, tvRiderName, tvRiderVehNo, tvRiderNo, tvDeliveryAddress, tvSubtotal, tvDeliveryFees, tvTotal;
    TextView tvRName, tvRVeh, tvRContact;
    ListView lvItemList;
    List itemList;
    ArrayAdapter<String> adapter;
    DatabaseReference cafeReference, orderReference,riderReference;
    String cName, cOrderID,cRiderID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_placed_order);
        Intent i = getIntent();
        cName = i.getStringExtra("cafeName");
        cOrderID = i.getStringExtra("Order ID");
        cRiderID = i.getStringExtra("Rider ID");

        tvOrderid = findViewById(R.id.tv_customer_order_details_orderid);
        tvDeliveryAddress = findViewById(R.id.tv_customer_order_details_deliveryadress);
        tvRiderName = findViewById(R.id.tv_customer_order_details_ridername);
        tvRiderVehNo= findViewById(R.id.tv_customer_order_details_ridervehno);
        tvRiderNo = findViewById(R.id.tv_customer_order_details_ridercontactno);
        tvSubtotal = findViewById(R.id.tv_customer_order_details_subtotal_price);
        tvDeliveryFees = findViewById(R.id.tv_customer_order_details_delivery_fees);
        tvTotal = findViewById(R.id.tv_customer_order_details_total_price);
        back = findViewById(R.id.btn_customer_order_back);
        btnStatus = findViewById(R.id.btn_customer_order_details_status);

        tvRName = findViewById(R.id.textViewridername);
        tvRContact = findViewById(R.id.textViewriderno);
        tvRVeh = findViewById(R.id.textViewridervehno);

        lvItemList = findViewById(R.id.lv_custoemr_order_details_item);


        cafeReference = FirebaseDatabase.getInstance().getReference("Cafe");
        orderReference = FirebaseDatabase.getInstance().getReference("Order");
        riderReference=FirebaseDatabase.getInstance().getReference("Rider");
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        adapter.clear();

        listViewController(lvItemList);


        // Retrieve data from the "Rider" table based on the rider ID
        orderReference.child(cOrderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String oRiderID = snapshot.child("RiderID").getValue(String.class);
                    String oStatus = snapshot.child("Order Status").getValue(String.class);
                    // Now that you have the rider ID, retrieve rider details
                    if (isValidStatus(oStatus)) {
                        retrieveRiderDetails(oRiderID,oStatus);
                    }else{
                        setRiderDetailsLoading();
                    }
                    // Retrieve other order details (oAddress, osubtotal, oTotal, oStatus)
                    String oAddress = snapshot.child("CustomerAddress").getValue(String.class);
                    String osubtotal = snapshot.child("Sub Total").getValue().toString();
                    String oTotal = snapshot.child("totalAmount").getValue().toString();
                    oStatus = snapshot.child("Order Status").getValue(String.class);

                    tvOrderid.setText(cOrderID);
                    tvDeliveryAddress.setText(oAddress);
                    tvSubtotal.setText(String.format("RM %.2f", Float.parseFloat(osubtotal)));
                    tvTotal.setText(String.format("RM %.2f", Float.parseFloat(oTotal)));
                    tvDeliveryFees.setText("RM 2.00");
                    btnStatus.setText(oStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        orderReference.child(cOrderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null) {
                    // String ocafeName = snapshot.child("CafeName").getValue().toString();
                    // String oDate = snapshot.child("Ordered Date").getValue().toString();
                    //  String oTime = snapshot.child("Ordered Time").getValue().toString();
                    String oAddress = snapshot.child("CustomerAddress").getValue().toString();
                    String osubtotal = snapshot.child("Sub Total").getValue().toString();
                    String oTotal = snapshot.child("totalAmount").getValue().toString();
                    String oStatus = snapshot.child("Order Status").getValue().toString();

                    tvOrderid.setText(cOrderID);

                    tvDeliveryAddress.setText(oAddress);
                    tvSubtotal.setText(String.format("RM %.2f", Float.parseFloat(osubtotal)));
                    tvTotal.setText(String.format("RM %.2f", Float.parseFloat(oTotal)));
                    tvDeliveryFees.setText("RM 2.00");
                    btnStatus.setText(oStatus);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        orderReference.child(cOrderID).child("ItemList").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot != null) {
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

        btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(CustomerPlacedOrderActivity.this, CustomerProfile.class);
                i.putExtra("signal", "order");
                startActivity(i);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cName != null) {
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
                                        if (cafeName.equals(cName)) {
                                            Intent i = new Intent(CustomerPlacedOrderActivity.this, CustomerMenuItem.class);
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
                } else {
                    Intent i = new Intent(CustomerPlacedOrderActivity.this, CustomerProfile.class);
                    i.putExtra("signal", "order");
                    startActivity(i);
                }

            }
        });
    }
    private void retrieveRiderDetails(String oRiderID,String oStatus){
        // Display "loading" for rider details when oStatus is "pending"
        if (oStatus.equals("pending")||oStatus.equals("cancelled")) {
            setRiderDetailsLoading();
            return; // No need to proceed with data retrieval
        }
        riderReference.child(oRiderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String riderName = snapshot.child("RiderName").getValue(String.class);
                    String riderVehNo = snapshot.child("VehicleNo").getValue(String.class);
                    String riderNo = snapshot.child("TelNo").getValue(String.class);

                    // Set the rider details in the corresponding TextViews
                    tvRiderName.setText(riderName);
                    tvRiderVehNo.setText(riderVehNo);
                    tvRiderNo.setText(riderNo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors in data retrieval
            }
        });
    }

    private void listViewController(ListView lvItemList) {
        lvItemList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                lvItemList.requestDisallowInterceptTouchEvent(true);
                int action = motionEvent.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_UP:
                        lvItemList.requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
    }

    private void setRiderDetailsLoading() {
        // Display "Loading" for rider details
       /* tvRiderName.setText("Loading");
        tvRiderVehNo.setText("Loading");
        tvRiderNo.setText("Loading");*/
        tvRiderName.setVisibility(View.INVISIBLE);
        tvRiderVehNo.setVisibility(View.INVISIBLE);
        tvRiderNo.setVisibility(View.INVISIBLE);
        tvRVeh.setVisibility(View.INVISIBLE);
        tvRName.setVisibility(View.INVISIBLE);
        tvRContact.setText("Waiting Cafe's respond...");
    }

    private boolean isValidStatus(String oStatus) {
        // Specify the valid statuses
        return oStatus.equals("in the kitchen") || oStatus.equals("on delivery") || oStatus.equals("completed");
    }
}
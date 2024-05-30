package com.example.loginpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.loginpage.Adapter.SalesItem;
import com.example.loginpage.Adapter.SalesListAdapter;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CafeReportingActivity extends AppCompatActivity {

    PieChart pieChart;
    List<PieEntry> pieEntries;
    DatabaseReference orderReference, databaseReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String cName;
    TextView completedOrder, cancelledOrder;

    RecyclerView recyclerView;
    SalesListAdapter salesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe_reporting2);

        pieEntries = new ArrayList<>();

        pieChart = findViewById(R.id.chart);
        completedOrder = findViewById(R.id.tv_completedOrder);
        cancelledOrder = findViewById(R.id.tv_cancelledOrder);


        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        salesListAdapter = new SalesListAdapter();
        recyclerView.setAdapter(salesListAdapter);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        String userID = firebaseUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Cafe");
        orderReference = FirebaseDatabase.getInstance().getReference("Order");

        databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                cName = snapshot.child("CafeName").getValue().toString();
                fetchDataForPieChart(cName);
                fetchDataForSalesList(cName);
                fetchDataForOrder(cName);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void fetchDataForOrder(String targetRestaurantName) {
        orderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int completedOrders = 0;
                int cancelledOrders = 0;

                // Get current month
                Calendar calendar = Calendar.getInstance();
                int currentMonth = calendar.get(Calendar.MONTH) + 1; // Months are 0-based

                // Iterate through the orders
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    // Check if the order belongs to the target restaurant
                    String cafeName = orderSnapshot.child("CafeName").getValue(String.class);

                    if (cafeName.equalsIgnoreCase(targetRestaurantName)) {
                        // Get the order status and Ordered Time
                        String orderStatus = orderSnapshot.child("Order Status").getValue(String.class);

                        String orderTime = orderSnapshot.child("Ordered Date").getValue(String.class);
                        int orderMonth = getMonthFromDateString(orderTime);

                        // Use equalsIgnoreCase for case-insensitive comparison
                        if (orderMonth == currentMonth) {
                            if ("completed".equalsIgnoreCase(orderStatus)) {
                                completedOrders++;
                            } else if ("cancelled".equalsIgnoreCase(orderStatus)) {
                                cancelledOrders++;
                            }
                        }
                    }
                }

                // Now you have the counts for completed and cancelled orders
                // You can use these counts as needed
                Log.d("OrderCounts", "Completed Orders: " + completedOrders + ", Cancelled Orders: " + cancelledOrders);
                completedOrder.setText(String.valueOf(completedOrders));
                cancelledOrder.setText(String.valueOf(cancelledOrders));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }


    private void fetchDataForSalesList(String targetRestaurantName) {
        orderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<SalesItem> salesItemList = new ArrayList<>();

                // Get current month
                Calendar calendar = Calendar.getInstance();
                int currentMonth = calendar.get(Calendar.MONTH) + 1; // Months are 0-based

                // Iterate through the orders
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    // Check if the order is from the current month
                    String orderTime = orderSnapshot.child("Ordered Date").getValue(String.class);
                    int orderMonth = getMonthFromDateString(orderTime);
//                    Toast.makeText(CafeReportingActivity.this, ""+orderMonth+""+currentMonth, Toast.LENGTH_SHORT).show();

                    String cafeName = orderSnapshot.child("CafeName").getValue(String.class);

                    // Use equalsIgnoreCase for case-insensitive comparison
                    if (orderMonth == currentMonth && cafeName.equalsIgnoreCase(targetRestaurantName)) {
                        // Get the items list for the order
                        DataSnapshot itemListSnapshot = orderSnapshot.child("ItemList");

                        // Iterate through items
                        for (DataSnapshot itemSnapshot : itemListSnapshot.getChildren()) {
                            String itemName = itemSnapshot.child("ItemName").getValue(String.class);
                            String quantityString = itemSnapshot.child("quantity").getValue(String.class);
                            int itemQuantity = Integer.parseInt(quantityString);

                            // Check if the item is already in the salesItemList
                            boolean itemExists = false;
                            for (SalesItem salesItem : salesItemList) {
                                if (salesItem.getItemName().equals(itemName)) {
                                    salesItem.setQuantitySold(salesItem.getQuantitySold() + itemQuantity);
                                    itemExists = true;
                                    break;
                                }
                            }

                            // If the item is not in the list, add it
                            if (!itemExists) {
                                salesItemList.add(new SalesItem(itemName, itemQuantity));
                            }
                        }
                    }
                }

                // Update the RecyclerView with the salesItemList
                salesListAdapter.setSalesList(salesItemList);
                salesListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }

    private void fetchDataForPieChart(String targetRestaurantName) {
        orderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Get current month
                Calendar calendar = Calendar.getInstance();
                int currentMonth = calendar.get(Calendar.MONTH) + 1; // Months are 0-based

                // Iterate through the orders
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {

                    // Check if the order is from the current month and belongs to the target restaurant
                    String cafeName = orderSnapshot.child("CafeName").getValue(String.class);
                    String orderTime = orderSnapshot.child("Ordered Date").getValue(String.class);
                    int orderMonth = getMonthFromDateString(orderTime);

                    if (orderMonth == currentMonth && cafeName.equals(targetRestaurantName)) {
                        // Get the items list for the order
                        DataSnapshot itemListSnapshot = orderSnapshot.child("ItemList");

                        // Iterate through items
                        for (DataSnapshot itemSnapshot : itemListSnapshot.getChildren()) {
                            String itemName = itemSnapshot.child("ItemName").getValue(String.class);
                            String iP = itemSnapshot.child("price").getValue(String.class);
                            Float itemPrice = Float.parseFloat(iP);
                            String iQ = itemSnapshot.child("quantity").getValue(String.class);
                            int itemQuantity = Integer.parseInt(iQ);

                            // Calculate total sales for the item
                            float totalSales = itemPrice * itemQuantity;

                            // Check if the item is already in the pieEntries
                            boolean itemExists = false;
                            for (PieEntry entry : pieEntries) {
                                if (entry.getLabel().equals(itemName)) {
                                    entry.setY(entry.getY() + totalSales);
                                    itemExists = true;
                                    break;
                                }
                            }

                            // If the item is not in the list, add it
                            if (!itemExists) {
                                pieEntries.add(new PieEntry(totalSales, itemName));
                            }
                        }
                    }
                }
                // Create a PieDataSet
                PieDataSet dataSet = new PieDataSet(pieEntries, "Sales");

                // Create a PieData object
                PieData data = new PieData(dataSet);

                dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                dataSet.setValueTextColor(getResources().getColor(R.color.lavender));
                data.setValueTextSize(12f);

                // Set data to the PieChart
                pieChart.setData(data);

                // Refresh the chart
                pieChart.invalidate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }

    private int getMonthFromDateString(String dateString) {
        if (dateString == null) {
            return -1; // Handle the case where the date string is null
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = dateFormat.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.get(Calendar.MONTH) + 1; // Months are 0-based
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Handle the case where parsing fails
        }
    }

}

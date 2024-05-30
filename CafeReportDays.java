package com.example.loginpage;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CafeReportDays extends AppCompatActivity {

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
        setContentView(R.layout.activity_cafe_report_days);

        pieEntries = new ArrayList<>();

        pieChart = findViewById(R.id.chart);
        completedOrder = findViewById(R.id.tv_completedOrder);
        cancelledOrder = findViewById(R.id.tv_cancelledOrder);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        salesListAdapter = new SalesListAdapter();
        recyclerView.setAdapter(salesListAdapter);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        String userID = firebaseUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Cafe");
        orderReference = FirebaseDatabase.getInstance().getReference("Order");

        databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cName = snapshot.child("CafeName").getValue(String.class);
                fetchData(cName, Calendar.getInstance());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors here
            }
        });

        // Show date picker when the user clicks on a date-related TextView or button
        findViewById(R.id.selectDateButton).setOnClickListener(view -> showDatePickerDialog());
    }

    private void fetchData(String targetRestaurantName, Calendar selectedDate) {
        orderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int completedOrders = 0;
                int cancelledOrders = 0;
                ArrayList<SalesItem> salesItemList = new ArrayList<>();

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    String cafeName = orderSnapshot.child("CafeName").getValue(String.class);
                    if (cafeName.equalsIgnoreCase(targetRestaurantName)) {
                        String orderStatus = orderSnapshot.child("Order Status").getValue(String.class);
                        String orderTime = orderSnapshot.child("Ordered Date").getValue(String.class);

                        Calendar orderCalendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        try {
                            Date date = dateFormat.parse(orderTime);
                            if (date != null) {
                                orderCalendar.setTime(date);

                                if (isSameDay(orderCalendar, selectedDate)) {
                                    if ("completed".equalsIgnoreCase(orderStatus)) {
                                        completedOrders++;
                                    } else if ("cancelled".equalsIgnoreCase(orderStatus)) {
                                        cancelledOrders++;
                                    }

                                    DataSnapshot itemListSnapshot = orderSnapshot.child("ItemList");

                                    for (DataSnapshot itemSnapshot : itemListSnapshot.getChildren()) {
                                        String itemName = itemSnapshot.child("ItemName").getValue(String.class);
                                        String quantityString = itemSnapshot.child("quantity").getValue(String.class);
                                        int itemQuantity = Integer.parseInt(quantityString);

                                        boolean itemExists = false;
                                        for (SalesItem salesItem : salesItemList) {
                                            if (salesItem.getItemName().equals(itemName)) {
                                                salesItem.setQuantitySold(salesItem.getQuantitySold() + itemQuantity);
                                                itemExists = true;
                                                break;
                                            }
                                        }

                                        if (!itemExists) {
                                            salesItemList.add(new SalesItem(itemName, itemQuantity));
                                        }
                                    }
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                completedOrder.setText(String.valueOf(completedOrders));
                cancelledOrder.setText(String.valueOf(cancelledOrders));

                salesListAdapter.setSalesList(salesItemList);
                salesListAdapter.notifyDataSetChanged();

                updatePieChart(targetRestaurantName, selectedDate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }

    private void updatePieChart(String targetRestaurantName, Calendar selectedDate) {
        ArrayList<PieEntry> selectedDayPieEntries = new ArrayList<>();

        orderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    String cafeName = orderSnapshot.child("CafeName").getValue(String.class);
                    String orderTime = orderSnapshot.child("Ordered Date").getValue(String.class);

                    Calendar orderCalendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    try {
                        Date date = dateFormat.parse(orderTime);
                        if (date != null) {
                            orderCalendar.setTime(date);

                            if (isSameDay(orderCalendar, selectedDate) && cafeName.equals(targetRestaurantName)) {
                                DataSnapshot itemListSnapshot = orderSnapshot.child("ItemList");

                                for (DataSnapshot itemSnapshot : itemListSnapshot.getChildren()) {
                                    String itemName = itemSnapshot.child("ItemName").getValue(String.class);
                                    String iP = itemSnapshot.child("price").getValue(String.class);
                                    Float itemPrice = Float.parseFloat(iP);
                                    String iQ = itemSnapshot.child("quantity").getValue(String.class);
                                    int itemQuantity = Integer.parseInt(iQ);

                                    float totalSales = itemPrice * itemQuantity;

                                    boolean itemExists = false;
                                    for (PieEntry entry : selectedDayPieEntries) {
                                        if (entry.getLabel().equals(itemName)) {
                                            entry.setY(entry.getY() + totalSales);
                                            itemExists = true;
                                            break;
                                        }
                                    }

                                    if (!itemExists) {
                                        selectedDayPieEntries.add(new PieEntry(totalSales, itemName));
                                    }
                                }
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                PieDataSet dataSet = new PieDataSet(selectedDayPieEntries, "Sales");
                PieData data = new PieData(dataSet);

                dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                dataSet.setValueTextColor(getResources().getColor(R.color.lavender));
                data.setValueTextSize(12f);

                pieChart.setData(data);
                pieChart.invalidate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, monthOfYear, dayOfMonth);
                        fetchData(cName, selectedDate);
                    }
                }, year, month, day);

        datePickerDialog.show();
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
}

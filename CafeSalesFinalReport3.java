package com.example.loginpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.loginpage.Adapter.SalesItem;
import com.example.loginpage.Adapter.SalesListAdapter;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CafeSalesFinalReport3 extends AppCompatActivity {
    PieChart pieChart;
    List<PieEntry> pieEntries;
    DatabaseReference orderReference, cafeReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String cName;
    TextView tvcompletedOrder, tvcancelledOrder, tvStartDate, tvEndDate;
    Button btnCalender;

    RecyclerView recyclerView;
    SalesListAdapter salesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe_sales_final_report3);
        pieChart = findViewById(R.id.chart_cafefinalsales);
        btnCalender = findViewById(R.id.btn_cafeFinalSales_Calender3);
        tvStartDate = findViewById(R.id.tv_cafe_sales_startdate);
        tvEndDate = findViewById(R.id.tv_cafe_sales_enddate);
        tvcompletedOrder = findViewById(R.id.tv_cafeFinalSales_completedOrder3);
        tvcancelledOrder = findViewById(R.id.tv_cafeFinalSales_cancelledOrder3);
        recyclerView = findViewById(R.id.recyclerView_cafefinalsales3);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        salesListAdapter = new SalesListAdapter();
        recyclerView.setAdapter(salesListAdapter);

        tvcancelledOrder.setText("0");
        tvcompletedOrder.setText("0");


        tvStartDate.setText("Start Date");
        tvEndDate.setText("End Date");

        orderReference = FirebaseDatabase.getInstance().getReference("Order");
        cafeReference = FirebaseDatabase.getInstance().getReference("Cafe");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        btnCalender.setOnClickListener(view -> DatePickerDialog());

    }
    private void DatePickerDialog(){
        MaterialDatePicker<Pair<Long,Long>> materialDatePicker = MaterialDatePicker.Builder.dateRangePicker().
                setTitleText("Select a date range").build();
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                String date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selection.first);
                String date2 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selection.second);
                tvStartDate.setText(date1);
                tvEndDate.setText(date2);
                fetchData(date1, date2);
            }
        });
        materialDatePicker.show(getSupportFragmentManager(),"DATE_PICKER");
    }

    private void fetchData(String startDate, String endDate){
        cafeReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cName = snapshot.child("CafeName").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        orderReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot snapshot = task.getResult();
                int completeCount = 0;
                int cancelledCount = 0;
                ArrayList<SalesItem> salesItemList = new ArrayList<>();
                ArrayList<PieEntry> selectedDayPieEntries = new ArrayList<>();

                for(DataSnapshot ch : snapshot.getChildren()){
                    String cafeName = ch.child("CafeName").getValue(String.class);
                    String oDate = ch.child("Ordered Date").getValue(String.class);
                    String oStatus = ch.child("Order Status").getValue(String.class);
                    if(cafeName.equals(cName)){
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        try {
                            Date orderDate = dateFormat.parse(oDate);
                            Date sDate = dateFormat.parse(startDate);
                            Date eDate = dateFormat.parse(endDate);
                            if(orderDate.after(sDate) && orderDate.before(eDate)){
                                if(oStatus.equals("completed")){
                                     completeCount++;
                                    DataSnapshot itemListSnapshot = ch.child("ItemList");
                                    for (DataSnapshot itemSnapshot : itemListSnapshot.getChildren()) {
                                        String itemName = itemSnapshot.child("ItemName").getValue(String.class);
                                        String iP = itemSnapshot.child("price").getValue(String.class);
                                        String quantityString = itemSnapshot.child("quantity").getValue(String.class);
                                        Float itemPrice = Float.parseFloat(iP);
                                        int itemQuantity = Integer.parseInt(quantityString);
                                        float totalSales = itemPrice * itemQuantity;

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
                                            selectedDayPieEntries.add(new PieEntry(totalSales, itemName));
                                        }
                                    }
                                }
                                else if(oStatus.equals("cancelled")){
                                    cancelledCount++;
                                }
                            }
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
                tvcancelledOrder.setText(""+cancelledCount);
                tvcompletedOrder.setText(""+completeCount);
                salesListAdapter.setSalesList(salesItemList);
                salesListAdapter.notifyDataSetChanged();

                PieDataSet dataSet = new PieDataSet(selectedDayPieEntries, "Sales");
                PieData data = new PieData(dataSet);

                dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                dataSet.setValueTextColor(getResources().getColor(R.color.lavender));
                data.setValueTextSize(12f);
                if(data != null){
                    pieChart.setData(data);
                    pieChart.invalidate();
                }
            }
        });

    }
}
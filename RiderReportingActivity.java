package com.example.loginpage;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class RiderReportingActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    int[] monthOrderNum;
    Button searchBtn;
    EditText year_et;
    String uid;
    Handler handler;
    PieChart pieChart;
    TableLayout tableLayout;
    float total = 0;
    TextView tvIncome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_reporting);
        pieChart = findViewById(R.id.chart);
        tvIncome = findViewById(R.id.tv_rider_totalincome);
        searchBtn = findViewById(R.id.search_btn);
        year_et = findViewById(R.id.year_tv);
        tableLayout = findViewById(R.id.tableLayout);
        monthOrderNum = new int[12];
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        uid = mAuth.getCurrentUser().getUid();
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String year = year_et.getText().toString();
                getOrders(uid, year);
            }
        });
        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                ArrayList<PieEntry> entries = new ArrayList<>();
                float sum = 0;
                for (int i = 0; i < 12; i++) {
                    sum += monthOrderNum[i];
                }
                sum = sum*2;
                tvIncome.setText(String.format("Total income: RM %.2f",sum));
                for (int i = 0; i < 12; i++) {
                    if (monthOrderNum[i] != 0) {
                        entries.add(new PieEntry((float) (monthOrderNum[i]*2), "Month " + (i + 1)));
                    }
                }
                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                PieData pieData = new PieData(dataSet);
                pieChart.setData(pieData);
                pieChart.invalidate();
                pieData.setValueTextSize(14f);
                //更新表格
                //清空
                tableLayout.removeAllViews();
                addRow(tableLayout,"Month","Num of Order");
                for (int i = 0; i < 12; i++) {
                        addRow(tableLayout,(i+1)+"",(monthOrderNum[i])+"");

                }
            }
        };
    }






    private void getOrders(String riderId,String year){
        for(int i = 0;i<12;i++){
            monthOrderNum[i] = 0;
        }
        DatabaseReference orderRef = mDatabase.child("Order");
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String rid = String.valueOf(dataSnapshot.child("RiderID").getValue());
                    String date = String.valueOf(dataSnapshot.child("Finish Time").getValue());
                    if (rid.equals(riderId)) {
                        MYdate m = getYearMonth(date);
                        if (m != null && m.year.equals(year)) {
                            int mon = Integer.parseInt(m.month);
                            monthOrderNum[mon - 1]++;
                        }
                    }
                }

                Log.d("RiderRep", Arrays.toString(monthOrderNum));
                handler.sendEmptyMessage(1);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

    // 动态添加行
    private void addRow(TableLayout tableLayout, String column1, String column2) {
        TableRow row = new TableRow(this);

        // 设置行布局参数
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(layoutParams);

        // 添加第一列数据
        TextView textView1 = new TextView(this);
        textView1.setText(column1);
        textView1.setPadding(8, 8, 8, 8);
        textView1.setBackgroundResource(R.drawable.border);
        row.addView(textView1);

        // 添加第二列数据
        TextView textView2 = new TextView(this);
        textView2.setText(column2);
        textView2.setPadding(8, 8, 8, 8);
        textView2.setBackgroundResource(R.drawable.border);
        row.addView(textView2);

        // 添加行到表格
        tableLayout.addView(row);
    }





    private MYdate getYearMonth(String finishTime){
        if (finishTime.length() >= 10) {
            String year = finishTime.substring(6,10);
            String month = finishTime.substring(3,5);

            return new MYdate(month, year);
        } else {
            // Handle the case where finishTime doesn't have enough characters
            // You can return a default value or throw an exception based on your requirements
            return null; // Or throw new IllegalArgumentException("Invalid finishTime length");
        }
    }


    class MYdate{
        String month;
        String year;

        public MYdate(String month, String year) {
            this.month = month;
            this.year = year;
        }
    }

}

package com.example.loginpage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.loginpage.Adapter.CustomerCartAdapter;
import com.example.loginpage.Adapter.CustomerCartItemList;
import com.example.loginpage.Adapter.CustomerOrderStatusList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class CustomerOrderLastPage extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_send_restaurant_noti);

        ListView lvOrderPending;

        ArrayAdapter<String> pendingAdapter;

        List<CustomerOrderStatusList> pendingStatusList;

        DatabaseReference orderReference;

        FirebaseUser mUser;

        lvOrderPending = findViewById(R.id.customer_cart_listview);

        pendingStatusList = new ArrayList<>();
        pendingStatusList.clear();



    }
}
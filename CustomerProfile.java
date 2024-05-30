package com.example.loginpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.loginpage.Profile.CustomerMainPageFragment;
import com.example.loginpage.Profile.CustomerOrderFragment;
import com.example.loginpage.Profile.CustomerProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


public class CustomerProfile extends AppCompatActivity {

    BottomNavigationView bnv;
    CustomerProfileFragment customerProfileFragment = new CustomerProfileFragment();
    CustomerOrderFragment customerOrderFragment = new CustomerOrderFragment();
    CustomerMainPageFragment customerMainPageFragment = new CustomerMainPageFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        bnv = findViewById(R.id.bottomNavigationView_customerProfile);
        Intent intent = getIntent();
        String signal = intent.getStringExtra("signal");
        if(signal!=null){
            if(signal.equals("order")){
                getSupportFragmentManager().beginTransaction().replace(R.id.customer_frameLayout, customerOrderFragment).commit();
                //getSupportFragmentManager().beginTransaction().replace(R.id.customer_frameLayout, customerMainPageFragment).commit();

            }
        }
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.customer_frameLayout, customerMainPageFragment).commit();
        }


        bnv.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch ((item.getItemId())){
                    case R.id.customer_nav_profile:
                        getSupportFragmentManager().beginTransaction().replace(R.id.customer_frameLayout, customerProfileFragment).commit();
                        return true;

                    case R.id.customer_nav_mainpage:
                        getSupportFragmentManager().beginTransaction().replace(R.id.customer_frameLayout, customerMainPageFragment).commit();
                        return true;

                    case R.id.customer_nav_order:
                        getSupportFragmentManager().beginTransaction().replace(R.id.customer_frameLayout, customerOrderFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }
}
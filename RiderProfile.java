package com.example.loginpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.loginpage.Profile.RiderOrderStatusFragment;
import com.example.loginpage.Profile.RiderProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class RiderProfile extends AppCompatActivity {

    BottomNavigationView bnv;

    RiderOrderStatusFragment riderOrderStatusFragment = new RiderOrderStatusFragment();
    RiderProfileFragment riderProfileFragment = new RiderProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_profile);
        Intent i = getIntent();
        String signal = i.getStringExtra("signal");

        bnv = findViewById(R.id.bottomNavigationView_riderProfile);

        if(signal != null){
            getSupportFragmentManager().beginTransaction().replace(R.id.rider_frameLayout, riderOrderStatusFragment).commit();
            //getSupportFragmentManager().beginTransaction().replace(R.id.rider_frameLayout, riderProfileFragment).commit();

        }
        else{
            //getSupportFragmentManager().beginTransaction().replace(R.id.rider_frameLayout, riderOrderStatusFragment).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.rider_frameLayout, riderProfileFragment).commit();
        }


        bnv.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.rider_nav_profile:
                        getSupportFragmentManager().beginTransaction().replace(R.id.rider_frameLayout, riderProfileFragment).commit();
                        return true;

                    case R.id.rider_nav_order:
                        getSupportFragmentManager().beginTransaction().replace(R.id.rider_frameLayout, riderOrderStatusFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }
}
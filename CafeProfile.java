package com.example.loginpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.loginpage.Profile.CafeMenuFragment;
import com.example.loginpage.Profile.CafeOrderFragment;
import com.example.loginpage.Profile.CafeProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class CafeProfile extends AppCompatActivity {

    //ActivityMainBinding binding;
    BottomNavigationView bnv;

    CafeOrderFragment cafeOrderFragment = new CafeOrderFragment();
    CafeMenuFragment cafeMenuFragment = new CafeMenuFragment();
    CafeProfileFragment cafeProfileFragment = new CafeProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_cafe_profile);
        Intent i = getIntent();
        String signal = i.getStringExtra("signal");

        bnv = findViewById(R.id.bottomNavigationView_cafeProfile);
        if(signal == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.cafe_frameLayout, cafeMenuFragment).commit();
           //getSupportFragmentManager().beginTransaction().replace(R.id.cafe_frameLayout, cafeOrderFragment).commit();
        }
        else if(signal.equals("on")){
            getSupportFragmentManager().beginTransaction().replace(R.id.cafe_frameLayout, cafeOrderFragment).commit();
        }



        bnv.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.cafe_nav_profile:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cafe_frameLayout, cafeProfileFragment).commit();
                        return true;
                    case R.id.cafe_nav_menu:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cafe_frameLayout, cafeMenuFragment).commit();
                        return true;
                    case R.id.cafe_nav_order:
                        getSupportFragmentManager().beginTransaction().replace(R.id.cafe_frameLayout, cafeOrderFragment).commit();
                        return true;
                }
                return false;
            }
        });



    }
}
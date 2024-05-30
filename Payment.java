package com.example.loginpage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Payment extends AppCompatActivity {

    private TextView balanceTV;
    private String balacce;
    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        balanceTV = findViewById(R.id.textViewAmount);
        Intent intent = getIntent();
        balacce = intent.getStringExtra("balance");
        assert balacce != null;
        String b = "RM"+String.format("%.2f",Float.parseFloat(balacce));
        balanceTV.setText(b);



        // Back arrow click listener (if needed)
        findViewById(R.id.imageViewBack).setOnClickListener(view -> onBackPressed());

        // Add click listener to the "bank card" TextView
        TextView bankCardTextView = findViewById(R.id.textViewBankCard);
        bankCardTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start PaymentCardActivity when bank card is clicked
                Intent intent = new Intent(Payment.this, PaymentCardActivity.class);
                intent.putExtra("balance",balacce);
                startActivityForResult(intent,1);
            }
        });



        // Your other initialization code goes here
        // For example, you can find other views by their IDs and set listeners or perform actions.
    }

    @Override
    protected void onResume() {
        super.onResume();
        String b = "RM"+String.format("%.2f",Float.parseFloat(balacce));
        balanceTV.setText(b);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode==2){
                //更新余额
                if(data!=null){
                    balacce = data.getStringExtra("balance");
                    String b = "RM"+balacce;
                    balanceTV.setText(b);

                }
            }
        }
    }
}

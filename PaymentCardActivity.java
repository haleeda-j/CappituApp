package com.example.loginpage;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class PaymentCardActivity extends AppCompatActivity {

    private EditText cardNumberEditText;
    private EditText expirationEditText;
    private EditText cvvEditText;
    private EditText amountEditText;
    private Button confirmPaymentButton;
    private Button pay_10_btn;
    private Button pay_20_btn;
    private Button pay_50_btn;
    private float balance;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_card);
        Intent intent = getIntent();
        String tb = intent.getStringExtra("balance");
        balance = Float.parseFloat(tb);

        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        //返回结果
                        Intent reIntent = new Intent();
                        reIntent.putExtra("balance", String.valueOf(balance));
                        setResult(2, reIntent);
                        finish();
                        break;
                    default:
                        break;
                }
            }
        };

        // 初始化控件
        pay_10_btn = findViewById(R.id.card_pay_10rm);
        pay_20_btn = findViewById(R.id.card_pay_20rm);
        pay_50_btn = findViewById(R.id.card_pay_50rm);
        cardNumberEditText = findViewById(R.id.editTextCardNumber);
        expirationEditText = findViewById(R.id.editTextExpiration);
        cvvEditText = findViewById(R.id.editTextCVV);
        amountEditText = findViewById(R.id.editTextAmount);
        confirmPaymentButton = findViewById(R.id.buttonConfirmPayment);

        // 为 cardNumberEditText 设置 InputFilter，限制输入长度为12到19位数字
        cardNumberEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(19)});

        // 为 cvvEditText 添加 InputFilter，限制输入长度为3位数字
        cvvEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        // 设置 expirationEditText 点击监听器以显示日期选择器
        expirationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        pay_10_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amountEditText.setText("10");
            }
        });

        pay_20_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amountEditText.setText("20");
            }
        });

        pay_50_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amountEditText.setText("50");
            }
        });

        // 设置按钮点击监听器
        confirmPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 处理支付操作
                performPayment();
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // 使用所选日期更新 expirationEditText
                expirationEditText.setText(String.format("%02d/%02d", month + 1, year % 100));
            }
        };

        // 创建一个 DatePickerDialog，以当前日期为默认日期
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                PaymentCardActivity.this,
                dateSetListener,
                year,
                month,
                day
        );

        // 将最小日期设置为当前日期
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void performPayment() {
        // 获取用户输入
        String cardNumber = cardNumberEditText.getText().toString().trim();
        String expiration = expirationEditText.getText().toString().trim();
        String cvv = cvvEditText.getText().toString().trim();
        String amount = amountEditText.getText().toString().trim();

        // 验证输入
        if (!isValidCardNumber(cardNumber)) {
            return;
        }

        if (!isValidDate(expiration)) {
            return;
        }

        if (!isValidCVV(cvv)) {
            return;
        }

        float money = Float.parseFloat(amount);
        if (money < 0 || money > 200) {
            Toast.makeText(PaymentCardActivity.this, "Invalid amount!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 继续处理支付操作
        balance = balance + money;

        // 模拟支付成功
        Toast.makeText(this, "Payment successful. Amount: " + amount, Toast.LENGTH_SHORT).show();
        // 在实际应用中，你需要在此处调用真实的支付接口，并处理支付结果
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String userID = firebaseUser.getUid();
        mDatabase.child("Customer").child(userID).child("walletAmount").setValue(String.valueOf(balance)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // 显示成功消息
                Toast.makeText(PaymentCardActivity.this, "TNG Card Top-Up Successful. Amount: " + amount, Toast.LENGTH_SHORT).show();

                handler.sendEmptyMessage(1);
            }
        });
    }

    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber.length() < 12 || cardNumber.length() > 19) {
            // 卡号长度不符合要求，显示错误消息
            cardNumberEditText.setError("Card number must be between 12 and 19 digits");
            return false;
        } else if (!cardNumber.matches("[0-9]+")) {
            // 卡号包含非数字字符，显示错误消息
            cardNumberEditText.setError("Only numeric characters allowed");
            return false;
        } else {
            // 卡号输入正确，清除错误消息
            cardNumberEditText.setError(null);
            return true;
        }
    }

    private boolean isValidDate(String date) {
        if (!date.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            // 日期格式不正确，显示错误消息
            expirationEditText.setError("Invalid date format. Please enter a valid expiration date.");
            return false;
        } else {
            // 日期格式正确，清除错误消息
            expirationEditText.setError(null);
            return true;
        }
    }

    private boolean isValidCVV(String cvv) {
        if (cvv.length() != 3 || !cvv.matches("[0-9]+")) {
            // CVV长度不符合要求或包含非数字字符，显示错误消息
            cvvEditText.setError("CVV must be a 3-digit number");
            return false;
        } else {
            // CVV输入正确，清除错误消息
            cvvEditText.setError(null);
            return true;
        }
    }
}

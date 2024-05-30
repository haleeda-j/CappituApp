//package com.example.loginpage;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.util.Log;
//import android.widget.Button;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.example.loginpage.Adapter.CustomerCartAdapter;
//import com.example.loginpage.Adapter.CustomerCartItemList;
//import com.example.loginpage.Adapter.CustomerMenuItemList;
//import com.example.loginpage.Adapter.OrderDetailAdapter;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.ArrayList;
//
//public class CafeOrderApproval extends AppCompatActivity {
//
//    ListView itemListView;
//    Handler handler;
//
//    OrderDetailAdapter cafeAdapter;
//    ArrayList<CustomerCartItemList> items = new ArrayList<>();
//
//    private DatabaseReference mDatabase, orderReference, cafeReference, customerReference;
//    FirebaseAuth mUser;
//   String  OrderID,OrderAddress, OrderSubtotal, OrderDate,OrderTime;
//
//    TextView tv_cafeName;
//    TextView orderID, address, subtotal,date,time;
//    Button btn_accept,btn_reject;
//    String cName;
//    DatabaseReference databaseReference;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fragment_cafe_order);
//        Intent i = getIntent();
//        cName = i.getStringExtra("cafeName");
//        databaseReference = FirebaseDatabase.getInstance().getReference("Cafe");
//        itemListView = findViewById(R.id.customer_cart_listview);
//      //  tv_cafeName = findViewById(R.id.tv_cafename);
//
//
//       // btn_accept = findViewById(R.id.acceptButton);
//     //   btn_reject = findViewById(R.id.rejectButton);
//
//        mUser = FirebaseAuth.getInstance();
//        mDatabase = FirebaseDatabase.getInstance().getReference("AddToCart");
//        orderReference = FirebaseDatabase.getInstance().getReference("Order");
//        cafeReference = FirebaseDatabase.getInstance().getReference("Cafe");
//        customerReference = FirebaseDatabase.getInstance().getReference("Customer");
//
//        //  tv_cafeName.setText(cName);
//
//
//        handler = new Handler(Looper.myLooper()) {
//            @Override
//            public void handleMessage(@NonNull Message msg) {
//                switch (msg.what) {
//                    case 1:
//                        cafeAdapter = new OrderDetailAdapter(CafeOrderApproval.this, R.layout.item, items);
//                        itemListView.setAdapter(cafeAdapter);
//                        orderID = findViewById(R.id.tv_orderid);
//                        address = findViewById(R.id.tv_Address);
//                        subtotal = findViewById(R.id.tv_price);
//                        date = findViewById(R.id.tv_date);
//                        time = findViewById(R.id.tv_time);
//                        Log.d("TA1G", "handleMessage: ");
//                        break;
//                }
//            }
//        };
//        loadItems(handler, cName);
//
//    }
//
//
//
//
//    private void loadItems(Handler handler, String cName){
//
//
//        cafeReference.child(mUser.getCurrentUser().getUid()).child("CafeName").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                DataSnapshot result = task.getResult();
//                if (result.hasChildren()) {
//                    for (DataSnapshot child : result.getChildren()) {
//                      String cafeName = child.child("CafeName").getValue().toString();
//
//                        if(cafeName.equals(cName)){
//                            orderReference.child(cName).addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    OrderID=snapshot.child("OrderId").getValue().toString();
//                                    OrderAddress=snapshot.child("CustomerAddress").getValue().toString();
//                                    OrderSubtotal=snapshot.child("Sub Total").getValue().toString();
//                                    OrderDate=snapshot.child("Ordered Date").getValue().toString();
//                                    OrderTime=snapshot.child("Ordered Time").getValue().toString();
//
//                                  orderID.setText(OrderID);
//                                  address.setText(OrderAddress);
//                                  subtotal.setText(OrderSubtotal);
//                                  date.setText(OrderDate);
//                                  time.setText(OrderTime);
//
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//
//
//                            });
//                        }
//                    }
//
//                }
//    }
//});
//    }
//
////
////      btn_accept.setOnClickListener(new View.OnClickListener() {
////        public void onClick(View v) {
////            //check if any field empty
////
////                Toast.makeText(CafeOrderApproval.this, "Accepted", Toast.LENGTH_SHORT).show();
////
////            }
////}
//// btn_reject.setOnClickListener(new View.OnClickListener() {
////        @Override
////        public void onClick(View v) {
////            builder = new AlertDialog.Builder(CafeOrderApproval.this)
////                    .setMessage("Do you want to reject this order?"+ oID.getText().toString())
////                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
////                        @Override
////                        public void onClick(DialogInterface dialogInterface, int i) {
////                            Toast.makeText(CafeOrderApproval.this, "Order deleted", Toast.LENGTH_SHORT).show();
////
////                            finish();
////                        }
////                    })
////                    .setNegativeButton("No", null)
////                    .show();
////
////        }
////    });
//}
//
//
//

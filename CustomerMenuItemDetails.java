package com.example.loginpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CustomerMenuItemDetails extends AppCompatActivity {
    TextView itemNameTV;
    TextView cafeNameTV;
    TextView itemDescriptionTV;
    ImageView ivItemPhoto;
    Handler handler;
    TextView item_detail;
    Button btn_inc,btn_dec;

   FirebaseAuth auth;
//FirebaseFirestore firestore;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
   Button addToCart;

   String itemName, cafeName, itemPrice, description, photoUri, signal;

   int count=1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_menu_item_details);
      //  reference= FirebaseDatabase.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase .getReference();
        auth=FirebaseAuth.getInstance();
     //  firestore=FirebaseFirestore.getInstance();


        Intent intent = getIntent();
        itemName = intent.getStringExtra("itemName");
       cafeName=intent.getStringExtra("cafeName");
       description = intent.getStringExtra("description");
         photoUri = intent.getStringExtra("ImageID");
        itemPrice = intent.getStringExtra("itemPrice");
        signal = intent.getStringExtra("signal");
        if(signal.equals("1")){
            count = Integer.parseInt(intent.getStringExtra("itemQty"));
        }
        cafeNameTV = findViewById(R.id.item_detail_cafe_name);
        itemNameTV = findViewById(R.id.item_detail_food_name);
        itemDescriptionTV = findViewById(R.id.item_detail_description);
        ivItemPhoto = findViewById(R.id.item_detail_food_photo);
        item_detail=findViewById(R.id.item_detail_item_num);
        btn_inc=findViewById(R.id.button1);
        btn_dec=findViewById(R.id.button2);
        addToCart=findViewById(R.id.item_detail_add_cart_btn);
        item_detail.setText(""+count);




        btn_inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                item_detail.setText(""+count);
            }
        });

       btn_dec.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(count<=1){
                   if(signal.equals("1")){
                       AlertDialog.Builder alert = new AlertDialog.Builder(CustomerMenuItemDetails.this);
                       alert.setTitle("Remove item");
                       alert.setMessage("Are you sure you want to remove this item?");
                       alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               databaseReference.child("AddToCart").child(auth.getCurrentUser().getUid())
                                       .child(cafeName).child(itemName).removeValue();
                               Toast.makeText(CustomerMenuItemDetails.this, "Remove Successfully", Toast.LENGTH_SHORT).show();
                               Intent intent = new Intent(CustomerMenuItemDetails.this, CustomerCart.class);
                               intent.putExtra("cafeName", cafeName);
                               startActivity(intent);

                           }
                       });
                       alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               dialogInterface.cancel();
                           }
                       });
                       alert.show();
                   }
               }
               else {
                   count--;
                   item_detail.setText("" + count);
               }
           }
       });

        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


              addedToCart();
                Intent intent = new Intent(CustomerMenuItemDetails.this, CustomerCart.class);
                intent.putExtra("cafeName", cafeName);
                startActivity(intent);

            }
        });


//        handler = new Handler(Looper.myLooper()){
//            @Override
//            public void handleMessage(@NonNull Message msg) {
//                super.handleMessage(msg);
//                switch (msg.what){
//                    case 1:
//
//                        break;
//                }
//            }
//        };
        itemNameTV.setText(itemName);
       cafeNameTV.setText(cafeName);
        itemDescriptionTV.setText(description);
        Picasso.with(this).load(Uri.parse(photoUri)).into(ivItemPhoto);
    }

    private void addedToCart() {
        /*String saveCurrentDate, saveCurrentTime;
        Calendar calForDate = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd,yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calForDate.getTime());*/

        final HashMap<String, Object> cartMap = new HashMap<>();

        cartMap.put("productName", itemName);
        //cartMap.put("CafeName",cafeName);
        cartMap.put("productDesc", description);
        cartMap.put("Price", itemPrice);
        cartMap.put("productImage", photoUri);
        //cartMap.put("currentDate", saveCurrentDate);
        //cartMap.put("currentTime", saveCurrentTime);
        cartMap.put("quantity", count);

         databaseReference.child("AddToCart")
                .child(auth.getCurrentUser().getUid())
                .child(cafeName)
                 .child(itemName)
                 .setValue(cartMap)
                 .addOnSuccessListener(new OnSuccessListener<Void>() {
                     @Override
                     public void onSuccess(Void unused) {
                         Toast.makeText(CustomerMenuItemDetails.this, "Added to a cart", Toast.LENGTH_SHORT).show();
                     }
                 })
                 .addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception e) {
                         Toast.makeText(CustomerMenuItemDetails.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                     }
                 });




//        firestore.collection("AddToCart").document(auth.getCurrentUser().getUid())
//                .collection("CurrentUser").add(cartMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentReference> task) {
//                        Toast.makeText(CustomerMenuItemDetails.this, "Added to a cart", Toast.LENGTH_SHORT).show();
//                        finish();
//                    }
//                });

    }

    }


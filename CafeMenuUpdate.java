package com.example.loginpage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.loginpage.Adapter.CafeMenuItemAdapterClass;
import com.example.loginpage.Adapter.CafeMenuItemList;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class CafeMenuUpdate extends AppCompatActivity {
    EditText etItemName, etItemPrice, etItemDesc;
    TextView tvTitle;
    Button btnCafeItemUpload, btnCafeItemSave, btnCafeItemBack;
    ImageView ivCateItem;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
    FirebaseAuth firebaseAuth;
    Uri imageUri,u;


    RecyclerView recyclerView;
    CafeMenuItemAdapterClass cafeMenuItemAdapterClass;
    List<CafeMenuItemList> cafeMenuItemListList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe_menu_update);


        etItemName = findViewById(R.id.et_CafeItemName);
        etItemPrice = findViewById(R.id.et_cafeItemPrice);
        etItemDesc = findViewById(R.id.et_cafeItemDesc);
        btnCafeItemUpload = findViewById(R.id.btn_cafeItemUpload);
        btnCafeItemBack = findViewById(R.id.btn_cafeItemBack);
        btnCafeItemSave = findViewById(R.id.btn_cafeItemSave);
        ivCateItem = findViewById(R.id.imageViewCafeItem);
        tvTitle = findViewById(R.id.tv_cafe_menu_title);


        firebaseDatabase= FirebaseDatabase.getInstance();

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();


        databaseReference= firebaseDatabase.getReference().child("Cafe")
                .child(firebaseAuth.getCurrentUser().getUid())
                        .child("Item");

        String editItemName = getIntent().getStringExtra("ItemName");
        if(editItemName!= null){
            tvTitle.setText("Edit Menu");
            etItemName.setText(getIntent().getStringExtra("ItemName"));
            etItemName.setFocusable(false);
            etItemPrice.setText(getIntent().getStringExtra("Price"));
            etItemDesc.setText(getIntent().getStringExtra("Desc"));
            String iUri = getIntent().getStringExtra("ItemImageUri");
            u= Uri.parse(iUri);
            Picasso.with(this).load(u).into(ivCateItem);
            //imageUri = u;


        }

        btnCafeItemUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        btnCafeItemSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemName = etItemName.getText().toString();
                String itemPrice = etItemPrice.getText().toString();
                String itemDesc = etItemDesc.getText().toString();

                if(itemName.isEmpty() || itemPrice.isEmpty() || itemDesc.isEmpty() || (imageUri == null && u == null)){
                    Toast.makeText(CafeMenuUpdate.this, "Please fill up all the details!", Toast.LENGTH_SHORT).show();
                }
                else{
                        uploadImage();
                    }

            }
        });

        btnCafeItemBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CafeMenuUpdate.this, CafeProfile.class);
                startActivity(intent);
            }
        });


    }
    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && data!=null && data.getData() !=null){

            imageUri = data.getData();
            Toast.makeText(CafeMenuUpdate.this, ""+imageUri, Toast.LENGTH_SHORT).show();
            ivCateItem.setImageURI(imageUri);
        }
    }
    private  void uploadImage(){
        if(imageUri != null){
            StorageReference fileReference = firebaseStorage.getReference().child("ItemImage").child(firebaseAuth.getCurrentUser().getUid()).child(etItemName.getText().toString());
            fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("ItemName", etItemName.getText().toString());
                            hashMap.put("price", etItemPrice.getText().toString());
                            hashMap.put("Description", etItemDesc.getText().toString());
                            hashMap.put("itemImageURL", uri.toString());

                            databaseReference
                                    .child(etItemName.getText().toString())
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(CafeMenuUpdate.this, "Success!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(CafeMenuUpdate.this, CafeProfile.class);
                                            startActivity(intent);
                                        }
                                    });

                        }
                    });

                }
            });

        }
        else{
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("ItemName", etItemName.getText().toString());
            hashMap.put("price", etItemPrice.getText().toString());
            hashMap.put("Description", etItemDesc.getText().toString());
            hashMap.put("itemImageURL", u.toString());

            databaseReference
                    .child(etItemName.getText().toString())
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(CafeMenuUpdate.this, "Success!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CafeMenuUpdate.this, CafeProfile.class);
                            startActivity(intent);
                        }
                    });

        }




        //uri = firebaseUser.getPhotoUrl();
    }
   /* private void UpdateInfo(){

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("ItemName", etItemName.getText().toString());
        hashMap.put("price", etItemPrice.getText().toString());
        hashMap.put("Description", etItemDesc.getText().toString());
        hashMap.put("itemImageURL", imageUri.toString());
        //maxid = Long.parseLong(databaseReference.getKey());
        //Toast.makeText(CafeMenuUpdate.this, , Toast.LENGTH_SHORT).show();
        databaseReference
                .child(etItemName.getText().toString())
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(CafeMenuUpdate.this, "Success!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CafeMenuUpdate.this, CafeProfile.class);
                        startActivity(intent);
                    }
                });
    }*/
}
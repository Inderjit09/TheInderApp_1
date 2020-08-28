package com.inderproduction.theinderapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.inderproduction.theinderapp.Adapters.CartListAdapter;
import com.inderproduction.theinderapp.Modals.OrderItem;
import com.inderproduction.theinderapp.Utilities.DisplayUtils;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartListAdapter.OnCartItemClickListener {

    private RecyclerView cartRecycler;
    private TextView cartPriceTotal;
    private Button btnCheckout;
    List<OrderItem> productData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartRecycler=findViewById(R.id.cart_recycler);
        cartPriceTotal = findViewById(R.id.cart_price);
        btnCheckout=findViewById(R.id.btn_checkout);

        productData = new ArrayList<>(OneApplication.cart.values());

        cartRecycler.setLayoutManager(new LinearLayoutManager(this));
        cartRecycler.setAdapter(new CartListAdapter(this,productData,this));
        updateAndCalculatePrice();

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                if(OneApplication.cart.size() >0){
                    Intent proceedCheckout;
                    if(FirebaseAuth.getInstance().getCurrentUser() != null){
                        proceedCheckout = new Intent(CartActivity.this,UserProfileActivity.class);
                    } else {
                        proceedCheckout = new Intent(CartActivity.this,LoginActivity.class);
                    }
                    proceedCheckout.putExtra("sender","cart");
                    startActivityForResult(proceedCheckout,1001);
                } else {
                    DisplayUtils.showToast(CartActivity.this,"No Items in Cart", Toast.LENGTH_SHORT);
                }
            }
                catch (Exception e){
                    Log.e("expp",e.toString());
                }
            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1001 && resultCode == RESULT_OK ){
            finish();
        }
    }

    @Override
    public void onItemIncreaseCount(int position, String category) {
            OneApplication.incrementItemCount(productData.get(position));
//        OneApplication.addItemToCart(productData.get(position),category);
        if (cartRecycler.getAdapter() != null) {
            productData = new ArrayList<>(OneApplication.cart.values());
            ((CartListAdapter)cartRecycler.getAdapter()).notifyItemChanged(position);
            updateAndCalculatePrice();
        }



    }

    @Override
    public void onItemDecreaseCount(int position, String category) {
            OneApplication.decrementItemCount(productData.get(position));
//        OneApplication.removeItemFromCart(productData.get(position),category);
        if (cartRecycler.getAdapter() != null) {
            productData = new ArrayList<>(OneApplication.cart.values());
            ((CartListAdapter)cartRecycler.getAdapter()).updateData(productData);
            updateAndCalculatePrice();
        }
    }

    private void updateAndCalculatePrice(){
        double totalPrice = OneApplication.calculatePrice();
        double tax = totalPrice*0.05;
        OneApplication.finalOrder.setOrderBaseTotal(totalPrice);
        OneApplication.finalOrder.setOrderTax(tax);
        OneApplication.finalOrder.setOrderGrandTotal(totalPrice + tax);
        cartPriceTotal.setText("Total Price : CAN "+String.valueOf(totalPrice).concat(" "));
    }
}

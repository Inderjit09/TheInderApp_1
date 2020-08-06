package com.inderproduction.theinderapp.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Ordering;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inderproduction.theinderapp.Modals.OrderItem;
import com.inderproduction.theinderapp.Modals.Pent;
import com.inderproduction.theinderapp.Modals.Shirt;
import com.inderproduction.theinderapp.Modals.Shoes;
import com.inderproduction.theinderapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.CartViewHolder> {

    private List<OrderItem> cartProduct;
    private OnCartItemClickListener mListener;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference folderReference = storage.getReference();

    private Context context;

    public CartListAdapter(Context context, List<OrderItem> data, OnCartItemClickListener listener){
        this.cartProduct = data;
        mListener = listener;
        this.context = context;
    }

    public void updateData(List<OrderItem> newData){
        this.cartProduct = new ArrayList<>();
        this.cartProduct.addAll(newData);
        notifyDataSetChanged();
    }

    public void updateSingleItem(OrderItem item,int position){
        cartProduct.set(position,item);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_list_item,parent,false);
        CartViewHolder holder = new CartViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CartViewHolder holder, final int position) {
        OrderItem o = cartProduct.get(position);

        holder.cartItemName.setText(o.getItemName());
        holder.cartItemColor.setText(o.getItemColor());
        double newPrice = o.getItemPrice() - ((double)o.getItemDiscount()/100 * o.getItemPrice());
        holder.cartItemPrice.setText("Rs "+newPrice+" x "+o.getItemCount());
        holder.cartItemSize.setText("Size: "+o.getItemSize());
        holder.cartItemBrand.setText(o.getItemBrand());
        String fileName = o.getItemID() + "_"+o.getItemColor().toUpperCase()+"_1.jpg";
        int MAXSIZE = 2 * 1024 * 1024;
        folderReference.child("products").child(fileName).getBytes(MAXSIZE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull final Task<byte[]> task) {
                if(task.isSuccessful() && task.getResult() != null){
//                            Toast.makeText(ProductDetail.this,"IMAGE LOADED",Toast.LENGTH_SHORT).show();
                    byte[] data = task.getResult();
                    final Bitmap b = BitmapFactory.decodeByteArray(data,0,data.length);
                    holder.cartItemImage.setImageBitmap(b);
                } else {
                    if(task.getException() != null){
//                                Log.e("IMAGE LOAD ERROR",task.getException().getLocalizedMessage());
                    }
                }
            }
        });




//        if (o instanceof Pent) {
//            Pent p = (Pent) o;
//            Picasso.get().load(Uri.parse(p.getProductImage())).fit().into(holder.cartItemImage);
//            holder.cartItemName.setText(p.getItemName());
//            holder.cartItemBrand.setText(String.valueOf(p.getItemPrice()));
//            holder.cartItemPrice.setText(String.valueOf(p.getCartCount()));
//            holder.cartItemColor.setText(String.valueOf(p.get));
//            category = p.getItemCategory();
//
//        } else if(o instanceof Shirt) {
//            Shirt s = (Shirt) o;
//            Picasso.get().load(Uri.parse(s.getProductImage())).into(holder.cartItemImage);
//            holder.cartItemName.setText(s.getItemName());
//            holder.cartItemBrand.setText(String.valueOf(s.getItemPrice()));
//            holder.cartItemPrice.setText(String.valueOf(s.getCartCount()));
//            category = s.getItemCategory();
//        }
//        else if(o instanceof Shoes) {
//            Shoes sh = (Shoes) o;
//            Picasso.get().load(Uri.parse(sh.getProductImage())).into(holder.cartItemImage);
//            holder.cartItemName.setText(sh.getItemName());
//            holder.cartItemBrand.setText(String.valueOf(sh.getItemPrice()));  // DEMO TO DISPLAY PRICE
//            holder.cartItemPrice.setText(String.valueOf(sh.getCartCount()));  // DEMO TO DISPLAY CART COUNT
//            category = sh.getItemCategory();
//        }

        final String finalCategory = o.getItemCategory();
        holder.cartIncreasebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemIncreaseCount(position, finalCategory);
            }
        });
        holder.cartDecreasebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemDecreaseCount(position,finalCategory);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartProduct.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder{

        private TextView cartItemName,cartItemBrand,cartItemPrice;
        private Button cartIncreasebtn,cartDecreasebtn;
        private ImageView cartItemImage;
        private TextView cartItemSize,cartItemColor;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            cartItemName=itemView.findViewById(R.id.cart_item_name);
            cartItemBrand=itemView.findViewById(R.id.cart_item_brand);
            cartItemPrice=itemView.findViewById(R.id.cart_item_price);
            cartIncreasebtn=itemView.findViewById(R.id.cart_inc);
            cartDecreasebtn=itemView.findViewById(R.id.cart_dec);
            cartItemImage=itemView.findViewById(R.id.cart_item_image);
            cartItemSize = itemView.findViewById(R.id.cart_itemsize);
            cartItemColor = itemView.findViewById(R.id.cart_itemcolor);
        }
    }

    public interface OnCartItemClickListener {
        void onItemIncreaseCount(int position,String category);
        void onItemDecreaseCount(int position,String category);
    }
}

package com.inderproduction.theinderapp.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inderproduction.theinderapp.Modals.Pent;
import com.inderproduction.theinderapp.Modals.Shirt;
import com.inderproduction.theinderapp.Modals.Shoes;
import com.inderproduction.theinderapp.R;
import com.inderproduction.theinderapp.SearchActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.SearchViewHolder> {

    private List<Object> searchList;
    private OnSearchItemClickListener mListener;
    private int screenWidth;

    private Context context;

    public SearchListAdapter(Context context, List<Object> data, OnSearchItemClickListener listener,int screenWidth){
        this.searchList = data;
        mListener = listener;
        this.context = context;
        this.screenWidth=screenWidth;
    }

    public void updateData(List<Object> newData){
        this.searchList = new ArrayList<>();
        this.searchList.addAll(newData);
        notifyDataSetChanged();
    }

    public void updateSingleItem(Object item,int position){
        searchList.set(position,item);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_list_item,parent,false);
        SearchViewHolder holder = new SearchViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final SearchViewHolder holder, final int position) {
        Object o = searchList.get(position);
        String productImage = "";
        String productName = "";
        double finalPrice = 0;
        int discount = 0;
        double initialPrice = 0;

        if (o instanceof Pent) {
                Pent j = (Pent) o;
                productImage = j.getProductImage();
                productName = j.getItemName();
                discount = j.getDiscount();
                initialPrice = j.getItemPrice();
                finalPrice = j.getItemPrice() - ((double)discount/100 * initialPrice);
        } else if(o instanceof Shirt) {
            Shirt j = (Shirt) o;
            productImage = j.getProductImage();
            productName = j.getItemName();
            discount = j.getDiscount();
            initialPrice = j.getItemPrice();
            finalPrice = j.getItemPrice() - ((double)discount/100 * initialPrice);
        } else  if(o instanceof Shoes){
            Shoes j = (Shoes) o;
            productImage = j.getProductImage();
            productName = j.getItemName();
            discount = j.getDiscount();
            initialPrice = j.getItemPrice();
            finalPrice = j.getItemPrice() - ((double)discount/100 * initialPrice);
        }

        Picasso.get().load(Uri.parse(productImage)).fit().into(holder.searchImage);
        holder.searchName.setText(productName);
        holder.searchPrice.setText(String.valueOf(initialPrice));
        if(discount>0){
            holder.searchDiscount.setText(discount+"% off");
            holder.searchFinalPrice.setText(String.valueOf(finalPrice));
            holder.searchPrice.setPaintFlags(holder.searchPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            holder.searchFinalPrice.setText(String.valueOf(initialPrice));
            holder.searchPrice.setText("");
            holder.searchDiscount.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder{

        private ImageView searchImage;
        private TextView searchName,searchPrice,searchFinalPrice,searchDiscount,searchRsTag;
//        private Button pentATC;

        public SearchViewHolder(@NonNull final View itemView) {
            super(itemView);
            itemView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth/2, LinearLayout.LayoutParams.WRAP_CONTENT));
            searchImage = itemView.findViewById(R.id.search_image);
            searchName = itemView.findViewById(R.id.search_name);
            searchPrice = itemView.findViewById(R.id.search_price);
            searchFinalPrice=itemView.findViewById(R.id.search_final_price);
            searchDiscount=itemView.findViewById(R.id.search_discount);
//            searchRsTag=itemView.findViewById(R.id.search_rs_tag);

//            pentATC = itemView.findViewById(R.id.pent_wish_buttton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        mListener.onItemClick(position);
                    }
                }
            });
        }
    }

    public interface OnSearchItemClickListener {
        void onItemAddToCart(int position,String category);
        void onItemClick(int position);
    }
}

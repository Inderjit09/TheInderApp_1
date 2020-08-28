package com.inderproduction.theinderapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inderproduction.theinderapp.Modals.OrderItem;
import com.inderproduction.theinderapp.Modals.Pent;
import com.inderproduction.theinderapp.Modals.Shirt;
import com.inderproduction.theinderapp.Modals.Shoes;
import com.inderproduction.theinderapp.Utilities.DisplayUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ProductDetail extends AppCompatActivity {

    private LinearLayout detailsHolder;
    private LinearLayout sizeHolder, otherImages, dynamicFieldsHolder, colorHolder;
    private ImageView imageFrame;
    private TextView detailProductName, detailProductPrice, detailProductDiscount, detailFinalProductPrice, detailProductRsTag;
    private TextView sizeChart;
    private TextView detailProductBrand, detailProductGender, detailProductCategory, detailProductMaterial, detailProductDeatil;
    private Button detailProductButton;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference folderReference = storage.getReference();
    private boolean defaultLoaded = false;

    int colorIndex;

    List<Uri> loadedImages = new ArrayList<>();

    private Object ob;
    private String SELECTED_ID = "";

    private OrderItem currentOrderItem = new OrderItem();
    private String itemTimestamp = "";
    private int width;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        DisplayMetrics matrices = getResources().getDisplayMetrics();
        width = matrices.widthPixels;

        detailsHolder = findViewById(R.id.details_holder);
        sizeHolder = findViewById(R.id.size_holder);
        imageFrame = findViewById(R.id.image_frame);
        otherImages = findViewById(R.id.other_images);
        sizeChart = findViewById(R.id.size_chart);
        detailProductButton = findViewById(R.id.detail_product_button);
        imageFrame.setLayoutParams(new LinearLayout.LayoutParams(width, width));

        detailProductDiscount = findViewById(R.id.detail_discount);
        detailProductPrice = findViewById(R.id.detail_price);
        detailFinalProductPrice = findViewById(R.id.detail_final_price);

        detailProductName = findViewById(R.id.detail_product_name);
        detailProductBrand = findViewById(R.id.detail_brand_value);
        detailProductGender = findViewById(R.id.detail_gender_value);
        detailProductCategory = findViewById(R.id.detail_category_value);
        detailProductMaterial = findViewById(R.id.detail_material_value);
        detailProductDeatil = findViewById(R.id.detail_product_detail_value);


        dynamicFieldsHolder = findViewById(R.id.dynamic_fields_holder);
        colorHolder = findViewById(R.id.color_holder);

        final String itemID = getIntent().getStringExtra("itemID");
        final String itemCategory = getIntent().getStringExtra("itemCategory");

        getDataFromServer(itemID, itemCategory);
        detailProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                itemTimestamp = "OI_" + System.currentTimeMillis();


                currentOrderItem.setOrderItemID(itemTimestamp);
                currentOrderItem.setItemCount(1);
                currentOrderItem.setItemID(itemID);
                currentOrderItem.setItemCategory(itemCategory);

                if (OneApplication.addItemToCart(currentOrderItem)) {

                    DisplayUtils.showToast(ProductDetail.this, "Added to Cart", Toast.LENGTH_SHORT);
                    finish();
                } else {
                    DisplayUtils.showToast(ProductDetail.this, "Already In Cart", Toast.LENGTH_SHORT);
                }

            }
        });

        sizeChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayUtils.showToast(ProductDetail.this, "Not Available Yet!", Toast.LENGTH_SHORT);
            }
        });

    }

    private void updateViews(Pent item) {
        detailProductName.setText(item.getItemName());
        detailProductBrand.setText(item.getItemBrand());
        detailProductGender.setText(item.getGender());
        detailProductCategory.setText(item.getItemCategory());
        detailProductMaterial.setText(item.getProductMaterial());
        detailProductDeatil.setText(item.getProductDetail());

        currentOrderItem.setItemCategory(item.getItemCategory());
        currentOrderItem.setItemName(item.getItemName());
        currentOrderItem.setItemBrand(item.getItemBrand());
        currentOrderItem.setItemPrice(item.getItemPrice());
        currentOrderItem.setItemDiscount(item.getDiscount());


        detailProductPrice.setText(String.valueOf(item.getItemPrice()));

        int dis = item.getDiscount();
        if (dis > 0) {
            detailProductDiscount.setText(String.valueOf(item.getDiscount()) + "% off");
            detailFinalProductPrice.setText("CAN " + String.valueOf(item.getItemPrice() - ((double) item.getDiscount() / 100 * item.getItemPrice())));
            detailProductPrice.setPaintFlags(detailProductPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            detailFinalProductPrice.setText("CAN " + String.valueOf(item.getItemPrice()));
            detailProductPrice.setText("");
            detailProductDiscount.setText("");
        }

        /*detailProductPrice.setText(item.getItemPrice()+" INR");
        detailProductPrice.setPaintFlags(detailProductPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        detailProductDiscount.setText(item.getDiscount()+"%");
        double newPrice = item.getItemPrice() - ((double)item.getDiscount()/100 * item.getItemPrice()) ;
        detailFinalProductPrice.setText("Rs. "+newPrice);*/


        addSizes(item.getSizesAvailable());
        try {
            addColors(item.getAvailableColors(), item.getAvailableImages());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateViews(Shirt item) {
        detailProductName.setText(item.getItemName());

        detailProductBrand.setText(item.getItemBrand());
        detailProductGender.setText(item.getGender());
        detailProductCategory.setText(item.getItemCategory());
        detailProductMaterial.setText(item.getProductMaterial());
        detailProductDeatil.setText(item.getProductDetail());

        currentOrderItem.setItemCategory(item.getItemCategory());
        currentOrderItem.setItemName(item.getItemName());
        currentOrderItem.setItemBrand(item.getItemBrand());
        currentOrderItem.setItemPrice(item.getItemPrice());
        currentOrderItem.setItemDiscount(item.getDiscount());


        detailProductPrice.setText(String.valueOf(item.getItemPrice()));
        int dis = item.getDiscount();
        if (dis > 0) {
            detailProductDiscount.setText(String.valueOf(item.getDiscount()) + "% off");
            detailFinalProductPrice.setText("CAN " + String.valueOf(item.getItemPrice() - ((double) item.getDiscount() / 100 * item.getItemPrice())));
            detailProductPrice.setPaintFlags(detailProductPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            detailFinalProductPrice.setText("CAN " + String.valueOf(item.getItemPrice()));
            detailProductPrice.setText("");
            detailProductDiscount.setText("");
        }

        addSizes(item.getSizesAvailable());
        try {
            addColors(item.getAvailableColors(), item.getAvailableImages());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateViews(Shoes item) {
        detailProductName.setText(item.getItemName());

        detailProductBrand.setText(item.getItemBrand());
        detailProductGender.setText(item.getGender());
        detailProductCategory.setText(item.getItemCategory());
        detailProductMaterial.setText(item.getProductMaterial());
        detailProductDeatil.setText(item.getProductDetail());

        currentOrderItem.setItemCategory(item.getItemCategory());
        currentOrderItem.setItemName(item.getItemName());
        currentOrderItem.setItemBrand(item.getItemBrand());
        currentOrderItem.setItemPrice(item.getItemPrice());
        currentOrderItem.setItemDiscount(item.getDiscount());


        detailProductPrice.setText(String.valueOf(item.getItemPrice()));
        int dis = item.getDiscount();
        if (dis > 0) {
            detailProductDiscount.setText(String.valueOf(item.getDiscount()) + "% off");
            detailFinalProductPrice.setText("CAN " + String.valueOf(item.getItemPrice() - ((double) item.getDiscount() / 100 * item.getItemPrice())));
            detailProductPrice.setPaintFlags(detailProductPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            detailFinalProductPrice.setText("CAN " + String.valueOf(item.getItemPrice()));
            detailProductPrice.setText("");
            detailProductDiscount.setText("");
        }

        addSizes(item.getSizesAvailable());


        addDynamicFields(item.getShoeType(), item.getSoleType());

        try {
            addColors(item.getAvailableColors(), item.getAvailableImages());
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void addSizes(List<String> sizes) {
        for (final String s : sizes) {
            if (currentOrderItem.getItemSize() == null) {
                currentOrderItem.setItemSize(s);
            }
            TextView tv = new TextView(this);

            int imgWidth = width / 10;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imgWidth, imgWidth);
            params.setMargins(10, 10, 10, 10);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(Color.WHITE);

            tv.setBackgroundResource(R.drawable.ic_fiber_manual_record_black_24dp);
            tv.setLayoutParams(params);
            tv.setText(s);
            tv.setTextSize(14.0f);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentOrderItem.setItemSize(s);
                    Toast.makeText(ProductDetail.this, "Size is " + s, Toast.LENGTH_SHORT).show();
                }
            });
            sizeHolder.addView(tv);
        }
    }

    private void addDynamicFields(String shoeType, String soleType) {

        String[] headings = {"Shoe Type", "Sole Type"};
        String[] contents = {shoeType, soleType};


        for (int x = 0; x < headings.length; x++) {
            //HOLDER TEXTVIEW
            LinearLayout shoeTypeLayout = new LinearLayout(this);
            LinearLayout.LayoutParams STparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            shoeTypeLayout.setOrientation(LinearLayout.HORIZONTAL);
            shoeTypeLayout.setLayoutParams(STparams);
            //HEADING TEXTVIEW
            TextView heading = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width / 2, LinearLayout.LayoutParams.WRAP_CONTENT);
            heading.setLayoutParams(params);
            heading.setText(headings[x] + " ");
            heading.setTextSize(20.0f);
            //CONTENT TEXVIEW
            TextView content = new TextView(this);
            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(width / 2, LinearLayout.LayoutParams.WRAP_CONTENT);
            content.setLayoutParams(contentParams);
            content.setText(contents[x]);
            content.setTextSize(20.0f);
            shoeTypeLayout.addView(heading);
            shoeTypeLayout.addView(content);
            dynamicFieldsHolder.addView(shoeTypeLayout);
        }


//        TextView tv2 = new TextView(this);
//        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        params.setMargins(10, 10, 10, 10);
//        tv2.setTextColor(Color.BLACK);
//        tv2.setLayoutParams(params);
//        tv2.setText("Sole Type " + soleType);
//        tv2.setTextSize(18.0f);
//        dynamicFieldsHolder.addView(tv2);
    }

    private void addColors(final List<String> colors, final List<Integer> images) throws JSONException {
        if (colors != null && colors.size() > 0)
            for (final String c : colors) {
                if (currentOrderItem.getItemColor() == null) {
                    currentOrderItem.setItemColor(c);
                }
                TextView tv3 = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50);
                params.setMargins(10, 10, 10, 10);
                tv3.setGravity(Gravity.CENTER);
                if (OneApplication.APPLICATION_COLORS != null) {
                    tv3.setBackgroundColor(Color.parseColor(OneApplication.APPLICATION_COLORS.getString(c)));
                } else {
                    tv3.setBackgroundColor(Color.BLACK);
                }
                tv3.setLayoutParams(params);
                tv3.setTextSize(14.0f);
                tv3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        colorIndex = colors.indexOf(c);
                        currentOrderItem.setItemColor(c);
                        addImages(SELECTED_ID, c, colorIndex, images);


                    }
                });
                colorHolder.addView(tv3);
            }
    }

    private void addImages(String productID, String color, int index, List<Integer> colorCount) {
        int MAXSIZE = 2 * 1024 * 1024;
        otherImages.removeAllViews();
//        for(int i = 0;i<imageColors.length;i++){
//            String s = imageColors[i];
        for (int k = 1; k <= colorCount.get(index); k++) {
            final ImageView im = createImageView();

            String fileName = productID + "_" + color.toUpperCase() + "_" + k + ".jpg";
//                Log.e("IMAGE_NAME",fileName);
            folderReference.child("products").child(fileName).getBytes(MAXSIZE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull final Task<byte[]> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
//                            Toast.makeText(ProductDetail.this,"IMAGE LOADED",Toast.LENGTH_SHORT).show();
                        byte[] data = task.getResult();
                        final Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
                        otherImages.addView(im);
                        im.setImageBitmap(b);
                        if (!defaultLoaded) {
                            imageFrame.setImageBitmap(b);
                            defaultLoaded = true;
                        }
                        im.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                imageFrame.setImageBitmap(b);
                            }
                        });
                    } else {
                        if (task.getException() != null) {

//                                Log.e("IMAGE LOAD ERROR",task.getException().getLocalizedMessage());
                        }
                    }
                }
            });
            //}
        }
    }


    private ImageView createImageView() {
        ImageView imageView = new ImageView(this);
        int imgWidth = width / 5;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imgWidth, imgWidth);
        params.setMargins(5, 5, 5, 5);
        imageView.setLayoutParams(params);
        return imageView;
    }

    private void getDataFromServer(String id, final String category) {

        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("database").whereEqualTo("itemID", id).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                    if (snapshot != null && task.getResult().getDocuments() != null) {
                        switch (category) {
                            case "pent":
                                Pent pent = snapshot.toObject(Pent.class);
                                ob = pent;
                                SELECTED_ID = pent.getItemID();
                                updateViews(pent);
                                addImages(pent.getItemID(), pent.getAvailableColors().get(0), 0, pent.getAvailableImages());
                                break;
                            case "shirt":
                                Shirt shirt = snapshot.toObject(Shirt.class);
                                ob = shirt;
                                SELECTED_ID = shirt.getItemID();
                                updateViews(shirt);
                                addImages(shirt.getItemID(), shirt.getAvailableColors().get(0), 0, shirt.getAvailableImages());
                                break;
                            case "footwear":
                                Shoes shoe = snapshot.toObject(Shoes.class);
                                ob = shoe;
                                SELECTED_ID = shoe.getItemID();
                                updateViews(shoe);
                                addImages(shoe.getItemID(), shoe.getAvailableColors().get(0), 0, shoe.getAvailableImages());
                                break;
                        }
                    }
                }
            }
        });
    }
}

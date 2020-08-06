package com.inderproduction.theinderapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.inderproduction.theinderapp.Adapters.CartListAdapter;
import com.inderproduction.theinderapp.Adapters.PentListAdapter;
import com.inderproduction.theinderapp.Adapters.SearchListAdapter;
import com.inderproduction.theinderapp.Modals.Pent;
import com.inderproduction.theinderapp.Utilities.CustomUtils;
import com.inderproduction.theinderapp.Utilities.DisplayUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SearchActivity extends AppCompatActivity implements SearchListAdapter.OnSearchItemClickListener {
    ProgressBar progressBar;
    Toolbar toolbar;
    RecyclerView recyclerSearchList;
    List<Object> searchData;
    int screenWidth;
    EditText searchBox;
    Spinner searchSpinner;
    private List<Object> completeData;
    private int selectedIndex = 0;
    String newString = "";
    private final String[] spinnerValues={"itemBrand","itemCategory"};
    private List<String> searchSpinnerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        toolbar = findViewById(R.id.shopping_toolbar);
        progressBar = findViewById(R.id.search_progress);
        recyclerSearchList = findViewById(R.id.search_list);
        searchBox = findViewById(R.id.search_box);
        searchSpinner = findViewById(R.id.search_spinner);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            //getSupportActionBar().setTitle("Title");
        }



        searchSpinnerList = new ArrayList<>();
        searchSpinnerList.add("Brand");
        searchSpinnerList.add("Category");

        selectedIndex = 0;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, searchSpinnerList);
        searchSpinner.setAdapter(adapter);

        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               selectedIndex = i;
           }

           @Override
           public void onNothingSelected(AdapterView<?> adapterView) { }
       });


        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = editable.toString().trim();

                if (!searchText.equals("") && searchText.length() > 3) {
                    Toast.makeText(SearchActivity.this,formatText(searchText),Toast.LENGTH_SHORT).show();
                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    CollectionReference colRef = firestore.collection("database");
                    colRef.whereEqualTo(spinnerValues[selectedIndex],formatText(searchText)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                completeData = new ArrayList<>();
                                for (DocumentSnapshot snap : task.getResult().getDocuments()) {

                                    Pent p = snap.toObject(Pent.class);
                                    completeData.add(p);
                                }
                                Log.e("COMPLETE DATA SIZEZ", completeData.size() + "");
                                updateListData(completeData);
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
    }

    private String formatText(String text) {
        if (searchSpinnerList.get(selectedIndex).equalsIgnoreCase("category")) {
            newString = "";
            newString = text.toLowerCase();
        } else {
            String[] words = text.split(" ");
             newString = "";
            for (String s : words) {
                String cap = (s.charAt(0) + "").toUpperCase();
                char[] data = s.toCharArray();
                data[0] = cap.toCharArray()[0];
                newString = newString + String.valueOf(data) + " ";
            }
        }
        return newString.trim();
    }


    private void updateListData(List<Object> newData){
        if(newData.size()>0){
            if(recyclerSearchList.getAdapter() != null){
                ((SearchListAdapter)recyclerSearchList.getAdapter()).updateData(newData);
            } else {
                recyclerSearchList.setLayoutManager(new GridLayoutManager(this,2));
                recyclerSearchList.setAdapter(new SearchListAdapter(this,newData,this,screenWidth));
            }
            progressBar.setVisibility(View.INVISIBLE);
        }
    }


    public void onItemAddToCart(int position, String category) {
//        OneApplication.addItemToCart(completeData.get(position), category);

        //JUST FOR UPDATING SINGLE ITEM # NOT USEFUL IN CURRENT SITUATION
//        if(productList.getAdapter() != null){
//            ((ClothListAdapter)productList.getAdapter()).updateSingleItem(completeData.get(position),position);
//        }

    }

    @Override
    public void onItemClick(int position) {
        Pent data = (Pent) completeData.get(position);
        Intent toDetails = new Intent(SearchActivity.this, ProductDetail.class);
        toDetails.putExtra("itemID", data.getItemID());
        toDetails.putExtra("itemCategory", data.getItemCategory());
        startActivity(toDetails);
    }
}


package com.example.yoshium.scraper;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import static java.lang.StrictMath.abs;


public class MainActivity extends Activity {
    private DatabaseReference mDatabase;
    private ListView mList;
    private ProductHolder productHolder;
    FirebaseRecyclerAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.addItemDecoration(new DividerItemDecoration(getBaseContext(),
                DividerItemDecoration.VERTICAL));

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("products");

        FirebaseRecyclerOptions<Product> options =
                new FirebaseRecyclerOptions.Builder<Product>()
                        .setQuery(query, Product.class)
                        .build();

        FirebaseRecyclerAdapter<Product, ProductHolder> adapter = new FirebaseRecyclerAdapter<Product, ProductHolder>(options) {
            private static final String TAG = "MyActivity";

            @NonNull
            @Override
            public ProductHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.view_item, viewGroup, false);

                return new ProductHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ProductHolder holder, int position, @NonNull Product model) {


                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8)
                {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    String website = model.getLink();
                    String final_price;

                    System.out.println("Before try : " + website);
                    try {
                        System.out.println("before jsoup connect");
                        Document doc = Jsoup.connect(website).get();
                        System.out.println("before product_price");
                        Elements product_price = doc.getElementsByClass("product-price");
                        int curr_price = Integer.parseInt(product_price.text().substring(1, product_price.text().indexOf(" ")));
                        //int curr_price = Integer.parseInt(product_price.text().replaceAll("(^|\\\\s)([0-9]+)($|\\\\s)", ""));
                        //int prev_price = Integer.parseInt(model.getPrice().replaceAll("(^|\\\\s)([0-9]+)($|\\\\s)+", ""));
                        int prev_price =  Integer.parseInt(model.getPrice().substring(1, model.getPrice().indexOf(" ")));
                        int diff = prev_price - curr_price;
                        if (curr_price < prev_price){
                            System.out.println("diff = " + curr_price + "-" + prev_price + "=" + diff);
                            final_price = "$" + curr_price + " CAD";
                            Product prod = new Product(model.getName(), model.getBrand() , final_price, model.getUrl(), model.getLink(), diff);
                            mDatabase.child("products").child(model.getBrand() + model.getName()).setValue(prod);

                        } else if (curr_price > prev_price){
                            System.out.println("diff = " + curr_price + "-" + prev_price + "=" + diff);
                            final_price = "$" + curr_price + " CAD";
                            Product prod = new Product(model.getName(), model.getBrand() , final_price, model.getUrl(), model.getLink(), diff);
                            mDatabase.child("products").child(model.getBrand() + model.getName()).setValue(prod);
                        } else {
                            final_price = "$" + curr_price + " CAD";
                        }
                        String t = "";
                        if(model.getPrice_diff() > 0)
                            t = "+" + model.getPrice_diff();
                        else if (model.getPrice_diff() < 0)
                            t = "-" + model.getPrice_diff();
                        else if (model.getPrice_diff() == 0){
                            t = "" + model.getPrice_diff();
                        }

                        holder.setAll(model.getBrand(), model.getName(), final_price, t);
                        holder.setImage(model.getUrl());
                        Log.e(TAG, model.getPrice());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

            }


        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);



        }

//    @Override
//    public void onStart() {
//        super.onStart();
//
//    }

    public void sendMessage(View view)
    {

        Intent intent = new Intent(MainActivity.this, Activity2.class);
        startActivity(intent);



    }
}



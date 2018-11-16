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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private ListView mList;
    private ProductHolder productHolder;
    FirebaseRecyclerAdapter adapter;
    private FirebaseAuth mAuth;
    private ImageButton mButton;

    Connection.Response response = null;

    String uid;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        mButton = (ImageButton) findViewById(R.id.button1);
        mButton.setVisibility(View.GONE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            System.out.println("USERID" + uid);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.addItemDecoration(new DividerItemDecoration(getBaseContext(),
                DividerItemDecoration.VERTICAL));

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child(uid);

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
                mButton.setVisibility(View.VISIBLE);
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

                    final String website = model.getLink();
                    String final_price = "";

                    try {
                        response = Jsoup.connect(website)
                                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                                .execute();
                        int statuscode = response.statusCode();
                        System.out.println("STATUS CODE " + statuscode);
                        Document doc = response.parse();
                        URL parseUrl = new URL(website);
                        System.out.println("DOC TITLE" + doc.title());
                        int curr_price = 0;
                        int prev_price = 0;

                        if (doc.title().equals(model.getPage_title())) {
                            System.out.println("TITLE MATCHES");
                            //SSENSE
                            if (parseUrl.getHost().equals("www.ssense.com")) {
                                Elements product_price = doc.getElementsByClass("product-price");
                                curr_price = Integer.parseInt(product_price.text().substring(1, product_price.text().indexOf(" "))); //Price from scraping
                                prev_price = Integer.parseInt(model.getPrice().substring(1, model.getPrice().indexOf(" "))); //Price from db
                            }

                            if (parseUrl.getHost().equals("www.grailed.com")) {
                                Elements product_price = doc.getElementsByClass("-price");

                                String parsePrice = product_price.text();

                                if (parsePrice.length() > 6) {
                                    parsePrice = parsePrice.substring(0, parsePrice.indexOf(" "));
                                    System.out.println("PARSE PRICE " + parsePrice);
                                }

                                parsePrice = parsePrice.replaceAll("[,]", "");

                                curr_price = Integer.parseInt(parsePrice.substring(1, parsePrice.length())); //Price from scraping
                                prev_price = Integer.parseInt(model.getPrice().substring(1, model.getPrice().indexOf(" "))); //Price from db
                            }

                            int diff = prev_price - curr_price;
                            if (curr_price < prev_price) {
                                System.out.println("diff = " + curr_price + "-" + prev_price + "=" + diff);
                                if (parseUrl.getHost().equals("www.ssense.com")) {
                                    final_price = "$" + curr_price + " CAD";
                                }
                                if (parseUrl.getHost().equals("www.grailed.com")) {
                                    final_price = "$" + curr_price + " USD";
                                }
                                Product prod = new Product(model.getPage_title(), model.getName(), model.getBrand(), final_price, model.getUrl(), model.getLink(), diff);
                                mDatabase.child(uid).child(model.getBrand() + model.getName()).setValue(prod);

                            } else if (curr_price > prev_price) {
                                System.out.println("diff = " + curr_price + "-" + prev_price + "=" + diff);
                                if (parseUrl.getHost().equals("www.ssense.com")) {
                                    final_price = "$" + curr_price + " CAD";
                                }
                                if (parseUrl.getHost().equals("www.grailed.com")) {
                                    final_price = "$" + curr_price + " USD";
                                }
                                Product prod = new Product(model.getPage_title(), model.getName(), model.getBrand(), final_price, model.getUrl(), model.getLink(), diff);
                                mDatabase.child(uid).child(model.getBrand() + model.getName()).setValue(prod);
                            } else {
                                if (parseUrl.getHost().equals("www.ssense.com")) {
                                    final_price = "$" + curr_price + " CAD";
                                }
                                if (parseUrl.getHost().equals("www.grailed.com")) {
                                    final_price = "$" + curr_price + " USD";
                                }
                            }

                            int percentage = (int) 100.0 * model.getPrice_diff() / curr_price;
                            System.out.println("%: " + model.getPrice_diff() + "/" + curr_price + "=" + percentage);
                            String t = "0";

                            if (model.getPrice_diff() > 0)
                                t = "(+" + percentage + "%)";
                            else if (model.getPrice_diff() < 0)
                                t = "(" + percentage + "%)";
                            else
                                t = "000";

                            Log.e(TAG, t);

                            holder.setAll(model.getBrand(), model.getName(), final_price, t, website);
                            holder.setImage(model.getUrl());
                        } else {
                            System.out.println("PRODUCT DOES NOT EXIST ANYMORE");
                        }


                        //mButton.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                        //System.out.println("PRODUCT DOES NOT EXIST ANYMORE");
                        String noStockMsg = "Sorry but " + model.getName() + " from " + model.getBrand() + "is no longer available.";
                        Toast.makeText(MainActivity.this, noStockMsg, Toast.LENGTH_LONG).show();
                        mDatabase.child(uid).child(model.getBrand() + model.getName()).removeValue();
                        e.printStackTrace();
                    }



                }

            }


        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getBaseContext(),
                DividerItemDecoration.VERTICAL));
        mButton.setVisibility(View.VISIBLE);




        }

//    @Override
//    public void onStart() {
//        super.onStart();
//
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.signOut:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, logIn.class);
                startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    public void sendMessage(View view)
    {

        Intent intent = new Intent(MainActivity.this, Activity2.class);
        intent.putExtra("ID", uid);
        startActivity(intent);



    }
    @Override
    public void onBackPressed() {

        return;
    }
}



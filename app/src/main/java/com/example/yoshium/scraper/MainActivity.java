package com.example.yoshium.scraper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetHost;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
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

    //For FAB
    FloatingActionMenu floatingActionMenu;
    FloatingActionButton fab_search, fab_link;

    //For JSOUP
    Connection.Response response = null;

    //User ID
    private String uid;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        //Init fab buttons
        floatingActionMenu = (FloatingActionMenu) findViewById(R.id.floatingactiomenu);
        fab_link = (FloatingActionButton) findViewById(R.id.floatingActionLink);
        fab_search = (FloatingActionButton) findViewById(R.id.floatingActionSearch);

        final Dialog fabDialog = new Dialog(MainActivity.this);
        fabDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fabDialog.setContentView(R.layout.dialog_link);

        //Onclick listener for link fab.
        fab_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(v);
            }
        });

        //Onclick listener for search fab.
        fab_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText fab_edit = (EditText) fabDialog.findViewById(R.id.url_edit);
                Button ssense_btn = (Button) fabDialog.findViewById(R.id.url_button);
                Button haven_btn = (Button) fabDialog.findViewById(R.id.haven_button);
                Button google_btn = (Button) fabDialog.findViewById(R.id.google_button);

                ssense_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String fab_query = fab_edit.getText().toString();
                        fab_edit.setText("");
                        System.out.println("fab_query" + fab_query);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        String changed_query = "https://www.ssense.com/en-ca/men?q=" + fab_query.replaceAll(" ", "%20");
                        System.out.println("fab_query" + changed_query);
                        intent.setData(Uri.parse(changed_query));
                        startActivity(intent);
                        fabDialog.dismiss();
                    }
                });

                haven_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String fab_query = fab_edit.getText().toString();
                        fab_edit.setText("");
                        System.out.println("fab_query" + fab_query);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        String changed_query = "https://shop.havenshop.com/pages/search-results?q=" + fab_query.replaceAll(" ", "%20");
                        System.out.println("fab_query" + changed_query);
                        intent.setData(Uri.parse(changed_query));
                        startActivity(intent);
                        fabDialog.dismiss();
                    }
                });

                google_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String fab_query = fab_edit.getText().toString();
                        fab_edit.setText("");
                        System.out.println("fab_query" + fab_query);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        String changed_query = "https://www.google.com/search?q=" + fab_query.replaceAll(" ", "%20");
                        System.out.println("fab_query" + changed_query);
                        intent.setData(Uri.parse(changed_query));
                        startActivity(intent);
                        fabDialog.dismiss();
                    }
                });

                fabDialog.show();
            }
        });


        //Set uid so no need to keep using user function in future.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        //Reference the FireBase database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        //Init RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(getBaseContext(),
                DividerItemDecoration.VERTICAL));

        //Set Query to user's database in backend.
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child(uid);

        //Set options to show in RecyclerView
        FirebaseRecyclerOptions<Product> options =
                new FirebaseRecyclerOptions.Builder<Product>()
                        .setQuery(query, Product.class)
                        .build();

        //Init RecyclerAdapter
        FirebaseRecyclerAdapter<Product, ProductHolder> adapter = new FirebaseRecyclerAdapter<Product, ProductHolder>(options) {

            private static final String TAG = "MyActivity";

            //Inflate layout "Product Holder" on each of the cells of RecyclerView
            @NonNull
            @Override
            public ProductHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.view_item, viewGroup, false);

                return new ProductHolder(view);
            }

            //Set what happens in each of the cells.
            @Override
            protected void onBindViewHolder(@NonNull final ProductHolder holder, int position, @NonNull Product model) {

                //Init Dialog
                final Dialog myDialog = new Dialog(MainActivity.this);
                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myDialog.setContentView(R.layout.dialog_content);

                final String test_name = model.getName();
                final String test_brand = model.getBrand();

                //Set onclick listener on each of the holders
                holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        TextView dialog_name = (TextView) myDialog.findViewById(R.id.dialog_name);
                        TextView dialog_brand = (TextView) myDialog.findViewById(R.id.dialong_brand);
                        Button view_browser = (Button) myDialog.findViewById(R.id.view_in_browser_btn);
                        Button del = (Button) myDialog.findViewById(R.id.delete_btn);

                        //Set onclick for the view browser
                        view_browser.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Redirect to site from db associated with the cell.
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse((String) holder.link.getText()));
                                v.getContext().startActivity(intent);
                                myDialog.dismiss();
                            }
                        });

                        del.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Delete selected cell from db.
                                String delete_message = test_brand + " " + test_name + " has been deleted from the database.";
                                Toast.makeText(MainActivity.this, delete_message, Toast.LENGTH_LONG).show();
                                mDatabase.child(uid).child(test_brand + test_name).removeValue();
                                myDialog.dismiss();
                            }
                        });

                        //Show the dialog
                        dialog_name.setText(test_name);
                        dialog_brand.setText(test_brand);
                        myDialog.show();
                    }
                });

                //Scrape the site and set onto the cells.
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8)
                {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    final String website = model.getLink();
                    String final_price = "";

                    try {
                        //Get response from URL provided.
                        response = Jsoup.connect(website)
                                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                                .execute();

                        //int statuscode = response.statusCode();

                        //Parse the HTML from the response
                        Document doc = response.parse();
                        URL parseUrl = new URL(website);

                        int curr_price = 0;
                        int prev_price = 0;

                        if ((doc.title().replaceAll("&", "and")).equals(model.getPage_title())) {
                            //SSENSE: Parse the price
                            if (parseUrl.getHost().equals("www.ssense.com")) {
                                Elements product_price = doc.getElementsByClass("product-price");
                                String parse_price = product_price.text();
                                if (parse_price.length() > 10 ) {
                                    parse_price = parse_price.substring(parse_price.indexOf(" ") + 5, parse_price.lastIndexOf(" ") + 4);
                                    System.out.println("PARSE PRICE " + parse_price);
                                }
                                curr_price = Integer.parseInt(parse_price.substring(1, parse_price.indexOf(" "))); //Price from scraping
                                prev_price = Integer.parseInt(model.getPrice().substring(1, model.getPrice().indexOf(" "))); //Price from db
                            }

                            //HAVEN
                            if (parseUrl.getHost().equals("shop.havenshop.com")) {
                                Elements product_price = doc.getElementsByClass("price");
                                curr_price = Integer.parseInt((product_price.text().substring(1, product_price.text().indexOf("."))).replaceAll(",", "")); //Price from scraping
                                System.out.println("curr_price" + curr_price);
                                prev_price = Integer.parseInt(model.getPrice().substring(1, model.getPrice().indexOf(" "))); //Price from db
                            }

                            //GRAILED: Parse the Price
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
                                if (parseUrl.getHost().equals("www.ssense.com") || parseUrl.getHost().equals("shop.havenshop.com")) {
                                    final_price = "$" + curr_price + " CAD";
                                }
                                if (parseUrl.getHost().equals("www.grailed.com")) {
                                    final_price = "$" + curr_price + " USD";
                                }
                                Product prod = new Product(model.getPage_title(), model.getName(), model.getBrand(), final_price, model.getUrl(), model.getLink(), diff);
                                mDatabase.child(uid).child(model.getBrand() + model.getName()).setValue(prod);

                            } else if (curr_price > prev_price) {
                                System.out.println("diff = " + curr_price + "-" + prev_price + "=" + diff);
                                if (parseUrl.getHost().equals("www.ssense.com") || parseUrl.getHost().equals("shop.havenshop.com")) {
                                    final_price = "$" + curr_price + " CAD";
                                }
                                if (parseUrl.getHost().equals("www.grailed.com")) {
                                    final_price = "$" + curr_price + " USD";
                                }
                                Product prod = new Product(model.getPage_title(), model.getName(), model.getBrand(), final_price, model.getUrl(), model.getLink(), diff);
                                mDatabase.child(uid).child(model.getBrand() + model.getName()).setValue(prod);
                            } else {
                                if (parseUrl.getHost().equals("www.ssense.com") || parseUrl.getHost().equals("shop.havenshop.com")) {
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

                            System.out.println("Model get Brand " + model.getBrand() + model.getName());
                            holder.setAll(model.getBrand(), model.getName(), final_price, t, website);
                            holder.setImage(model.getUrl());
                        }

                    } catch (IOException e) {
                        //If product is taken down from website, delete from database and notify user with Toast.
                        //MAYBE: In future, instead of Toast use Dialog instead to be more apparent.
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

        }


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



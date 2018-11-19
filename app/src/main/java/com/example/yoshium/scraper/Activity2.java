package com.example.yoshium.scraper;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;


public class Activity2 extends Activity {

    EditText mEditText;
    private DatabaseReference mDatabase;
    private String key;
    private String image_url;
    private String id;
    ImageButton mImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_2);

        mEditText = findViewById(R.id.edit_text);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        id = getIntent().getStringExtra("ID");

        mImageButton = (ImageButton) findViewById(R.id.back_btn);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity2.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void save(View v){

        new doit().execute();
        Intent intent = new Intent(Activity2.this, MainActivity.class);
        startActivity(intent);
    }


    public class doit extends AsyncTask<Void, Void, Void> {
        String msg;
        String website;
        @Override
        protected Void doInBackground(Void... voids) {
            website = mEditText.getText().toString();
            try {
                Document doc = Jsoup.connect(website).get();
                URL parseUrl = new URL(website);

                String page_title;
                Elements product_name;
                Elements product_brand;
                Elements product_price;
                Elements imageElements;

                //SSENSE
                if (parseUrl.getHost().equals("www.ssense.com")) {
                    page_title = doc.title();
                    product_name = doc.getElementsByClass("product-name");
                    product_brand = doc.getElementsByClass("product-brand");
                    product_price = doc.getElementsByClass("product-price");
                    imageElements = doc.getElementsByTag("img");
                    image_url = imageElements.get(1).attr("data-srcset");

                    key = product_brand.text() + product_name.text();

                    Product prod = new Product(page_title, product_name.text(), product_brand.text(),product_price.text(), image_url, website, 0);
                    mDatabase.child(id).child(key).setValue(prod);
                    mEditText.getText().clear();
                }

                //GRAILED
                if (parseUrl.getHost().equals("www.grailed.com")) {
                    page_title = doc.title();
                    product_name = doc.getElementsByClass("listing-title sub-title");
                    product_brand = doc.getElementsByClass("designer jumbo");
                    product_price = doc.getElementsByClass("-price");
                    String parsePrice = product_price.text();
                    System.out.println("PRODUCT PRICE " + product_price.text() );

                    if (parsePrice.length() > 6){
                        parsePrice = parsePrice.substring(0, parsePrice.indexOf(" "));
                        System.out.println("PARSE PRICE " + parsePrice);
                    }

                    parsePrice = parsePrice.replaceAll("[,]", "");


                    imageElements = doc.getElementsByTag("img");
                    image_url = imageElements.get(0).attr("src");
                    System.out.println("IMAGEURL" + image_url);

                    key = product_brand.text() + product_name.text();

                    Product prod = new Product(page_title, product_name.text(), product_brand.text(),(parsePrice + " USD"), image_url, website, 0);
                    System.out.println("prod made");
                    mDatabase.child(id).child(key).setValue(prod);
                    System.out.println(key + " posted to database.");
                    mEditText.getText().clear();
                }


            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}

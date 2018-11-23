package com.example.yoshium.scraper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.net.URL;


public class Activity2 extends Activity {

    EditText mEditText;
    private DatabaseReference mDatabase;
    private String key;
    private String image_url;
    private String id;
    TextView title_msg;
    TextView supported_sites;
    ImageButton mImageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_2);

        title_msg = (TextView) findViewById(R.id.url_textview);
        supported_sites = (TextView) findViewById(R.id.sites_supported);

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
        String check = mEditText.getText().toString();

        try {
            URL checkURL = new URL(check);
            if (!((checkURL.getHost().equals("www.ssense.com")) || (checkURL.getHost().equals("www.grailed.com")) || (checkURL.getHost().equals("shop.havenshop.com")))){
                Toast.makeText(Activity2.this,"Please enter a valid URL.", Toast.LENGTH_SHORT).show();
                mEditText.getText().clear();
                supported_sites.setTextColor(Color.RED);
            } else {
                new doit().execute();
                Intent intent = new Intent(Activity2.this, MainActivity.class);
                startActivity(intent);
            }
        } catch (MalformedURLException e) {
            Toast.makeText(Activity2.this,"Please enter a valid URL.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public String clearSpecials(String s){
        s = s.replaceAll( "&" , "and" );
        s = s.replaceAll( "[^a-zA-Z0-9 ]" , "" );
        return(s);
    }

    public class doit extends AsyncTask<Void, Void, Void> {
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
                    page_title = doc.title().replaceAll("&", "and");

                    product_name = doc.getElementsByClass("product-name");
                    String parse_name = product_name.text();
                    parse_name = parse_name.replaceAll("&", "and");

                    product_brand = doc.getElementsByClass("product-brand");
                    String parse_brand = product_brand.text();
                    parse_brand = parse_brand.replaceAll("&", "and");
                    product_price = doc.getElementsByClass("product-price");

                    String parse_price = product_price.text();
                    if (parse_price.length() > 10 ) {
                        parse_price = parse_price.substring(parse_price.indexOf(" ") + 5, parse_price.lastIndexOf(" ") + 4);
                    }
                    System.out.println("SSENSE PRICE " + parse_price);
                    imageElements = doc.getElementsByTag("img");
                    image_url = imageElements.get(1).attr("data-srcset");

                    key = parse_brand + parse_name;

                    Product prod = new Product(page_title, parse_name, parse_brand ,parse_price, image_url, website, 0);
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

                if (parseUrl.getHost().equals("shop.havenshop.com")) {
                    page_title = doc.title();
                    String haven_product_name = page_title.substring(0, (page_title.lastIndexOf("–")));
                    haven_product_name = haven_product_name.replaceAll( "[^a-zA-Z0-9 ]" , "" );
                    product_brand = doc.getElementsByClass("product-heading-vendor");
                    product_price = doc.getElementsByClass("price");
                    String first_cad = product_price.text().substring(0,product_price.text().indexOf(" ")-3);
                    first_cad = first_cad.replaceAll(",", "");
                    String second_cad = first_cad + " " + product_price.text().substring(product_price.text().indexOf(" ")+1 , product_price.text().indexOf("D")+1);
                    imageElements = doc.getElementsByTag("img");
                    image_url = "https://" +  imageElements.get(4).attr("src");


                    key = product_brand.first().text() + haven_product_name;

                    Product prod = new Product(page_title, haven_product_name, (product_brand.first()).text(),second_cad, image_url, website, 0);
                    mDatabase.child(id).child(key).setValue(prod);
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

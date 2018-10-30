package com.example.yoshium.scraper;

import android.app.Activity;
import android.app.Application;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Activity2 extends Activity {

    private static final String FILE_NAME = "example.txt";
    EditText mEditText;
    private DatabaseReference mDatabase;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_2);
        mEditText = findViewById(R.id.edit_text);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        key = mDatabase.child("products").push().getKey();
    }

    public void save(View v){
        new doit().execute();
    }

    public void load(View v){

            String text;

    }

    public class doit extends AsyncTask<Void, Void, Void> {
        String msg;
        String website;
        @Override
        protected Void doInBackground(Void... voids) {
            website = mEditText.getText().toString();
            try {
                Document doc = Jsoup.connect(website).get();
                Elements product_name = doc.getElementsByClass("product-name");
                Elements product_brand = doc.getElementsByClass("product-brand");
                Elements product_price = doc.getElementsByClass("product-price");
                System.out.println("BEFORE:::::");
                //Elements image = doc.select("img.product-detail.lazyloaded");
                //System.out.println("image_text : " + image.text());
                Elements imageElements = doc.getElementsByTag("img");
                int count = 1;
                String url = "";
                for (Element element : imageElements) {
                    System.out.println("src: "+element.attr("data-srcset"));

                    System.out.println("title: "+element.attr("alt"));
                    System.out.println("count: " + count);
                    if(count == 2) {
                        url = element.attr("data-srcset");
                    }
                    count++;

                }
                msg =   product_brand.text() + "'s " + product_name.text() + " stored." ;

                HashMap<String, String> dataMap = new HashMap<String, String>();
                dataMap.put("link", website);
                dataMap.put("product_name", product_name.text());
                dataMap.put("product_brand", product_brand.text());
                dataMap.put("current_price", product_price.text());
//                dataMap.put("previous_price", )

//                mDatabase.child(product_name.text()).child("product_brand").setValue(product_brand.text());
//                mDatabase.child(product_name.text()).child("current_price").setValue(product_price.text());
                Product prod = new Product(product_name.text(), product_brand.text(),product_price.text(), url);
                mDatabase.child("products").child(key).setValue(prod);
                mEditText.getText().clear();




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

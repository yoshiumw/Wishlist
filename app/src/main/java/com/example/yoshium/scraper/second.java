package com.example.yoshium.scraper;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class second extends AppCompatActivity {
    private static final String FILE_NAME = "example.txt";
    EditText mEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mEditText = findViewById(R.id.edit_text);
    }

    public void save(View v){
        new doit().execute();
    }

    public void load(View v){
        FileInputStream fis = null;

        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine())!= null){
                sb.append(text).append("\n");
            }

            mEditText.setText(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class doit extends AsyncTask<Void, Void, Void>{
        String words;
        String website;
        @Override
        protected Void doInBackground(Void... voids) {
            website = mEditText.getText().toString();
            FileOutputStream fos = null;

            try {
                Document doc = Jsoup.connect(website).get();
                Elements content = doc.getElementsByClass("product-name");
                words = content.text();

                fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
                fos.write(words.getBytes());

                mEditText.getText().clear();
                Toast.makeText(getApplicationContext(), "Saved to " + getFilesDir() + "/" + FILE_NAME , Toast.LENGTH_LONG).show();


            }catch(Exception e){
                e.printStackTrace();
            } finally {
                if (fos != null){
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mEditText.setText(words);
        }
    }
}

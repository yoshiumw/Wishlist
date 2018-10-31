package com.example.yoshium.scraper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.type.Color;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class ProductHolder extends RecyclerView.ViewHolder {
    private final TextView brand;
    private final TextView name;
    private final TextView price;
    private final ImageView image;
    private final TextView diff;

    public ProductHolder(@NonNull View itemView) {
        super(itemView);
        brand = (TextView) itemView.findViewById(R.id.text_brand);
        name = (TextView) itemView.findViewById(R.id.text_name);
        price = (TextView) itemView.findViewById(R.id.text_price);
        image = (ImageView) itemView.findViewById(R.id.imageView);
        diff = (TextView) itemView.findViewById(R.id.text_diff);
    }

    public void setAll(String b, String n, String p, String d){
        brand.setText(b);
        name.setText(n);
        price.setText(p);


        if (d.charAt(0) == '-'){
            diff.setTextColor(0xFF32CD32);
        } else if (d.charAt(0) == '+'){
          diff.setTextColor(0xFFFF0000);
        } else if (d.charAt(0) == '0'){
            d = " ";
        }

        diff.setText(d);

    }

    public void setBrand(String t){
        brand.setText(t);
    }

    public void setName(String t){
        name.setText(t);
    }

    public void setPrice(String t){
        price.setText(t);
    }

    public void setImage(String u){
        Picasso.get().load(u).into(image);
    }

}

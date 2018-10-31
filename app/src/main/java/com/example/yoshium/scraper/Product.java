package com.example.yoshium.scraper;

import android.os.AsyncTask;
import android.os.StrictMode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import static java.lang.StrictMath.abs;

public class Product {
    public String name;
    public String brand;
    public String price;
    public String image_url;
    public String link;
    public int price_diff;


    public Product() {

    }



    public Product(String name, String brand, String price, String image_url, String link, int price_diff){
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.image_url = image_url;
        this.link = link;
        this.price_diff = price_diff;
    }

    public int getPrice_diff() {
        return price_diff;
    }

    public void setPrice_diff(int price_diff) {
        this.price_diff = price_diff;
    }


    public String getUrl() {
        return image_url;
    }

    public void setUrl(String url) {
        this.image_url = url;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}

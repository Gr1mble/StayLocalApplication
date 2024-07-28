package com.example.staylocal.fragments.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Business implements Serializable {
    private String docId;
    private String userId;
    private String name;
    private String address;
    private String imgURL;
    private String hours;
    private double rating;
    private String phone;

    private double latitude;
    private double longitude;
    private ArrayList<String> categories = new ArrayList<>();
    private String price;

    public Business() {}

    public Business(String name, String address, String imgURL, String hours, double rating, String phone, String price) {
        this.name = name;
        this.address = address;
        this.imgURL = imgURL;
        this.hours = hours;
        this.rating = rating;
        this.phone = phone;
        this.price = price;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void addCategory(String category) {
        this.categories.add(category);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

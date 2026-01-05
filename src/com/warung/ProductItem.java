/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.warung;

/**
 *
 * @author fahri
 */
public class ProductItem {
    private int id;
    private String name;
    private String sku;
    private int stock;
    private int price;

    // Constructor tanpa parameter
    public ProductItem() {
    }

    // Constructor dengan parameter
    public ProductItem(int id, String name, String sku, int stock, int price) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.stock = stock;
        this.price = price;
    }

    // Getter dan Setter
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    // Optional: untuk debugging / logging
    @Override
    public String toString() {
        return "ProductItem{" +
                "name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", stock=" + stock +
                ", price=" + price +
                '}';
    }
}

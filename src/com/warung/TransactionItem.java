/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.warung;

/**
 *
 * @author fahri
 */
public class TransactionItem {
    private int productId;
    private int qty;
    private int subTotalPrice;

    // Empty constructor
    public TransactionItem() {
    }

    // Constructor
    public TransactionItem(int productId, int qty, int subTotalPrice) {
        this.productId = productId;
        this.qty = qty;
        this.subTotalPrice = subTotalPrice;
    }

    // Getters & Setters
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getSubTotalPrice() {
        return subTotalPrice;
    }

    public void setSubTotalPrice(int subTotalPrice) {
        this.subTotalPrice = subTotalPrice;
    }
}

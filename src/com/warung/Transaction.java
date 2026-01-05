/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.warung;

/**
 *
 * @author fahri
 */
public class Transaction {
    private long id;
    private String code;
    private String status;
    private long total;

    public Transaction(long id, String code, long total, String status) {
        this.id = id;
        this.code = code;
        this.total = total;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return code + " | Rp " + String.format("%,d", total) + " | " + status;
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JInternalFrame.java to edit this template
 */
package com.warung;

import com.warung.utils.Currency;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import java.awt.Window;
import javax.swing.SwingUtilities;
import java.sql.PreparedStatement;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author fahri
 */
public class TransactionNewForm extends javax.swing.JInternalFrame {
    
    public static final List<TransactionItem> transactionItems = new ArrayList<>();
    public static final List<ProductItem> productItems = new ArrayList<>();
    private DefaultTableModel transactionModel = null;
    private int totalPrice = 0;
    

    /**
     * Creates new form TransactionNewForm
     */
    public TransactionNewForm() {
        initComponents();
        loadItem();
        
        Currency curr = new Currency();

        textPay.setFormatterFactory(
            new javax.swing.text.DefaultFormatterFactory(curr.createRupiahFormatter())
        );

        textPay.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        textPay.setValue(0L);

        textPay.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateReturn();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateReturn();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateReturn();
            }
        });
    }
    
    public void loadItem (){
        selectProduct.removeAllItems();
        
        transactionModel = (DefaultTableModel) tableTransaction.getModel();
        transactionModel.addColumn("Nama Produk");
        transactionModel.addColumn("Jumlah");
        transactionModel.addColumn("Harga Satuan");
        transactionModel.addColumn("Sub Total");

        String sql = "SELECT id, name, sku, price FROM products";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String sku = rs.getString("sku");
                String name = rs.getString("name");
                int price = rs.getInt("price");
                
                ProductItem productItem = new ProductItem(id, name, sku, 1, price);
                productItems.add(productItem);

                if (sku == null) {
                    sku = "NO-SKU";
                }

                selectProduct.addItem(name);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }
    
    private void updateReturn() {
        Currency curr = new Currency();

        Object value = textPay.getValue();
        System.out.println(value);
        int pay = (value instanceof Number) ? ((Number) value).intValue(): 0;

        int change = pay - totalPrice;

        txtReturn.setText(
            curr.formatRupiah(Math.max(change, 0))
        );
    }
    
    private void saveTransaction() {
        
        int number = ThreadLocalRandom.current().nextInt(100000, 1000000);

        Currency curr = new Currency();
        int paidAmount = curr.parseRupiah(textPay.getText());
        int changeAmount = paidAmount - totalPrice;

        if (paidAmount < totalPrice) {
            JOptionPane.showMessageDialog(this,
                "Uang bayar kurang!",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        Connection conn = null;
        PreparedStatement psTransaction = null;
        PreparedStatement psItem = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // 🔥 TRANSACTION START

            // 1️⃣ INSERT TRANSACTION
            String sqlTransaction =
                "INSERT INTO transactions (total_price, paid_amount, change_amount, transaction_code, status) " +
                "VALUES (?, ?, ?, ?, ?)";

            psTransaction = conn.prepareStatement(
                sqlTransaction,
                Statement.RETURN_GENERATED_KEYS
            );

            psTransaction.setInt(1, totalPrice);
            psTransaction.setInt(2, paidAmount);
            psTransaction.setInt(3, changeAmount);
            psTransaction.setString(4, "WR-" + number);
            psTransaction.setString(5, "Cash");
            psTransaction.executeUpdate();

            // 2️⃣ GET GENERATED TRANSACTION ID
            rs = psTransaction.getGeneratedKeys();
            int transactionId = 0;
            if (rs.next()) {
                transactionId = rs.getInt(1);
            }

            // 3️⃣ INSERT TRANSACTION ITEMS
            String sqlItem =
                "INSERT INTO transaction_items " +
                "(transaction_id, product_id, qty, sub_total_price) " +
                "VALUES (?, ?, ?, ?)";

            psItem = conn.prepareStatement(sqlItem);

            for (TransactionItem item : transactionItems) {
                psItem.setInt(1, transactionId);
                psItem.setInt(2, item.getProductId());
                psItem.setInt(3, item.getQty());
                psItem.setInt(4, item.getSubTotalPrice());
                psItem.addBatch();
            }

            psItem.executeBatch();

            conn.commit(); // ✅ COMMIT

            JOptionPane.showMessageDialog(this,
                "Transaksi berhasil disimpan\n" +
                "Kembalian: " + curr.formatRupiah(changeAmount),
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );

            resetTransactionForm();

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback(); // ❌ ROLLBACK
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Gagal menyimpan transaksi",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        } finally {
            try {
                if (rs != null) rs.close();
                if (psTransaction != null) psTransaction.close();
                if (psItem != null) psItem.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    
    private void handleBarcode(String barcode) {

        // Find product by barcode
        for (int i = 0; i < productItems.size(); i++) {
            ProductItem product = productItems.get(i);

            if (product.getSku().equals(barcode)) {

                // auto select product
                selectProduct.setSelectedIndex(i);

                // default qty = 1
                textQty.setText("1");

                // auto add product
                btnAddActionPerformed(null);

                return;
            }
        }

        JOptionPane.showMessageDialog(this,
            "Produk tidak ditemukan!\nBarcode: " + barcode,
            "Not Found",
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    private void resetTransactionForm() {
        transactionItems.clear();
        transactionModel.setRowCount(0);

        totalPrice = 0;
        txtTotalPrice.setText("Rp. 0");
        txtReturn.setText("Rp. 0");

        textPay.setValue(0);
        textQty.setText("");
    }
    
    public void resetForm() {
        textQty.setText("");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel4 = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        txtReturn = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        btnClose = new javax.swing.JButton();
        textPay = new javax.swing.JFormattedTextField();
        txtTotalPrice = new javax.swing.JLabel();
        textQty = new javax.swing.JFormattedTextField();
        selectProduct = new javax.swing.JComboBox<>();
        btnAdd = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableTransaction = new javax.swing.JTable();
        btnScanBarcode = new javax.swing.JButton();

        jLabel4.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jLabel4.setText("Kembalian");

        btnSave.setText("Simpan");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        jLabel5.setText("Pilih Barang");

        txtReturn.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        txtReturn.setText("Rp. 0");

        jLabel3.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jLabel3.setText("Bayar");

        jLabel2.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jLabel2.setText("Total");

        jLabel6.setText("Jumlah");

        btnClose.setText("Tutup");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        textPay.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#"))));

        txtTotalPrice.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        txtTotalPrice.setText("Rp. 0");

        textQty.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#"))));

        btnAdd.setText("Tambahkan");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        jLabel1.setText("Tambah Transaksi Baru");

        tableTransaction.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tableTransaction);

        btnScanBarcode.setText("Scan Barcode");
        btnScanBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnScanBarcodeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 646, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel4))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(txtTotalPrice)
                                    .addComponent(txtReturn)
                                    .addComponent(textPay, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(btnClose)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnSave))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addGap(41, 41, 41)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(textQty, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                                            .addComponent(selectProduct, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addComponent(jLabel6))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnAdd)
                                .addGap(18, 18, 18)
                                .addComponent(btnScanBarcode)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnScanBarcode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel5)
                                .addComponent(selectProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(9, 9, 9)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel6)
                                .addComponent(textQty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtTotalPrice))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(textPay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtReturn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnClose))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        
        if (textQty.getText().equals("")) {
            JOptionPane.showMessageDialog(this,
                "Jumlah belum di input!",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        Currency curr = new Currency();
        ProductItem selectedProduct = productItems.get(selectProduct.getSelectedIndex());

        int qty = Integer.parseInt(textQty.getText());
        int price = selectedProduct.getPrice();
        int subTotal = qty * price;

        boolean found = false;

        // 🔁 Check if product already exists
        for (int i = 0; i < transactionItems.size(); i++) {

            TransactionItem item = transactionItems.get(i);

            if (item.getProductId() == selectedProduct.getId()) {

                int newQty = item.getQty() + qty;
                int newSubTotal = newQty * price;

                // update list
                item.setQty(newQty);
                item.setSubTotalPrice(newSubTotal);

                // update table row
                transactionModel.setValueAt(newQty, i, 1);
                transactionModel.setValueAt(
                    curr.formatRupiah(newSubTotal), i, 3
                );

                found = true;
                break;
            }
        }

        // ➕ If not found, add new item
        if (!found) {
            transactionItems.add(new TransactionItem(
                selectedProduct.getId(),
                qty,
                subTotal
            ));

            transactionModel.addRow(new Object[]{
                selectedProduct.getName(),
                qty,
                curr.formatRupiah(price),
                curr.formatRupiah(subTotal)
            });
        }

        // 🔢 Recalculate total price
        totalPrice = 0;
        for (TransactionItem item : transactionItems) {
            totalPrice += item.getSubTotalPrice();
        }

        txtTotalPrice.setText(curr.formatRupiah(totalPrice));
        this.resetForm();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        // TODO add your handling code here:
        saveTransaction();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnScanBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScanBarcodeActionPerformed
        // TODO add your handling code here:
        Window window = SwingUtilities.getWindowAncestor(this);

//        new BarcodeScannerDialog(window, barcode -> {
//            handleBarcode(barcode);
//        }).setVisible(true);
        
        new Thread(() -> {
            javax.swing.SwingUtilities.invokeLater(() -> {
                BarcodeScannerDialog dialog =
                    new BarcodeScannerDialog(window, barcode -> {
                        handleBarcode(barcode);
                    });
                dialog.setVisible(true);
            });
        }).start();
    }//GEN-LAST:event_btnScanBarcodeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnScanBarcode;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<String> selectProduct;
    private javax.swing.JTable tableTransaction;
    private javax.swing.JFormattedTextField textPay;
    private javax.swing.JFormattedTextField textQty;
    private javax.swing.JLabel txtReturn;
    private javax.swing.JLabel txtTotalPrice;
    // End of variables declaration//GEN-END:variables
}

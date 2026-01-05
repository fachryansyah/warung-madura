/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.warung.utils;

import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author fahri
 */
public class Currency {
    public String formatRupiah(int num) {
        Locale indo = new Locale("id", "ID");
        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(indo);
        rupiahFormat.setMaximumFractionDigits(0);
        rupiahFormat.setMinimumFractionDigits(0);
        return rupiahFormat.format(num);
    }
    
    public NumberFormatter createRupiahFormatter() {
        NumberFormat format = NumberFormat.getNumberInstance(
            new Locale("id", "ID")
        );

        format.setGroupingUsed(true); // 1.000.000
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);

        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Long.class);
        formatter.setAllowsInvalid(false); // block letters
        formatter.setCommitsOnValidEdit(true); // reactive

        return formatter;
    }
    
    public int parseRupiah(String value) {
        if (value == null || value.isEmpty()) return 0;

        return Integer.parseInt(
            value.replace(".", "").replace(",", "")
        );
    }


}

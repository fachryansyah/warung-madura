/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.warung;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.Window;
import javax.swing.SwingUtilities;

import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 *
 * @author fahri
 */
public class BarcodeScannerDialog extends JDialog implements Runnable {
    private Webcam webcam;
    private boolean running = true;
    private BarcodeListener listener;

    public interface BarcodeListener {
        void onScanned(String barcode);
    }
    
    static {
        System.setProperty("webcam.driver", "default");
    }
    
    public BarcodeScannerDialog(Window parent, BarcodeListener listener) {
        super(parent, "Scan Barcode", ModalityType.APPLICATION_MODAL);
        this.listener = listener;

        webcam = Webcam.getDefault();
        webcam.open();

        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        add(panel);

        setSize(400, 300);
        setLocationRelativeTo(parent);

        new Thread(this).start();
    }

    @Override
    public void run() {
        while (running) {
            try {
                BufferedImage image = webcam.getImage();
                if (image == null) continue;

                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                Result result = new MultiFormatReader().decode(bitmap);

                if (result != null) {
                    running = false;
                    webcam.close();
                    dispose();

                    listener.onScanned(result.getText());
                    break;
                }

            } catch (NotFoundException e) {
                // ignore (no barcode detected)
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
        }
    }
}

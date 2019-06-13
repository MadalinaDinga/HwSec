/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terminal;

import java.io.File;
import java.io.RandomAccessFile;
import java.security.KeyFactory;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.smartcardio.CardChannel;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import static terminal.Tester.readFile;

/**
 *
 * @author tom
 */
public class PoSTerminalGUI extends javax.swing.JFrame {

    State state = null;
    String amount;
    String pin;
    int amountNumber;
    private RSAPrivateKey terminalPrivateKey;
    private RSAPublicKey terminalPublicKey;
    private RSAPublicKey masterVerifyKey;
    private byte[] terminalKeyCertificate;
    private RSAPublicKey cardVerifyKey;
    private CardChannel applet;
    private RSAPublicKey cardEncryptionKey;
    private PaymentProtocol pp;

    void wrongPin() {
        setState(State.RETRY_PIN);
    }

    void blockedCard() {
        screen.setText("Your card got blocked.\n");
        screen.append("Transaction is aborted.\n");
        setState(State.GOT_BLOCKED);
    }
    
    private enum State {
        NONE,
        ENTERING_AMOUNT,
        ENTERING_PIN,
        GOT_BLOCKED,
        RETRY_PIN,
        DONE
    }
    
    private void setState(State s) {
        state = s;
        switch (s) {
            case NONE:
                screen.setText("Aborted!\n");
                break;
            case ENTERING_AMOUNT:
                amount = "";
                screen.setText("");
                screen.append("Enter the amount to be paid.\n");
            break;
            case ENTERING_PIN:
                pin = "";
                amountNumber = Integer.parseInt(amount);
                screen.append("\nEnter your pin:\n->");
            break;
            case RETRY_PIN:
                pin = "";
                String[] lines = screen.getText().split("\n");
                screen.setText("");
                screen.append(lines[0] + "\n");
                screen.append(lines[1] + "\n");
                screen.append("Wrong pin, please enter again.\n");
                screen.append("-> ");
                break;
        }
    }
    
    private void setLastLine(String newText) {
        String [] lines = screen.getText().split("\n");
        lines[screen.getLineCount() - 1] = newText;
        screen.setText(String.join("\n", lines));
    }
    
    private void numberEntered(String number) {
        switch (state) {
            case ENTERING_AMOUNT:
                amount += number;
                if (amount.length() > 1) {
                    setLastLine(amount);
                } else {
                    screen.append(amount);
                }
                break;
            case RETRY_PIN:
            case ENTERING_PIN:
                pin += number;
                setLastLine("-> " + String.join("", java.util.Collections.nCopies(pin.length(), "*")));
                break;                
        }
    }
    
    /**
     * Creates new form PoSTerminalGUI
     */
    public PoSTerminalGUI() {
        loadKeys();
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        screen = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton1.setText("1");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("2");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("3");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("4");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("5");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("6");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setText("7");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("8");
        jButton8.setActionCommand("jButton8");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setText("9");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setText("0");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton11.setText("accept");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jButton12.setText("correct");
        jButton12.setToolTipText("");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jButton13.setText("abort");
        jButton13.setToolTipText("");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jButton14.setText("new payment");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        screen.setColumns(20);
        screen.setRows(5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jButton7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jButton8)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jButton9))
                                .addComponent(jButton10))
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jButton14)
                                .addComponent(jButton13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jButton1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton3)
                            .addGap(18, 18, 18)
                            .addComponent(jButton11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jButton4)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton6)
                            .addGap(18, 18, 18)
                            .addComponent(jButton12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(screen, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(screen, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton11))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(jButton5)
                    .addComponent(jButton6)
                    .addComponent(jButton12))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton7)
                    .addComponent(jButton8)
                    .addComponent(jButton9)
                    .addComponent(jButton13))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton10)
                    .addComponent(jButton14))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        numberEntered("5");
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        setState(State.ENTERING_AMOUNT);
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        
        switch (state) {
            case ENTERING_AMOUNT:
                try {
                    AuthenticationProtocol ap = new AuthenticationProtocol(
                            terminalPublicKey, terminalPrivateKey,
                            masterVerifyKey, terminalKeyCertificate);
                    
                    applet = Utils.get();
                    
                    if (ap.run(applet)) {
                        System.out.println("Authenticated");
                        cardVerifyKey = ap.cardVerifyKey;
                        cardEncryptionKey = ap.cardEncryptionKey;
                        setState(state.ENTERING_PIN);
                    } else {
                        System.err.println("Card not recognized.");
                        setState(State.ENTERING_AMOUNT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Failed to connnect to card.");
                }
                break;
            case ENTERING_PIN:
                // Send pin to terminal.
                try {
                    amountNumber = Integer.parseInt(amount);
                    pp = new PaymentProtocol(this, amountNumber, terminalPublicKey, terminalPrivateKey, cardVerifyKey, cardEncryptionKey);
                    if (pp.run(applet)) {
                        paymentSucces();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Failed to connect to card");
                }
                break;
            case RETRY_PIN:
                try {
                    if (pp.run(applet)) {
                        paymentSucces();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }//GEN-LAST:event_jButton11ActionPerformed

    private void paymentSucces() {
        screen.setText("Payment successfull!");
        setState(State.DONE);
    }
    
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        numberEntered("1");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        numberEntered("2");
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        numberEntered("3");
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        numberEntered("4");
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        numberEntered("6");
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        numberEntered("7");
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        numberEntered("8");
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        numberEntered("9");
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        numberEntered("0");
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        switch (state) {
            case ENTERING_AMOUNT:
                amount = "";
                setLastLine(amount);
                break;
            case ENTERING_PIN:
                pin = "";
                setLastLine("-> " + pin);
                break;
        }
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        setState(State.NONE);
    }//GEN-LAST:event_jButton13ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PoSTerminalGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PoSTerminalGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PoSTerminalGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PoSTerminalGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PoSTerminalGUI().setVisible(true);
            }
        });
    }
    
    private void loadKeys() {
        try {
            File f = new File("pos-key-pub");
            if (f.exists() && !f.isDirectory()) {
                KeyFactory factory = KeyFactory.getInstance("RSA");
                // Get private key from file
                byte[] data = readFile("pos-key-priv");
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(data);
                terminalPrivateKey = (RSAPrivateKey) factory.generatePrivate(spec);
                System.out.println("Loaded private key.");
                // Get public key from file
                data = readFile("pos-key-pub");
                X509EncodedKeySpec specPub = new X509EncodedKeySpec(data);
                terminalPublicKey = (RSAPublicKey) factory.generatePublic(specPub);
                System.out.println("Loaded public key.");
                // Get master verify key
                data = readFile("master-key-pub");
                specPub = new X509EncodedKeySpec(data);
                masterVerifyKey = (RSAPublicKey) factory.generatePublic(specPub);
                
                
                terminalKeyCertificate = readFile("pos-certificate");
                
            } else {
                System.err.println("No keys available for pos terminal.");
            }
        } catch (Exception ioe) {
            System.err.println("Failed to load keys.");
            ioe.printStackTrace();
            System.exit(1);
        }
    }

    protected void insufficientBalance() {
        screen.setText("Insufficient balance on card!");
        state = null;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JTextArea screen;
    // End of variables declaration//GEN-END:variables
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terminal;

import common.Constants;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.Signature;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 *
 * @author tom
 */
public class PaymentProtocol extends Protocol {
    
    private final PoSTerminalGUI gui;
    private final RSAPrivateKey terminalPrivateKey;
    private final RSAPublicKey terminalPublicKey;
    private final RSAPublicKey cardVerifyKey;
    private final int amount;
    public byte[] proofOfPayment;
    
    public PaymentProtocol(PoSTerminalGUI gui,
            int amount,
            RSAPublicKey terminalPubKey,
            RSAPrivateKey terminalPrivateKey,
            RSAPublicKey cardVerifyKey) {
        this.gui = gui;
        this.amount = amount;
        this.terminalPublicKey = terminalPubKey;
        this.terminalPrivateKey = terminalPrivateKey;
        this.cardVerifyKey = cardVerifyKey;
    }
    
    /**
     * Runs the Payment protocol and sets the field proofOfPayment upon success.
     * @return True if successfully performed payment, false otherwise.
     */
    public boolean run(CardChannel applet) {
        ResponseAPDU rapdu;
        
        try {
            // Signal start of the protocol
            rapdu = sendCommand(applet, startPayment(), 0x9000, "Start authentication message returned SW: ");
        
            
            // Send the amount
            ByteBuffer buff = ByteBuffer.allocate(2);
            buff.putShort((short) amount);
            byte[] amountBytes = buff.array();
            
            System.out.println(Constants.SW_INSUFFICIENT_BALANCE);
            
            try {
                rapdu = sendCommand(applet, amount(amountBytes), 0x9000, "Sending over the amount resulted in SW: ");
            } catch (CardException e) {
                // Check if SW is in the message.
                if ( e.getMessage().contains("" + Constants.SW_INSUFFICIENT_BALANCE) ) {
                    System.err.println("Insufficient balance");
                    gui.insufficientBalance();
                    return false;
                } else {
                    throw e;
                }
            }
            byte[] cardNonce = rapdu.getData();
        
            // Send nonce to the card
            SecureRandom random = new SecureRandom();
            byte[] nonce = new byte[Constants.CHALLENGE_LENGTH];  
            
            rapdu = sendCommand(applet, nonce(nonce), 0x9000, "Sending over the nonce resulted in SW: ");
            byte[] signature = rapdu.getData();
            
            // Verify that both the card and terminal have observed the samve values
            if (!verifyCardSignature(amountBytes, nonce, cardNonce, signature)) {
                System.err.println("Signature not valid.");
                return false;
            }
            
            byte[] signedValues = signValues(amountBytes, nonce, cardNonce);
            rapdu = sendCommand(applet, signature(signedValues), 0x9000, "Sending over signature of values resulted in SW: ");
            byte[] proofOfPayment = rapdu.getData();
            
            // Verify received payment.
            if (!verifyPayment(amountBytes, nonce, cardNonce, proofOfPayment)) {
                System.err.println("Transaction invalid");
                return false;
            }
            
            this.proofOfPayment = proofOfPayment;
            
            System.out.println("Valid transaction.");
            
        } catch (CardException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private CommandAPDU startPayment() {
        return new CommandAPDU(0, Constants.START_PAYMENT_PROTOCOL, 0, 0, null, 0);
    }
    
    private CommandAPDU amount(byte[] amount) {
        return new CommandAPDU(0, 0, 0, 0, amount, 0);
    }
    
    private CommandAPDU nonce(byte[] nonce) {
        return new CommandAPDU(0, 0, 0, 0, nonce, 0);
    }
    
    private CommandAPDU signature(byte[] signature) {
        return new CommandAPDU(0, 0, 0, 0, signature, 0);
    }
    
    private boolean verifyPayment(byte[] amount, byte[] tNonce, byte[] cNonce, byte[] signature) {
        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(cardVerifyKey);
            
            sig.update(amount);
            sig.update(tNonce);
            sig.update(cNonce);
            sig.update(Utils.unsignedByteFromBigInt(terminalPublicKey.getModulus()) );
            sig.update(Utils.unsignedByteFromBigInt(terminalPublicKey.getPublicExponent()) );
            
            return sig.verify(signature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean verifyCardSignature(byte[] amount, byte[] nonce, byte[] counter, byte[] signature) {
        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(cardVerifyKey);

            sig.update(amount);
            sig.update(nonce);
            sig.update(counter);

            if (sig.verify(signature)) {
                System.out.println("Signature is valid");
                return true;
            } else {
                System.err.println("Signature on nonces and amount is invalid");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private byte[] signValues(byte[] amount, byte[] nonce, byte[] counter) {
        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(terminalPrivateKey);

            sig.update(amount);
            sig.update(nonce);
            sig.update(counter);

            return sig.sign(); 
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}

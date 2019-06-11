/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terminal;

import java.math.BigInteger;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import common.Constants;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.RSAPublicKeySpec;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author tom
 */
public class AuthenticationProtocol {

    private final byte[] terminalKeyCertificate;
    private final RSAPublicKey masterVerifyKey;
    private final RSAPrivateKey terminalPrivateKey;
    private final RSAPublicKey terminalPublicKey;
    private RSAPublicKey cardVerifyKey;
 
    public AuthenticationProtocol(
            RSAPublicKey terminalPubKey,
            RSAPrivateKey terminalPrivateKey,
            RSAPublicKey masterVerifyKey,
            byte[] terminalKeyCertificate) {
        this.terminalPublicKey = terminalPubKey;
        this.terminalPrivateKey = terminalPrivateKey;
        this.masterVerifyKey = masterVerifyKey;
        this.terminalKeyCertificate = terminalKeyCertificate;
    }
    
    /**
     * Runs the authentication protocol and sets the fields cardVerifyKey, cardEncryptionKey
     * @return True if successfully authenticated with card, false otherwise.
     */
    public boolean run(CardChannel applet) {
        ResponseAPDU rapdu;
        byte[] cardModulus, cardExponent, cardCertificate;
        
        try {
            // Indicate start of the protocol to the card
            rapdu = sendCommand(applet, startAuthenticationProtocol(), 0x9000, "Start authentication message returned SW: ");
            
            // Exchange moduli with the card
            rapdu = sendCommand(applet, modulus(), 0x9000, "Exchanging the modulus resulted in SW: ");
            cardModulus = rapdu.getData();
            
            // Exchange exponents with the card
            rapdu = sendCommand(applet, exponent(), 0x9000, "Exchanging the exponent resulted in SW: ");
            cardExponent = rapdu.getData();
            
            // Exchange certificates with the card
            rapdu = sendCommand(applet, certificate(), 0x9000, "Exchanging the certificate resulted in SW: ");
            cardCertificate = rapdu.getData();
            
            if (!verifyKeyCertificate(cardModulus, cardExponent, cardCertificate, masterVerifyKey)) {
                System.out.println("Signature of card is invalid.");
                return false;
            }
            
            cardVerifyKey = buildCardKeys(cardModulus, cardExponent);
            
            // Challenge the card
            SecureRandom random = new SecureRandom();
            byte[] nonce = new byte[Constants.CHALLENGE_LENGTH];            
            
            rapdu = sendCommand(applet, challenge(nonce), 0x9000, "Sending challenge to card resulted in SW: ");
            
            if (!verifyKeyCertificate(Constants.CHALLENGE_TAG, nonce, rapdu.getData(), cardVerifyKey)) {
                System.err.println("Card failed to repond to the challenge");
                return false;
            }
            
            // Get challenged by the card
            rapdu = sendCommand(applet, readyForChallenge(), 0x9000, "Requesting challenge from card resulted in SW: ");
            byte[] challenge = rapdu.getData();
            
            if (isWellFormattedChallenge(challenge)) {
                System.err.println("Received challenge is illformated.");
                return false;
            }
            
            byte[] response = respondToChallenge(challenge, terminalPrivateKey);
            
            // Respond to the challenge received by the card
            rapdu = sendCommand(applet, respondToChallenge(response), 0x9000, "Response to challenge of card resulted in SW: ");
        
        
        } catch (CardException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private CommandAPDU startAuthenticationProtocol() {
        return new CommandAPDU(0, Constants.START_AUTHENTICATION_PROTOCOL, 0, 0, null, 0);
    }
    
    private CommandAPDU modulus() {
        return new CommandAPDU(0, 0, 0, 0, Utils.unsignedByteFromBigInt(terminalPublicKey.getModulus()), 0);
    }
    
    private CommandAPDU exponent() {
        return new CommandAPDU(0, 0, 0, 0, Utils.unsignedByteFromBigInt(terminalPublicKey.getPublicExponent()), 0);
    }
    
    private CommandAPDU certificate() {
        return new CommandAPDU(0, 0, 0, 0, terminalKeyCertificate, 0);
    }
    
    private CommandAPDU challenge(byte[] nonce) {
        byte[] msg = ArrayUtils.addAll(Constants.CHALLENGE_TAG, nonce);
        return new CommandAPDU(0, 0, 0, 0, msg, 0);
    }
    
    private CommandAPDU readyForChallenge() {
        return new CommandAPDU(0, 0, 0, 0, null, 0);
    }
    
    private CommandAPDU respondToChallenge(byte[] response) {
        return new CommandAPDU(0, 0, 0, 0, response, 0);
    }
    
    private ResponseAPDU sendCommand(CardChannel applet, CommandAPDU capdu, int expectedSW, String reason) throws CardException {
        ResponseAPDU rapdu = applet.transmit(capdu);
        if (rapdu.getSW() != expectedSW) 
                throw new CardException(reason + rapdu.getSW());
        return rapdu;
    }

    private boolean verifyKeyCertificate(byte[] modulus, byte[] exponent, byte[] certificate, RSAPublicKey verificationKey) {
        try{
            Signature verifier = Signature.getInstance("SHA1withRSA");
        
            verifier.initVerify(verificationKey);
            verifier.update(modulus);
            verifier.update(exponent);
            
            return verifier.verify(certificate);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private RSAPublicKey buildCardKeys(byte[] modulus, byte[] exponent) throws CardException {
        try {
            RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(1, modulus), new BigInteger(1, exponent));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pk = kf.generatePublic(spec);
            return (RSAPublicKey) pk;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CardException("Key received from the card is not valid");
        }
    }
    
    private boolean isWellFormattedChallenge(byte[] challenge) {
        return ByteBuffer.wrap(Constants.CHALLENGE_TAG, 0, 3).equals(ByteBuffer.wrap(challenge,1,3));
    }
    
    private byte[] respondToChallenge(byte[] challenge, RSAPrivateKey signingKey) throws CardException {
        try {
            Signature signer = Signature.getInstance("SHA1withRSA");
            signer.initSign(signingKey);
            signer.update(challenge);
            return signer.sign();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CardException("Failed to respond to challenge received by the card");
        }
    }
}

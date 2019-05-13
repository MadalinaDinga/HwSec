package applet;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;


public class EchoApplet extends Applet implements ISO7816 {
    
    /** Instruction code for echoing */
    private static final byte INS_ECHO = (byte) 0x02;

    /** Buffer in RAM */
    private static byte[] tmp;

    public EchoApplet() {
        tmp = JCSystem.makeTransientByteArray((short)256,JCSystem.CLEAR_ON_RESET);
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        (new EchoApplet()).register(bArray, (short)(bOffset + 1), bArray[bOffset]);
    }

    public void process(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte instructionByte = buffer[OFFSET_INS];
        
        /* Ignore the APDU that selects this applet... */
        if (selectingApplet()) {
            return;
        }

        switch(instructionByte) {
            case INS_ECHO:
                short len = readBuffer(apdu, tmp, (short) 0, (short) 0);
                Util.arrayCopy(tmp, (byte) 0, buffer, ISO7816.OFFSET_CDATA, len);
                apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, len);
                break;

            default:
                ISOException.throwIt(SW_INS_NOT_SUPPORTED);
        }

    }

     /**
    * Copies <code>length</code> bytes of data (starting at
    * <code>OFFSET_CDATA</code>) from <code>apdu</code> to <code>dest</code>
    * (starting at <code>offset</code>).
    *
    * This method will set <code>apdu</code> to incoming.
    *
    * @param apdu the APDU.
    * @param dest destination byte array.
    * @param offset offset into the destination byte array.
    * @param length number of bytes to copy.
    */
   private short readBuffer(APDU apdu, byte[] dest, short offset,
   short length) {
        // Buffer of APDU
        byte[] buffer = apdu.getBuffer();
        // incoming apdu command length
        short bytesLeft = (short) (buffer[OFFSET_LC] & 0x00FF);
        // Read number of bytes 
        short readCount = apdu.setIncomingAndReceive();
        short len = readCount;
        while ( bytesLeft > 0){
            // process bytes in buffer[5] to buffer[readCount+4];
            bytesLeft -= readCount;
            readCount = apdu.receiveBytes ( OFFSET_CDATA );
            Util.arrayCopy(buffer, OFFSET_CDATA, dest, offset,readCount);
            }
        return len;
    }

}
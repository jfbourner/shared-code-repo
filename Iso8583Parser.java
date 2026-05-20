public class Iso8583Parser {

    private final MessageFactory<IsoMessage> messageFactory;

    public Iso8583Parser() throws IOException {
        // Load field definitions from classpath (src/main/resources/iso8583.xml)
        messageFactory = ConfigParser.createFromClasspathConfig("iso8583.xml");

        // If your messages use binary bitmap (most FPG/banking scenarios do)
        messageFactory.setUseBinaryBitmap(true);

        // If your messages use a binary message type indicator
        // messageFactory.setUseBinaryMessages(true);

        // Charset for ALPHA/LLVAR string fields - important if you're dealing
        // with Windows-1252 encoded messages (as you've seen in FPG)
        messageFactory.setCharacterEncoding("ISO-8859-1");
    }

    public void parse(byte[] rawMessage) throws ParseException, IOException {

        // Second arg = ISO header length (e.g. 12 for "ISO015000050", 0 if none)
        IsoMessage msg = messageFactory.parseMessage(rawMessage, 12);

        System.out.printf("MTI: %04X%n", msg.getType());
        System.out.println("ISO Header: " + msg.getIsoHeader());

        // --- Field access by number ---

        // getField() returns null if the field wasn't in the bitmap
        IsoValue<String> pan = msg.getField(2);
        if (pan != null) {
            System.out.println("Field 2  (PAN):              " + pan.getValue());
        }

        IsoValue<String> processingCode = msg.getField(3);
        if (processingCode != null) {
            System.out.println("Field 3  (Processing Code):  " + processingCode.getValue());
        }

        // getObjectValue() is a convenience shortcut that skips the IsoValue wrapper
        String amount = msg.getObjectValue(4);
        System.out.println("Field 4  (Amount):           " + amount);

        String stan = msg.getObjectValue(11);
        System.out.println("Field 11 (STAN):              " + stan);

        // DATE10 fields come back as java.util.Date
        java.util.Date txDate = msg.getObjectValue(7);
        System.out.println("Field 7  (Tx Date/Time):     " + txDate);

        // BINARY fields come back as byte[]
        byte[] pinData = msg.getObjectValue(52);
        if (pinData != null) {
            System.out.println("Field 52 (PIN Data):         " + bytesToHex(pinData));
        }

        // Check field type at runtime if needed
        IsoValue<?> rrn = msg.getField(37);
        if (rrn != null) {
            System.out.printf("Field 37 (RRN): type=%s, length=%d, value=%s%n",
                    rrn.getType(), rrn.getLength(), rrn.getValue());
        }

        // Iterate all present fields (only those set in the bitmap)
        System.out.println("\n--- All present fields ---");
        for (int i = 2; i <= 128; i++) {
            IsoValue<?> field = msg.getField(i);
            if (field != null) {
                System.out.printf("  Field %3d | %-10s | len=%-4d | %s%n",
                        i, field.getType(), field.getLength(), field.getValue());
            }
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }
}
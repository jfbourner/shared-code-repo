package com.example.iso;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOStringFieldPackager;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * UTF-8 Fixed Length String Field Packager
 * Pads with spaces on the right, trims spaces when unpacking
 */
public class IFA_FIXED_UTF8 extends ISOStringFieldPackager {

    public IFA_FIXED_UTF8() {
        super();
    }

    public IFA_FIXED_UTF8(int len, String description) {
        super(len, description);
    }

    @Override
    public byte[] pack(ISOComponent c) throws ISOException {
        String data = (String) c.getValue();
        if (data == null) {
            data = "";
        }

        // Convert to UTF-8 bytes
        byte[] utf8Data = data.getBytes(StandardCharsets.UTF_8);

        // Check if data fits in specified length
        if (utf8Data.length > getLength()) {
            throw new ISOException("UTF-8 data length " + utf8Data.length +
                    " exceeds field length " + getLength());
        }

        // Create fixed-length array and copy data
        byte[] packed = new byte[getLength()];
        Arrays.fill(packed, (byte) 0x20); // Fill with spaces
        System.arraycopy(utf8Data, 0, packed, 0, utf8Data.length);

        return packed;
    }

    @Override
    public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
        if (b.length - offset < getLength()) {
            throw new ISOException("Not enough data. Need " + getLength() +
                    " bytes, available " + (b.length - offset));
        }

        // Extract fixed-length data
        byte[] fieldData = new byte[getLength()];
        System.arraycopy(b, offset, fieldData, 0, getLength());

        // Convert from UTF-8 and trim trailing spaces
        String value = new String(fieldData, StandardCharsets.UTF_8).trim();
        c.setValue(value);

        return getLength();
    }

    @Override
    public void unpack(ISOComponent c, java.io.InputStream in) throws ISOException {
        try {
            byte[] fieldData = new byte[getLength()];
            if (in.read(fieldData) != getLength()) {
                throw new ISOException("Unable to read " + getLength() + " bytes");
            }

            String value = new String(fieldData, StandardCharsets.UTF_8).trim();
            c.setValue(value);

        } catch (Exception e) {
            throw new ISOException("Error unpacking UTF-8 fixed field: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "UTF-8 Fixed Length: " + getLength() + " byte UTF-8 encoded string";
    }
}
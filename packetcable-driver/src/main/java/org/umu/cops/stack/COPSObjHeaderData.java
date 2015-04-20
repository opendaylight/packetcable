package org.umu.cops.stack;

/**
 * Class designed for simply containing the COPSHeader and the total message's byte count.
 *
 * No need to test as this class will be tested implicitly via other tests and this does not contain or need
 * any domain specific logic
 */
public class COPSObjHeaderData {

    /**
     * The actual header to be injected into the appropriate COPSMsg object
     */
    final public COPSObjHeader header;

    /**
     * The total number of bytes contained within the inbound message being parsed.
     */
    final public int msgByteCount;

    /**
     * Constructor
     * @param hdr - the COPS message header
     * @param numBytes - the total number of bytes contained within the message envelope
     */
    public COPSObjHeaderData(final COPSObjHeader hdr, final int numBytes) {
        this.header = hdr;
        this.msgByteCount = numBytes;
    }
}

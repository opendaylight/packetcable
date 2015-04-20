package org.umu.cops.stack;

/**
 * Class designed for simply containing the COPSHeader and the total message's byte count.
 *
 * No need to test as this class will be tested implicitly via other tests and this does not contain or need
 * any domain specific logic
 */
class COPSHeaderData {

    /**
     * The actual header to be injected into the appropriate COPSMsg object
     */
    final COPSHeader header;

    /**
     * The total number of bytes contained within the inbound message being parsed.
     */
    final int msgByteCount;

    /**
     * Constructor
     * @param hdr - the COPS message header
     * @param numBytes - the total number of bytes contained within the message envelope
     */
    public COPSHeaderData(final COPSHeader hdr, final int numBytes) {
        this.header = hdr;
        this.msgByteCount = numBytes;
    }
}

/**
 @header@
 */

package org.pcmm.base;

import java.io.IOException;
import java.net.Socket;

import org.umu.cops.stack.COPSData;

/**
 * Base interface for all PCMM objects, it define the {@code S-Type},
 * {@code S-Num} and the data length
 * 
 */
public interface IPCMMBaseObject {

    /**
     * sets the S-Type
     *
     * @param stype
     */
    void setSType(byte stype);

    /**
     *
     * @return S-Type
     */
    byte getSType();

    /**
     * sets the S-Num
     *
     * @param snum
     *            S-Num
     */
    void setSNum(byte snum);

    /**
     * gets the S-Num
     *
     * @return S-Num
     */
    byte getSNum();

    /**
     * sets the length;
     *
     * @param len
     */
    void setLength(short len);

    /**
     * gets the length;
     *
     * @return length
     */
    short getLength();

    /**
     * sets the COPS data
     *
     * @param data
     *            COPS data
     */
    void setData(COPSData data);

    /**
     * gets the COPS data
     *
     * @return COPS data
     */
    COPSData getData();

    void writeData(Socket id) throws IOException;

	byte[] getAsBinaryArray();

}

/**
 @header@
 */
package org.pcmm.messages;

import java.util.Properties;

import org.umu.cops.stack.COPSMsg;

/**
 * 
 * Factory used to create {@code COPSMsg} based on message type input and a list
 * of properties.
 * 
 */
public interface IMessageFactory {

    /**
     * creates a new message with the specified message type.
     *
     * @param messageType
     *            message type
     * @return new message.
     */
    COPSMsg create(byte messageType);

    /**
     * creates a new message with the specified message type and content
     *
     * @param messageType
     *            message type
     * @param properties
     *            message content.
     * @return new message.
     */
    COPSMsg create(byte messageType, Properties properties);
}

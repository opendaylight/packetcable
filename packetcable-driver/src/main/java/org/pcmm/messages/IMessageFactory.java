/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.messages;

import org.umu.cops.stack.COPSHeader.OPCode;
import org.umu.cops.stack.COPSMsg;

import java.util.Properties;

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
    COPSMsg create(OPCode messageType);

    /**
     * creates a new message with the specified message type and content
     *
     * @param messageType
     *            message type
     * @param properties
     *            message content.
     * @return new message.
     */
    COPSMsg create(OPCode messageType, Properties properties);
}

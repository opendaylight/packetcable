/**
 
 * Copyright (c) 2014 CableLabs.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html

 */


package org.pcmm.state;

/**
 * <p>
 * Each stateful server should implement this interface, to be able to save and
 * retrieve clients' state
 * </p>
 *
 *
 *
 */
public interface IStateful {

    /**
     * records the collected client state
     */
    void recordState();

    /**
     *
     * @return recorded state.
     */
    IState getRecoredState();

}

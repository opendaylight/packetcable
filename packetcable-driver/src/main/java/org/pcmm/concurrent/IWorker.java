/*
 * Copyright (c) 2014 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.concurrent;

import java.util.concurrent.Callable;
/**
 *
 */
public interface IWorker extends Runnable {

	/**
	 * defines the task to be performed by this worker
	 *
	 * @param c
	 */
	void task(Callable<?> c);

	/**
	 * defines wait time before start working on the task
	 *
	 * @param t
	 */
	void shouldWait(int t);

	/**
	 * ends the current working task.
	 */
	void done();

}

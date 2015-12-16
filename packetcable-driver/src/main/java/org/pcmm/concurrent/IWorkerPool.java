/*
 * Copyright (c) 2014 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.concurrent;

import org.pcmm.base.IAdapter;

/**
 *
 */
public interface IWorkerPool extends IAdapter<IWorker> {
	// handles 32 workers
	static int DEFAULT_MAX_WORKERS = 32;

	/**
	 * schedules a worker for beginning its task after t milliseconds.
	 *
	 * @param worker
	 *            : the worker
	 * @param t
	 *            : time to wait
	 * @return the id of the worker (PID) to be used for killing the worker if
	 *         needed
	 */
	int schedule(IWorker worker, int t);

	/**
	 * schedules a worker for immediate execution.
	 *
	 * @param worker
	 *            : the worker
	 * @return the id of the worker (PID) to be used for killing the worker if
	 *         needed
	 */
	int schedule(IWorker worker);

	/**
	 * kills the worker with the specified pid
	 *
	 * @param pid
	 */
	void sendKillSignal(int pid);

	/**
	 * sends a terminate signal for all active workers and recycles the pool.
	 */
	void killAll();

	/**
	 * cleans up the pool from finished tasks
	 */
	void recycle();

}

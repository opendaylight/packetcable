package org.pcmm.concurrent.impl;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.pcmm.concurrent.IWorker;
import org.pcmm.concurrent.IWorkerPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pool to manage PCMM workers
 */
public class WorkerPool implements IWorkerPool {

	/**
	 * 
	 */
	private Map<Integer, WeakReference<IWorker>> workersMap;

	private Logger logger = LoggerFactory.getLogger(IWorkerPool.class);
	private ExecutorService executor;

	public WorkerPool() {
		this(DEFAULT_MAX_WORKERS);
	}

	public WorkerPool(int size) {
		logger.info("Pool size :" + size);
		workersMap = new HashMap<Integer, WeakReference<IWorker>>();
		executor = Executors.newFixedThreadPool(size);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.threading.IWorkerPool#schedule(org.pcmm.threading.IWorker,
	 * int)
	 */
	@Override
	public int schedule(IWorker worker, int t) {
		if (worker == null)
			return -1;
		logger.debug("woker[" + worker + "] added, starts in " + t + " ms");
		WeakReference<IWorker> workerRef = new WeakReference<IWorker>(worker);
		int ref = workerRef.hashCode();
		workersMap.put(ref, workerRef);
		worker.shouldWait(t);
		executor.execute(worker);
		return ref;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.pcmm.concurrent.IWorkerPool#schedule(org.pcmm.concurrent.IWorker)
	 */
	@Override
	public int schedule(IWorker worker) {
		return schedule(worker, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.concurrent.IWorkerPool#sendKillSignal(int)
	 */
	@Override
	public void sendKillSignal(int pid) {
		if (workersMap.size() > 0) {
			WeakReference<IWorker> weakRef = workersMap.get(pid);
			if (weakRef != null) {
				IWorker ref = weakRef.get();
				if (ref != null)
					ref.done();
				if (!weakRef.isEnqueued()) {
					weakRef.clear();
					weakRef.enqueue();
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.threading.IWorkerPool#killAll()
	 */
	@Override
	public void killAll() {
		for (WeakReference<IWorker> weakRef : workersMap.values()) {
			IWorker ref = weakRef.get();
			if (ref != null)
				ref.done();
			if (!weakRef.isEnqueued()) {
				weakRef.clear();
				weakRef.enqueue();
			}
		}
		recycle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.threading.IWorkerPool#recycle()
	 */
	@Override
	public void recycle() {
		for (Iterator<Integer> pids = workersMap.keySet().iterator(); pids.hasNext();) {
			WeakReference<IWorker> weakRef = workersMap.get(pids.next());
			IWorker ref = weakRef.get();
			if (ref == null) {
				if (!weakRef.isEnqueued()) {
					weakRef.clear();
					weakRef.enqueue();
				}
				workersMap.remove(weakRef);
			}
		}

	}

	@Override
	public Object adapt(Object object, Class<?> clazz) {
		if (clazz.isAssignableFrom(object.getClass()))
			return object;
		return null;
	}

	@Override
	public IWorker adapt(Object object) {
		IWorker worker = (IWorker) adapt(object, IWorker.class);
		if (worker == null) {
			if (object instanceof Callable)
				worker = new Worker((Callable<?>) object);
			else if (object instanceof Runnable) {
				final Runnable runner = (Runnable) object;
				worker = new Worker(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						((Runnable) runner).run();
						return null;
					}
				});
			}
		}
		return worker;
	}

}

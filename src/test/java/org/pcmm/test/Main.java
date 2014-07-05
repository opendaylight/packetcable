/**
 *
 */
package org.pcmm.test;

import org.pcmm.rcd.ICMTS;
import org.pcmm.rcd.IPCMMPolicyServer;
import org.pcmm.rcd.IPCMMPolicyServer.IPSCMTSClient;
import org.pcmm.rcd.impl.CMTS;
import org.pcmm.rcd.impl.PCMMPolicyServer;

/**
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ICMTS icmts = new CMTS();
		icmts.startServer();
		IPCMMPolicyServer ps = new PCMMPolicyServer();
		IPSCMTSClient client = ps.requestCMTSConnection("localhost");
		client.gateSet();
		// IWorkerPool pool = new WorkerPool(2);
		// IWorker worker = new Worker(new Callable<String>() {
		// @Override
		// public String call() throws Exception {
		// System.out
		// .println("Main.main(...).new Callable() {...}.call()");
		// return null;
		// }
		// });
		// IWorker worker2 = new Worker(new Callable<String>() {
		// @Override
		// public String call() throws Exception {
		// System.out
		// .println("|||||||Main.main(...).new Callable() {...}.call()||||||||||||");
		// return null;
		// }
		// });
		// pool.schedule(worker2, 2000);
		// pool.schedule(worker, 500);
		// pool.recycle();


	}
}

package org.umu.cops.stack;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Server used to test client/server socket communications.
 */
public class TestCOPSServer extends Thread {

    private final static Logger logger = LoggerFactory.getLogger(TestCOPSServer.class);
    private final ServerSocket serverSocket;
    final List<COPSMsg> copsMsgs = new ArrayList<>();

    public TestCOPSServer(final int port) throws IOException {
        serverSocket = new ServerSocket(port);
        Assert.assertTrue(serverSocket.isBound());
    }

    public void close() throws IOException {
        this.interrupt();
        serverSocket.close();
    }

    public void run() {
        try {
            final Socket socket = serverSocket.accept();
            final COPSMsg copsMsg = COPSTransceiver.receiveMsg(socket);
            logger.info("Read value = " + copsMsg);
            copsMsgs.add(copsMsg);
            socket.close();
        } catch (Exception e) {
            logger.error("Error processing message", e);
        }

    }
}

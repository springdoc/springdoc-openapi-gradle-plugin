package com.example.demo;

import java.net.ServerSocket;
import java.net.SocketTimeoutException;

/**
 * A process started by the application, like a dev server: it listens on a port until it is stopped, or for at most
 * two minutes without connections.
 */
public class ChildProcess {

	static final int PORT = 8085;

	private static final int IDLE_TIMEOUT_IN_MILLIS = 120_000;

	public static void main(String[] args) throws Exception {
		try (ServerSocket server = new ServerSocket(PORT)) {
			server.setSoTimeout(IDLE_TIMEOUT_IN_MILLIS);
			while (true) {
				server.accept().close();
			}
		}
		catch (SocketTimeoutException e) {
			// nobody stopped it
		}
	}

}

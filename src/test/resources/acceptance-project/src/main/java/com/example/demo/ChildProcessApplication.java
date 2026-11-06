package com.example.demo;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Paths;

/**
 * Starts a child process, waits until it listens, then starts the application.
 */
public class ChildProcessApplication {

	public static void main(String[] args) throws Exception {
		String java = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
		new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), ChildProcess.class.getName()).start();
		waitUntilListening(ChildProcess.PORT);
		DemoApplication.main(args);
	}

	private static void waitUntilListening(int port) throws InterruptedException {
		for (int attempt = 0; attempt < 100; attempt++) {
			try (Socket socket = new Socket()) {
				socket.connect(new InetSocketAddress("localhost", port), 100);
				return;
			}
			catch (Exception e) {
				Thread.sleep(100);
			}
		}
		throw new IllegalStateException("The child process does not listen on port " + port);
	}

}

package com.example.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Behaves like an application that is still starting: refuses connections for a few seconds (the port is not open
 * yet), then resets the first connections after reading the request, then serves a minimal OpenAPI document.
 */
public class ResetConnectionsApplication {

	private static final String API_DOCS = "{\"openapi\":\"3.0.1\",\"paths\":{\"/hello\":{}}}";

	public static void main(String[] args) throws Exception {
		Thread.sleep(3000);
		int resets = 3;
		try (ServerSocket server = new ServerSocket(8080)) {
			while (true) {
				try (Socket client = server.accept()) {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
					String line;
					while ((line = reader.readLine()) != null && !line.isEmpty()) {
						// skip the request headers
					}
					if (resets-- > 0) {
						client.setSoLinger(true, 0);
						continue;
					}
					byte[] body = API_DOCS.getBytes(StandardCharsets.UTF_8);
					OutputStream out = client.getOutputStream();
					out.write(("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: " + body.length
							+ "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
					out.write(body);
					out.flush();
				}
			}
		}
	}

}

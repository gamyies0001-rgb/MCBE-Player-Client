package net.unproprietary.mcbe_client.networking;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
 * @version 1.0.0
 * @apiNote A consumer-friendly way to manage servers
 * @category Networking, 
 */
public class Servers {
	private static Set<Server> added_servers = new HashSet<>();
	
	/**
	 * Adds a server to the current server list.
	 * 
	 * @param ip The IP address, or URL, of the server to add.
	 * @param port The port, usually 19132 for bedrock, of the server to add
	 * @return {@code true} if the server was added successfully, or {@code false} if an exception was thrown,
	 * @apiNote v1.0.0, Not finished
	 * @since 06-07-2026
	 *
	 */
	public static boolean addServer(String ip, int port) {
		try {
			Servers.added_servers.add(new Server(new URI(ip), port));
			return true;
		} catch (IOException | URISyntaxException e) {
			if (e instanceof URISyntaxException) {
				e.printStackTrace();
			}
			return false;
		}
	}
	
	/**
	 * Returns a {@code Set<Server>} with every server added in this instance.
	 * 
	 * @apiNote Note: For security reasons, this returns a copy of the internal set, not the set itself
	 * @return {@code Set<Server>} a copy of the internal database.
	 */
	
	public static Set<Server> getServers() {
		return new HashSet<>(added_servers);
	}
}

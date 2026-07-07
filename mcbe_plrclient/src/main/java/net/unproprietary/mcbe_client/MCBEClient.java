package net.unproprietary.mcbe_client;
import net.unproprietary.mcbe_client.networking.*;

public class MCBEClient {
	public static void main(String[] args) {
		Servers.addServer("buzz.cosmosmc.org", 19132);
		System.out.println(Servers.getServers().iterator().next().motd);
	}
}

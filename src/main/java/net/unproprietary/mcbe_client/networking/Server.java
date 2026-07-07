package net.unproprietary.mcbe_client.networking;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;

public class Server {
	// P2P config was going to be something but then I scrapped it.
	public static class P2PConfig {
		public boolean hostedByPlayer = false;
		public boolean hostedByXbox = false;
		public boolean hostedExternally = false;
		public boolean official = false;
		public boolean advertised = false;
		
		public String playerGTag = "";
		public String realmIcode = "";
	}
	
	public URI ip;
	public int port = 19132;
	//public final P2PConfig p2pconf;
	
	public String motd;
	public String serverName;
	public int playersOnline;
	public int maxPlayers;
	
	public Server(URI ip, int port) throws IOException {
		this.ip = ip;
		this.port = port;
		
		final byte ID_UNCONNECTED_PING = 0x01;
		final byte ID_UNCONNECTED_PONG = 0x1C;
		
		final byte[] OFFLINE_MAGIC = {
				0x00, (byte) 0xFF, (byte) 0xFF, 0x00,
				(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
				(byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD,
				0x12, 0x34, 0x56, 0x78
		};
		
		try {
			InetAddress address = InetAddress.getByName(ip.toString());
			ByteBuffer ping = ByteBuffer
					.allocate(1 + Long.BYTES + OFFLINE_MAGIC.length + Long.BYTES)
					.order(ByteOrder.BIG_ENDIAN);
			
			ping.put(ID_UNCONNECTED_PING);
			ping.putLong(System.currentTimeMillis());
			ping.put(OFFLINE_MAGIC);
			ping.putLong(new SecureRandom().nextLong());
			
			try (DatagramSocket socket = new DatagramSocket()) {
				socket.connect(new InetSocketAddress(ip.toString(), port));
				socket.setSoTimeout(3000);
				
				byte[] request = ping.array();
				socket.send(new DatagramPacket(request, request.length));
				
				byte[] reply = new byte[2048];
				DatagramPacket packet = new DatagramPacket(reply, reply.length);
				socket.receive(packet);
				
				if (packet.getLength() < 1 + Long.BYTES + Long.BYTES + OFFLINE_MAGIC.length) {
					throw new IOException("Failed to add server: Exception during requesting server information: Response Packet Length does not match expected length. (Expected: atleast " + (1 + Long.BYTES + Long.BYTES + OFFLINE_MAGIC.length) + " bytes; Received: " + packet.getLength() + " bytes)");
				}
				
				ByteBuffer response = ByteBuffer
						.wrap(packet.getData(), 0, packet.getLength())
						.order(ByteOrder.BIG_ENDIAN);
				
				if (response.get() != ID_UNCONNECTED_PONG) {
					throw new IOException("Failed to add server: Exception during requesting server information: Expected response type UNCONNECTED_PONG, received a response of a different type.");
				}
				
				response.getLong();
				long guid = response.getLong();
				
				for (byte expected : OFFLINE_MAGIC) {
					if (response.get() != expected) {
						throw new IOException("Failed to add server: Exception during requesting server information: Response Packet's OFFLINE_MAGIC does not match our OFFLINE_MAGIC.");
					}
				}
				
				int identifierLength = Short.toUnsignedInt(response.getShort());
				
				
				if (identifierLength > response.remaining()) {
					throw new IOException("Failed to add server: Exception during requesting server information: Invalid Bedrock Server identifier length.");
				}
				
				byte[] identifierBytes = new byte[identifierLength];
				response.get(identifierBytes);
				
				String identifier = new String(identifierBytes, StandardCharsets.UTF_8);
				String[] fields = identifier.split(";", -1);
				
				if (fields.length < 6 || !fields[0].equalsIgnoreCase("MCPE")) {
					throw new IOException("Failed to add server: Exception during requesting server information: ServerInfo section of Response does not start with Bedrock Server Identifier 'MCPE'");
				}
				
				this.motd = fields[1];
				this.serverName = fields.length > 7 ? fields[7] : fields[1]; // use MOTD as fallback;
				this.playersOnline = Integer.parseInt(fields[4]);
				this.maxPlayers = Integer.parseInt(fields[5]);
			}
		} catch (IOException e) {
			System.err.println("Failed to add server! Please check you have entered the correct settings, the server is running, UDP isn't blocked, or this is a Xbox Live server (which we do not have access to)");
			System.err.println("More information can be found below:");
			e.printStackTrace(System.err);
			
			throw e;
		}
	}
}

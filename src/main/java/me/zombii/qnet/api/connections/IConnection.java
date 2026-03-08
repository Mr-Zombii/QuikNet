package me.zombii.qnet.api.connections;

import me.zombii.qnet.api.ITCPClient;
import me.zombii.qnet.api.ITCPServer;
import me.zombii.qnet.api.packet.IPacket;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.api.Side;

import java.io.IOException;

/**
 * The API class that defines the basic layout for a client/server connection.
 *
 * @since 1.0.0
 * @author Mr-Zombii
 */
public interface IConnection {

    /**
     * Sends a packet using a protocol to the client/server.
     * @param packetProtocol the protocol the packet belongs to.
     * @param packet the packet to send.
     */
    void sendPacket(IPacketProtocol packetProtocol, IPacket packet) throws IOException;

    /**
     * Swaps the packet-protocol being used to read &amp; write packets in the server/client.
     * @param protocol the protocol to swap to.
     */
    void swap(IPacketProtocol protocol);

    /**
     * Gets the protocol currently used by the server/client to read &amp; write packets.
     * @return the used packet-protocol.
     */
    IPacketProtocol getProtocol();

    /**
     * Gets the side the connection belongs to.
     * @return the owning side.
     */
    Side getSide();

    /**
     * Disconnects the client (on server) or Disconnects from the server (on client).
     */
    void disconnect();

    /**
     * Checks if the connection is using the java.net backend implementation.
     * @return true or false if it's using the java.net backend or not.
     */
    boolean isUsingJavaNet();

    /**
     * Checks if the connection is using the Netty backend implementation.
     * @return true of false if it's using the Netty backend or not.
     */
    boolean isUsingNetty();

    /**
     * Gets the java.net backend interface if {@link IConnection#isUsingJavaNet()} was true.
     * @return the java.net backend interface.
     */
    IJavaNetConnection asJavaNetConnection();

    /**
     * Gets the Netty backend interface if {@link IConnection#isUsingNetty()} was true.
     * @return the Netty backend interface.
     */
    INettyConnection asNettyConnection();

    /**
     * Gets the owning server if {@link IConnection#getSide()} returns {@link Side#SERVER};
     * @return the owning server.
     */
    ITCPServer getServer();

    /**
     * Gets the owning client if {@link IConnection#getSide()} returns {@link Side#CLIENT};
     * @return the owning client.
     */
    ITCPClient getClient();

}

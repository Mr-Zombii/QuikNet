package me.zombii.qnet.api.connections;

import me.zombii.qnet.api.ITCPServer;
import me.zombii.qnet.api.packet.IPacket;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.api.Side;

import java.io.IOException;

public interface IConnection {

    void sendPacket(IPacketProtocol packetProtocol, IPacket packet) throws IOException;

    void broadcastPacketToOthers(IPacketProtocol packetProtocol, IPacket packet) throws IOException;

    void swap(IPacketProtocol protocol);
    IPacketProtocol getProtocol();

    Side getSide();
    void disconnect();

    boolean isUsingJavaNet();
    boolean isUsingNetty();

    ITCPServer getServer();

    IJavaNetConnection asJavaNetConnection();
    INettyConnection asNettyConnection();

}

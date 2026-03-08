package me.zombii.qnet.api;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.zombii.qnet.api.connections.IConnection;
import me.zombii.qnet.api.packet.IPacket;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.impl.javanet.tcp.JavaNetTCPServer;
import me.zombii.qnet.impl.netty.tcp.NettyTCPServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

public interface ITCPServer {

    static ITCPServer newNettyServer() {
        return new NettyTCPServer();
    }

    static ITCPServer newNettyServer(Consumer<IConnection> onConnectionAccepted, Consumer<IConnection> onConnectionClosed) {
        return new NettyTCPServer(onConnectionAccepted, onConnectionClosed);
    }

    static ITCPServer newJavaNetServer() throws IOException {
        return new JavaNetTCPServer();
    }

    static ITCPServer newJavaNetServer(Consumer<IConnection> onConnectionAccepted, Consumer<IConnection> onConnectionClosed) throws IOException {
        return new JavaNetTCPServer(onConnectionAccepted, onConnectionClosed);
    }

    void bind(IPacketProtocol protocol, String address);
    void bind(IPacketProtocol protocol, InetSocketAddress address);
    void stop();
    void disconnect(IConnection connection);

    void broadcastPacket(IPacketProtocol protocol, IPacket packet) throws IOException;
    void broadcastPacketToAllExcept(IConnection connection, IPacketProtocol protocol, IPacket packet) throws IOException;
    ObjectList<IConnection> getAllConnections();

    Consumer<IConnection> getOnConnectionAccepted();
    Consumer<IConnection> getOnConnectionClosed();

    boolean isRunning();
}

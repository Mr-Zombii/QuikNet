package me.zombii.qnet.api;

import me.zombii.qnet.api.connections.IConnection;
import me.zombii.qnet.api.packet.IPacket;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.impl.javanet.tcp.JavaNetTCPClient;
import me.zombii.qnet.impl.netty.tcp.NettyTCPClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

public interface ITCPClient extends IConnection {

    static ITCPClient newNettyClient() {
        return new NettyTCPClient();
    }

    static ITCPClient newNettyClient(Consumer<ITCPClient> onConnectionAccepted, Consumer<ITCPClient> onConnectionClosed) {
        return new NettyTCPClient(onConnectionAccepted, onConnectionClosed);
    }

    static ITCPClient newJavaNetClient() throws IOException {
        return new JavaNetTCPClient();
    }

    static ITCPClient newJavaNetClient(Consumer<ITCPClient> onConnectionAccepted, Consumer<ITCPClient> onConnectionClosed) throws IOException {
        return new JavaNetTCPClient(onConnectionAccepted, onConnectionClosed);
    }

    void connect(IPacketProtocol protocol, String address) throws IOException;
    void connect(IPacketProtocol protocol, String address, int timeout) throws IOException;
    void connect(IPacketProtocol protocol, InetSocketAddress address) throws IOException;
    void connect(IPacketProtocol protocol, InetSocketAddress address, int timeout) throws IOException;
    void sendPacket(IPacketProtocol protocol, IPacket packet) throws IOException;
    boolean isConnected();
    void disconnect();

}

package me.zombii.qnet.impl.netty.tcp;

import io.netty.channel.ChannelHandlerContext;
import me.zombii.qnet.api.ITCPServer;
import me.zombii.qnet.io.Serializer;
import me.zombii.qnet.api.connections.IConnection;
import me.zombii.qnet.api.connections.IJavaNetConnection;
import me.zombii.qnet.api.packet.IPacket;
import me.zombii.qnet.api.packet.IPacketFormat;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.api.connections.INettyConnection;
import me.zombii.qnet.api.Side;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NettyTCPServerConnection implements INettyConnection {

    NettyTCPServer server;
    ChannelHandlerContext context;
    IPacketProtocol protocol;

    public NettyTCPServerConnection(NettyTCPServer server, ChannelHandlerContext context, IPacketProtocol protocol) throws IOException {
        this.server = server;
        this.context = context;
        this.protocol = protocol;
    }

    @Override
    public void sendPacket(IPacketProtocol packetProtocol, IPacket packet) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Serializer serializer = new Serializer(byteArrayOutputStream);

        IPacketFormat packetFormat = packetProtocol.getPacketFormat();
        packetFormat.write(packetProtocol, serializer, packet);

        serializer.close();
        byte[] dataPacket = byteArrayOutputStream.toByteArray();

        if (context != null && context.channel().isActive() && context.channel().isWritable()) {
            context.writeAndFlush(dataPacket).addListener(future -> {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                }
            });
        } else throw new IOException("Tried sending packet to disconnected client");
    }

    @Override
    public void broadcastPacketToOthers(IPacketProtocol packetProtocol, IPacket packet) throws IOException {
        for (IConnection connection : server.getAllConnections()) {
            if (connection != this) {
                connection.sendPacket(protocol, packet);
            }
        }
    }

    @Override
    public void swap(IPacketProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public IPacketProtocol getProtocol() {
        return protocol;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }

    @Override
    public void disconnect() {
        server.disconnect(this);
    }

    @Override
    public boolean isUsingJavaNet() {
        return false;
    }

    @Override
    public boolean isUsingNetty() {
        return true;
    }

    @Override
    public IJavaNetConnection asJavaNetConnection() {
        throw new UnsupportedOperationException("Tried to get NettyConnection as JavaNetConnection");
    }

    @Override
    public INettyConnection asNettyConnection() {
        return this;
    }

    @Override
    public ITCPServer getServer() {
        return server;
    }

    @Override
    public ChannelHandlerContext getHandlerContext() {
        return context;
    }

    @Override
    public void setHandlerContext(ChannelHandlerContext context) {
        this.context = context;
    }
}

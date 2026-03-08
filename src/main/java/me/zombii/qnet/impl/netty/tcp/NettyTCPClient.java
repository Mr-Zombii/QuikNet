package me.zombii.qnet.impl.netty.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.zombii.qnet.api.ITCPClient;
import me.zombii.qnet.api.ITCPServer;
import me.zombii.qnet.io.Serializer;
import me.zombii.qnet.api.connections.IJavaNetConnection;
import me.zombii.qnet.api.packet.IPacket;
import me.zombii.qnet.api.packet.IPacketFormat;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.api.connections.INettyConnection;
import me.zombii.qnet.impl.netty.tcp.channel.client.NettyTCPClientChannelIntializer;
import me.zombii.qnet.api.Side;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class NettyTCPClient implements ITCPClient, INettyConnection {

    public final AtomicBoolean connected = new AtomicBoolean(false);

    public final Consumer<ITCPClient> onConnectionEstablished;
    public final Consumer<ITCPClient> onConnectionClosed;

    public NettyTCPClient() {
        this((a) -> {}, (a) -> {});
    }

    public NettyTCPClient(Consumer<ITCPClient> onConnectionEstablished, Consumer<ITCPClient> onConnectionClosed) {
        this.onConnectionEstablished = onConnectionEstablished;
        this.onConnectionClosed = onConnectionClosed;
    }

    Thread clientThread;

    public void connect(IPacketProtocol defaultProtocol, String address) throws IOException {
        connect(defaultProtocol, address, 0);
    }

    public void connect(IPacketProtocol defaultProtocol, String address, int timeout) throws IOException {
        InetSocketAddress addressToBind;

        String[] parts = address.split(":");
        if (parts.length == 2) {
            addressToBind = new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
            connect(defaultProtocol, addressToBind, timeout);
            return;
        }

        addressToBind = new InetSocketAddress(parts[0], 12345);
        connect(defaultProtocol, addressToBind, timeout);
    }

    public void connect(IPacketProtocol defaultProtocol, InetSocketAddress address) throws IOException {
        connect(defaultProtocol, address, 0);
    }

    public void connect(IPacketProtocol defaultProtocol, InetSocketAddress address, int timeout) throws IOException {
        if (connected.get())
            throw new IOException("Already connected!");

        clientThread = new Thread(() -> {
            try {
                run(defaultProtocol, address, timeout);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (channel != null)
                    channel.close();
            }
        });

        clientThread.setName("TCPClient on address: " + address);
        clientThread.setDaemon(true);
        clientThread.start();
    }

    IPacketProtocol protocol;

    public void sendPacket(IPacketProtocol packetProtocol, IPacket packet) throws IOException {
        if (!connected.get())
            throw new IOException("Client is not connected");

        if (!channel.isActive() || !channel.isWritable())
            throw new IOException("Client is not connected");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Serializer packetSerializer = new Serializer(stream);
        IPacketFormat packetFormat = packetProtocol.getPacketFormat();
        packetFormat.write(packetProtocol, packetSerializer, packet);
        packetSerializer.close();

        if (context != null && context.channel().isActive() && context.channel().isWritable()) {
            context.writeAndFlush(stream.toByteArray()).addListener(future -> {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                }
            });
        } else throw new IOException("Tried sending packet from disconnected client");
    }

    Bootstrap bootstrap;
    Channel channel;
    EventLoopGroup eventLoopGroup;

    @SuppressWarnings({"unchecked", "rawtypes", "CallToPrintStackTrace"})
    private void run(IPacketProtocol defaultProtocol, InetSocketAddress address, int timeout) throws IOException {
        this.protocol = defaultProtocol;


        eventLoopGroup = new NioEventLoopGroup();

        bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyTCPClientChannelIntializer(this, defaultProtocol));

        try {
            channel = bootstrap.connect(address).syncUninterruptibly().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            // we are closing the thread now!
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public boolean isConnected() {
        return connected.get();
    }

    public void disconnect() {
        if (!this.connected.get())
            throw new IllegalStateException("Cannot disconnect client twice!");

        this.connected.set(false);
        this.onConnectionClosed.accept(this);
        try {
            channel.close();
            this.clientThread.interrupt();
        } catch (Exception ignore) {}
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
    public ITCPServer getServer() {
        throw new UnsupportedOperationException("Tried to get the server from the client-side.");
    }

    @Override
    public ITCPClient getClient() {
        return this;
    }

    @Override
    public IJavaNetConnection asJavaNetConnection() {
        throw new UnsupportedOperationException("Tried to get NettyClient as JavaNetClient");
    }

    @Override
    public INettyConnection asNettyConnection() {
        return this;
    }

    public Side getSide() {
        return Side.CLIENT;
    }

    @Override
    public void swap(IPacketProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public IPacketProtocol getProtocol() {
        return protocol;
    }

    ChannelHandlerContext context;
    @Override
    public ChannelHandlerContext getHandlerContext() {
        return context;
    }

    @Override
    public void setHandlerContext(ChannelHandlerContext context) {
        this.context = context;
    }
}

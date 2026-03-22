package me.zombii.qnet.impl.netty.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.zombii.qnet.api.ITCPServer;
import me.zombii.qnet.api.connections.IConnection;
import me.zombii.qnet.api.packet.IPacket;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.api.connections.INettyConnection;
import me.zombii.qnet.impl.netty.tcp.channel.server.NettyTCPServerChannelInitializer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class NettyTCPServer implements ITCPServer {

    private ServerBootstrap bootstrap;
    private final AtomicBoolean running = new AtomicBoolean(false);

    final ObjectList<IConnection> connections;

    public final Object2ObjectMap<ChannelHandlerContext, INettyConnection> connectionMap;
    public final Object2ObjectMap<INettyConnection, ChannelHandlerContext> reverseConnectionMap;

    EventLoopGroup parentEventLoop;
    EventLoopGroup childEventLoop;
    Channel channel;

    Consumer<IConnection> onConnectionAccepted;
    Consumer<IConnection> onConnectionClosed;

    public NettyTCPServer() {
        this((a) -> {}, (a) -> {});
    }

    public NettyTCPServer(Consumer<IConnection> onConnectionAccepted, Consumer<IConnection> onConnectionClosed) {
        this.onConnectionAccepted = onConnectionAccepted;
        this.onConnectionClosed = onConnectionClosed;

        connections = new ObjectArrayList<>();
        connectionMap = new Object2ObjectOpenHashMap<>();
        reverseConnectionMap = new Object2ObjectOpenHashMap<>();
    }

    private void remakeBootstrap(IPacketProtocol defaultProtocol) {
        parentEventLoop = new NioEventLoopGroup();
        childEventLoop = new NioEventLoopGroup();

        bootstrap = new ServerBootstrap();
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.group(parentEventLoop, childEventLoop)
                .channel(NioServerSocketChannel.class)
                .childHandler(new NettyTCPServerChannelInitializer(this, defaultProtocol));

        connections.clear();
        connectionMap.clear();
        reverseConnectionMap.clear();
        running.set(false);
    }

    public void bind(IPacketProtocol defaultProtocol, String address) {
        InetSocketAddress addressToBind;

        String[] parts = address.split(":");
        if (parts.length == 2) {
            addressToBind = new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
            bind(defaultProtocol, addressToBind);
            return;
        }

        addressToBind = new InetSocketAddress(parts[0], 12345);
        bind(defaultProtocol, addressToBind);
    }

    Thread serverThread;

    public void bind(IPacketProtocol defaultProtocol, InetSocketAddress address) {
        if (running.get())
            throw new IllegalStateException("Server is already bound and running");

        serverThread = new Thread(() -> {
            try {
                run(defaultProtocol, address);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (channel != null)
                    channel.close();
            }
        });

        serverThread.setName("TCPServer on port: " + address.getPort());
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void run(IPacketProtocol defaultProtocol, InetSocketAddress address) throws IOException {
        if (bootstrap == null || channel == null || !channel.isOpen())
            remakeBootstrap(defaultProtocol);

        try {
            channel = bootstrap.bind(address).syncUninterruptibly().channel();
            running.set(true);
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            // we are closing the thread now!
        } finally {
            parentEventLoop.shutdownGracefully();
            childEventLoop.shutdownGracefully();
        }
    }

    public void stop() {
        channel.close();
        serverThread.interrupt();
        running.set(false);
    }

    public void disconnect(IConnection connection) {
        if (connection.isUsingNetty())
            throw new IllegalStateException("Cannot disconnect a javanet-connection from a netty server");

        INettyConnection iNettyConnection = connection.asNettyConnection();

        iNettyConnection.getHandlerContext().close();
    }


    public void broadcastPacket(IPacketProtocol protocol, IPacket packet) throws IOException {
        for (IConnection connection : getAllConnections()) {
            connection.sendPacket(protocol, packet);
        }
    }

    public void broadcastPacketToAllExcept(IConnection connectionToExclude, IPacketProtocol packetProtocol, IPacket packet) throws IOException {
        for (IConnection connection : getAllConnections()) {
            if (connection != connectionToExclude)
                connection.sendPacket(packetProtocol, packet);
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public ObjectList<IConnection> getAllConnections() {
        return connections;
    }

    public Consumer<IConnection> getOnConnectionAccepted() {
        return onConnectionAccepted;
    }

    public Consumer<IConnection> getOnConnectionClosed() {
        return onConnectionClosed;
    }
}

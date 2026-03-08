package me.zombii.qnet.impl.javanet.tcp;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.zombii.qnet.api.ITCPServer;
import me.zombii.qnet.api.connections.IConnection;
import me.zombii.qnet.api.connections.IJavaNetConnection;
import me.zombii.qnet.api.packet.IPacket;
import me.zombii.qnet.api.packet.IPacketProtocol;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class JavaNetTCPServer implements ITCPServer {

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final ObjectList<IConnection> connections;

    private final Object2ObjectMap<SocketAddress, JavaNetTCPServerConnection> connectionMap;

    private final Consumer<IConnection> onConnectionAccepted;
    private final Consumer<IConnection> onConnectionClosed;

    public JavaNetTCPServer() throws IOException {
        this((a) -> {}, (a) -> {});
    }

    public JavaNetTCPServer(Consumer<IConnection> onConnectionAccepted, Consumer<IConnection> onConnectionClosed) throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        executorService = Executors.newFixedThreadPool(4);

        connections = new ObjectArrayList<>();
        connectionMap = new Object2ObjectOpenHashMap<>();

        this.onConnectionAccepted = onConnectionAccepted;
        this.onConnectionClosed = onConnectionClosed;
    }

    private void remakeSocket() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        executorService = Executors.newFixedThreadPool(4);

        connections.clear();
        connectionMap.clear();
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
                try {
                    serverSocket.close();
                } catch (IOException ignore) {}
            }
        });

        serverThread.setName("TCPServer on port: " + address.getPort());
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void run(IPacketProtocol defaultProtocol, InetSocketAddress address) throws IOException {
        if (this.serverSocket.isClosed())
            remakeSocket();

        serverSocket.bind(address);

        if (!running.get()) {
            executorService.execute(() -> {
                while (serverSocket.isBound() && running.get()) {
                    for (int i = 0; i < connections.size(); i++) {
                        try {
                            ((JavaNetTCPServerConnection)connections.get(i)).tick();
                        } catch (IOException e) {
                            System.err.println("Connection " + connections.get(i).asJavaNetConnection().getSocket().getLocalAddress() + " failed to tick, removing.");
                            e.printStackTrace();

                            IJavaNetConnection connection = connections.get(i).asJavaNetConnection();
                            connections.remove(connection);
                            connectionMap.remove(connections.get(i).asJavaNetConnection().getSocket().getLocalSocketAddress());
                            try {
                                connections.get(i).asJavaNetConnection().getSocket().close();
                            } catch (IOException ignore) {}
                            i--;
                        }
                    }
                }
            });
        }

        running.set(true);

        try {
            while (running.get()) {
                if (Thread.currentThread().isInterrupted()) {
                    List<IConnection> toRemove = new ArrayList<>(connections);
                    toRemove.forEach(this::disconnect);
                    return;
                }
                Socket socket = serverSocket.accept();

                JavaNetTCPServerConnection connection = new JavaNetTCPServerConnection(this, socket, defaultProtocol);
                connectionMap.put(socket.getLocalSocketAddress(), connection);
                connections.add(connection);
                onConnectionAccepted.accept(connection);
            }
        } catch (SocketException e) {
            // Closing Thread
        }
    }

    public void stop() {
        List<IConnection> toRemove = new ArrayList<>(connections);
        toRemove.forEach(this::disconnect);
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        running.set(false);

        executorService.shutdownNow();
    }

    public void disconnect(IConnection connection) {
        if (connection.isUsingNetty())
            throw new IllegalStateException("Cannot disconnect a netty-connection from a javanet server");

        IJavaNetConnection connectionToDisconnect = connection.asJavaNetConnection();

        connections.remove(connectionToDisconnect);
        connectionMap.remove(connectionToDisconnect.getSocket().getLocalSocketAddress());
        try {
            connectionToDisconnect.getSocket().close();
        } catch (IOException ignore) {}
        onConnectionClosed.accept(connection);
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

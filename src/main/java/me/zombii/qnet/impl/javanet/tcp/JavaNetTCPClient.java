package me.zombii.qnet.impl.javanet.tcp;

import me.zombii.qnet.api.ITCPClient;
import me.zombii.qnet.api.ITCPServer;
import me.zombii.qnet.io.Deserializer;
import me.zombii.qnet.io.Serializer;
import me.zombii.qnet.api.connections.IJavaNetConnection;
import me.zombii.qnet.api.packet.IPacket;
import me.zombii.qnet.api.packet.IPacketFormat;
import me.zombii.qnet.api.packet.IPacketHandler;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.api.connections.INettyConnection;
import me.zombii.qnet.api.Side;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;

public class JavaNetTCPClient implements ITCPClient, IJavaNetConnection {

    private Socket socket;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    private final Consumer<ITCPClient> onConnectionEstablished;
    private final Consumer<ITCPClient> onConnectionClosed;

    public JavaNetTCPClient() {
        this((a) -> {}, (a) -> {});
    }

    public JavaNetTCPClient(Consumer<ITCPClient> onConnectionEstablished, Consumer<ITCPClient> onConnectionClosed) {
        this.socket = new Socket();
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
                try {
                    this.socket.close();
                } catch (IOException ignore) {}
            }
        });

        clientThread.setName("TCPClient on address: " + address);
        clientThread.setDaemon(true);
        clientThread.start();
    }

    OutputStream outputStream;
    Serializer serializer;

    IPacketProtocol protocol;

    public void sendPacket(IPacketProtocol packetProtocol, IPacket packet) throws IOException {
        if (!connected.get())
            throw new IOException("Client is not connected");

        if (socket.isClosed() || !socket.isConnected())
            throw new IOException("Client is not connected");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Serializer packetSerializer = new Serializer(stream);
        IPacketFormat packetFormat = packetProtocol.getPacketFormat();
        packetFormat.write(packetProtocol, packetSerializer, packet);
        packetSerializer.close();
        serializer.writeByteArray(stream.toByteArray());
        outputStream.flush();
    }

    @Override
    public void broadcastPacketToOthers(IPacketProtocol packetProtocol, IPacket packet) throws IOException {
        throw new IOException("Cannot broadcast packet from client!");
    }

    @SuppressWarnings({"unchecked", "rawtypes", "CallToPrintStackTrace"})
    private void run(IPacketProtocol defaultProtocol, InetSocketAddress address, int timeout) throws IOException {
        if (this.socket.isClosed()) {
            this.socket = new Socket();
            this.socket.setReuseAddress(true);
        }

        this.socket.connect(address, timeout);
        if (!socket.isConnected())
            return;

        this.protocol = defaultProtocol;

        InputStream inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        Deserializer deserializer = new Deserializer(inputStream);
        serializer = new Serializer(outputStream);

        this.connected.set(socket.isConnected());
        this.onConnectionEstablished.accept(this);

        while (this.connected.get()) {
            if (socket.isClosed() || !socket.isConnected()) {
                this.connected.set(false);
                break;
            }

            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            IPacketFormat packetFormat = protocol.getPacketFormat();

            try {
                if (!socket.isClosed() && inputStream.available() > 0) {
                    byte[] packetBytes = deserializer.readByteArray();
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packetBytes);
                    Deserializer packetDeserializer = new Deserializer(byteArrayInputStream);

                    try {
                        IPacket packet = packetFormat.read(protocol, packetDeserializer);
                        IPacketHandler handler = protocol.getHandler(getSide(), packet);
                        if (handler == null) {
                            System.err.println("Packet " + packet.getClass().getName() + " has been received without a handler for the " + getSide() + " side.");
                        } else {
                            handler.handle(getSide(), this, protocol, packet);
                        }
                    } finally {
                        packetDeserializer.close();
                    }
                }
            } catch (DataFormatException e) {
                e.printStackTrace();
            }
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
            this.clientThread.interrupt();
            outputStream.flush();
            this.socket.close();
        } catch (Exception ignore) {}
    }

    @Override
    public boolean isUsingJavaNet() {
        return true;
    }

    @Override
    public boolean isUsingNetty() {
        return false;
    }

    @Override
    public ITCPServer getServer() {
        throw new UnsupportedOperationException("Not supported on a client connection.");
    }

    @Override
    public IJavaNetConnection asJavaNetConnection() {
        return this;
    }

    @Override
    public INettyConnection asNettyConnection() {
        throw new UnsupportedOperationException("Tried to get JavaNetConnection as NettyConnection");
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

    @Override
    public Socket getSocket() {
        return socket;
    }

    @Override
    public void setSocket(Socket socket) {
        this.socket = socket;
    }


}

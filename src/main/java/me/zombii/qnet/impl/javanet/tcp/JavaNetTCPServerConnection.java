package me.zombii.qnet.impl.javanet.tcp;

import me.zombii.qnet.api.ITCPServer;
import me.zombii.qnet.io.Deserializer;
import me.zombii.qnet.io.Serializer;
import me.zombii.qnet.api.connections.IConnection;
import me.zombii.qnet.api.connections.IJavaNetConnection;
import me.zombii.qnet.api.packet.IPacket;
import me.zombii.qnet.api.packet.IPacketFormat;
import me.zombii.qnet.api.packet.IPacketHandler;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.api.connections.INettyConnection;
import me.zombii.qnet.api.Side;

import java.io.*;
import java.net.Socket;
import java.util.zip.DataFormatException;

public class JavaNetTCPServerConnection implements IJavaNetConnection {

    JavaNetTCPServer server;
    Socket socket;
    IPacketProtocol protocol;

    InputStream inputStream;
    OutputStream outputStream;

    Serializer serializer;
    Deserializer deserializer;

    public JavaNetTCPServerConnection(JavaNetTCPServer server, Socket socket, IPacketProtocol protocol) throws IOException {
        this.server = server;
        this.socket = socket;
        this.protocol = protocol;

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        deserializer = new Deserializer(inputStream);
        serializer = new Serializer(outputStream);
    }

    @SuppressWarnings({"unchecked", "rawtypes", "CallToPrintStackTrace"})
    public void tick() throws IOException {
        IPacketFormat packetFormat = protocol.getPacketFormat();

        if (socket.isConnected() && !socket.isClosed()) {
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

    @Override
    public void sendPacket(IPacketProtocol packetProtocol, IPacket packet) throws IOException {
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
        return true;
    }

    @Override
    public boolean isUsingNetty() {
        return false;
    }

    @Override
    public IJavaNetConnection asJavaNetConnection() {
        return this;
    }

    @Override
    public INettyConnection asNettyConnection() {
        throw new UnsupportedOperationException("Tried to get JavaNetConnection as NettyConnection");
    }


    @Override
    public ITCPServer getServer() {
        return server;
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

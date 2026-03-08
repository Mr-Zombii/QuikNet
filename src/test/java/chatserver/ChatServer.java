package chatserver;

import chatserver.packets.MessagePacket;
import me.zombii.qnet.api.ITCPServer;
import me.zombii.qnet.api.connections.IConnection;
import me.zombii.qnet.api.packet.IPacketHandler;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.api.Side;

import java.awt.*;
import java.io.IOException;

public class ChatServer {

    public static void main(String[] args) throws IOException, AWTException {
        IPacketProtocol protocol = IPacketProtocol.makeDefault();
        protocol.registerPacket(MessagePacket.class);
        protocol.registerHandler(Side.SERVER, MessagePacket.class, new IPacketHandler<MessagePacket>() {
            @Override
            public void handle(Side side, IConnection connection, IPacketProtocol protocol, MessagePacket packet) throws IOException {
                System.out.println(packet.getSender() + " > " + packet.getMessage());
                connection.broadcastPacketToOthers(protocol, packet);
            }
        });

        ITCPServer tcpServer = ITCPServer.newJavaNetServer();
        tcpServer.bind(protocol, "0.0.0.0:12345");

        while (true) {}
    }

}

package chatserver;

import chatserver.packets.MessagePacket;
import me.zombii.qnet.api.ITCPClient;
import me.zombii.qnet.api.connections.IConnection;
import me.zombii.qnet.api.packet.IPacketHandler;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.api.Side;

import java.io.IOException;
import java.util.Scanner;

public class ChatClient {

    public static void main(String[] args) throws IOException {
        IPacketProtocol protocol = IPacketProtocol.makeDefault();
        protocol.registerPacket(MessagePacket.class);
        protocol.registerHandler(Side.CLIENT, MessagePacket.class, new IPacketHandler<MessagePacket>() {
            @Override
            public void handle(Side side, IConnection connection, IPacketProtocol protocol, MessagePacket packet) throws IOException {
                System.out.println(packet.getSender() + " > " + packet.getMessage());
            }
        });

        Scanner scanner = new Scanner(System.in);
        System.out.print("Sender Name: ");
        String name = scanner.nextLine();

        String serverIp = "127.0.0.1:12345";

        ITCPClient tcpClient = ITCPClient.newNettyClient();
        tcpClient.connect(protocol, serverIp);

        while (!tcpClient.isConnected()) {}
        System.out.println("Connected To Server: " + serverIp);

        MessagePacket packet = new MessagePacket();
        while (true) {
            System.out.print("Message: \n");
            String msg = scanner.nextLine();
            System.out.println(name + " > " + msg);
            packet.setSender(name);
            packet.setMessage(msg);
            tcpClient.sendPacket(protocol, packet);
        }
    }

}

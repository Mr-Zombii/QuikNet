import me.zombii.qnet.api.ITCPClient;
import me.zombii.qnet.api.ITCPServer;
import me.zombii.qnet.io.Deserializer;
import me.zombii.qnet.io.Serializer;
import me.zombii.qnet.api.packet.IPacket;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.api.Side;

import java.io.IOException;

public class ConnectionTest {

    public static class HelloPacket implements IPacket {

        String data;

        public HelloPacket() {}

        public HelloPacket(String data) {
            this.data = data;
        }

        @Override
        public void read(Deserializer deserializer) throws IOException {
            data = deserializer.readString();
        }

        @Override
        public void write(Serializer serializer) throws IOException {
            serializer.writeString(data);
        }
    }

    public static class DisconnectPacket implements IPacket {}

    public static void main(String[] args) throws IOException {
        IPacketProtocol protocol = IPacketProtocol.makeDefault();
        protocol.registerPacket(HelloPacket.class);
        protocol.registerHandler(Side.SERVER, HelloPacket.class,
                (side, connection, protocol1, packet) -> {
                    System.out.println(packet.data);
                    connection.sendPacket(protocol1, new DisconnectPacket());
                    connection.disconnect();
                    connection.getServer().stop();
                }
        );
        protocol.registerPacket(DisconnectPacket.class);
        protocol.registerHandler(Side.CLIENT, DisconnectPacket.class,
                (side, connection, protocol2, packet) -> {
                    connection.disconnect();
                }
        );

        ITCPServer server = ITCPServer.newJavaNetServer();
        server.bind(protocol, "0.0.0.0:54321");

        while (!server.isRunning()) {
        }

        ITCPClient client = ITCPClient.newNettyClient((c) -> {
            try {
                c.sendPacket(protocol, new HelloPacket("HI!"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, (a) -> {});
        client.connect(protocol, "127.0.0.1:54321");
        while (!client.isConnected()) {}

        while (client.isConnected()) {}
    }

}

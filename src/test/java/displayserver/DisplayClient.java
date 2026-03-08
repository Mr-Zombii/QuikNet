package displayserver;

import displayserver.packet.ScreenPacket;
import me.zombii.qnet.api.ITCPClient;
import me.zombii.qnet.api.connections.IConnection;
import me.zombii.qnet.api.packet.IPacketHandler;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.api.Side;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class DisplayClient {

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame();
        Thread a = new Thread(() -> {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);

            frame.setVisible(true);
        });
        a.setDaemon(true);
        a.start();

        final Graphics[] g = {null};

        IPacketProtocol protocol = IPacketProtocol.makeDefault();
        protocol.registerPacket(ScreenPacket.class);

        protocol.registerHandler(Side.CLIENT, ScreenPacket.class, new IPacketHandler<ScreenPacket>() {
            @Override
            public void handle(Side side, IConnection connection, IPacketProtocol protocol, ScreenPacket packet) throws IOException {
                BufferedImage image = ImageCompressor.decompressJpegFromMemory(packet.imageData);
                if (g[0] == null) {
                    g[0] = frame.getGraphics();
                }
                g[0].drawImage(image, 0, 0, frame.getWidth(), frame.getHeight(), null);
            }
        });

        String serverIp = "127.0.0.1:54321";

        ITCPClient tcpClient = ITCPClient.newNettyClient();
        tcpClient.connect(protocol, serverIp);

        while (!tcpClient.isConnected()) {}
        System.out.println("Connected To Server: " + serverIp);

        while (true) {}
    }

}

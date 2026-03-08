package displayserver;

import displayserver.packet.ScreenPacket;
import me.zombii.qnet.api.ITCPServer;
import me.zombii.qnet.api.packet.IPacketProtocol;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class DisplayServer {

    public static void main(String[] args) throws IOException, AWTException {
        IPacketProtocol protocol = IPacketProtocol.makeDefault();
        protocol.registerPacket(ScreenPacket.class);

        ITCPServer tcpServer = ITCPServer.newNettyServer();
        tcpServer.bind(protocol, "0.0.0.0:54321");

        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        robot.setAutoDelay(0);

        ScreenPacket screenPacket = new ScreenPacket();
        while (true) {
            if (tcpServer.getAllConnections().isEmpty())
                continue;

            BufferedImage image = robot.createScreenCapture(screenRect);
            Graphics graphics = image.getGraphics();

            Point mousePosition = MouseInfo.getPointerInfo().getLocation();
            int x = mousePosition.x;
            int y = mousePosition.y;
            if (x - 1 < image.getWidth() && y - 1 < image.getHeight()) {
                graphics.setColor(Color.BLACK);
                graphics.drawRect(x, y, 6, 6);
                graphics.setColor(Color.WHITE);
                graphics.drawRect(x + 1, y + 1, 4, 4);
            }

            byte[] data = ImageCompressor.compressToJpegInMemory(image, 0.3f);

            screenPacket.setImageData(data);
            try {
                tcpServer.broadcastPacket(protocol, screenPacket);
            } catch (IOException e) {}
        }
    }

}

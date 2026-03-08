package displayserver.packet;

import me.zombii.qnet.io.Deserializer;
import me.zombii.qnet.io.Serializer;
import me.zombii.qnet.api.packet.IPacket;

import java.io.IOException;

public class ScreenPacket implements IPacket {

    public byte[] imageData;

    public ScreenPacket() {}

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    @Override
    public void read(Deserializer deserializer) throws IOException {
        this.imageData = deserializer.readByteArray();
    }

    @Override
    public void write(Serializer serializer) throws IOException {
        serializer.writeByteArray(imageData);
    }

}

package chatserver.packets;

import me.zombii.qnet.io.Deserializer;
import me.zombii.qnet.io.Serializer;
import me.zombii.qnet.api.packet.IPacket;

import java.io.IOException;

public class MessagePacket implements IPacket {

    String sender;
    String message;

    public MessagePacket() {}

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void read(Deserializer deserializer) throws IOException {
        this.sender = deserializer.readString();
        this.message = deserializer.readString();
    }

    @Override
    public void write(Serializer serializer) throws IOException {
        serializer.writeString(sender);
        serializer.writeString(message);
    }

}

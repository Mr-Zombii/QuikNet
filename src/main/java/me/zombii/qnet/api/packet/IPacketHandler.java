package me.zombii.qnet.api.packet;

import me.zombii.qnet.api.connections.IConnection;
import me.zombii.qnet.api.Side;

import java.io.IOException;

public interface IPacketHandler<T extends IPacket> {

    void handle(Side side, IConnection connection, IPacketProtocol protocol, T packet) throws IOException;

}

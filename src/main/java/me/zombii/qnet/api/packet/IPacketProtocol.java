package me.zombii.qnet.api.packet;

import me.zombii.qnet.api.Side;
import me.zombii.qnet.impl.packet.DefaultPacketProtocol;

public interface IPacketProtocol {

    static IPacketProtocol makeDefault() {
        return new DefaultPacketProtocol();
    }

    static IPacketProtocol packetProtocol(IPacketFormat packetFormat) {
        return new DefaultPacketProtocol(packetFormat);
    }

    int registerPacket(Class<? extends IPacket> packetClass);
    int registerPacket(Class<? extends IPacket> packetClass, int id);

    void registerHandler(Side side, int id, IPacketHandler<?> handler);
    <T extends IPacket> void registerHandler(Side side, Class<T> packetClass, IPacketHandler<T> handler);

    IPacketHandler<?> getHandler(Side side, int id);
    <T extends IPacket> IPacketHandler<?> getHandler(Side side, Class<T> packetClass);
    <T extends IPacket> IPacketHandler<?> getHandler(Side side, IPacket packet);

    int getPacketId(IPacket packet);
    int getPacketId(Class<? extends IPacket> packetClass);
    Class<? extends IPacket> getPacketClass(int id);

    boolean hasPacketBeenRegistered(Class<? extends IPacket> packetClass);
    boolean hasPacketIdBeenTaken(int packetId);

    IPacket instancePacket(int id);

    IPacketFormat getPacketFormat();
}

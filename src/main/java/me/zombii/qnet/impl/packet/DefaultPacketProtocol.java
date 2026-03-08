package me.zombii.qnet.impl.packet;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.zombii.qnet.api.packet.IPacket;
import me.zombii.qnet.api.packet.IPacketFormat;
import me.zombii.qnet.api.packet.IPacketHandler;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.api.Side;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DefaultPacketProtocol implements IPacketProtocol {

    boolean canOverridePacketsAndPacketHandlers;
    IPacketFormat packetFormat;

    public DefaultPacketProtocol() {
        this(new DefaultPacketFormat(), false);
    }

    public DefaultPacketProtocol(boolean allowPacketAndHandlerOverwriting) {
        this(new DefaultPacketFormat(), allowPacketAndHandlerOverwriting);
    }

    public DefaultPacketProtocol(IPacketFormat packetFormat) {
        this(packetFormat, false);
    }

    public DefaultPacketProtocol(IPacketFormat packetFormat, boolean allowPacketAndHandlerOverwriting) {
        this.canOverridePacketsAndPacketHandlers = allowPacketAndHandlerOverwriting;
        this.packetFormat = packetFormat;
    }

    Int2ObjectMap<Class<? extends IPacket>> id2Packet = new Int2ObjectArrayMap<>();
    Object2IntMap<Class<? extends IPacket>> packet2Id = new Object2IntOpenHashMap<>();
    Int2ObjectMap<Constructor<? extends IPacket>> id2Constructor = new Int2ObjectArrayMap<>();

    Int2ObjectMap<IPacketHandler<?>> id2ClientHandler = new Int2ObjectArrayMap<>();
    Int2ObjectMap<IPacketHandler<?>> id2ServerHandler = new Int2ObjectArrayMap<>();

    int counter = 0;

    @Override
    public int registerPacket(Class<? extends IPacket> packetClass) {
        while (id2Packet.containsKey(counter)) // allow it to not-override existing packets when doing lazy-registration.
            counter++;

        return registerPacket(packetClass, counter);
    }

    @Override
    public int registerPacket(Class<? extends IPacket> packetClass, int id) {
        if (!canOverridePacketsAndPacketHandlers && id2Packet.containsKey(id))
            throw new IllegalArgumentException("A packet has already been registered to this id: " + id + ".");

        Constructor<? extends IPacket> constructor = null;
        try {
            constructor = packetClass.getConstructor();
        } catch (NoSuchMethodException ignore) {}

        try {
            if (constructor == null) {
                constructor = packetClass.getDeclaredConstructor();
                constructor.setAccessible(true);
            }
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new IllegalArgumentException(packetClass.getName() + " has no default constructor!");
        }

        id2Constructor.put(id, constructor);
        id2Packet.put(id, packetClass);
        packet2Id.put(packetClass, id);
        return id;
    }

    @Override
    public void registerHandler(Side side, int id, IPacketHandler<?> handler) {
        if (!id2Packet.containsKey(id))
            throw new IllegalArgumentException("Packet " + id + " has not been assigned a packet, please register a packet to this id before registering a handler.");

        switch (side) {
            case CLIENT:
                if (!canOverridePacketsAndPacketHandlers && id2ClientHandler.containsKey(id))
                    throw new IllegalArgumentException("A client packet handler has already been registered to the id: " + id + ".");
                id2ClientHandler.put(id, handler);
                break;
            case SERVER:
                if (!canOverridePacketsAndPacketHandlers && id2ServerHandler.containsKey(id))
                    throw new IllegalArgumentException("A server packet handler has already been registered to the id: " + id + ".");
                id2ServerHandler.put(id, handler);
                break;
        }
    }

    @Override
    public <T extends IPacket> void registerHandler(Side side, Class<T> packetClass, IPacketHandler<T> handler) {
        if (!packet2Id.containsKey(packetClass))
            throw new IllegalArgumentException("Packet " + packetClass + " has not been registered.");

        int packetId = packet2Id.getInt(packetClass);
        registerHandler(side, packetId, handler);
    }

    @Override
    public IPacketHandler<?> getHandler(Side side, int id) {
        if (!id2Packet.containsKey(id))
            throw new IllegalArgumentException("Packet " + id + " has not been assigned a packet, please register a packet to this id before requesting a handler for it.");

        switch (side) {
            case CLIENT:
                if (id2ClientHandler.containsKey(id))
                    return id2ClientHandler.get(id);
                return null;
            case SERVER:
                if (id2ServerHandler.containsKey(id))
                    return id2ServerHandler.get(id);
                return null;
        }
        return null;
    }

    @Override
    public <T extends IPacket> IPacketHandler<?> getHandler(Side side, Class<T> packetClass) {
        if (!packet2Id.containsKey(packetClass))
            throw new IllegalArgumentException("Packet " + packetClass + " has not been registered.");

        int packetId = packet2Id.getInt(packetClass);
        return getHandler(side, packetId);
    }

    @Override
    public <T extends IPacket> IPacketHandler<?> getHandler(Side side, IPacket packet) {
        return getHandler(side, packet.getClass());
    }

    @Override
    public int getPacketId(IPacket packet) {
        return getPacketId(packet.getClass());
    }

    @Override
    public int getPacketId(Class<? extends IPacket> packetClass) {
        if (!packet2Id.containsKey(packetClass))
            throw new IllegalArgumentException("Packet " + packetClass + " has not been registered.");

        return packet2Id.getInt(packetClass);
    }

    @Override
    public Class<? extends IPacket> getPacketClass(int id) {
        if (!id2Packet.containsKey(id))
            throw new IllegalArgumentException("Packet " + id + " has not been assigned a packet, please register a packet to this id before requesting a class for it.");

        return id2Packet.get(id);
    }

    @Override
    public boolean hasPacketBeenRegistered(Class<? extends IPacket> packetClass) {
        return packet2Id.containsKey(packetClass);
    }

    @Override
    public boolean hasPacketIdBeenTaken(int packetId) {
        return id2Packet.containsKey(packetId);
    }

    @Override
    public IPacket instancePacket(int id) {
        if (!id2Packet.containsKey(id))
            throw new IllegalArgumentException("Packet " + id + " has not been assigned a packet, please register a packet to this id before requesting a class for it.");

        Constructor<? extends IPacket> constructor = id2Constructor.get(id);

        try {
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            // shouldn't happen at all
            throw new RuntimeException(e);
        }
    }

    @Override
    public IPacketFormat getPacketFormat() {
        return packetFormat;
    }
}

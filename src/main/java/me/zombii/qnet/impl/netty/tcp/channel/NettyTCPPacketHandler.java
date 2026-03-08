package me.zombii.qnet.impl.netty.tcp.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.zombii.qnet.io.Deserializer;
import me.zombii.qnet.api.packet.IPacket;
import me.zombii.qnet.api.packet.IPacketFormat;
import me.zombii.qnet.api.packet.IPacketHandler;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.api.connections.INettyConnection;
import me.zombii.qnet.api.Side;

import java.io.ByteArrayInputStream;
import java.util.function.Function;

public class NettyTCPPacketHandler extends SimpleChannelInboundHandler<byte[]> {

    Function<ChannelHandlerContext, INettyConnection> handler;

    public NettyTCPPacketHandler(Function<ChannelHandlerContext, INettyConnection> handler) {
        this.handler = handler;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        INettyConnection connection = handler.apply(ctx);
        IPacketProtocol protocol = connection.getProtocol();
        Side side = connection.getSide();

        try (ByteArrayInputStream in = new ByteArrayInputStream(msg)) {
            IPacketFormat packetFormat = protocol.getPacketFormat();
            Deserializer deserializer = new Deserializer(in);
            IPacket packet = packetFormat.read(protocol, deserializer);
            IPacketHandler handler = protocol.getHandler(side, packet);
            if (handler == null) {
                System.err.println("Packet " + packet.getClass().getName() + " has been received without a handler for the " + side + " side.");
            } else {
                handler.handle(side, connection, protocol, packet);
            }
        }
    }
}

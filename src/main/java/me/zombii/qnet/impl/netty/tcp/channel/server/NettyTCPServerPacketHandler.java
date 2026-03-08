package me.zombii.qnet.impl.netty.tcp.channel.server;

import io.netty.channel.ChannelHandlerContext;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.impl.netty.tcp.NettyTCPServer;
import me.zombii.qnet.impl.netty.tcp.NettyTCPServerConnection;
import me.zombii.qnet.impl.netty.tcp.channel.NettyTCPPacketHandler;

import java.io.IOException;

public class NettyTCPServerPacketHandler extends NettyTCPPacketHandler {

    NettyTCPServer server;
    IPacketProtocol defaultProtocol;

    public NettyTCPServerPacketHandler(NettyTCPServer server, IPacketProtocol defaultProtocol) {
        super(server.connectionMap);
        this.server = server;
        this.defaultProtocol = defaultProtocol;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        NettyTCPServerConnection connection = new NettyTCPServerConnection(
                server, ctx, defaultProtocol
        );
        server.connectionMap.put(ctx, connection);
        server.reverseConnectionMap.put(connection, ctx);
        server.getAllConnections().add(connection);
        server.getOnConnectionAccepted().accept(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        NettyTCPServerConnection connection = (NettyTCPServerConnection) server.connectionMap.get(ctx);
        server.reverseConnectionMap.remove(connection);
        server.getAllConnections().remove(connection);
        server.connectionMap.remove(connection.getHandlerContext());
        server.getOnConnectionClosed().accept(connection);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            System.out.println("Connection closed by remove host: " + ctx.channel().remoteAddress());
        } else cause.printStackTrace();

        ctx.close();
    }
}

package me.zombii.qnet.impl.netty.tcp.channel.client;

import io.netty.channel.ChannelHandlerContext;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.impl.netty.tcp.NettyTCPClient;
import me.zombii.qnet.impl.netty.tcp.channel.NettyTCPPacketHandler;

import java.io.IOException;

public class NettyTCPClientPacketHandler extends NettyTCPPacketHandler {

    NettyTCPClient client;
    IPacketProtocol defaultProtocol;

    public NettyTCPClientPacketHandler(NettyTCPClient client, IPacketProtocol defaultProtocol) {
        super((a) -> client);
        this.client = client;
        this.defaultProtocol = defaultProtocol;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        client.onConnectionClosed.accept(client);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        client.connected.set(true);
        client.setHandlerContext(ctx);
        client.onConnectionEstablished.accept(client);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            System.out.println("Connection closed by server: " + ctx.channel().remoteAddress());
        } else cause.printStackTrace();

        ctx.close();
    }
}

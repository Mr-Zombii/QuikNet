package me.zombii.qnet.impl.netty.tcp.channel.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import me.zombii.qnet.api.packet.IPacketProtocol;
import me.zombii.qnet.impl.netty.tcp.NettyTCPServer;

public class NettyTCPServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    NettyTCPServer server;

    IPacketProtocol defaultProtocol;

    public NettyTCPServerChannelInitializer(
            NettyTCPServer server,
            IPacketProtocol defaultProtocol
    ) {
        this.defaultProtocol = defaultProtocol;
        this.server = server;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldPrepender(4));
        pipeline.addLast(new ByteArrayEncoder());

        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        pipeline.addLast(new ByteArrayDecoder());
        pipeline.addLast(new NettyTCPServerPacketHandler(server, defaultProtocol));
    }

}

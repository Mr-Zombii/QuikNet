package me.zombii.qnet.api.connections;

import io.netty.channel.ChannelHandlerContext;

public interface INettyConnection extends IConnection {

    ChannelHandlerContext getHandlerContext();
    void setHandlerContext(ChannelHandlerContext socket);


}

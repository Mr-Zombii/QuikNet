package me.zombii.qnet.api.connections;

import java.net.Socket;

public interface IJavaNetConnection extends IConnection {

    Socket getSocket();
    void setSocket(Socket socket);

}

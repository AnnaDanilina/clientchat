package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;

    public String getName() {
        return name;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/auth")) {
                            String[] parts = str.split("\\s");
                            String nick = server.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                            if (nick != null) {
                                if (!server.isNickBusy(nick)) {
                                    sendMsg("/authok " + nick);
                                    name = nick;
                                    server.broadcastMsg(name + " зашел в чат");
                                    server.subscribe(this);
                                    break;
                                } else sendMsg("Учетная запись уже используется");
                            } else {
                                sendMsg("Неверные логин/пароль");
                            }
                        }
                    }
                    while (true) {
                        String str = in.readUTF();
                        System.out.println("from " + name + ": " + str);
                        if (str.equals("/end")) break;
                        if (str.startsWith("/w ")){
                            String [] strar = str.split(" ",3);
                            server.sendMsgToNick(this,strar[1],strar[2]);

                        } else
                            server.broadcastMsg(name + ": " + str);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    server.broadcastMsg(name + " вышел из чата");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

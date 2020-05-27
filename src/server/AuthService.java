package server;

public interface AuthService {
    void start();

    String getNickByLoginPass(String login, String pass);
    boolean changeNick(ClientHandler c, String newNick);
    void stop();
}
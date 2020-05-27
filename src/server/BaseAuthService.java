package server;

import java.util.ArrayList;

public class BaseAuthService  implements AuthService  {
    private class Entry {
        private String login;
        private String pass;
        private String nick;

        public Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }
    }

    private ArrayList<Entry> entries;

    @Override
    public void start() { }

    @Override
    public void stop() { }

    @Override
    public boolean changeNick(ClientHandler c, String newNick) {
        for (Entry o : entries) {
            if (o.nick.equals(newNick))
                return false;
        }
        for (Entry o : entries) {
            if (o.nick.equals(c.getName())) {
                o.nick = newNick;
                return true;
            }
        }
        return false;
    }

    public BaseAuthService() {
        entries = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            entries.add(new Entry("login" +i, "pass" +i, "nick" +i));
        }
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (Entry o : entries) {
            if (o.login.equals(login) && o.pass.equals(pass)) return o.nick;
        }
        return null;
    }


}

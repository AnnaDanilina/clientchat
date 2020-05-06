package clientchat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Controller implements Initializable {

    @FXML
    public TextArea jta;
    @FXML
    public TextField jtf;

    public TextField loginField;
    public PasswordField passField;

    public HBox bottomPanel;
    public HBox upperPanel;

    private boolean authorized;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        if (!this.authorized) {
            upperPanel.setVisible(true);
            bottomPanel.setVisible(false);
        } else {
            upperPanel.setVisible(false);
            bottomPanel.setVisible(true);
            jta.clear();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            setAuthorized(false);
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/authok")) {
                            setAuthorized(true);
                            break;
                        }
                        jta.appendText(str + "\n");
                    }
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            break;
                        }
                        jta.appendText(str + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setAuthorized(false);
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            Platform.exit();
        }
    }

    public void onSendMsg() {
        try {
            out.writeUTF(jtf.getText());
            if (jtf.getText().equals("/end")) {
                socket.close();
            }
            jtf.clear();
            jtf.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onEnter() {
        String str = jtf.getText();
        if (str.length() > 0) {
            try {
                out.writeUTF(jtf.getText());
                jtf.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onAuthClick() {
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passField.getText());
            loginField.clear();
            passField.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
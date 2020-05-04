package clientchat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public void sendMsg() {
        String str = jtf.getText();
        if (str.length() > 0) {
            try {
                out.writeUTF(jtf.getText());
                jtf.clear();
                jtf.requestFocus();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 87);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        jta.appendText(str + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            Platform.exit();
        }

    }
}
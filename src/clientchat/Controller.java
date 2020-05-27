package clientchat;
import javafx.scene.control.Alert;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
    public VBox clientsPanel;

    public BorderPane mainPanel;
    public ListView clientsList;
    public ObservableList<String> obsClients;

    private boolean timeOUT;
    private boolean authorized;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String myNick = "";

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        if (this.timeOUT) {
            showTimeOUT();
        } else {
            if (!this.authorized) {
                upperPanel.setManaged(true);
                bottomPanel.setManaged(false);
                upperPanel.setVisible(true);
                bottomPanel.setVisible(false);
                clientsPanel.setManaged(false);
                clientsPanel.setVisible(false);
                Platform.runLater(() -> Main.mainStage.setTitle("JavaFX Client"));
            } else {
                upperPanel.setManaged(false);
                bottomPanel.setManaged(true);
                upperPanel.setVisible(false);
                bottomPanel.setVisible(true);
                clientsPanel.setManaged(true);
                clientsPanel.setVisible(true);
                jta.clear();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        timeOUT = false;
        setAuthorized(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(120000);
                    if (!authorized) {
                        timeOUT = true;
                        try {
                            socket.close();
                        } catch (IOException e) {
                        } finally {
                            showTimeOUT();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void start() {
        try {
            setAuthorized(false);
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            obsClients = FXCollections.observableArrayList();
            clientsList.setItems(obsClients);
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/authok")) {
                            setAuthorized(true);

                            myNick = str.split("\\s")[1];
                            Platform.runLater(() -> Main.mainStage.setTitle("JavaFX Client: " + myNick));
                            break;
                        }
                        jta.appendText(str + "\n");
                    }
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }
                            if (str.startsWith("/clients ")) {
                                String[] s = str.split("\\s");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        clientsList.getItems().clear();
                                        for (int i = 1; i < s.length; i++) {
                                            clientsList.getItems().add(s[i]);
                                        }
                                    }
                                });
                            }
                            if (str.startsWith("/yournickis")) {
                                myNick = str.split("\\s")[1];
                                Platform.runLater(() -> Main.mainStage.setTitle("JavaFX Client: " + myNick));
                            }
                        } else {
                            jta.appendText(str + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        setAuthorized(false);
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
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
        if (socket == null || socket.isClosed())
            start();
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passField.getText());
            loginField.clear();
            passField.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            jtf.setText("/w " + clientsList.getSelectionModel().getSelectedItem().toString() + " ");
            jtf.requestFocus();
            jtf.selectEnd();
        }
    }

    public void showTimeOUT(){
        jta.appendText("Время подключения истекло");
        upperPanel.setManaged(false);
        upperPanel.setVisible(false);
        Platform.runLater(() -> Main.mainStage.setTitle("Время подключения истекло"));

    }
}
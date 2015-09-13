/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Patrik Falk
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.swehacker.serialmonitor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SerialMonitor extends Application implements Initializable {

    private ObservableList<String> portNames = FXCollections.observableArrayList(SerialPortList.getPortNames());
    private ObservableList<String> baudrates = FXCollections.observableArrayList("110", "300", "600", "1200", "2400", "4800", "9600", "14400", "19200", "28800", "38400", "56000", "57600", "115200");
    private ObservableList<String> databitList = FXCollections.observableArrayList("5", "6", "7", "8");
    private ObservableList<String> stopbitList = FXCollections.observableArrayList("1", "2", "1.5");
    private ObservableList<String> paritylist = FXCollections.observableArrayList("NONE", "ODD", "EVEN", "MARK", "SPACE");

    @FXML
    private ChoiceBox port;
    @FXML
    private ChoiceBox baudrate;
    @FXML
    private ChoiceBox parity;
    @FXML
    private ChoiceBox stopbits;
    @FXML
    private ChoiceBox databits;
    @FXML
    private TextArea response;
    @FXML
    private TextField send;
    @FXML
    private Button btnSerialConnect;
    @FXML
    private Button btnSerialSend;
    @FXML
    private CheckBox crCheckBox;
    @FXML
    private CheckBox lfCheckBox;

    private SerialPort serialPort;

    private List<String> commandHistory = new ArrayList<>(1000);
    private int commandIndex = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        port.setItems(portNames);
        if (!portNames.isEmpty()) {
            port.setValue(portNames.get(0));
        }

        baudrate.setItems(baudrates);
        baudrate.setValue("9600");

        databits.setItems(databitList);
        databits.setValue(databitList.get(3));

        stopbits.setItems(stopbitList);
        stopbits.setValue(stopbitList.get(0));

        parity.setItems(paritylist);
        parity.setValue(paritylist.get(0));

        if (port.getValue() == null) {
            btnSerialConnect.setDisable(true);
        }

        btnSerialSend.setDisable(true);
        send.setEditable(false);
        HBox.setHgrow(send, Priority.ALWAYS);
        VBox.setVgrow(response, Priority.ALWAYS);

        send.setOnKeyReleased(event -> {
            if (KeyCode.UP == event.getCode() && !commandHistory.isEmpty()) {
                send.setText(commandHistory.get(commandIndex));
                if (commandIndex > 0) {
                    commandIndex--;
                }
            }

            if (KeyCode.DOWN == event.getCode() && !commandHistory.isEmpty()) {
                if (commandIndex < (commandHistory.size() - 1)) {
                    send.setText(commandHistory.get(++commandIndex));
                } else {
                    send.setText("");
                }

            }

            if (KeyCode.ENTER == event.getCode()) {
                commandIndex = commandHistory.size() - 1;
            }
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Serial Monitor");
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));

        Scene scene = new Scene(root, 600, 800);
        scene.getStylesheets().add(getClass().getResource("/stylesheets/main.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void serialConnect() {
        if (serialPort == null || !serialPort.isOpened()) {
            closeSerialPort();
            openSerialPort();
            btnSerialConnect.setText("Disconnect");
        } else {
            serialCloseAction();
            btnSerialConnect.setText("Connect");
        }
    }

    public void serialCloseAction() {
        closeSerialPort();
        btnSerialSend.setDisable(true);
        send.setEditable(false);
        baudrate.setDisable(false);
        stopbits.setDisable(false);
        databits.setDisable(false);
        parity.setDisable(false);
        port.setDisable(false);
    }

    public void serialSendAction() {
        try {
            response.appendText(send.getText() + "\n");
            commandHistory.add(send.getText());
            serialPort.writeString(send.getText());

            if (crCheckBox.isSelected()) {
                serialPort.writeString("\r");
            }

            if (lfCheckBox.isSelected()) {
                serialPort.writeString("\n");
            }

            send.clear();
        } catch (Throwable t) {
            response.appendText(t.getMessage() + "\n");
        }
    }

    private void openSerialPort() {
        serialPort = new SerialPort((String) port.getValue());
        try {
            serialPort.openPort();
            serialPort.setParams(Integer.parseInt((String) baudrate.getValue()), Databits.getBits((String) databits.getValue()), Stopbits.getBits((String) stopbits.getValue()), Parity.valueOf((String) parity.getValue()).getParity());//Set params
            serialPort.addEventListener(event -> {
                if (event.isRXCHAR()) {
                    try {
                        byte buffer[] = serialPort.readBytes();
                        Platform.runLater(() -> {
                            response.appendText(new String(buffer));
                        });
                    } catch (SerialPortException ex) {
                        response.appendText(ex.getMessage() + "\n");
                    }
                }
            });

            btnSerialSend.setDisable(false);
            send.setEditable(true);
            baudrate.setDisable(true);
            stopbits.setDisable(true);
            databits.setDisable(true);
            parity.setDisable(true);
            port.setDisable(true);
        } catch (SerialPortException ex) {
            response.appendText(ex.getMessage() + "\n");
        }
    }

    private void closeSerialPort() {
        if (serialPort != null && serialPort.isOpened()) {
            try {
                serialPort.closePort();
            } catch (SerialPortException ex) {
                ex.printStackTrace();
            }
        }

        serialPort = null;
    }

    public static void main(String... args) {
        launch(args);
    }
}

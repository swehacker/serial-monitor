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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jssc.*;

import java.net.URL;
import java.util.ResourceBundle;

public class SerialMonitor extends Application implements Initializable {

    private ObservableList<String> baudrates = FXCollections.observableArrayList("110", "300", "600", "1200", "2400", "4800", "9600", "14400", "19200", "28800", "38400", "56000", "57600", "115200");
    private ObservableList<String> portNames = FXCollections.observableArrayList(SerialPortList.getPortNames());

    @FXML
    private ChoiceBox port;
    @FXML
    private ChoiceBox baudrate;
    @FXML
    private TextArea response;
    @FXML
    private TextField send;
    @FXML
    private Button btnSerialConnect;
    @FXML
    private Button btnSerialClose;
    @FXML
    private Button btnSerialSend;
    @FXML
    private CheckBox crCheckBox;
    @FXML
    private CheckBox lfCheckBox;

    private SerialPort serialPort;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        port.setItems(portNames);
        if ( !portNames.isEmpty() ) {
            port.setValue(portNames.get(0));
        }

        baudrate.setItems(baudrates);
        baudrate.setValue("9600");

        if (port.getValue() == null) {
            btnSerialConnect.setDisable(true);
        }
        btnSerialClose.setDisable(true);
        btnSerialSend.setDisable(true);
        send.setEditable(false);
        HBox.setHgrow(send, Priority.ALWAYS);
        VBox.setVgrow(response, Priority.ALWAYS);
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
        closeSerialPort();
        openSerialPort();
    }

    public void serialClose() {
        closeSerialPort();
        btnSerialConnect.setDisable(false);
        btnSerialClose.setDisable(true);
        btnSerialSend.setDisable(true);
        send.setEditable(false);
        baudrate.setDisable(false);
        port.setDisable(false);
    }

    public void serialSend() {
        try {
            response.appendText(send.getText() + "\n");
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

    public void baudrateSelected() {
        if ( baudrate.getValue() == null ) {
            btnSerialConnect.setDisable(true);
        } else {
            btnSerialConnect.setDisable(false);
        }
    }

    private void openSerialPort() {
        serialPort = new SerialPort((String)port.getValue());
        try {
            serialPort.openPort();
            serialPort.setParams(Integer.parseInt((String)baudrate.getValue()), 8, 1, 0);//Set params
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;
            serialPort.setEventsMask(mask);
            serialPort.addEventListener(event -> {
                StringBuilder inputBuffer = new StringBuilder();
                if (event.isRXCHAR() && event.getEventValue() > 0) {
                    try {
                        byte buffer[] = serialPort.readBytes();
                        for (byte b : buffer) {
                            inputBuffer.append((char) b);
                        }

                        response.appendText(inputBuffer.toString());
                    } catch (SerialPortException ex) {
                        response.appendText(ex.getMessage() + "\n");
                    }
                }
            });

            btnSerialConnect.setDisable(true);
            btnSerialClose.setDisable(false);
            btnSerialSend.setDisable(false);
            send.setEditable(true);
            baudrate.setDisable(true);
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

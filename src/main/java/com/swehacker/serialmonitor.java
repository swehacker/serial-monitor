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

package com.swehacker;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import jssc.SerialPortList;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        port.setItems(portNames);
        baudrate.setItems(baudrates);
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

    public void handleConnect() {
        response.appendText("Hepp");
    }

    public static void main(String... args) {
        launch(args);
    }

}

package org.example.Main;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.DataObjects.Product;
import org.example.Pages.BasePage;
import org.example.Pages.LoginPage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Client extends Application {

    //session variables
    public static Map<String, Object> sessionVariables = new HashMap<>();
    public static List<Product> cartProducts = new ArrayList<>();

    //networking
    public static Socket s;
    public static PrintWriter packetSender;


    //GUI
    WebView webView = new WebView();
    WebEngine webEngine = webView.getEngine();
    public static String htmlPath, imagePath;

    public static BasePage currentPage;

    public static void sendRequest(String request){
        System.out.println("sending request");
        packetSender.println(request);
    }

    @Override
    public void start(Stage stage) {

        //disable right-click
        webView.setContextMenuEnabled(false);

        //htmlPath = getClass().getResource("/GUI/HTML").toExternalForm();
        //htmlPath = "file:///C:/Users/Danel/Desktop/Client_jar/GUI/HTML/";
        htmlPath = "file:///" + new File("").getAbsolutePath() + "\\GUI\\HTML\\";


        currentPage = new LoginPage(webView);

        // Load the HTML file into the WebView from the resources folder
        System.out.println(htmlPath + "LoginPage.html");
        webEngine.load(htmlPath + "LoginPage.html");

        //configure page after loading
        Worker<Void> worker = webEngine.getLoadWorker();
        worker.stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                currentPage.configure();
            }

        });


        // Add the WebView to a StackPane layout
        StackPane root = new StackPane();
        root.getChildren().add(webView);

        // Set the Scene to display the StackPane layout
        Scene scene = new Scene(root, 1280, 720);
        stage.setScene(scene);

        //start maximized
        stage.setMaximized(true);

        // Show the Stage
        stage.show();


        }

    public static void main(String[] args) {
        try {
            s = new Socket("localhost", 1234);
            packetSender = new PrintWriter(s.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        launch(args);

        //when closing the application, close the socket
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
package org.example.Pages;

import javafx.scene.web.WebView;
import org.example.Main.Client;
import org.example.Utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLInputElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

public class LoginPage extends BasePage {


    public LoginPage(WebView webView) {
        super(webView);
    }

    @Override
    public void configure() {
        Document document = webEngine.getDocument();

        //listener for login button
        HTMLInputElement submitButton = (HTMLInputElement) document.getElementById("submit");
        EventTarget submitButtonEvent = (EventTarget) submitButton;
        submitButtonEvent.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                String username = getInputValue("id", "username");
                String password = getInputValue("id", "password");

                try {
                    password = Utils.encryptPassword(password);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                // Add the values to the map
                pageVariables.put("username", username);
                pageVariables.put("password", password);
                // Send the request to the server
                if(!username.isEmpty() && !password.isEmpty()){
                    Client.sendRequest("login|" + username + "|" + password);

                    String response = readResponse();
                    System.out.println(response);
                    if (response.contains("User found: 1")) {
                        Client.sessionVariables.put("username", username);
                        Client.sessionVariables.put("email", response.split("\\|")[4]);
                        Client.sessionVariables.put("phone", response.split("\\|")[6]);
                        Client.sessionVariables.put("admin", response.split("\\|")[7]);
                        Client.sessionVariables.put("city", response.split("\\|")[10]);
                        Client.sessionVariables.put("street", response.split("\\|")[11]);
                        Client.sessionVariables.put("zipcode", response.split("\\|")[12]);
                        setPage(new HomePage(webView));
                    }
                    else if(response.contains("User found: 0")){
                        displayElement(getElement("id", "loginError"), true);
                    }

                }
            }
        }, false);

        //listener for register button
        Element registerButtonElement = document.getElementById("register");
        EventTarget registerButton = (EventTarget) registerButtonElement;
        registerButton.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                setPage(new RegisterPage(webView));
            }
        }, false);

    }
}

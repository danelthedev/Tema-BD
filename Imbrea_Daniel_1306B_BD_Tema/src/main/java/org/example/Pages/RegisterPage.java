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

public class RegisterPage extends BasePage {

    InputStreamReader stream;
    BufferedReader in;

    public RegisterPage(WebView webView) {
        super(webView);
    }

    @Override
    public void configure() {
        Document document = webEngine.getDocument();

        //listener for register button
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

                String email = getInputValue("id", "email");
                String phone = getInputValue("id", "phone");

                String city = getInputValue("id", "city");
                String street = getInputValue("id", "street");
                String zipcode = getInputValue("id", "zipcode");

                // Add the values to the map
                Client.sessionVariables.put("username", username);
                Client.sessionVariables.put("password", password);
                Client.sessionVariables.put("email", email);
                Client.sessionVariables.put("phone", phone);

                Client.sessionVariables.put("city", city);
                Client.sessionVariables.put("street", street);
                Client.sessionVariables.put("zipcode", zipcode);

                Client.sessionVariables.put("admin", "0");

                // Send the request to the server
                String response = "";
                if(!username.isEmpty() && !password.isEmpty() && !email.isEmpty() && !phone.isEmpty() && !city.isEmpty() && !street.isEmpty() && !zipcode.isEmpty()){
                    String request = "register|" + username + "|" + password + "|" + email + "|" + phone + "|" + city + "|" + street + "|" + zipcode;

                    Client.sendRequest(request);
                    response = readResponse();
                    System.out.println("Response: " + response);
                }
                else{
                    displayElement(getElement("id", "requiredFieldsError"), true);
                }

                if (response.contains("User registered successfully")) {
                    Client.sessionVariables.put("username", username);
                    setPage(new HomePage(webView));
                }
                else if(!response.isEmpty()){
                    List<Element> errors = List.of(getElement("id", "requiredFieldsError"), getElement("id", "phoneError"), getElement("id", "nameError"), getElement("id", "passwordError"), getElement("id", "zipcodeError"), getElement("id", "emailError"));

                    for(Element error : errors)
                        displayElement(error, false);

                    if(response.contains("PHONE_NUMBER"))
                        displayElement(getElement("id", "phoneError"), true);
                    else if(response.contains("EMAIL"))
                        displayElement(getElement("id", "emailError"), true);
                    else if(response.contains("NAME"))
                        displayElement(getElement("id", "nameError"), true);
                    else if(response.contains("PASSWORD"))
                        displayElement(getElement("id", "passwordError"), true);
                    else
                        displayElement(getElement("id", "zipcodeError"), true);


                }

            }
        }, false);

        //listener for login button
        Element loginButtonElement = document.getElementById("login");
        EventTarget loginButton = (EventTarget) loginButtonElement;
        loginButton.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                setPage(new LoginPage(webView));
            }
        }, false);

    }

}

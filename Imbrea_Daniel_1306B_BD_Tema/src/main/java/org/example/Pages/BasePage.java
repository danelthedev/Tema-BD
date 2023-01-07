package org.example.Pages;

import javafx.application.Application;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.example.Main.Client;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePage {
    WebView webView;
    WebEngine webEngine;

    //networking (read packages)
    InputStreamReader stream;
    BufferedReader packetReader;

    Map<String, Object> pageVariables = new HashMap<>();

    public void setPage(BasePage page){
        Client.currentPage = page;
        //load the html file from resources/html
        System.out.println("Setting page to " + page.getClass().getSimpleName());
        webEngine.load(Client.htmlPath + page.getClass().getSimpleName() + ".html");
    }

    public BasePage(WebView webView) {
        this.webView = webView;
        this.webEngine = webView.getEngine();
    }

    public abstract void configure();

    String readResponse(){
        try {
            stream = new InputStreamReader(Client.s.getInputStream());
            packetReader = new BufferedReader(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String response = "";
        int chr = ' ';
        try {
            //read the entire response from the buffer using a while loop
            while (chr != 13) {
                chr = packetReader.read();
                response += String.valueOf((char) chr);
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    String getInputValue(String by, String value) {
        switch (by) {
            case "id":
                return webEngine.executeScript("document.getElementById('" + value + "').value").toString();
            case "class":
                return webEngine.executeScript("document.getElementsByClassName('" + value + "')[0].value").toString();
            default:
                return null;
        }

    }

    Element getElement(String by, String value){
        switch (by) {
            case "id":
                return (Element) webEngine.executeScript("document.getElementById('" + value + "')");
            case "class":
                return (Element) webEngine.executeScript("document.getElementsByClassName('" + value + "')[0]");
            default:
                return null;
        }
    }

    void configureUserMenu(){
        Document document = webEngine.getDocument();

        //listener for user-button button
        Element homeButtonElement = document.getElementById("storeTitle");
        EventTarget homeButton = (EventTarget) homeButtonElement;
        homeButton.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                setPage(new HomePage(webView));
            }
        }, false);

        //add event listener for cart button
        Element cartButtonElement = document.getElementById("Cart");
        EventTarget cartButton = (EventTarget) cartButtonElement;
        cartButton.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event event) {
                //go to cart page
                setPage(new CartPage(webView));
            }
        }, false);

        //add event listener for Account Information button
        Element accountInfoButtonElement = document.getElementById("Account Information");
        EventTarget accountInfoButton = (EventTarget) accountInfoButtonElement;
        accountInfoButton.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event event) {
                //go to account information page
                setPage(new AccountInformationPage(webView));
            }
        }, false);

        //add event listener for logout button
        Element logoutButtonElement = document.getElementById("Logout");
        EventTarget logoutButton = (EventTarget) logoutButtonElement;
        logoutButton.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event event) {
                //go to login page
                Client.sessionVariables.clear();
                setPage(new LoginPage(webView));
            }
        }, false);

        //unhide admin tools button if user is admin
        if(Client.sessionVariables.get("admin").equals("1")){
            Element adminToolsButtonElement = document.getElementById("Admin Tools");
            adminToolsButtonElement.setAttribute("style", "display: block;");
        }

        //add event listener for admin tools button
        Element adminToolsButtonElement = document.getElementById("Admin Tools");
        EventTarget adminToolsButton = (EventTarget) adminToolsButtonElement;
        adminToolsButton.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event event) {
                //go to admin tools page
                setPage(new AdminToolsPage(webView));
            }
        }, false);

    }

    void displayElement(Element element, Boolean show){
        if(show){
            element.setAttribute("style", "display: block");
        }else{
            element.setAttribute("style", "display: none");
        }
    }

}

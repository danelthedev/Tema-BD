package org.example.Pages;

import javafx.scene.web.WebView;
import org.example.DataObjects.Product;
import org.example.Main.Client;
import org.example.Utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.util.List;

public class CartPage extends BasePage{

    public CartPage(WebView webView) {
        super(webView);
    }

    public void addCartItemsToPage(){
        Document document = webEngine.getDocument();
        Element cartItems = document.getElementById("CartContainer");

        //update the cart items information using requests
        for(Product item : Client.cartProducts){
            Client.sendRequest("getProductSpecsById|" + item.getActualProductId());
            String response = readResponse();
            item.setProductName(response.split("\\|")[0]);
            item.setPrice(Float.parseFloat(response.split("\\|")[1]));
        }

        //go through the cart items and add them to the page in divs that contain the product name and price as a pre
        for (Product item : Client.cartProducts) {
            Element div = document.createElement("div");
            div.setAttribute("class", "CartItem");
            div.setAttribute("id", item.getProductName());
            Element pre = document.createElement("pre");
            pre.setTextContent(item.getProductName() + "\nPrice: " + item.getPrice() + " $");
            div.appendChild(pre);
            cartItems.appendChild(div);
        }
    }

    @Override
    public void configure() {
        Document document = webEngine.getDocument();
        //execute the javascript function to replace the username
        webEngine.executeScript(Utils.replaceVariable("username"));

        addCartItemsToPage();

        configureUserMenu();

        //listener for finalizeOrderButton button
        Element finalizeOrderButtonElement = document.getElementById("finalizeOrderButton");
        EventTarget finalizeOrderButton = (EventTarget) finalizeOrderButtonElement;
        finalizeOrderButton.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                String request = "finalizeOrder|" + Client.sessionVariables.get("username") + "|";
                for(Product item : Client.cartProducts)
                    request += item.getActualProductId() + ",";

                if(request.endsWith(",")) {
                    Client.sendRequest(request);
                    Client.cartProducts.clear();
                    setPage(new HomePage(webView));
                }
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

    }
}

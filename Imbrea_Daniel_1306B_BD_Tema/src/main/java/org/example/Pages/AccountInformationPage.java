package org.example.Pages;

import javafx.scene.web.WebView;
import org.example.Utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

public class AccountInformationPage extends BasePage {
    public AccountInformationPage(WebView webView) {
        super(webView);
    }

    @Override
    public void configure() {
        configureUserMenu();
        webEngine.executeScript(Utils.replaceVariable("username"));
        webEngine.executeScript(Utils.replaceVariable("email"));
        webEngine.executeScript(Utils.replaceVariable("phone"));
        webEngine.executeScript(Utils.replaceVariable("city"));
        webEngine.executeScript(Utils.replaceVariable("street"));
        webEngine.executeScript(Utils.replaceVariable("zipcode"));



    }

}

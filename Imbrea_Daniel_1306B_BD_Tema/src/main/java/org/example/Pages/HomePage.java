package org.example.Pages;

import javafx.scene.control.ChoiceBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.example.DataObjects.Chair;
import org.example.DataObjects.NightStand;
import org.example.DataObjects.Product;
import org.example.DataObjects.Table;
import org.example.Main.Client;
import org.example.Utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends BasePage{

    String tableImg = "https://img.lovepik.com/element/40020/3142.png_300.png";
    String chairImg = "https://previews.123rf.com/images/zirka/zirka1108/zirka110800061/10224948-color-drawing-a-piece-of-furniture-chair.jpg";
    String nightStandImg = "https://i.pinimg.com/474x/40/6a/33/406a339fc8b9e64aaaef163be45d1952.jpg";

    public HomePage(WebView webView) {
        super(webView);
    }

    public void configureAddToCartButtons(){
        Document document = webEngine.getDocument();
        //get count of elements with class productCell
        int productCells = (int) webEngine.executeScript(Utils.getCountOfElements("class", "productCell"));
        //add event listener for all add to cart buttons
        for (int i = 0; i < productCells; i++) {
            Element addToCartButtonElement = document.getElementById("addToCartButton" + i);
            EventTarget addToCartButton = (EventTarget) addToCartButtonElement;
            addToCartButton.addEventListener("click", new EventListener() {
                @Override
                public void handleEvent(Event event) {
                    Element productElement = (Element) addToCartButtonElement.getParentNode();
                    String productName = productElement.getChildNodes().item(0).getTextContent();
                    String productPrice = productElement.getChildNodes().item(2).getTextContent();
                    float actualPrice = Float.parseFloat(productPrice.substring(7, productPrice.length() - 2));

                    //change text of button to say "added to cart" and disable clicking on it
                    if(addToCartButtonElement.getTextContent().equals("Added to cart")) {
                        //remove product from cart
                        for (Product product : Client.cartProducts) {
                            //get product id using request
                            String request = "getProductId|" + productName;
                            Client.sendRequest(request);
                            String response = readResponse();
                            response = response.replaceAll("[^0-9]", "");
                            Integer productId = Integer.parseInt(response);

                            if(product.getActualProductId() == productId){
                                System.out.println("removing product");
                                Client.cartProducts.remove(product);
                                break;
                            }
                        }
                        addToCartButtonElement.setTextContent("Add to cart");
                        addToCartButtonElement.setAttribute("class", "addToCartButton");
                    }
                    else {
                        //get product id using request
                        Client.sendRequest("getProductId|" + productName);
                        String productId = readResponse();
                        productId = productId.replaceAll("[^0-9]", "");
                        System.out.println("Product id = " + productId);
                        Client.cartProducts.add(new Product(productId, productName, actualPrice));
                        addToCartButtonElement.setTextContent("Added to cart");
                        addToCartButtonElement.setAttribute("class", "addedToCart");
                    }
                    System.out.println(Client.cartProducts);
                }
            }, false);
        }
    }

    public void addProductsToPage(Product[] products, Element table){
        //add products to the page
        Document document = webEngine.getDocument();

        for (int i = 0; i < products.length; i++) {
            if (i % 4 == 0) {
                Element row = document.createElement("tr");
                table.appendChild(row);
            }
            Element cell = document.createElement("td");
            cell.setAttribute("class", "productCell");
            cell.setAttribute("id", "productCell" + i);

            //create elements for product that contains the product name, product type and price
            //add the product name
            Element productName = document.createElement("h3");
            productName.setAttribute("class", "productName");
            productName.setTextContent(products[i].getProductName());
            cell.appendChild(productName);
            //add image element
            Element image = document.createElement("img");
            switch (products[i].getProductType()) {
                case "table":
                    image.setAttribute("src", tableImg);
                    break;
                case "chair":
                    image.setAttribute("src", chairImg);
                    break;
                case "night_stand":
                    image.setAttribute("src", nightStandImg);
                    break;
            }
            image.setAttribute("class", "productImage");
            cell.appendChild(image);
            //add the product price
            Element productPrice = document.createElement("p");
            productPrice.setAttribute("class", "productPrice");
            productPrice.setTextContent("Price: " + products[i].getPrice() + " $");
            cell.appendChild(productPrice);
            //add a p with text "Product specifications"
            Element productSpecs = document.createElement("p");
            productSpecs.setTextContent("Specifications:");
            cell.appendChild(productSpecs);
            //add product specifications
            switch (products[i].getProductType()) {
                case "table":
                    Element tableSpecs = document.createElement("pre");
                    tableSpecs.setAttribute("class", "productSpecs");
                    Table tableProduct = (Table) products[i];

                    tableSpecs.setTextContent(  "Dimensions: " + tableProduct.getDimensions() +
                            "\nFeet count: " + tableProduct.getFeetNo());
                    cell.appendChild(tableSpecs);
                    break;
                case "chair":
                    Element chairSpecs = document.createElement("pre");
                    chairSpecs.setAttribute("class", "productSpecs");
                    Chair chairProduct = (Chair) products[i];

                    chairSpecs.setTextContent(  "Dimensions: " + chairProduct.getHidraulic() +
                            "\nLombar support: " + chairProduct.getLombarSupport() +
                            "\nWheels: " + chairProduct.getWheels());
                    cell.appendChild(chairSpecs);
                    break;
                case "night_stand":
                    Element nightStandSpecs = document.createElement("pre");
                    nightStandSpecs.setAttribute("class", "productSpecs");
                    NightStand nightStandProduct = (NightStand) products[i];

                    nightStandSpecs.setTextContent(  "\nHeight: " + nightStandProduct.getHeight() +
                            "\nDrawers count: " + nightStandProduct.getDrawersNo());
                    cell.appendChild(nightStandSpecs);
                    break;
            }

            //create an add to cart button
            Element addToCartButton = document.createElement("button");

            //if the name of the product is in the cart, change the text of the button to "added to cart"

            addToCartButton.setAttribute("class", "addToCartButton");
            addToCartButton.setTextContent("Add to cart");

            for (Product product : Client.cartProducts) {
                //get product id of item from cart and product on page using request
                String productId = String.valueOf(product.getActualProductId());
                productId = productId.replaceAll("[^0-9]", "");
                Client.sendRequest("getProductId|" + products[i].getProductName());
                String productId2 = readResponse();
                productId2 = productId2.replaceAll("[^0-9]", "");

                if(productId.equals(productId2)){
                    addToCartButton.setTextContent("Added to cart");
                    addToCartButton.setAttribute("class", "addedToCart");
                    break;
                }
            }



            addToCartButton.setAttribute("id", "addToCartButton" + i);
            cell.appendChild(addToCartButton);

            table.getLastChild().appendChild(cell);
        }

        // add empty cells to fill the last row
        int remainingCells = 4 - (products.length % 4);
        if (remainingCells < 4) {
            for (int i = 0; i < remainingCells; i++) {
                Element cell = webEngine.getDocument().createElement("td");
                table.getLastChild().appendChild(cell);
            }
        }

        //add event listener for all add to cart buttons
        configureAddToCartButtons();
    }

    public Product[] getProductSpecs(Product[] products){
        for(Product product : products){
            String request = "getProductSpecs|" + product.getProductType() + "s|" + product.getActualProductId();
            Client.sendRequest(request);
            String response = readResponse();
            List<String> specs = Utils.deserializeProductSpec(response, product.getProductType());
            System.out.println(response + "\n" + specs + "\n\n");

            switch (product.getProductType()){
                case "table":
                    //convert product to table
                    ((Table) product).setWidth(Integer.parseInt(specs.get(0)));
                    ((Table) product).setLength(Integer.parseInt(specs.get(1)));
                    ((Table) product).setHeight(Integer.parseInt(specs.get(2)));
                    ((Table) product).setFeetNo(Integer.parseInt(specs.get(3)));
                    break;
                case "chair":
                    //convert product to chair
                    ((Chair) product).setHidraulic(specs.get(0).equals("1") ? Boolean.TRUE : Boolean.FALSE);
                    ((Chair) product).setLombarSupport(specs.get(1).equals("1") ? Boolean.TRUE : Boolean.FALSE);
                    ((Chair) product).setWheels(specs.get(1).equals("1") ? Boolean.TRUE : Boolean.FALSE);
                    break;
                case "night_stand":
                    //convert product to nightstand
                    ((NightStand) product).setDrawersNo(Integer.parseInt(specs.get(0)));
                    ((NightStand) product).setHeight(Integer.parseInt(specs.get(1)));
                    break;
            }

        }
        return products;
    }

    public void initHomePageContent(){
        //execute the javascript function to replace the username
        webEngine.executeScript(Utils.replaceVariable("username"));

        //get products
        Client.sendRequest("getAllProducts");
        String response = readResponse();
        Product[] products = Utils.deserializeProducts(response);
        for(Product product : products){
            System.out.println(product);
        }
        products = getProductSpecs(products);

        System.out.println("Product specs:\n\n");
        //go through the products and send requests to get product specs
        System.out.println(Client.cartProducts);
        addProductsToPage(products, (Element) webEngine.getDocument().getElementById("productsTable"));

    }

    @Override
    public void configure() {
        Document document = webEngine.getDocument();

        Element table = document.createElement("table");
        table.setAttribute("id", "productsTable");
        getElement("id", "MainContent").appendChild(table);

        initHomePageContent();

        configureUserMenu();

        //add event listener for apply filter button
        Element applyFilterButtonElement = document.getElementById("apply-filters-button");
        EventTarget applyFilterButton = (EventTarget) applyFilterButtonElement;
        applyFilterButton.addEventListener("click", new EventListener() {
        @Override
        public void handleEvent(Event event) {
            String filterRequest = "getProducts";

            //remove all children of the table
            Element table = document.getElementById("productsTable");
            Utils.clearChildren(table);
            //get sort method
            String sortMethod = (String) webEngine.executeScript(Utils.getSelectedValue("id", "sortMethod"));
            filterRequest += "|sortMethod=" + sortMethod;

            //get product type
            //get table checkbox value
            Boolean getTables = (Boolean) webEngine.executeScript(Utils.getCheckboxValue("id", "tables-checkbox"));
            Boolean getChairs = (Boolean) webEngine.executeScript(Utils.getCheckboxValue("id", "chairs-checkbox"));
            Boolean getNightStands = (Boolean) webEngine.executeScript(Utils.getCheckboxValue("id", "night-stands-checkbox"));
            if (getTables || getChairs || getNightStands) {
                filterRequest += "|productType=";
                if (getTables) {
                    filterRequest += "tables,";
                }
                if (getChairs) {
                    filterRequest += "chairs,";
                }
                if (getNightStands) {
                    filterRequest += "night_stands,";
                }
            }

            //get string from search bar
            String searchString = getInputValue("id", "searchBar");
            if (!searchString.equals("")) {
                filterRequest += "|searchString=" + searchString;
            }

            Client.sendRequest(filterRequest);
            String response = readResponse();
            System.out.println(response);
            Product[] products = new Product[Utils.deserializeProducts(response).length];
            for(int i = 0; i < products.length; i++) {
                products[i] = Utils.deserializeProducts(response)[i];
                System.out.println(products[i]);
            }
            products = getProductSpecs(products);
            addProductsToPage(products, table);

            }
        }, false);


    }

}

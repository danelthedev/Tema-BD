package org.example.Pages;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.DataObjects.Chair;
import org.example.DataObjects.NightStand;
import org.example.DataObjects.Product;
import org.example.DataObjects.Table;
import org.example.Main.Client;
import org.example.Utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.util.ArrayList;
import java.util.List;

public class AdminToolsPage extends BasePage{

    public AdminToolsPage(WebView webView) {
        super(webView);
    }

    public void configureDeleteButtons(){
        Document document = webEngine.getDocument();
        //get count of elements with class productCell
        int productCells = (int) webEngine.executeScript(Utils.getCountOfElements("class", "productCell"));
        //add event listener for all add to cart buttons
        for (int i = 0; i < productCells; i++) {
            Element deleteButtonElement = document.getElementById("deleteButton" + i);
            EventTarget deleteButton = (EventTarget) deleteButtonElement;
            deleteButton.addEventListener("click", new EventListener() {
                @Override
                public void handleEvent(Event event) {
                    Element productElement = (Element) deleteButtonElement.getParentNode();
                    String productName = productElement.getChildNodes().item(0).getTextContent();

                    Client.sendRequest("deleteProduct|" + productName);

                    String response = readResponse();

                    //refresh page using webEngine
                    webEngine.executeScript("location.reload()");

                    System.out.println(Client.cartProducts);
                }
            }, false);
        }
    }

    //create a pop-up containing a form to modify the product
    public void configureModifyButtons() {
        Document document = webEngine.getDocument();
        //get count of elements with class productCell
        int productCells = (int) webEngine.executeScript(Utils.getCountOfElements("class", "productCell"));
        //add event listener for all modify buttons
        for (int i = 0; i < productCells; i++) {
            Element modifyButtonElement = document.getElementById("modifyButton" + i);
            EventTarget modifyButton = (EventTarget) modifyButtonElement;
            modifyButton.addEventListener("click", new EventListener() {
                @Override
                public void handleEvent(Event event) {
                    Element productElement = (Element) modifyButtonElement.getParentNode();
                    String productName = productElement.getChildNodes().item(0).getTextContent();

                    //get the product type with a request
                    Client.sendRequest("getProductType|" + productName);
                    String response = readResponse();

                    //create a pop-up window
                    Stage window = new Stage();
                    window.initModality(Modality.APPLICATION_MODAL);
                    window.setTitle("Modify Product");
                    window.setMinWidth(250);

                    //create a form
                    VBox layout = new VBox(10);
                    layout.setAlignment(Pos.CENTER);
                    Label label = new Label("Modify " + productName);
                    TextField nameField = new TextField();
                    nameField.setPromptText("Name");
                    TextField priceField = new TextField();
                    priceField.setPromptText("Price");
                    //only allow numbers to be entered
                    priceField.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue.matches("\\d*")) {
                            priceField.setText(newValue.replaceAll("[^\\d]", ""));
                        }
                    });
                    TextField quantityField = new TextField();
                    quantityField.setPromptText("Quantity");
                    //only allow numbers to be entered
                    quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue.matches("\\d*")) {
                            quantityField.setText(newValue.replaceAll("[^\\d]", ""));
                        }
                    });

                    //add the fields depending on the product type
                    if(response.contains("chair")){
                        //checkboxes for hidraulic, lombar support, and wheels
                        CheckBox hidraulic = new CheckBox("Hidraulic");
                        CheckBox lombarSupport = new CheckBox("Lombar Support");
                        CheckBox wheels = new CheckBox("Wheels");

                        //dropdown for material using request
                        Client.sendRequest("getTable|materials");
                        String materialsResponse = readResponse();
                        //only keep odd indexes
                        List<String> materials = new ArrayList<>();
                        for (int i = 0; i < materialsResponse.split("\\|").length; i++) {
                            if(i % 2 != 0){
                                materials.add(materialsResponse.split("\\|")[i]);
                            }
                        }
                        ChoiceBox<String> materialChoiceBox = new ChoiceBox<>();
                        materialChoiceBox.getItems().addAll(materials);
                        materialChoiceBox.setValue(materials.get(0));

                        //create a button to submit the form
                        Button submitButton = new Button("Submit");
                        submitButton.setOnAction(e -> {
                            String request = "modifyProduct|chair|" + productName + "|";
                            //get the values from the fields
                            request += !nameField.getText().isEmpty() ? ",name=" + nameField.getText() : "";
                            request += !priceField.getText().isEmpty() ? ",price=" + priceField.getText() : "";
                            request += !quantityField.getText().isEmpty() ? ",quantity=" + quantityField.getText() : "";
                            request += !materialChoiceBox.getValue().isEmpty() ? ",material=" + materialChoiceBox.getValue() : "";
                            request += hidraulic.isSelected() ? ",hidraulic=1" : ",hidraulic=0";
                            request += lombarSupport.isSelected() ? ",lombarSupport=1" : ",lombarSupport=0";
                            request += wheels.isSelected() ? ",wheels=1" : ",wheels=0";

                            //send a request to modify the product
                            Client.sendRequest(request);
                            String result = readResponse();
                            if(result.contains("success")) {
                                //refresh page using webEngine
                                webEngine.executeScript("location.reload()");
                                window.close();
                            }
                        });

                        layout.getChildren().addAll(label, nameField, priceField, quantityField, hidraulic, lombarSupport, wheels, materialChoiceBox, submitButton);
                    }
                    else if(response.contains("night_stand")){
                        //text field for height and number of drawers that only accept numbers
                        TextField heightField = new TextField();
                        heightField.setPromptText("Height");

                        //make widthField only accept numbers
                        heightField.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (!newValue.matches("\\d*")) {
                                heightField.setText(newValue.replaceAll("[^\\d]", ""));
                            }
                        });

                        TextField drawersField = new TextField();
                        drawersField.setPromptText("Number of Drawers");

                        //make drawersField only accept numbers
                        drawersField.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (!newValue.matches("\\d*")) {
                                drawersField.setText(newValue.replaceAll("[^\\d]", ""));
                            }
                        });

                        //dropdown for material using request
                        Client.sendRequest("getTable|materials");
                        String materialsResponse = readResponse();
                        //only keep odd indexes
                        List<String> materials = new ArrayList<>();
                        for (int i = 0; i < materialsResponse.split("\\|").length; i++) {
                            if(i % 2 != 0){
                                materials.add(materialsResponse.split("\\|")[i]);
                            }
                        }
                        ChoiceBox<String> materialChoiceBox = new ChoiceBox<>();
                        materialChoiceBox.getItems().addAll(materials);
                        materialChoiceBox.setValue(materials.get(0));

                        //dropdown for shape using request
                        Client.sendRequest("getTable|shapes");
                        String shapesResponse = readResponse();
                        //only keep odd indexes
                        List<String> shapes = new ArrayList<>();
                        for (int i = 0; i < shapesResponse.split("\\|").length; i++) {
                            if(i % 2 != 0){
                                shapes.add(shapesResponse.split("\\|")[i]);
                            }
                        }
                        ChoiceBox<String> shapeChoiceBox = new ChoiceBox<>();
                        shapeChoiceBox.getItems().addAll(shapes);
                        shapeChoiceBox.setValue(shapes.get(0));


                        //create a button to submit the form
                        Button submitButton = new Button("Submit");
                        submitButton.setOnAction(e -> {
                            String request = "modifyProduct|night_stand|" + productName + "|";
                            //get the values from the fields
                            request += !nameField.getText().isEmpty() ? ",name=" + nameField.getText() : "";
                            request += !priceField.getText().isEmpty() ? ",price=" + priceField.getText() : "";
                            request += !quantityField.getText().isEmpty() ? ",quantity=" + quantityField.getText() : "";
                            request += !materialChoiceBox.getValue().isEmpty() ? ",material=" + materialChoiceBox.getValue() : "";
                            request += !heightField.getText().isEmpty() ? ",height=" + heightField.getText() : "";
                            request += !drawersField.getText().isEmpty() ? ",drawersNo=" + drawersField.getText() : "";
                            request += !shapeChoiceBox.getValue().isEmpty() ? ",shape=" + shapeChoiceBox.getValue() : "";

                            //send a request to modify the product
                            Client.sendRequest(request);
                            String result = readResponse();

                            if(result.contains("success")) {
                                //refresh page using webEngine
                                webEngine.executeScript("location.reload()");

                                window.close();
                            }
                        });

                        layout.getChildren().addAll(label, nameField, priceField, quantityField, heightField, drawersField, materialChoiceBox, shapeChoiceBox, submitButton);
                    }
                    else if(response.contains("table")){
                        //text field for width, length, height and feet number that only accept numbers
                        TextField widthField = new TextField();
                        widthField.setPromptText("Width");

                        //make widthField only accept numbers
                        widthField.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (!newValue.matches("\\d*")) {
                                widthField.setText(newValue.replaceAll("[^\\d]", ""));
                            }
                        });

                        TextField lengthField = new TextField();
                        lengthField.setPromptText("Length");

                        //make lengthField only accept numbers
                        lengthField.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (!newValue.matches("\\d*")) {
                                lengthField.setText(newValue.replaceAll("[^\\d]", ""));
                            }
                        });

                        TextField heightField = new TextField();
                        heightField.setPromptText("Length");

                        //make heightField only accept numbers
                        heightField.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (!newValue.matches("\\d*")) {
                                heightField.setText(newValue.replaceAll("[^\\d]", ""));
                            }
                        });

                        //feet number
                        TextField feetField = new TextField();
                        feetField.setPromptText("Number of Feet");

                        //make feetField only accept numbers
                        feetField.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (!newValue.matches("\\d*")) {
                                feetField.setText(newValue.replaceAll("[^\\d]", ""));
                            }
                        });

                        //dropdown for material using request
                        Client.sendRequest("getTable|materials");
                        String materialsResponse = readResponse();
                        //only keep odd indexes
                        List<String> materials = new ArrayList<>();
                        for (int i = 0; i < materialsResponse.split("\\|").length; i++) {
                            if(i % 2 != 0){
                                materials.add(materialsResponse.split("\\|")[i]);
                            }
                        }
                        ChoiceBox<String> materialChoiceBox = new ChoiceBox<>();
                        materialChoiceBox.getItems().addAll(materials);
                        materialChoiceBox.setValue(materials.get(0));

                        //dropdown for shape using request
                        Client.sendRequest("getTable|shapes");
                        String shapesResponse = readResponse();
                        //only keep odd indexes
                        List<String> shapes = new ArrayList<>();
                        for (int i = 0; i < shapesResponse.split("\\|").length; i++) {
                            if(i % 2 != 0){
                                shapes.add(shapesResponse.split("\\|")[i]);
                            }
                        }
                        ChoiceBox<String> shapeChoiceBox = new ChoiceBox<>();
                        shapeChoiceBox.getItems().addAll(shapes);
                        shapeChoiceBox.setValue(shapes.get(0));


                        //create a button to submit the form
                        Button submitButton = new Button("Submit");
                        submitButton.setOnAction(e -> {
                            String request = "modifyProduct|table|" + productName + "|";
                            //get the values from the fields
                            request += !nameField.getText().isEmpty() ? ",name=" + nameField.getText() : "";
                            request += !priceField.getText().isEmpty() ? ",price=" + priceField.getText() : "";
                            request += !quantityField.getText().isEmpty() ? ",quantity=" + quantityField.getText() : "";
                            request += !materialChoiceBox.getValue().isEmpty() ? ",material=" + materialChoiceBox.getValue() : "";
                            request += !widthField.getText().isEmpty() ? ",width=" + widthField.getText() : "";
                            request += !lengthField.getText().isEmpty() ? ",length=" + lengthField.getText() : "";
                            request += !heightField.getText().isEmpty() ? ",height=" + heightField.getText() : "";
                            request += !shapeChoiceBox.getValue().isEmpty() ? ",shape=" + shapeChoiceBox.getValue() : "";
                            request += !feetField.getText().isEmpty() ? ",feetNo=" + feetField.getText() : "";

                            //send a request to modify the product
                            Client.sendRequest(request);
                            String result = readResponse();

                            if(result.contains("success")) {
                                //refresh page using webEngine
                                webEngine.executeScript("location.reload()");
                                window.close();
                            }
                        });


                        layout.getChildren().addAll(label, nameField, priceField, quantityField, widthField, lengthField, heightField, materialChoiceBox, shapeChoiceBox, submitButton);
                    }

                    //show the pop-up window
                    Scene scene = new Scene(layout);
                    window.setScene(scene);
                    window.showAndWait();
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

            //create delete button
            Element addToCartButton = document.createElement("button");
            addToCartButton.setAttribute("class", "adminButton");
            addToCartButton.setTextContent("Delete");
            addToCartButton.setAttribute("id", "deleteButton" + i);

            //create modify button
            Element modifyButton = document.createElement("button");
            modifyButton.setAttribute("class", "adminButton");
            modifyButton.setTextContent("Modify");
            modifyButton.setAttribute("id", "modifyButton" + i);

            cell.appendChild(addToCartButton);
            cell.appendChild(modifyButton);

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
        //configureAddToCartButtons();
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

    @Override
    public void configure() {
        Document document = webEngine.getDocument();
        webEngine.executeScript(Utils.replaceVariable("username"));
        configureUserMenu();

        //add event listener for add chair button
        Element insertChairButtonElement = document.getElementById("add-chair");
        EventTarget insertChairButton = (EventTarget) insertChairButtonElement;
        insertChairButton.addEventListener("click", evt -> {

            //create a popup window that says bazinga and has a text field and a button
            //when the button is clicked, the text field value is sent to the server

            //create a popup window
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Add product");
            popup.setMinWidth(250);
            popup.setMinHeight(250);

            //chair name input
            Label nameLabel = new Label();
            nameLabel.setText("Chair name:");
            TextField nameField = new TextField();

            //chair price input
            Label priceLabel = new Label();
            priceLabel.setText("Chair price:");
            TextField priceField = new TextField();

            //make priceField only accept numbers
            priceField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    priceField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });


            //checkboxes for hidraulic, lombar support and wheels
            CheckBox hidraulicCheckBox = new CheckBox("Hidraulic");
            CheckBox lombarSupportCheckBox = new CheckBox("Lombar support");
            CheckBox wheelsCheckBox = new CheckBox("Wheels");

            //dropdown for material
            //send request for materials
            Client.sendRequest("getTable|materials");
            String materialsString = readResponse().replace("\n", "");
            //split string at | and only add odd indexes to a list using streams
            List<String> materials = new ArrayList<>();
            for (int i = 0; i < materialsString.split("\\|").length; i++) {
                if (i % 2 == 1) {
                    materials.add(materialsString.split("\\|")[i]);
                }
            }

            Label materialLabel = new Label();
            materialLabel.setText("Material:");
            ChoiceBox materialChoiceBox = new ChoiceBox();
            materialChoiceBox.getItems().addAll(materials);

            //create an "add product" button, that only adds the product if all fields are filled
            Button addButton = new Button("Add product");
            addButton.setOnAction(e -> {
                if (!(nameField.getText().isEmpty() || priceField.getText().isEmpty() || materialChoiceBox.getValue() == null)) {
                    Client.sendRequest("addProduct|chair|" + nameField.getText() + "|" + priceField.getText() + "|" + hidraulicCheckBox.isSelected() + "|" + lombarSupportCheckBox.isSelected() + "|" + wheelsCheckBox.isSelected() + "|" + materialChoiceBox.getValue());
                    String response = readResponse();

                    if(response.contains("success")){
                        popup.close();
                        webEngine.executeScript("location.reload()");
                    }
                }

            });

            //create a layout
            VBox layout = new VBox(10);

            layout.getChildren().addAll(nameLabel, nameField, priceLabel, priceField, hidraulicCheckBox, lombarSupportCheckBox, wheelsCheckBox, materialLabel, materialChoiceBox, addButton);

            layout.setAlignment(Pos.CENTER);

            //display the popup window
            Scene scene = new Scene(layout);
            popup.setScene(scene);
            popup.showAndWait();


        }, false);
        //add event listener for add table button
        Element insertTableButtonElement = document.getElementById("add-table");
        EventTarget insertTableButton = (EventTarget) insertTableButtonElement;
        insertTableButton.addEventListener("click", evt -> {

            //create a popup window
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Add product");
            popup.setMinWidth(250);
            popup.setMinHeight(250);

            //chair name input
            Label nameLabel = new Label();
            nameLabel.setText("Table name:");
            TextField nameField = new TextField();

            //chair price input
            Label priceLabel = new Label();
            priceLabel.setText("Table price:");
            TextField priceField = new TextField();

            //make priceField only accept numbers
            priceField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    priceField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

            //width input
            Label widthLabel = new Label();
            widthLabel.setText("Width:");
            TextField widthField = new TextField();

            //make widthField only accept numbers
            widthField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    widthField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

            //length input
            Label lengthLabel = new Label();
            lengthLabel.setText("Length:");
            TextField lengthField = new TextField();

            //make lengthField only accept numbers
            lengthField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    lengthField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

            //height input
            Label heightLabel = new Label();
            heightLabel.setText("Height:");
            TextField heightField = new TextField();

            //make heightField only accept numbers
            heightField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    heightField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

            //feet no input
            Label feetNoLabel = new Label();
            feetNoLabel.setText("Feet no:");
            TextField feetNoField = new TextField();

            //make feetNoField only accept numbers
            feetNoField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    feetNoField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

            //dropdown for material
            //send request for materials
            Client.sendRequest("getTable|materials");
            String materialsString = readResponse().replace("\n", "");
            //split string at | and only add odd indexes to a list using streams
            List<String> materials = new ArrayList<>();
            for (int i = 0; i < materialsString.split("\\|").length; i++) {
                if (i % 2 == 1) {
                    materials.add(materialsString.split("\\|")[i]);
                }
            }

            Label materialLabel = new Label();
            materialLabel.setText("Material:");
            ChoiceBox materialChoiceBox = new ChoiceBox();
            materialChoiceBox.getItems().addAll(materials);

            //dropdown for shape
            //send request for shapes
            Client.sendRequest("getTable|shapes");
            String shapesString = readResponse().replace("\n", "");
            //split string at | and only add odd indexes to a list using streams
            List<String> shapes = new ArrayList<>();
            for (int i = 0; i < shapesString.split("\\|").length; i++) {
                if (i % 2 == 1) {
                    shapes.add(shapesString.split("\\|")[i]);
                }
            }

            Label shapeLabel = new Label();
            shapeLabel.setText("Shape:");
            ChoiceBox shapeChoiceBox = new ChoiceBox();
            shapeChoiceBox.getItems().addAll(shapes);

            //create an "add product" button, that only adds the product if all fields are filled
            Button addButton = new Button("Add product");
            addButton.setOnAction(e -> {
                if (!(nameField.getText().isEmpty() || priceField.getText().isEmpty() || widthField.getText().isEmpty() || lengthField.getText().isEmpty() || heightField.getText().isEmpty() || feetNoField.getText().isEmpty() || materialChoiceBox.getValue() == null || shapeChoiceBox.getValue() == null)) {
                    Client.sendRequest("addProduct|table|" + nameField.getText() + "|" + priceField.getText() + "|" + widthField.getText() + "|" + lengthField.getText() + "|" + heightField.getText() + "|" + feetNoField.getText() + "|" + materialChoiceBox.getValue() + "|" + shapeChoiceBox.getValue());
                    String response = readResponse();

                    if(response.contains("success")){
                        popup.close();
                        webEngine.executeScript("location.reload()");
                    }
                }

            });

            //create a layout
            VBox layout = new VBox(10);

            layout.getChildren().addAll(nameLabel, nameField, priceLabel, priceField, widthLabel, widthField, lengthLabel, lengthField, heightLabel, heightField, feetNoLabel, feetNoField, materialLabel, materialChoiceBox, shapeLabel, shapeChoiceBox, addButton);

            layout.setAlignment(Pos.CENTER);

            //display the popup window
            Scene scene = new Scene(layout);
            popup.setScene(scene);
            popup.showAndWait();
        }, false);
        //add event listener for add night stand button
        Element insertNightStandButtonElement = document.getElementById("add-night-stand");
        EventTarget insertNightStandButton = (EventTarget) insertNightStandButtonElement;
        insertNightStandButton.addEventListener("click", evt -> {

            //create a popup window
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Add product");
            popup.setMinWidth(250);
            popup.setMinHeight(250);

            //night stand name input
            Label nameLabel = new Label();
            nameLabel.setText("Night stand name:");
            TextField nameField = new TextField();

            //chair price input
            Label priceLabel = new Label();
            priceLabel.setText("Night stand price:");
            TextField priceField = new TextField();

            //make priceField only accept numbers
            priceField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    priceField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

            //height input
            Label heightLabel = new Label();
            heightLabel.setText("Height:");
            TextField heightField = new TextField();

            //make heightField only accept numbers
            heightField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    heightField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

            //drawers no input
            Label drawersNoLabel = new Label();
            drawersNoLabel.setText("Drawers no:");
            TextField drawersNoField = new TextField();

            //make drawersNoField only accept numbers
            drawersNoField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    drawersNoField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

            //dropdown for material
            //send request for materials
            Client.sendRequest("getTable|materials");
            String materialsString = readResponse().replace("\n", "");
            //split string at | and only add odd indexes to a list using streams
            List<String> materials = new ArrayList<>();
            for (int i = 0; i < materialsString.split("\\|").length; i++) {
                if (i % 2 == 1) {
                    materials.add(materialsString.split("\\|")[i]);
                }
            }

            Label materialLabel = new Label();
            materialLabel.setText("Material:");
            ChoiceBox materialChoiceBox = new ChoiceBox();
            materialChoiceBox.getItems().addAll(materials);

            //dropdown for shape
            //send request for shapes
            Client.sendRequest("getTable|shapes");
            String shapesString = readResponse().replace("\n", "");
            //split string at | and only add odd indexes to a list using streams
            List<String> shapes = new ArrayList<>();
            for (int i = 0; i < shapesString.split("\\|").length; i++) {
                if (i % 2 == 1) {
                    shapes.add(shapesString.split("\\|")[i]);
                }
            }

            Label shapeLabel = new Label();
            shapeLabel.setText("Shape:");
            ChoiceBox shapeChoiceBox = new ChoiceBox();
            shapeChoiceBox.getItems().addAll(shapes);

            //create an "add product" button, that only adds the product if all fields are filled
            Button addButton = new Button("Add product");
            addButton.setOnAction(e -> {
                if (!(nameField.getText().isEmpty() || priceField.getText().isEmpty() || heightField.getText().isEmpty() || drawersNoField.getText().isEmpty() || materialChoiceBox.getValue() == null || shapeChoiceBox.getValue() == null)) {
                    Client.sendRequest("addProduct|night_stand|" + nameField.getText() + "|" + priceField.getText() + "|" + heightField.getText() + "|" + drawersNoField.getText() + "|" + materialChoiceBox.getValue() + "|" + shapeChoiceBox.getValue());
                    String response = readResponse();

                    if(response.contains("success")){
                        popup.close();
                        webEngine.executeScript("location.reload()");
                    }
                }

            });

            //create a layout
            VBox layout = new VBox(10);

            layout.getChildren().addAll(nameLabel, nameField, priceLabel, priceField, heightLabel, heightField, drawersNoLabel, drawersNoField, materialLabel, materialChoiceBox, shapeLabel, shapeChoiceBox, addButton);

            layout.setAlignment(Pos.CENTER);

            //display the popup window
            Scene scene = new Scene(layout);
            popup.setScene(scene);
            popup.showAndWait();
        }, false);

        //get products
        Client.sendRequest("getAllProducts");
        String response = readResponse();
        Product[] products = Utils.deserializeProducts(response);
        for (Product product: products) {
            System.out.println(product.getProductName());
        }

        products = getProductSpecs(products);

        System.out.println("Product specs:\n\n");
        //go through the products and send requests to get product specs
        System.out.println(Client.cartProducts);
        addProductsToPage(products, (Element) webEngine.getDocument().getElementById("productsTable"));

        configureDeleteButtons();
        configureModifyButtons();
    }
}

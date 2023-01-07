package org.example.Utils;

import org.example.DataObjects.Chair;
import org.example.DataObjects.NightStand;
import org.example.DataObjects.Product;
import org.example.DataObjects.Table;
import org.example.Main.Client;
import org.w3c.dom.Element;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static String encryptPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes());
        StringBuilder result = new StringBuilder();
        for (byte b : hash) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString().substring(0,49);
    }

    public static String replaceVariable(String variableName){
        return "{\n" +
                "  // Get all the elements on the page\n" +
                "  var elements = document.getElementsByTagName('*');\n" +
                "\n" +
                "  // Loop through all the elements\n" +
                "  for (var i = 0; i < elements.length; i++) {\n" +
                "    // Get the current element\n" +
                "    var element = elements[i];\n" +
                "\n" +
                "    // Loop through all the child nodes of the element\n" +
                "    for (var j = 0; j < element.childNodes.length; j++) {\n" +
                "      // Get the current child node\n" +
                "      var node = element.childNodes[j];\n" +
                "\n" +
                "      // Check if the node is a text node\n" +
                "      if (node.nodeType === 3) {\n" +
                "        // Replace the text content of the node with the updated text\n" +
                "        var text = node.nodeValue;\n" +
                "        var replacedText = text.replace('${" + variableName + "}', '" + Client.sessionVariables.get(variableName) + "');\n" +
                "\n" +
                "        if (replacedText !== text) {\n" +
                "          element.replaceChild(document.createTextNode(replacedText), node);\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String replaceVariable(String variableName, String value){
        return "{\n" +
                "  // Get all the elements on the page\n" +
                "  var elements = document.getElementsByTagName('*');\n" +
                "\n" +
                "  // Loop through all the elements\n" +
                "  for (var i = 0; i < elements.length; i++) {\n" +
                "    // Get the current element\n" +
                "    var element = elements[i];\n" +
                "\n" +
                "    // Loop through all the child nodes of the element\n" +
                "    for (var j = 0; j < element.childNodes.length; j++) {\n" +
                "      // Get the current child node\n" +
                "      var node = element.childNodes[j];\n" +
                "\n" +
                "      // Check if the node is a text node\n" +
                "      if (node.nodeType === 3) {\n" +
                "        // Replace the text content of the node with the updated text\n" +
                "        var text = node.nodeValue;\n" +
                "        var replacedText = text.replace('${" + variableName + "}', '" + value + "');\n" +
                "\n" +
                "        if (replacedText !== text) {\n" +
                "          element.replaceChild(document.createTextNode(replacedText), node);\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String getSelectedValue(String by, String value){
        switch (by){
            case "id":
                return  "document.getElementById(\"" + value + "\").options[document.getElementById(\"" + value + "\").selectedIndex].value;";
            default:
                return null;
        }
    }

    public static Product[] deserializeProducts(String response) {
        String[] entries = response.split("\n");
        Product[] products = new Product[entries.length - 1];

        for (int i = 0; i < entries.length; i++) {
            String[] productData = entries[i].split("\\|");
            if(productData.length > 1){
                String productName = productData[1];
                String productType = productData[2];
                float price = Float.parseFloat(productData[3]);
                int quantity = Integer.parseInt(productData[7]);
                int actualProductId = Math.max(Math.max(Integer.parseInt(productData[4]), Integer.parseInt(productData[5])), Integer.parseInt(productData[6]));

                switch (productType){
                    case "chair":
                        products[i] = new Chair(productName, price, productType, quantity, actualProductId);
                        break;
                    case "table":
                        products[i] = new Table(productName, price, productType, quantity, actualProductId);
                        break;
                    case "night_stand":
                        products[i] = new NightStand(productName, price, productType, quantity, actualProductId);
                        break;
                    default:
                        break;
                }

            }
        }
        return products;
    }

    public static String getCheckboxValue(String by, String value){
        switch (by){
            case "id":
                return  "document.getElementById(\"" + value + "\").checked;";
            default:
                return null;
        }
    }

    public static void clearChildren(Element element){
        while (element.hasChildNodes()) {
            element.removeChild(element.getFirstChild());
        }
    }

    public static List<String> deserializeProductSpec(String response, String productType) {

        //split response by character |
        List<String> productData = List.of(response.split("\\|"));

        switch (productType){
            case "chair":
                //booleans
                return List.of(productData.get(2), productData.get(3), productData.get(4));

            case "table":
                //width, height, length, feet no
                return List.of(productData.get(2), productData.get(3), productData.get(4), productData.get(5));

            case "night_stand":
                //height, no of drawers
                return List.of(productData.get(2), productData.get(3));
        }

        return null;
    }

    public static String getCountOfElements(String by, String value){
        switch (by){
            case "class":
                return "document.getElementsByClassName(\"" + value + "\").length;";
            default:
                return null;
        }
    }

}

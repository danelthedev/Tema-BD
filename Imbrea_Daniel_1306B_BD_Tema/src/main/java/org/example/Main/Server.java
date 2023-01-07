package org.example.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public class Server {

    Boolean running = false;

    JFrame frame;
    JButton startButton;
    JLabel statusLabel;

    ServerSocket server;
    List<Socket> clients;

    private Connection con;
    PreparedStatement ps;

    //setup methods

    //create interface
    private void createInterface() {
        frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);
        //create start button
        startButton = new JButton("Start");
        //create status label
        statusLabel = new JLabel("Not running");

        startButton.addActionListener(e -> {
            if(statusLabel.getText().equals("Not running")) {
                setupNetwork();
                setupDatabase();
                running = true;
                statusLabel.setText("Running");
            }
            else{
                running = false;
                statusLabel.setText("Not running");
                //close all connections
                try {
                    server.close();
                    for(Socket client : clients){
                        client.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        //place the label above the button, centered
        frame.add(statusLabel, "North");
        frame.add(startButton, "Center");
        frame.setVisible(true);

    }

    //connect to database
    private void setupDatabase(){
        // Load the Oracle JDBC driver
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Connect to the database
        try {
            con = DriverManager.getConnection(
                    "jdbc:oracle:thin:@bd-dc.cs.tuiasi.ro:1539:orcl",
                    "bd026",
                    "bd026");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //check if connection is successful
        if (con != null) {
            System.out.println("Server connected to the database!");
        } else {
            System.out.println("Server failed to make connection to database!");
        }

        try {
            if(con != null)
                con.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    //setup server socket
    private void setupNetwork(){
        //allow multiple clients to connect to the server
        clients = new ArrayList<Socket>();

        try {
            server = new ServerSocket(1234);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //server constructor

    private Server(){
        createInterface();
    }

    //network methods

    //when a client connects, add it to the list of clients, and start a new thread for it
    public void handleConnection() {
        try {
            Socket client = server.accept();
            System.out.println("New client connected: " + client.getInetAddress());
            clients.add(client);
            new ClientHandler(client, this).start();
        } catch (IOException e) {
            System.out.println("Socket is closed");
        }

    }

    //handle connections while the server is up
    private void run(){
        handleConnection();
    }

    //database methods

    //run a query and return the result
    public String runSQLQuery(String query) {
        ResultSet rs = null;
        int success = 0;

        String type = query.split(" ")[0];

        //run the query
        try {
            ps = con.prepareStatement(query);

            if(type.toLowerCase().equals("select")) {
                rs = ps.executeQuery();
            }
            else {
                System.out.println("Executing non-select query");
                success = ps.executeUpdate();
                System.out.println("Query status: " + success);
                if (success == 0)
                    return "Unknown error, aborting query\n";

            }
        } catch (SQLException e) {
            return "Error: " + e.getMessage() + "Caused by query: " + query;
        }

        // Package the results into a string if it's a select query
        if(type.equalsIgnoreCase("select")) {
            StringBuilder result = new StringBuilder();
            try {
                while (rs.next()) {
                    //print all columns
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
                        result.append(rs.getString(i)).append("|");
                    result.append("\n");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return result.toString();
        }
        //Otherwise, return the status of the query
        else if(success == 1)
            return "Query executed successfully!";

        return null;
    }

    public void commitTransaction(){
        try {
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //main method

    public static void main(String[] args) throws InterruptedException {
        //create the server
        Server server = new Server();
        //wait for clients to connect
        while(true) {
            Thread.sleep(1);
            if(server.running) {
                server.run();
            }
        }
    }


}

class ClientHandler extends Thread {
    private Socket client;
    private Server queryRunner;
    PrintWriter packetSender;

    public ClientHandler(Socket client, Server queryRunner) {
        this.client = client;
        this.queryRunner = queryRunner;

        try {
            this.packetSender = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void handleRequest(String request) {
        List<String> requestElements = List.of(request.split("\\|"));
        String requestType = requestElements.get(0);
        System.out.println(requestElements);
        String response = "";

        switch (requestType){
            case "login": {
                String userExist = queryRunner.runSQLQuery("SELECT COUNT(*)" +
                        "FROM customers" +
                        " WHERE name = '" + requestElements.get(1) +
                        "' AND password = '" + requestElements.get(2) + "'");

                String userData = queryRunner.runSQLQuery("SELECT * FROM customers WHERE name = '" + requestElements.get(1) + "'" +
                                                            " AND password = '" + requestElements.get(2) + "'");
                if(userExist.contains("1")) {
                    String address_id = userData.split("\\|")[6];
                    userData += queryRunner.runSQLQuery("SELECT * FROM addresses WHERE address_id = " + address_id);
                }

                System.out.println("Sending response: " + userExist.replace("\n", "") + userData);

                response = "User found: " + userExist;
                packetSender.println(response + userExist.replace("\n", "") + userData);
                break;
            }
            case "register": {
                queryRunner.runSQLQuery("INSERT INTO addresses(city, street, zipcode)\n" +
                        "VALUES ('" + requestElements.get(5) +
                        "', '" + requestElements.get(6) +
                        "', '" + requestElements.get(7) + "')");

                queryRunner.commitTransaction();

                //get max address id
                String addressId = queryRunner.runSQLQuery("SELECT MAX(address_id) FROM addresses").replace("|\n", "");
                String registrationStatus = queryRunner.runSQLQuery("INSERT INTO customers (name, password, email, phone_number, address_id, admin)\n" +
                        "VALUES ('" + requestElements.get(1) + "', '" +
                        requestElements.get(2) + "', '" +
                        requestElements.get(3) + "', '" +
                        requestElements.get(4) + "', " +
                        addressId + ", 0)");

                queryRunner.commitTransaction();

                System.out.println("Sending response: " + registrationStatus);

                if (registrationStatus.equals("Query executed successfully!"))
                    packetSender.println("User registered successfully: " + queryRunner.runSQLQuery("SELECT * FROM customers WHERE name = '" + requestElements.get(1) + "'"));
                else
                    packetSender.println("User registration failed! " + registrationStatus);

                break;
            }
            case "getProductId":{
                response = queryRunner.runSQLQuery("SELECT product_id FROM products WHERE product_name = '" + requestElements.get(1) + "'");
                System.out.println("Product id = " + response);
                packetSender.println(response);
                break;
            }
            case "getAllProducts": {
                response = queryRunner.runSQLQuery("SELECT * FROM products");
                System.out.println("Sending response:\n" + response);
                packetSender.println(response);
                break;
            }
            case "getProductSpecs": {
                switch (requestElements.get(1)) {
                    case "tables":
                        response = queryRunner.runSQLQuery("SELECT * FROM tables WHERE table_id = " + requestElements.get(2));
                        break;
                    case "chairs":
                        response = queryRunner.runSQLQuery("SELECT * FROM chairs WHERE chair_id = " + requestElements.get(2));
                        break;
                    case "night_stands":
                        response = queryRunner.runSQLQuery("SELECT * FROM night_stands WHERE night_stand_id = " + requestElements.get(2));
                        break;
                    default:
                        break;
                }

                System.out.println("Sending response:\n" + response);
                packetSender.println(response);
                break;
            }
            case "getProductSpecsById":{
                String productId = requestElements.get(1);
                String productName = queryRunner.runSQLQuery("SELECT product_name FROM products WHERE product_id = " + productId).replace("|\n", "");
                String price = queryRunner.runSQLQuery("SELECT price FROM products WHERE product_id = " + productId).replace("|\n", "");
                //send product name and price
                System.out.println("Sending response: " + productName + "|" + price);
                packetSender.println(productName + "|" + price);
                break;
            }
            case "getProducts":{
                //init request parameters
                String sortMethod = "";
                List<String> productTypes = new ArrayList<>();
                String searchedWord = "";

                for (String element : requestElements) {
                    if (element.contains("sortMethod")) {
                        sortMethod = element.split("=")[1];
                    }
                    if (element.contains("productType")) {
                        //get all product types
                        String PT = element.split("=")[1];
                        //split PT into individual product types
                        productTypes.addAll(List.of(PT.split(",")));
                        //remove empty string if it exists
                        productTypes.removeIf(s -> s.equals(""));
                    }
                    if (element.contains("searchString")) {
                        searchedWord = element.split("=")[1];
                    }
                }

                //sorting

                String[] sortParams = sortMethod.split("/");
                for(int i = 0; i < sortParams.length; i++)
                    System.out.println(sortParams[i]);
                String field = sortParams[0].equals("price") ? "price" : "product_name";
                String direction = sortParams[1];

                //filtering
                System.out.println("Product types: " + productTypes);

                String query="";

                if(productTypes.size() == 0 || productTypes.size() == 3) {
                    query = "SELECT * FROM products";
                    if(!searchedWord.isEmpty())
                        query += " WHERE LOWER(product_name) LIKE LOWER('%" + searchedWord + "%')";
                    query += " ORDER BY " + field + " " + direction;
                    response = queryRunner.runSQLQuery(query);
                }else{
                    for(String productType : productTypes) {
                        String pt = productType.substring(0, productType.length() - 1);
                        query = "SELECT * FROM products WHERE product_type = '" + pt + "'";

                        if(!searchedWord.isEmpty())
                            query += " AND LOWER(product_name) LIKE LOWER('%" + searchedWord + "%')";

                        query += " ORDER BY " + field + " " + direction;
                        response += queryRunner.runSQLQuery(query);
                    }

                }

                System.out.println("Query: " + query);

                System.out.println("Sending response:\n" + response);
                packetSender.println(response);
                break;
            }
            case "finalizeOrder":{
                //get user_id with name as username
                int userId = Integer.parseInt(queryRunner.runSQLQuery("SELECT customer_id FROM customers WHERE name = '" + requestElements.get(1) + "'").replace("|\n", ""));
                //get product list from request
                List<String> productIds = List.of(requestElements.get(2).split(","));

                for(String productId : productIds)
                    queryRunner.runSQLQuery("INSERT INTO orders (customer_id, delivery_date, product_id) VALUES (" + userId + ", SYSDATE + 2, " + productId + ")");

                queryRunner.commitTransaction();
                break;
            }
            case "deleteProduct":{
                String productName = requestElements.get(1);

                //find product id
                System.out.println("SELECT MAX(MAX(chair_id, table_id), night_stand_id), product_type FROM products WHERE product_name = '" + productName + "'");
                String productId = queryRunner.runSQLQuery("SELECT GREATEST(chair_id, table_id, night_stand_id), product_type FROM products WHERE product_name = '" + productName + "'").replace("|\n", "");
                System.out.println("Product id = " + productId);
                //delete product from products table
                System.out.println("DELETE FROM products WHERE product_name = '" + productName + "'");
                response += queryRunner.runSQLQuery("DELETE FROM products WHERE product_name = '" + productName + "'");
                System.out.println(response);
                //delete product from its table
                System.out.println("DELETE FROM " + productId.split("\\|")[1] + "s WHERE " + productId.split("\\|")[1] + "_id = " + productId.split("\\|")[0]);
                response = queryRunner.runSQLQuery("DELETE FROM " + productId.split("\\|")[1] + "s WHERE " + productId.split("\\|")[1] + "_id = " + productId.split("\\|")[0]);
                System.out.println(response);


                queryRunner.commitTransaction();

                System.out.println("Sending response:\n" + response);
                packetSender.println(response);

                break;
            }
            case "getTable": {
                String tableName = requestElements.get(1);
                response = queryRunner.runSQLQuery("SELECT * FROM " + tableName);
                System.out.println("Sending response:\n" + response);
                packetSender.println(response);
                break;
            }
            case "addProduct": {
                String query;

                String productType = requestElements.get(1);
                String productName = requestElements.get(2);
                String productPrice = requestElements.get(3);

                //depending on product type, get its parameters and add product to its table
                switch (productType){
                    case "chair": {
                        String hidraulic = requestElements.get(4).contains("true") ? "1" : "0";
                        String lombarSupport = requestElements.get(5).contains("true") ? "1" : "0";
                        String wheels = requestElements.get(6).contains("true") ? "1" : "0";
                        String material = requestElements.get(7);

                        //insert in individual table
                        String materialId = queryRunner.runSQLQuery("SELECT material_id FROM materials WHERE material_name = '" + material + "'").replace("|\n", "");
                        query = "INSERT INTO chairs (chair_name, hidraulic, lombar_support, wheels, material_id) VALUES ('" + productName + "', " + hidraulic + ", " + lombarSupport + ", " + wheels + ", " + materialId + ")";

                        System.out.println(query);
                        response = queryRunner.runSQLQuery(query);
                        System.out.println("Response:\n" + response);

                        //if the response is successful, insert in products table
                        if(response.contains("Query executed successfully!")){
                            String chairId = queryRunner.runSQLQuery("SELECT chair_id FROM chairs WHERE chair_name = '" + productName + "'").replace("|\n", "");
                            query = "INSERT INTO products (product_name, product_type, price, chair_id, quantity) VALUES ('" + productName + "', '" + productType + "', " + productPrice + ", " + chairId + ", 1)";
                            System.out.println(query);
                            response = queryRunner.runSQLQuery(query);
                            System.out.println("Sending response:\n" + response);
                            packetSender.println(response);
                            if(!response.contains("Query executed successfully!"))
                                queryRunner.runSQLQuery("DELETE FROM chairs WHERE chair_name = '" + productName + "'");
                        }
                        else{
                            packetSender.println(response);
                        }

                        break;
                    }
                    case "table":{
                        String width = requestElements.get(4);
                        String length = requestElements.get(5);
                        String height = requestElements.get(6);
                        String feetNo = requestElements.get(7);
                        String material = requestElements.get(8);
                        String shape = requestElements.get(9);

                        String materialId = queryRunner.runSQLQuery("SELECT material_id FROM materials WHERE material_name = '" + material + "'").replace("|\n", "");
                        String shapeId = queryRunner.runSQLQuery("SELECT shape_id FROM shapes WHERE shape_name = '" + shape + "'").replace("|\n", "");

                        query = "INSERT INTO tables (table_name, width, length, height, feet_no, material_id, shape_id) VALUES ('" + productName + "', " + width + ", " + length + ", " + height + ", " + feetNo + ", " + materialId + ", " + shapeId + ")";
                        System.out.println(query);
                        response = queryRunner.runSQLQuery(query);
                        System.out.println("Response:\n" + response);

                        if(response.contains("Query executed successfully!")){
                            String tableId = queryRunner.runSQLQuery("SELECT table_id FROM tables WHERE table_name = '" + productName + "'").replace("|\n", "");
                            query = "INSERT INTO products (product_name, product_type, price, table_id, quantity) VALUES ('" + productName + "', '" + productType + "', " + productPrice + ", " + tableId + ", 1)";
                            System.out.println(query);
                            response = queryRunner.runSQLQuery(query);
                            System.out.println("Sending response:\n" + response);
                            packetSender.println(response);
                            if(!response.contains("Query executed successfully!"))
                                queryRunner.runSQLQuery("DELETE FROM tables WHERE table_name = '" + productName + "'");
                        }
                        else{
                            packetSender.println(response);
                        }
                        break;
                    }
                    case "night_stand":{
                        String height = requestElements.get(4);
                        String drawersNo = requestElements.get(5);
                        String material = requestElements.get(6);
                        String shape = requestElements.get(7);

                        String materialId = queryRunner.runSQLQuery("SELECT material_id FROM materials WHERE material_name = '" + material + "'").replace("|\n", "");
                        String shapeId = queryRunner.runSQLQuery("SELECT shape_id FROM shapes WHERE shape_name = '" + shape + "'").replace("|\n", "");

                        query = "INSERT INTO night_stands (night_stand_name, height, drawers_no, material_id, shape_id) VALUES ('" + productName + "', " + height + ", " + drawersNo + ", " + materialId + ", " + shapeId + ")";
                        System.out.println(query);
                        response = queryRunner.runSQLQuery(query);
                        System.out.println("Response:\n" + response);

                        if(response.contains("Query executed successfully!")){
                            String nightStandId = queryRunner.runSQLQuery("SELECT night_stand_id FROM night_stands WHERE night_stand_name = '" + productName + "'").replace("|\n", "");
                            query = "INSERT INTO products (product_name, product_type, price, night_stand_id, quantity) VALUES ('" + productName + "', '" + productType + "', " + productPrice + ", " + nightStandId + ", 1)";
                            System.out.println(query);
                            response = queryRunner.runSQLQuery(query);
                            System.out.println("Sending response:\n" + response);
                            packetSender.println(response);
                            if(!response.contains("Query executed successfully!"))
                                queryRunner.runSQLQuery("DELETE FROM night_stands WHERE night_stand_name = '" + productName + "'");
                        }
                        else{
                            packetSender.println(response);
                        }
                        break;
                    }
                }


                queryRunner.commitTransaction();

                break;
            }
            case "getProductType":{
                String productName = requestElements.get(1);
                String productType = queryRunner.runSQLQuery("SELECT product_type FROM products WHERE product_name = '" + productName + "'").replace("|\n", "");
                System.out.println(productType);
                packetSender.println(productType);
                break;
            }
            case "modifyProduct":{
                String productType = requestElements.get(1);
                String productName = requestElements.get(2);
                String productChanges = requestElements.get(3);
                //split product changes into a list at
                List<String> changes = List.of(productChanges.split(","));

                String modifiedName = "", modifiedPrice = "", modifiedQuantity = "", modifiedMaterial = "", modifiedMaterialId = "";

                //check if an element from changes contains "name="
                for(String change : changes){
                    if(change.contains("name=")){
                        modifiedName = change.replace("name=", "");
                    }
                    else if(change.contains("price=")){
                        modifiedPrice = change.replace("price=", "");
                    }
                    else if(change.contains("quantity=")){
                        modifiedQuantity = change.replace("quantity=", "");
                    }
                    else if(change.contains("material=")){
                        modifiedMaterial = change.replace("material=", "");
                        modifiedMaterialId = queryRunner.runSQLQuery("SELECT material_id FROM materials WHERE material_name = '" + modifiedMaterial + "'").replace("|\n", "");
                    }
                }

                switch(productType){
                    case "chair":{
                        String modifiedHidraulic = "", modifiedLombarSupport = "", modifiedWheels = "";

                        for(String change : changes){
                            if(change.contains("hidraulic=")){
                                modifiedHidraulic = change.replace("hidraulic=", "");
                            }
                            else if(change.contains("lombar_support=")){
                                modifiedLombarSupport = change.replace("lombar_support=", "");
                            }
                            else if(change.contains("wheels=")){
                                modifiedWheels = change.replace("wheels=", "");
                            }
                        }

                        String query = "UPDATE products SET";
                        if(!modifiedName.equals(""))
                            query += " product_name = '" + modifiedName + "',";
                        if(!modifiedPrice.equals(""))
                            query += " price = " + modifiedPrice + ",";
                        if(!modifiedQuantity.equals(""))
                            query += " quantity = " + modifiedQuantity + ",";
                        query = query.substring(0, query.length() - 1);

                        response = queryRunner.runSQLQuery(query + " WHERE product_name = '" + productName + "'");

                        if(response.contains("Query executed successfully!")){
                            query = "UPDATE chairs SET";
                            if (!modifiedName.equals(""))
                                query += " chair_name = '" + modifiedName + "',";
                            if (!modifiedMaterialId.equals(""))
                                query += " material_id = " + modifiedMaterialId + ",";
                            if (!modifiedHidraulic.equals(""))
                                query += " hidraulic = " + modifiedHidraulic + ",";
                            if (!modifiedLombarSupport.equals(""))
                                query += " lombar_support = " + modifiedLombarSupport + ",";
                            if (!modifiedWheels.equals(""))
                                query += " wheels = " + modifiedWheels + ",";
                            query = query.substring(0, query.length() - 1);

                            response = queryRunner.runSQLQuery(query + " WHERE chair_name = '" + productName + "'");
                            packetSender.println(response);

                            if(response.contains("Query executed successfully!")) {
                                queryRunner.commitTransaction();
                                System.out.println("Modified product!");
                            }
                        }else{
                            packetSender.println(response);
                        }

                        break;
                    }
                    case "night_stand":{
                        String modifiedHeight = "", modifiedDrawersNo = "", modifiedShape = "", modifiedShapeId = "";

                        for(String change : changes){
                            if(change.contains("height=")){
                                modifiedHeight = change.replace("height=", "");
                            }
                            else if(change.contains("drawersNo=")){
                                modifiedDrawersNo = change.replace("drawersNo=", "");
                            }
                            else if(change.contains("shape=")){
                                modifiedShape = change.replace("shape=", "");
                                modifiedShapeId = queryRunner.runSQLQuery("SELECT shape_id FROM shapes WHERE shape_name = '" + modifiedShape + "'").replace("|\n", "");
                            }
                        }

                        String query = "UPDATE products SET";
                        if(!modifiedName.equals(""))
                            query += " product_name = '" + modifiedName + "',";
                        if(!modifiedPrice.equals(""))
                            query += " price = " + modifiedPrice + ",";
                        if(!modifiedQuantity.equals(""))
                            query += " quantity = " + modifiedQuantity + ",";
                        query = query.substring(0, query.length() - 1);

                        response = queryRunner.runSQLQuery(query + " WHERE product_name = '" + productName + "'");

                        if(response.contains("Query executed successfully!")) {
                            query = "UPDATE night_stands SET";
                            if (!modifiedName.equals(""))
                                query += " night_stand_name = '" + modifiedName + "',";
                            if (!modifiedMaterialId.equals(""))
                                query += " material_id = " + modifiedMaterialId + ",";
                            if (!modifiedHeight.equals(""))
                                query += " height = " + modifiedHeight + ",";
                            if (!modifiedDrawersNo.equals(""))
                                query += " drawers_no = " + modifiedDrawersNo + ",";
                            if (!modifiedShapeId.equals(""))
                                query += " shape_id = " + modifiedShapeId + ",";
                            query = query.substring(0, query.length() - 1);

                            response = queryRunner.runSQLQuery(query + " WHERE night_stand_name = '" + productName + "'");
                            packetSender.println(response);

                            if (response.contains("Query executed successfully!")) {
                                queryRunner.commitTransaction();
                                System.out.println("Modified product!");
                            }
                        }else{
                            packetSender.println(response);
                        }

                        break;
                    }
                    case "table":{
                        String modifiedHeight = "", modifiedWidth = "", modifiedLength = "", modifiedShape = "", modifiedShapeId = "", modifiedFeetNo = "";

                        for(String change : changes){
                            if(change.contains("height=")){
                                modifiedHeight = change.replace("height=", "");
                            }
                            else if(change.contains("width=")){
                                modifiedWidth = change.replace("width=", "");
                            }
                            else if(change.contains("length=")){
                                modifiedLength = change.replace("length=", "");
                            }
                            else if(change.contains("shape=")){
                                modifiedShape = change.replace("shape=", "");
                                modifiedShapeId = queryRunner.runSQLQuery("SELECT shape_id FROM shapes WHERE shape_name = '" + modifiedShape + "'").replace("|\n", "");
                            }
                            else if(change.contains("feetNo=")){
                                modifiedFeetNo = change.replace("feetNo=", "");
                            }
                        }

                        String query = "UPDATE products SET";
                        if(!modifiedName.equals(""))
                            query += " product_name = '" + modifiedName + "',";
                        if(!modifiedPrice.equals(""))
                            query += " price = " + modifiedPrice + ",";
                        if(!modifiedQuantity.equals(""))
                            query += " quantity = " + modifiedQuantity + ",";
                        query = query.substring(0, query.length() - 1);

                        response = queryRunner.runSQLQuery(query + " WHERE product_name = '" + productName + "'");

                        if(response.contains("Query executed successfully!")) {
                            query = "UPDATE tables SET";
                            if (!modifiedName.equals(""))
                                query += " table_name = '" + modifiedName + "',";
                            if (!modifiedMaterialId.equals(""))
                                query += " material_id = " + modifiedMaterialId + ",";
                            if (!modifiedHeight.equals(""))
                                query += " height = " + modifiedHeight + ",";
                            if (!modifiedWidth.equals(""))
                                query += " width = " + modifiedWidth + ",";
                            if (!modifiedLength.equals(""))
                                query += " length = " + modifiedLength + ",";
                            if (!modifiedShapeId.equals(""))
                                query += " shape_id = " + modifiedShapeId + ",";
                            if (!modifiedFeetNo.equals(""))
                                query += " feet_no = " + modifiedFeetNo + ",";
                            query = query.substring(0, query.length() - 1);

                            response = queryRunner.runSQLQuery(query + " WHERE table_name = '" + productName + "'");
                            System.out.println(response);
                            packetSender.println(response);

                            if (response.contains("Query executed successfully!")) {
                                queryRunner.commitTransaction();
                                System.out.println("Modified product!");
                            }

                        }else{
                            packetSender.println(response);
                        }

                        break;
                    }
                }

                break;
            }
        }

    }

    public void run() {
        //if the client sends a packet, handle it
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while(true) {
                String request = in.readLine();
                System.out.println("Received request: " + request);
                if(request != null)
                    handleRequest(request);
                else {
                    System.out.println("Client disconnected!");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

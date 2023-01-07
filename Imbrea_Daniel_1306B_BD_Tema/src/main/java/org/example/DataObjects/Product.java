package org.example.DataObjects;

import java.util.List;

public class Product {
    String productName;
    float price;
    String productType;
    int quantity;
    int actualProductId;

    public Product(String actualProductId, String productName, float price){
        this.productName = productName;
        this.price = price;
        this.actualProductId = Integer.parseInt(actualProductId);
    }

    public Product(String productName, float price){
        this.productName = productName;
        this.price = price;
    }

    public Product(String productName, float price, String productType, int quantity, int actualProductId) {
        this.productName = productName;
        this.price = price;
        this.productType = productType;
        this.quantity = quantity;
        this.actualProductId = actualProductId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getActualProductId(){
        return actualProductId;
    }

}

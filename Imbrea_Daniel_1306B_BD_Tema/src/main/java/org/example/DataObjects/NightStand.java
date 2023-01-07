package org.example.DataObjects;

import java.util.List;

public class NightStand extends Product {
    float height;
    int drawersNo;

    public NightStand(String productName, float price, String productType, int quantity, int actualProductId, float height, int drawersNo) {
        super(productName, price, productType, quantity, actualProductId);
        this.height = height;
        this.drawersNo = drawersNo;
    }

    public NightStand(String productName, float price, String productType, int quantity, int actualProductId) {
        super(productName, price, productType, quantity, actualProductId);
    }

    //getters

    public String getHeight() {
        return height + "cm";
    }

    public String getDrawersNo() {
        return String.valueOf(drawersNo);
    }

    //setters

    public void setHeight(float height) {
        this.height = height;
    }

    public void setDrawersNo(int drawersNo) {
        this.drawersNo = drawersNo;
    }

}

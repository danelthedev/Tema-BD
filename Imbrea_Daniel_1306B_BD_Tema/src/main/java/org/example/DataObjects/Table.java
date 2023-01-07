package org.example.DataObjects;

import java.util.List;

public class Table extends Product {
    int length, width, height, feetNo;

    public Table(String productName, float price, String productType, int quantity, int actualProductId, int length, int width, int height, int feetNo) {
        super(productName, price, productType, quantity, actualProductId);
        this.length = length;
        this.width = width;
        this.height = height;
        this.feetNo = feetNo;
    }

    public Table(String productName, float price, String productType, int quantity, int actualProductId) {
        super(productName, price, productType, quantity, actualProductId);
    }

    public String getDimensions() {
        return length + "cm x " + width + "cm x " + height + "cm";
    }

    public String getFeetNo() {
        return String.valueOf(feetNo);
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setFeetNo(int feetNo) {
        this.feetNo = feetNo;
    }


}

package org.example.DataObjects;

import java.util.List;

public class Chair extends Product {
    Boolean hidraulic, lombarSupport, wheels;

    public Chair(String productName, float price, String productType, int quantity, int actualProductId, Boolean hidraulic, Boolean lombarSupport, Boolean wheels) {
        super(productName, price, productType, quantity, actualProductId);
        this.hidraulic = hidraulic;
        this.lombarSupport = lombarSupport;
        this.wheels = wheels;
    }

    public Chair(String productName, float price, String productType, int quantity, int actualProductId) {
        super(productName, price, productType, quantity, actualProductId);
    }

    public String getHidraulic() {
        return String.valueOf(hidraulic);
    }

    public String getLombarSupport() {
        return String.valueOf(lombarSupport);
    }

    public String getWheels() {
        return String.valueOf(wheels);
    }


    public void setHidraulic(Boolean hidraulic) {
        this.hidraulic = hidraulic;
    }

    public void setLombarSupport(Boolean lombarSupport) {
        this.lombarSupport = lombarSupport;
    }

    public void setWheels(Boolean wheels) {
        this.wheels = wheels;
    }

}

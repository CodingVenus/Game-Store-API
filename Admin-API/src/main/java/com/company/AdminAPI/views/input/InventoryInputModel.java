package com.company.AdminAPI.views.input;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Objects;

public class InventoryInputModel {

    private int inventoryId;
    @Positive(message = "ProductId should be positive!")
    private int productId;
    @PositiveOrZero(message = "Quantity should be positive or zero!")
    private int quantity;


    @Override
    public int hashCode() {
        return Objects.hash(getInventoryId(), getProductId(), getQuantity());
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryInputModel that = (InventoryInputModel) o;
        return getInventoryId() == that.getInventoryId() &&
                getProductId() == that.getProductId() &&
                getQuantity() == that.getQuantity();
    }
}
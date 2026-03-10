// ═══════════════════════════════════════════════════════════
// FILE 2: dto/CartItem.java
// Create this if it doesn't exist already
// ═══════════════════════════════════════════════════════════

package com.smartbake.backend.dto;

import com.smartbake.backend.entity.Product;
import java.io.Serializable;

public class CartItem implements Serializable {

    private Product product;
    private int quantity;

    public CartItem() {}

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getSubtotal() {
        return product != null ? product.getPrice() * quantity : 0;
    }
}

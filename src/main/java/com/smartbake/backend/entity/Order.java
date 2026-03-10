// ═══════════════════════════════════════════════════════════
// FILE 1: entity/Order.java — CRITICAL FIX
// Replace your entire Order.java with this
// ═══════════════════════════════════════════════════════════

package com.smartbake.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private double totalAmount;
    private String status = "PENDING";
    private LocalDateTime createdAt = LocalDateTime.now();
    private String paymentMethod;
    private String paybillNumber = "0716624266";
    private String riderName;
    private String riderPhone;
    private String riderBikePlate;
    private Double deliveryFee;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerPhone;

    private String deliveryAddress;
    private String notes;

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    // ← FIXED: was throwing UnsupportedOperationException!
    public void setUser(User user) { this.user = user; }
    public User getUser() { return user; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaybillNumber() { return paybillNumber; }
    public void setPaybillNumber(String paybillNumber) { this.paybillNumber = paybillNumber; }
    public String getRiderName() { return riderName; }
    public void setRiderName(String riderName) { this.riderName = riderName; }
    public String getRiderPhone() { return riderPhone; }
    public void setRiderPhone(String riderPhone) { this.riderPhone = riderPhone; }
    public String getRiderBikePlate() { return riderBikePlate; }
    public void setRiderBikePlate(String riderBikePlate) { this.riderBikePlate = riderBikePlate; }
    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

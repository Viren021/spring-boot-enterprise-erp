package com.example.erp_service_inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id") // <-- THIS IS THE FIX!
    private Long productId;

    private Integer quantity;
    private String status;
}
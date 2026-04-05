package com.miapp.inventory_system.products.infrastructure.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class ProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 150)
    private String description;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "purchase_price")
    private BigDecimal purchasePrice;

    @Column(name = "sale_price")
    private BigDecimal salePrice;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
